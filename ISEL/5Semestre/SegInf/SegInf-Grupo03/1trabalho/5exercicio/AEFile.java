import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AEFile {
    private static final String CIPHER_TRANSFORM = "AES/CBC/PKCS5Padding";
    private static final String HMAC_ALGO = "HmacSHA256";
    private static final int AES_KEY_BITS = 256;
    private static final int HMAC_KEY_BYTES = 32; // 256-bit HMAC key
    private static final int IV_BYTES = 16;
    private static final int HMAC_TAG_BYTES = 32;

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            usage();
            return;
        }
        String cmd = args[0];
        switch (cmd) {

            case "-genkey" -> {
                if (args.length != 2) { usage(); return; }
                generateKeyFile(Paths.get(args[1]));
                System.out.println("Chave gerada em: " + args[1]);
            }

            case "-cipher" -> {
                if (args.length != 4) { usage(); return; }
                encryptFile(Paths.get(args[1]), Paths.get(args[2]), Paths.get(args[3]));
                System.out.println("Ficheiro cifrado em: " + args[3]);
            }

            case "-decipher" -> {
                if (args.length != 4) { usage(); return; }
                boolean ok = decryptFile(Paths.get(args[1]), Paths.get(args[2]), Paths.get(args[3]));
                System.out.println("Autenticidade: " + (ok ? "AUTÊNTICA" : "NÃO AUTÊNTICA"));
                if (!ok) {
                    System.out.println("Nota: o ficheiro de saída pode estar inválido se a MAC falhar.");
                } else {
                    System.out.println("Ficheiro decifrado em: " + args[3]);
                }
            }

            default -> usage();
        }
    }

    private static void usage() {
        System.out.println("Uso:");
        System.out.println("  -genkey <keyfile>");
        System.out.println("  -cipher <inputFile> <keyfile> <outputBase64File>");
        System.out.println("  -decipher <inputBase64File> <keyfile> <outputFile>");
    }

    private static void generateKeyFile(Path keyFile) throws Exception {
        SecureRandom sr = SecureRandom.getInstanceStrong();
        KeyGenerator kg = KeyGenerator.getInstance("AES");
        kg.init(AES_KEY_BITS, sr);
        SecretKey aesKey = kg.generateKey();

        byte[] hmacKeyBytes = new byte[HMAC_KEY_BYTES];
        sr.nextBytes(hmacKeyBytes);

        String aesB64 = Base64.getEncoder().encodeToString(aesKey.getEncoded());
        String hmacB64 = Base64.getEncoder().encodeToString(hmacKeyBytes);

        String out = aesB64 + System.lineSeparator() + hmacB64 + System.lineSeparator();
        Files.write(keyFile, out.getBytes("UTF-8"), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private static SecretKeySpec[] loadKeys(Path keyFile) throws Exception {
        // retorna [aesKeySpec, hmacKeySpec]
        byte[] lines = Files.readAllBytes(keyFile);
        String s = new String(lines, "UTF-8").trim();
        String[] parts = s.split("\\r?\\n");
        if (parts.length < 2) throw new IllegalArgumentException("Key file must contain two Base64 lines: AESkey then HMACkey");
        byte[] aesBytes = Base64.getDecoder().decode(parts[0].trim());
        byte[] hmacBytes = Base64.getDecoder().decode(parts[1].trim());
        SecretKeySpec aes = new SecretKeySpec(aesBytes, "AES");
        SecretKeySpec hmac = new SecretKeySpec(hmacBytes, HMAC_ALGO);
        return new SecretKeySpec[]{aes, hmac};
    }

    private static void encryptFile(Path inFile, Path keyFile, Path outBase64File) throws Exception {
        SecretKeySpec[] keys = loadKeys(keyFile);
        SecretKeySpec aesKey = keys[0];
        SecretKeySpec hmacKey = keys[1];

        SecureRandom sr = SecureRandom.getInstanceStrong();
        byte[] iv = new byte[IV_BYTES];
        sr.nextBytes(iv);

        Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORM);
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, new IvParameterSpec(iv));

        Mac mac = Mac.getInstance(HMAC_ALGO);
        mac.init(hmacKey);

        try (InputStream fis = Files.newInputStream(inFile);
             OutputStream fos = Files.newOutputStream(outBase64File, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
             OutputStream b64Out = Base64.getEncoder().wrap(fos)) {

            b64Out.write(iv);
            mac.update(iv);

            byte[] inBuf = new byte[8192];
            int read;
            while ((read = fis.read(inBuf)) != -1) {
                byte[] ct = cipher.update(inBuf, 0, read);
                if (ct != null && ct.length > 0) {
                    b64Out.write(ct);
                    mac.update(ct);
                }
            }
            byte[] finalCt = cipher.doFinal();
            if (finalCt != null && finalCt.length > 0) {
                b64Out.write(finalCt);
                mac.update(finalCt);
            }

            byte[] tag = mac.doFinal();
            b64Out.write(tag);
            b64Out.flush();
        }
    }

    private static boolean decryptFile(Path inBase64File, Path keyFile, Path outFile) throws Exception {
        SecretKeySpec[] keys = loadKeys(keyFile);
        SecretKeySpec aesKey = keys[0];
        SecretKeySpec hmacKey = keys[1];

        Path temp = Files.createTempFile("aefile_raw_", ".bin");
        temp.toFile().deleteOnExit();
        try (InputStream fis = Files.newInputStream(inBase64File);
             InputStream b64In = Base64.getDecoder().wrap(fis);
             OutputStream tmpOut = Files.newOutputStream(temp, StandardOpenOption.TRUNCATE_EXISTING)) {

            byte[] buf = new byte[8192];
            int r;
            while ((r = b64In.read(buf)) != -1) tmpOut.write(buf, 0, r);
        }

        long total = Files.size(temp);
        if (total < IV_BYTES + HMAC_TAG_BYTES) {
            throw new IllegalArgumentException("Ficheiro demasiado pequeno para conter IV + TAG");
        }

        try (RandomAccessFile raf = new RandomAccessFile(temp.toFile(), "r")) {
            byte[] iv = new byte[IV_BYTES];
            raf.readFully(iv);

            long ciphertextLen = total - IV_BYTES - HMAC_TAG_BYTES;
            byte[] expectedTag = new byte[HMAC_TAG_BYTES];
            raf.seek(IV_BYTES + ciphertextLen);
            raf.readFully(expectedTag);

            Mac mac = Mac.getInstance(HMAC_ALGO);
            mac.init(hmacKey);
            mac.update(iv);

            raf.seek(IV_BYTES);
            byte[] buffer = new byte[8192];
            long remaining = ciphertextLen;
            while (remaining > 0) {
                int toRead = (int)Math.min(buffer.length, remaining);
                int got = raf.read(buffer, 0, toRead);
                if (got <= 0) throw new EOFException();
                mac.update(buffer, 0, got);
                remaining -= got;
            }
            byte[] calcTag = mac.doFinal();

            boolean authOK = MessageDigest.isEqual(calcTag, expectedTag);

            if (!authOK) {
                return false;
            }

            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORM);
            cipher.init(Cipher.DECRYPT_MODE, aesKey, new IvParameterSpec(iv));

            raf.seek(IV_BYTES);
            try (OutputStream fos = Files.newOutputStream(outFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                long remain = ciphertextLen;
                while (remain > 0) {
                    int toRead = (int)Math.min(buffer.length, remain);
                    int got = raf.read(buffer, 0, toRead);
                    if (got <= 0) throw new EOFException();
                    byte[] pt = cipher.update(buffer, 0, got);
                    if (pt != null && pt.length > 0) fos.write(pt);
                    remain -= got;
                }
                byte[] finalPt = cipher.doFinal();
                if (finalPt != null && finalPt.length > 0) fos.write(finalPt);
            }
            return true;
        } finally {
            try {
                Files.deleteIfExists(temp);
            } catch (IOException e) {
            }
        }
    }
}
