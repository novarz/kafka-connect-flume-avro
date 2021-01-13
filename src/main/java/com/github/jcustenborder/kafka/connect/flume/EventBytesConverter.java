/**
 * Copyright © 2017 Sergio Duran (sduran@confluent.io)
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

import com.google.common.collect.ImmutableMap;
import org.apache.flume.source.avro.AvroFlumeEvent;
import org.apache.kafka.common.utils.SystemTime;
import org.apache.kafka.common.utils.Time;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.header.ConnectHeaders;
import org.apache.kafka.connect.header.Headers;
import org.apache.kafka.connect.source.SourceRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.HashMap;
import java.util.Map;

class EventBytesConverter implements EventConverter {
  final FlumeAvroSourceConnectorConfig config;

  final Map<String, Object> sourcePartition = ImmutableMap.of();
  final Map<String, Object> sourceOffset = ImmutableMap.of();
  private Time time = new SystemTime();
  private static final Logger log = LoggerFactory.getLogger(EventResponder.class);
  static final String FIELD_BODY = "body";
  static final String FIELD_SENDER = "sender";
  static final String FIELD_HEADERS = "headers";
  static final Schema KEY_SCHEMA = SchemaBuilder.struct()
            .name("org.santander.flume.source.avro.AvroFlumeEventKey")
            .field(FIELD_SENDER, SchemaBuilder.string().doc("The remote address for the sender of the message.").build())
            .build();
  static final Schema VALUE_SCHEMA = SchemaBuilder.struct()
            .name("org.santander.flume.source.avro.AvroFlumeEvent")
            .field(
                    FIELD_HEADERS,
                    SchemaBuilder.map(
                            Schema.STRING_SCHEMA,
                            Schema.STRING_SCHEMA
                    ).build()
            )
            .build();



  public EventBytesConverter(FlumeAvroSourceConnectorConfig config) {
    this.config = config;
  }

  @Override
  public SourceRecord record(AvroFlumeEvent event, String sender) {
    final Schema keySchema;
    final Struct key;
    HashMap<String,String> eventkey = new HashMap<>();

    log.debug("adding headers");
    Headers headers = new ConnectHeaders();
    byte[] bytes = new byte[event.getBody().remaining()];

    event.getBody().get(bytes, 0, bytes.length);
    headers.addBytes("logevent",bytes);
    log.debug("adding value fields");


    if (null != event.getHeaders()) {
      event.getHeaders().forEach((clave, valor) -> {
        if (null != valor) {
          String headerName = clave.toString() ;
          String v = valor.toString();
          eventkey.put(headerName,v);
        }
      });
     // eventkey.put("content",new String(bytes, StandardCharsets.UTF_8));
    }

    final Struct value = new Struct(this.VALUE_SCHEMA)
            .put(FIELD_HEADERS, eventkey);

    log.debug("adding key schema");

          keySchema = this.KEY_SCHEMA;
          key = new Struct(this.KEY_SCHEMA).put(FIELD_SENDER,eventkey.getOrDefault ("host","nulo"));

      return new SourceRecord(
              sourcePartition,
              sourceOffset,
              this.config.topic,
              null,
              null,
              eventkey.getOrDefault ("host","nulo"),
              this.VALUE_SCHEMA,
              value,
              this.time.milliseconds(),
              headers
      );
  }
}
