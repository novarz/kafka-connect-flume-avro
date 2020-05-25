package com.github.sduran.kafka.connect.flume;

import org.apache.kafka.common.config.ConfigException;
import org.jboss.netty.handler.ipfilter.IpFilterRule;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class FlumeAvroSourceConnectorConfigTest {

  @TestFactory
  public Stream<DynamicTest> parseFilterRule() {
    return Stream.of(
        "allow:ip:127.*",
        "allow:name:localhost",
        "deny:ip:*"
    ).map(r -> dynamicTest(r, () -> {
      IpFilterRule filterRule = FlumeAvroSourceConnectorConfig.parseFilterRule("filter", r);
      assertNotNull(filterRule);
    }));
  }

  @TestFactory
  public Stream<DynamicTest> validate() {
    return Stream.of(
        "allow:ip:127.*",
        "allow:name:localhost",
        "deny:ip:*"
    ).map(r -> dynamicTest(r, () -> {
      FlumeAvroSourceConnectorConfig.IpFilterRuleValidator validator = new FlumeAvroSourceConnectorConfig.IpFilterRuleValidator();
      validator.ensureValid("filter", r);
    }));
  }

  @TestFactory
  public Stream<DynamicTest> parseFilterRuleError() {
    return Stream.of(
        "allow:something:localhost",
        "permit:ip:*",
        "allow:host:"
    ).map(r -> dynamicTest(r, () -> {
      assertThrows(ConfigException.class, () -> {
        IpFilterRule filterRule = FlumeAvroSourceConnectorConfig.parseFilterRule("filter", r);
      });
    }));
  }

  @TestFactory
  public Stream<DynamicTest> validateError() {
    return Stream.of(
        "allow:something:localhost",
        "permit:ip:*",
        "allow:host:"
    ).map(r -> dynamicTest(r, () -> {
      assertThrows(ConfigException.class, () -> {
        FlumeAvroSourceConnectorConfig.IpFilterRuleValidator validator = new FlumeAvroSourceConnectorConfig.IpFilterRuleValidator();
        validator.ensureValid("filter", r);
      });
    }));
  }


}
