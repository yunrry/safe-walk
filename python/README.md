# Safe Walk 데이터 처리 스크립트

이 디렉토리에는 Safe Walk 프로젝트에서 사용하는 다양한 데이터를 처리하고 데이터베이스에 저장하는 Python 스크립트들이 포함되어 있습니다.

## 스크립트 목록

### 1. legal_dong_coordinates_to_db.py
**행정 법정동 중심좌표 데이터 임포터**

- **기능**: CSV 파일의 행정 법정동 중심좌표 데이터를 MySQL 데이터베이스에 저장
- **입력 파일**: `/workspace/csv/행정 법정동 중심좌표.csv`
- **테이블명**: `legal_dong_coordinates`

#### 테이블 스키마
```sql
CREATE TABLE legal_dong_coordinates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(20) NOT NULL COMMENT '행정구역 코드',
    sido VARCHAR(50) NOT NULL COMMENT '시도명',
    sigungu VARCHAR(50) NOT NULL COMMENT '시군구명',
    eupmyeondong VARCHAR(50) NOT NULL COMMENT '읍면동명',
    sub_area VARCHAR(50) NULL COMMENT '하위 지역',
    latitude DECIMAL(10, 7) NOT NULL COMMENT '위도',
    longitude DECIMAL(10, 7) NOT NULL COMMENT '경도',
    code_type VARCHAR(10) NOT NULL COMMENT '코드 종류',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_code (code)
);
```

#### 사용법
```bash
# 1. 의존성 설치
pip install -r requirements.txt

# 2. 환경변수 설정
cp .env.example .env
# .env 파일을 열어 데이터베이스 정보 수정

# 3. 스크립트 실행
python python/legal_dong_coordinates_to_db.py
```

### 2. 기존 스크립트들
- `accident_statics_to_db.py`: 사고 통계 데이터 처리
- `elderly_pedestrian_accident_to_db.py`: 고령 보행자 사고 데이터 처리
- `holiday_accident_to_db.py`: 공휴일 사고 데이터 처리
- `local_government_accident_to_db.py`: 지자체 사고 데이터 처리
- `pedestrian_accident_to_db.py`: 보행자 사고 데이터 처리
- `risk_area_to_db.py`: 위험 지역 데이터 처리
- `extract_all_legal_dongs.py`: 전체 법정동 추출
- `extract_legal_dong.py`: 개별 법정동 추출
- `legal_dong_utils.py`: 법정동 관련 유틸리티

## 환경 설정

### 필수 패키지
```txt
pandas==2.1.4
numpy==1.25.2
PyMySQL==1.1.0
SQLAlchemy==2.0.23
python-dotenv==1.0.0
```

### 데이터베이스 연결
`.env` 파일에 다음 정보를 설정해야 합니다:
- `DB_HOST`: 데이터베이스 호스트
- `DB_PORT`: 데이터베이스 포트 (기본값: 3306)
- `DB_USER`: 데이터베이스 사용자명
- `DB_PASSWORD`: 데이터베이스 비밀번호
- `DB_NAME`: 데이터베이스명 (기본값: safewalk)

## 주의사항

1. **데이터 백업**: 스크립트 실행 전 중요한 데이터는 반드시 백업하세요.
2. **권한 확인**: 데이터베이스에 테이블 생성 및 데이터 삽입 권한이 있는지 확인하세요.
3. **인코딩**: CSV 파일은 UTF-8 인코딩을 사용해야 합니다.
4. **메모리**: 대용량 파일 처리 시 충분한 메모리가 필요합니다.