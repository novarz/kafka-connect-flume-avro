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
import com.github.jcustenborder.kafka.connect.utils.config.Description;
import com.github.jcustenborder.kafka.connect.utils.config.DocumentationImportant;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.source.SourceConnector;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Description("Connector is used to emulate a `Flume Avro Source <https://flume.apache.org/FlumeUserGuide.html#avro-source>`_ to " +
    "allow Flume Agents to forward events to a Kafka Connect pipeline.")
@DocumentationImportant("This connector listens on a network port. Running more than one task or running in distributed " +
    "mode can cause some undesired effects if another task already has the port open. It is recommended that you run this " +
    "connector in :term:`Standalone Mode`.")
public class FlumeAvroSourceConnector extends SourceConnector {
  @Override
  public String version() {
    return VersionUtil.version(this.getClass());
  }


  FlumeAvroSourceConnectorConfig config;
  Map<String, String> settings;

  @Override
  public void start(Map<String, String> settings) {
    this.config = new FlumeAvroSourceConnectorConfig(settings);
    this.settings = settings;
  }

  @Override
  public Class<? extends Task> taskClass() {
    return FlumeAvroSourceTask.class;
  }

  @Override
  public List<Map<String, String>> taskConfigs(int i) {
    return Arrays.asList(this.settings);
  }

  @Override
  public void stop() {

  }

  @Override
  public ConfigDef config() {
    return FlumeAvroSourceConnectorConfig.config();
  }
}
