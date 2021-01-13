package com.github.jcustenborder.kafka.connect.flume;

import org.apache.flume.source.avro.AvroFlumeEvent;
import org.apache.kafka.connect.source.SourceRecord;

public interface EventConverter {
    SourceRecord record(AvroFlumeEvent event, String sender);
}
