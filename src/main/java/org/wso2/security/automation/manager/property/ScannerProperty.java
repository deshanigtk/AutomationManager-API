package org.wso2.security.automation.manager.property;/*
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

    private static String dynamicScannerIsReady;
    private static String dynamicScannerStartScan;
    private static String dynamicScannerGetReport;

    private static String automationManagerHost;
    private static String automationManagerPort;
    private static String tempFolderPath;


    static {
        properties = new Properties();
        try {
            properties.load(new FileInputStream(new File(ScannerProperty.class.getClassLoader().getResource("scanner.properties").getFile())));
            staticScannerDockerImage = properties.getProperty("scanner.static.docker.image");
            dynamicScannerDockerImage = properties.getProperty("scanner.dynamic.docker.image");
            zapDockerImage = properties.getProperty("scanner.zap.docker.image");
            staticScannerIsReady = properties.getProperty("scanner.static.is-ready");
            staticScannerStartScan = properties.getProperty("scanner.static.start-scan");
            staticScannerGetReport = properties.getProperty("scanner.static.get-report");
            dynamicScannerIsReady = properties.getProperty("scanner.dynamic.is-ready");
            dynamicScannerStartScan = properties.getProperty("scanner.dynamic.start-scan");
            dynamicScannerGetReport = properties.getProperty("scanner.dynamic.get.report");
            automationManagerHost = properties.getProperty("automation.manager.host");
            automationManagerPort = properties.getProperty("automation.manager.port");
            tempFolderPath = properties.getProperty("temp.dir");

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

    public static String getDynamicScannerIsReady() {
        return dynamicScannerIsReady;
    }

    public static String getDynamicScannerStartScan() {
        return dynamicScannerStartScan;
    }

    public static String getDynamicScannerGetReport() {
        return dynamicScannerGetReport;
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
}
