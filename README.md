# Introduction

The Kafka Connect Flume Avro connector is used to emulate a Flume [Avro Source](https://flume.apache.org/FlumeUserGuide.html#avro-source) 
to allow Flume Agents to forward events to a Kafka Connect pipeline.

# Configuration

## FlumeAvroSourceConnector

Connector is used to emulate a Flume [Avro Source](https://flume.apache.org/FlumeUserGuide.html#avro-source) to allow Flume Agents to forward events to a Kafka Connect pipeline.

```properties
name=connector1
tasks.max=1
connector.class=com.github.jcustenborder.kafka.connect.flume.FlumeAvroSourceConnector

# Set these required values
ssl.enabled=
ssl.keystore.password=
topic=
ssl.keystore.path=
```

| Name                  | Description                                                                                                                                            | Type     | Default | Valid Values                                             | Importance |
|-----------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------|----------|---------|----------------------------------------------------------|------------|
| topic                 | Topic to write the data to.                                                                                                                            | string   |         |                                                          | high       |
| ssl.enabled           | Flag to determine if ssl should be configured for the connection.                                                                                      | boolean  |         |                                                          | medium     |
| ssl.keystore.password | The password for the java keystore containing the certificate.                                                                                         | password |         |                                                          | medium     |
| ssl.keystore.path     | The path to the keystore on the local file system.                                                                                                     | string   |         |                                                          | medium     |
| bind                  | IP Address or hostname to bind to.                                                                                                                     | string   | 0.0.0.0 |                                                          | high       |
| port                  | Port to bind to.                                                                                                                                       | int      | 4545    | ValidPort{start=1025, end=65535}                         | high       |
| ssl.keystore.type     | The type of keystore.                                                                                                                                  | string   | PKCS12  | [PKCS12, JKS]                                            | medium     |
| compression           | The compression type. This must match on both flume agent and the connector.                                                                           | string   | NONE    | ValidEnum{enum=CompressionType, allowed=[DEFLATE, NONE]} | low        |
| key.type              | The type of key to include. NONE will set the key field as null. HEADERS will include the headers. SENDER will include the host that sent the message. | string   | NONE    | ValidEnum{enum=KeyType, allowed=[NONE, HEADERS, SENDER]} | low        |
| worker.threads        | The number of worker threads to spawn.                                                                                                                 | int      | 10      | [1,...,1000]                                             | low        |


