# Multi-stage image for Render.
# Build stage compiles Spring Boot + React; runtime is a slim JRE.
# Secrets come from Render env vars — do not bake them into this image.

FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

ENV MAVEN_OPTS="-XX:MaxRAMPercentage=70.0"
ENV NODE_OPTIONS="--max-old-space-size=2048"

COPY . .
RUN chmod +x mvnw \
    && ./mvnw -B -Pprod -Dmaven.test.skip=true package --no-transfer-progress

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

RUN groupadd --system app && useradd --system --gid app app
COPY --from=build /app/target/fullstackapp-0.0.1-SNAPSHOT.jar app.jar
USER app

EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-XX:MaxRAMPercentage=70.0 -XX:+UseSerialGC -XX:+UseContainerSupport -Xss256k"

# Render sets PORT; application-prod.yml binds server.port to ${PORT:8080}.
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
