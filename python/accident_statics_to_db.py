#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import os
import pandas as pd
import mysql.connector
from sqlalchemy import create_engine, text
from dotenv import load_dotenv
import logging

# 로깅 설정
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

class AccidentStatisticsImporter:
    def __init__(self):
        """초기화 및 환경변수 로드"""
        load_dotenv()
        
        # 데이터베이스 연결 설정
        self.db_config = {
            'host': os.getenv('DB_HOST', 'localhost'),
            'port': int(os.getenv('DB_PORT', 3306)),
            'user': os.getenv('DB_USERNAME', 'root'),
            'password': os.getenv('DB_PASSWORD', ''),
            'database': os.getenv('DB_NAME', 'safe_walk'),
            'charset': os.getenv('DB_CHARSET', 'utf8mb4')
        }
        
        # SQLAlchemy 엔진 생성
        connection_string = f"mysql+mysqlconnector://{self.db_config['user']}:{self.db_config['password']}@{self.db_config['host']}:{self.db_config['port']}/{self.db_config['database']}?charset={self.db_config['charset']}"
        self.engine = create_engine(connection_string)
        
        # CSV 컬럼 매핑 (실제 CSV 컬럼명 기준)
        self.column_mapping = {
            '연도': 'accident_year',
            '대상사고 구분명': 'accident_type_name',
            '법정동코드': 'sido_code',
            '사고건수': 'accident_count',
            '사고건수 구성비': 'accident_count_ratio',
            '사망자수': 'death_count',
            '사망자수 구성비': 'death_count_ratio',
            '치사율': 'fatality_rate',
            '부상자수': 'injury_count',
            '부상자수 구성비': 'injury_count_ratio',
            '인구10만명당 사고건수': 'accident_per_100k_population',
            '자동차1만대당 사고건수': 'accident_per_10k_vehicles',
            '과속': 'speeding_count',
            '중앙선 침범': 'center_line_violation_count',
            '신호위반': 'signal_violation_count',
            '안전거리 미확보': 'unsafe_distance_count',
            '안전운전 의무 불이행': 'unsafe_driving_count',
            '보행자 보호의무 위반': 'pedestrian_protection_violation_count',
            '기타': 'other_violation_count',
            '차대사람': 'vehicle_pedestrian_accident_count',
            '차대차': 'vehicle_vehicle_accident_count',
            '차량단독': 'single_vehicle_accident_count',
            '철길건널목': 'railroad_crossing_accident_count'
        }
        
        # Sido 코드 매핑
        self.sido_mapping = {
            '1100': '서울특별시',
            '1200': '부산광역시',
            '2200': '대구광역시',
            '2300': '인천광역시',
            '2400': '광주광역시',
            '2500': '대전광역시',
            '2600': '울산광역시',
            '2700': '세종특별자치시',
            '1300': '경기도',
            '1400': '강원특별자치도',
            '1500': '충청북도',
            '1600': '충청남도',
            '1700': '전북특별자치도',
            '1800': '전라남도',
            '1900': '경상북도',
            '2000': '경상남도',
            '2100': '제주특별자치도'
        }

    def create_table(self):
        """데이터베이스 테이블 생성"""
        try:
            print("🏗️ 테이블 생성 중...")
            
            create_table_sql = """
            CREATE TABLE IF NOT EXISTS accident_statics (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                accident_year INT NOT NULL COMMENT '연도',
                accident_type_name VARCHAR(50) NOT NULL COMMENT '대상사고 구분명',
                sido_code VARCHAR(10) NOT NULL COMMENT '법정동코드',
                sido_name VARCHAR(50) COMMENT '시도명',
                accident_count INT COMMENT '사고건수',
                accident_count_ratio DECIMAL(5,2) COMMENT '사고건수 구성비',
                death_count INT COMMENT '사망자수',
                death_count_ratio DECIMAL(5,2) COMMENT '사망자수 구성비',
                fatality_rate DECIMAL(5,2) COMMENT '치사율',
                injury_count INT COMMENT '부상자수',
                injury_count_ratio DECIMAL(5,2) COMMENT '부상자수 구성비',
                accident_per_100k_population DECIMAL(10,2) COMMENT '인구10만명당 사고건수',
                accident_per_10k_vehicles DECIMAL(10,2) COMMENT '자동차1만대당 사고건수',
                speeding_count INT COMMENT '과속',
                center_line_violation_count INT COMMENT '중앙선 침범',
                signal_violation_count INT COMMENT '신호위반',
                unsafe_distance_count INT COMMENT '안전거리 미확보',
                unsafe_driving_count INT COMMENT '안전운전 의무 불이행',
                pedestrian_protection_violation_count INT COMMENT '보행자 보호의무 위반',
                other_violation_count INT COMMENT '기타',
                vehicle_pedestrian_accident_count INT COMMENT '차대사람',
                vehicle_vehicle_accident_count INT COMMENT '차대차',
                single_vehicle_accident_count INT COMMENT '차량단독',
                railroad_crossing_accident_count INT COMMENT '철길건널목',
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                INDEX idx_year (accident_year),
                INDEX idx_sido (sido_code),
                INDEX idx_accident_type (accident_type_name)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='교통사고 통계 데이터';
            """
            
            with self.engine.connect() as connection:
                connection.execute(text(create_table_sql))
                connection.commit()
            
            print("✅ 테이블 생성 완료")
            
        except Exception as e:
            print(f"❌ 테이블 생성 실패: {e}")
            raise

    def load_csv_data(self, csv_path):
        """CSV 파일 로드 및 전처리"""
        try:
            print(f"📁 CSV 파일 로드 중: {csv_path}")
            
            # CSV 파일 읽기 (한글 인코딩 처리)
            df = pd.read_csv(csv_path, encoding='utf-8')
            
            print(f"📊 원본 데이터 형태: {df.shape}")
            print(f"📋 원본 컬럼: {list(df.columns)}")
            
            # 컬럼명 변경
            df = df.rename(columns=self.column_mapping)
            
            # Sido 코드를 시도명으로 변환
            df['sido_name'] = df['sido_code'].map(self.sido_mapping)
            
            # 데이터 타입 변환
            try:
                # 숫자형 컬럼 처리
                numeric_columns = [
                    'accident_count', 'accident_count_ratio', 'death_count', 'death_count_ratio',
                    'fatality_rate', 'injury_count', 'injury_count_ratio',
                    'accident_per_100k_population', 'accident_per_10k_vehicles',
                    'speeding_count', 'center_line_violation_count', 'signal_violation_count',
                    'unsafe_distance_count', 'unsafe_driving_count', 'pedestrian_protection_violation_count',
                    'other_violation_count', 'vehicle_pedestrian_accident_count',
                    'vehicle_vehicle_accident_count', 'single_vehicle_accident_count',
                    'railroad_crossing_accident_count'
                ]
                
                for col in numeric_columns:
                    if col in df.columns:
                        df[col] = pd.to_numeric(df[col], errors='coerce').fillna(0)
                
                # 연도 컬럼 처리
                if 'accident_year' in df.columns:
                    df['accident_year'] = pd.to_numeric(df['accident_year'], errors='coerce').fillna(0).astype(int)
                
            except Exception as e:
                print(f"⚠️ 데이터 타입 변환 중 오류: {e}")
            
            # NaN 값을 None으로 변환 (MySQL에서 오류 방지)
            df = df.where(pd.notnull(df), None)
            
            print(f"🔍 전처리 후 데이터 형태: {df.shape}")
            print(f"🔍 전처리 후 컬럼: {list(df.columns)}")
            
            return df
            
        except Exception as e:
            print(f"❌ CSV 로드 실패: {e}")
            raise

    def save_to_database(self, df):
        """데이터를 데이터베이스에 저장"""
        try:
            print("💾 데이터베이스에 저장 중...")
            
            # 테이블에 데이터 삽입 (기존 데이터는 무시)
            df.to_sql('accident_statics', 
                     con=self.engine, 
                     if_exists='append', 
                     index=False, 
                     method='multi',
                     chunksize=1000)
            
            print(f"✅ {len(df)}건의 데이터가 성공적으로 저장되었습니다.")
            
            # 저장된 데이터 확인
            with self.engine.connect() as connection:
                result = connection.execute(text("SELECT COUNT(*) as total FROM accident_statics"))
                total_count = result.fetchone()[0]
                print(f"📊 총 저장된 데이터: {total_count}건")
                
        except Exception as e:
            print(f"❌ 데이터베이스 저장 실패: {e}")
            raise

    def run_import(self, csv_path):
        """전체 임포트 프로세스 실행"""
        try:
            print("🚀 교통사고 통계 데이터 임포트 시작")
            print("=" * 50)
            
            # 1. 테이블 생성
            self.create_table()
            
            # 2. CSV 데이터 로드 및 전처리
            df = self.load_csv_data(csv_path)
            
            # 3. 데이터베이스에 저장
            self.save_to_database(df)
            
            print("=" * 50)
            print("🎉 모든 데이터 임포트가 완료되었습니다!")
            
        except Exception as e:
            print(f"❌ 임포트 실패: {e}")
            raise

def main():
    """메인 실행 함수"""
    try:
        # CSV 파일 경로
        csv_path = "csv/AccidentStatistics.csv"
        
        # 임포터 인스턴스 생성 및 실행
        importer = AccidentStatisticsImporter()
        importer.run_import(csv_path)
        
    except Exception as e:
        print(f"❌ 프로그램 실행 실패: {e}")
        return 1
    
    return 0

if __name__ == "__main__":
    exit(main())
