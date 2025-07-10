## 📋 예약 상세 조회 API 테스트 가이드

### 🎯 새로 추가된 API

#### **GET** `/api/reservations/{id}`
예약 상세 정보를 조회합니다.

**파라미터:**
- `{id}`: 예약 ID (Path Variable)
- `userId`: 사용자 ID (Query Parameter)

**인증:** JWT 토큰 필요 (USER 권한)

---

### 🧪 테스트 방법

#### 1. cURL을 사용한 테스트
```bash
curl -X GET "http://localhost:8080/api/reservations/1?userId=1" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json"
```

#### 2. 예상 응답 (성공)
```json
{
  "success": true,
  "data": {
    "id": 1,
    "studio": {
      "id": 1,
      "name": "스튜디오 A",
      "phone": "02-1234-5678",
      "location": "서울시 강남구",
      "hourlyBaseRate": 30000,
      "perPersonRate": 5000
    },
    "reservationDate": "2025-07-15",
    "startTime": "14:00",
    "endTime": "16:00",
    "peopleCount": 2,
    "totalAmount": 80000,
    "status": "confirmed",
    "cancelReason": null,
    "cancelledAt": null,
    "createdAt": "2025-07-10T10:30:00",
    "updatedAt": "2025-07-10T10:30:00"
  },
  "message": "예약 상세 정보를 조회했습니다."
}
```

#### 3. 예상 응답 (오류)
```json
{
  "success": false,
  "data": null,
  "message": "본인의 예약만 조회할 수 있습니다."
}
```

---

### 🔒 보안 검증
1. **본인 예약만 조회**: 다른 사용자 ID로 요청시 오류
2. **JWT 토큰 필수**: 토큰 없이 요청시 401 오류
3. **존재하지 않는 예약**: 404 오류

---

### 📊 응답 데이터 설명

| 필드 | 타입 | 설명 | 필수 |
|------|------|------|------|
| `id` | Long | 예약 ID | ✅ |
| `studio` | Object | 스튜디오 정보 | ✅ |
| `studio.id` | Long | 스튜디오 ID | ✅ |
| `studio.name` | String | 스튜디오 이름 | ✅ |
| `studio.phone` | String | 스튜디오 전화번호 | ✅ |
| `studio.location` | String | 스튜디오 위치 | ✅ |
| `studio.hourlyBaseRate` | Long | 시간당 기본 요금 | ✅ |
| `studio.perPersonRate` | Long | 인당 추가 요금 | ✅ |
| `reservationDate` | String | 예약 날짜 (YYYY-MM-DD) | ✅ |
| `startTime` | String | 시작 시간 (HH:MM) | ✅ |
| `endTime` | String | 종료 시간 (HH:MM) | ✅ |
| `peopleCount` | Integer | 인원 수 | ✅ |
| `totalAmount` | Long | 총 금액 | ✅ |
| `status` | String | 예약 상태 | ✅ |
| `cancelReason` | String | 취소 사유 | ❌ |
| `cancelledAt` | String | 취소 시간 | ❌ |
| `createdAt` | String | 생성 시간 | ✅ |
| `updatedAt` | String | 수정 시간 | ✅ |

---

### 🚀 Swagger UI에서 테스트
1. 브라우저에서 `http://localhost:8080/swagger-ui.html` 접속
2. `Reservation` 섹션 확장
3. `GET /api/reservations/{id}` 엔드포인트 선택
4. `Try it out` 클릭
5. `id`와 `userId` 파라미터 입력
6. JWT 토큰 설정 후 `Execute` 클릭

---

### 💡 활용 예시

#### 프론트엔드에서 사용
```javascript
// 예약 상세 조회
async function getReservationDetail(reservationId, userId) {
  try {
    const response = await fetch(
      `/api/reservations/${reservationId}?userId=${userId}`,
      {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      }
    );
    
    const data = await response.json();
    
    if (data.success) {
      // 예약 상세 정보 표시
      displayReservationDetail(data.data);
    } else {
      // 오류 처리
      showError(data.message);
    }
  } catch (error) {
    console.error('예약 조회 실패:', error);
  }
}
```

#### 총 비용 계산 검증
```javascript
function verifyTotalAmount(reservation) {
  const { studio, startTime, endTime, peopleCount, totalAmount } = reservation;
  
  const hours = calculateHours(startTime, endTime);
  const expectedAmount = 
    (studio.hourlyBaseRate * hours) + 
    (studio.perPersonRate * peopleCount * hours);
  
  console.log('계산된 금액:', expectedAmount);
  console.log('실제 금액:', totalAmount);
  console.log('일치:', expectedAmount === totalAmount);
}
```
