package org.wso2.security.tools.automation.manager.scanner.statics;/*
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.security.tools.automation.manager.entity.scanner.statics.StaticScannerEntity;

public interface StaticScanner extends Runnable {
    Logger LOGGER = LoggerFactory.getLogger(StaticScanner.class);

    static int calculatePort(int id) {
        if (40000 + id > 65535) {
            id = 1;
        }
        return (40000 + id) % 65535;
    }

    void init(String userId, String testName, String ipAddress, String productName, String wumLevel, boolean isFileUpload, String uploadLocation, String zipFileName, String gitUrl,
              String gitUserName, String gitPassword);

    StaticScannerEntity startContainer();

    void startScan();
}
