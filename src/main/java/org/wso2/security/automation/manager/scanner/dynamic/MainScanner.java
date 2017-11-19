package org.wso2.security.automation.manager.scanner.dynamic;/*
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

import org.wso2.security.automation.manager.entity.scanner.dynamic.DynamicScannerEntity;
import org.wso2.security.automation.manager.entity.scanner.dynamic.ProductManagerEntity;

public class MainScanner implements Runnable {

    private ProductManager productManager;
    private DynamicScanner dynamicScanner;

    public MainScanner(ProductManager productManager, DynamicScanner dynamicScanner) {
        this.productManager = productManager;
        this.dynamicScanner = dynamicScanner;
    }

    @Override
    public void run() {
        DynamicScannerEntity dynamicScannerEntity = dynamicScanner.startDynamicScannerContainer();
        if (dynamicScannerEntity != null) {
            if (true) {
                ProductManagerEntity productManagerEntity = productManager.startContainer();
                if (productManagerEntity != null) {
                    if (productManager.hostAvailabilityCheck(productManagerEntity.getIpAddress(), productManagerEntity.getHostPort(), 12 * 3)) {
                        if (productManager.startWso2Server()) {
                            if (productManager.hostAvailabilityCheck(productManagerEntity.getIpAddress(), 9443, 12 * 5)) {
                                dynamicScanner.startScan(dynamicScannerEntity, productManagerEntity);
//                                dynamicScanner.getReportAndMail(dynamicScannerEntity.getContainerId(), fileUploadLocation + File.separator + ScannerProperty.getZapReport());
                            }
                        }
                    }
                }
//            } else {
//                if (productManager.hostAvailabilityCheck(, wso2ServerPort, 10)) {
//                    startScan(dynamicScannerEntity, null);
//                }
            }
        }
    }
}
