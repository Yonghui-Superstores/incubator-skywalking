/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.skywalking.oap.server.core.cache;

import com.google.common.cache.*;
import java.util.Objects;
import org.apache.skywalking.oap.server.core.Const;
import org.apache.skywalking.oap.server.core.register.ProjectInventory;
import org.apache.skywalking.oap.server.core.storage.StorageModule;
import org.apache.skywalking.oap.server.core.storage.cache.IProjectInventoryCacheDAO;
import org.apache.skywalking.oap.server.library.module.*;
import org.slf4j.*;

import static java.util.Objects.*;

/**
 * @author peng-yongsheng
 */
public class ProjectInventoryCache implements Service {

    private static final Logger logger = LoggerFactory.getLogger(ProjectInventoryCache.class);

    private final Cache<String, Integer> projectCache = CacheBuilder.newBuilder().initialCapacity(100).maximumSize(1000).build();
    private final Cache<Integer, ProjectInventory> projectIdCache = CacheBuilder.newBuilder().initialCapacity(100).maximumSize(1000).build();

    private final ModuleManager moduleManager;
    private IProjectInventoryCacheDAO cacheDAO;

    public ProjectInventoryCache(ModuleManager moduleManager) {
        this.moduleManager = moduleManager;
    }

    private IProjectInventoryCacheDAO getCacheDAO() {
        if (isNull(cacheDAO)) {
            this.cacheDAO = moduleManager.find(StorageModule.NAME).provider().getService(IProjectInventoryCacheDAO.class);
        }
        return this.cacheDAO;
    }

    public int getProjectId(String externalProjectId) {
        Integer projectId = projectCache.getIfPresent(ProjectInventory.buildId(externalProjectId));

        if (Objects.isNull(projectId) || projectId == Const.NONE) {
            projectId = getCacheDAO().getProjectId(externalProjectId);
            if (projectId != Const.NONE) {
                projectCache.put(ProjectInventory.buildId(externalProjectId), projectId);
            }
        }
        return projectId;
    }

    public ProjectInventory get(int projectId) {
        if (logger.isDebugEnabled()) {
            logger.debug("Get project by id {} from cache", projectId);
        }

        ProjectInventory projectInventory = projectIdCache.getIfPresent(projectId);

        if (isNull(projectInventory)) {
            projectInventory = getCacheDAO().get(projectId);
            if (nonNull(projectInventory)) {
                projectIdCache.put(projectId, projectInventory);
            }
        }

        if (logger.isDebugEnabled()) {
            if (Objects.isNull(projectInventory)) {
                logger.debug("project id {} not find in cache.", projectId);
            }
        }

        return projectInventory;
    }
}
