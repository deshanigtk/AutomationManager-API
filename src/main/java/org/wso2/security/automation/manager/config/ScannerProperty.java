package org.wso2.security.automation.manager.config;/*
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ScannerProperty {

    private static Properties properties;
    private static String staticScannerDockerImage;
    private static String dynamicScannerDockerImage;
    private static String zapDockerImage;

    private static String staticScannerIsReady;
    private static String staticScannerStartScan;
    private static String staticScannerGetReport;

    private static String productManagerIsReady;
    private static String productManagerStartServer;
    private static int productManagerProductPort;

    private static String automationManagerHost;
    private static String automationManagerPort;
    private static String tempFolderPath;

    private static String zapReport;

    static {
        properties = new Properties();
        try {
            properties.load(new FileInputStream(new File(ScannerProperty.class.getClassLoader().getResource("scanner.properties").getFile())));
            staticScannerDockerImage = properties.getProperty("scanner.static.docker.image");
            dynamicScannerDockerImage = properties.getProperty("product.manager.docker.image");
            zapDockerImage = properties.getProperty("scanner.zap.docker.image");
            staticScannerIsReady = properties.getProperty("scanner.static.is-ready");
            staticScannerStartScan = properties.getProperty("scanner.static.start-scan");
            staticScannerGetReport = properties.getProperty("scanner.static.get-report");
            productManagerIsReady = properties.getProperty("product.manager.is-ready");
            productManagerStartServer = properties.getProperty("product.manager.start-server");
            productManagerProductPort = Integer.parseInt(properties.getProperty("product.manager.product-port"));
            automationManagerHost = properties.getProperty("automation.manager.host");
            automationManagerPort = properties.getProperty("automation.manager.port");
            tempFolderPath = properties.getProperty("temp.dir");
            zapReport=properties.getProperty("zap.report");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getStaticScannerDockerImage() {
        return staticScannerDockerImage;
    }

    public static String getDynamicScannerDockerImage() {
        return dynamicScannerDockerImage;
    }

    public static String getZapDockerImage() {
        return zapDockerImage;
    }

    public static String getStaticScannerIsReady() {
        return staticScannerIsReady;
    }

    public static String getStaticScannerStartScan() {
        return staticScannerStartScan;
    }

    public static String getStaticScannerGetReport() {
        return staticScannerGetReport;
    }

    public static String getProductManagerIsReady() {
        return productManagerIsReady;
    }

    public static String getProductManagerStartServer() {
        return productManagerStartServer;
    }

    public static int getProductManagerProductPort() {
        return productManagerProductPort;
    }

    public static String getAutomationManagerHost() {
        return automationManagerHost;
    }

    public static String getAutomationManagerPort() {
        return automationManagerPort;
    }

    public static String getTempFolderPath() {
        return tempFolderPath;
    }

    public static String getZapReport() {
        return zapReport;
    }
}
