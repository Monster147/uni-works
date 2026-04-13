import java.nio.file.*;
import java.security.MessageDigest;

public class H16 {
    public static byte[] h16OfBytes(byte[] data) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(data);
        return new byte[]{digest[0], digest[1]};
    }

    public static byte[] h16OfFile(Path p) throws Exception{
        byte[] data = Files.readAllBytes(p);
        return h16OfBytes(data);
    }

    public static String hex(byte[] b){
        StringBuilder sb = new StringBuilder();
        for (byte x : b) sb.append(String.format("%02x", x & 0xff));
        return sb.toString();
    }
}