import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

public class CollisonFinder {
    private static final Path GOOD = Paths.get("GoodApp.java");
    private static final Path TEMPLATE = Paths.get("BadApp.java");
    private static final Path OUT = Paths.get("BadApp_candidate.java");

    public static void main(String[] args) throws Exception {
        long start = System.nanoTime();
        byte[] target = H16.h16OfFile(GOOD);
        System.out.println("Target H16 (GoodApp): " + H16.hex(target));

        String template = Files.readString(TEMPLATE);
        long tries = 0;
        for (int nonce = 0; nonce <= 0xFFFFF; nonce++) {
            String candidateSource = injectNonce(template, nonce);
            Files.writeString(OUT, candidateSource);
            byte[] h = H16.h16OfFile(OUT);
            tries++;
            if (h[0] == target[0] && h[1] == target[1]) {
                long end = System.nanoTime();
                double seconds = (end - start) / 1000000000.0;
                System.out.printf(Locale.ROOT,
                        "Collision found after %d tries. nonce=%d, H16=%s, in %.3f seconds.%n%n",
                        tries, nonce, H16.hex(h), seconds);
                System.out.println("Candidate written to: " + OUT.toAbsolutePath());
                return;
            }
            if ((nonce & 0x3FFF) == 0) {
                System.out.println("Tried: " + tries + " nonce=" + nonce + " current H16=" + H16.hex(h));
            }
        }
        long end = System.nanoTime();
        double seconds = (end - start) / 1000000000.0;
        System.out.printf(
                "Finished search without finding collision. Total time: %.3f seconds.%n", seconds);
    }

    private static String injectNonce(String template, int nonce) {
        String comment = String.format("// nonce=%d\n", nonce);
        int idx = template.indexOf("public class");
        if (idx == -1) {
            return comment + template;
        }
        return template.substring(0, idx) + comment + template.substring(idx);
    }
}
