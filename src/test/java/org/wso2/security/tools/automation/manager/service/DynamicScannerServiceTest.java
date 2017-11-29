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
package org.wso2.security.tools.automation.manager.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;
import org.wso2.security.tools.automation.manager.exception.AutomationManagerException;
import org.wso2.security.tools.automation.manager.repository.dynamicscanner.DynamicScannerRepository;
import org.wso2.security.tools.automation.manager.service.dynamicscanner.DynamicScannerService;

import java.io.FileInputStream;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link DynamicScannerService}
 */
@RunWith(SpringRunner.class)
public class DynamicScannerServiceTest {

    private DynamicScannerRepository dynamicScannerRepositoryMock;
    private DynamicScannerService dynamicScannerService;

    @Before
    public void setup() {
        dynamicScannerRepositoryMock = Mockito.mock(DynamicScannerRepository.class);
        dynamicScannerService = new DynamicScannerService(dynamicScannerRepositoryMock);
    }

    @Test
    public void testStartScan() {
        String scanType = "zap";
        String userId = "xxx@gmail.com";
        String testName = "test";
        String productName = "test product";
        String wumLevel = "test wum";
        boolean productUploadAsZipFile = false;
        String wso2ServerHost = "localhost";
        int wso2ServerPort = 9878;

        try {
            MultipartFile urlListFile = new MockMultipartFile("mock", new FileInputStream
                    ("/home/deshani/Documents/uploaded"));
            dynamicScannerService.startScan(scanType, userId, testName, productName, wumLevel, productUploadAsZipFile,
                    null, urlListFile, wso2ServerHost, wso2ServerPort);

        } catch (IOException | AutomationManagerException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }
}
