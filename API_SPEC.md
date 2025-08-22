# 함께줍줍(Hamkae) API 간단 명세 (FE 핵심)

## 기본
- Base URL: `http://localhost:8080`
- Auth: 필요한 API에 `Authorization: Bearer {JWT}`
- 공통 응답: `ApiResponse<T>` → `success:boolean`, `message:string`, `data:T|null`

## 1) 인증

### POST /auth/register
- Body(JSON)
```json
{ "name":"홍길동", "username":"hong123", "password":"password123" }
```
- 200
```json
{ "success":true, "message":"회원가입 성공", "data": { "user_id":1 } }
```

### POST /auth/login
- Body(JSON)
```json
{ "username":"hong123", "password":"password123" }
```
- 200
```json
{
  "success":true,
  "message":"로그인 성공",
  "data":{
    "token":"JWT_TOKEN",
    "user":{ "id":1, "name":"홍길동", "points":0 }
  }
}
```

## 2) 마커

### POST /markers (사진 포함 등록)
- Header: `Authorization: Bearer {JWT}`
- Body(form-data): `lat:string`, `lng:string`, `description:string`, `images:file[]?`
- 200
```json
{
  "success":true,
  "message":"마커 등록 완료",
  "data":{
    "marker_id":10,
    "uploaded_images":["/images/2025/08/18/uuid.jpg"],
    "image_count":1
  }
}
```

### GET /markers (활성 마커 목록)
- 200
```json
{
  "success":true,
  "message":"마커 조회 완료",
  "data":[
    {
      "id":10,
      "lat":37.5665,
      "lng":126.978,
      "description":"공원 입구",
      "status":"ACTIVE",
      "createdAt":"2025-08-18T10:12:00",
      "photos":[ { "id":101, "type":"BEFORE", "imagePath":"/images/..." } ]
    }
  ]
}
```

### GET /markers/{id}
- 200: `MarkerResponseDTO`

### GET /markers/user/{userId}
- 200: `MarkerResponseDTO[]`

### DELETE /markers/{id}
- Header: `Authorization: Bearer {JWT}`
- 200
```json
{ "success":true, "message":"마커와 연결된 모든 사진이 완전히 삭제되었습니다." }
```

## 3) 사진

### POST /photos/upload/cleanup (AFTER 업로드)
- Header: `Authorization: Bearer {JWT}`
- Body(form-data): `marker_id:number`, `images:file[]`
- 동작: 업로드 즉시 200, AI 검증은 비동기 → 상태는 `/ai-verification/status/{markerId}` 조회
- 200
```json
{
  "success":true,
  "message":"청소 인증용 사진 업로드 완료",
  "data":{ "photo_ids":[201,202], "marker_id":10, "type":"AFTER", "count":2 }
}
```

### GET /photos/{id}
- 200
```json
{
  "success":true,
  "message":"사진 조회 완료",
  "data":{
    "id":201,
    "marker_id":10,
    "user_id":1,
    "image_path":"/images/2025/08/18/uuid.jpg",
    "type":"AFTER",
    "verification_status":"PENDING",
    "created_at":"2025-08-18T10:20:00",
    "gpt_response":null
  }
}
```

### GET /photos/marker/{markerId}
- 200
```json
{
  "success":true,
  "message":"마커별 사진 조회 완료",
  "data":{
    "marker_id":10,
    "total_count":3,
    "photos":[
      { "id":101, "type":"BEFORE", "image_path":"/images/...", "verification_status":"PENDING", "created_at":"2025-08-18T10:10:00" }
    ]
  }
}
```

## 4) AI 검증

### POST /ai-verification/verify/{markerId} (수동 검증)
- 200 (`GptVerificationResponseDTO`)
```json
{
  "success":true,
  "message":"AI 검증이 완료되었습니다.",
  "data":{
    "success":true,
    "verificationResult":"APPROVED",
    "gptResponse":"{\"verification_result\":\"APPROVED\",\"confidence\":0.86}",
    "confidence":0.86,
    "verifiedAt":"2025-08-18T10:21:30"
  }
}
```

### GET /ai-verification/status/{markerId}
- 200
```json
{
  "success":true,
  "message":"검증 상태 조회 완료",
  "data":{
    "markerId":10,
    "hasBeforePhotos":true,
    "beforePhotoCount":1,
    "hasAfterPhotos":true,
    "afterPhotoCount":2,
    "verificationStatus":"APPROVED",
    "gptResponse":"{\"verification_result\":\"APPROVED\",\"confidence\":0.86}"
  }
}
```

### GET /ai-verification/health
- 200: `{ status, service, timestamp, pointsReward }`

---

## 데이터 구조(간단 타입)
- MarkerResponseDTO
  - `id:number`, `lat:number`, `lng:number`, `description:string`, `status:"ACTIVE"|"CLEANED"|"REMOVED"`, `createdAt:string`, `photos:{id:number,type:"BEFORE"|"AFTER",imagePath:string}[]`
- PhotoResponseDTO
  - `id:number`, `markerId:number`, `userId:number`, `imagePath:string`, `type:"BEFORE"|"AFTER"`, `verificationStatus:"PENDING"|"APPROVED"|"REJECTED"`, `gptResponse?:string`, `createdAt:string`
- GptVerificationResponseDTO
  - `success:boolean`, `verificationResult:"APPROVED"|"REJECTED"`, `gptResponse:string`, `confidence:number`, `errorMessage?:string`, `verifiedAt:string`

## 업로드 제약(요점)
- 확장자: `jpg|jpeg|png`, 크기: `100KB~10MB`, 해상도: `640x480~4096x4096`
- AFTER 중복 업로드 제한(같은 마커 동일 타입 재업로드 불가)
- 정적 제공: `/images/**`
