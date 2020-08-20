/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.skywalking.apm.agent.core.logging;

import org.apache.skywalking.apm.agent.core.boot.BootService;
import org.apache.skywalking.apm.agent.core.boot.DefaultImplementor;
import org.apache.skywalking.apm.agent.core.boot.DefaultNamedThreadFactory;
import org.apache.skywalking.apm.agent.core.conf.Config;
import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;
import org.apache.skywalking.apm.agent.core.logging.core.LogLevel;
import org.apache.skywalking.apm.util.RunnableWithExceptionProtection;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@DefaultImplementor
public class LoggingService implements BootService, Runnable {

    private static final ILog LOGGER = LogManager.getLogger(LoggingService.class);

    private boolean debug = false;
    private LogLevel levelSetting = LogLevel.INFO;
    private volatile ScheduledFuture<?> logLevelScanFuture;

    @Override
    public void prepare() throws Throwable {
    }

    @Override
    public void boot() throws Throwable {
        levelSetting = Config.Logging.LEVEL;
        if (levelSetting.equals(LogLevel.DEBUG)) {
            debug = true;
            return;
        }

        logLevelScanFuture = Executors
                .newSingleThreadScheduledExecutor(new DefaultNamedThreadFactory("LoggingService-produce"))
                .scheduleAtFixedRate(new RunnableWithExceptionProtection(this, new RunnableWithExceptionProtection.CallbackWhenException() {
                    @Override
                    public void handle(Throwable t) {
                        LOGGER.error("LoggingService failed to execute.", t);
                    }
                }), 0, 1, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        boolean scanResult = scan();
        if (scanResult && !debug) {
            debug = true;
            Config.Logging.LEVEL = LogLevel.DEBUG;
            LOGGER.info("Change the logging level to " + LogLevel.DEBUG.name());
        } else if (!scanResult && debug) {
            debug = false;
            Config.Logging.LEVEL = levelSetting;
            LOGGER.info("Change the logging level to " + levelSetting.name());
        }
    }

    @Override
    public void onComplete() throws Throwable {

    }

    @Override
    public void shutdown() throws Throwable {
        logLevelScanFuture.cancel(true);
    }

    private boolean scan() {
        File debug = new File(Config.Logging.DIR + "/debug");
        return debug.exists();
    }
}
