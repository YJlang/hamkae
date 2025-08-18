# 🗑️ 함께줍줍 (Hamkae) - 안양시 쓰레기 문제 해결을 위한 시민 참여형 플랫폼

## 📋 프로젝트 개요

**함께줍줍**은 안양시의 쓰레기 문제를 해결하기 위한 시민 참여형 플랫폼입니다. 시민들이 쓰레기를 발견하면 제보하고, 청소 후 인증 사진을 업로드하면 AI가 검증하여 포인트를 지급하는 시스템입니다.

### 🎯 주요 기능
- **쓰레기 제보**: GPS 기반 위치 마커 등록 (사진 포함)
- **청소 인증**: 청소 완료 사진 업로드 및 자동 타입 분류
- **AI 검증**: GPT API를 통한 청소 전후 사진 비교 검증
- **포인트 시스템**: 검증 완료 시 포인트 적립 및 상품권 교환
- **시민 참여**: 안양시민들의 환경 보호 활동 장려

---

## 🏗️ 기술 스택

### Backend
- **Framework**: Spring Boot 3.5.4
- **Language**: Java 21
- **Build Tool**: Gradle
- **Database**: MySQL 8.0
- **ORM**: Spring Data JPA (Hibernate)
- **Security**: JWT + BCrypt (Spring Security 미사용)

### AI & External APIs
- **AI 검증**: OpenAI GPT API
- **지도 API**: (프론트엔드에서 처리)

---

## 📊 데이터베이스 설계

### ERD 다이어그램
- **파일**: `together_erd.dbml`
- **도구**: dbdiagram.io에서 확인 가능

### 주요 테이블
1. **users** - 사용자 정보 및 포인트 관리
2. **markers** - 쓰레기 위치 마커 정보
3. **photos** - 제보/인증 사진 관리 (BEFORE/AFTER 타입)
4. **point_history** - 포인트 적립/사용 이력
5. **rewards** - 상품권 교환 요청

---

## 🚀 개발 현황

### ✅ 완료된 기능

#### 1. 사용자 인증 시스템 (2025-08-13)
- **User 엔티티**: 이름, 아이디, 비밀번호, 포인트, 생성/수정일시
- **회원가입 API**: `POST /auth/register`
- **로그인 API**: `POST /auth/login` (JWT 토큰 발급)
- **비밀번호 암호화**: BCrypt 사용
- **JWT 토큰 관리**: 24시간 유효기간

#### 2. 마커 관리 시스템 (2025-08-13)
- **Marker 엔티티**: 위치(lat, lng), 설명, 상태, 제보자, 생성/수정일시
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

#### 5. 도메인 엔티티 및 DTO (2025-08-13)
- **핵심 엔티티**: User, Marker, Photo, PointHistory, Reward
- **DTO 클래스**: 모든 API 요청/응답을 위한 Data Transfer Object
- **관계 매핑**: 1:N 관계 설정 및 Cascade 설정

#### 5. 프로젝트 구조
- **패키지 구조**: controller, service, repository, domain, DTO, config
- **공통 응답 형식**: `ApiResponse<T>` 클래스로 일관된 API 응답
- **CORS 설정**: 프론트엔드 연동을 위한 CORS 설정
- **한글 주석**: 협업을 위한 상세한 한글 주석 추가

### 🔄 진행 중인 기능
- **AI 검증 시스템**: ✅ GPT API 연동 완료, ✅ 다단계 검증 구현 완료
- **포인트 시스템**: 적립/사용/이력 관리

### 📋 예정된 기능
- **상품권 교환**: 포인트를 상품권으로 교환
- **마이페이지**: 사용자 활동 내역 조회
- **사용자 프로필**: 프로필 수정 및 관리

---

## 📚 API 명세서

### 📖 상세 API 문서
- **파일**: `함께줍줍_API명세서.md`
- **포맷**: Markdown 형식
- **내용**: 모든 엔드포인트, 요청/응답 형식, 개발 우선순위

### 🔑 주요 API 엔드포인트

#### 사용자 인증
```
POST /auth/register - 회원가입
POST /auth/login   - 로그인
```

