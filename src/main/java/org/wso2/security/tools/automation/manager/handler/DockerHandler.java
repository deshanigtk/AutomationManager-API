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

package org.wso2.security.tools.automation.manager.handler;

import com.google.common.collect.ImmutableList;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.security.tools.automation.manager.exception.AutomationManagerRuntimeException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Handling class for Docker
 *
 * @author Deshani Geethika
 */
@SuppressWarnings("unused")
public class DockerHandler {

    private static DockerClient dockerClient;
    private final static Logger LOGGER = LoggerFactory.getLogger(DockerHandler.class);

    private static DockerClient getDockerClient() throws DockerCertificateException {
        if (dockerClient == null) {
            dockerClient = DefaultDockerClient.fromEnv().build();
        }
        return dockerClient;
    }

    public static boolean pullImage(String imageName) {
        if (!checkIfImageIsAvailable(imageName)) {
            try {
                getDockerClient().pull(imageName);
                return checkIfImageIsAvailable(imageName);
            } catch (DockerException | InterruptedException | DockerCertificateException e) {
                throw new AutomationManagerRuntimeException("Error occurred while pulling the image", e);
            }
        }
        return false;
    }

    private static boolean checkIfImageIsAvailable(String imageName) {
        List<Image> images;
        try {
            images = getDockerClient().listImages();

            for (Image image : images) {
                ImmutableList<String> tags = image.repoTags();
                if (tags != null) {
                    for (String tag : tags) {
                        if (imageName.equals(tag)) {
                            LOGGER.info("Image is available");
                            return true;
                        }
                    }
                }
            }
            LOGGER.info("Image not available locally");
            return false;
        } catch (InterruptedException | DockerCertificateException | DockerException e) {
            throw new AutomationManagerRuntimeException("Error occurred while checking image availability", e);
        }
    }

    public static String createContainer(String imageName, String ipAddress, String containerPort, String hostPort,
                                         List<String> cmd, String[] env) {

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
                .env(env)
                .build();

        try {
            return getDockerClient().createContainer(containerConfig).id();
        } catch (DockerException | InterruptedException | DockerCertificateException e) {
            throw new AutomationManagerRuntimeException("Error occurred while creating the container", e);
        }
    }

    public static boolean startContainer(String containerId) {
        try {
            getDockerClient().startContainer(containerId);
            return "running".equals(inspectContainer(containerId).state().status());
        } catch (DockerException | InterruptedException | DockerCertificateException e) {
            throw new AutomationManagerRuntimeException("Error occurred while starting the container", e);
        }
    }

    public static ContainerInfo inspectContainer(String containerId) {
        try {
            return getDockerClient().inspectContainer(containerId);
        } catch (DockerException | InterruptedException | DockerCertificateException e) {
            throw new AutomationManagerRuntimeException("Error occurred while inspecting the container", e);
        }
    }

    public static void killContainer(String containerId) {
        try {
            getDockerClient().killContainer(containerId);
        } catch (DockerException | InterruptedException | DockerCertificateException e) {
            throw new AutomationManagerRuntimeException("Error occurred while killing the container", e);
        }
    }

    public static void removeContainer(String containerId) {
        try {
            getDockerClient().removeContainer(containerId);
        } catch (DockerException | InterruptedException | DockerCertificateException e) {
            throw new AutomationManagerRuntimeException("Error occurred while removing the container", e);
        }
    }

    public static void restartContainer(String containerId) {
        try {
            getDockerClient().restartContainer(containerId);
        } catch (DockerCertificateException | DockerException | InterruptedException e) {
            throw new AutomationManagerRuntimeException("Error occurred while restarting the container", e);
        }
    }

    public static String getContainerLogs(String container_id) throws Exception {
        final String logs;
        try (LogStream stream = getDockerClient().logs(container_id, DockerClient.LogsParam.stdout(),
                DockerClient.LogsParam.stderr())) {
            logs = stream.readFully();
            return logs;
        }
    }

    public static List<Container> getRunningContainersList() throws Exception {
        return getDockerClient().listContainers();
    }

    public static List<Container> getAllContainersList() throws Exception {
        return getDockerClient().listContainers(DockerClient.ListContainersParam.allContainers());
    }

    public static void closeDockerClient() throws Exception {
        getDockerClient().close();
    }

    public static void copyFilesFromContainer(String containerId, String filePathToCopy, String destinationFile,
                                              File destinationFolder)
            throws IOException, DockerCertificateException, DockerException, InterruptedException {

        try (InputStream inputStream = getDockerClient().archiveContainer(containerId, filePathToCopy);
             FileOutputStream outputStream = new FileOutputStream(new File(destinationFolder, destinationFile))) {
            int read;
            byte[] bytes = new byte[1024];

            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
        }
    }

    public static void copyFilesToContainer(InputStream inputStream, String containerId, String path)
            throws DockerCertificateException, InterruptedException, DockerException, IOException {
        getDockerClient().copyToContainer(inputStream, containerId, path);
    }
}
