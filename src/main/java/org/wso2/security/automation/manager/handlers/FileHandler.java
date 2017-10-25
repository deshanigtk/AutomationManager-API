package org.wso2.security.automation.manager.handlers;/*
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
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileHandler.class);

    public static boolean uploadFile(MultipartFile file, String fileUploadPath) {
        try {
            byte[] bytes = file.getBytes();
            BufferedOutputStream stream =
                    new BufferedOutputStream(new FileOutputStream(new File(fileUploadPath)));
            stream.write(bytes);
            stream.close();
            return true;

        } catch (IOException e) {
            LOGGER.error("File is not uploaded" + e.toString());
        }

        return false;
    }

    public static boolean deleteUploadedFile(String filePath) {
        boolean isDeleted = false;
        if (new File(filePath).exists()) {
            isDeleted = new File(filePath).delete();
            if (isDeleted) {
                LOGGER.info("Successfully deleted file: " + filePath);
            } else {
                LOGGER.error("Cannot delete file");
            }
        } else {
            LOGGER.error("File doesn't exist");
        }
        return isDeleted;
    }
}
