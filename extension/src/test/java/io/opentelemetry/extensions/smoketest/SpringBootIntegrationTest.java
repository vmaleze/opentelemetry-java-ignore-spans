package io.opentelemetry.extensions.smoketest;

import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SpringBootIntegrationTest extends IntegrationTest {

  @Test
  public void extensionsAreLoadedFromJavaagent() throws IOException, InterruptedException {
    startTargetWithExtendedAgent();

    runGetRequest("/ping");
    runGetRequest("/actuator/health");
    runGetRequest("/health/readiness");
    runGetRequest("/actuator/metrics");
    runGetRequest("/actuator/info");

    Collection<ExportTraceServiceRequest> traces = waitForTraces();

    Assertions.assertEquals(1, countSpansByName(traces, "GET /ping"));
    Assertions.assertEquals(0, countSpansByName(traces, "GET /actuator/health"));
    Assertions.assertEquals(0, countSpansByName(traces, "GET /actuator/metrics"));
    Assertions.assertEquals(0, countSpansByName(traces, "GET /health/readiness"));
    Assertions.assertEquals(1, countSpansByName(traces, "GET /actuator/info"));

    Assertions.assertEquals(1, countSpansByStringAttribute(traces, "url.path", "/ping"));
    Assertions.assertEquals(
      0, countSpansByStringAttribute(traces, "url.path", "/actuator/health"));
    Assertions.assertEquals(
      0, countSpansByStringAttribute(traces, "url.path", "/health/readiness"));
    Assertions.assertEquals(
      0, countSpansByStringAttribute(traces, "url.path", "/actuator/metrics"));
    Assertions.assertEquals(
      1, countSpansByStringAttribute(traces, "url.path", "/actuator/info"));

    stopTarget();
  }

  private void runGetRequest(String route) throws IOException {
    String url = String.format("http://localhost:%d%s", target.getMappedPort(8080), route);
    Request request = new Request.Builder().url(url).get().build();

    try (Response response = client.newCall(request).execute()) {
      Assertions.assertTrue(
          response.code() < 500,
          () -> String.format("GET %s returned HTTP %d", route, response.code()));
    }
  }

  @Override
  protected Map<String, String> getExtraEnv() {
    return Collections.singletonMap("OTEL_DROP_SPANS", ".*/health,.*/metrics,.*/prometheus");
  }
}
