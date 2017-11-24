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

import org.wso2.security.tools.automation.manager.config.ScannerProperties;
import org.wso2.security.tools.automation.manager.entity.dynamicscanner.DynamicScannerEntity;
import org.wso2.security.tools.automation.manager.entity.productmanager.ProductManagerEntity;
import org.wso2.security.tools.automation.manager.handler.ServerHandler;
import org.wso2.security.tools.automation.manager.scanner.dynamicscanner.productmanager.ProductManager;

/**
 * Main scanner
 *
 * @author Deshani Geethika
 */
public class DynamicScannerExecutor implements Runnable {
    private ProductManager productManager;
    private DynamicScanner dynamicScanner;

    public DynamicScannerExecutor(ProductManager productManager, DynamicScanner dynamicScanner) {
        this.productManager = productManager;
        this.dynamicScanner = dynamicScanner;
    }

    @Override
    public void run() {
        //TODO:meaning and why these are needed
        String productHostRelativeToScanner;
        String productHostRelativeToAutomationManager;
        int productPort;

        DynamicScannerEntity dynamicScannerEntity = dynamicScanner.startScanner();
        //TODO:change to call internal private mthds
        ProductManagerEntity productManagerEntity = productManager.startProductManager();
        //TODO:add if
        ServerHandler.hostAvailabilityCheck(productManager.getHost(), productManager.getPort(), 12 * 5);
        if (productManager.startServer()) {
            productHostRelativeToScanner = productManager.getHost();
            productHostRelativeToAutomationManager = productManager.getHost();
            productPort = ScannerProperties.getProductManagerProductPort();
            dynamicScanner.startScan(productHostRelativeToScanner, productHostRelativeToAutomationManager, productPort);
        }
    }
}
