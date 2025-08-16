import os
import pandas as pd
import mysql.connector
from sqlalchemy import create_engine, text
from dotenv import load_dotenv
import logging
import json

# 로깅 설정
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

class RiskAreaImporter:
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
            '연도코드': 'year_code',
            '시군구코드': 'sigungu_code',
            '사고위험지역명': 'risk_area_name',
            '사고위험지역': 'risk_area_code',
            '사고위험지역폴리곤': 'risk_area_polygon',
            '총사고건수': 'total_accident_count',
            '총사망자수': 'total_death_count',
            '총중상자수': 'total_serious_injury_count',
            '총경상자수': 'total_minor_injury_count',
            '총부상신고자수': 'total_injury_report_count',
            '사고분석유형명': 'accident_analysis_type',
            '중심점utmkx좌표': 'center_utmk_x',
            '중심점utmky좌표': 'center_utmk_y'
        }
        
        # 시군구 코드 매핑 (주요 지역)
        self.sigungu_mapping = {
            '11110': '서울특별시 종로구',
            '11140': '서울특별시 중구',
            '11170': '서울특별시 용산구',
            '11200': '서울특별시 성동구',
            '11215': '서울특별시 광진구',
            '11230': '서울특별시 동대문구',
            '11260': '서울특별시 중랑구',
            '11290': '서울특별시 성북구',
            '11305': '서울특별시 강북구',
            '11320': '서울특별시 도봉구',
            '11350': '서울특별시 노원구',
            '11380': '서울특별시 은평구',
            '11410': '서울특별시 서대문구',
            '11440': '서울특별시 마포구',
            '11470': '서울특별시 양천구',
            '11500': '서울특별시 강서구',
            '11530': '서울특별시 구로구',
            '11545': '서울특별시 금천구',
            '11560': '서울특별시 영등포구',
            '11590': '서울특별시 동작구',
            '11620': '서울특별시 관악구',
            '11650': '서울특별시 서초구',
            '11680': '서울특별시 강남구',
            '11710': '서울특별시 송파구',
            '11740': '서울특별시 강동구'
        }

    def create_table(self):
        """데이터베이스 테이블 생성"""
        try:
            print("🏗️ 사고 위험지역 테이블 생성 중...")
            
            create_table_sql = """
            CREATE TABLE IF NOT EXISTS risk_areas (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                year_code INT NOT NULL COMMENT '연도코드',
                sigungu_code VARCHAR(10) NOT NULL COMMENT '시군구코드',
                sigungu_name VARCHAR(50) COMMENT '시군구명',
                risk_area_name TEXT NOT NULL COMMENT '사고위험지역명',
                risk_area_code VARCHAR(20) NOT NULL COMMENT '사고위험지역',
                risk_area_polygon TEXT COMMENT '사고위험지역폴리곤',
                total_accident_count INT COMMENT '총사고건수',
                total_death_count INT COMMENT '총사망자수',
                total_serious_injury_count INT COMMENT '총중상자수',
                total_minor_injury_count INT COMMENT '총경상자수',
                total_injury_report_count INT COMMENT '총부상신고자수',
                accident_analysis_type VARCHAR(200) COMMENT '사고분석유형명',
                center_utmk_x DECIMAL(15,4) COMMENT '중심점utmkx좌표',
                center_utmk_y DECIMAL(15,4) COMMENT '중심점utmky좌표',
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                INDEX idx_year (year_code),
                INDEX idx_sigungu (sigungu_code),
                INDEX idx_risk_area (risk_area_code),
                INDEX idx_accident_count (total_accident_count),
                UNIQUE KEY uk_risk_area (year_code, sigungu_code, risk_area_code)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사고 위험지역 데이터';
            """
            
            with self.engine.connect() as connection:
                connection.execute(text(create_table_sql))
                connection.commit()
            
            print("✅ 사고 위험지역 테이블 생성 완료")
            
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
            
            # 시군구 코드를 시군구명으로 변환
            df['sigungu_name'] = df['sigungu_code'].map(self.sigungu_mapping)
            
            # 데이터 타입 변환
            try:
                # 숫자형 컬럼 처리
                numeric_columns = [
                    'total_accident_count', 'total_death_count', 'total_serious_injury_count',
                    'total_minor_injury_count', 'total_injury_report_count',
                    'center_utmk_x', 'center_utmk_y'
                ]
                
                for col in numeric_columns:
                    if col in df.columns:
                        df[col] = pd.to_numeric(df[col], errors='coerce').fillna(0)
                
                # 연도 코드 처리
                if 'year_code' in df.columns:
                    df['year_code'] = pd.to_numeric(df['year_code'], errors='coerce').fillna(0).astype(int)
                
                # 폴리곤 데이터 처리 (WKT 형식)
                if 'risk_area_polygon' in df.columns:
                    # WKT 폴리곤 데이터를 그대로 저장 (PostGIS나 공간 데이터 처리 시 활용 가능)
                    def validate_polygon(polygon_str):
                        try:
                            if pd.isna(polygon_str) or polygon_str == '':
                                return None
                            # WKT 형식 검증 (POLYGON으로 시작하는지 확인)
                            if polygon_str.startswith('POLYGON'):
                                return polygon_str
                            else:
                                return None
                        except:
                            return None
                    
                    df['risk_area_polygon'] = df['risk_area_polygon'].apply(validate_polygon)
                
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
            df.to_sql('risk_areas', 
                     con=self.engine, 
                     if_exists='append', 
                     index=False, 
                     method='multi',
                     chunksize=1000)
            
            print(f"✅ {len(df)}건의 데이터가 성공적으로 저장되었습니다.")
            
            # 저장된 데이터 확인
            with self.engine.connect() as connection:
                result = connection.execute(text("SELECT COUNT(*) as total FROM risk_areas"))
                total_count = result.fetchone()[0]
                print(f"📊 총 저장된 데이터: {total_count}건")
                
        except Exception as e:
            print(f"❌ 데이터베이스 저장 실패: {e}")
            raise

    def run_import(self, csv_path):
        """전체 임포트 프로세스 실행"""
        try:
            print("🚀 사고 위험지역 데이터 임포트 시작")
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
        csv_path = "csv/RiskArea.csv"
        
        # 임포터 인스턴스 생성 및 실행
        importer = RiskAreaImporter()
        importer.run_import(csv_path)
        
    except Exception as e:
        print(f"❌ 프로그램 실행 실패: {e}")
        return 1
    
    return 0

if __name__ == "__main__":
    exit(main())
