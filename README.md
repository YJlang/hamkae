# 🗑️ 함께줍줍 (Hamkae) - 안양시 쓰레기 문제 해결을 위한 시민 참여형 플랫폼

## 📋 프로젝트 개요

**함께줍줍**은 안양시의 쓰레기 문제를 해결하기 위한 시민 참여형 플랫폼입니다. 시민들이 쓰레기를 발견하면 제보하고, 청소 후 인증 사진을 업로드하면 AI가 검증하여 포인트를 지급하는 시스템입니다.

### 🎯 주요 기능
- **쓰레기 제보**: GPS 기반 위치 마커 등록 (다중 사진 업로드 지원)
- **청소 인증**: 청소 완료 사진 업로드 및 자동 타입 분류 (BEFORE/AFTER)
- **AI 검증**: GPT-4o를 통한 청소 전후 사진 비교 검증 (신뢰도 기반 보너스)
- **포인트 시스템**: 검증 완료 시 자동 포인트 적립 (100pt + 보너스 최대 20pt)
- **상품권 교환**: 포인트로 상품권 즉시 교환 및 핀번호 자동 발급
- **사용자 프로필**: 활동 통계, 포인트 현황, 교환 이력 관리
- **시민 참여**: 안양시민들의 환경 보호 활동 장려 및 보상

---

## 🏗️ 기술 스택

### Backend
- **Framework**: Spring Boot 3.5.4
- **Language**: Java 21
- **Build Tool**: Gradle 8.0+
- **Database**: MySQL 8.0
- **ORM**: Spring Data JPA (Hibernate 6.0+)
- **Security**: JWT + BCrypt (Spring Security 미사용)
- **AI Integration**: OpenAI GPT-4o API
- **File Upload**: Multipart file handling with local storage

### Frontend
- **Framework**: React 19.1.1
- **Build Tool**: Vite 7.1.0
- **Styling**: Tailwind CSS 3.4+
- **State Management**: React Context API
- **HTTP Client**: Axios
- **Routing**: React Router DOM
- **UI Components**: Custom components with Tailwind CSS
- **Mobile Optimization**: Responsive design for mobile devices

### AI & External APIs
- **AI 검증**: OpenAI GPT-4o API (이미지 분석 전용)
- **지도 API**: Kakao Maps API (프론트엔드)
- **Image Processing**: Automatic image type classification

### Infrastructure
- **Web Server**: Nginx (Reverse Proxy + SSL)
- **SSL Certificate**: Let's Encrypt (Certbot)
- **Deployment**: Amazon Linux 2023 VM
- **File Storage**: Local file system with organized directory structure

---

## 📊 데이터베이스 설계

### ERD 다이어그램
- **파일**: `together_erd.dbml`
- **도구**: dbdiagram.io에서 확인 가능
- **최종 업데이트**: 2025-08-22

### 주요 테이블 구조

#### 1. **users** - 사용자 정보 및 포인트 관리
```sql
- id: bigint (Primary Key, Auto Increment)
- name: varchar(100) - 사용자 이름
- username: varchar(100) - 로그인용 아이디 (Unique)
- password: varchar(255) - 암호화된 비밀번호 (BCrypt)
- points: integer - 보유 포인트 (기본값: 0)
- created_at: timestamp - 가입일시
- updated_at: timestamp - 수정일시
```

#### 2. **markers** - 쓰레기 위치 마커 정보
```sql
- id: bigint (Primary Key, Auto Increment)
- lat: decimal(10,8) - 위도
- lng: decimal(11,8) - 경도
- address: varchar(500) - 실제 주소 정보
- description: text - 쓰레기 위치 설명
- status: varchar(20) - 마커 상태 (active/cleaned/removed)
- reported_by: bigint - 제보자 ID (users.id 참조)
- created_at: timestamp - 등록일시
- updated_at: timestamp - 수정일시
```

#### 3. **photos** - 제보/인증 사진 관리
```sql
- id: bigint (Primary Key, Auto Increment)
- marker_id: bigint - 연결된 마커 ID
- user_id: bigint - 업로드한 사용자 ID
- filename: varchar(255) - 파일명
- file_path: varchar(500) - 이미지 파일 경로
- type: varchar(20) - 사진 타입 (BEFORE/AFTER)
- verification_status: varchar(20) - 검증 상태
- gpt_response: text - GPT API 응답 결과
- created_at: timestamp - 업로드일시
- updated_at: timestamp - 수정일시
```

