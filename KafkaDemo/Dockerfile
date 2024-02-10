#FROM flink:1.18.1-java17
FROM flink-openj9
USER flink
COPY build/libs/KafkaDemo-*.jar /opt/flink/usrlib/KafkaDemo.jar
RUN wget -P /opt/flink/lib \
    https://repo.maven.apache.org/maven2/org/apache/flink/flink-sql-connector-kafka/3.1.0-1.18/flink-sql-connector-kafka-3.1.0-1.18.jar
    ##https://repo.maven.apache.org/maven2/org/apache/flink/flink-connector-kafka/3.1.0-1.18/flink-connector-kafka-3.1.0-1.18.jar \
          #    https://repo.maven.apache.org/maven2/org/apache/flink/flink-connector-base/1.18.1/flink-connector-base-1.18.1.jar \
          #    https://repo.maven.apache.org/maven2/org/apache/kafka/kafka-clients/3.6.1/kafka-clients-3.6.1.jar
