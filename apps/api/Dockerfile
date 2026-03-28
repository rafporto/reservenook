FROM eclipse-temurin:21-jdk AS builder
WORKDIR /workspace
COPY gradlew gradlew
COPY gradlew.bat gradlew.bat
COPY gradle gradle
COPY settings.gradle.kts settings.gradle.kts
COPY build.gradle.kts build.gradle.kts
COPY apps/api/build.gradle.kts apps/api/build.gradle.kts
COPY apps/api/src apps/api/src
RUN chmod +x gradlew && ./gradlew :apps:api:bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
ENV SERVER_PORT=8080
COPY --from=builder /workspace/apps/api/build/libs/reservenook-api.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
