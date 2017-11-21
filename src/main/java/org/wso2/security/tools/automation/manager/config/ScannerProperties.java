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
package org.wso2.security.tools.automation.manager.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * The main contract of the {@code ScannerProperty} class is to read values from properties file.
 * Creates a static {@link Properties} object and loads the {@code scanner.properties} file into the object.
 * Then the property values are set to variables. This class provides getter methods to get the property values.
 *
 * @author Deshani Geethika
 */
public class ScannerProperties {
    private static Properties properties;
    //Names of the Docker images
    private static String findsecbugsScannerDockerImage;
    private static String dependencyCheckDockerImage;
    private static String productManagerDockerImage;
    private static String zapDockerImage;
    //
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

    private static String wso2ProductKeyUsername;
    private static String wso2ProductValueUsername;
    private static String wso2ProductKeyPassword;
    private static String wso2ProductValuePassword;
    private static String wso2ProductManagementConsoleLoginUrl;
    private static String wso2ProductManagementConsoleLogoutUrl;

    private static String statusInitiated;
    private static String statusCreated;
    private static String statusRunning;
    private static String statusCompleted;
    private static String statusFailed;

    private static String datePattern;

    static {
        properties = new Properties();
        try {
            properties.load(new FileInputStream(new File(ScannerProperties.class.getClassLoader().getResource
                    ("scanner.properties").getFile())));
            findsecbugsScannerDockerImage = properties.getProperty("scanner.findsecbugs.docker.image");
            dependencyCheckDockerImage = properties.getProperty("scanner.dependency-check.docker.image");
            productManagerDockerImage = properties.getProperty("product.manager.docker.image");
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
            zapReport = properties.getProperty("zap.report");
            wso2ProductKeyUsername = properties.getProperty("wso2.product.key.username");
            wso2ProductValueUsername = properties.getProperty("wso2.product.value.username");
            wso2ProductKeyPassword = properties.getProperty("wso2.product.key.password");
            wso2ProductValuePassword = properties.getProperty("wso2.product.value.password");
            wso2ProductManagementConsoleLoginUrl = properties.getProperty("wso2.product.login.url");
            wso2ProductManagementConsoleLogoutUrl = properties.getProperty("wso2.product.logout.url");

            statusInitiated = properties.getProperty("status.initiated");
            statusCreated = properties.getProperty("status.created");
            statusRunning = properties.getProperty("status.running");
            statusCompleted = properties.getProperty("status.completed");
            statusFailed = properties.getProperty("status.failed");
            datePattern = properties.getProperty("date.pattern");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getFindsecbugsScannerDockerImage() {
        return findsecbugsScannerDockerImage;
    }

    public static String getDependencyCheckDockerImage() {
        return dependencyCheckDockerImage;
    }

    public static String getProductManagerDockerImage() {
        return productManagerDockerImage;
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

    public static String getWso2ProductKeyUsername() {
        return wso2ProductKeyUsername;
    }

    public static String getWso2ProductValueUsername() {
        return wso2ProductValueUsername;
    }

    public static String getWso2ProductKeyPassword() {
        return wso2ProductKeyPassword;
    }

    public static String getWso2ProductValuePassword() {
        return wso2ProductValuePassword;
    }

    public static String getWso2ProductManagementConsoleLoginUrl() {
        return wso2ProductManagementConsoleLoginUrl;
    }

    public static String getWso2ProductManagementConsoleLogoutUrl() {
        return wso2ProductManagementConsoleLogoutUrl;
    }

    public static String getStatusInitiated() {
        return statusInitiated;
    }

    public static String getStatusCreated() {
        return statusCreated;
    }

    public static String getStatusRunning() {
        return statusRunning;
    }

    public static String getStatusCompleted() {
        return statusCompleted;
    }

    public static String getStatusFailed() {
        return statusFailed;
    }

    public static String getDatePattern() {
        return datePattern;
    }
}
