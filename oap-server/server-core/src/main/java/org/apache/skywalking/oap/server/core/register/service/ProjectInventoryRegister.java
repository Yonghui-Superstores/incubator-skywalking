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

package org.apache.skywalking.oap.server.core.register.service;

import com.google.gson.JsonObject;
import java.util.Objects;
import org.apache.skywalking.oap.server.core.*;
import org.apache.skywalking.oap.server.core.cache.ProjectInventoryCache;
import org.apache.skywalking.oap.server.core.register.ProjectInventory;
import org.apache.skywalking.oap.server.core.register.worker.InventoryStreamProcessor;
import org.apache.skywalking.oap.server.library.module.ModuleDefineHolder;
import org.slf4j.*;

import static java.util.Objects.isNull;

/**
 * @author peng-yongsheng
 */
public class ProjectInventoryRegister implements IProjectInventoryRegister {

    private static final Logger logger = LoggerFactory.getLogger(ProjectInventoryRegister.class);

    private final ModuleDefineHolder moduleDefineHolder;
    private ProjectInventoryCache projectInventoryCache;

    public ProjectInventoryRegister(ModuleDefineHolder moduleDefineHolder) {
        this.moduleDefineHolder = moduleDefineHolder;
    }

    private ProjectInventoryCache getProjectInventoryCache() {
        if (isNull(projectInventoryCache)) {
            this.projectInventoryCache = moduleDefineHolder.find(CoreModule.NAME).provider().getService(ProjectInventoryCache.class);
        }
        return projectInventoryCache;
    }

    @Override public int getOrCreate(String externalProjectId, String projectName, JsonObject properties) {
        int projectId = getProjectInventoryCache().getProjectId(externalProjectId);

        if (projectId == Const.NONE) {
            ProjectInventory projectInventory = new ProjectInventory();
            projectInventory.setName(projectName);
            projectInventory.setExternalProjectId(externalProjectId);

            long now = System.currentTimeMillis();
            projectInventory.setRegisterTime(now);
            projectInventory.setHeartbeatTime(now);
            projectInventory.setLastUpdateTime(now);
            projectInventory.setProperties(properties);

            InventoryStreamProcessor.getInstance().in(projectInventory);
        }
        return projectId;
    }

    @Override public void update(int projectId, String projectName, JsonObject properties) {
        ProjectInventory projectInventory = getProjectInventoryCache().get(projectId);
        if (Objects.nonNull(projectInventory)) {
            if (properties != null || !compare(projectInventory, projectName)) {
                projectInventory = projectInventory.getClone();
                projectInventory.setName(projectName);
                projectInventory.setProperties(properties);
                projectInventory.setLastUpdateTime(System.currentTimeMillis());

                InventoryStreamProcessor.getInstance().in(projectInventory);
            }
        } else {
            logger.warn("Project {} name/properties update, but not found in storage.", projectId);
        }
    }

    @Override public void heartbeat(int projectId, long heartBeatTime) {
        ProjectInventory projectInventory = getProjectInventoryCache().get(projectId);
        if (Objects.nonNull(projectInventory)) {
            projectInventory = projectInventory.getClone();
            projectInventory.setHeartbeatTime(heartBeatTime);

            InventoryStreamProcessor.getInstance().in(projectInventory);
        } else {
            logger.warn("Project {} heartbeat, but not found in storage.", projectId);
        }
    }

    private boolean compare(ProjectInventory newProjectInventory, String projectName) {
        if (Objects.nonNull(newProjectInventory)) {
            return projectName.equals(newProjectInventory.getName());
        }
        return true;
    }
}
