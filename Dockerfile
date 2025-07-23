# 1) 빌드 단계: Gradle 공식 이미지를 사용해 JAR 파일을 생성합니다.
FROM gradle:7.6-jdk17 AS builder
WORKDIR /workspace

# 의존성 캐싱을 위해 Gradle 설정 파일과 래퍼만 먼저 복사합니다.
COPY settings.gradle settings.gradle
COPY build.gradle build.gradle
COPY gradlew gradlew
COPY gradle gradle

# 소스 코드 전체를 복사하고, 테스트를 제외하고 부트 JAR을 빌드합니다.
COPY src src
RUN chmod +x gradlew \
 && ./gradlew bootJar --no-daemon -x test

# ---------------------------------------------------------------------
# 2) 런타임 단계: 가벼운 JRE 이미지를 사용해 애플리케이션만 실행합니다.
FROM openjdk:17-jre-slim
WORKDIR /app

# 빌드 단계에서 생성된 fat‑jar 파일을 가져옵니다.
COPY --from=builder /workspace/build/libs/*.jar app.jar

# 컨테이너가 리스닝할 포트를 선언합니다. (기본 8080)
EXPOSE 8080

# 컨테이너 시작 시 Spring Boot 애플리케이션을 실행합니다.
ENTRYPOINT ["java", "-jar", "app.jar"]