#### 4. **point_history** - 포인트 적립/사용 이력
```sql
- id: bigint (Primary Key, Auto Increment)
- user_id: bigint - 사용자 ID
- points: integer - 변동 포인트 (양수: 적립, 음수: 사용)
- type: varchar(20) - 포인트 타입 (EARNED/USED)
- description: text - 포인트 변동 사유
- related_photo_id: bigint - 관련 사진 ID
- created_at: timestamp - 변동일시
- updated_at: timestamp - 수정일시
```

#### 5. **rewards** - 상품권 교환 요청
```sql
- id: bigint (Primary Key, Auto Increment)
- user_id: bigint - 요청 사용자 ID
- points_used: integer - 사용한 포인트
- reward_type: varchar(50) - 상품권 타입
- status: varchar(20) - 처리 상태 (APPROVED)
- created_at: timestamp - 요청일시
- updated_at: timestamp - 수정일시
```

#### 6. **reward_pins** - 상품권 핀번호 관리
```sql
- id: bigint (Primary Key, Auto Increment)
- reward_id: bigint - 연결된 상품권 ID
- pin_number: varchar(16) - 16자리 핀번호 (Unique)
- is_used: boolean - 사용 여부
- used_at: timestamp - 사용일시
- expires_at: timestamp - 만료일시 (발급일 + 1년)
- created_at: timestamp - 발급일시
- updated_at: timestamp - 수정일시
```

### 관계 매핑
- **users ↔ markers**: 1:N (한 사용자가 여러 마커 제보)
- **users ↔ photos**: 1:N (한 사용자가 여러 사진 업로드)
- **markers ↔ photos**: 1:N (한 마커에 여러 사진 연결)
- **users ↔ point_history**: 1:N (한 사용자의 포인트 변동 내역)
- **photos ↔ point_history**: 1:N (한 사진으로 인한 포인트 적립)
- **users ↔ rewards**: 1:N (한 사용자의 상품권 교환 요청)
- **rewards ↔ reward_pins**: 1:1 (한 상품권당 하나의 핀번호)

---

## 🚀 개발 현황

### ✅ 완료된 기능

#### 1. 사용자 인증 시스템 (2025-08-13)
- **User 엔티티**: 이름, 아이디, 비밀번호, 포인트, 생성/수정일시
- **회원가입 API**: `POST /auth/register`
- **로그인 API**: `POST /auth/login` (JWT 토큰 발급)
- **비밀번호 암호화**: BCrypt 사용
- **JWT 토큰 관리**: 24시간 유효기간

#### 2. 마커 관리 시스템 (2025-08-13) ✨ **최신 업데이트!**
- **Marker 엔티티**: 위치(lat, lng), 주소, 설명, 상태, 제보자, 생성/수정일시
- **주소 필드 추가**: `address` 필드로 실제 주소 정보 저장
- **제보자 정보**: `reportedBy` 관계로 제보자 정보 관리
- **마커 등록 API**: `POST /markers` (다중 사진 업로드 지원)
- **마커 조회 API**: `GET /markers`, `GET /markers/{id}`
- **마커 삭제 API**: `DELETE /markers/{id}` (연관 사진 및 파일 완전 삭제)
- **상태 관리**: active, cleaned, removed 상태 변경

#### 3. 사진 관리 시스템 (2025-08-17) ✨ **대폭 업데이트!**
- **Photo 엔티티**: 파일명, 경로, 타입(BEFORE/AFTER), 검증상태, GPT응답
- **자동 타입 분류**: 
  - 마커 등록 시 사진 → 자동 `BEFORE` 타입
  - 청소 인증 사진 → 자동 `AFTER` 타입
- **이미지 품질 검증**: 해상도, 밝기, 파일 크기, 형식 검증
- **중복 업로드 방지**: 같은 마커에 같은 타입 사진 중복 업로드 차단
- **사진 업로드**: 로컬 경로 `uploads/images/`에 저장
- **파일 관리**: UUID 기반 고유 파일명, 날짜별 폴더 구조
- **다중 사진 지원**: 마커당 여러 장의 사진 업로드 가능

#### 4. AI 검증 시스템 (2025-08-17) ✨ **대폭 개선!**
- **GPT-4o 모델**: 이미지 분석 전용 모델로 업그레이드
- **다단계 검증**: 이미지 품질 → 위치 일치성 → 시간 간격 → AI 검증
- **구체적 판단 기준**: 인공 쓰레기 vs 자연 요소 명확 구분
- **정확한 프롬프트**: 객관적이고 측정 가능한 검증 기준
- **응답 파싱 개선**: 더 안정적인 JSON 파싱 로직

