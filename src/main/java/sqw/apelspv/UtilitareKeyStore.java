package sqw.apelspv;

import javax.net.ssl.*;
import java.io.*;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/**
 *
 * @author radub
 */
public class UtilitareKeyStore {
    // bazat pe InstallCert

    private static final char[] HEXDIGITS = "0123456789abcdef".toCharArray();

    public static KeyStore createKeyStore() throws Exception {
        File file = new File("./anafserverstore.keystore");
        KeyStore keyStore = KeyStore.getInstance("JKS");
        if (file.exists()) {
            // if exists, load
            System.out.println("Incarc keystore ANAF");
            keyStore.load(new FileInputStream(file), "123456".toCharArray());
        } else {
            // if not exists, create
            System.out.println("Creez si incarc keystore ANAF");
            keyStore.load(null, null);
            keyStore.store(new FileOutputStream(file), "123456".toCharArray());

        }
        return keyStore;
    }

    public static void adaugCertificatAnafOnlineInKeyStore() throws Exception {

        String host = "anaf.ro";
        char[] passphrase = "123456".toCharArray();
        int port = 443;

        System.out.println("Loading KeyStore anafserverstore.keystore...");
        InputStream in = new FileInputStream("./anafserverstore.keystore");
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(in, passphrase);
        in.close();

        SSLContext context = SSLContext.getInstance("SSLv3"); // sau TLS
        TrustManagerFactory tmf
                = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ks);
        X509TrustManager defaultTrustManager = (X509TrustManager) tmf.getTrustManagers()[0];
        SavingTrustManager tm = new SavingTrustManager(defaultTrustManager);
        context.init(null, new TrustManager[]{tm}, null);
        SSLSocketFactory factory = context.getSocketFactory();

        System.out.println("Deschid conexiunea catre " + host + ":" + port + "...");
        SSLSocket socket = (SSLSocket) factory.createSocket(host, port);
        socket.setSoTimeout(10000);
        try {
            System.out.println("Starting SSL handshake...");
            socket.startHandshake();
            socket.close();
            System.out.println();
            System.out.println("No errors, certificate is already trusted");
        } catch (SSLException e) {
            System.out.println();
            e.printStackTrace(System.out);
        }

        X509Certificate[] chain = tm.chain;
        if (chain == null) {
            System.out.println("Nu am reusit sa obtin server certificate chain");
            return;
        }

        System.out.println();
        System.out.println("Serverul a trimis " + chain.length + " certificat(e):");
        System.out.println();
        MessageDigest sha1 = MessageDigest.getInstance("SHA1");
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        for (int i = 0; i < chain.length; i++) {
            X509Certificate cert = chain[i];
//            System.out.println(" " + (i + 1) + " Subject " + cert.getSubjectDN());
//            System.out.println("   Issuer  " + cert.getIssuerDN());
            sha1.update(cert.getEncoded());
//            System.out.println("   sha1    " + toHexString(sha1.digest()));
            md5.update(cert.getEncoded());
//            System.out.println("   md5     " + toHexString(md5.digest()));
//            System.out.println();

            String alias = host + "-" + (i);
            
            if(cert.getSubjectDN().getName().contains("anaf")) {
                            System.out.println(" " + (i + 1) + " Subject " + cert.getSubjectDN());
            System.out.println("   Issuer  " + cert.getIssuerDN());
                ks.setCertificateEntry(alias, cert);
                System.out.println();
                System.out.println("Am adaugat in keystore certificatul cu alias-ul '"
                    + alias + "'");
            }
            
            OutputStream out = new FileOutputStream("./anafserverstore.keystore");
            ks.store(out, passphrase);
            out.close();

            //System.out.println();
            //System.out.println(cert);
//            System.out.println();
//            System.out.println("Am adaugat in keystore certificatul cu alias-ul '"
//                    + alias + "'");

        }

    }

    private static String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 3);
        for (int b : bytes) {
            b &= 0xff;
            sb.append(HEXDIGITS[b >> 4]);
            sb.append(HEXDIGITS[b & 15]);
            sb.append(' ');
        }
        return sb.toString();
    }

    private static class SavingTrustManager implements X509TrustManager {

        private final X509TrustManager tm;
        private X509Certificate[] chain;

        SavingTrustManager(X509TrustManager tm) {
            this.tm = tm;
        }

        public X509Certificate[] getAcceptedIssuers() {
            throw new UnsupportedOperationException();
        }

        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
            throw new UnsupportedOperationException();
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
            this.chain = chain;
            tm.checkServerTrusted(chain, authType);
        }
    }

    private static InputStream fullStream(String fname) throws IOException {
        FileInputStream fis = new FileInputStream(fname);
        DataInputStream dis = new DataInputStream(fis);
        byte[] bytes = new byte[dis.available()];
        dis.readFully(bytes);
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        return bais;
    }

    public static void adaugaCertificatAnafinKeyStore() throws Exception {
        String alias = "ANAF";
        String certfile = "./-anafro.crt";
        String fisierKeystore = "./anafserverstore.keystore";
        char[] password = "123456".toCharArray();

        FileInputStream is = new FileInputStream(fisierKeystore);

        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
        keystore.load(is, password);

        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        InputStream certstream = fullStream(certfile);
        Certificate certs = cf.generateCertificate(certstream);

        File keystoreFile = new File(fisierKeystore);
// Incarc keystore
        FileInputStream in = new FileInputStream(keystoreFile);
        keystore.load(in, password);
        in.close();

// adaug certificat
        keystore.setCertificateEntry(alias, certs);

// Salvez continut
        FileOutputStream out = new FileOutputStream(keystoreFile);
        keystore.store(out, password);
        out.close();
    }

    public static void main(String[] args) throws Exception {
        createKeyStore();
        adaugCertificatAnafOnlineInKeyStore();
    }

}
