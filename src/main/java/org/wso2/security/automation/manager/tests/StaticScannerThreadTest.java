package org.wso2.security.automation.manager.tests;/*
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.wso2.security.automation.manager.handlers.MailHandler;
import org.wso2.security.automation.manager.repository.StaticScannerRepository;
import org.wso2.security.automation.manager.scanners.StaticScannerThread;

@RunWith(SpringRunner.class)
@SpringBootTest
public class StaticScannerThreadTest {

    private StaticScannerRepository staticScannerRepositoryMock;
    private StaticScannerThread staticScannerThread;
    private MailHandler mailHandlerMock;

    @Before
    public void setup() {
        staticScannerRepositoryMock = Mockito.mock(StaticScannerRepository.class);
        mailHandlerMock = Mockito.mock(MailHandler.class);
    }

    @Test
    public void testStartScan() throws Exception {

        String userId = "test@gmail.com";
        String name = "staticScannerTest";
        String ipAddress = "0.0.0.0";
        String productName = "testProduct";
        String wumLevel = "testWum";
        boolean isFileUpload = false;
        String url = "https://github.com/gabrielf/maven-samples";
        boolean isFindSecBugs = true;
        boolean isDependencyCheck = true;

//        when(staticScannerRepositoryMock.findOneByContainerId());


        staticScannerThread = new StaticScannerThread(userId, name, ipAddress, productName, wumLevel, isFileUpload, null, null,
                url, "master", null, isFindSecBugs, isDependencyCheck);
//        System.out.println(staticScannerThread.findAll());
//        System.out.println(staticScannerThread());


    }
}
