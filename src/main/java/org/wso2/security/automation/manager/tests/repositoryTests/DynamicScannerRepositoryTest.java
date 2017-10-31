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
import org.wso2.security.automation.manager.entity.DynamicScanner;
import org.wso2.security.automation.manager.repository.DynamicScannerRepository;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class DynamicScannerRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private DynamicScannerRepository dynamicScannerRepository;


    @Test
    public void testExample() throws Exception {
        String containerId = "testContainerId";
        String name = "testName";
        String userId = "deshani@wso2.com";

        DynamicScanner dynamicScannerToPersist = new DynamicScanner();
        dynamicScannerToPersist.setContainerId(containerId);
        dynamicScannerToPersist.setName(name);
        dynamicScannerToPersist.setUserId(userId);

        this.entityManager.persist(dynamicScannerToPersist);
        DynamicScanner dynamicScanner = this.dynamicScannerRepository.findOneByContainerId(containerId);
        assertThat(dynamicScanner.getName()).isEqualTo(name);
        assertThat(dynamicScanner.getUserId()).isEqualTo(userId);
    }

    @Test
    public void testFindByUserId() throws Exception {
        String containerId = "testContainerId1";
        String name = "testName";
        String userId = "deshani@wso2.com";

        DynamicScanner dynamicScannerToPersist = new DynamicScanner();
        dynamicScannerToPersist.setContainerId(containerId);
        dynamicScannerToPersist.setName(name);
        dynamicScannerToPersist.setUserId(userId);

        this.entityManager.persist(dynamicScannerToPersist);
        containerId = "testContainerId2";
        name = "testName";
        userId = "deshani@wso2.com";

        dynamicScannerToPersist = new DynamicScanner();
        dynamicScannerToPersist.setContainerId(containerId);
        dynamicScannerToPersist.setName(name);
        dynamicScannerToPersist.setUserId(userId);

        Iterable<DynamicScanner> dynamicScanners = this.dynamicScannerRepository.findByUserId(containerId);
        while (dynamicScanners.iterator().hasNext()) {
            assertThat(dynamicScanners.iterator().next());
            assertThat(dynamicScanners.iterator().next());
        }
    }
}
