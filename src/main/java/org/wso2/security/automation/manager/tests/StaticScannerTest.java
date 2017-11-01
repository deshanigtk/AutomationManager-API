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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.wso2.security.automation.manager.controller.scannerControllers.StaticScannerController;
import org.wso2.security.automation.manager.handlers.MailHandler;
import org.wso2.security.automation.manager.service.StaticScannerService;

import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@WebMvcTest(StaticScannerController.class)
//@ContextConfiguration(classes = StaticScannerController.class)
public class StaticScannerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StaticScannerService staticScannerService;

    @MockBean
    private MailHandler mailHandler;

    @Test
    public void startScanShouldReturnMessageFromService() throws Exception {

        String userId = "deshani@wso2.com";
        String name = "staticScannerTest";
        String ipAddress = "0.0.0.0";
        boolean isFileUpload = false;
        String url = "https://github.com/gabrielf/maven-samples";
        boolean isFindSecBugs = true;
        boolean isDependencyCheck = true;

        Mockito.when(staticScannerService.startStaticScan(userId, name, ipAddress, isFileUpload, null,
                url, null, null, isFindSecBugs, isDependencyCheck)).thenReturn("Ok");

        System.out.println("PPPPPPPPPPPPPPPPPPPPPP");


        RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/staticScanner/startScan")
                .param("userId", userId)
                .param("name", name)
                .param("ipAddress", ipAddress)
                .param("isFileUpload", String.valueOf(isFileUpload))
                .param("url", url)
                .param("isFindSecBugs", String.valueOf(isFindSecBugs))
                .param("isDependencyCheck", String.valueOf(isDependencyCheck));

        this.mockMvc.perform(requestBuilder).andExpect(status().isOk())
//                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ok")));


//        System.out.println(result.getResponse().getContentAsString().length());
    }
}
