package io.opentelemetry.extensions;

import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizer;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizerProvider;

/**
 * Note this class is wired into SPI via
 * {@code resources/META-INF/services/io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizerProvider}
 */
public class DropSpansExtension implements AutoConfigurationCustomizerProvider {

  @Override
  public void customize(AutoConfigurationCustomizer autoConfiguration) {
    autoConfiguration
        .addTracerProviderCustomizer((sdkTracerProviderBuilder, configProperties) ->
            sdkTracerProviderBuilder.setSampler(new DropSpansFromEnvVariableSampler()));
  }
}
