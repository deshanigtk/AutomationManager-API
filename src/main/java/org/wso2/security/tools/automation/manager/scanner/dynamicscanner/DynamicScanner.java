/*
 * Copyright (c) ${date}, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.security.tools.automation.manager.scanner.dynamicscanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.security.tools.automation.manager.entity.dynamicscanner.DynamicScannerEntity;

/**
 * Dynamic scanner interface
 *
 * @author Deshani Geethika
 */
public interface DynamicScanner {

    Logger LOGGER = LoggerFactory.getLogger(DynamicScanner.class);

    int calculateDynamicScannerPort(int id);

    void init(String userId, String ipAddress, boolean isFileUpload, String fileUploadLocation, String urlListFileName,
              String wso2ServerHost, int wso2ServerPort, String scannerHost, int scannerPort);

    DynamicScannerEntity startScanner();

    void startScan(String productHostRelativeToScanner, String productHostRelativeToThis, int productPort);

    boolean isContainer();
}
