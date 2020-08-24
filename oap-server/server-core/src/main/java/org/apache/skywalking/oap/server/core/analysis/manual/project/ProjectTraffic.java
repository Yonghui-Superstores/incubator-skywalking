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

package org.apache.skywalking.oap.server.core.analysis.manual.project;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.skywalking.oap.server.core.Const;
import org.apache.skywalking.oap.server.core.analysis.IDManager;
import org.apache.skywalking.oap.server.core.analysis.MetricsExtension;
import org.apache.skywalking.oap.server.core.analysis.Stream;
import org.apache.skywalking.oap.server.core.analysis.metrics.Metrics;
import org.apache.skywalking.oap.server.core.analysis.worker.MetricsStreamProcessor;
import org.apache.skywalking.oap.server.core.remote.grpc.proto.RemoteData;
import org.apache.skywalking.oap.server.core.source.DefaultScopeDefine;
import org.apache.skywalking.oap.server.core.storage.StorageBuilder;
import org.apache.skywalking.oap.server.core.storage.annotation.Column;

import java.util.HashMap;
import java.util.Map;

@Stream(name = ProjectTraffic.INDEX_NAME, scopeId = DefaultScopeDefine.PROJECT, builder = ProjectTraffic.Builder.class, processor = MetricsStreamProcessor.class)
@MetricsExtension(supportDownSampling = false, supportUpdate = false)
@EqualsAndHashCode(of = {
    "name"
})
public class ProjectTraffic extends Metrics {
    public static final String INDEX_NAME = "project_traffic";

    public static final String NAME = "name";

    @Setter
    @Getter
    @Column(columnName = NAME, matchQuery = true)
    private String name = Const.EMPTY_STRING;

    @Override
    public String id() {
        return IDManager.ProjectId.buildId(name);
    }

    @Override
    public int remoteHashCode() {
        return this.hashCode();
    }

    @Override
    public void deserialize(final RemoteData remoteData) {
        setName(remoteData.getDataStrings(0));
        // Time bucket is not a part of persistent, but still is required in the first time insert.
        setTimeBucket(remoteData.getDataLongs(0));
    }

    @Override
    public RemoteData.Builder serialize() {
        final RemoteData.Builder builder = RemoteData.newBuilder();
        builder.addDataStrings(name);
        // Time bucket is not a part of persistent, but still is required in the first time insert.
        builder.addDataLongs(getTimeBucket());
        return builder;
    }

    public static class Builder implements StorageBuilder<ProjectTraffic> {

        @Override
        public ProjectTraffic map2Data(final Map<String, Object> dbMap) {
            ProjectTraffic projectTraffic = new ProjectTraffic();
            projectTraffic.setName((String) dbMap.get(NAME));
            return projectTraffic;
        }

        @Override
        public Map<String, Object> data2Map(final ProjectTraffic storageData) {
            Map<String, Object> map = new HashMap<>();
            map.put(NAME, storageData.getName());
            return map;
        }
    }

    @Override
    public void combine(final Metrics metrics) {

    }

    @Override
    public void calculate() {

    }

    @Override
    public Metrics toHour() {
        return null;
    }

    @Override
    public Metrics toDay() {
        return null;
    }
}