#### 5. 도메인 엔티티 및 DTO (2025-08-13) ✨ **최신 업데이트!**
- **핵심 엔티티**: User, Marker, Photo, PointHistory, Reward, RewardPin
- **DTO 클래스**: 모든 API 요청/응답을 위한 Data Transfer Object
- **MarkerResponseDTO**: 주소 및 제보자 정보 포함
- **관계 매핑**: 1:N 관계 설정 및 Cascade 설정

#### 6. 프로젝트 구조
- **패키지 구조**: controller, service, repository, domain, DTO, config
- **공통 응답 형식**: `ApiResponse<T>` 클래스로 일관된 API 응답
- **CORS 설정**: 프론트엔드 연동을 위한 CORS 설정
- **한글 주석**: 협업을 위한 상세한 한글 주석 추가

#### 7. 포인트 시스템 (2025-08-20) ✨ **완성!**
- **포인트 적립**: AI 검증 승인 시 자동 포인트 적립 (기본 100pt + 신뢰도 보너스 최대 20pt)
- **포인트 사용**: 상품권 교환 시 포인트 차감
- **이력 관리**: 모든 포인트 변동 내역 자동 기록
- **통계 제공**: 월별, 타입별 포인트 통계 조회

#### 8. 상품권 교환 시스템 (2025-08-20) ✨ **완성!**
- **즉시 교환**: 포인트로 상품권 즉시 교환 (승인 과정 없음)
- **핀번호 발급**: 16자리 고유 핀번호 자동 생성
- **유효기간**: 발급일로부터 1년간 유효
- **사용 이력**: 핀번호 사용 내역 추적

#### 9. 프론트엔드 UI/UX (2025-08-22) ✨ **대폭 개선!**
- **반응형 디자인**: 모바일 최적화된 UI
- **사용자명 표시**: 로그인된 사용자명 동적 표시
- **제보자 정보**: 마커별 제보자 정보 표시
- **주소 표시**: 위치 섹션에 실제 주소 표시 (코멘트와 분리)
- **마커 삭제**: 제보 내역에서 활성 마커 삭제 기능
- **1:1 문의 제거**: 불필요한 기능 제거로 UI 간소화

#### 10. 파일 업로드 시스템 (2025-08-22) ✨ **완벽 최적화!**
- **무제한 업로드**: 파일 크기 제한 완전 해제
- **모바일 최적화**: 모바일 카메라 사진 자유롭게 업로드
- **Nginx 최적화**: 클라이언트 요청 크기 제한 해제
- **Spring Boot 최적화**: multipart 설정 완벽 최적화

---

## 🔧 주요 오류 해결 과정

### 1. 파일 업로드 413 에러 해결 (2025-08-22)
**문제**: 모바일에서 사진 업로드 시 "파일이 너무 큽니다" 413 에러 발생
**원인**: 
- Spring Boot: `max-file-size=100MB` 설정
- Nginx: `client_max_body_size 50M` 설정
- 프론트엔드: 50MB 제한 하드코딩
**해결**:
- Spring Boot: `max-file-size=-1` (무제한)
- Nginx: `client_max_body_size 0` (무제한)
- 프론트엔드: 모든 파일 크기 제한 제거
- 결과: 모바일에서 어떤 크기의 사진이든 업로드 가능

### 2. 이미지 403/404 에러 해결 (2025-08-22)
**문제**: 업로드된 이미지 접근 시 403 Forbidden 또는 404 Not Found 에러
**원인**: 
- Nginx 사용자가 `/root` 디렉토리 접근 불가
- 이미지 경로가 하드코딩되어 있음
**해결**:
- 이미지 디렉토리를 `/var/www/hamkae/images/`로 이동
- Nginx 설정에서 `alias /var/www/hamkae/images/` 설정
- 백엔드 코드에서 환경 변수 기반 경로 설정
- 결과: 모든 이미지 정상 접근 가능

### 3. CORS 이중 설정 문제 해결 (2025-08-22)
**문제**: 프론트엔드와 백엔드에서 중복 CORS 설정으로 인한 오류
**원인**: 
- Spring Boot WebConfig에서 CORS 설정
- Nginx에서도 CORS 헤더 추가
**해결**:
- Spring Boot: CORS 설정을 백엔드 API에만 적용
- Nginx: 정적 파일과 이미지에만 CORS 헤더 적용
- 결과: CORS 오류 완전 해결

