/**
 * Copyright © 2017 Jeremy Custenborder (jcustenborder@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.sduran.kafka.connect.flume;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.api.RpcClient;
import org.apache.flume.api.RpcClientFactory;
import org.apache.flume.event.SimpleEvent;
import org.apache.kafka.connect.source.SourceRecord;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

public class FlumeAvroSourceTaskTest extends BaseSourceTaskTest<FlumeAvroSourceConnectorConfig, FlumeAvroSourceTask> {
  private static final Logger log = LoggerFactory.getLogger(FlumeAvroSourceTaskTest.class);

  @Override
  protected Map<String, String> settings() {
    return ImmutableMap.of();
  }

  @Override
  protected FlumeAvroSourceConnectorConfig config(Map<String, String> settings) {
    return new FlumeAvroSourceConnectorConfig(settings);
  }

  @Override
  protected FlumeAvroSourceTask newTask() {
    return new FlumeAvroSourceTask();
  }


  @Test
  public void poll() throws EventDeliveryException {
    int port = this.port.incrementAndGet();
    this.task.start(
        ImmutableMap.of(
            FlumeAvroSourceConnectorConfig.TOPIC_CONF, "flume",
            FlumeAvroSourceConnectorConfig.PORT_CONF, Integer.toString(port)
        )
    );
    final int count = 50;
    RpcClient client = RpcClientFactory.getDefaultInstance("localhost", port);
    List<Event> events = IntStream.range(0, count).boxed()
        .map(i -> {
          Event event = new SimpleEvent();
          event.setBody(String.format("This is message %s", i).getBytes(Charsets.UTF_8));
          return event;
        }).collect(Collectors.toList());

    List<SourceRecord> records = assertRecords(client, events);
  }


  private List<SourceRecord> assertRecords(RpcClient client, List<Event> events) throws EventDeliveryException {
    List<SourceRecord> records = new ArrayList<>();
    log.info("assertRecords() - Calling appendBatch with {} flume events", events.size());
    client.appendBatch(events);
    assertTimeoutPreemptively(Duration.ofSeconds(30), () -> {
      while (records.size() != events.size()) {
        log.trace("assertRecords() - polling task.");
        List<SourceRecord> poll = this.task.poll();
        if (null != poll) {
          log.trace("assertRecords() - task.poll() returned {} record(s).", poll.size());
          records.addAll(poll);
        }
      }
    });
    return records;
  }

  @Test
  public void version() {
    this.task.start(
        ImmutableMap.of(
            FlumeAvroSourceConnectorConfig.TOPIC_CONF, "flume",
            FlumeAvroSourceConnectorConfig.SSL_ENABLED_CONF, "false"
        )
    );
    assertNotNull(this.task.version(), "version should not be null.");
  }
}
