package org.wso2.security.automationmanager.handlers;/*
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

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class DockerHandler {

    private static DockerClient dockerClient = null;
    private final static Logger LOGGER = LoggerFactory.getLogger(DockerHandler.class);


    private static DockerClient getDockerClient() throws DockerCertificateException {
        if (dockerClient == null) {
            dockerClient = DefaultDockerClient.fromEnv().build();
        }
        return dockerClient;

    }

    public static boolean pullImage(String imageName) {
        try {
            if (getDockerClient().searchImages(imageName) == null) {
                getDockerClient().pull(imageName);
            }
            return true;
        } catch (DockerException | InterruptedException | DockerCertificateException e) {
            e.printStackTrace();
            LOGGER.error(e.toString());
            return false;
        }
    }

    public static String createContainer(String imageName, String ipAddress, String containerPort, String hostPort, String[] cmd) {
        String[] ports = {containerPort, hostPort};
        HashMap<String, List<PortBinding>> portBindings = new HashMap<>();

        for (String port : ports) {
            List<PortBinding> hostPorts = new ArrayList<>();
            hostPorts.add(PortBinding.of(ipAddress, port));
            portBindings.put(port, hostPorts);
        }

        HostConfig hostConfig = HostConfig.builder().portBindings(portBindings).build();
        ContainerConfig containerConfig = ContainerConfig.builder()
                .hostConfig(hostConfig)
                .image(imageName).exposedPorts(ports)
                .cmd(cmd)
                .build();

        ContainerCreation creation = null;
        try {
            creation = getDockerClient().createContainer(containerConfig);
        } catch (DockerException | InterruptedException | DockerCertificateException e) {
            e.printStackTrace();
        }
        return creation.id();
    }

    public static boolean startContainer(String containerId) {
        try {
            getDockerClient().startContainer(containerId);
        } catch (DockerException | InterruptedException | DockerCertificateException e) {
            e.printStackTrace();
            LOGGER.error(e.toString());
        }
        return "running".equals(inspectContainer(containerId).state().status());
    }

    static ContainerInfo inspectContainer(String containerId) {
        ContainerInfo containerInfo = null;
        try {
            containerInfo = getDockerClient().inspectContainer(containerId);
        } catch (DockerException | InterruptedException | DockerCertificateException e) {
            e.printStackTrace();
            LOGGER.error(e.toString());
        }
        return containerInfo;
    }

    public static void killContainer(String containerId) throws Exception {
        getDockerClient().killContainer(containerId);
    }

    public static void removeContainer(String containerId) throws Exception {
        getDockerClient().removeContainer(containerId);
    }

    public static void restartContainer(String containerId) throws DockerCertificateException, DockerException, InterruptedException {
        getDockerClient().restartContainer(containerId);
    }

    public static String getContainerLogs(String container_id) throws Exception {
        final String logs;
        try (LogStream stream = getDockerClient().logs(container_id, DockerClient.LogsParam.stdout(), DockerClient.LogsParam.stderr())) {
            logs = stream.readFully();
            return logs;
        }
    }

    public static List<Container> getRunningContainersList() throws Exception {
        return getDockerClient().listContainers();
    }

    public static List<Container> getAllContainerslist() throws Exception {
        return getDockerClient().listContainers(DockerClient.ListContainersParam.allContainers());
    }

    public static void closeDockerClient() throws Exception {
        getDockerClient().close();
    }

    public static void copyFilesFromContainer(String containerId, String filePathToCopy, String destinationFile, File destinationFolder) throws IOException, DockerCertificateException, DockerException, InterruptedException {

        InputStream inputStream = getDockerClient().archiveContainer(containerId, filePathToCopy);

        FileOutputStream outputStream =
                new FileOutputStream(new File(destinationFolder, destinationFile));

        int read;
        byte[] bytes = new byte[1024];

        while ((read = inputStream.read(bytes)) != -1) {
            outputStream.write(bytes, 0, read);
        }

        if (inputStream != null) {
            inputStream.close();
        }
        if (outputStream != null) {
            outputStream.close();
        }

    }

    public static void copyFilesToContainer(InputStream inputStream, String containerId, String path) throws DockerCertificateException, InterruptedException, DockerException, IOException {
        getDockerClient().copyToContainer(inputStream, containerId, path);
    }
}