### 4. 데이터베이스 연결 문제 해결 (2025-08-22)
**문제**: Spring Boot 시작 시 "Failed to determine suitable jdbc url" 에러
**원인**: 
- 환경 변수가 제대로 전달되지 않음
- 데이터베이스 이름 불일치 (`hamkae` vs `hamkae_db`)
**해결**:
- 환경 변수 파일과 application.properties 일치
- JVM 인자로 환경 변수 명시적 전달
- DDL 모드를 `update`로 변경하여 스키마 자동 업데이트
- 결과: 백엔드 정상 시작

### 5. 프론트엔드 라우팅 문제 해결 (2025-08-22)
**문제**: "Mainpage is not defined" 에러로 페이지 접속 불가
**원인**: App.jsx에서 Mainpage import 누락
**해결**:
- Mainpage import 추가
- 프론트엔드 재빌드
- 결과: 모든 페이지 정상 접속 가능

### 6. 마커 삭제 API 연동 문제 해결 (2025-08-22)
**문제**: 제보 내역에서 마커 삭제 버튼 클릭 시 "markerAPI.delete is not a function" 에러
**원인**: markerAPI에 delete 메서드 누락
**해결**:
- markerAPI에 delete 메서드 추가
- 백엔드 API 엔드포인트와 정확히 매칭
- 결과: 마커 삭제 기능 정상 작동

### 7. API 엔드포인트 405 에러 해결 (2025-08-22)
**문제**: 상품권 교환 시 "Request failed with status code 405" 에러
**원인**: 프론트엔드에서 `/api/rewards/redeem` 호출, 백엔드에는 `/api/rewards` 존재
**해결**:
- 프론트엔드 API 호출 엔드포인트 수정
- `pointHistoryAPI.js`에서 올바른 엔드포인트 사용
- 결과: 상품권 교환 기능 정상 작동

---

## 🚀 배포 가이드

### 서버 환경 설정
```bash
# 환경 변수 설정
export DB_URL="jdbc:mysql://localhost:3306/hamkae_db?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8&allowPublicKeyRetrieval=true"
export DB_USERNAME="root"
export DB_PASSWORD="your_password"
export UPLOAD_DIR="/var/www/hamkae/images/"
export APP_BASE_URL="https://hamkae.sku-sku.com"
export OPENAI_API_KEY="your-openai-api-key"

# 백엔드 시작
nohup java \
  -Dspring.profiles.active=prod \
  -DDB_URL="${DB_URL}" \
  -DDB_USERNAME="${DB_USERNAME}" \
  -DDB_PASSWORD="${DB_PASSWORD}" \
  -DUPLOAD_DIR="${UPLOAD_DIR}" \
  -DOPENAI_API_KEY="${OPENAI_API_KEY}" \
  -jar hamkae.jar > hamkae.log 2>&1 &
```

### Nginx 설정
- **파일**: `nginx-hamkae-final.conf`
- **SSL**: Let's Encrypt 자동 인증서
- **파일 업로드**: 무제한 크기 허용
- **이미지 서빙**: `/var/www/hamkae/images/` 경로

### 프론트엔드 배포
```bash
# 빌드
npm run build

# 서버에 배포
scp -r dist/* root@your-server:/var/www/hamkae/
```

---

## 📱 사용자 가이드

### 모바일 최적화
- **카메라 사진**: 어떤 해상도든 자유롭게 업로드 가능
- **파일 크기**: 제한 없음 (모든 크기 지원)
- **HDR 모드**: 자유롭게 사용 가능
- **브라우저**: 최신 모바일 브라우저 권장

### 주요 기능 사용법
1. **쓰레기 제보**: 지도에서 위치 선택 → 사진 촬영 → 설명 입력
2. **청소 인증**: 제보된 마커 선택 → 청소 후 사진 업로드 → AI 검증 대기
3. **포인트 확인**: 마이페이지에서 포인트 현황 및 이력 확인
4. **상품권 교환**: 포인트 전환 페이지에서 상품권 교환

---

## 🔮 향후 개발 계획

### 단기 계획 (1-2주)
- [ ] 사용자 프로필 이미지 업로드 기능
- [ ] 푸시 알림 시스템 (청소 완료 알림)
- [ ] 통계 대시보드 개선

### 중기 계획 (1-2개월)
- [ ] 관리자 페이지 개발
- [ ] 월간/연간 리더보드 시스템
- [ ] 지역별 통계 및 분석

### 장기 계획 (3-6개월)
- [ ] 모바일 앱 개발 (React Native)
- [ ] AI 검증 정확도 향상
- [ ] 다른 도시로 확장

---

## 📞 문의 및 지원

- **개발자**: 윤준하
- **프로젝트**: 함께줍줍 (Hamkae)
- **버전**: 1.0.0
- **최종 업데이트**: 2025-08-22

---

## 📄 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다.

