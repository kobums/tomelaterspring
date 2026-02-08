# Backend (To Me, Later)

`To Me, Later`의 백엔드 서버입니다. Spring Boot와 Kotlin으로 구축되었습니다.

## 🛠 설치 및 실행 (Setup & Run)

### 사전 요구사항
- JDK 17 이상
- Docker (MySQL 실행용)

### 1. 환경 변수 설정
`.env` 파일이 `src/main/resources` 또는 프로젝트 루트에 있어야 합니다. (보안상 저장소에는 포함되지 않음)
필요한 주요 환경 변수는 다음과 같습니다:
- `DB_URL`, `DB_USER`, `DB_PASSWORD`
- `MAIL_USERNAME`, `MAIL_PASSWORD` (Google SMTP)
- `JWT_SECRET`

### 2. 데이터베이스 실행
프로젝트 루트의 `docker-compose.yml`을 사용하여 MySQL 컨테이너를 실행합니다.
```bash
# 프로젝트 루트 디렉토리에서 실행
docker-compose up -d
```

### 3. 서버 실행
`tomelater` 디렉토리로 이동하여 Gradle 래퍼를 사용해 실행합니다.

**Mac/Linux:**
```bash
cd tomelater
./gradlew bootRun
```

**Windows:**
```bash
cd tomelater
.\gradlew.bat bootRun
```

서버는 기본적으로 `8080` 포트에서 실행됩니다.

## 📁 디렉토리 구조
```
tomelater/
├── src/main/kotlin/com/gowoobro/tomelater/
│   ├── config/      # 설정 클래스 (Security, Cors 등)
│   ├── controller/  # API 컨트롤러
│   ├── entity/      # JPA 엔티티
│   ├── repository/  # Repository 인터페이스
│   ├── scheduler/   # 스케줄러 (MailScheduler)
│   └── service/     # 비즈니스 로직
└── src/main/resources/
     └── application.yml # 애플리케이션 설정
```

## 🧪 테스트 실행
```bash
./gradlew test
```
