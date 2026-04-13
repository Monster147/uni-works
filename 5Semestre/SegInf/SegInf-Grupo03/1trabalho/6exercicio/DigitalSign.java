import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Enumeration;

public class DigitalSign {
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            usage();
            return;
        }
        String cmd = args[0];
        String hashFlag = args[1];
        String algorithm = switch (hashFlag) {
            case "-sha1" -> "SHA1withRSA";
            case "-sha256" -> "SHA256withRSA";
            default -> {
                System.out.println("Função de hash inválida. Use -sha1 ou -sha256.");
                yield null;
            }
        };
        switch (cmd) {

            case "-sign" -> {
                if (args.length != 6) {
                    usage();
                    return;
                }
                signFile(algorithm, args[2], args[3], args[4].toCharArray(), args[5]);
                System.out.println("Ficheiro assinado em: " + args[5]);
            }
            
            case "-verify" -> {
                if (args.length != 7) {
                    usage();
                    return;
                }
                boolean valid = verifySignature(algorithm, args[2], args[3], args[4], args[5], args[6].toCharArray());
                System.out.println("Assinatura: " + (valid ? "VÁLIDA" : "INVÁLIDA"));
            }

            default -> usage();
        }
    }

    private static void usage() {
        System.out.println("""
            Uso:
              -sign   -sha1|-sha256 <inputFile> <keystore.pfx> <password> <outputSignature>
              -verify -sha1|-sha256 <inputFile> <signature> <targetCertificate.cer> <truststore.jks> <password>
            """);
    }

    private static void signFile(String algorithm, String inputFile, String keystore, char[] password, String outputFile) throws Exception {
        KeyStore ks = KeyStore.getInstance("PKCS12");
        try (FileInputStream fis = new FileInputStream(keystore)) {
            ks.load(fis, password);
        }

        PrivateKey privateKey = null;
        Enumeration<String> aliases = ks.aliases();
        while (aliases.hasMoreElements()) {
            String currentAlias = aliases.nextElement();
            if (ks.isKeyEntry(currentAlias)) {
                Key key = ks.getKey(currentAlias, password);
                if (key instanceof PrivateKey) {
                    privateKey = (PrivateKey) key;
                    break;
                }
            }
        }

        Signature signature = Signature.getInstance(algorithm);
        signature.initSign(privateKey);
        try (InputStream is = new BufferedInputStream(Files.newInputStream(Path.of(inputFile)))) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                signature.update(buffer, 0, bytesRead);
            }
        }
        byte[] digitalSignature = signature.sign();
        Files.write(Path.of(outputFile), digitalSignature);
        System.out.println("Arquivo assinado com sucesso.");
    }


    private static boolean verifySignature(String algorithm, String inputFile, String signatureFile, String certificateFile, String truststorePath, char[] password) throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate signerCert;
        try (InputStream is = Files.newInputStream(Path.of(certificateFile))) {
            signerCert = (X509Certificate) cf.generateCertificate(is);
        }

        KeyStore truststore = KeyStore.getInstance("JKS");
        try (InputStream fis = Files.newInputStream(Path.of(truststorePath))) {
            truststore.load(fis, password);
        }

        List<X509Certificate> intermediates = new ArrayList<>();
        Path intermediatesDir = Path.of("certificates-keys/intermediates");
        if (Files.exists(intermediatesDir) && Files.isDirectory(intermediatesDir)) {
            try (var stream = Files.newDirectoryStream(intermediatesDir, "*.cer")) {
                for (Path p : stream) {
                    try (InputStream is = Files.newInputStream(p)) {
                        intermediates.add((X509Certificate) cf.generateCertificate(is));
                    }
                }
            }
        }

        List<X509Certificate> allCerts = new ArrayList<>();
        allCerts.add(signerCert);
        allCerts.addAll(intermediates);

        CertStore intermediateStore = CertStore.getInstance("Collection", new CollectionCertStoreParameters(allCerts));

        X509CertSelector selector = new X509CertSelector();
        selector.setCertificate(signerCert);

        PKIXBuilderParameters pkixParams = new PKIXBuilderParameters(truststore, selector);
        pkixParams.addCertStore(intermediateStore);
        pkixParams.setRevocationEnabled(false);

        try {
            CertPathBuilder builder = CertPathBuilder.getInstance("PKIX");
            builder.build(pkixParams);
            System.out.println("Cadeia de certificação válida.");
        } catch (CertPathBuilderException e) {
            System.out.println("Cadeia inválida: " + e.getMessage());
            return false;
        }

        byte[] sigBytes = Files.readAllBytes(Path.of(signatureFile));
        Signature sig = Signature.getInstance(algorithm);
        sig.initVerify(signerCert.getPublicKey());

        try (InputStream is = Files.newInputStream(Path.of(inputFile))) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1)
                sig.update(buffer, 0, bytesRead);
        }

        boolean valid = sig.verify(sigBytes);
        System.out.println(valid
                ? "Assinatura VÁLIDA e certificado confiável."
                : "Assinatura INVÁLIDA.");
        return valid;
    }
}
