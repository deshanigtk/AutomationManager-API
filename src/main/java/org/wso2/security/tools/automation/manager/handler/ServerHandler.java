package org.wso2.security.tools.automation.manager.handler;/*
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;

public class ServerHandler {

    private final static Logger LOGGER = LoggerFactory.getLogger(ServerHandler.class);

    public static boolean hostAvailabilityCheck(String host, int port, int times) {
        int i = 0;
        while (i < times) {
            LOGGER.info("Checking host availability...");
            try (Socket s = new Socket(host, port)) {
                LOGGER.info(host + ":" + port + " is available");
                return true;
            } catch (IOException e) {
                LOGGER.error(e.toString());
                try {
                    Thread.sleep(5000);
                    i++;
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return false;
    }
}
