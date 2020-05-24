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

import com.github.jcustenborder.kafka.connect.utils.VersionUtil;
import com.github.jcustenborder.kafka.connect.utils.data.SourceRecordDeque;
import com.github.jcustenborder.kafka.connect.utils.data.SourceRecordDequeBuilder;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.avro.ipc.NettyServer;
import org.apache.avro.ipc.Server;
import org.apache.avro.ipc.specific.SpecificResponder;
import org.apache.flume.source.avro.AvroSourceProtocol;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class FlumeAvroSourceTask extends SourceTask {
  private static final Logger log = LoggerFactory.getLogger(FlumeAvroSourceTask.class);

  @Override
  public String version() {
    return VersionUtil.version(this.getClass());
  }

  static ThreadFactory threadFactory(String format) {
    return new ThreadFactoryBuilder()
        .setDaemon(true)
        .setNameFormat(format)
        .build();
  }

  FlumeAvroSourceConnectorConfig config;
  EventResponder eventResponder;
  SourceRecordDeque records;

  Server server;

  @Override
  public void start(Map<String, String> settings) {
    this.config = new FlumeAvroSourceConnectorConfig(settings);
    this.records = SourceRecordDequeBuilder.of()
        .batchSize(4096)
        .emptyWaitMs(100)
        .build();
    this.eventResponder = new EventResponder(this.config, this.records);

    Executor bossExecutor = Executors.newCachedThreadPool(
        threadFactory("avro kafka boss-%d")
    );
    Executor workerExecutor = Executors.newFixedThreadPool(
        this.config.workerThreads,
        threadFactory("avro kafka worker-%d")
    );

    NioServerSocketChannelFactory channelFactory = new NioServerSocketChannelFactory(
        bossExecutor,
        workerExecutor
    );
    ChannelPipelineFactory pipelineFactory = new AdvancedChannelPipelineFactory(this.config);

    SpecificResponder responder = new SpecificResponder(AvroSourceProtocol.class, this.eventResponder);
    this.server = new NettyServer(
        responder,
        new InetSocketAddress(this.config.bind, this.config.port),
        channelFactory,
        pipelineFactory,
        null
    );

    this.server.start();
  }

  @Override
  public List<SourceRecord> poll() throws InterruptedException {
    return this.records.getBatch();
  }

  @Override
  public void stop() {
    log.info("stop() - closing server.");
    this.server.close();
    try {
      this.server.join();
    } catch (InterruptedException e) {
      log.error("Exception thrown during shutdown", e);
    }
  }
}
