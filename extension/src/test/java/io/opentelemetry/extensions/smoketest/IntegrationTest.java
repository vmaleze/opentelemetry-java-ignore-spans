package io.opentelemetry.extensions.smoketest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.trace.v1.Span;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.MountableFile;

abstract class IntegrationTest {

  private static final Logger logger = LoggerFactory.getLogger(IntegrationTest.class);

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  protected static OkHttpClient client = OkHttpUtils.client();

  private static final Network network = Network.newNetwork();

  /**
   * Subclasses can override this method to customise target application's environment
   */
  protected Map<String, String> getExtraEnv() {
    return Collections.emptyMap();
  }

  private static GenericContainer backend;

  @SuppressWarnings("resource")
  @BeforeAll
  static void setupSpec() {
    backend =
        new GenericContainer<>(
            "ghcr.io/open-telemetry/opentelemetry-java-instrumentation/smoke-test-fake-backend:20230409.4651925481")
            .withExposedPorts(8080)
            .waitingFor(Wait.forHttp("/health").forPort(8080))
            .withNetwork(network)
            .withNetworkAliases("backend")
            .withLogConsumer(new Slf4jLogConsumer(logger));
//            .withCreateContainerCmdModifier(cmd -> cmd.withPlatform("linux/arm64"));
    backend.start();
  }

  protected GenericContainer<?> target;

  void startTargetWithExtendedAgent() {
    target = buildTargetContainer();
    target.start();
  }

  @SuppressWarnings("resource")
  private GenericContainer<?> buildTargetContainer() {
    return new GenericContainer<>("ghcr.io/vmaleze/opentelemetry-java-ignore-monitoring-spans/smoke-test-spring-boot-actuator:jdk17-20230427.4820033766")
        .withExposedPorts(8080)
        .withNetwork(network)
        .withLogConsumer(new Slf4jLogConsumer(logger))
        .withCopyFileToContainer(
            MountableFile.forHostPath(System.getProperty("io.opentelemetry.smoketest.extendedAgentPath")),
            "/opentelemetry-javaagent.jar")
        // Adds instrumentation agent with debug configuration to the target application
        .withEnv(
            "JAVA_TOOL_OPTIONS",
            "-javaagent:/opentelemetry-javaagent.jar -Dotel.javaagent.debug=true")
        .withEnv("OTEL_EXPORTER_OTLP_ENDPOINT", "http://backend:8080")
        .withEnv(getExtraEnv());
  }

  @AfterEach
  void cleanup() throws IOException {
    client
        .newCall(
            new Request.Builder()
                .url(String.format("http://localhost:%d/clear", backend.getMappedPort(8080)))
                .build())
        .execute()
        .close();
  }

  void stopTarget() {
    target.stop();
  }

  @AfterAll
  static void cleanupSpec() {
    backend.stop();
  }

  protected static int countSpansByName(
      Collection<ExportTraceServiceRequest> traces, String spanName) {
    return (int) getSpanStream(traces).filter(it -> it.getName().equals(spanName)).count();
  }

  protected static Stream<Span> getSpanStream(Collection<ExportTraceServiceRequest> traces) {
    return traces.stream()
        .flatMap(it -> it.getResourceSpansList().stream())
        .flatMap(it -> it.getScopeSpansList().stream())
        .flatMap(it -> it.getSpansList().stream());
  }

  protected Collection<ExportTraceServiceRequest> waitForTraces()
      throws IOException, InterruptedException {
    String content = waitForContent();

    return StreamSupport.stream(OBJECT_MAPPER.readTree(content).spliterator(), false)
        .map(
            it -> {
              ExportTraceServiceRequest.Builder builder = ExportTraceServiceRequest.newBuilder();
              try {
                JsonFormat.parser().merge(OBJECT_MAPPER.writeValueAsString(it), builder);
              } catch (InvalidProtocolBufferException | JsonProcessingException e) {
                e.printStackTrace();
              }
              return builder.build();
            })
        .collect(Collectors.toList());
  }

  private String waitForContent() throws IOException, InterruptedException {
    long previousSize = 0;
    long deadline = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(30);
    String content = "[]";
    while (System.currentTimeMillis() < deadline) {

      Request request =
          new Request.Builder()
              .url(String.format("http://localhost:%d/get-traces", backend.getMappedPort(8080)))
              .build();

      try (ResponseBody body = client.newCall(request).execute().body()) {
        content = body.string();
      }

      if (content.length() > 2 && content.length() == previousSize) {
        break;
      }
      previousSize = content.length();
      System.out.printf("Current content size %d%n", previousSize);
      TimeUnit.MILLISECONDS.sleep(500);
    }

    return content;
  }
}
