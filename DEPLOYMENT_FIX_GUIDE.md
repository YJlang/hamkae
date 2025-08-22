# 🚀 함께줍줍 (Hamkae) 배포 문제 해결 가이드

## **현재 문제 상황**
1. **403 Forbidden**: 이미지 파일 접근 불가
2. **Connection Refused**: 백엔드 서버 연결 실패
3. **데이터베이스 연결 오류**: 환경변수 문제

## **단계별 해결 방법**

### **1단계: 환경변수 파일 설정**

```bash
# 서버에서 실행
cd /root/hamkae

# server.env를 .env로 복사
cp server.env .env

# .env 파일 편집 (실제 값 입력)
vi .env
```

**.env 파일 내용 예시:**
```bash
# 데이터베이스 설정
DB_URL=jdbc:mysql://localhost:3306/hamkae_db?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8&allowPublicKeyRetrieval=true
DB_USERNAME=root
DB_PASSWORD=실제_MySQL_루트_비밀번호

# 파일 업로드 경로
UPLOAD_DIR=/root/hamkae/uploads/images/

# OpenAI GPT API 설정
OPENAI_API_KEY=실제_OpenAI_API_키
OPENAI_MODEL=gpt-4o
OPENAI_TIMEOUT=60000
OPENAI_MAX_TOKENS=1500

# AI 검증 설정
AI_VERIFICATION_ENABLED=true
AI_VERIFICATION_POINTS_REWARD=100
AI_VERIFICATION_MIN_INTERVAL_MINUTES=0

# 애플리케이션 기본 URL
APP_BASE_URL=https://hamkae.sku-sku.com
```

### **2단계: MySQL 서비스 확인 및 시작**

```bash
# MySQL 서비스 상태 확인
systemctl status mysqld

# MySQL이 실행되지 않았다면 시작
systemctl start mysqld

# MySQL 서비스 활성화 (재부팅 시 자동 시작)
systemctl enable mysqld

# MySQL 연결 테스트
mysql -uroot -p
```

### **3단계: 백엔드 실행**

```bash
# 실행 스크립트 권한 부여
chmod +x start-backend.sh

# 백엔드 시작
./start-backend.sh
```

**또는 수동으로 실행:**
```bash
# 기존 프로세스 종료
pkill -f hamkae.jar

# 환경변수 로드
source .env

# 백엔드 실행
nohup java \
  -Dspring.profiles.active=prod \
  -DDB_URL="${DB_URL}" \
  -DDB_USERNAME="${DB_USERNAME}" \
  -DDB_PASSWORD="${DB_PASSWORD}" \
  -DUPLOAD_DIR="${UPLOAD_DIR}" \
  -DOPENAI_API_KEY="${OPENAI_API_KEY}" \
  -jar hamkae.jar > hamkae.log 2>&1 &

# 로그 확인
tail -f hamkae.log
```

### **4단계: 이미지 디렉토리 권한 설정**

```bash
# 업로드 디렉토리 생성 및 권한 설정
mkdir -p /root/hamkae/uploads/images
chmod 755 /root/hamkae/uploads/images

# 기존 이미지 파일 권한 수정
find /root/hamkae/uploads/images -type f -exec chmod 644 {} \;
find /root/hamkae/uploads/images -type d -exec chmod 755 {} \;

# 소유자 확인
chown -R root:root /root/hamkae/uploads/images
```

### **5단계: Nginx 설정 업데이트**

```bash
# Nginx 설정 파일 복사
cp nginx-hamkae.conf /etc/nginx/conf.d/hamkae.conf

# Nginx 설정 테스트
nginx -t

# Nginx 재시작
systemctl restart nginx

# Nginx 상태 확인
systemctl status nginx
```

### **6단계: 방화벽 설정 확인**

```bash
# HTTP/HTTPS 포트 확인
firewall-cmd --list-ports

# 포트가 없다면 추가
firewall-cmd --permanent --add-port=80/tcp
firewall-cmd --permanent --add-port=443/tcp
firewall-cmd --reload
```

### **7단계: 서비스 상태 확인**

```bash
# 백엔드 프로세스 확인
ps aux | grep hamkae.jar

# 포트 사용 확인
netstat -tlnp | grep :8080

# 로그 확인
tail -f /root/hamkae/hamkae.log

# 이미지 접근 테스트
curl -I https://hamkae.sku-sku.com/images/2025/08/22/51dbae57-ced3-48a2-b97d-cc0ecdbc66f0.jpg
```

## **문제 해결 체크리스트**

- [ ] `.env` 파일에 실제 MySQL 비밀번호 입력
- [ ] `.env` 파일에 실제 OpenAI API 키 입력
- [ ] MySQL 서비스 실행 중
- [ ] 백엔드 애플리케이션 실행 중 (포트 8080)
- [ ] 이미지 디렉토리 권한 755
- [ ] Nginx 설정 업데이트 및 재시작
- [ ] 방화벽에서 80/443 포트 열림

## **로그 확인 명령어**

```bash
# 백엔드 로그
tail -f /root/hamkae/hamkae.log

# Nginx 에러 로그
tail -f /var/log/nginx/error.log

# Nginx 이미지 접근 로그
tail -f /var/log/nginx/images_access.log
tail -f /var/log/nginx/images_error.log
```

## **문제가 지속될 경우**

1. **이미지 403 에러**: SELinux 상태 확인
   ```bash
   sestatus
   # SELinux가 enabled라면 비활성화 고려
   ```

2. **백엔드 연결 실패**: 방화벽 및 보안 그룹 확인
   - AWS 보안 그룹에서 8080 포트 인바운드 규칙 확인

3. **데이터베이스 연결 실패**: MySQL 사용자 권한 확인
   ```bash
   mysql -uroot -p
   SHOW GRANTS FOR 'root'@'localhost';
   ```

## **성공 확인**

모든 단계가 완료되면:
1. 웹사이트 접속: `https://hamkae.sku-sku.com`
2. 이미지 표시 확인
3. 쓰레기 제보 및 청소 인증 테스트
4. 모바일에서도 정상 작동 확인
