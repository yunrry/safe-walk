#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import pandas as pd
import mysql.connector
from sqlalchemy import create_engine, text
from dotenv import load_dotenv
import os
import sys
from datetime import datetime

# .env 파일 로드
load_dotenv()

class AccidentStatisticsImporter:
    def __init__(self):
        """초기화 - 데이터베이스 연결 설정"""
        self.db_config = {
            'host': os.getenv('DB_HOST', 'localhost'),
            'port': int(os.getenv('DB_PORT', 3306)),
            'user': os.getenv('DB_USER', 'root'),
            'password': os.getenv('DB_PASSWORD'),
            'database': os.getenv('DB_NAME', 'safe_walk'),
            'charset': os.getenv('DB_CHARSET', 'utf8mb4')
        }
        
        # SQLAlchemy 엔진 생성
        connection_string = f"mysql+mysqlconnector://{self.db_config['user']}:{self.db_config['password']}@{self.db_config['host']}:{self.db_config['port']}/{self.db_config['database']}?charset={self.db_config['charset']}"
        self.engine = create_engine(connection_string)
        
    def create_table(self):
        """accident_statics 테이블 생성"""
        create_table_sql = """
        CREATE TABLE IF NOT EXISTS accident_statics (
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
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
        COMMENT='교통사고 통계 데이터';
        """
        
        try:
            with self.engine.connect() as connection:
                connection.execute(text(create_table_sql))
                connection.commit()
            print("✅ accident_statics 테이블이 성공적으로 생성되었습니다.")
        except Exception as e:
            print(f"❌ 테이블 생성 중 오류 발생: {e}")
            raise
    
    def preprocess_csv_data(self, df):
        """CSV 데이터 전처리"""
        print("📊 CSV 데이터 전처리 중...")
        
        # 컬럼명 매핑 (한글 -> 영어)
        column_mapping = {
            '사고발생일': 'accident_date',
            '발생일시': 'accident_date',
            '사고발생시간': 'accident_time', 
            '발생시간': 'accident_time',
            '요일': 'day_of_week',
            '시도': 'sido_name',
            '시도명': 'sido_name',
            '시군구': 'sigungu_name', 
            '시군구명': 'sigungu_name',
            '사고유형': 'accident_type',
            '사고종류': 'accident_type',
            '기상': 'weather_condition',
            '기상상태': 'weather_condition',
            '도로형태': 'road_type',
            '노면상태': 'road_surface',
            '사망자수': 'death_count',
            '사망': 'death_count',
            '중상자수': 'serious_injury_count',
            '중상': 'serious_injury_count',
            '경상자수': 'minor_injury_count',
            '경상': 'minor_injury_count',
            '부상신고자수': 'injury_report_count',
            '부상신고': 'injury_report_count',
            '차량수': 'vehicle_count',
            '연령층': 'age_group',
            '연령대': 'age_group',
            '성별': 'gender',
            '위도': 'location_lat',
            '경도': 'location_lng'
        }
        
        # 컬럼명 변경
        for old_col, new_col in column_mapping.items():
            if old_col in df.columns:
                df = df.rename(columns={old_col: new_col})
        
        # 필수 컬럼들 확인 및 생성
        required_columns = [
            'accident_date', 'accident_time', 'day_of_week', 'sido_name', 'sigungu_name',
            'accident_type', 'weather_condition', 'road_type', 'road_surface',
            'death_count', 'serious_injury_count', 'minor_injury_count', 'injury_report_count',
            'vehicle_count', 'age_group', 'gender', 'location_lat', 'location_lng'
        ]
        
        for col in required_columns:
            if col not in df.columns:
                if 'count' in col:
                    df[col] = 0
                else:
                    df[col] = None
        
        # 데이터 타입 변환
        try:
            # 날짜 처리
            if 'accident_date' in df.columns:
                df['accident_date'] = pd.to_datetime(df['accident_date'], errors='coerce')
            
            # 시간 처리 (HH:MM 형식으로 변환)
            if 'accident_time' in df.columns:
                df['accident_time'] = pd.to_datetime(df['accident_time'], format='%H:%M', errors='coerce').dt.time
            
            # 숫자형 컬럼 처리
            numeric_columns = ['death_count', 'serious_injury_count', 'minor_injury_count', 
                             'injury_report_count', 'vehicle_count', 'location_lat', 'location_lng']
            
            for col in numeric_columns:
                if col in df.columns:
                    df[col] = pd.to_numeric(df[col], errors='coerce').fillna(0)
            
            # 총 부상자 수 계산
            df['total_injury_count'] = (df['serious_injury_count'].fillna(0) + 
                                      df['minor_injury_count'].fillna(0) + 
                                      df['injury_report_count'].fillna(0))
            
        except Exception as e:
            print(f"⚠️ 데이터 전처리 중 경고: {e}")
        
        print(f"📈 전처리 완료: {len(df)}행, {len(df.columns)}열")
        return df
    
    def load_csv_data(self, csv_file_path):
        """CSV 파일 로드"""
        try:
            print(f"📂 CSV 파일 로딩 중: {csv_file_path}")
            
            # 다양한 인코딩으로 시도
            encodings = ['utf-8', 'cp949', 'euc-kr', 'utf-8-sig']
            df = None
            
            for encoding in encodings:
                try:
                    df = pd.read_csv(csv_file_path, encoding=encoding)
                    print(f"✅ 인코딩 '{encoding}'로 파일 로딩 성공")
                    break
                except UnicodeDecodeError:
                    continue
            
            if df is None:
                raise Exception("지원하는 인코딩으로 파일을 읽을 수 없습니다.")
            
            print(f"📊 로드된 데이터: {len(df)}행, {len(df.columns)}열")
            print("컬럼명:", list(df.columns))
            
            return df
            
        except Exception as e:
            print(f"❌ CSV 파일 로딩 실패: {e}")
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
    
    def import_accident_statistics(self, csv_file_path):
        """전체 임포트 프로세스 실행"""
        try:
            print("🚀 교통사고 통계 데이터 임포트 시작")
            print("="*50)
            
            # 1. 테이블 생성
            self.create_table()
            
            # 2. CSV 파일 로드
            df = self.load_csv_data(csv_file_path)
            
            # 3. 데이터 전처리
            df_processed = self.preprocess_csv_data(df)
            
            # 4. 데이터베이스에 저장
            self.save_to_database(df_processed)
            
            print("="*50)
            print("🎉 교통사고 통계 데이터 임포트 완료!")
            
        except Exception as e:
            print(f"💥 임포트 프로세스 실패: {e}")
            sys.exit(1)

def main():
    """메인 함수"""
    # CSV 파일 경로 설정
    csv_file_path = os.path.join(os.path.dirname(os.path.dirname(__file__)), 'csv', 'AccientStatistics.csv')
    
    if not os.path.exists(csv_file_path):
        print(f"❌ CSV 파일을 찾을 수 없습니다: {csv_file_path}")
        print("💡 csv 디렉토리에 AccientStatistics.csv 파일을 배치해주세요.")
        sys.exit(1)
    
    # 임포터 실행
    importer = AccidentStatisticsImporter()
    importer.import_accident_statistics(csv_file_path)

if __name__ == "__main__":
    main()