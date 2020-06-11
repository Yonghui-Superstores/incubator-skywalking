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

package org.apache.skywalking.oap.server.core.query;

import org.apache.skywalking.oap.server.core.Const;
import org.apache.skywalking.oap.server.core.UnexpectedException;
import org.apache.skywalking.oap.server.core.analysis.manual.service.ServiceFailureDotHeatMapMetrics;
import org.apache.skywalking.oap.server.core.analysis.manual.service.ServiceSuccessDotHeatMapMetrics;
import org.apache.skywalking.oap.server.core.analysis.metrics.IntKeyLongValue;
import org.apache.skywalking.oap.server.core.analysis.metrics.IntKeyLongValueHashMap;
import org.apache.skywalking.oap.server.core.analysis.metrics.ThermodynamicMetrics;
import org.apache.skywalking.oap.server.core.cache.ProjectInventoryCache;
import org.apache.skywalking.oap.server.core.query.entity.Dots;
import org.apache.skywalking.oap.server.core.storage.StorageModule;
import org.apache.skywalking.oap.server.core.storage.query.IServiceDotsQueryDAO;
import org.apache.skywalking.oap.server.library.module.ModuleManager;
import org.apache.skywalking.oap.server.library.module.Service;
import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;

/**
 * @author peng-yongsheng
 */
public class ServiceDotsQueryService implements Service {

    private static final Logger logger = LoggerFactory.getLogger(ServiceDotsQueryService.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormat.forPattern("yyyyMMddHHmmss");

    private final ModuleManager moduleManager;
    private IServiceDotsQueryDAO dotsQueryDAO;

    public ServiceDotsQueryService(ModuleManager moduleManager) {
        this.moduleManager = moduleManager;
    }

    private IServiceDotsQueryDAO getDotsQueryDAO() {
        if (dotsQueryDAO == null) {
            dotsQueryDAO = moduleManager.find(StorageModule.NAME).provider().getService(IServiceDotsQueryDAO.class);
        }
        return dotsQueryDAO;
    }

    public Dots getDots(final boolean isError, final String serviceId, final long startTimeBucket, final long endTimeBucket, final int axisXStep) throws IOException, ParseException {
        String[][] idGroups = idGroups(serviceId, startTimeBucket, endTimeBucket, axisXStep);

        Map<String, ? extends ThermodynamicMetrics> dotHeatmap;
        if (isError) {
            dotHeatmap = getDotsQueryDAO().getFailureDots("service_failure_dot_heatmap", ids(idGroups), startTimeBucket, endTimeBucket);
        } else {
            dotHeatmap = getDotsQueryDAO().getSuccessDots("service_success_dot_heatmap", ids(idGroups), startTimeBucket, endTimeBucket);
        }

        List<ThermodynamicMetrics> thermodynamicMetrics = new ArrayList<>();
        for (int i = 0; i < idGroups.length; i++) {
            String[] ids = idGroups[i];
            ThermodynamicMetrics metrics = null;
            for (String id : ids) {
                if (metrics == null) {
                    metrics = dotHeatmap.get(id);
                } else if (dotHeatmap.containsKey(id)) {
                    metrics.combine(dotHeatmap.get(id));
                }
            }

            if (metrics != null) {
                thermodynamicMetrics.add(metrics);
            } else {
                thermodynamicMetrics.add(new ServiceSuccessDotHeatMapMetrics());
            }
        }

        Dots dots = new Dots();
        for (int i = 0; i < thermodynamicMetrics.size(); i++) {
            ThermodynamicMetrics metrics = thermodynamicMetrics.get(i);
            if (!metrics.getDetailGroup().isEmpty()) {
                Collection<IntKeyLongValue> values = metrics.getDetailGroup().values();
                for (IntKeyLongValue value : values) {
                    dots.addNode(i, value.getKey() * metrics.getStep(), value.getValue());
                }
            }
        }

        return dots;
    }

    public List<String> ids(String[][] idGroups) {
        List<String> ids = new ArrayList<>();

        for (String[] idGroup : idGroups) {
            ids.addAll(Arrays.asList(idGroup));
        }

        return ids;
    }

    public String[][] idGroups(final String serviceId, final long startTimeBucket, final long endTimeBucket, final int step) {
        DateTime startTime = DateTime.parse(String.valueOf(startTimeBucket), FORMATTER);
        DateTime endTime = DateTime.parse(String.valueOf(endTimeBucket), FORMATTER);

        int seconds = Seconds.secondsBetween(startTime, endTime).getSeconds();
        if (seconds > 5000) {
            throw new UnexpectedException("The max step from start time to end time is 5000.");
        }

        double x = Math.ceil((double) seconds / (double) step);

        String[][] ids = new String[(int) x][step];

        int axisX = 0;
        for (int i = 0; i < seconds; i++) {
            ids[axisX] = new String[step];

            for (int s = 0; s < step; s++) {
                ids[axisX][s] = startTime.toString(FORMATTER) + Const.ID_SPLIT + serviceId;
                startTime = startTime.plusSeconds(1);
            }
            i = i + (step - 1);

            axisX++;
        }

        return ids;
    }
}
