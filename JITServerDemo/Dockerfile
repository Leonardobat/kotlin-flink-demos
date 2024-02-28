#FROM flink:1.18.1-java17
FROM flink-openj9
USER flink
COPY build/libs/KafkaDemo-*.jar /opt/flink/usrlib/KafkaDemo.jar
RUN wget -P /opt/flink/lib \
    https://repo.maven.apache.org/maven2/org/apache/flink/flink-sql-connector-kafka/3.1.0-1.18/flink-sql-connector-kafka-3.1.0-1.18.jar
