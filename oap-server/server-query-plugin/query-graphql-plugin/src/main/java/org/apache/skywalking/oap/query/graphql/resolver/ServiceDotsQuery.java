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

package org.apache.skywalking.oap.query.graphql.resolver;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import org.apache.skywalking.oap.query.graphql.type.Duration;
import org.apache.skywalking.oap.server.core.CoreModule;
import org.apache.skywalking.oap.server.core.query.DurationUtils;
import org.apache.skywalking.oap.server.core.query.ServiceDotsQueryService;
import org.apache.skywalking.oap.server.core.query.entity.Dots;
import org.apache.skywalking.oap.server.core.query.entity.Step;
import org.apache.skywalking.oap.server.library.module.ModuleManager;

import java.io.IOException;
import java.text.ParseException;

/**
 * @author peng-yongsheng
 */
public class ServiceDotsQuery implements GraphQLQueryResolver {

    private final ModuleManager moduleManager;
    private ServiceDotsQueryService dotsQueryService;

    public ServiceDotsQuery(ModuleManager moduleManager) {
        this.moduleManager = moduleManager;
    }

    private ServiceDotsQueryService getDotsQueryService() {
        if (dotsQueryService == null) {
            this.dotsQueryService = moduleManager.find(CoreModule.NAME).provider().getService(ServiceDotsQueryService.class);
        }
        return dotsQueryService;
    }

    public Dots getDots(final boolean isError, final String serviceId, final Duration duration, final int axisXStep) throws IOException, ParseException {
        if (!duration.getStep().equals(Step.SECOND)) {
            throw new UnsupportedOperationException("Just support second step.");
        }

        long startTimeBucket = DurationUtils.INSTANCE.exchangeToTimeBucket(duration.getStart());
        long endTimeBucket = DurationUtils.INSTANCE.exchangeToTimeBucket(duration.getEnd());
        return getDotsQueryService().getDots(isError, serviceId, startTimeBucket, endTimeBucket, axisXStep);
    }
}
