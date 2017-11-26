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
import org.wso2.security.tools.automation.manager.config.ScannerProperties;
import org.wso2.security.tools.automation.manager.exception.DynamicScannerException;
import org.wso2.security.tools.automation.manager.exception.ProductManagerException;
import org.wso2.security.tools.automation.manager.scanner.dynamicscanner.productmanager.ProductManager;

/**
 * Main scanner
 *
 * @author Deshani Geethika
 */
public class DynamicScannerExecutor implements Runnable {
    private ProductManager productManager;
    private DynamicScanner dynamicScanner;
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());


    public DynamicScannerExecutor(ProductManager productManager, DynamicScanner dynamicScanner) {
        this.productManager = productManager;
        this.dynamicScanner = dynamicScanner;
    }

    @Override
    public void run() {
        //If the scanner is a docker container, 
        String productHostRelativeToScanner;
        String productHostRelativeToAutomationManager;
        int productPort;

        try {
            dynamicScanner.startScanner();
            LOGGER.info(String.valueOf(dynamicScanner.getId()));
            productManager.startProductManager(dynamicScanner.getId());
            if (productManager.startServer()) {
                productHostRelativeToScanner = productManager.getHost();
                productHostRelativeToAutomationManager = productManager.getHost();
                productPort = ScannerProperties.getProductManagerProductPort();
                dynamicScanner.startScan(productHostRelativeToScanner, productHostRelativeToAutomationManager,
                        productPort);

            }
        } catch (ProductManagerException | DynamicScannerException e) {
            e.printStackTrace();
        }
    }
}
