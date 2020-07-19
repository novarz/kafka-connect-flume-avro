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

import com.github.jcustenborder.kafka.connect.utils.data.SourceRecordDeque;
import org.apache.avro.AvroRemoteException;
import org.apache.flume.source.avro.AvroFlumeEvent;
import org.apache.flume.source.avro.AvroSourceProtocol;
import org.apache.flume.source.avro.Status;
import org.apache.kafka.connect.source.SourceRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

class EventResponder implements AvroSourceProtocol {
  private static final Logger log = LoggerFactory.getLogger(EventResponder.class);

  final FlumeAvroSourceConnectorConfig config;
  final EventConverter converter;
  final SourceRecordDeque records;

  public EventResponder(FlumeAvroSourceConnectorConfig config, SourceRecordDeque records) {
    this.config = config;
    this.records = records;
    this.converter = new EventConverter(this.config);
  }

  @Override
  public Status append(AvroFlumeEvent event) throws AvroRemoteException {
    Status result;
    try {
      SourceRecord record = this.converter.record(event, null);
      this.records.add(record);
      log.debug("Record Added");
      result = Status.OK;
    } catch (Exception ex) {
      log.error("Exception thrown", ex);
      result = Status.FAILED;
    }

    return result;
  }

  @Override
  public Status appendBatch(List<AvroFlumeEvent> events) throws AvroRemoteException {
    Status result;
    try {
      for (AvroFlumeEvent event : events) {
        append(event);
      }
      log.debug("Record Added");
      result = Status.OK;

    } catch (Exception ex) {
      log.error("Exception thrown", ex);
      result = Status.FAILED;
    }

    return result;
  }
}
