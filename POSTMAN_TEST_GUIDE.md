# 🗑️ 함께줍줍(Hamkae) POSTMAN 테스트 가이드

> **포인트 시스템 & 상품권 교환 완성 버전 v2.0**

## 📋 목차

1. [환경 설정](#환경-설정)
2. [인증 API 테스트](#인증-api-테스트)
3. [마커 관리 API 테스트](#마커-관리-api-테스트)
4. [사진 관리 API 테스트](#사진-관리-api-테스트)
5. [AI 검증 API 테스트](#ai-검증-api-테스트)
6. [🆕 포인트 이력 API 테스트](#포인트-이력-api-테스트)
7. [🆕 상품권 교환 API 테스트](#상품권-교환-api-테스트)
8. [🆕 상품권 핀번호 API 테스트](#상품권-핀번호-api-테스트)
9. [🆕 사용자 프로필 API 테스트](#사용자-프로필-api-테스트)
10. [전체 시나리오 테스트](#전체-시나리오-테스트)

---

## 환경 설정

### Base URL
```
http://localhost:8080
```

### POSTMAN 환경변수 설정
1. **Environment 생성**: `Hamkae-Local`
2. **변수 추가**:
   ```
   base_url: http://localhost:8080
   jwt_token: {{로그인 후 설정}}
   user_id: {{로그인 후 설정}}
   marker_id: {{마커 등록 후 설정}}
   reward_id: {{상품권 교환 후 설정}}
   pin_number: {{핀번호 발급 후 설정}}
   ```

### 공통 Headers
```
Content-Type: application/json
Authorization: Bearer {{jwt_token}}
```

---

## 인증 API 테스트

### 1.1 회원가입

**요청**
```http
POST {{base_url}}/auth/register
Content-Type: application/json

{
  "name": "테스트유저",
  "username": "test_user_001",
  "password": "test123456"
}
```

**예상 응답 (200)**
```json
{
  "success": true,
  "message": "회원가입 성공",
  "data": {
    "user_id": 1
  }
}
```

**테스트 스크립트**
```javascript
pm.test("회원가입 성공", function () {
    pm.response.to.have.status(200);
    pm.expect(pm.response.json().success).to.be.true;
    pm.environment.set("user_id", pm.response.json().data.user_id);
});
```

### 1.2 로그인

**요청**
```http
POST {{base_url}}/auth/login
Content-Type: application/json

{
  "username": "test_user_001",
  "password": "test123456"
}
```

**예상 응답 (200)**
```json
{
  "success": true,
  "message": "로그인 성공",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VybmFtZSI6InRlc3RfdXNlcl8wMDEiLCJpYXQiOjE3MzY4MTk2MDAsImV4cCI6MTczNjkwNjAwMH0.abc123...",
    "user": {
      "id": 1,
      "name": "테스트유저",
      "username": "test_user_001",
      "points": 0
    }
  }
}
```

**테스트 스크립트**
```javascript
pm.test("로그인 성공", function () {
    pm.response.to.have.status(200);
    pm.expect(pm.response.json().success).to.be.true;
    pm.environment.set("jwt_token", pm.response.json().data.token);
});
```

---

## 마커 관리 API 테스트

### 2.1 마커 등록 (BEFORE 사진 포함)

**요청**
```http
POST {{base_url}}/markers
Authorization: Bearer {{jwt_token}}
Content-Type: multipart/form-data

Body (form-data):
- lat: "37.3943"
- lng: "126.9568"
- description: "공원 입구 쓰레기 발견"
- images: [파일 선택 - JPG/PNG 이미지]
```

**예상 응답 (200)**
```json
{
  "success": true,
  "message": "마커 등록 완료",
  "data": {
    "marker_id": 1,
    "uploaded_images": [
      "/images/2025/01/14/uuid-123.jpg"
    ],
    "image_count": 1,
    "created_at": "2025-01-14T10:00:00"
  }
}
```

**테스트 스크립트**
```javascript
pm.test("마커 등록 성공", function () {
    pm.response.to.have.status(200);
    pm.expect(pm.response.json().success).to.be.true;
    pm.environment.set("marker_id", pm.response.json().data.marker_id);
});
```

### 2.2 마커 목록 조회

**요청**
```http
GET {{base_url}}/markers?status=ACTIVE&page=0&size=10
```

**예상 응답 (200)**
```json
{
  "success": true,
  "message": "마커 조회 완료",
  "data": {
    "content": [
      {
        "id": 1,
        "lat": 37.3943,
        "lng": 126.9568,
        "description": "공원 입구 쓰레기 발견",
        "status": "ACTIVE",
        "createdAt": "2025-01-14T10:00:00",
        "user": {
          "id": 1,
          "name": "테스트유저"
        },
        "photos": [
          {
            "id": 1,
            "type": "BEFORE",
            "imagePath": "/images/2025/01/14/uuid-123.jpg"
          }
        ]
      }
    ],
    "totalElements": 1,
    "totalPages": 1,
    "currentPage": 0,
    "size": 10
  }
}
```

---

## 사진 관리 API 테스트

### 3.1 청소 인증용 AFTER 사진 업로드

**요청**
```http
POST {{base_url}}/photos/upload/cleanup
Authorization: Bearer {{jwt_token}}
Content-Type: multipart/form-data

Body (form-data):
- marker_id: {{marker_id}}
- images: [청소 완료 사진 파일]
```

**예상 응답 (200)**
```json
{
  "success": true,
  "message": "청소 인증용 사진 업로드 완료",
  "data": {
    "photo_ids": [2],
    "marker_id": 1,
    "type": "AFTER",
    "count": 1,
    "uploaded_images": [
      "/images/2025/01/14/uuid-456.jpg"
    ],
    "created_at": "2025-01-14T10:30:00"
  }
}
```

**테스트 스크립트**
```javascript
pm.test("AFTER 사진 업로드 성공", function () {
    pm.response.to.have.status(200);
    pm.expect(pm.response.json().success).to.be.true;
    pm.expect(pm.response.json().data.type).to.eql("AFTER");
});
```

### 3.2 마커별 사진 조회

**요청**
```http
GET {{base_url}}/photos/marker/{{marker_id}}
```

**예상 응답 (200)**
```json
{
  "success": true,
  "message": "마커별 사진 조회 완료",
  "data": {
    "marker_id": 1,
    "total_count": 2,
    "before_count": 1,
    "after_count": 1,
    "photos": [
      {
        "id": 1,
        "type": "BEFORE",
        "image_path": "/images/2025/01/14/uuid-123.jpg",
        "verification_status": "PENDING",
        "created_at": "2025-01-14T10:00:00",
        "user": {
          "id": 1,
          "name": "테스트유저"
        }
      },
      {
        "id": 2,
        "type": "AFTER",
        "image_path": "/images/2025/01/14/uuid-456.jpg",
        "verification_status": "PENDING",
        "created_at": "2025-01-14T10:30:00",
        "user": {
          "id": 1,
          "name": "테스트유저"
        }
      }
    ]
  }
}
```

---

## AI 검증 API 테스트

### 4.1 AI 검증 수행

**요청**
```http
POST {{base_url}}/ai-verification/verify/{{marker_id}}
Authorization: Bearer {{jwt_token}}
```

**예상 응답 (200) - 승인**
```json
{
  "success": true,
  "message": "AI 검증이 완료되었습니다.",
  "data": {
    "success": true,
    "verificationResult": "APPROVED",
    "gptResponse": "{\"verification_result\":\"APPROVED\",\"confidence\":0.86,\"reason\":\"청소 전후 사진을 비교한 결과, 쓰레기가 제거되었음을 확인했습니다.\"}",
    "confidence": 0.86,
    "verifiedAt": "2025-01-14T10:35:00",
    "processingTime": 2.5,
    "pointsRewarded": 120
  }
}
```

**테스트 스크립트**
```javascript
pm.test("AI 검증 성공", function () {
    pm.response.to.have.status(200);
    pm.expect(pm.response.json().success).to.be.true;
    pm.expect(pm.response.json().data.verificationResult).to.eql("APPROVED");
    pm.expect(pm.response.json().data.pointsRewarded).to.be.above(100);
});
```

### 4.2 AI 검증 상태 조회

**요청**
```http
GET {{base_url}}/ai-verification/status/{{marker_id}}
```

**예상 응답 (200)**
```json
{
  "success": true,
  "message": "검증 상태 조회 완료",
  "data": {
    "markerId": 1,
    "hasBeforePhotos": true,
    "beforePhotoCount": 1,
    "hasAfterPhotos": true,
    "afterPhotoCount": 1,
    "verificationStatus": "APPROVED",
    "verificationResult": "APPROVED",
    "confidence": 0.86,
    "verifiedAt": "2025-01-14T10:35:00",
    "gptResponse": "{\"verification_result\":\"APPROVED\",\"confidence\":0.86}",
    "pointsRewarded": 120,
    "canVerify": false,
    "verificationProgress": "COMPLETED"
  }
}
```

---

## 🆕 포인트 이력 API 테스트

### 5.1 포인트 이력 조회

**요청**
```http
GET {{base_url}}/api/point-history
Authorization: Bearer {{jwt_token}}
```

**예상 응답 (200)**
```json
{
  "success": true,
  "message": "포인트 이력 조회 성공",
  "data": [
    {
      "id": 1,
      "points": 120,
      "type": "EARNED",
      "description": "청소 인증 완료 (신뢰도: 86%)",
      "createdAt": "2025-01-14T10:35:00",
      "relatedPhotoId": 2
    }
  ]
}
```

**테스트 스크립트**
```javascript
pm.test("포인트 이력 조회 성공", function () {
    pm.response.to.have.status(200);
    pm.expect(pm.response.json().success).to.be.true;
    pm.expect(pm.response.json().data).to.be.an('array');
    if (pm.response.json().data.length > 0) {
        pm.expect(pm.response.json().data[0]).to.have.property('points');
        pm.expect(pm.response.json().data[0]).to.have.property('type');
    }
});
```

### 5.2 포인트 통계 조회

**요청**
```http
GET {{base_url}}/api/point-history/statistics
Authorization: Bearer {{jwt_token}}
```

**예상 응답 (200)**
```json
{
  "success": true,
  "message": "포인트 통계 조회 성공",
  "data": {
    "totalEarned": 120,
    "totalUsed": 0,
    "currentPoints": 120,
    "availablePoints": 120
  }
}
```

### 5.3 포인트 이력 필터링

**요청**
```http
GET {{base_url}}/api/point-history/filter?type=EARNED&limit=10
Authorization: Bearer {{jwt_token}}
```

**예상 응답 (200)**
```json
{
  "success": true,
  "message": "포인트 이력 조회 성공",
  "data": [
    {
      "id": 1,
      "points": 120,
      "type": "EARNED",
      "description": "청소 인증 완료 (신뢰도: 86%)",
      "createdAt": "2025-01-14T10:35:00",
      "relatedPhotoId": 2
    }
  ]
}
```

---

## 🆕 상품권 교환 API 테스트

### 6.1 상품권 즉시 교환 (POST 방식)

**요청 방법 1: 포인트 명시**
```http
POST {{base_url}}/api/rewards
Authorization: Bearer {{jwt_token}}
Content-Type: application/json

{
  "pointsUsed": 5000,
  "rewardType": "FIVE_THOUSAND"
}
```

**요청 방법 2: 포인트 자동 계산**
```http
POST {{base_url}}/api/rewards
Authorization: Bearer {{jwt_token}}
Content-Type: application/json

{
  "rewardType": "FIVE_THOUSAND"
}
```

**예상 응답 (400) - 포인트 부족**
```json
{
  "success": false,
  "message": "보유 포인트가 부족합니다. 현재: 120, 필요: 5000",
  "data": null
}
```

**포인트 충분할 때 예상 응답 (200)**
```json
{
  "success": true,
  "message": "상품권 교환이 완료되었습니다",
  "data": {
    "id": 1,
    "pointsUsed": 5000,
    "rewardType": "FIVE_THOUSAND",
    "quantity": 1,
    "status": "APPROVED",
    "createdAt": "2025-01-14T11:00:00",
    "processedAt": "2025-01-14T11:00:00",
    "pinNumbers": ["1234-5678-9012-3456"]
  }
}
```

**테스트 스크립트 (성공 시)**
```javascript
pm.test("상품권 즉시 교환 성공", function () {
    pm.response.to.have.status(200);
    pm.expect(pm.response.json().success).to.be.true;
    pm.expect(pm.response.json().data.status).to.eql("APPROVED");
    pm.expect(pm.response.json().data.pinNumbers).to.be.an('array');
    pm.environment.set("reward_id", pm.response.json().data.id);
    pm.environment.set("pin_number", pm.response.json().data.pinNumbers[0]);
});
```

### 6.1.1 간편 상품권 교환 (GET 방식)

**요청 (가장 간단한 방법)**
```http
GET {{base_url}}/api/rewards/exchange/FIVE_THOUSAND
Authorization: Bearer {{jwt_token}}
```

**다른 상품권 타입 예시**
```http
GET {{base_url}}/api/rewards/exchange/TEN_THOUSAND
Authorization: Bearer {{jwt_token}}
```

```http
GET {{base_url}}/api/rewards/exchange/THIRTY_THOUSAND
Authorization: Bearer {{jwt_token}}
```

**예상 응답 (200)**
```json
{
  "success": true,
  "message": "상품권 교환이 완료되었습니다 (간편 교환)",
  "data": {
    "id": 1,
    "pointsUsed": 5000,
    "rewardType": "FIVE_THOUSAND",
    "quantity": 1,
    "status": "APPROVED",
    "createdAt": "2025-01-14T11:00:00",
    "processedAt": "2025-01-14T11:00:00",
    "pinNumbers": ["1234-5678-9012-3456"]
  }
}
```

**테스트 스크립트**
```javascript
pm.test("간편 상품권 교환 성공", function () {
    pm.response.to.have.status(200);
    pm.expect(pm.response.json().success).to.be.true;
    pm.expect(pm.response.json().data.status).to.eql("APPROVED");
    pm.expect(pm.response.json().data.pinNumbers).to.be.an('array');
    pm.environment.set("reward_id", pm.response.json().data.id);
});
```

### 6.2 상품권 교환 이력 조회

**요청**
```http
GET {{base_url}}/api/rewards
Authorization: Bearer {{jwt_token}}
```

**예상 응답 (200)**
```json
{
  "success": true,
  "message": "상품권 교환 이력 조회 성공",
  "data": [
    {
      "id": 1,
      "pointsUsed": 5000,
      "rewardType": "5천원 상품권",
      "status": "PENDING",
      "createdAt": "2025-01-14T11:00:00",
      "approvedAt": null,
      "rejectedAt": null,
      "reason": null
    }
  ]
}
```



---

## 🆕 상품권 핀번호 API 테스트

### 7.1 내 핀번호 목록 조회

**요청**
```http
GET {{base_url}}/api/reward-pins
Authorization: Bearer {{jwt_token}}
```

**예상 응답 (200)**
```json
{
  "success": true,
  "message": "핀번호 목록 조회 성공",
  "data": [
    {
      "id": 1,
      "rewardId": 1,
      "maskedPinNumber": "****-****-****-5678",
      "rewardType": "5천원 상품권",
      "pointsUsed": 5000,
      "issuedAt": "2025-01-14T11:05:00",
      "expiresAt": "2026-01-14T11:05:00",
      "isUsed": false,
      "usedAt": null,
      "isAvailable": true,
      "isExpired": false
    }
  ]
}
```

**테스트 스크립트**
```javascript
pm.test("핀번호 목록 조회 성공", function () {
    pm.response.to.have.status(200);
    pm.expect(pm.response.json().success).to.be.true;
    pm.expect(pm.response.json().data).to.be.an('array');
});
```

### 7.2 사용 가능한 핀번호 조회

**요청**
```http
GET {{base_url}}/api/reward-pins/available
Authorization: Bearer {{jwt_token}}
```

**예상 응답 (200)**
```json
{
  "success": true,
  "message": "사용 가능한 핀번호 조회 성공",
  "data": [
    {
      "id": 1,
      "rewardId": 1,
      "maskedPinNumber": "****-****-****-5678",
      "rewardType": "5천원 상품권",
      "pointsUsed": 5000,
      "issuedAt": "2025-01-14T11:05:00",
      "expiresAt": "2026-01-14T11:05:00",
      "isUsed": false,
      "isAvailable": true,
      "isExpired": false
    }
  ]
}
```

### 7.3 특정 상품권의 실제 핀번호 조회

**요청**
```http
GET {{base_url}}/api/reward-pins/reward/{{reward_id}}
Authorization: Bearer {{jwt_token}}
```

**예상 응답 (200)**
```json
{
  "success": true,
  "message": "핀번호 조회 성공",
  "data": {
    "id": 1,
    "rewardId": 1,
    "maskedPinNumber": "****-****-****-5678",
    "fullPinNumber": "1234-5678-9012-5678",
    "rewardType": "5천원 상품권",
    "pointsUsed": 5000,
    "issuedAt": "2025-01-14T11:05:00",
    "expiresAt": "2026-01-14T11:05:00",
    "isUsed": false,
    "isAvailable": true
  }
}
```

**테스트 스크립트**
```javascript
pm.test("실제 핀번호 조회 성공", function () {
    pm.response.to.have.status(200);
    pm.expect(pm.response.json().success).to.be.true;
    pm.expect(pm.response.json().data).to.have.property('fullPinNumber');
    pm.environment.set("pin_number", pm.response.json().data.fullPinNumber);
});
```

### 7.4 핀번호 사용 처리

**요청**
```http
POST {{base_url}}/api/reward-pins/use/{{pin_number}}
```

**예상 응답 (200)**
```json
{
  "success": true,
  "message": "핀번호 사용 처리 완료",
  "data": {
    "id": 1,
    "rewardType": "5천원 상품권",
    "pointsUsed": 5000,
    "isUsed": true,
    "usedAt": "2025-01-14T12:30:00"
  }
}
```

**테스트 스크립트**
```javascript
pm.test("핀번호 사용 처리 성공", function () {
    pm.response.to.have.status(200);
    pm.expect(pm.response.json().success).to.be.true;
    pm.expect(pm.response.json().data.isUsed).to.be.true;
});
```

### 7.5 핀번호 정보 확인

**요청**
```http
GET {{base_url}}/api/reward-pins/info/{{pin_number}}
```

**예상 응답 (200)**
```json
{
  "success": true,
  "message": "핀번호 정보 조회 성공",
  "data": {
    "id": 1,
    "rewardType": "5천원 상품권",
    "pointsUsed": 5000,
    "issuedAt": "2025-01-14T11:05:00",
    "expiresAt": "2026-01-14T11:05:00",
    "isUsed": true,
    "isAvailable": false,
    "isExpired": false
  }
}
```

---

## 🆕 사용자 프로필 API 테스트

### 8.1 사용자 프로필 조회

**요청**
```http
GET {{base_url}}/api/users/profile
Authorization: Bearer {{jwt_token}}
```

**예상 응답 (200)**
```json
{
  "success": true,
  "message": "사용자 프로필 조회 성공",
  "data": {
    "id": 1,
    "name": "테스트유저",
    "username": "test_user_001",
    "points": 120,
    "totalEarnedPoints": 120,
    "totalUsedPoints": 0,
    "reportedMarkersCount": 1,
    "uploadedPhotosCount": 2,
    "rewardExchangeCount": 1,
    "issuedPinsCount": 1,
    "createdAt": "2025-01-14T09:00:00",
    "updatedAt": "2025-01-14T12:30:00"
  }
}
```

**테스트 스크립트**
```javascript
pm.test("사용자 프로필 조회 성공", function () {
    pm.response.to.have.status(200);
    pm.expect(pm.response.json().success).to.be.true;
    pm.expect(pm.response.json().data).to.have.property('username');
    pm.expect(pm.response.json().data).to.have.property('points');
});
```

### 8.2 포인트 현황 요약

**요청**
```http
GET {{base_url}}/api/users/points/summary
Authorization: Bearer {{jwt_token}}
```

**예상 응답 (200)**
```json
{
  "success": true,
  "message": "포인트 현황 조회 성공",
  "data": {
    "totalEarned": 120,
    "totalUsed": 0,
    "currentPoints": 120,
    "availablePoints": 120
  }
}
```

### 8.3 활동 요약

**요청**
```http
GET {{base_url}}/api/users/activity/summary
Authorization: Bearer {{jwt_token}}
```

**예상 응답 (200)**
```json
{
  "success": true,
  "message": "활동 요약 조회 성공",
  "data": {
    "userId": 1,
    "username": "test_user_001",
    "name": "테스트유저",
    "currentPoints": 120,
    "totalEarnedPoints": 120,
    "totalUsedPoints": 0,
    "reportedMarkersCount": 1,
    "uploadedPhotosCount": 2,
    "rewardExchangeCount": 1,
    "issuedPinsCount": 1,
    "memberSince": "2025-01-14T09:00:00"
  }
}
```

---

## 전체 시나리오 테스트

### 완전한 플로우 테스트 시나리오

```
1. 회원가입 → 로그인
2. 마커 등록 (BEFORE 사진 포함)
3. AFTER 사진 업로드
4. AI 검증 수행
5. 포인트 적립 확인
6. 포인트가 충분할 때까지 1-5 반복
7. 상품권 즉시 교환 (핀번호 자동 발급)
8. 핀번호 확인 및 사용
9. 최종 프로필 확인
```

### POSTMAN Collection 구조

```
📁 Hamkae API v2.0
├── 📁 1. Authentication
│   ├── POST Register
│   └── POST Login
├── 📁 2. Markers
│   ├── POST Create Marker
│   ├── GET List Markers
│   ├── GET Marker by ID
│   └── DELETE Marker
├── 📁 3. Photos
│   ├── POST Upload AFTER Photos
│   ├── GET Photo by ID
│   └── GET Photos by Marker
├── 📁 4. AI Verification
│   ├── POST Verify Marker
│   ├── GET Verification Status
│   └── GET Health Check
├── 📁 5. Point History 🆕
│   ├── GET Point History
│   ├── GET Point Statistics
│   └── GET Filtered History
├── 📁 6. Rewards 🆕
│   ├── POST Instant Exchange
│   └── GET Exchange History
├── 📁 7. Reward Pins 🆕
│   ├── GET My Pins
│   ├── GET Available Pins
│   ├── GET Used Pins
│   ├── GET Pin by Reward ID
│   ├── POST Use Pin
│   └── GET Pin Info
└── 📁 8. User Profile 🆕
    ├── GET User Profile
    ├── GET Points Summary
    └── GET Activity Summary
```

### 환경별 테스트

**Development**
```
base_url: http://localhost:8080
```

**Staging** (예시)
```
base_url: http://staging.hamkae.com
```

**Production** (예시)
```
base_url: http://api.hamkae.com
```

---

## 💡 테스트 팁

### 1. 공통 테스트 스크립트
모든 요청에 추가할 수 있는 공통 테스트:

```javascript
pm.test("응답 시간 확인", function () {
    pm.expect(pm.response.responseTime).to.be.below(5000);
});

pm.test("응답 구조 확인", function () {
    pm.expect(pm.response.json()).to.have.property('success');
    pm.expect(pm.response.json()).to.have.property('message');
    pm.expect(pm.response.json()).to.have.property('data');
});
```

### 2. 데이터 생성 스크립트
테스트 데이터 자동 생성:

```javascript
// Pre-request Script
pm.globals.set("random_username", "user_" + Math.random().toString(36).substr(2, 9));
pm.globals.set("random_password", "pass_" + Math.random().toString(36).substr(2, 8));
pm.globals.set("current_timestamp", new Date().toISOString());
```

### 3. 에러 처리 테스트
다양한 에러 상황 테스트:

```javascript
pm.test("인증 실패 처리", function () {
    if (pm.response.code === 401) {
        pm.expect(pm.response.json().success).to.be.false;
        pm.expect(pm.response.json().message).to.include("인증");
    }
});
```

### 4. 성능 테스트
응답 시간 모니터링:

```javascript
pm.test("API 성능 확인", function () {
    pm.expect(pm.response.responseTime).to.be.below(2000); // 2초 이내
});
```

---

**🎯 이 가이드를 통해 함께줍줍 앱의 모든 API를 체계적으로 테스트할 수 있습니다!**

> **참고**: 실제 테스트 시에는 이미지 파일 준비, JWT 토큰 유효성 관리, 환경별 설정 등을 고려해야 합니다.
