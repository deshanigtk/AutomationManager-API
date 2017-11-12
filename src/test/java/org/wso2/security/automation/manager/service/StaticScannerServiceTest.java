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
import org.springframework.test.context.junit4.SpringRunner;
import org.wso2.security.automation.manager.entity.StaticScannerEntity;
import org.wso2.security.automation.manager.handler.MailHandler;
import org.wso2.security.automation.manager.repository.StaticScannerRepository;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
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
        String userId = "test@test.com";
        String name = "testName";
        String ipAddress = "0.0.0.0";
        String productName = "testProduct";
        String wumLevel = "testWum";
        boolean isFileUpload = false;
        String url = "https://github.com/gabrielf/maven-samples";
        boolean isFindSecBugs = true;
        boolean isDependencyCheck = true;

        staticScannerService.startStaticScan(userId, name, ipAddress, productName, wumLevel, isFileUpload, null,
                url, "master", null, isFindSecBugs, isDependencyCheck);
    }

    @Test
    public void testFindOneByContainerId() throws Exception {
        String userId = "test@test.com";
        String name = "testName";
        String ipAddress = "0.0.0.0";
        String containerId = "testContainerId";

        StaticScannerEntity staticScanner = new StaticScannerEntity();
        staticScanner.setUserId(userId);
        staticScanner.setTestName(name);
        staticScanner.setIpAddress(ipAddress);
        staticScanner.setContainerId(containerId);

        Mockito.when(staticScannerRepositoryMock.findOneByContainerId(containerId)).thenReturn(staticScanner);
        assertEquals(staticScanner, staticScannerService.findOneByContainerId(containerId));
    }

    @Test
    public void testFindByUserId() throws Exception {
        String userId = "test@test.com";
        String name = "testName";
        String ipAddress = "0.0.0.0";
        String containerId = "testContainerId";

        StaticScannerEntity staticScanner = new StaticScannerEntity();
        staticScanner.setUserId(userId);
        staticScanner.setTestName(name);
        staticScanner.setIpAddress(ipAddress);
        staticScanner.setContainerId(containerId);

        Mockito.when(staticScannerRepositoryMock.findByUserId(userId)).thenReturn(Collections.singletonList(staticScanner));
        assertEquals(Collections.singletonList(staticScanner), staticScannerService.findByUserId(userId));
    }

    @Test
    public void testFindAll() throws Exception {
        String userId = "test@test.com";
        String name = "testName";
        String ipAddress = "0.0.0.0";
        String containerId = "testContainerId";

        StaticScannerEntity staticScanner = new StaticScannerEntity();
        staticScanner.setUserId(userId);
        staticScanner.setTestName(name);
        staticScanner.setIpAddress(ipAddress);
        staticScanner.setContainerId(containerId);

        Mockito.when(staticScannerRepositoryMock.findAll()).thenReturn(Collections.singletonList(staticScanner));
        assertEquals(Collections.singletonList(staticScanner), staticScannerService.findAll());
    }

    @Test
    public void testSave() throws Exception {
        String userId = "test@test.com";
        String name = "testName";
        String ipAddress = "0.0.0.0";
        String containerId = "testContainerId";

        StaticScannerEntity staticScanner = new StaticScannerEntity();
        staticScanner.setUserId(userId);
        staticScanner.setTestName(name);
        staticScanner.setIpAddress(ipAddress);
        staticScanner.setContainerId(containerId);

        Mockito.when(staticScannerRepositoryMock.save(staticScanner)).thenReturn(staticScanner);
        assertEquals(staticScanner, staticScannerService.save(staticScanner));
    }

//    @Test
//    public void testGetReportAndMail() throws Exception {
//        String userId = "test@test.com";
//        String name = "staticScannerTest";
//        String ipAddress = "0.0.0.0";
//        String containerId = "testContainerId";
//
//        StaticScanner staticScanner = new StaticScanner();
//        staticScanner.setUserId(userId);
//        staticScanner.setTestName(name);
//        staticScanner.setIpAddress(ipAddress);
//        staticScanner.setContainerId(containerId);
//
//        Mockito.when(staticScannerService.findOneByContainerId(containerId)).thenReturn(staticScanner);
//        Mockito.when(mailHandlerMock.sendMail(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyObject(), Mockito.anyString()));
//    }

}