#### 마커 관리
```
POST   /markers     - 마커 등록 (다중 사진 업로드 지원)
GET    /markers     - 모든 마커 조회
GET    /markers/{id} - 특정 마커 조회
DELETE /markers/{id} - 마커 삭제 (연관 사진 및 파일 완전 삭제)
```

#### 사진 관리 ✨ **업데이트됨!**
```
POST /photos/upload/cleanup - 청소 인증용 사진 업로드 (자동 AFTER 타입)
GET  /photos/{id}           - 특정 사진 정보 조회
GET  /photos/marker/{markerId} - 마커별 사진 목록 조회
```

**📸 사진 타입 자동 분류**
- **마커 등록 시**: 사진들이 자동으로 `BEFORE` 타입으로 저장
- **청소 인증 시**: 사진이 자동으로 `AFTER` 타입으로 저장

---

## 🛠️ 개발 환경 설정

### 필수 요구사항
- **Java**: 21 이상
- **MySQL**: 8.0 이상
- **Gradle**: 8.0 이상

### 로컬 개발 환경 설정

#### 1. 데이터베이스 설정
```sql
CREATE DATABASE hamkae_db;
```

#### 2. 애플리케이션 설정
```properties
# application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/hamkae_db
spring.datasource.username=root
spring.datasource.password=wnsgk677400

# 파일 업로드 경로 (Windows 개발용)
app.upload.dir=C:/together/hamkae/uploads/images/
```

#### 3. 애플리케이션 실행
```bash
# Gradle을 통한 실행
./gradlew bootRun

# 또는 IDE에서 HamkaeApplication.java 실행
```

### 개발 서버 정보
- **포트**: 8080
- **URL**: http://localhost:8080
- **API 문서**: http://localhost:8080 (Postman 테스트 권장)

---

## 🧪 테스트 가이드

### Postman 테스트

#### 회원가입 테스트
```http
POST http://localhost:8080/auth/register
Content-Type: application/json

{
  "name": "홍길동",
  "username": "hong123",
  "password": "password123"
}
```

#### 로그인 테스트
```http
POST http://localhost:8080/auth/login
Content-Type: application/json

{
  "username": "hong123",
  "password": "password123"
}
```

#### 마커 등록 테스트 (사진 포함)
```http
POST http://localhost:8080/markers
Authorization: Bearer {로그인에서_받은_토큰}
Content-Type: multipart/form-data

Body (form-data):
- latitude: 37.5665
- longitude: 126.9780
- description: "테스트 마커입니다"
- images: [이미지파일1.jpg] (파일 선택)
- images: [이미지파일2.jpg] (파일 선택)
```

#### 청소 인증 사진 업로드 테스트
```http
POST http://localhost:8080/photos/upload/cleanup
Authorization: Bearer {로그인에서_받은_토큰}
Content-Type: multipart/form-data

Body (form-data):
- marker_id: 1 (마커 등록에서 받은 ID)
- images: [청소완료사진.jpg] (파일 선택)
```

### 예상 응답 형식
```json
{
  "success": true,
  "message": "로그인 성공",
  "data": {
    "token": "jwt_token_here",
    "user": {
      "id": 1,
      "name": "홍길동",
      "points": 0
    }
  }
}
```

---

## 📁 프로젝트 구조

```
hamkae/
├── src/
│   ├── main/
│   │   ├── java/com/example/hamkae/
│   │   │   ├── controller/     # API 엔드포인트
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── MarkerController.java
│   │   │   │   └── PhotoController.java
│   │   │   ├── service/        # 비즈니스 로직
│   │   │   │   ├── AuthService.java
│   │   │   │   ├── MarkerService.java
│   │   │   │   ├── PhotoService.java
│   │   │   │   └── FileUploadService.java
│   │   │   ├── repository/     # 데이터 접근 계층
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── MarkerRepository.java
│   │   │   │   └── PhotoRepository.java
│   │   │   ├── domain/         # 엔티티 클래스
│   │   │   │   ├── User.java
│   │   │   │   ├── Marker.java
│   │   │   │   └── Photo.java
│   │   │   ├── DTO/            # 데이터 전송 객체
│   │   │   └── config/         # 설정 클래스
│   │   └── resources/
│   │       ├── application.properties  # 데이터베이스 설정
│   │       └── templates/
│   └── test/                   # 테스트 코드
├── uploads/                    # 업로드된 이미지 저장소
│   └── images/                 # 날짜별 폴더 구조
├── build.gradle                # Gradle 의존성 설정
├── together_erd.dbml           # 데이터베이스 ERD
├── 함께줍줍_API명세서.md        # API 명세서
└── README.md                   # 프로젝트 문서
```

