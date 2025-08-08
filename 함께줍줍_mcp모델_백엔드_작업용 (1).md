# 함께줍줍 MCP 모델 백엔드 작업용 (해커톤 간소화 버전)

## 1. 프로젝트 개요

- **목표**: 안양시 쓰레기 문제 해결을 위한 시민 참여형 플랫폼 백엔드 개발
- **우선순위**: 최소 기능 구현(MVP) + 안정적인 동작
- **프레임워크**: Spring Boot (Spring Security 미사용)
- **DB**: MySQL
- **AI 처리**: GPT API를 이용해 사진 비교 (쓰레기 전/후 사진 검증)

---

## 2. 주요 기능

1. **사용자 제출 사진 처리**
   - 업로드된 사진 저장 (로컬 또는 S3 등)
   - GPT API 호출로 기존 사진과 비교하여 청소 여부 확인
2. **마커 관리**
   - 쓰레기 위치 마커 생성/조회/삭제
   - 지도 API와 연동 (좌표 기반)
3. **포인트 시스템**
   - 인증 통과 시 포인트 지급
   - 포인트 조회 및 차감/사용 처리
4. **상품권 교환**
   - 포인트 → 상품권 요청 저장 (실제 발급은 외부 처리)

---

## 3. API 설계 (간단)

### 3.1 사진 업로드 & 검증

- **POST /photos/upload**
  - 요청: 이미지 파일 + 마커 ID
  - 처리: 저장 → GPT API 비교 → 결과 반환

### 3.2 마커 관리

- **POST /markers** (새 마커 등록)
- **GET /markers** (모든 마커 조회)
- **DELETE /markers/{id}** (마커 삭제)

### 3.3 포인트

- **GET /points/{userId}**
- **POST /points/add**
- **POST /points/use**

---

## 4. DB 구조 (간단)

```
users (id, name, email, points)
markers (id, lat, lng, description, status)
photos (id, marker_id, user_id, image_path, type, created_at)
rewards (id, user_id, points_used, reward_type, status)
```

---

## 5. GPT API 사진 비교 흐름

1. 사진 업로드 시 기존 마커의 전/후 사진 불러오기
2. GPT API에 두 사진 전달 (프롬프트로 변화 감지 요청)
3. 결과가 "청소됨"이면 포인트 지급
4. DB에 결과 저장

---

## 6. 개발 우선순위 Step-by-Step

1. MySQL 연동 및 기본 Entity/Repository 생성
2. 사진 업로드 API 구현 (파일 저장)
3. GPT API 연동하여 비교 기능 구현
4. 마커 등록/조회 API 구현
5. 포인트 로직 추가
6. 최소한의 에러 처리 및 응답 형식 통일

---

## 7. 테스트 방법

- Postman으로 API 호출 테스트
- 로컬 MySQL로 데이터 확인
- GPT API 응답 Mocking으로 속도 확보

