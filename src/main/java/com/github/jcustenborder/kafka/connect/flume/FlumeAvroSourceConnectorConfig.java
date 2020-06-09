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

import com.github.jcustenborder.kafka.connect.utils.config.ConfigUtils;
import com.github.jcustenborder.kafka.connect.utils.config.ConfigKeyBuilder;
import com.github.jcustenborder.kafka.connect.utils.config.recommenders.Recommenders;
import com.github.jcustenborder.kafka.connect.utils.config.validators.Validators;
import com.github.jcustenborder.kafka.connect.utils.config.validators.filesystem.ValidFile;
import com.google.common.collect.ImmutableList;
import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Type;
import org.apache.kafka.common.config.ConfigException;
import org.jboss.netty.handler.ipfilter.IpFilterRule;
import org.jboss.netty.handler.ipfilter.PatternRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class FlumeAvroSourceConnectorConfig extends AbstractConfig {
  private static final Logger log = LoggerFactory.getLogger(FlumeAvroSourceConnectorConfig.class);
  public final String bind;
  public final int port;
  public final CompressionType compressionType;
  public final boolean sslEnabled;
  public final String sslKeyStorePath;
  public final String sslKeyStorePassword;
  public final String topic;
  public final int workerThreads;
  public final String keystoreType;
  public final List<IpFilterRule> ipFilterRules;



  public FlumeAvroSourceConnectorConfig(Map<String, String> settings) {
    super(config(), settings);
    this.topic = this.getString(TOPIC_CONF);
    this.bind = this.getString(BIND_CONF);
    this.port = this.getInt(PORT_CONF);
    this.compressionType = ConfigUtils.getEnum(CompressionType.class, this, COMPRESSION_CONF);
    this.sslEnabled = this.getBoolean(SSL_ENABLED_CONF);
    this.sslKeyStorePath = this.getString(SSL_KEYSTORE_PATH_CONF);
    this.sslKeyStorePassword = this.getPassword(SSL_KEYSTORE_PASSWORD_CONF).value();
    this.workerThreads = this.getInt(WORKER_THREADS_CONF);
    this.keystoreType = this.getString(SSL_KEYSTORE_TYPE_CONF);
    this.ipFilterRules = ipFilterRules();
  }

  static final Pattern IP_FILTER_RULE_PATTERN = Pattern.compile("^(allow|deny):(ip|name):(.+)$");


  static IpFilterRule parseFilterRule(String name, String value) {
    Matcher matcher = IP_FILTER_RULE_PATTERN.matcher(value);
    if (!matcher.matches()) {
      throw new ConfigException(
          name,
          value,
          String.format("filter rule must match pattern '%s'", IP_FILTER_RULE_PATTERN.pattern())
      );
    }
    final String groupAllowDeny = matcher.group(1), groupPatternType = matcher.group(2), groupPattern = matcher.group(3);
    log.trace(
        "parseFilterRule() - value='{}' groupAllowDeny = '{}' groupPatternType = '{}' groupPattern = '{}'",
        value,
        groupAllowDeny,
        groupPatternType,
        groupPattern
    );
    final boolean allow;
    switch (groupAllowDeny) {
      case "allow":
        allow = true;
        break;
      case "deny":
        allow = false;
        break;
      default:
        throw new ConfigException(
            name,
            value,
            "Rules must be 'allow' or 'deny'"
        );
    }
    final String patternType;
    switch (groupPatternType) {
      case "ip":
        patternType = "i";
        break;
      case "name":
        patternType = "n";
        break;
      default:
        throw new ConfigException(
            name,
            value,
            "Pattern types must be ip or name"
        );
    }
    final String pattern = String.format("%s:%s", patternType, groupPattern);

    try {
      return new PatternRule(allow, pattern);
    } catch (Exception e) {
      ConfigException exception = new ConfigException(
          name,
          value,
          "Exception thrown while creating rule"
      );
      exception.initCause(e);
      throw exception;
    }
  }


  static class IpFilterRuleValidator implements ConfigDef.Validator {

    void checkString(String name, String value) {
      IpFilterRule ipFilterRule = parseFilterRule(name, value);
    }

    @Override
    public void ensureValid(String name, Object value) {
      if (value instanceof String) {
        String stringValue = (String) value;
        checkString(name, stringValue);
      } else if (value instanceof List) {
        List<String> listValue = (List<String>) value;
        listValue.forEach(rule -> checkString(name, rule));
      } else {
        throw new ConfigException(
            name,
            value,
            "value must be a String or List"
        );
      }
    }
  }


  List<IpFilterRule> ipFilterRules() {
    List<String> rules = this.getList(IP_FILTER_RULES_CONF);
    return rules.stream()
        .map(r -> parseFilterRule(IP_FILTER_RULES_CONF, r))
        .collect(Collectors.toList());
  }

  public static final String BIND_CONF = "bind";
  static final String BIND_DOC = "IP Address or hostname to bind to.";
  static final String BIND_DEFAULT = "0.0.0.0";

  public static final String PORT_CONF = "port";
  static final String PORT_DOC = "Port to bind to.";
  static final int PORT_DEFAULT = 4545;

  public static final String COMPRESSION_CONF = "compression";
  static final String COMPRESSION_DOC = "The compression type. This must match on both flume agent and the connector. " + ConfigUtils.enumDescription(CompressionType.class);
  static final CompressionType COMPRESSION_DEFAULT = CompressionType.NONE;

  public static final String SSL_GROUP = "SSL";

  public static final String SSL_ENABLED_CONF = "ssl.enabled";
  static final String SSL_ENABLED_DOC = "Flag to determine if ssl should be configured for the connection.";
  static final boolean SSL_ENABLED_DEFAULT = false;

  public static final String SSL_KEYSTORE_PATH_CONF = "ssl.keystore.path";
  static final String SSL_KEYSTORE_PATH_DOC = "The path to the keystore on the local file system.";

  public static final String SSL_KEYSTORE_TYPE_CONF = "ssl.keystore.type";
  static final String SSL_KEYSTORE_TYPE_DOC = "The type of keystore.";

  public static final String SSL_KEYSTORE_PASSWORD_CONF = "ssl.keystore.password";
  static final String SSL_KEYSTORE_PASSWORD_DOC = "The password for the java keystore containing the certificate.";

  public static final String TOPIC_CONF = "topic";
  static final String TOPIC_DOC = "Topic to write the data to.";

  public static final String WORKER_THREADS_CONF = "worker.threads";
  static final String WORKER_THREADS_DOC = "The number of worker threads to spawn.";

  public static final String IP_FILTER_RULES_CONF = "ip.filter.rules";
  static final String IP_FILTER_RULES_DOC = "ip.filter.rules";

  public static ConfigDef config() {
    return new ConfigDef()
        .define(
            ConfigKeyBuilder.of(TOPIC_CONF, Type.STRING)
                .importance(ConfigDef.Importance.HIGH)
                .documentation(TOPIC_DOC)
                .build()
        )
        .define(
            ConfigKeyBuilder.of(BIND_CONF, Type.STRING)
                .importance(ConfigDef.Importance.HIGH)
                .documentation(BIND_DOC)
                .defaultValue(BIND_DEFAULT)
                .build()
        )
        .define(
            ConfigKeyBuilder.of(PORT_CONF, Type.INT)
                .importance(ConfigDef.Importance.HIGH)
                .documentation(PORT_DOC)
                .defaultValue(PORT_DEFAULT)
                .validator(Validators.validPort())
                .build()
        )
        .define(
            ConfigKeyBuilder.of(COMPRESSION_CONF, Type.STRING)
                .importance(ConfigDef.Importance.LOW)
                .documentation(COMPRESSION_DOC)
                .defaultValue(COMPRESSION_DEFAULT.toString())
                .validator(Validators.validEnum(CompressionType.class))
                .recommender(Recommenders.enumValues(CompressionType.class))
                .build()
        )
        .define(
            ConfigKeyBuilder.of(IP_FILTER_RULES_CONF, Type.LIST)
                .importance(ConfigDef.Importance.LOW)
                .documentation(IP_FILTER_RULES_DOC)
                .defaultValue(ImmutableList.of())
                .build()
        )
        .define(
            ConfigKeyBuilder.of(WORKER_THREADS_CONF, Type.INT)
                .importance(ConfigDef.Importance.LOW)
                .documentation(WORKER_THREADS_DOC)
                .defaultValue(10)
                .validator(ConfigDef.Range.between(1, 1000))
                .build()
        )
        .define(
            ConfigKeyBuilder.of(SSL_GROUP, SSL_ENABLED_CONF, Type.BOOLEAN)
                .importance(ConfigDef.Importance.MEDIUM)
                .documentation(SSL_ENABLED_DOC)
                .displayName("SSL Enabled")
                .defaultValue(SSL_ENABLED_DEFAULT)
                .orderInGroup(0)
                .build()
        )
        .define(
            ConfigKeyBuilder.of(SSL_GROUP, SSL_KEYSTORE_PATH_CONF, Type.STRING)
                .importance(ConfigDef.Importance.MEDIUM)
                .documentation(SSL_KEYSTORE_PATH_DOC)
                .recommender(Recommenders.visibleIf(SSL_ENABLED_CONF, true))
                .defaultValue("")
                .validator(Validators.blankOr(ValidFile.of()))
                .orderInGroup(1)
                .build()
        )
        .define(
            ConfigKeyBuilder.of(SSL_GROUP, SSL_KEYSTORE_TYPE_CONF, Type.STRING)
                .importance(ConfigDef.Importance.MEDIUM)
                .documentation(SSL_KEYSTORE_TYPE_DOC)
                .validator(ConfigDef.ValidString.in("PKCS12", "JKS"))
                .recommender(Recommenders.visibleIf(SSL_ENABLED_CONF, true))
                .orderInGroup(2)
                .defaultValue("PKCS12")
                .build()
        )
        .define(
            ConfigKeyBuilder.of(SSL_GROUP, SSL_KEYSTORE_PASSWORD_CONF, Type.PASSWORD)
                .importance(ConfigDef.Importance.MEDIUM)
                .documentation(SSL_KEYSTORE_PASSWORD_DOC)
                .recommender(Recommenders.visibleIf(SSL_ENABLED_CONF, true))
                .defaultValue("")
                .orderInGroup(3)
                .build()
        )

            ;
  }
}
