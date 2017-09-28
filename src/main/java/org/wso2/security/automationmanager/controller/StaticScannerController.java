package org.wso2.security.automationmanager.controller;
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

import org.apache.http.HttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.wso2.security.automationmanager.entity.StaticScanner;
import org.wso2.security.automationmanager.handlers.DockerHandler;
import org.wso2.security.automationmanager.handlers.HttpRequestHandler;
import org.wso2.security.automationmanager.service.StaticScannerService;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;

@PropertySource("classpath:global.properties")
@Controller
@RequestMapping("/staticScanner")
public class StaticScannerController {

    @Value("${STATIC_SCANNER_DOCKER_IMAGE}")
    private String dockerImage;

    @Value("${CLONE_FROM_GITHUB}")
    private String cloneFromGitHub;

    @Value("${UPLOAD_PRODUCT_ZIP_FILE_AND_EXTRACT}")
    private String uploadProductZipFileAndExtract;

    @Value("${FIND_SEC_BUGS}")
    private String runFindSecBugs;

    @Autowired
    private StaticScannerService staticScannerService;

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @PostMapping(value = "start")
    public @ResponseBody
    String start(@RequestParam String userId, @RequestParam String ipAddress, @RequestParam String containerPort, @RequestParam String hostPort) {
        if (DockerHandler.pullImage(dockerImage)) {
            String containerId = DockerHandler.createContainer(dockerImage, ipAddress, containerPort, hostPort, null);

            if (containerId != null) {
                String createdTime = new SimpleDateFormat("yyyy-MM-dd:HH.mm.ss").format(new Date());
                StaticScanner staticScanner = new StaticScanner(containerId, userId, createdTime, ipAddress, containerPort, hostPort);
                staticScannerService.save(staticScanner);

                if (DockerHandler.startContainer(containerId)) {
                    staticScanner = staticScannerService.findOne(containerId);
                    staticScanner.setStatus("running");
                    staticScannerService.save(staticScanner);
                    return containerId;
                }

            }
        }
        return null;
    }

    @PostMapping(value = "cloneProductFromGitHub")
    public @ResponseBody
    boolean clone(@RequestParam String containerId, @RequestParam String url, @RequestParam String branch, @RequestParam String tag) {
        try {
            StaticScanner staticScanner = staticScannerService.findOne(containerId);
            if (staticScanner.getStatus().equals("running")) {
                URI uri = (new URIBuilder()).setHost(staticScanner.getIpAddress()).setPort(Integer.parseInt(staticScanner.getHostPort())).setScheme("http")
                        .setPath(cloneFromGitHub)
                        .addParameter("url", url)
                        .addParameter("branch", branch)
                        .addParameter("tag", tag)
                        .build();
                HttpResponse httpResponse = HttpRequestHandler.sendGetRequest(uri);
                boolean isProductCloned = Boolean.parseBoolean(httpResponse.getEntity().getContent().toString());
                if (isProductCloned) {
                    staticScanner.setProductAvailable(true);
                    staticScannerService.save(staticScanner);
                    LOGGER.info("Product successfully cloned");

                    return true;
                }
            } else {
                LOGGER.error("Container is not running");
            }

        } catch (URISyntaxException | IOException e) {
            LOGGER.error("Product is not cloned");
            LOGGER.error(e.toString());
        }
        return false;
    }

    @PostMapping(value = "runFindSecBugs")
    public @ResponseBody
    void runFindSecBugs(@RequestParam String containerId) {
        try {
            StaticScanner staticScanner = staticScannerService.findOne(containerId);
            if (staticScanner.getStatus().equals("running") && staticScanner.isProductAvailable()) {

                URI uri = (new URIBuilder()).setHost(staticScanner.getIpAddress()).setPort(Integer.parseInt(staticScanner.getHostPort())).setScheme("http").setPath(runFindSecBugs)
                        .build();
                HttpRequestHandler.sendGetRequest(uri);
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @PostMapping(value = "uploadProductZipFileAndExtract")
    public @ResponseBody
    void uploadProductZipFileAndExtract(@RequestParam String containerId, @RequestParam MultipartFile zipFile) throws IOException {
        try {
            StaticScanner staticScanner = staticScannerService.findOne(containerId);

            URI uri = (new URIBuilder()).setHost(staticScanner.getIpAddress()).setPort(Integer.parseInt(staticScanner.getHostPort())).setScheme("http")
                    .setPath(uploadProductZipFileAndExtract)
                    .build();

            HttpRequestHandler.sendMultipartRequest(uri, zipFile, null);

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}