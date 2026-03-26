# Multi-stage build for smaller image size
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

# Copy pom.xml and download dependencies first (better layer caching)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean install
RUN mkdir -p /app && \
    mvn clean package -DskipTests && \
    ls -la target/ && \
    mv target/csv-processor-*.jar /app/csv-processor.jar

# Runtime stage - smaller image with only JRE
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /app/csv-processor.jar ./csv-processor.jar

# Create directory for data files
RUN mkdir -p /data

# Set JVM options for memory efficiency
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XshowSettings:vm -XX:+PrintGCDetails -XX:+PrintGCDetails"

WORKDIR /data

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/csv-processor.jar \"$0\" \"$@\""]
CMD ["--help"]
