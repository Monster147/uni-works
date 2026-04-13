import javax.net.ssl.*;
import java.io.*;
import java.security.KeyStore;

public class HttpsClient {
    public static void main(String[] args) throws Exception {

        // Carregar truststore com a CA do servidor
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        try (FileInputStream fis = new FileInputStream("roots.jks")) {
            trustStore.load(fis, "changeit".toCharArray());
        }

        TrustManagerFactory tmf =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);

        // Criar SSLContext que confia nessa CA
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tmf.getTrustManagers(), null);

        // Criar SSLSocket e ligar ao servidor HTTPS
        SSLSocketFactory factory = sslContext.getSocketFactory();
        try (SSLSocket sslSocket = (SSLSocket) factory.createSocket("localhost", 4433)) {

            // Forçar handshake
            sslSocket.startHandshake();

            // Obter sessão TLS e cipher suite negociada
            SSLSession session = sslSocket.getSession();
            System.out.println("Protocolo:    " + session.getProtocol());
            System.out.println("Cipher Suite: " + session.getCipherSuite());
        }
    }
}
