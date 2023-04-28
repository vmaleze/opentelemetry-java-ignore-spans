# OpenTelemetry monitoring extension

This project embeds a simple extension in the opentelemetry javaagent that will ignore common monitoring traces.

As long as the url finishes with one of the following, the traces will be ignored by the agent :
* /health
* /metrics

## Usage
Simply download the [latest]((https://github.com/vmaleze/opentelemetry-java-ignore-monitoring-spans/releases)) version instead of the javaagent, and you are good to go.  
For more information, you can refer to the [java instrumentation](https://opentelemetry.io/docs/instrumentation/java/automatic/) or the [operator](https://github.com/open-telemetry/opentelemetry-operator#use-customized-or-vendor-instrumentation) if you already inject the agent

## Current versions
* Extension version => [1.0.0](https://github.com/vmaleze/opentelemetry-java-ignore-monitoring-spans/releases)
* OpenTelemetry java agent => 1.25.0

## References :
* [Embedded extension](https://github.com/open-telemetry/opentelemetry-java-instrumentation/blob/main/examples/extension/README.md#embed-extensions-in-the-opentelemetry-agent) of the opentelemetry agent to create a new agent.
* [NewRelic exemples](https://github.com/newrelic/newrelic-opentelemetry-examples)
