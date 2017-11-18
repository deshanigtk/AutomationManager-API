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

import org.springframework.data.repository.CrudRepository;
import org.wso2.security.automation.manager.entity.scanner.dynamic.ZapEntity;

/**
 * Zap repository
 *
 * @author Deshani Geethika
 */
public interface ZapRepository extends CrudRepository<ZapEntity, Integer> {

    ZapEntity findOneByContainerId(String containerId);

    Iterable<ZapEntity> findByUserId(String userId);
}
