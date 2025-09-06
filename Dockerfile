FROM gradle:8.14.3-jdk-alpine AS builder

WORKDIR /app

COPY build.gradle settings.gradle gradlew ./
COPY gradle gradle/
COPY src ./src
RUN ./gradlew clean build -x test

FROM eclipse-temurin:21-jre-alpine-3.22
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
CMD ["java", "-jar", "app.jar"]

EXPOSE 8080