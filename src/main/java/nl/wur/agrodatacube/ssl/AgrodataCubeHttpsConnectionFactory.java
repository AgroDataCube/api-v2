/*
* Copyright 2019 Wageningen Environmental Research
*
* For licensing information read the included LICENSE.txt file.
*
* Unless required by applicable law or agreed to in writing, this software
* is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
* ANY KIND, either express or implied.
 */
package nl.wur.agrodatacube.ssl;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import java.net.URL;

/**
 *
 * @author rande001
 */
public class AgrodataCubeHttpsConnectionFactory {

    private TrustManager[] trustAllCerts;
    SSLContext sslContext;
    HostnameVerifier allHostsValid;

    public AgrodataCubeHttpsConnectionFactory() {

        //
        // Create a trustmanager that accests all critificates.
        //
        trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }

            }
        };

        // Create all-trusting host name verifier
        allHostsValid = new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };

        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    }

    public HttpsURLConnection getConnection(URL url) {
        try {
            sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
            return (HttpsURLConnection) url.openConnection();
        } catch (Exception e) {
            throw new RuntimeException(e.getLocalizedMessage());
        }
    }
    
    public HttpsURLConnection getConnection(String urlString) {
        try {
            URL url = new URL(urlString);
            return getConnection(url);
        } catch (Exception e) {
            throw new RuntimeException(e.getLocalizedMessage());
        }
    }
}
