package io.opentelemetry.extensions;

import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizer;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizerProvider;

public class DropSpansExtension implements AutoConfigurationCustomizerProvider {

    @Override
    public void customize(AutoConfigurationCustomizer autoConfiguration) {
        var dropSpanSampler = new DropSpanSampler();
        autoConfiguration.addTracerProviderCustomizer(
                (builder, p) -> builder.setSampler(dropSpanSampler)
        );
    }
}
