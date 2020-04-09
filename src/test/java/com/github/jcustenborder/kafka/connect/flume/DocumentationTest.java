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

import com.github.jcustenborder.kafka.connect.utils.BaseDocumentationTest;
import com.github.jcustenborder.kafka.connect.utils.jackson.ObjectMapperFactory;
import com.github.jcustenborder.kafka.connect.utils.templates.ImmutableSourceConnectorExample;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import org.apache.kafka.common.utils.Time;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.header.ConnectHeaders;
import org.apache.kafka.connect.header.Headers;
import org.apache.kafka.connect.source.SourceRecord;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.File;
import java.util.stream.Stream;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class DocumentationTest extends BaseDocumentationTest {
  @Disabled
  @TestFactory
  public Stream<DynamicTest> generateExamples() {

    File outputPath = new File("src/test/resources/com/github/jcustenborder/kafka/connect/flume/FlumeAvroSourceConnector");

    byte[] message = "No one will read this message.".getBytes(Charsets.UTF_8);
    Headers headers = new ConnectHeaders();
    headers.addString("flume.header", "adsfasdf");
    SourceRecord standard = new SourceRecord(
        ImmutableMap.of(),
        ImmutableMap.of(),
        "flume",
        null,
        null,
        null,
        Schema.BYTES_SCHEMA,
        message,
        Time.SYSTEM.milliseconds(),
        headers
    );

    return Stream.of(
        ImmutableSourceConnectorExample.builder()
            .name("standard")
            .title("Standard")
            .description("This example will listen for events from Flume on all ip addresses using the standard port. " +
                "The received messages are written to the `flume` topic.")
            .putConfig(FlumeAvroSourceConnectorConfig.TOPIC_CONF, "flume")
            .output(standard)
            .build(),
        ImmutableSourceConnectorExample.builder()
            .name("network-restrictions")
            .title("Network Restrictions")
            .description("This example will listen for events from Flume on all ip addresses using the standard port. " +
                "Any network traffic that is not from `localhost` or `127.0.*` will be dropped.")
            .putConfig(FlumeAvroSourceConnectorConfig.TOPIC_CONF, "flume")
            .putConfig(FlumeAvroSourceConnectorConfig.IP_FILTER_RULES_CONF, "allow:ip:127.*,allow:name:localhost,deny:ip:*")
            .output(standard)
            .build(),
        ImmutableSourceConnectorExample.builder()
            .name("inline-transformation")
            .title("Inline transformation")
            .tip("This example is specific to converting FIX financial data, however this pattern " +
                "can be used to convert data before it is written to Kafka.")
            .note("This example requires the plugin containing `com.github.jcustenborder.kafka.connect.transform.fix.FromFIX$Value` " +
                "to be available in the plugin path on the connect worker.")
            .description("This example will receive the message from a Flume Avro Sink which has a " +
                "value of bytes. The FIX transformation will read the value of the record and " +
                "replace the value with a Kafka Connect Struct based on the FIX message it received.")
            .putConfig(FlumeAvroSourceConnectorConfig.TOPIC_CONF, "flume")
            .putConfig(FlumeAvroSourceConnectorConfig.IP_FILTER_RULES_CONF, "allow:ip:127.*,allow:name:localhost,deny:ip:*")
            .putTransformations(
                "FromFix",
                ImmutableMap.of("type", "com.github.jcustenborder.kafka.connect.transform.fix.FromFIX$Value")
            )
            .output(standard)
            .build()
    ).map(e -> dynamicTest(e.getName(), () -> {
      File outputFile = new File(outputPath, e.getName() + ".json");
      ObjectMapperFactory.INSTANCE.writeValue(outputFile, e);
    }));
  }
}
