package org.wso2.security.automation.manager.controller.scannerControllers;
/*
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.wso2.security.automation.manager.entity.Zap;
import org.wso2.security.automation.manager.service.ZapService;

@Controller
@RequestMapping("zap")
public class ZapController {

    private final ZapService zapService;

    @Autowired
    public ZapController(ZapService zapService) {
        this.zapService = zapService;
    }

    @GetMapping(path = "kill")
    public @ResponseBody
    void kill(@RequestParam String containerId) throws Exception {
        zapService.kill(containerId);
    }
}
