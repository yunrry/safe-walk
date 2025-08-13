# safe-walk
한국관광데이터랩 공모전

## 🚶‍♂️ 관광지 보행자 위험구간 알림 서비스

> 관광지에서 보행자 안전을 위한 실시간 위험구간 정보 제공 서비스

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)
![Redis](https://img.shields.io/badge/Redis-7.0-red)
![Docker](https://img.shields.io/badge/Docker-latest-blue)
![AWS](https://img.shields.io/badge/AWS-EC2%2FRDS-orange)

---

## 📋 목차

- [프로젝트 소개](#-프로젝트-소개)
- [주요 기능](#-주요-기능)
- [기술 스택](#-기술-스택)
- [시스템 아키텍처](#-시스템-아키텍처)
- [설치 및 실행](#-설치-및-실행)
- [API 문서](#-api-문서)
- [환경 설정](#-환경-설정)
- [배포](#-배포)
- [기여하기](#-기여하기)

---

## 🎯 프로젝트 소개

관광지를 방문하는 보행자들의 안전을 위해 **한국 관광 데이터**와 **도로교통공단 오픈 API**를 활용하여 실시간 위험구간 정보를 제공하는 서비스입니다.

### 문제 인식
- 관광지 방문객들의 지역 교통상황에 대한 정보 부족
- 연휴기간 관광지 주변 교통사고 증가
- 보행자 사고다발지역에 대한 사전 정보 제공 필요

### 솔루션
- GPS 기반 실시간 위험구간 알림
- 관광철/연휴기간 특화 위험도 정보
- 연령대별 맞춤형 안전 가이드

---

## ✨ 주요 기능

### 🗺️ 위험구간 지도 서비스
- 보행자 사고다발지역 실시간 표시
- 위험도 4단계 등급 (안전/주의/위험/매우위험)
- 관광지 주변 500m 반경 위험구간 알림

### 📱 실시간 알림 시스템
- GPS 기반 위험구간 접근 알림
- 연휴기간/관광철 위험도 증가 알림
- 날씨/시간대별 동적 위험도 업데이트

### 👥 개인화 서비스
- 연령대별 맞춤 위험도 (고령자 특별 관리)
- 관광지 유형별 특화 정보
- 사용자 위치 기반 주변 위험구간 조회

### 📊 통계 및 분석
- 지역별 사고 통계 제공
- 법규위반/사고원인별 안전수칙
- 관광지별 사고 패턴 분석

---

## 🛠 기술 스택

### Backend
- **Framework**: Spring Boot 3.2.0
- **Language**: Java 21
- **Database**: MySQL 8.0
- **Cache**: Redis 7.0
- **Build Tool**: Gradle 8.0

### Infrastructure
- **Containerization**: Docker & Docker Compose
- **Cloud**: AWS (EC2, RDS, ElastiCache)
- **CI/CD**: GitHub Actions
- **Documentation**: Swagger UI

### External APIs
- 도로교통공단 보행자 사고다발지역정보 API
- 도로교통공단 연휴기간별 사고다발지역정보 API
- 도로교통공단 세부링크 도로위험지수정보 API

---

## 🏗 시스템 아키텍처

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Client App    │────│   Load Balancer │────│   Spring Boot   │
│   (Mobile/Web)  │    │     (ALB)       │    │   Application   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                                       │
                                              ┌────────┴────────┐
                                              │                 │
                                    ┌─────────────────┐ ┌─────────────────┐
                                    │   MySQL RDS     │ │  Redis Cache    │
                                    │   (Master/Slave)│ │  (ElastiCache)  │
                                    └─────────────────┘ └─────────────────┘
                                              │
                                    ┌─────────────────┐
                                    │  External APIs  │
                                    │ (도로교통공단)    │
                                    └─────────────────┘
```

---

## 🚀 설치 및 실행

### 사전 요구사항
- Java 21+
- Docker & Docker Compose
- 도로교통공단 OpenAPI 인증키

### 2. 환경 변수 설정
```bash
# .env 파일 생성
cp .env.example .env

# 필수 환경 변수 설정
KOROAD_API_KEY=your_api_key_here
MYSQL_ROOT_PASSWORD=your_password
```

### 3. Docker Compose로 실행
```bash
# 개발 환경 실행
docker-compose -f docker-compose.dev.yml up -d

# 프로덕션 환경 실행
docker-compose up -d
```

### 4. 로컬 개발 환경 실행
```bash
# 의존성 설치 및 빌드
./gradlew build

# 애플리케이션 실행
./gradlew bootRun
```

---

## 📚 API 문서

### Swagger UI
애플리케이션 실행 후 다음 URL에서 API 문서를 확인할 수 있습니다:
```
http://localhost:8080/swagger-ui/index.html
```

### 주요 API 엔드포인트

#### 위험구간 조회
```http
GET /api/v1/risk-areas/nearby
```
**파라미터**: `latitude`, `longitude`, `radius`

#### 실시간 위험도 조회
```http
GET /api/v1/risk-index/realtime
```
**파라미터**: `lineString`, `vehicleType`

#### 관광지별 통계
```http
GET /api/v1/statistics/tourist-spot/{spotId}
```

#### 사용자 알림 설정
```http
POST /api/v1/notifications/settings
PUT /api/v1/notifications/settings/{id}
```

---

## ⚙️ 환경 설정

### application.yml 설정 예시
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/tourism_safety
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:password}
  
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false

external:
  koroad:
    api-key: ${KOROAD_API_KEY}
    base-url: https://opendata.koroad.or.kr/data/rest

logging:
  level:
    com.tourism.safety: DEBUG
```

### Docker 환경 변수
```bash
# Database
DB_HOST=mysql
DB_PORT=3306
DB_NAME=tourism_safety
DB_USERNAME=root
DB_PASSWORD=your_secure_password

# Redis
REDIS_HOST=redis
REDIS_PORT=6379

# External API
KOROAD_API_KEY=your_api_key

# Application
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080
```

---

## 🚢 배포

### GitHub Actions CI/CD
`.github/workflows/deploy.yml`에 정의된 배포 파이프라인:

1. **테스트**: Unit/Integration 테스트 실행
2. **빌드**: Docker 이미지 빌드
3. **푸시**: AWS ECR에 이미지 푸시
4. **배포**: AWS EC2에 자동 배포

### AWS 인프라 구성



### 배포 명령어
```bash
# 프로덕션 배포
./scripts/deploy.sh

# 롤백
./scripts/rollback.sh

# 헬스체크
curl http://your-domain/actuator/health
```

---

## 🧪 테스트

### 테스트 실행
```bash
# 전체 테스트
./gradlew test

# 통합 테스트만
./gradlew integrationTest

# 커버리지 리포트 생성
./gradlew jacocoTestReport
```

### 테스트 환경 설정
```bash
# 테스트용 데이터베이스 설정
docker-compose -f docker-compose.test.yml up -d

# 테스트 데이터 로드
./gradlew flywayMigrate -Pflyway.configFiles=src/test/resources/flyway.conf
```

---

## 📊 모니터링

### 애플리케이션 메트릭
- **Actuator**: `/actuator/health`, `/actuator/metrics`
- **로그 수집**: 
- **성능 모니터링**: 

### 주요 지표
- API 응답시간
- 외부 API 호출 성공률
- Redis 캐시 히트율
- 데이터베이스 커넥션 풀 상태

---

## 🤝 기여하기

### 개발 프로세스
1. **Issue 생성**: 기능 요청이나 버그 리포트
2. **Fork & Branch**: `feature/기능명` 또는 `fix/버그명`
3. **개발**: 코딩 컨벤션 및 테스트 작성
4. **Pull Request**: 코드 리뷰 요청

### 코딩 컨벤션
- **Java**: Google Java Style Guide
- **Commit Message**: Conventional Commits
- **Branch Naming**: `feature/`, `fix/`, `hotfix/`

### 이슈 템플릿
```markdown
## 기능 설명
간단한 기능 설명

## 상세 요구사항
- [ ] 요구사항 1
- [ ] 요구사항 2

## 기대 효과
이 기능으로 인한 기대 효과
```

---

## 📝 라이센스

이 프로젝트는 MIT 라이센스 하에 배포됩니다. 자세한 내용은 [LICENSE](LICENSE) 파일을 참조하세요.

---

## 📞 연락처

- **개발팀**: 
- **이슈 리포트**:
- **문서**: 

---

## 🔗 관련 링크

- [도로교통공단 OpenAPI](https://opendata.koroad.or.kr/)
- [Spring Boot 공식 문서](https://spring.io/projects/spring-boot)

---
