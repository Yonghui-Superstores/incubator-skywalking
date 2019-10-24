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

package org.apache.skywalking.oap.server.core.register;

import com.google.common.base.Strings;
import com.google.gson.*;
import java.util.*;
import lombok.*;
import org.apache.skywalking.oap.server.core.Const;
import org.apache.skywalking.oap.server.core.analysis.Stream;
import org.apache.skywalking.oap.server.core.register.worker.InventoryStreamProcessor;
import org.apache.skywalking.oap.server.core.remote.grpc.proto.RemoteData;
import org.apache.skywalking.oap.server.core.source.*;
import org.apache.skywalking.oap.server.core.storage.StorageBuilder;
import org.apache.skywalking.oap.server.core.storage.annotation.Column;

import static org.apache.skywalking.oap.server.core.source.DefaultScopeDefine.PROJECT_INVENTORY;

/**
 * @author peng-yongsheng
 */
@ScopeDeclaration(id = PROJECT_INVENTORY, name = "ProjectInventory")
@Stream(name = ProjectInventory.INDEX_NAME, scopeId = DefaultScopeDefine.SERVICE_INVENTORY, builder = ProjectInventory.Builder.class, processor = InventoryStreamProcessor.class)
public class ProjectInventory extends RegisterSource {

    public static final String INDEX_NAME = "project_inventory";

    public static final String NAME = "name";
    public static final String EXTERNAL_PROJECT_ID = "external_project_id";
    public static final String PROPERTIES = "properties";
    private static final Gson GSON = new Gson();

    @Setter @Getter @Column(columnName = NAME, matchQuery = true) private String name = Const.EMPTY_STRING;
    @Setter @Getter @Column(columnName = EXTERNAL_PROJECT_ID) private String externalProjectId;
    @Getter(AccessLevel.PRIVATE) @Column(columnName = PROPERTIES) private String prop = Const.EMPTY_JSON_OBJECT_STRING;
    @Getter private JsonObject properties;

    public static String buildId(String externalProjectId) {
        return externalProjectId;
    }

    @Override public String id() {
        return buildId(externalProjectId);
    }

    @Override public int hashCode() {
        int result = 17;
        result = 31 * result + externalProjectId.hashCode();
        return result;
    }

    public void setProperties(JsonObject properties) {
        this.properties = properties;
        if (properties != null && properties.keySet().size() > 0) {
            this.prop = properties.toString();
        }
    }

    private void setProp(String prop) {
        this.prop = prop;
        if (!Strings.isNullOrEmpty(prop)) {
            this.properties = GSON.fromJson(prop, JsonObject.class);
        }
    }

    public boolean hasProperties() {
        return prop != null && prop.length() > 0;
    }

    public ProjectInventory getClone() {
        ProjectInventory inventory = new ProjectInventory();
        inventory.setSequence(getSequence());
        inventory.setRegisterTime(getRegisterTime());
        inventory.setHeartbeatTime(getHeartbeatTime());
        inventory.setName(name);
        inventory.setExternalProjectId(externalProjectId);
        inventory.setLastUpdateTime(getLastUpdateTime());
        inventory.setProp(prop);

        return inventory;
    }

    @Override public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;

        ProjectInventory source = (ProjectInventory)obj;
        if (!name.equals(source.getName()))
            return false;
        if (!externalProjectId.equals(source.getExternalProjectId()))
            return false;

        return true;
    }

    @Override public RemoteData.Builder serialize() {
        RemoteData.Builder remoteBuilder = RemoteData.newBuilder();
        remoteBuilder.addDataIntegers(getSequence());

        remoteBuilder.addDataLongs(getRegisterTime());
        remoteBuilder.addDataLongs(getHeartbeatTime());
        remoteBuilder.addDataLongs(getLastUpdateTime());

        remoteBuilder.addDataStrings(Strings.isNullOrEmpty(name) ? Const.EMPTY_STRING : name);
        remoteBuilder.addDataStrings(Strings.isNullOrEmpty(externalProjectId) ? Const.EMPTY_STRING : externalProjectId);
        remoteBuilder.addDataStrings(Strings.isNullOrEmpty(prop) ? Const.EMPTY_STRING : prop);
        return remoteBuilder;
    }

    @Override public void deserialize(RemoteData remoteData) {
        setSequence(remoteData.getDataIntegers(0));

        setRegisterTime(remoteData.getDataLongs(0));
        setHeartbeatTime(remoteData.getDataLongs(1));
        setLastUpdateTime(remoteData.getDataLongs(2));

        setName(remoteData.getDataStrings(0));
        setExternalProjectId(remoteData.getDataStrings(1));
        setProp(remoteData.getDataStrings(2));
    }

    @Override public int remoteHashCode() {
        return 0;
    }

    @Override public boolean combine(RegisterSource registerSource) {
        boolean isChanged = super.combine(registerSource);
        ProjectInventory inventory = (ProjectInventory)registerSource;

        if (inventory.getLastUpdateTime() >= this.getLastUpdateTime()) {
            this.name = inventory.getName();
            setProp(inventory.getProp());
            isChanged = true;
        }

        return isChanged;
    }

    public static class Builder implements StorageBuilder<ProjectInventory> {

        @Override public ProjectInventory map2Data(Map<String, Object> dbMap) {
            ProjectInventory inventory = new ProjectInventory();
            inventory.setSequence(((Number)dbMap.get(SEQUENCE)).intValue());
            inventory.setName((String)dbMap.get(NAME));
            inventory.setExternalProjectId((String)dbMap.get(EXTERNAL_PROJECT_ID));
            inventory.setRegisterTime(((Number)dbMap.get(REGISTER_TIME)).longValue());
            inventory.setHeartbeatTime(((Number)dbMap.get(HEARTBEAT_TIME)).longValue());
            inventory.setLastUpdateTime(((Number)dbMap.get(LAST_UPDATE_TIME)).longValue());
            inventory.setProp((String)dbMap.get(PROPERTIES));
            return inventory;
        }

        @Override public Map<String, Object> data2Map(ProjectInventory storageData) {
            Map<String, Object> map = new HashMap<>();
            map.put(SEQUENCE, storageData.getSequence());
            map.put(NAME, storageData.getName());
            map.put(EXTERNAL_PROJECT_ID, storageData.getExternalProjectId());
            map.put(REGISTER_TIME, storageData.getRegisterTime());
            map.put(HEARTBEAT_TIME, storageData.getHeartbeatTime());
            map.put(LAST_UPDATE_TIME, storageData.getLastUpdateTime());
            map.put(PROPERTIES, storageData.getProp());
            return map;
        }
    }
}
