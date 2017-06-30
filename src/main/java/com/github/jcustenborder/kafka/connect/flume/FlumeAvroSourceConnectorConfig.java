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

import com.github.jcustenborder.kafka.connect.utils.config.ConfigUtils;
import com.github.jcustenborder.kafka.connect.utils.config.ValidEnum;
import com.github.jcustenborder.kafka.connect.utils.config.ValidPort;
import com.google.common.collect.ImmutableList;
import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.jboss.netty.handler.ipfilter.IpFilterRule;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

class FlumeAvroSourceConnectorConfig extends AbstractConfig {
  public final String bind;
  public final int port;
  public final CompressionType compressionType;
  public final boolean sslEnabled;
  public final String sslKeyStorePath;
  public final String sslKeyStorePassword;
  public final String topic;
  public final KeyType keyType;
  public final int workerThreads;
  public final String keystoreType;
  public final List<IpFilterRule> ipFilterRules;


  public FlumeAvroSourceConnectorConfig(Map<String, String> settings) {
    super(config(), settings);
    this.topic = this.getString(TOPIC_CONF);
    this.bind = this.getString(BIND_CONF);
    this.port = this.getInt(PORT_CONF);
    this.compressionType = ConfigUtils.getEnum(CompressionType.class, this, COMPRESSION_CONF);
    this.keyType = ConfigUtils.getEnum(KeyType.class, this, KEY_TYPE_CONF);
    this.sslEnabled = this.getBoolean(SSL_ENABLED_CONF);
    this.sslKeyStorePath = this.getString(SSL_KEYSTORE_PATH_CONF);
    this.sslKeyStorePassword = this.getPassword(SSL_KEYSTORE_PASSWORD_CONF).value();
    this.workerThreads = this.getInt(WORKER_THREADS_CONF);
    this.keystoreType = this.getString(KEY_TYPE_CONF);

    //TODO: Correct this functionality
    this.ipFilterRules = ImmutableList.of();
  }

  public static final String BIND_CONF = "bind";
  static final String BIND_DOC = "IP Address or hostname to bind to.";
  static final String BIND_DEFAULT = "0.0.0.0";

  public static final String PORT_CONF = "port";
  static final String PORT_DOC = "Port to bind to.";
  static final int PORT_DEFAULT = 4545;

  public static final String COMPRESSION_CONF = "compression";
  static final String COMPRESSION_DOC = "The compression type. This must match on both flume agent and the connector.";
  static final CompressionType COMPRESSION_DEFAULT = CompressionType.NONE;

  public static final String SSL_GROUP = "SSL";

  public static final String SSL_ENABLED_CONF = "ssl.enabled";
  static final String SSL_ENABLED_DOC = "Flag to determine if ssl should be configured for the connection.";

  public static final String SSL_KEYSTORE_PATH_CONF = "ssl.keystore.path";
  static final String SSL_KEYSTORE_PATH_DOC = "The path to the keystore on the local file system.";

  public static final String SSL_KEYSTORE_TYPE_CONF = "ssl.keystore.type";
  static final String SSL_KEYSTORE_TYPE_DOC = "The type of keystore.";

  public static final String SSL_KEYSTORE_PASSWORD_CONF = "ssl.keystore.password";
  static final String SSL_KEYSTORE_PASSWORD_DOC = "The password for the java keystore containing the certificate.";

  public static final String TOPIC_CONF = "topic";
  static final String TOPIC_DOC = "Topic to write the data to.";

  public static final String KEY_TYPE_CONF = "key.type";
  static final String KEY_TYPE_DOC = "The type of key to include. NONE will set the key field as null. " +
      "HEADERS will include the headers. SENDER will include the host that sent the message.";

  public static final String WORKER_THREADS_CONF = "worker.threads";
  static final String WORKER_THREADS_DOC = "The number of worker threads to spawn.";

  public static ConfigDef config() {
    return new ConfigDef()
        .define(TOPIC_CONF, ConfigDef.Type.STRING, ConfigDef.NO_DEFAULT_VALUE, ConfigDef.Importance.HIGH, TOPIC_DOC)
        .define(BIND_CONF, ConfigDef.Type.STRING, BIND_DEFAULT, ConfigDef.Importance.HIGH, BIND_DOC)
        .define(COMPRESSION_CONF, ConfigDef.Type.STRING, COMPRESSION_DEFAULT.toString(), ValidEnum.of(CompressionType.class), ConfigDef.Importance.LOW, COMPRESSION_DOC)
        .define(KEY_TYPE_CONF, ConfigDef.Type.STRING, KeyType.NONE.toString(), ValidEnum.of(KeyType.class), ConfigDef.Importance.LOW, KEY_TYPE_DOC)
        .define(WORKER_THREADS_CONF, ConfigDef.Type.INT, 10, ConfigDef.Range.between(1, 1000), ConfigDef.Importance.LOW, WORKER_THREADS_DOC)
        .define(SSL_ENABLED_CONF, ConfigDef.Type.BOOLEAN, ConfigDef.NO_DEFAULT_VALUE, ConfigDef.Importance.MEDIUM, SSL_ENABLED_DOC, SSL_GROUP, 1, ConfigDef.Width.SHORT, "SSL")
        .define(SSL_KEYSTORE_PATH_CONF, ConfigDef.Type.STRING, ConfigDef.NO_DEFAULT_VALUE, ConfigDef.Importance.MEDIUM, SSL_KEYSTORE_PATH_DOC, SSL_GROUP, 2, ConfigDef.Width.SHORT, "SSL", Arrays.asList(SSL_ENABLED_CONF))
        .define(SSL_KEYSTORE_TYPE_CONF, ConfigDef.Type.STRING, "PKCS12", ConfigDef.ValidString.in("PKCS12", "JKS"), ConfigDef.Importance.MEDIUM, SSL_KEYSTORE_TYPE_DOC, SSL_GROUP, 2, ConfigDef.Width.SHORT, "SSL", Arrays.asList(SSL_ENABLED_CONF))
        .define(SSL_KEYSTORE_PASSWORD_CONF, ConfigDef.Type.PASSWORD, ConfigDef.NO_DEFAULT_VALUE, ConfigDef.Importance.MEDIUM, SSL_KEYSTORE_PASSWORD_DOC, SSL_GROUP, 3, ConfigDef.Width.SHORT, "SSL", Arrays.asList(SSL_ENABLED_CONF))
        .define(PORT_CONF, ConfigDef.Type.INT, PORT_DEFAULT, ValidPort.of(), ConfigDef.Importance.HIGH, PORT_DOC);
  }
}
