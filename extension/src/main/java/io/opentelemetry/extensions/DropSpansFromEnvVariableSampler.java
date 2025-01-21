package io.opentelemetry.extensions;

import static io.opentelemetry.api.common.AttributeKey.stringKey;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.sdk.trace.samplers.SamplingDecision;
import io.opentelemetry.sdk.trace.samplers.SamplingResult;
import java.util.List;
import java.util.Objects;

public class DropSpansFromEnvVariableSampler implements Sampler {

  public static final AttributeKey<String> URL_PATH = stringKey("url.path");

  @Override
  public SamplingResult shouldSample(
      Context parentContext,
      String traceId,
      String name,
      SpanKind spanKind,
      Attributes attributes,
      List<LinkData> parentLinks) {

    final var dropSpansEnv = System.getenv("OTEL_DROP_SPANS");
    if (dropSpansEnv != null) {
      for (var span : dropSpansEnv.split(",")) {
        if (spanKind == SpanKind.SERVER && Objects.requireNonNull(attributes.get(URL_PATH)).matches(span)) {
          return SamplingResult.create(SamplingDecision.DROP);
        }
      }
    }

    return SamplingResult.create(SamplingDecision.RECORD_AND_SAMPLE);
  }

  @Override
  public String getDescription() {
    return "DropSpansFromEnvVariableSampler";
  }
}
