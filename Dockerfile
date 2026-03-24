# ---------- BUILD STAGE ----------
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# copy gradle wrapper trước để cache dependency
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

# download dependencies (cache layer)
RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon || true

# copy source
COPY src src

# build jar
RUN ./gradlew bootJar -x test --no-daemon

# ---------- RUNTIME STAGE ----------
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S app && adduser -S app -G app

COPY --from=builder /app/build/libs/*.jar app.jar

USER app

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

EXPOSE $BE_PORT

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]
