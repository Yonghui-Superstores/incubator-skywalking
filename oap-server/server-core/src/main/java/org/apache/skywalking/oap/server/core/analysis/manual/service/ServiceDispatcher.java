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

import org.apache.skywalking.oap.server.core.analysis.SourceDispatcher;
import org.apache.skywalking.oap.server.core.analysis.worker.SecondMetricsStreamProcessor;
import org.apache.skywalking.oap.server.core.source.Segment;
import org.apache.skywalking.oap.server.library.util.BooleanUtils;

import java.util.Random;

/**
 * @author peng-yongsheng
 */
public class ServiceDispatcher implements SourceDispatcher<Segment> {

    private static final int STEP = 50;
    private static final int NUM_OF_STEPS = 200;
    private Random random = new Random();

    @Override
    public void dispatch(Segment source) {
        if (BooleanUtils.valueToBoolean(source.getIsError())) {
            failureDot(source);
        } else {
            successDot(source);
        }
    }

    private void successDot(Segment source) {
        ServiceSuccessDotHeatMapMetrics metrics = new ServiceSuccessDotHeatMapMetrics();
        metrics.setTimeBucket(source.getTimeBucket());
        metrics.setEntityId(String.valueOf(source.getServiceId()));
//        metrics.combine(source.getLatency(), STEP, NUM_OF_STEPS);
        metrics.combine(random.nextInt(10000), STEP, NUM_OF_STEPS);
        SecondMetricsStreamProcessor.getInstance().in(metrics);
    }

    private void failureDot(Segment source) {
        ServiceFailureDotHeatMapMetrics metrics = new ServiceFailureDotHeatMapMetrics();
        metrics.setTimeBucket(source.getTimeBucket());
        metrics.setEntityId(String.valueOf(source.getServiceId()));
//        metrics.combine(source.getLatency(), STEP, NUM_OF_STEPS);
        metrics.combine(random.nextInt(10000), STEP, NUM_OF_STEPS);
        SecondMetricsStreamProcessor.getInstance().in(metrics);
    }
}
