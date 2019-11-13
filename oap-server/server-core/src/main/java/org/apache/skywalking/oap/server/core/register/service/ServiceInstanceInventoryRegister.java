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
import org.apache.skywalking.oap.server.core.cache.ServiceInstanceInventoryCache;
import org.apache.skywalking.oap.server.core.register.ServiceInstanceInventory;
import org.apache.skywalking.oap.server.core.register.worker.InventoryStreamProcessor;
import org.apache.skywalking.oap.server.library.module.ModuleDefineHolder;
import org.apache.skywalking.oap.server.library.util.BooleanUtils;
import org.slf4j.*;

import static java.util.Objects.isNull;

/**
 * @author peng-yongsheng
 */
public class ServiceInstanceInventoryRegister implements IServiceInstanceInventoryRegister {

    private static final Logger logger = LoggerFactory.getLogger(ServiceInstanceInventoryRegister.class);

    private final ModuleDefineHolder moduleDefineHolder;
    private ServiceInstanceInventoryCache serviceInstanceInventoryCache;

    public ServiceInstanceInventoryRegister(ModuleDefineHolder moduleDefineHolder) {
        this.moduleDefineHolder = moduleDefineHolder;
    }

    private ServiceInstanceInventoryCache getServiceInstanceInventoryCache() {
        if (isNull(serviceInstanceInventoryCache)) {
            serviceInstanceInventoryCache = moduleDefineHolder.find(CoreModule.NAME).provider().getService(ServiceInstanceInventoryCache.class);
        }
        return serviceInstanceInventoryCache;
    }

    @Override
    public int getOrCreate(int serviceId, String serviceInstanceName, String uuid, long registerTime, JsonObject properties) {
        return getOrCreate(Const.NONE,serviceId,serviceInstanceName,uuid,registerTime,properties);
    }

    @Override public int getOrCreate(int projectId, int serviceId, String serviceInstanceName, String uuid, long registerTime,
                                     JsonObject properties) {
        if (logger.isDebugEnabled()) {
            logger.debug("Get or create service instance by service instance name, service id: {}, service instance name: {},uuid: {}, registerTime: {}", serviceId, serviceInstanceName, uuid, registerTime);
        }

        int serviceInstanceId = getServiceInstanceInventoryCache().getServiceInstanceId(serviceId, uuid);

        if (serviceInstanceId == Const.NONE) {
            ServiceInstanceInventory serviceInstanceInventory = new ServiceInstanceInventory();
            serviceInstanceInventory.setServiceId(serviceId);
            serviceInstanceInventory.setName(serviceInstanceName);
            serviceInstanceInventory.setInstanceUUID(uuid);
            serviceInstanceInventory.setIsAddress(BooleanUtils.FALSE);
            serviceInstanceInventory.setAddressId(Const.NONE);

            serviceInstanceInventory.setRegisterTime(registerTime);
            serviceInstanceInventory.setHeartbeatTime(registerTime);

            serviceInstanceInventory.setProperties(properties);

            serviceInstanceInventory.setProjectId(projectId);

            InventoryStreamProcessor.getInstance().in(serviceInstanceInventory);
        }
        return serviceInstanceId;
    }

    @Override public int getOrCreate(int serviceId, int addressId, long registerTime) {
        if (logger.isDebugEnabled()) {
            logger.debug("get or create service instance by getAddress id, service id: {}, getAddress id: {}, registerTime: {}", serviceId, addressId, registerTime);
        }

        int serviceInstanceId = getServiceInstanceInventoryCache().getServiceInstanceId(serviceId, addressId);

        if (serviceInstanceId == Const.NONE) {
            ServiceInstanceInventory serviceInstanceInventory = new ServiceInstanceInventory();
            serviceInstanceInventory.setServiceId(serviceId);
            serviceInstanceInventory.setName(Const.EMPTY_STRING);
            serviceInstanceInventory.setIsAddress(BooleanUtils.TRUE);
            serviceInstanceInventory.setAddressId(addressId);

            serviceInstanceInventory.setRegisterTime(registerTime);
            serviceInstanceInventory.setHeartbeatTime(registerTime);

            InventoryStreamProcessor.getInstance().in(serviceInstanceInventory);
        }
        return serviceInstanceId;
    }

    @Override public void heartbeat(int serviceInstanceId, long heartBeatTime) {
        ServiceInstanceInventory serviceInstanceInventory = getServiceInstanceInventoryCache().get(serviceInstanceId);
        if (Objects.nonNull(serviceInstanceInventory)) {
            serviceInstanceInventory.setHeartbeatTime(heartBeatTime);
            InventoryStreamProcessor.getInstance().in(serviceInstanceInventory);
        } else {
            logger.warn("Service instance {} heartbeat, but not found in storage.", serviceInstanceId);
        }
    }
}
