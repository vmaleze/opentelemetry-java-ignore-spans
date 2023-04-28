FROM busybox

COPY extension/build/libs/opentelemetry-javaagent.jar /javaagent.jar

RUN chmod -R go+r /javaagent.jar
