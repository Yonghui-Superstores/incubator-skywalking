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

package org.apache.skywalking.apm.agent.core.jvm.cpu;

import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;
import org.apache.skywalking.apm.network.common.v3.CPU;

/**
 * The unit of CPU usage is 1/10000. The backend is using `avg` func directly, and query for percentage requires this
 * unit.
 */
public abstract class CPUMetricsAccessor {
    private double lastCPUTimeNs;
    private long lastSampleTimeNs;
    private final int cpuCoreNum;
    ILog logger = LogManager.getLogger(CPUMetricsAccessor.class);

    public CPUMetricsAccessor(int cpuCoreNum) {
        this.cpuCoreNum = cpuCoreNum;
    }

    protected void init() {
        lastCPUTimeNs = this.getCpuTime();
        lastSampleTimeNs = System.nanoTime();
    }

    protected abstract long getCpuTime();

    protected abstract double getCpuLoader();

    public CPU getCPUMetrics() {
        double percentCpuLoad = this.getCpuLoader();

        CPU.Builder cpuBuilder = CPU.newBuilder();
        logger.info("CPUMetricsAccessor CpuLoader Number:" + (percentCpuLoad * 100));
        return cpuBuilder.setUsagePercent(percentCpuLoad * 100).build();
    }
}
