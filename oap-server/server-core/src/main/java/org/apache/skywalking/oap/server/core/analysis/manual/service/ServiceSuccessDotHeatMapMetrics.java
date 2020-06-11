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

package org.apache.skywalking.oap.server.core.analysis.manual.service;

import lombok.Getter;
import lombok.Setter;
import org.apache.skywalking.oap.server.core.Const;
import org.apache.skywalking.oap.server.core.analysis.Stream;
import org.apache.skywalking.oap.server.core.analysis.metrics.IntKeyLongValueHashMap;
import org.apache.skywalking.oap.server.core.analysis.metrics.Metrics;
import org.apache.skywalking.oap.server.core.analysis.metrics.ThermodynamicMetrics;
import org.apache.skywalking.oap.server.core.analysis.worker.SecondMetricsStreamProcessor;
import org.apache.skywalking.oap.server.core.remote.grpc.proto.RemoteData;
import org.apache.skywalking.oap.server.core.source.DefaultScopeDefine;
import org.apache.skywalking.oap.server.core.storage.StorageBuilder;
import org.apache.skywalking.oap.server.core.storage.annotation.Column;
import org.apache.skywalking.oap.server.core.storage.annotation.IDColumn;

import java.util.HashMap;
import java.util.Map;

@Stream(name = ServiceSuccessDotHeatMapMetrics.INDEX_NAME, scopeId = DefaultScopeDefine.SERVICE, builder = ServiceSuccessDotHeatMapMetrics.Builder.class, processor = SecondMetricsStreamProcessor.class)
public class ServiceSuccessDotHeatMapMetrics extends ThermodynamicMetrics {

    public static final String INDEX_NAME = "service_success_dot_heatmap";

    @Setter
    @Getter
    @Column(columnName = ENTITY_ID)
    @IDColumn
    private String entityId;

    @Override
    public String id() {
        String splitJointId = String.valueOf(getTimeBucket());
        splitJointId += Const.ID_SPLIT + entityId;
        return splitJointId;
    }

    @Override
    public int remoteHashCode() {
        int result = 17;
        result = 31 * result + entityId.hashCode();
        return result;
    }

    @Override
    public void deserialize(RemoteData remoteData) {
        setTimeBucket(remoteData.getDataLongs(0));
        setEntityId(remoteData.getDataStrings(0));
        setStep(remoteData.getDataIntegers(0));
        setNumOfSteps(remoteData.getDataIntegers(1));

        setDetailGroup(new org.apache.skywalking.oap.server.core.analysis.metrics.IntKeyLongValueHashMap(30));

        for (org.apache.skywalking.oap.server.core.remote.grpc.proto.IntKeyLongValuePair element : remoteData.getDataIntLongPairListList()) {
            super.getDetailGroup().put(element.getKey(), new org.apache.skywalking.oap.server.core.analysis.metrics.IntKeyLongValue(element.getKey(), element.getValue()));
        }
    }

    @Override
    public RemoteData.Builder serialize() {
        RemoteData.Builder remoteBuilder = RemoteData.newBuilder();

        remoteBuilder.addDataLongs(getTimeBucket());
        remoteBuilder.addDataStrings(getEntityId());
        remoteBuilder.addDataIntegers(getStep());
        remoteBuilder.addDataIntegers(getNumOfSteps());

        for (org.apache.skywalking.oap.server.core.analysis.metrics.IntKeyLongValue intKeyLongValue : super.getDetailGroup().values()) {
            remoteBuilder.addDataIntLongPairList(intKeyLongValue.serialize());
        }
        return remoteBuilder;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + entityId.hashCode();
        result = 31 * result + (int) getTimeBucket();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;

        ServiceSuccessDotHeatMapMetrics metrics = (ServiceSuccessDotHeatMapMetrics) obj;
        if (!entityId.equals(metrics.entityId))
            return false;

        if (getTimeBucket() != metrics.getTimeBucket())
            return false;

        return true;
    }

    public static class Builder implements StorageBuilder<ServiceSuccessDotHeatMapMetrics> {

        @Override
        public ServiceSuccessDotHeatMapMetrics map2Data(Map<String, Object> dbMap) {
            ServiceSuccessDotHeatMapMetrics metrics = new ServiceSuccessDotHeatMapMetrics();
            metrics.setTimeBucket(((Number) dbMap.get(TIME_BUCKET)).longValue());
            metrics.setEntityId((String) dbMap.get(ENTITY_ID));
            metrics.setStep(((Number) dbMap.get(STEP)).intValue());
            metrics.setNumOfSteps(((Number) dbMap.get(NUM_OF_STEPS)).intValue());
            metrics.setDetailGroup(new IntKeyLongValueHashMap((String) dbMap.get(DETAIL_GROUP)));
            return metrics;
        }

        @Override
        public Map<String, Object> data2Map(ServiceSuccessDotHeatMapMetrics storageData) {
            Map<String, Object> map = new HashMap<>();
            map.put(TIME_BUCKET, storageData.getTimeBucket());
            map.put(ENTITY_ID, storageData.getEntityId());
            map.put(STEP, storageData.getStep());
            map.put(NUM_OF_STEPS, storageData.getNumOfSteps());
            map.put(DETAIL_GROUP, storageData.getDetailGroup());
            return map;
        }
    }


    @Override
    public Metrics toHour() {
        throw new UnsupportedOperationException("Just supported second metrics.");
    }

    @Override
    public Metrics toDay() {
        throw new UnsupportedOperationException("Just supported second metrics.");
    }

    @Override
    public Metrics toMonth() {
        throw new UnsupportedOperationException("Just supported second metrics.");
    }
}
