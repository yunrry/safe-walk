#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
행정 법정동 중심좌표 데이터를 MySQL 데이터베이스에 저장하는 스크립트
파일: 행정 법정동 중심좌표.csv
"""

import pandas as pd
import pymysql
from sqlalchemy import create_engine, text
import logging
from typing import Optional
import os
from dotenv import load_dotenv

# 환경변수 로드
load_dotenv()

# 로깅 설정
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

class LegalDongCoordinatesImporter:
    """행정 법정동 중심좌표 데이터 임포터"""
    
    def __init__(self):
        self.engine = self._create_engine()
        self.table_name = "legal_dong_coordinates"
        
    def _create_engine(self):
        """데이터베이스 엔진 생성"""
        db_config = {
            'host': os.getenv('DB_HOST', 'localhost'),
            'port': int(os.getenv('DB_PORT', 3306)),
            'user': os.getenv('DB_USER', 'root'),
            'password': os.getenv('DB_PASSWORD', 'password'),
            'database': os.getenv('DB_NAME', 'safewalk'),
            'charset': 'utf8mb4'
        }
        
        connection_string = (
            f"mysql+pymysql://{db_config['user']}:{db_config['password']}@"
            f"{db_config['host']}:{db_config['port']}/{db_config['database']}?"
            f"charset={db_config['charset']}"
        )
        
        return create_engine(connection_string, echo=False)
    
    def create_table(self):
        """테이블 생성"""
        create_table_sql = f"""
        CREATE TABLE IF NOT EXISTS {self.table_name} (
            id BIGINT AUTO_INCREMENT PRIMARY KEY,
            code VARCHAR(20) NOT NULL COMMENT '행정구역 코드',
            sido VARCHAR(50) NOT NULL COMMENT '시도명',
            sigungu VARCHAR(50) NOT NULL COMMENT '시군구명',
            eupmyeondong VARCHAR(50) NOT NULL COMMENT '읍면동명',
            sub_area VARCHAR(50) NULL COMMENT '하위 지역',
            latitude DECIMAL(10, 7) NOT NULL COMMENT '위도',
            longitude DECIMAL(10, 7) NOT NULL COMMENT '경도',
            code_type VARCHAR(10) NOT NULL COMMENT '코드 종류',
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
            INDEX idx_code (code),
            INDEX idx_sido_sigungu (sido, sigungu),
            INDEX idx_coordinates (latitude, longitude),
            UNIQUE KEY uk_code (code)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
        COMMENT='행정 법정동 중심좌표 정보';
        """
        
        try:
            with self.engine.connect() as connection:
                connection.execute(text(create_table_sql))
                connection.commit()
            logger.info(f"테이블 '{self.table_name}' 생성 완료")
        except Exception as e:
            logger.error(f"테이블 생성 실패: {e}")
            raise
    
    def load_csv_data(self, csv_file_path: str) -> pd.DataFrame:
        """CSV 파일 로드 및 전처리"""
        try:
            # CSV 파일 읽기 (한글 인코딩 처리)
            df = pd.read_csv(csv_file_path, encoding='utf-8-sig')
            
            # 컬럼명을 영어로 변경
            column_mapping = {
                '코드': 'code',
                '시도': 'sido',
                '시군구': 'sigungu', 
                '읍면동': 'eupmyeondong',
                '하위': 'sub_area',
                '위도': 'latitude',
                '경도': 'longitude',
                '코드종류': 'code_type'
            }
            
            df = df.rename(columns=column_mapping)
            
            # 데이터 타입 변환
            df['latitude'] = pd.to_numeric(df['latitude'], errors='coerce')
            df['longitude'] = pd.to_numeric(df['longitude'], errors='coerce')
            
            # 빈 문자열을 NULL로 변환
            df['sub_area'] = df['sub_area'].replace('', None)
            
            # 필수 컬럼 검증
            required_columns = ['code', 'sido', 'sigungu', 'eupmyeondong', 
                              'latitude', 'longitude', 'code_type']
            
            for col in required_columns:
                if col not in df.columns:
                    raise ValueError(f"필수 컬럼 '{col}'이 누락되었습니다.")
            
            # 결측값 제거
            df = df.dropna(subset=['code', 'latitude', 'longitude'])
            
            logger.info(f"CSV 데이터 로드 완료: {len(df)}행")
            return df
            
        except Exception as e:
            logger.error(f"CSV 파일 로드 실패: {e}")
            raise
    
    def insert_data(self, df: pd.DataFrame):
        """데이터베이스에 데이터 삽입"""
        try:
            # 기존 데이터 삭제 (전체 갱신)
            with self.engine.connect() as connection:
                connection.execute(text(f"DELETE FROM {self.table_name}"))
                connection.commit()
            
            logger.info("기존 데이터 삭제 완료")
            
            # 새 데이터 삽입
            df.to_sql(
                name=self.table_name,
                con=self.engine,
                if_exists='append',
                index=False,
                method='multi',
                chunksize=1000
            )
            
            logger.info(f"데이터 삽입 완료: {len(df)}행")
            
        except Exception as e:
            logger.error(f"데이터 삽입 실패: {e}")
            raise
    
    def validate_data(self) -> dict:
        """삽입된 데이터 검증"""
        try:
            with self.engine.connect() as connection:
                # 총 레코드 수
                total_count = connection.execute(
                    text(f"SELECT COUNT(*) as count FROM {self.table_name}")
                ).fetchone()
                
                # 시도별 통계
                sido_stats = connection.execute(
                    text(f"""
                    SELECT sido, COUNT(*) as count 
                    FROM {self.table_name} 
                    GROUP BY sido 
                    ORDER BY count DESC 
                    LIMIT 10
                    """)
                ).fetchall()
                
                # 코드 중복 검사
                duplicate_codes = connection.execute(
                    text(f"""
                    SELECT code, COUNT(*) as count 
                    FROM {self.table_name} 
                    GROUP BY code 
                    HAVING count > 1
                    """)
                ).fetchall()
                
                validation_result = {
                    'total_count': total_count[0] if total_count else 0,
                    'sido_stats': [dict(row._mapping) for row in sido_stats],
                    'duplicate_codes': [dict(row._mapping) for row in duplicate_codes]
                }
                
                logger.info(f"데이터 검증 완료 - 총 {validation_result['total_count']}건")
                
                if validation_result['duplicate_codes']:
                    logger.warning(f"중복 코드 발견: {len(validation_result['duplicate_codes'])}건")
                
                return validation_result
                
        except Exception as e:
            logger.error(f"데이터 검증 실패: {e}")
            raise

def main():
    """메인 실행 함수"""
    csv_file_path = "/workspace/csv/행정 법정동 중심좌표.csv"
    
    try:
        # CSV 파일 존재 여부 확인
        if not os.path.exists(csv_file_path):
            logger.error(f"CSV 파일을 찾을 수 없습니다: {csv_file_path}")
            return
        
        logger.info("행정 법정동 중심좌표 데이터 임포트 시작")
        
        # 임포터 인스턴스 생성
        importer = LegalDongCoordinatesImporter()
        
        # 1. 테이블 생성
        importer.create_table()
        
        # 2. CSV 데이터 로드
        df = importer.load_csv_data(csv_file_path)
        
        # 3. 데이터 삽입
        importer.insert_data(df)
        
        # 4. 데이터 검증
        validation_result = importer.validate_data()
        
        logger.info("=== 임포트 완료 ===")
        logger.info(f"총 레코드 수: {validation_result['total_count']:,}건")
        
        # 시도별 통계 출력
        logger.info("\n=== 시도별 통계 (상위 10개) ===")
        for stat in validation_result['sido_stats']:
            logger.info(f"{stat['sido']}: {stat['count']:,}건")
        
        if validation_result['duplicate_codes']:
            logger.warning(f"\n중복 코드가 {len(validation_result['duplicate_codes'])}건 발견되었습니다.")
        else:
            logger.info("\n중복 코드 없음 ✓")
            
    except Exception as e:
        logger.error(f"임포트 실패: {e}")
        raise

if __name__ == "__main__":
    main()