---

## 🚀 개발 우선순위

### 1순위 (MVP 핵심 기능) ✅ **완료!**
- ✅ 사용자 인증 (회원가입/로그인)
- ✅ 마커 등록/조회/삭제
- ✅ 사진 업로드 (다중 파일 지원)
- ✅ 자동 사진 타입 분류 (BEFORE/AFTER)
- 🔄 AI 검증

### 2순위 (사용자 경험)
- 🔄 포인트 조회/이력
- 📋 상품권 교환
- 📋 마이페이지

### 3순위 (편의 기능)
- 📋 프로필 수정
- 📋 상세 조회
- 📋 통계 및 분석

---

## 👥 개발팀

### 멋사 해커톤 3팀 - 함께줍줍
- **프로젝트**: 안양시 쓰레기 문제 해결을 위한 시민 참여형 플랫폼
- **목표**: 최소 기능 구현(MVP) + 안정적인 동작
- **기간**: 해커톤 기간

---

## 📝 개발 일지

### 2025-08-17 (오늘!) ✨ **AI 검증 시스템 대폭 개선!**
- ✅ **AI 검증 형평성 문제 해결**: GPT-4o 모델로 업그레이드
- ✅ **다단계 검증 시스템 구현**: 이미지 품질 → 위치 일치성 → 시간 간격 → AI 검증
- ✅ **이미지 품질 검증 서비스**: 해상도, 밝기, 파일 크기, 형식 검증
- ✅ **중복 업로드 방지**: 같은 마커에 같은 타입 사진 중복 업로드 차단
- ✅ **구체적 판단 기준**: 인공 쓰레기 vs 자연 요소 명확 구분
- ✅ **정확한 프롬프트 설계**: 객관적이고 측정 가능한 검증 기준
- ✅ **응답 파싱 개선**: 더 안정적인 JSON 파싱 로직

### 2025-08-13 ✨ **대폭 업데이트!**
- ✅ 마커 관리 시스템 완전 구현 (등록/조회/삭제)
- ✅ 사진 업로드 시스템 완전 구현 (다중 파일 지원)
- ✅ **자동 사진 타입 분류 시스템 구현** (BEFORE/AFTER)
- ✅ **청소 인증용 사진 업로드 API 구현** (`POST /photos/upload/cleanup`)
- ✅ **PhotoController 및 PhotoService 신규 생성**
- ✅ **PhotoRepository 신규 생성**
- ✅ 파일 업로드 서비스 구현 (로컬 저장, UUID 파일명, 날짜별 폴더)
- ✅ 도메인 엔티티 완전 구현 (User, Marker, Photo, PointHistory, Reward)
- ✅ 모든 DTO 클래스 구현 완료
- ✅ 마커 삭제 시 연관 사진 및 파일 완전 삭제 구현
- ✅ JPA Auditing 설정으로 자동 타임스탬프 관리
- ✅ CORS 설정으로 프론트엔드 연동 준비 완료
- ✅ 상세한 한글 주석으로 협업 환경 구축

### 2025-08-12
- ✅ 프로젝트 초기 설정 완료
- ✅ 사용자 인증 시스템 구현 완료
- ✅ JWT + BCrypt 보안 설정 완료
- ✅ 공통 응답 형식 클래스 구현 완료
- ✅ 상세한 한글 주석 추가 완료
- ✅ API 명세서 작성 완료
- ✅ ERD 설계 완료

---

## 🔗 관련 링크

- **GitHub 저장소**: [hackthon3-BE](https://github.com/YJlang/hackthon3-BE)
- **API 명세서**: `함께줍줍_API명세서.md`
- **데이터베이스 설계**: `together_erd.dbml`

---

## 📞 문의사항

프로젝트 관련 문의사항이나 개발 이슈가 있으시면 GitHub Issues를 통해 연락해주세요.

---

**함께 만들어가는 깨끗한 안양시! 🗑️✨**

