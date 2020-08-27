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

package org.apache.skywalking.oap.server.core.source;

import lombok.Getter;
import lombok.Setter;
import org.apache.skywalking.oap.server.core.analysis.IDManager;
import org.apache.skywalking.oap.server.core.analysis.NodeType;

import static org.apache.skywalking.oap.server.core.source.DefaultScopeDefine.SERVICE_META;

@Getter
@Setter
@ScopeDeclaration(id = SERVICE_META, name = "ServiceMeta")
@ScopeDefaultColumn.VirtualColumnDefinition(fieldName = "entityId", columnName = "entity_id", isID = true, type = String.class)
public class ServiceMeta extends Source {
    @Override
    public int scope() {
        return DefaultScopeDefine.SERVICE_META;
    }

    @Override
    public String getEntityId() {
        return IDManager.ServiceID.buildId(name, NodeType.Normal);
    }

    private String name;
    private NodeType nodeType;

    @Getter
    @Setter
    @ScopeDefaultColumn.DefinedByField(columnName = "project_id")
    private String projectId;

    @Override
    public void prepare() {
        projectId = IDManager.ProjectId.buildId(IDManager.ProjectId.getProjectName(name));
    }
}
