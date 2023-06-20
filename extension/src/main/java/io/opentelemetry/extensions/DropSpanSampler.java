package io.opentelemetry.extensions;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.internal.StringUtils;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.sdk.trace.samplers.SamplingDecision;
import io.opentelemetry.sdk.trace.samplers.SamplingResult;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DropSpanSampler implements Sampler {

    private final Map<SpanKind, Map<AttributeKey<String>, Set<Pattern>>> rules = new HashMap<>();

    public DropSpanSampler() {
        final var properties = System.getenv();
        final var dropSpans = properties.keySet()
                .stream()
                .filter(k ->
                        k.startsWith(
                                "OTEL_DROP_SPANS"
                        )
                )
                .collect(Collectors.toMap(k -> k, properties::get));
        for (var entry : dropSpans.entrySet()) {
            final var key = entry.getKey().replaceFirst("OTEL_DROP_SPANS_", "");
            final var spanKey = extractSpanKind(key);
            final var attribute = key.replaceFirst(".*_", "").toLowerCase();
            for (var span : Arrays.stream(entry.getValue().split(","))
                    .filter(s -> !StringUtils.isNullOrEmpty(s))
                    .collect(Collectors.toSet())) {
                rules.computeIfAbsent(spanKey, k -> new HashMap<>())
                        .computeIfAbsent(AttributeKey.stringKey(attribute), attributes -> new HashSet<>())
                        .add(Pattern.compile(span));
            }
        }

    }

    @Override
    public SamplingResult shouldSample(
            Context parentContext,
            String traceId,
            String name,
            SpanKind spanKind,
            Attributes attributes,
            List<LinkData> parentLinks
    ) {
        if (rules.containsKey(spanKind)) {
            if (shouldBeDropped(rules.get(spanKind), attributes)) {
                return SamplingResult.create(SamplingDecision.DROP);
            }
        }
        return SamplingResult.create(SamplingDecision.RECORD_AND_SAMPLE);
    }

    @Override
    public String getDescription() {
        return "DropSpanSampler";
    }


    private SpanKind extractSpanKind(String key) {
        return SpanKind.valueOf(
                Arrays.stream(key.split("_"))
                        .map(k -> k.trim().toUpperCase())
                        .findFirst()
                        .orElse("CLIENT")
        );
    }

    private boolean shouldBeDropped(Map<AttributeKey<String>, Set<Pattern>> rules, Attributes attributes) {
        for (var rule : rules.entrySet()) {
            if (attributes.asMap().containsKey(rule.getKey())) {
                for (var p : rule.getValue()) {
                    var value = attributes.get(rule.getKey());
                    if (p.asMatchPredicate().test(value)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
