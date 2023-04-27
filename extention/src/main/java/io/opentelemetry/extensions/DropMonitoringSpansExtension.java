package io.opentelemetry.extensions;

import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.contrib.sampler.RuleBasedRoutingSampler;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizer;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizerProvider;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;

/**
 * Note this class is wired into SPI via
 * {@code resources/META-INF/services/io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizerProvider}
 */
public class DropMonitoringSpansExtension implements AutoConfigurationCustomizerProvider {

  @Override
  public void customize(AutoConfigurationCustomizer autoConfiguration) {
    // Set the sampler to be the default parentbased_always_on, but drop calls to health and metrics endpoints
    autoConfiguration.addTracerProviderCustomizer(
        (sdkTracerProviderBuilder, configProperties) ->
            sdkTracerProviderBuilder.setSampler(
                Sampler.parentBased(
                    RuleBasedRoutingSampler.builder(SpanKind.SERVER, Sampler.alwaysOn())
                        .drop(SemanticAttributes.HTTP_TARGET, ".*/health")
                        .drop(SemanticAttributes.HTTP_TARGET, ".*/metrics")
                        .build())));
  }
}
