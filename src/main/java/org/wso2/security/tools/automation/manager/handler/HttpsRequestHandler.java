package org.wso2.security.tools.automation.manager.handler;
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

import org.apache.http.impl.execchain.RequestAbortedException;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyStore;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

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

    private static KeyStore trustStore;
    private static HttpsURLConnection httpsURLConnection;
    private static SSLSocketFactory sslSocketFactory;

    private static boolean isInitialized = false;

    static {
        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> {
            // ip address of the service URL(like.23.28.244.244)
//                if (hostname.equals("23.28.244.244"))
            return true;

        });
    }

    private static void init() {
        try {
            trustStore = KeyStore.getInstance(trustStoreType);
            System.out.println(trustStore);
            InputStream inputStream = HttpsRequestHandler.class.getClassLoader().getResourceAsStream(trustStorePath);
            assert inputStream != null;
            trustStore.load(inputStream, trustStorePassword.toCharArray());

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(trustManagerType);
            trustManagerFactory.init(trustStore);

            SSLContext sslContext = SSLContext.getInstance(protocol);

            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
            sslSocketFactory = sslContext.getSocketFactory();
            System.out.println(sslSocketFactory);
            System.out.println(sslContext);

            isInitialized = true;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static HttpsURLConnection sendRequest(String link, Map<String, String> requestHeaders, Map<String, Object> requestParams,
                                                 String method) throws RequestAbortedException, UnsupportedEncodingException {

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


            System.out.println(httpsURLConnection);
            return httpsURLConnection;

        } catch (IOException e) {
            throw new RequestAbortedException("Https request aborted");
        }
    }

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
            builder.append(entry.getKey())
                    .append(": ");

            List<String> headerValues = entry.getValue();

            Iterator<String> it = headerValues.iterator();
            if (it.hasNext()) {
                builder.append(it.next());

                while (it.hasNext()) {
                    builder.append(", ")
                            .append(it.next());
                }
            }
            builder.append("\n");
        }

        return builder.toString();
    }

    public static List<String> getResponseValue(String key, HttpsURLConnection httpsURLConnection) {
        Map<String, List<String>> headerFields = httpsURLConnection.getHeaderFields();
        for (Map.Entry<String, List<String>> entry : headerFields.entrySet()) {
            System.out.println(entry.getKey());
            System.out.println(entry.getValue());
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