/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.security.tools.automation.manager.scanner.dynamicscanner.factory;

import org.wso2.security.tools.automation.manager.config.DynamicScannerProperties;
import org.wso2.security.tools.automation.manager.scanner.dynamicscanner.cloudbased.CloudBasedDynamicScanner;
import org.wso2.security.tools.automation.manager.scanner.dynamicscanner.containerbased.ContainerBasedDynamicScanner;
import org.wso2.security.tools.automation.manager.scanner.dynamicscanner.containerbased.zap.ZapScanner;

/**
 * The class {@link ContainerBasedDynamicScannerFactory} implements abstract methods of
 * {@link AbstractDynamicScannerFactory} to create instances of {@link ContainerBasedDynamicScanner}
 */
public class ContainerBasedDynamicScannerFactory extends AbstractDynamicScannerFactory {

    @Override
    public CloudBasedDynamicScanner getCloudBasedDynamicScanner(String type) {
        return null;
    }

    /**
     * Check the scan type and return a scanner instance
     *
     * @param type Scanner type
     * @return {@link ContainerBasedDynamicScanner} instance
     */
    @Override
    public ContainerBasedDynamicScanner getContainerBasedDynamicScanner(String type) {
        ContainerBasedDynamicScanner dynamicScanner = null;
        if (DynamicScannerProperties.getZapScannerType().equalsIgnoreCase(type)) {
            dynamicScanner = new ZapScanner();
        }
        return dynamicScanner;
    }
}
