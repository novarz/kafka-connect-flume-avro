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

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.compression.ZlibDecoder;
import org.jboss.netty.handler.codec.compression.ZlibEncoder;
import org.jboss.netty.handler.ipfilter.IpFilterRuleHandler;
import org.jboss.netty.handler.ssl.SslHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


class AdvancedChannelPipelineFactory implements ChannelPipelineFactory {
  private static final Logger log = LoggerFactory.getLogger(AdvancedChannelPipelineFactory.class);
  final FlumeAvroSourceConnectorConfig config;

  public AdvancedChannelPipelineFactory(FlumeAvroSourceConnectorConfig config) {
    this.config = config;
  }

  private SSLContext createServerSSLContext() {
    try {
      KeyStore ks = KeyStore.getInstance(this.config.keystoreType);
      final char[] password = this.config.sslKeyStorePassword.toCharArray();
      ks.load(new FileInputStream(this.config.sslKeyStorePath), password);

      // Set up key manager factory to use our key store
      KeyManagerFactory kmf = KeyManagerFactory.getInstance(getAlgorithm());
      kmf.init(ks, password);

      SSLContext serverContext = SSLContext.getInstance("TLS");
      serverContext.init(kmf.getKeyManagers(), null, null);
      return serverContext;
    } catch (Exception e) {
      throw new Error("Failed to initialize the server-side SSLContext", e);
    }
  }

  private String getAlgorithm() {
    String algorithm = Security.getProperty(
        "ssl.KeyManagerFactory.algorithm");
    if (algorithm == null) {
      algorithm = "SunX509";
    }
    return algorithm;
  }

  @Override
  public ChannelPipeline getPipeline() throws Exception {
    ChannelPipeline pipeline = Channels.pipeline();
    if (CompressionType.DEFLATE == this.config.compressionType) {
      ZlibEncoder encoder = new ZlibEncoder(6);
      pipeline.addFirst("deflater", encoder);
      pipeline.addFirst("inflater", new ZlibDecoder());
    }

    if (this.config.sslEnabled) {
      SSLEngine sslEngine = createServerSSLContext().createSSLEngine();
      sslEngine.setUseClientMode(false);
      List<String> excludeProtocols = Arrays.asList("SSLv3");
      List<String> enabledProtocols = new ArrayList<String>();
      for (String protocol : sslEngine.getEnabledProtocols()) {
        if (!excludeProtocols.contains(protocol)) {
          enabledProtocols.add(protocol);
        }
      }
      sslEngine.setEnabledProtocols(enabledProtocols.toArray(new String[0]));
      log.info("SSLEngine protocols enabled: " +
          Arrays.asList(sslEngine.getEnabledProtocols()));
      // addFirst() will make SSL handling the first stage of decoding
      // and the last stage of encoding this must be added after
      // adding compression handling above
      pipeline.addFirst("ssl", new SslHandler(sslEngine));
    }

    if (!this.config.ipFilterRules.isEmpty()) {

//      log.info("Setting up ipFilter with the following rule definition: " +
//          patternRuleConfigDefinition);
      IpFilterRuleHandler ipFilterHandler = new IpFilterRuleHandler();
      ipFilterHandler.addAll(this.config.ipFilterRules);
      log.info("Adding ipFilter with " + ipFilterHandler.size() + " rules");

      pipeline.addFirst("ipFilter", ipFilterHandler);
    }

    return pipeline;
  }
}
