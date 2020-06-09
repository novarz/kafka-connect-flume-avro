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
package com.github.jcustenborder.kafka.connect.flume;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.connect.source.SourceTask;
import org.apache.kafka.connect.source.SourceTaskContext;
import org.apache.kafka.connect.storage.OffsetStorageReader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class BaseSourceTaskTest<Config extends AbstractConfig, Task extends SourceTask> {
  protected abstract Map<String, String> settings();

  protected abstract Config config(Map<String, String> settings);

  protected abstract Task newTask();

  protected Task task;
  protected SourceTaskContext taskContext;
  protected OffsetStorageReader offsetStorageReader;
  protected final AtomicInteger port = new AtomicInteger(
      FlumeAvroSourceConnectorConfig.PORT_DEFAULT
  );

  @BeforeEach
  public void setupTask() {
    this.offsetStorageReader = mock(OffsetStorageReader.class);
    this.taskContext = mock(SourceTaskContext.class);
    when(this.taskContext.offsetStorageReader()).thenReturn(this.offsetStorageReader);
    this.task = newTask();
    this.task.initialize(this.taskContext);
  }


  @AfterEach
  public void teardownTask() {
    this.task.stop();
  }
}
