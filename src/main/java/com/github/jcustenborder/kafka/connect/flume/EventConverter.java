/**
 * Copyright © 2017 Jeremy Custenborder (jcustenborder@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jcustenborder.kafka.connect.flume;

import com.google.common.collect.ImmutableMap;
import org.apache.flume.source.avro.AvroFlumeEvent;
import org.apache.kafka.common.utils.SystemTime;
import org.apache.kafka.common.utils.Time;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;

import java.util.Map;

class EventConverter {
  static final String FIELD_BODY = "body";
  static final String FIELD_SENDER = "sender";
  static final String FIELD_HEADERS = "headers";
  final FlumeAvroSourceConnectorConfig config;

  final Schema keySchema;
  final Schema valueSchema;
  final Map<String, Object> sourcePartition = ImmutableMap.of();
  final Map<String, Object> sourceOffset = ImmutableMap.of();
  Time time = new SystemTime();


  public EventConverter(FlumeAvroSourceConnectorConfig config) {
    this.config = config;
    this.valueSchema = SchemaBuilder.struct()
        .name("org.apache.flume.source.avro.AvroFlumeEvent")
        .field(
            FIELD_HEADERS,
            SchemaBuilder.map(
                Schema.STRING_SCHEMA,
                Schema.STRING_SCHEMA
            ).build()
        )
        .field(FIELD_BODY, Schema.BYTES_SCHEMA)
        .build();

    SchemaBuilder keySchemaBuilder = SchemaBuilder.struct()
        .name("com.github.jcustenborder.kafka.connect.flume.AvroFlumeEventKey");

    switch (this.config.keyType) {
      case HEADERS:
        keySchemaBuilder.field(
            FIELD_HEADERS,
            SchemaBuilder.map(
                Schema.STRING_SCHEMA,
                Schema.STRING_SCHEMA
            ).build()
        );
        break;
      case NONE:
        break;
      case SENDER:
        keySchemaBuilder.field(FIELD_SENDER,
            SchemaBuilder.string().doc("The remote address for the sender of the message.").build()
        );
        break;
    }

    this.keySchema = keySchemaBuilder.build();

  }

  SourceRecord record(AvroFlumeEvent event, String sender) {
    final Struct value = new Struct(this.valueSchema)
        .put(FIELD_BODY, event.getBody())
        .put(FIELD_HEADERS, event.getHeaders());

    final Schema keySchema;
    final Struct key;

    if (KeyType.NONE == this.config.keyType) {
      keySchema = null;
      key = null;
    } else {
      keySchema = this.keySchema;
      key = new Struct(keySchema);

      switch (this.config.keyType) {
        case HEADERS:
          key.put(FIELD_HEADERS, event.getHeaders());
          break;
        case SENDER:
          key.put(FIELD_SENDER, sender);
          break;
      }
    }

    return new SourceRecord(
        sourcePartition,
        sourceOffset,
        this.config.topic,
        null,
        keySchema,
        key,
        this.valueSchema,
        value,
        this.time.milliseconds()
    );
  }
}
