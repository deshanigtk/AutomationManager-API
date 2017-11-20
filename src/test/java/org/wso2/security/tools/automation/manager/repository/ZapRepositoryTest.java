///*
// * Copyright (c) ${date}, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
// *
// * WSO2 Inc. licenses this file to you under the Apache License,
// * Version 2.0 (the "License"); you may not use this file except
// * in compliance with the License.
// * You may obtain a copy of the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied.  See the License for the
// * specific language governing permissions and limitations
// * under the License.
// */
//
//package org.wso2.security.automation.manager.repository;
//
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
//import org.springframework.test.context.junit4.SpringRunner;
//import ZapEntity;
//
//import static org.junit.Assert.assertEquals;
//
//@RunWith(SpringRunner.class)
//@DataJpaTest
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
//public class ZapRepositoryTest {
//
//
//    @Autowired
//    private TestEntityManager entityManager;
//
//    @Autowired
//    private ZapRepository zapRepository;
//
//
//    @Test
//    public void testExample() throws Exception {
//        String containerId = "testContainerId";
//        String name = "testName";
//        String userId = "test@test.com";
//
//        ZapEntity zapToPersist = new ZapEntity();
//        zapToPersist.setContainerId(containerId);
//        zapToPersist.setName(name);
//        zapToPersist.setUserId(userId);
//
//        entityManager.persist(zapToPersist);
//        ZapEntity zap = zapRepository.findOneByContainerId(containerId);
//        assertEquals(name, zap.getName());
//    }
//
//    @Test
//    public void testFindByUserId() throws Exception {
//        String containerId = "testContainerId";
//        String name = "testName";
//        String userId = "test@test.com";
//
//        ZapEntity zapToPersist = new ZapEntity();
//        zapToPersist.setContainerId(containerId);
//        zapToPersist.setName(name);
//        zapToPersist.setUserId(userId);
//
//        entityManager.persist(zapToPersist);
//
//        Iterable<ZapEntity> zaps = zapRepository.findByUserId(containerId);
//        while (zaps.iterator().hasNext()) {
//            assertEquals(zapToPersist, zaps.iterator().next());
//        }
//    }
//}
