package org.wso2.security.automation.manager.handlers;
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

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HttpRequestHandler {
    private static HttpClient httpClient = HttpClients.createDefault();

    private static List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();

    public static HttpResponse sendGetRequest(URI request) {
        HttpResponse httpResponse;
        HttpGet httpGetRequest = new HttpGet(request);
        try {
            httpResponse = httpClient.execute(httpGetRequest);
            if (httpResponse != null) {
                return httpResponse;
            }
        } catch (IOException e) {
//            e.printStackTrace();
            System.out.println(e.getMessage());
        }
        return null;
    }

    public static HttpResponse sendPostRequest(URI request, ArrayList<NameValuePair> parameters) {
        try {
            HttpPost httpPostRequest = new HttpPost(request);

            if (parameters != null) {
                for (NameValuePair parameter : parameters) {
                    urlParameters.add(new BasicNameValuePair(parameter.getName(), parameter.getValue()));
                }
                httpPostRequest.setEntity(new UrlEncodedFormEntity(urlParameters));
            }
            HttpResponse httpResponse = httpClient.execute(httpPostRequest);
            if (httpResponse != null) {
                return httpResponse;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static HttpResponse sendMultipartRequest(URI uri, Map<String, MultipartFile> files, Map<String, String> textBody) {
        HttpResponse response = null;
        try {
            HttpPost uploadFile = new HttpPost(uri);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            if (textBody != null) {
                for (Map.Entry<String, String> entry : textBody.entrySet()) {
                    builder.addTextBody(entry.getKey(), entry.getValue(), ContentType.TEXT_PLAIN);
                }
            }
            // This attaches the file to the POST:
            for (Map.Entry<String, MultipartFile> entry : files.entrySet()) {
                InputStream inputStream = entry.getValue().getInputStream();

                builder.addBinaryBody(
                        entry.getKey(),
                        inputStream,
                        ContentType.APPLICATION_OCTET_STREAM,
                        entry.getValue().getOriginalFilename()
                );

            }

            HttpEntity multipart = builder.build();
            uploadFile.setEntity(multipart);
            response = httpClient.execute(uploadFile);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    public static String printResponse(HttpResponse response) {
        BufferedReader rd;
        try {
            rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            return result.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return e.toString();
        }
    }

    public static boolean saveResponseToFile(HttpResponse response, File destinationFile) throws Exception {
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            InputStream inputStream = entity.getContent();
            FileOutputStream output = new FileOutputStream(destinationFile);
            int l;
            byte[] tmp = new byte[2048];
            while ((l = inputStream.read(tmp)) != -1) {
                output.write(tmp, 0, l);
            }
            output.close();
            inputStream.close();
            return true;
        }
        return false;
    }
}
