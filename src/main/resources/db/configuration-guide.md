# ===========================================
# Studio-Pick 예약 시스템 설정 가이드
# ===========================================

## 시스템 설정 (SystemSetting 테이블)

### 예약 관련 설정
```sql
INSERT INTO "SystemSetting" (setting_key, setting_value, description, category, data_type, default_value) VALUES
('reservation.max.people', '20', '예약 최대 인원 수', 'reservation', 'INTEGER', '20'),
('reservation.min.hours', '1', '예약 최소 시간', 'reservation', 'INTEGER', '1'),
('reservation.max.hours', '8', '예약 최대 시간', 'reservation', 'INTEGER', '8'),
('reservation.advance.days', '90', '예약 가능한 최대 사전 일수', 'reservation', 'INTEGER', '90'),
('reservation.cancel.hours', '24', '예약 취소 가능 시간(시간 전)', 'reservation', 'INTEGER', '24'),
('payment.min.amount', '10000', '최소 결제 금액', 'payment', 'INTEGER', '10000'),
('studio.operating.start.hour', '9', '스튜디오 운영 시작 시간', 'studio', 'INTEGER', '9'),
('studio.operating.end.hour', '18', '스튜디오 운영 종료 시간', 'studio', 'INTEGER', '18'),
('studio.default.hourly.rate', '30000', '스튜디오 기본 시간당 요금', 'studio', 'INTEGER', '30000'),
('studio.default.per.person.rate', '5000', '스튜디오 기본 인당 요금', 'studio', 'INTEGER', '5000'),
('studio.default.max.people', '10', '스튜디오 기본 최대 인원', 'studio', 'INTEGER', '10'),
('pagination.default.size', '10', '페이징 기본 크기', 'pagination', 'INTEGER', '10'),
('pagination.max.size', '100', '페이징 최대 크기', 'pagination', 'INTEGER', '100');
```

## Redis 캐시 설정 최적화

### application.yml 추가 설정
```yaml
spring:
  cache:
    type: redis
    redis:
      time-to-live: 1800000  # 30분
      cache-null-values: false
      use-key-prefix: true
      key-prefix: "studio-pick:"
  
  data:
    redis:
      host: localhost
      port: 6379
      database: 0
      timeout: 2000ms
      lettuce:
        pool:
          max-active: 10
          max-idle: 10
          min-idle: 1
          max-wait: -1ms

logging:
  level:
    org.springframework.cache: DEBUG
    org.example.studiopick: INFO
    org.hibernate.SQL: WARN  # 프로덕션에서는 WARN으로 변경
```

## 보안 설정 강화

### JWT 설정 개선
```yaml
jwt:
  secret: ${JWT_SECRET:Toughtimesneverlastbuttoughpeopledo}
  access-token-expiration: ${JWT_ACCESS_EXPIRATION:1800000}  # 30분
  refresh-token-expiration: ${JWT_REFRESH_EXPIRATION:604800000}  # 7일
  issuer: "studio-pick"
  audience: "studio-pick-users"
```

### CORS 설정
```yaml
cors:
  allowed-origins: 
    - "http://localhost:3000"
    - "https://studio-pick.com"
  allowed-methods: 
    - GET
    - POST
    - PUT
    - DELETE
    - PATCH
    - OPTIONS
  allowed-headers: "*"
  allow-credentials: true
  max-age: 3600
```

## 성능 모니터링 설정

### Actuator 설정
```yaml
management:
  endpoints:
    web:
      exposure:
        include: 
          - health
          - metrics
          - prometheus
          - info
          - loggers
  endpoint:
    health:
      show-details: when-authorized
      probes:
        enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
    distribution:
      percentiles-histogram:
        http.server.requests: true
```

## 데이터베이스 연결 풀 최적화

### HikariCP 설정
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: ${DB_POOL_SIZE:20}
      minimum-idle: ${DB_POOL_MIN_IDLE:5}
      connection-timeout: ${DB_CONNECTION_TIMEOUT:30000}
      idle-timeout: ${DB_IDLE_TIMEOUT:600000}
      max-lifetime: ${DB_MAX_LIFETIME:1800000}
      leak-detection-threshold: ${DB_LEAK_DETECTION:60000}
      auto-commit: false
      connection-test-query: "SELECT 1"
```

## 프로파일별 설정

### application-dev.yml
```yaml
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        use_sql_comments: true
  
logging:
  level:
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
    org.example.studiopick: DEBUG
```

### application-prod.yml
```yaml
spring:
  jpa:
    show-sql: false
    properties:
      hibernate:
        format_sql: false
        use_sql_comments: false
  
logging:
  level:
    org.hibernate.SQL: WARN
    org.example.studiopick: INFO
    org.springframework.web: WARN
```

## 환경변수 설정 예시

### .env 파일
```bash
# 데이터베이스
DB_URL=jdbc:postgresql://localhost:5432/studio_pick
DB_USERNAME=postgres
DB_PASSWORD=your_password
DB_POOL_SIZE=20

# JWT
JWT_SECRET=your_super_secret_key_here
JWT_ACCESS_EXPIRATION=1800000
JWT_REFRESH_EXPIRATION=604800000

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# 외부 API
TOSS_CLIENT_KEY=your_toss_client_key
TOSS_SECRET_KEY=your_toss_secret_key
KAKAO_CLIENT_ID=your_kakao_client_id

# AWS S3
AWS_ACCESS_KEY=your_aws_access_key
AWS_SECRET_KEY=your_aws_secret_key
AWS_S3_BUCKET=your_s3_bucket
AWS_REGION=ap-northeast-2

# 기타
ACTIVE_PROFILE=dev
SERVER_PORT=8080
```

## 모니터링 및 알림 설정

### 로그 설정
```yaml
logging:
  config: classpath:logback-spring.xml
  pattern:
    console: "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/studio-pick.log
  logback:
    rollingpolicy:
      max-file-size: 100MB
      max-history: 30
      total-size-cap: 3GB
```

## 보안 체크리스트

1. **환경변수 사용**: 민감한 정보는 모두 환경변수로 관리
2. **HTTPS 적용**: 프로덕션에서는 반드시 HTTPS 사용
3. **CORS 설정**: 허용할 오리진만 명시적으로 설정
4. **JWT 보안**: 충분히 복잡한 시크릿 키 사용
5. **데이터베이스**: 최소 권한 원칙 적용
6. **API 레이트 리미팅**: 과도한 요청 방지
7. **로그 모니터링**: 보안 이벤트 로깅 및 모니터링

## 성능 최적화 가이드

1. **인덱스 최적화**: 정기적인 쿼리 성능 분석
2. **캐시 전략**: 자주 조회되는 데이터 캐싱
3. **연결 풀 튜닝**: 애플리케이션 부하에 맞는 설정
4. **쿼리 최적화**: N+1 문제 해결
5. **압축 설정**: 응답 데이터 압축 활성화
