/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.security.tools.automation.manager.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.wso2.security.tools.automation.manager.entity.staticscanner.StaticScannerEntity;
import org.wso2.security.tools.automation.manager.entity.staticscanner.containerbased.dependencycheck
        .DependencyCheckEntity;
import org.wso2.security.tools.automation.manager.entity.staticscanner.containerbased.findsecbugs.FindSecBugsEntity;
import org.wso2.security.tools.automation.manager.exception.AutomationManagerException;
import org.wso2.security.tools.automation.manager.repository.staticscanner.StaticScannerRepository;
import org.wso2.security.tools.automation.manager.service.staticscanner.StaticScannerService;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link StaticScannerService}
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class StaticScannerServiceTest {

    private StaticScannerRepository staticScannerRepositoryMock;
    private StaticScannerService staticScannerService;

    @Before
    public void setup() {
        staticScannerRepositoryMock = Mockito.mock(StaticScannerRepository.class);
        staticScannerService = new StaticScannerService(staticScannerRepositoryMock);
    }

    @Test
    public void testStartScan() {
        String scanType = "fsb";
        String userId = "test@test.com";
        String testName = "testName";
        String productName = "testProduct";
        String wumLevel = "testWum";
        boolean sourceCodeUploadAsZip = false;
        String url = "https://github.com/gabrielf/maven-samples";
        try {
            staticScannerService.startScan(scanType, userId, testName, productName, wumLevel, sourceCodeUploadAsZip,
                    null,
                    url);
        } catch (AutomationManagerException e) {
            assertTrue(false);
        }
    }

//    @Test
//    public void testFindOneByContainerId() throws Exception {
//        String userId = "test@test.com";
//        String name = "testName";
//        String ipAddress = "0.0.0.0";
//        String containerId = "testContainerId";
//
//        Co staticScanner = new ContainerBasedStaticScannerEntity();
//        staticScanner.setUserId(userId);
//        staticScanner.setTestName(name);
//        staticScanner.setIpAddress(ipAddress);
//        staticScanner.setContainerId(containerId);
//
//        Mockito.when(staticScannerRepositoryMock.findOneByContainerId(containerId)).thenReturn(staticScanner);
//        assertEquals(staticScanner, staticScannerService.findOneByContainerId(containerId));
//    }

    @Test
    public void testFindByUserId() throws Exception {
        String userId = "test@test.com";
        String testName = "testName";
        StaticScannerEntity staticScanner = new FindSecBugsEntity();
        staticScanner.setUserId(userId);
        staticScanner.setTestName(testName);
        Mockito.when(staticScannerRepositoryMock.findByUserId(userId)).thenReturn(Collections.singletonList
                (staticScanner));
        assertEquals(Collections.singletonList(staticScanner), staticScannerService.findByUserId(userId));
    }

    @Test
    public void testFindAll() throws Exception {
        String userId = "test@test.com";
        String name = "testName";

        StaticScannerEntity staticScanner = new DependencyCheckEntity();
        staticScanner.setUserId(userId);
        staticScanner.setTestName(name);
        Mockito.when(staticScannerRepositoryMock.findAll()).thenReturn(Collections.singletonList(staticScanner));
        assertEquals(Collections.singletonList(staticScanner), staticScannerService.findAll());
    }

    @Test
    public void testSave() throws Exception {
        String userId = "test@test.com";
        String testName = "testName";
        StaticScannerEntity staticScanner = new FindSecBugsEntity();
        staticScanner.setUserId(userId);
        staticScanner.setTestName(testName);
        Mockito.when(staticScannerRepositoryMock.save(staticScanner)).thenReturn(staticScanner);
        assertEquals(staticScanner, staticScannerService.save(staticScanner));
    }
}
