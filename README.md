# OpenTelemetry spans drop extension

This project embeds a simple extension in the opentelemetry javaagent that will drop spans based on the env variable `OTEL_DROP_SPANS`.

## Usage

Add the `OTEL_DROP_SPANS` env variable and set the spans to drop using the `,` as a separator

### [Java instrumentation](https://opentelemetry.io/docs/instrumentation/java/automatic/)
Simply download the [latest](https://github.com/vmaleze/opentelemetry-java-ignore-spans/releases) version instead of the javaagent, and you are good to go.  


### [Opentelemetry operator](https://github.com/open-telemetry/opentelemetry-operator#use-customized-or-vendor-instrumentation)

```yaml
apiVersion: opentelemetry.io/v1alpha1
kind: Instrumentation
metadata:
  name: my-instrumentation
spec:
  java:
    env:
      # Will drop spans towards health and metrics endpoints
      - name: OTEL_DROP_SPANS
        value: .*/health,.*/metrics
    image: ghcr.io/vmaleze/opentelemetry-java-ignore-spans:2.7.0
```

## Current versions
* Extension version => [2.7.0](https://github.com/vmaleze/opentelemetry-java-ignore-spans/releases)
* [OpenTelemetry java agent](https://github.com/open-telemetry/opentelemetry-java-instrumentation) => 2.7.0
* [OpenTelemetry SDK](https://github.com/open-telemetry/opentelemetry-java) => 1.42.0

## References :
* [Embedded extension](https://github.com/open-telemetry/opentelemetry-java-instrumentation/blob/main/examples/extension/README.md#embed-extensions-in-the-opentelemetry-agent)
* [NewRelic exemples](https://github.com/newrelic/newrelic-opentelemetry-examples)
