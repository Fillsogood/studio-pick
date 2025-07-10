## 🚨 MyBatis Mapper Bean 문제 해결 가이드

### 문제
`ReservationSearchMapper` Bean을 찾을 수 없다는 오류 발생

### 해결 시도 순서

#### 1단계: 기본 설정 확인 ✅
- [x] `@MapperScan` 경로 수정
- [x] `@Mapper` 어노테이션 추가
- [x] `@Repository` 어노테이션 추가
- [x] MyBatis 설정 클래스 생성

#### 2단계: 애플리케이션 재시작
```bash
./gradlew clean
./gradlew bootRun
```

#### 3단계: 여전히 문제가 있다면 - 임시 해결책
다음 중 하나를 선택:

**옵션 A: MyBatis 없이 JPA만 사용**
- AdminReservationServiceImpl에서 MyBatis 호출을 JPA로 변경
- ReservationServiceImpl에서 MyBatis 호출을 JPA로 변경

**옵션 B: 매퍼를 빈으로 수동 등록**
```java
@Configuration
public class MapperConfig {
    
    @Bean
    public ReservationSearchMapper reservationSearchMapper(SqlSessionFactory sqlSessionFactory) {
        return sqlSessionFactory.getConfiguration().getMapper(ReservationSearchMapper.class, sqlSessionFactory.openSession());
    }
}
```

### 4단계: 디버그 로그 활성화
application.yml에 추가:
```yaml
logging:
  level:
    org.mybatis: DEBUG
    org.apache.ibatis: DEBUG
    org.springframework.beans: DEBUG
```

### 현재 상태
- ✅ MapperScan 설정 수정 완료
- ✅ MyBatis 설정 클래스 생성 완료
- ✅ Mapper 어노테이션 추가 완료

### 다음 실행할 명령어
```bash
./gradlew clean bootRun
```

만약 여전히 문제가 발생하면, 에러 로그를 전체적으로 확인해서 다른 원인이 있는지 파악해주세요.
