ARG DOCKER_REGISTRY
ARG MAVEN_IMAGE_VERSION=3.9.6-eclipse-temurin-17
ARG JRE_IMAGE_VERSION=17-jre-jammy

FROM ${DOCKER_REGISTRY}library/maven:${MAVEN_IMAGE_VERSION} AS deps
WORKDIR /app
COPY pom.xml settings.xml ./
RUN mkdir -p /root/.m2 && cp settings.xml /root/.m2/settings.xml
RUN mvn -B -e -C -T 1C dependency:resolve -DskipTests

FROM ${DOCKER_REGISTRY}library/maven:${MAVEN_IMAGE_VERSION} AS builder
WORKDIR /app
COPY --from=deps /root/.m2 /root/.m2
COPY pom.xml settings.xml ./
RUN mkdir -p /root/.m2 && cp settings.xml /root/.m2/settings.xml
COPY src ./src
RUN mvn clean package -DskipTests -B -e -C -T 1C

FROM ${DOCKER_REGISTRY}library/eclipse-temurin:${JRE_IMAGE_VERSION} AS runtime
LABEL org.opencontainers.image.title="home-tools-backend"
LABEL org.opencontainers.image.description="home-tools-backend"
LABEL org.opencontainers.image.authors="home-tools"

WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
