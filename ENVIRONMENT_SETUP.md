# 🔧 함께줍줍 서버 환경변수 설정 가이드

## 📋 **서비스 파일 환경변수 설정**

VM의 서비스 파일에서 다음 환경변수들을 실제 값으로 변경해야 합니다.

### **1. 데이터베이스 설정**
```bash
# 서비스 파일 수정
nano /etc/systemd/system/hamkae.service

# 다음 줄들을 실제 값으로 변경:
Environment="DB_PASSWORD=실제_데이터베이스_비밀번호_입력"
```

### **2. OpenAI API 키 설정**
```bash
# OpenAI API 키를 실제 값으로 변경:
Environment="OPENAI_API_KEY=실제_OpenAI_API_키_입력"
```

## 🚀 **업데이트된 서비스 파일 적용**

### **1. 서비스 파일 복사**
```bash
# 로컬에서 업데이트된 서비스 파일을 서버로 전송
scp -i hackathon_server48.pem hamkae.service ec2-user@43.202.43.20:/tmp/

# 서버에서 파일 이동
sudo mv /tmp/hamkae.service /etc/systemd/system/hamkae.service
```

### **2. 서비스 재시작**
```bash
# 서비스 재로드 및 재시작
sudo systemctl daemon-reload
sudo systemctl restart hamkae

# 상태 확인
sudo systemctl status hamkae
```

## 🔍 **환경변수 확인**

### **현재 설정된 환경변수 확인**
```bash
# 서비스 파일 내용 확인
sudo cat /etc/systemd/system/hamkae.service

# 환경변수 적용 확인
sudo systemctl show hamkae --property=Environment
```

### **로그에서 환경변수 확인**
```bash
# 애플리케이션 로그 확인
sudo journalctl -u hamkae -f

# 로그 파일 확인
sudo tail -f /root/hamkae/logs/hamkae.log
```

## ⚠️ **주의사항**

1. **데이터베이스 비밀번호**: 실제 MySQL root 비밀번호로 변경
2. **OpenAI API 키**: 유효한 API 키로 변경
3. **파일 경로**: `/root/hamkae/uploads/images/` 경로가 존재하는지 확인
4. **권한**: 서비스 파일과 디렉토리 권한 확인

## 🎯 **설정 완료 후 확인**

```bash
# 서비스 상태 확인
sudo systemctl status hamkae

# 포트 사용 확인
sudo netstat -tlnp | grep :8080

# 웹 접속 테스트
curl -k https://hamkae.sku-sku.com/api/
```

---

**환경변수 설정 완료 후 서비스가 정상적으로 시작되어야 합니다! 🚀**
