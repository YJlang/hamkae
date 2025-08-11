# 🗑️ 함께줍줍 (Hamkae) - 안양시 쓰레기 문제 해결을 위한 시민 참여형 플랫폼

## 📋 프로젝트 개요

**함께줍줍**은 안양시의 쓰레기 문제를 해결하기 위한 시민 참여형 플랫폼입니다. 시민들이 쓰레기를 발견하면 제보하고, 청소 후 인증 사진을 업로드하면 AI가 검증하여 포인트를 지급하는 시스템입니다.

### 🎯 주요 기능
- **쓰레기 제보**: GPS 기반 위치 마커 등록
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
3. **photos** - 제보/인증 사진 관리
4. **point_history** - 포인트 적립/사용 이력
5. **rewards** - 상품권 교환 요청

---

## 🚀 개발 현황

### ✅ 완료된 기능

#### 1. 사용자 인증 시스템 (2024-12-19)
- **User 엔티티**: 이름, 아이디, 비밀번호, 포인트, 생성/수정일시
- **회원가입 API**: `POST /auth/register`
- **로그인 API**: `POST /auth/login` (JWT 토큰 발급)
- **비밀번호 암호화**: BCrypt 사용
- **JWT 토큰 관리**: 24시간 유효기간

#### 2. 프로젝트 구조
- **패키지 구조**: controller, service, repository, domain, DTO, config
- **공통 응답 형식**: `ApiResponse<T>` 클래스로 일관된 API 응답
- **CORS 설정**: 프론트엔드 연동을 위한 CORS 설정
- **한글 주석**: 협업을 위한 상세한 한글 주석 추가

### 🔄 진행 중인 기능
- **마커 관리 시스템**: 쓰레기 위치 등록/조회/삭제
- **사진 업로드 시스템**: 제보용/인증용 사진 처리
- **AI 검증 시스템**: GPT API 연동 및 사진 비교

### 📋 예정된 기능
- **포인트 시스템**: 적립/사용/이력 관리
- **상품권 교환**: 포인트를 상품권으로 교환
- **마이페이지**: 사용자 활동 내역 조회

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

#### 마커 관리 (예정)
```
POST   /markers     - 마커 등록
GET    /markers     - 모든 마커 조회
GET    /markers/{id} - 특정 마커 조회
DELETE /markers/{id} - 마커 삭제
```

#### 사진 관리 (예정)
```
POST /photos/upload - 사진 업로드
POST /photos/{id}/verify - AI 검증 요청
```

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
│   │   │   ├── service/        # 비즈니스 로직
│   │   │   ├── repository/     # 데이터 접근 계층
│   │   │   ├── domain/         # 엔티티 클래스
│   │   │   ├── DTO/            # 데이터 전송 객체
│   │   │   └── config/         # 설정 클래스
│   │   └── resources/
│   │       ├── application.properties  # 데이터베이스 설정
│   │       └── templates/
│   └── test/                   # 테스트 코드
├── build.gradle                # Gradle 의존성 설정
├── together_erd.dbml           # 데이터베이스 ERD
├── 함께줍줍_API명세서.md        # API 명세서
└── README.md                   # 프로젝트 문서
```

---

## 🚀 개발 우선순위

### 1순위 (MVP 핵심 기능)
- ✅ 사용자 인증 (회원가입/로그인)
- 🔄 마커 등록/조회
- 🔄 사진 업로드
- 🔄 AI 검증

### 2순위 (사용자 경험)
- 📋 포인트 조회/이력
- 📋 상품권 교환
- 📋 마이페이지

### 3순위 (편의 기능)
- 📋 프로필 수정
- 📋 마커 삭제
- 📋 상세 조회

---

## 👥 개발팀

### 멋사 해커톤 3팀 - 함께줍줍
- **프로젝트**: 안양시 쓰레기 문제 해결을 위한 시민 참여형 플랫폼
- **목표**: 최소 기능 구현(MVP) + 안정적인 동작
- **기간**: 해커톤 기간

---

## 📝 개발 일지

### 2024-12-19
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
