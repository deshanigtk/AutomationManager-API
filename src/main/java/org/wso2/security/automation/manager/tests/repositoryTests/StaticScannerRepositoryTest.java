package org.wso2.security.automation.manager.tests.repositoryTests;/*
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;
import org.wso2.security.automation.manager.entity.StaticScanner;
import org.wso2.security.automation.manager.repository.StaticScannerRepository;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class StaticScannerRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private StaticScannerRepository staticScannerRepository;

    @Test
    public void testFindByContainerId() throws Exception {
        String containerId = "testContainerId";
        String name = "testName";
        String userId = "deshani@wso2.com";

        StaticScanner staticScannerToPersist = new StaticScanner();
        staticScannerToPersist.setContainerId(containerId);
        staticScannerToPersist.setName(name);
        staticScannerToPersist.setUserId(userId);

        this.entityManager.persist(staticScannerToPersist);
        StaticScanner staticScanner = this.staticScannerRepository.findOneByContainerId(containerId);
        assertThat(staticScanner.getName()).isEqualTo(name);
        assertThat(staticScanner.getUserId()).isEqualTo(userId);
    }

    @Test
    public void testFindByUserId() throws Exception {
        String containerId = "testContainerId1";
        String name = "testName";
        String userId = "deshani@wso2.com";

        StaticScanner staticScannerToPersist = new StaticScanner();
        staticScannerToPersist.setContainerId(containerId);
        staticScannerToPersist.setName(name);
        staticScannerToPersist.setUserId(userId);

        this.entityManager.persist(staticScannerToPersist);
        containerId = "testContainerId2";
        name = "testName";
        userId = "deshani@wso2.com";

        staticScannerToPersist = new StaticScanner();
        staticScannerToPersist.setContainerId(containerId);
        staticScannerToPersist.setName(name);
        staticScannerToPersist.setUserId(userId);

        Iterable<StaticScanner> staticScanners = this.staticScannerRepository.findByUserId(containerId);
        while (staticScanners.iterator().hasNext()) {
            assertThat(staticScanners.iterator().next());
            assertThat(staticScanners.iterator().next());
        }
    }
}
