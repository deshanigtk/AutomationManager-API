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

import org.wso2.security.tools.automation.manager.entity.scanner.dynamicscanner.DynamicScannerEntity;
import org.wso2.security.tools.automation.manager.entity.scanner.dynamicscanner.ProductManagerEntity;
import org.wso2.security.tools.automation.manager.handler.ServerHandler;

/**
 * Main scanner
 *
 * @author Deshani Geethika
 */
public class MainScanner implements Runnable {

    private ProductManager productManager;
    private DynamicScanner dynamicScanner;

    public MainScanner(ProductManager productManager, DynamicScanner dynamicScanner) {
        this.productManager = productManager;
        this.dynamicScanner = dynamicScanner;
    }

    @Override
    public void run() {

        DynamicScannerEntity dynamicScannerEntity = dynamicScanner.startContainer();

        if (dynamicScannerEntity != null) {
            if (productManager.isFileUpload()) {
                ProductManagerEntity productManagerEntity = productManager.startContainer();
                if (productManagerEntity != null) {
                    if (ServerHandler.hostAvailabilityCheck(productManagerEntity.getIpAddress(),
                            productManagerEntity.getHostPort(), 12 * 3)) {
                        if (productManager.startWso2Server()) {
                            if (ServerHandler.hostAvailabilityCheck(productManagerEntity.getIpAddress(), 9443, 12 *
                                    5)) {
                                dynamicScanner.startScan(dynamicScannerEntity, productManagerEntity);
                            }
                        }
                    }
                }
            } else {
                if (ServerHandler.hostAvailabilityCheck(productManager.getWso2ServerHost(),
                        productManager.getWso2ServerPort(), 10)) {
                    // complete
                }
            }
        }
    }
}
