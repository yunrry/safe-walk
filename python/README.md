# 데이터 임포트 스크립트 모음

## 1. 행정 법정동 중심좌표 데이터 임포트

### 개요
`행정 법정동 중심좌표.csv` 파일의 데이터를 MySQL 데이터베이스에 저장하는 Python 스크립트입니다.

### 기능
- CSV 파일에서 행정 법정동 중심좌표 데이터 로드
- MySQL 데이터베이스 테이블 자동 생성
- 데이터 전처리 및 타입 변환
- 배치 처리로 대용량 데이터 효율적 저장
- 시도별 데이터 통계 제공

### 테이블 구조
```sql
CREATE TABLE administrative_legal_dongs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(20) COMMENT '법정동 코드',
    sido VARCHAR(50) COMMENT '시도명',
    sigungu VARCHAR(50) COMMENT '시군구명',
    eup_myeon_dong VARCHAR(50) COMMENT '읍면동명',
    sub_level VARCHAR(50) COMMENT '하위 행정구역',
    latitude DECIMAL(12,9) COMMENT '위도',
    longitude DECIMAL(12,9) COMMENT '경도',
    code_type VARCHAR(10) COMMENT '코드 종류',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

## 2. 인기관광지 데이터 임포트

### 개요
`세대별 인기관광지(전체).csv`와 `세대별 핫플레이스(전체).csv` 파일의 데이터를 병합하여 MySQL 데이터베이스에 저장하는 Python 스크립트입니다.

### 기능
- 두 개의 CSV 파일에서 인기관광지 데이터 로드
- 데이터 병합 및 전처리
- 경상북도 경주시 지역 정보 자동 매핑
- 출처 파일별 데이터 구분
- MySQL 데이터베이스 테이블 자동 생성

### 테이블 구조
```sql
CREATE TABLE popular_tourist_spots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sido_name VARCHAR(50) COMMENT '광역시/도명',
    sigungu_name VARCHAR(50) COMMENT '시/군/구명',
    spot_name VARCHAR(200) COMMENT '인기관광지명',
    tourist_spot_id VARCHAR(100) COMMENT '관광지ID',
    category VARCHAR(100) COMMENT '구분',
    age_group VARCHAR(20) COMMENT '연령대',
    ratio DECIMAL(5,2) COMMENT '비율',
    base_year_month VARCHAR(10) COMMENT '기준년월',
    growth_rate DECIMAL(5,2) COMMENT '성장율',
    source_file VARCHAR(100) COMMENT '출처파일',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

## 3. 방문자 급등동네(내국인) 데이터 임포트

### 개요
`방문자 급등동네(내국인).csv` 파일의 데이터를 MySQL 데이터베이스에 저장하는 Python 스크립트입니다.

### 기능
- CSV 파일에서 방문자 급등동네 데이터 로드
- 관광객수, 증감율 등 통계 데이터 처리
- MySQL 데이터베이스 테이블 자동 생성
- 증감율 상위 동네 자동 분석

### 테이블 구조
```sql
CREATE TABLE visitor_boom_neighborhoods (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ranking INT COMMENT '순위',
    sido_name VARCHAR(50) COMMENT '시도명',
    sigungu_name VARCHAR(50) COMMENT '시군구명',
    administrative_dong VARCHAR(50) COMMENT '행정동명',
    visitor_count DECIMAL(15,2) COMMENT '관광객수',
    last_year_visitor_count DECIMAL(15,2) COMMENT '전년동기관광객수',
    growth_rate DECIMAL(5,2) COMMENT '증감율',
    base_year_month VARCHAR(10) COMMENT '기준년월',
    search_date VARCHAR(20) COMMENT '조회일자',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

## 기능
- CSV 파일에서 행정 법정동 중심좌표 데이터 로드
- MySQL 데이터베이스 테이블 자동 생성
- 데이터 전처리 및 타입 변환
- 배치 처리로 대용량 데이터 효율적 저장
- 시도별 데이터 통계 제공

## 테이블 구조
```sql
CREATE TABLE administrative_legal_dongs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(20) NOT NULL COMMENT '법정동 코드',
    sido VARCHAR(50) NOT NULL COMMENT '시도명',
    sigungu VARCHAR(50) NOT NULL COMMENT '시군구명',
    eup_myeon_dong VARCHAR(50) COMMENT '읍면동명',
    sub_level VARCHAR(50) COMMENT '하위 행정구역',
    latitude DECIMAL(12,9) COMMENT '위도',
    longitude DECIMAL(12,9) COMMENT '경도',
    code_type VARCHAR(10) COMMENT '코드 종류',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

## 설치 및 실행

### 1. 가상환경 활성화
```bash
# 가상환경 활성화
source venv/bin/activate  # macOS/Linux
# 또는
venv\Scripts\activate     # Windows
```

### 2. 필요한 패키지 설치
```bash
pip install -r requirements.txt
```

### 3. 환경변수 설정
`.env` 파일을 생성하고 데이터베이스 연결 정보를 설정:
```env
DB_HOST=localhost
DB_PORT=3306
DB_USERNAME=root
DB_PASSWORD=your_password
DB_NAME=safe_walk
DB_CHARSET=utf8mb4
```

### 4. 스크립트 실행

#### 행정 법정동 중심좌표 데이터 임포트
```bash
python administrative_legal_dong_to_db.py
```

#### 인기관광지 데이터 임포트
```bash
python popular_tourist_spots_to_db.py
```

#### 방문자 급등동네(내국인) 데이터 임포트
```bash
python visitor_boom_neighborhood_to_db.py
```

## CSV 파일 형식

### 행정 법정동 중심좌표
- 파일명: `행정 법정동 중심좌표.csv`
- 인코딩: UTF-8
- 컬럼: 코드, 시도, 시군구, 읍면동, 하위, 위도, 경도, 코드종류

### 인기관광지 데이터
- 파일명: `20250817000707_세대별 인기관광지(전체).csv`
- 파일명: `20250817000712_세대별 핫플레이스(전체).csv`
- 인코딩: UTF-8
- 컬럼: 순위, 관광지ID, 관심지점명, 구분, 연령대, 비율, 기준년월, 시도명, 시군구명, 성장율

### 방문자 급등동네(내국인) 데이터
- 파일명: `20250817000715_방문자 급등동네(내국인).csv`
- 인코딩: UTF-8
- 컬럼: 순위, 시도명, 시군구명, 행정동명, 관광객수, 전년동기관광객수, 증감율, 기준년월, 조회일자

## 주의사항
- 실행 전 데이터베이스 연결 정보 확인
- 기존 데이터가 있다면 모두 삭제 후 새로 저장
- CSV 파일이 `csv/` 디렉토리에 위치해야 함
- 인기관광지 데이터는 두 파일을 병합하여 저장
