package org.wso2.security.automation.manager.tests.serviceTests;/*
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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.wso2.security.automation.manager.entity.StaticScanner;
import org.wso2.security.automation.manager.handlers.MailHandler;
import org.wso2.security.automation.manager.repository.StaticScannerRepository;
import org.wso2.security.automation.manager.service.StaticScannerService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)

public class StaticScannerServiceTest {

    private StaticScannerRepository staticScannerRepositoryMock;
    private StaticScannerService staticScannerService;
    private MailHandler mailHandlerMock;

    @Before
    public void setup() {
        staticScannerRepositoryMock = Mockito.mock(StaticScannerRepository.class);
        mailHandlerMock = Mockito.mock(MailHandler.class);
        staticScannerService = new StaticScannerService(staticScannerRepositoryMock, mailHandlerMock);
    }

    @Test
    public void testStartScan() throws Exception {

        String userId = "test@gmail.com";
        String name = "staticScannerTest";
        String ipAddress = "0.0.0.0";
        boolean isFileUpload = false;
        String url = "https://github.com/gabrielf/maven-samples";
        boolean isFindSecBugs = true;
        boolean isDependencyCheck = true;

        assertEquals("Ok", staticScannerService.startStaticScan(userId, name, ipAddress, isFileUpload, null,
                url, "master", null, isFindSecBugs, isDependencyCheck));

    }

    @Test
    public void testFindOneByContainerId() throws Exception {

        String userId = "test@gmail.com";
        String name = "staticScannerTest";
        String ipAddress = "0.0.0.0";
        String containerId = "testContainerId";

        StaticScanner staticScanner = new StaticScanner();
        staticScanner.setUserId(userId);
        staticScanner.setName(name);
        staticScanner.setIpAddress(ipAddress);
        staticScanner.setContainerId(containerId);


        assertNotNull(staticScannerService.findOneByContainerId(containerId));

    }


}
