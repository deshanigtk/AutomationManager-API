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

package org.wso2.security.automation.manager.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;
import org.wso2.security.automation.manager.entity.DynamicScannerEntity;

import static org.junit.Assert.assertEquals;

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
        String userId = "test@test.com";

        DynamicScannerEntity dynamicScannerToPersist = new DynamicScannerEntity();
        dynamicScannerToPersist.setContainerId(containerId);
        dynamicScannerToPersist.setTestName(name);
        dynamicScannerToPersist.setUserId(userId);

        entityManager.persist(dynamicScannerToPersist);
        DynamicScannerEntity dynamicScanner = dynamicScannerRepository.findOneByContainerId(containerId);
        assertEquals(name, dynamicScanner.getTestName());
    }

    @Test
    public void testFindByUserId() throws Exception {
        String containerId = "testContainerId";
        String name = "testName";
        String userId = "test@test.com";

        DynamicScannerEntity dynamicScannerToPersist = new DynamicScannerEntity();
        dynamicScannerToPersist.setContainerId(containerId);
        dynamicScannerToPersist.setTestName(name);
        dynamicScannerToPersist.setUserId(userId);

        entityManager.persist(dynamicScannerToPersist);

        Iterable<DynamicScannerEntity> dynamicScanners = dynamicScannerRepository.findByUserId(containerId);
        while (dynamicScanners.iterator().hasNext()) {
            assertEquals(dynamicScannerToPersist, dynamicScanners.iterator().next());
        }
    }
}
