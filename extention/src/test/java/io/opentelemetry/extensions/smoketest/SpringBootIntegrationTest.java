/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extensions.smoketest;

import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import java.io.IOException;
import java.util.Collection;
import okhttp3.Request;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SpringBootIntegrationTest extends IntegrationTest {

  @Test
  public void extensionsAreLoadedFromJavaagent() throws IOException, InterruptedException {
    startTargetWithExtendedAgent();

    runGetRequest("/ping");
    runGetRequest("/actuator/health");
    runGetRequest("/actuator/metrics");
    runGetRequest("/actuator/info");

    Collection<ExportTraceServiceRequest> traces = waitForTraces();

    Assertions.assertEquals(1, countSpansByName(traces, "GET /ping"));
    Assertions.assertEquals(0, countSpansByName(traces, "GET /actuator/health"));
    Assertions.assertEquals(0, countSpansByName(traces, "GET /actuator/metrics"));
    Assertions.assertEquals(1, countSpansByName(traces, "GET /actuator/info"));

    stopTarget();
  }

  private void runGetRequest(String route) throws IOException {
    String url = String.format("http://localhost:%d%s", target.getMappedPort(8080), route);
    Request request = new Request.Builder().url(url).get().build();

    client.newCall(request).execute();
  }
}
