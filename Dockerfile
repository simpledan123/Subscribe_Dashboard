FROM eclipse-temurin:25-jdk AS builder
WORKDIR /app
COPY gradlew gradlew.bat ./
COPY gradle ./gradle
RUN chmod +x gradlew
# 의존성만 먼저 받아서 레이어 캐시 활용
COPY build.gradle settings.gradle ./
RUN ./gradlew dependencies --no-daemon || true
# 소스 빌드
COPY src ./src
RUN ./gradlew bootJar -x test --no-daemon

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]