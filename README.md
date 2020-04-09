# Introduction
[Documentation](https://jcustenborder.github.io/kafka-connect-documentation/projects/kafka-connect-flume-avro) | [Confluent Hub](https://www.confluent.io/hub/jcustenborder/kafka-connect-flume-avro)

The plugin provides a way for users to send data from Apache Flume to Kafka bypassing a Flume Receiver.

# Installation

## Confluent Hub

The following command can be used to install the plugin directly from the Confluent Hub using the
[Confluent Hub Client](https://docs.confluent.io/current/connect/managing/confluent-hub/client.html).

```bash
confluent-hub install jcustenborder/kafka-connect-flume-avro:latest
```

## Manually

The zip file that is deployed to the [Confluent Hub](https://www.confluent.io/hub/jcustenborder/kafka-connect-flume-avro) is available under
`target/components/packages/`. You can manually extract this zip file which includes all dependencies. All the dependencies
that are required to deploy the plugin are under `target/kafka-connect-target` as well. Make sure that you include all the dependencies that are required
to run the plugin.

1. Create a directory under the `plugin.path` on your Connect worker.
2. Copy all of the dependencies under the newly created subdirectory.
3. Restart the Connect worker.


# Source Connectors
## [FlumeAvroSourceConnector](https://jcustenborder.github.io/kafka-connect-documentation/projects/kafka-connect-flume-avro/sources/FlumeAvroSourceConnector.html)

```
com.github.jcustenborder.kafka.connect.flume.FlumeAvroSourceConnector
```

Connector is used to emulate a `Flume Avro Source <https://flume.apache.org/FlumeUserGuide.html#avro-source>`_ to allow Flume Agents to forward events to a Kafka Connect pipeline.
### Important

This connector listens on a network port. Running more than one task or running in distributed mode can cause some undesired effects if another task already has the port open. It is recommended that you run this connector in :term:`Standalone Mode`.
### Configuration

#### General


##### `topic`

Topic to write the data to.

*Importance:* HIGH

*Type:* STRING



##### `bind`

IP Address or hostname to bind to.

*Importance:* HIGH

*Type:* STRING

*Default Value:* 0.0.0.0



##### `port`

Port to bind to.

*Importance:* HIGH

*Type:* INT

*Default Value:* 4545

*Validator:* ValidPort{start=1025, end=65535}



##### `compression`

The compression type. This must match on both flume agent and the connector. `DEFLATE` - Compress messages with the deflate algorithm., `NONE` - No compression

*Importance:* LOW

*Type:* STRING

*Default Value:* NONE

*Validator:* Matches: ``DEFLATE``, ``NONE``



##### `ip.filter.rules`

ip.filter.rules

*Importance:* LOW

*Type:* LIST



##### `worker.threads`

The number of worker threads to spawn.

*Importance:* LOW

*Type:* INT

*Default Value:* 10

*Validator:* [1,...,1000]


#### SSL


##### `ssl.enabled`

Flag to determine if ssl should be configured for the connection.

*Importance:* MEDIUM

*Type:* BOOLEAN



##### `ssl.keystore.password`

The password for the java keystore containing the certificate.

*Importance:* MEDIUM

*Type:* PASSWORD

*Default Value:* [hidden]



##### `ssl.keystore.path`

The path to the keystore on the local file system.

*Importance:* MEDIUM

*Type:* STRING

*Validator:* Empty String or Absolute path to a file that exists.



##### `ssl.keystore.type`

The type of keystore.

*Importance:* MEDIUM

*Type:* STRING

*Default Value:* PKCS12

*Validator:* [PKCS12, JKS]






# Development

## Building the source

```bash
mvn clean package
```

## Contributions

Contributions are always welcomed! Before you start any development please create an issue and
start a discussion. Create a pull request against your newly created issue and we're happy to see
if we can merge your pull request. First and foremost any time you're adding code to the code base
you need to include test coverage. Make sure that you run `mvn clean package` before submitting your
pull to ensure that all of the tests, checkstyle rules, and the package can be successfully built.