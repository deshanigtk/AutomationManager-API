/*
*  Copyright (c) ${date}, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.security.tools.automation.manager.handler;

import org.apache.http.impl.execchain.RequestAbortedException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Utility methods for HTTPS request handling
 *
 * @author Deshani Geethika
 */
public class HttpsRequestHandler {

    private static final String trustStoreType = "JKS";
    private static final String trustManagerType = "SunX509";
    private static final String protocol = "TLSv1.2";
    private static final String trustStorePath = "org/wso2/security/tools/automation/manager/truststore.jks";
    private static final String trustStorePassword = "wso2carbon";
    private static SSLSocketFactory sslSocketFactory;
    private static boolean isInitialized = false;

    /**
     * Set host name verification for wso2 server
     */
    static {
        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> {
            // ip address of the service URL(like.23.28.244.244)
//                if (hostname.equals("23.28.244.244"))
            return true;

        });
    }

    /**
     * Initialise {@link SSLSocketFactory}
     * <p>Creates a {@link KeyStore} instance and load key store file into it. Then the created {@link KeyStore}
     * instance is passed to {@link TrustManagerFactory} and the trust managers from {@link TrustManagerFactory}
     * instance are passed into {@link SSLContext} instance</p>
     */
    private static void init() {
        KeyStore trustStore;
        try {
            trustStore = KeyStore.getInstance(trustStoreType);
            InputStream inputStream = HttpsRequestHandler.class.getClassLoader().getResourceAsStream(trustStorePath);
            assert inputStream != null;
            trustStore.load(inputStream, trustStorePassword.toCharArray());
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(trustManagerType);
            trustManagerFactory.init(trustStore);
            SSLContext sslContext = SSLContext.getInstance(protocol);
            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
            sslSocketFactory = sslContext.getSocketFactory();
            isInitialized = true;
        } catch (IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException |
                KeyManagementException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a HTTPS request
     *
     * @param link           URL of the server
     * @param requestHeaders Map of request headers
     * @param requestParams  Map of request parameters
     * @param method         Method type (GET or POST)
     * @return HTTP URL connection instance
     * @throws RequestAbortedException      Signals that the request has been aborted
     * @throws UnsupportedEncodingException The Character Encoding is not supported
     */
    public static HttpsURLConnection sendRequest(String link, Map<String, String> requestHeaders, Map<String, Object>
            requestParams, String method) throws RequestAbortedException, UnsupportedEncodingException {
        HttpsURLConnection httpsURLConnection;
        if (!isInitialized) {
            init();
        }
        StringBuilder postData = new StringBuilder();
        if (requestParams != null) {
            for (Map.Entry<String, Object> param : requestParams.entrySet()) {
                if (postData.length() != 0) {
                    postData.append('&');
                }
                postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                postData.append('=');
                postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
            }
        }
        try {
            URL url = new URL(link + "?" + postData.toString());
            httpsURLConnection = (HttpsURLConnection) url.openConnection();
            httpsURLConnection.setSSLSocketFactory(sslSocketFactory);
            httpsURLConnection.setRequestMethod(method);
            httpsURLConnection.setInstanceFollowRedirects(false);
            if (requestHeaders != null) {
                for (Map.Entry<String, String> entry : requestHeaders.entrySet()) {
                    httpsURLConnection.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            return httpsURLConnection;
        } catch (IOException e) {
            throw new RequestAbortedException("Https request aborted");
        }
    }

    /**
     * Reads a response and returns it as a {@code String}
     *
     * @param httpsURLConnection {@link HttpsURLConnection} instance
     * @return response read as a string
     * @throws IOException If an I/O exception is occurred
     */
    public static String printResponse(HttpsURLConnection httpsURLConnection) throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append(httpsURLConnection.getResponseCode())
                .append(" ")
                .append(httpsURLConnection.getResponseMessage())
                .append("\n");

        Map<String, List<String>> headerFields = httpsURLConnection.getHeaderFields();
        for (Map.Entry<String, List<String>> entry : headerFields.entrySet()) {
            if (entry.getKey() == null) {
                continue;
            }
            builder.append(entry.getKey()).append(": ");
            List<String> headerValues = entry.getValue();
            Iterator<String> it = headerValues.iterator();
            if (it.hasNext()) {
                builder.append(it.next());
                while (it.hasNext()) {
                    builder.append(", ").append(it.next());
                }
            }
            builder.append("\n");
        }
        return builder.toString();
    }

    /**
     * Extract a value from response header
     *
     * @param key                Key of the required value
     * @param httpsURLConnection HTTPS URL Connection
     * @return a list of values for a given key
     */
    public static List<String> extractValueFromResponseHeader(String key, HttpsURLConnection httpsURLConnection) {
        Map<String, List<String>> headerFields = httpsURLConnection.getHeaderFields();
        for (Map.Entry<String, List<String>> entry : headerFields.entrySet()) {
            if (entry.getKey() == null) {
                continue;
            }
            if (entry.getKey().equals(key)) {
                return entry.getValue();
            }
        }
        return null;
    }
}