# Accident Statistics DB Importer

교통사고 통계 CSV 데이터를 MySQL 데이터베이스에 저장하는 Python 스크립트입니다.

## 🚀 사용 방법

### 1. 환경 설정

#### 패키지 설치
```bash
pip install -r requirements.txt
```

#### .env 파일 설정
프로젝트 루트에 있는 `.env` 파일에서 MySQL 연결 정보를 설정하세요:

```env
DB_HOST=localhost
DB_PORT=3306
DB_NAME=safe_walk
DB_USER=root
DB_PASSWORD=your_password
DB_CHARSET=utf8mb4
```

### 2. CSV 파일 준비

`csv/AccidentStatistics.csv` 파일을 준비하세요. 스크립트는 다음과 같은 한글 컬럼명을 자동으로 영어로 변환합니다:

| 한글 컬럼명 | 영어 컬럼명 | 타입 |
|------------|------------|------|
| 사고발생일 | accident_date | DATE |
| 사고발생시간 | accident_time | TIME |
| 요일 | day_of_week | VARCHAR(10) |
| 시도명 | sido_name | VARCHAR(50) |
| 시군구명 | sigungu_name | VARCHAR(50) |
| 사고유형 | accident_type | VARCHAR(100) |
| 기상상태 | weather_condition | VARCHAR(50) |
| 도로형태 | road_type | VARCHAR(100) |
| 노면상태 | road_surface | VARCHAR(50) |
| 사망자수 | death_count | INT |
| 중상자수 | serious_injury_count | INT |
| 경상자수 | minor_injury_count | INT |
| 부상신고자수 | injury_report_count | INT |
| 차량수 | vehicle_count | INT |
| 연령층 | age_group | VARCHAR(20) |
| 성별 | gender | VARCHAR(10) |
| 위도 | location_lat | DECIMAL(10,8) |
| 경도 | location_lng | DECIMAL(11,8) |

### 3. 스크립트 실행

```bash
cd python
python accident_statics_to_db.py
```

## 📊 생성되는 테이블 구조

```sql
CREATE TABLE accident_statics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    accident_date DATE NOT NULL COMMENT '사고발생일',
    accident_time TIME COMMENT '사고발생시간',
    day_of_week VARCHAR(10) COMMENT '요일',
    sido_name VARCHAR(50) COMMENT '시도명',
    sigungu_name VARCHAR(50) COMMENT '시군구명',
    accident_type VARCHAR(100) COMMENT '사고유형',
    weather_condition VARCHAR(50) COMMENT '기상상태',
    road_type VARCHAR(100) COMMENT '도로형태',
    road_surface VARCHAR(50) COMMENT '노면상태',
    death_count INT DEFAULT 0 COMMENT '사망자수',
    serious_injury_count INT DEFAULT 0 COMMENT '중상자수',
    minor_injury_count INT DEFAULT 0 COMMENT '경상자수',
    injury_report_count INT DEFAULT 0 COMMENT '부상신고자수',
    total_injury_count INT DEFAULT 0 COMMENT '총 부상자수',
    vehicle_count INT DEFAULT 0 COMMENT '차량수',
    age_group VARCHAR(20) COMMENT '연령층',
    gender VARCHAR(10) COMMENT '성별',
    location_lat DECIMAL(10, 8) COMMENT '위도',
    location_lng DECIMAL(11, 8) COMMENT '경도',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_accident_date (accident_date),
    INDEX idx_sido_sigungu (sido_name, sigungu_name),
    INDEX idx_accident_type (accident_type)
);
```

## 🔧 주요 기능

- **자동 테이블 생성**: `accident_statics` 테이블이 없으면 자동으로 생성
- **다중 인코딩 지원**: UTF-8, CP949, EUC-KR 등 자동 감지
- **컬럼명 자동 매핑**: 한글 컬럼명을 영어로 자동 변환
- **데이터 타입 변환**: 날짜, 시간, 숫자 타입 자동 변환
- **에러 처리**: 상세한 에러 메시지와 진행 상황 표시
- **배치 처리**: 대용량 데이터 효율적 처리 (1000건씩)

## ⚠️ 주의사항

1. MySQL 서버가 실행 중이어야 합니다.
2. `safe_walk` 데이터베이스가 미리 생성되어 있어야 합니다.
3. 사용자에게 테이블 생성 및 데이터 삽입 권한이 있어야 합니다.
4. CSV 파일의 인코딩이 UTF-8, CP949, EUC-KR 중 하나여야 합니다.

## 🐛 문제 해결

### 연결 오류
```
❌ 테이블 생성 중 오류 발생: Access denied for user
```
- DB 사용자 권한 확인
- .env 파일의 DB 정보 확인

### 인코딩 오류
```
❌ CSV 파일 로딩 실패: 지원하는 인코딩으로 파일을 읽을 수 없습니다
```
- CSV 파일을 UTF-8로 저장 후 재시도

### 파일 없음 오류
```
❌ CSV 파일을 찾을 수 없습니다
```
- `csv/AccidentStatistics.csv` 파일 존재 확인
- 파일명 대소문자 확인