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

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.utils.Bytes;
import org.apache.skywalking.apm.agent.core.boot.OverrideImplementor;
import org.apache.skywalking.apm.agent.core.boot.ServiceManager;
import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;
import org.apache.skywalking.apm.agent.core.logging.core.LogService;
import org.apache.skywalking.apm.agent.core.logging.core.SystemOutWriter;

import java.nio.charset.StandardCharsets;

@OverrideImplementor(LogService.class)
public class KafkaLogService extends LogService {

    private static final ILog logger = LogManager.getLogger(KafkaLogService.class);

    private KafkaProducer<Bytes, Bytes> producer;
    private String topic;

    @Override
    public void prepare() throws Throwable {
        topic = KafkaLoggingPluginConfig.Plugin.KafkaLogger.TOPIC_NAME;
    }

    @Override
    public void boot() {
        producer = ServiceManager.INSTANCE.findService(KafkaProducerManager.class).getProducer();
    }

    @Override
    public void onComplete() throws Throwable {

    }

    @Override
    public void shutdown() throws Throwable {

    }

    @Override
    public void write(String message) {
        producer.send(new ProducerRecord<Bytes, Bytes>(topic, null, Bytes.wrap(message.getBytes(StandardCharsets.UTF_8))), new Callback() {
            @Override
            public void onCompletion(RecordMetadata metadata, Exception exception) {
                if (exception != null) {
                    SystemOutWriter.INSTANCE.write(exception.getMessage());
                }
            }
        });
    }

    @Override
    public void flush() throws NullPointerException {
        if (producer == null) {
            logger.warn("KafkaProducer is creating");
            return;
        }
        producer.flush();
    }
}
