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

package org.wso2.security.automation.manager.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;
import org.wso2.security.automation.manager.handler.MailHandler;
import org.wso2.security.automation.manager.repository.DynamicScannerRepository;

import java.io.FileInputStream;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DynamicScannerServiceTest {

    private DynamicScannerRepository dynamicScannerRepositoryMock;
    private DynamicScannerService dynamicScannerService;
    private ZapService zapServiceMock;
    private MailHandler mailHandlerMock;

    @Before
    public void setup() {
        dynamicScannerRepositoryMock = Mockito.mock(DynamicScannerRepository.class);
        mailHandlerMock = Mockito.mock(MailHandler.class);
        dynamicScannerService = new DynamicScannerService(dynamicScannerRepositoryMock, mailHandlerMock, zapServiceMock);
    }

    @Test
    public void testStartScan() throws Exception {
        String userId = "test@test.com";
        String name = "testName";
        String ipAddress = "0.0.0.0";
        String productName = "testProduct";
        String wumLevel = "testWum";
        boolean isFileUpload = false;
        MultipartFile urlListFile = new MockMultipartFile("urlFile", new FileInputStream("/home/deshani/Documents/urlList"));
        String wso2ServerHost = "172.17.0.1";
        int wso2ServerPort = 8089;
        boolean isAuthenticatedScan = true;

        dynamicScannerService.startScan(userId, name, ipAddress, productName, wumLevel, isFileUpload, null,
                urlListFile, wso2ServerHost, wso2ServerPort, isAuthenticatedScan);
    }
}
