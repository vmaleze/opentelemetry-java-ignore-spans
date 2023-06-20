package io.opentelemetry.extensions;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.internal.StringUtils;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.contrib.sampler.RuleBasedRoutingSampler;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizer;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizerProvider;
import io.opentelemetry.sdk.trace.samplers.Sampler;

import java.util.Arrays;
import java.util.stream.Collectors;

public class DropSpansExtension implements AutoConfigurationCustomizerProvider {

    @Override
    public void customize(AutoConfigurationCustomizer autoConfiguration) {
        // Set the sampler to be the default parentbased_always_on, but drop calls listed in the env variable

        // Set the sampler to be the default parentbased_always_on, but drop calls listed in the env variable
        final var properties = System.getenv();
        final var dropSpans = properties.keySet()
                .stream()
                .filter(k ->
                        k.startsWith(
                                "OTEL_DROP_SPANS"
                        )
                )
                .collect(Collectors.toMap(k -> k, properties::get));
        if (!dropSpans.isEmpty()) {
            for (var entry : dropSpans.entrySet()) {
                final var key = entry.getKey().replaceFirst("OTEL_DROP_SPANS_", "");
                final var attribute = key.replaceFirst(".*_", "").toLowerCase();
                final var dropSpanBuilder = RuleBasedRoutingSampler.builder(extractSpanKind(key), Sampler.alwaysOn());
                for (var span : Arrays.stream(entry.getValue().split(","))
                        .filter(s -> !StringUtils.isNullOrEmpty(s))
                        .collect(Collectors.toSet())) {

                    dropSpanBuilder.drop(AttributeKey.stringKey(attribute), span);
                }
                autoConfiguration.addTracerProviderCustomizer(
                        (builder, p) -> builder.setSampler(
                                Sampler.parentBased(dropSpanBuilder.build())
                        )
                );
            }
        }
    }

    private SpanKind extractSpanKind(String key) {
        return SpanKind.valueOf(
                Arrays.stream(key.split("_"))
                        .map(k -> k.trim().toUpperCase())
                        .findFirst()
                        .orElse("CLIENT")
        );
    }
}
