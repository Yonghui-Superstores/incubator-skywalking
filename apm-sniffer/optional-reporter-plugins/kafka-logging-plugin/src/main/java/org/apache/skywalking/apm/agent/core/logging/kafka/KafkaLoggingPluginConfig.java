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

package org.apache.skywalking.apm.agent.core.logging.kafka;

import org.apache.skywalking.apm.agent.core.boot.PluginConfig;

import java.util.HashMap;
import java.util.Map;

public class KafkaLoggingPluginConfig {
    public static class Plugin {
        @PluginConfig(root = KafkaLoggingPluginConfig.class)
        public static class KafkaLogger {
            /**
             * <B>bootstrap.servers</B>: A list of host/port pairs to use for establishing the initial connection to the Kafka cluster.
             * This list should be in the form host1:port1,host2:port2,...
             */
            public static String BOOTSTRAP_SERVERS = "localhost:9092";

            public static String TOPIC_NAME = "operation-jszt-003";

            public static Map<String, String> PRODUCER_CONFIG = new HashMap<>();
        }
    }
}
