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

package org.apache.skywalking.apm.agent.core.logging.core;

import org.apache.skywalking.apm.agent.core.boot.DefaultNamedThreadFactory;
import org.apache.skywalking.apm.agent.core.boot.ServiceManager;
import org.apache.skywalking.apm.util.RunnableWithExceptionProtection;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class KafkaWriter implements IWriter {

    private static KafkaWriter INSTANCE;
    private static final Object CREATE_LOCK = new Object();

    private ArrayBlockingQueue logBuffer;

    public KafkaWriter() {
        logBuffer = new ArrayBlockingQueue(1024);

        final ArrayList<String> outputLogs = new ArrayList<String>(200);
        Executors.newSingleThreadScheduledExecutor(new DefaultNamedThreadFactory("KafkaWriter"))
                .scheduleAtFixedRate(new RunnableWithExceptionProtection(new Runnable() {
                    @Override
                    public void run() {
                        LogService logService = ServiceManager.INSTANCE.findService(LogService.class);
                        if (logService != null) {
                            logBuffer.drainTo(outputLogs);

                            try {
                                for (String message : outputLogs) {
                                    logService.write(message);
                                }

                                logService.flush();
                            } catch (Throwable t) {
                                SystemOutWriter.INSTANCE.write("skywalking-agent WARN: " + t.toString());

                                for (String message : outputLogs) {
                                    SystemOutWriter.INSTANCE.write(message);
                                }
                            } finally {
                                outputLogs.clear();
                            }
                        } else {
                            SystemOutWriter.INSTANCE.write("Could not get SkyWalking KafkaWriter service!");
                        }
                    }
                }, new RunnableWithExceptionProtection.CallbackWhenException() {
                    @Override
                    public void handle(Throwable t) {
                    }
                }), 0, 1, TimeUnit.SECONDS);
    }

    public static KafkaWriter get() {
        if (INSTANCE == null) {
            synchronized (CREATE_LOCK) {
                if (INSTANCE == null) {
                    INSTANCE = new KafkaWriter();
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public void write(String message) {
        logBuffer.offer(message);
    }
}
