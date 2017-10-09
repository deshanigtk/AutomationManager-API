package org.wso2.security.automation.manager.controller.viewControllers;
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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.wso2.security.automation.manager.controller.StaticScannerController;
import org.wso2.security.automation.manager.entity.User;
import org.wso2.security.automation.manager.repository.UserRepository;

@Controller
public class StaticScannerViewController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StaticScannerController staticScannerController;

    @GetMapping(value = "/add") // Map ONLY GET Requests
    public @ResponseBody
    String addNewUser(@RequestParam String name, @RequestParam String email) {
        // @ResponseBody means the returned String is the response, not a view name
        // @RequestParam means it is a parameter from the GET or POST request

        User n = new User();
        n.setName(name);
        n.setEmail(email);
        userRepository.save(n);
        return "Saved";
    }

    @GetMapping(value = "/all")
    public @ResponseBody
    Iterable<User> getAllUsers() {
        // This returns a JSON or XML with the users
        return userRepository.findAll();
    }

    @GetMapping(value = "/signin")
    public String signIn() {
        return "signin/signin";
    }

    @GetMapping(value = "/mainScanners")
    public String index() {
        return "scanners";
    }

    @PostMapping(value = "staticScanner/started")
    public String started(@RequestParam String userId, @RequestParam String ipAddress, @RequestParam int containerPort, @RequestParam int hostPort) throws InterruptedException {
        String containerId = staticScannerController.start(userId, ipAddress, containerPort, hostPort);
        if (containerId != null) {
            Thread.sleep(4000);
            staticScannerController.configureNotificationManager(containerId);
            return "staticScanner/productUploader";
        }
        return "scanners";
    }

    @PostMapping(value = "staticScanner/productCloned")
    public String productZipUploaded(@RequestParam String containerId, @RequestParam String url, @RequestParam String branch, @RequestParam String tag) throws InterruptedException {
        String cloned = staticScannerController.clone(containerId, url, branch, tag);

        return "staticScanner";

    }


}