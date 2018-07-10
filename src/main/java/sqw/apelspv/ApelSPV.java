/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sqw.apelspv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.log4j.Logger;
import sqw.certificat.CertificateChooser;
import sqw.certificat.Sign;

/**
 *
 * @author 22276637
 */
public class ApelSPV {

    private static final Logger LOGGER = Logger.getLogger(ApelSPV.class);
    //se modifica cu PIN-ul certificatului
    private static final String PIN = "12345678";
    private static final String PROXY_IP = null;
    private static final String PROXY_PORT = null;
     //cale catre fisierul de configurare al tokenului. in cazul de fata aladdin. Modelele de fisiere de configurare se pot regasi in DUKIntegrator (surse publicate pe www.anaf.ro) 
    private static final String CALE_FISIER_CONFIGURARE="C:\\DUKIntegrator\\dist\\config\\aladdin.cfg";

    public static void main(String[] args) {

        if (PROXY_IP != null) {
            System.setProperty("https.proxyHost", PROXY_IP);
            System.setProperty("https.proxyPort", PROXY_PORT);
            System.setProperty("http.proxyHost", PROXY_IP);
            System.setProperty("http.proxyPort", PROXY_PORT);
        }
        System.setProperty("https.protocols", "TLSv1");

        //avem incredere in orice certificat. Corect ar fi sa se adauge certificatul de la Anaf la trusted
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    @Override
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        // am intotdeauna incredere
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        // am intotdeauna incredere
                    }
                }
            };

            SSLContext sc = SSLContext.getInstance("SSL");
            // Creez empty HostnameVerifier
            HostnameVerifier hv = new HostnameVerifier() {
                public boolean verify(String arg0, SSLSession arg1) {
                    return true;
                }
            };
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(hv);

        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            LOGGER.error("err0: ", e);
        }
      
        //implementare CertificateChooser. In exemplul de mai jos se alege intotdeauna primul, dar, printr-o implementare diferita, se poate alege orice certificat de pe token - exemplu este in DUKIntegrator
        CertificateChooser c = new CertificateChooserImpl(0);
        //citire token
        Sign sign = new Sign();
        String err = sign.initSign(PIN, CALE_FISIER_CONFIGURARE, c, null);
        if (err != null) {
            LOGGER.error(err);
        } else {
            try {
                KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
                KeyStore ks = KeyStore.getInstance("PKCS11");
                char[] key = new char[0];
                ks.load(null, key);
                ks.setKeyEntry(sign.getCertAlias()._alias, sign.getPrivateKey(), key, sign.getChain());
                kmf.init(ks, PIN.toCharArray());
                //realizare SSL 
                CookieHandler.setDefault(new CookieManager());

                SSLContext sslContext = SSLContext.getInstance("SSLv3");
                sslContext.init(kmf.getKeyManagers(), null, null);
                SSLSocketFactory factory = sslContext.getSocketFactory();
                Socket socket;
                //prin proxy
                if (PROXY_IP != null) {
                    SocketAddress addr = new InetSocketAddress(PROXY_IP, Integer.parseInt(PROXY_PORT));
                    Proxy proxy = new Proxy(Proxy.Type.HTTP, addr);
                    socket = new Socket(proxy);
                } else {
                    socket = new Socket();
                }
                InetSocketAddress dest = new InetSocketAddress("webserviced.anaf.ro", 443);
                socket.connect(dest);
                SSLSocket sslsocket = (SSLSocket) factory.createSocket(socket, socket
                        .getInetAddress().getHostName(), socket.getPort(), true);
                sslsocket.setUseClientMode(true);
                sslsocket.setSoTimeout(100000);
                sslsocket.setUseClientMode(true);
                sslsocket.setKeepAlive(true);
                sslsocket.startHandshake();
                String inputLine;
               
             //exemplu pentru obtinere lista mesaje pe 50 zile. Similar se apeleaza si descarcarea (download) sau alte metode care pot apare in viitor
                String url = "https://webserviced.anaf.ro/SPVWS2/rest/listaMesaje?zile=500";
                URL obj = new URL(url);
                HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
                //Setare contextul de SSL
                con.setSSLSocketFactory(factory);
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        con.getInputStream()));
                //obtinere raspuns json
                while ((inputLine = in.readLine()) != null) {
                    System.out.println(inputLine);
                }

                in.close();
                con.disconnect();

            } catch (NoSuchAlgorithmException ex) {
                LOGGER.error("err1: ", ex);
            } catch (KeyStoreException ex) {
                LOGGER.error("err2: ", ex);
            } catch (IOException ex) {
                LOGGER.error("err3: ", ex);
            } catch (CertificateException ex) {
                LOGGER.error("err4: ", ex);
            } catch (UnrecoverableKeyException ex) {
                LOGGER.error("err5: ", ex);
            } catch (KeyManagementException ex) {
                LOGGER.error("err6: ", ex);
            }
        }
    }

}
