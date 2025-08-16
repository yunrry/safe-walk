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

class ElderlyPedestrianAccidentImporter:
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
            '사고다발지fid': 'accident_hotspot_fid',
            '사고다발지id': 'accident_hotspot_id',
            '법정동코드': 'sido_code',
            '지점코드': 'point_code',
            '시도시군구명': 'sido_sigungu_name',
            '지점명': 'point_name',
            '사고건수': 'accident_count',
            '사상자수': 'casualty_count',
            '사망자수': 'death_count',
            '중상자수': 'serious_injury_count',
            '경상자수': 'minor_injury_count',
            '부상신고자수': 'injury_report_count',
            '경도': 'longitude',
            '위도': 'latitude',
            '다발지역폴리곤': 'hotspot_polygon'
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
            print("🏗️ 노인 보행자 사고 다발지 테이블 생성 중...")
            
            create_table_sql = """
            CREATE TABLE IF NOT EXISTS elderly_pedestrian_accident_hotspots (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                accident_hotspot_fid BIGINT NOT NULL COMMENT '사고다발지fid',
                accident_hotspot_id BIGINT NOT NULL COMMENT '사고다발지id',
                sido_code VARCHAR(10) NOT NULL COMMENT '법정동코드',
                point_code VARCHAR(20) NOT NULL COMMENT '지점코드',
                sido_sigungu_name VARCHAR(100) NOT NULL COMMENT '시도시군구명',
                point_name TEXT COMMENT '지점명',
                legal_dong VARCHAR(50) COMMENT '법정동',
                accident_count INT COMMENT '사고건수',
                casualty_count INT COMMENT '사상자수',
                death_count INT COMMENT '사망자수',
                serious_injury_count INT COMMENT '중상자수',
                minor_injury_count INT COMMENT '경상자수',
                injury_report_count INT COMMENT '부상신고자수',
                longitude DECIMAL(12,9) COMMENT '경도',
                latitude DECIMAL(12,9) COMMENT '위도',
                hotspot_polygon JSON COMMENT '다발지역폴리곤',
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                INDEX idx_hotspot_fid (accident_hotspot_fid),
                INDEX idx_hotspot_id (accident_hotspot_id),
                INDEX idx_sido_code (sido_code),
                INDEX idx_point_code (point_code),
                INDEX idx_legal_dong (legal_dong),
                INDEX idx_location (longitude, latitude),
                UNIQUE KEY uk_hotspot_fid (accident_hotspot_fid)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='노인 보행자 사고 다발지 데이터';
            """
            
            with self.engine.connect() as connection:
                connection.execute(text(create_table_sql))
                connection.commit()
            
            print("✅ 노인 보행자 사고 다발지 테이블 생성 완료")
            
            # 기존 테이블에 법정동 칼럼이 없다면 추가
            self.add_legal_dong_column()
            
        except Exception as e:
            print(f"❌ 테이블 생성 실패: {e}")
            raise

    def add_legal_dong_column(self):
        """기존 테이블에 법정동 칼럼 추가"""
        try:
            print("🔧 법정동 칼럼 추가 확인 중...")
            
            # 테이블 구조 확인
            check_column_sql = """
            SELECT COLUMN_NAME 
            FROM INFORMATION_SCHEMA.COLUMNS 
            WHERE TABLE_SCHEMA = :database 
            AND TABLE_NAME = 'elderly_pedestrian_accident_hotspots' 
            AND COLUMN_NAME = 'legal_dong'
            """
            
            with self.engine.connect() as connection:
                result = connection.execute(text(check_column_sql), 
                                         {"database": self.db_config['database']})
                column_exists = result.fetchone() is not None
                
                if not column_exists:
                    print("➕ 법정동 칼럼 추가 중...")
                    
                    add_column_sql = """
                    ALTER TABLE elderly_pedestrian_accident_hotspots 
                    ADD COLUMN legal_dong VARCHAR(50) COMMENT '법정동' AFTER point_name
                    """
                    
                    connection.execute(text(add_column_sql))
                    connection.commit()
                    
                    # 인덱스 추가
                    add_index_sql = """
                    ALTER TABLE elderly_pedestrian_accident_hotspots 
                    ADD INDEX idx_legal_dong (legal_dong)
                    """
                    
                    connection.execute(text(add_index_sql))
                    connection.commit()
                    
                    print("✅ 법정동 칼럼 추가 완료")
                else:
                    print("✅ 법정동 칼럼이 이미 존재합니다")
                    
        except Exception as e:
            print(f"⚠️ 법정동 칼럼 추가 중 오류: {e}")

    def extract_legal_dong(self, point_name):
        """
        지점명에서 법정동을 추출하는 함수
        
        예시:
        - "서울특별시 종로구 종로2가(종로2가교차로 부근)" -> "종로2가"
        - "서울특별시 동대문구 제기동(용두교 부근)" -> "제기동"
        - "경기도 고양시 일산동구 마두동(양주주차타워앞 인근)" -> "마두동"
        - "충남 서산시 읍내동(부춘동주민자치센터 부근)" -> "읍내동"
        """
        try:
            if pd.isna(point_name) or point_name == '':
                return None
            
            # 괄호가 있는지 확인
            if '(' not in point_name:
                return None
            
            # 괄호 앞부분을 추출
            before_parenthesis = point_name.split('(')[0]
            
            # 공백으로 단어를 분리
            words = before_parenthesis.strip().split()
            
            # 마지막 단어가 법정동
            if len(words) > 0:
                legal_dong = words[-1]
                return legal_dong.strip()
            
            return None
            
        except Exception as e:
            print(f"⚠️ 지점명 '{point_name}' 처리 중 오류: {e}")
            return None

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
            
            # 법정동 추출 및 추가
            print("🔍 법정동 추출 중...")
            df['legal_dong'] = df['point_name'].apply(self.extract_legal_dong)
            
            # 추출 결과 샘플 출력
            print(f"📊 추출된 법정동 샘플:")
            sample_data = df[['point_name', 'legal_dong']].head(5)
            for idx, row in sample_data.iterrows():
                print(f"   {row['point_name']} -> {row['legal_dong']}")
            
            # 데이터 타입 변환
            try:
                # 숫자형 컬럼 처리
                numeric_columns = [
                    'accident_count', 'casualty_count', 'death_count', 'serious_injury_count',
                    'minor_injury_count', 'injury_report_count', 'longitude', 'latitude'
                ]
                
                for col in numeric_columns:
                    if col in df.columns:
                        df[col] = pd.to_numeric(df[col], errors='coerce').fillna(0)
                
                # BIGINT 컬럼 처리
                bigint_columns = ['accident_hotspot_fid', 'accident_hotspot_id']
                for col in bigint_columns:
                    if col in df.columns:
                        df[col] = pd.to_numeric(df[col], errors='coerce').fillna(0).astype('Int64')
                
                # JSON 컬럼 처리 (폴리곤 데이터)
                if 'hotspot_polygon' in df.columns:
                    # JSON 문자열을 파싱하여 유효성 검증
                    def validate_polygon(polygon_str):
                        try:
                            if pd.isna(polygon_str) or polygon_str == '':
                                return None
                            # JSON 파싱 테스트
                            json.loads(polygon_str)
                            return polygon_str
                        except:
                            return None
                    
                    df['hotspot_polygon'] = df['hotspot_polygon'].apply(validate_polygon)
                
            except Exception as e:
                print(f"⚠️ 데이터 타입 변환 중 오류: {e}")
            
            # NaN 값을 None으로 변환 (MySQL에서 오류 방지)
            df = df.where(pd.notnull(df), None)
            
            # 필요한 컬럼만 선택 (pandas에서 생성된 _m 접미사 컬럼 제거)
            required_columns = [
                'accident_hotspot_fid', 'accident_hotspot_id', 'sido_code', 'point_code',
                'sido_sigungu_name', 'point_name', 'legal_dong', 'accident_count',
                'casualty_count', 'death_count', 'serious_injury_count', 'minor_injury_count',
                'injury_report_count', 'longitude', 'latitude', 'hotspot_polygon'
            ]
            
            # 존재하는 컬럼만 선택
            existing_columns = [col for col in required_columns if col in df.columns]
            df = df[existing_columns]
            
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
            
            # 기존 데이터 삭제 (테이블 초기화)
            print("🗑️ 기존 데이터 삭제 중...")
            with self.engine.connect() as connection:
                connection.execute(text("DELETE FROM elderly_pedestrian_accident_hotspots"))
                connection.commit()
            print("✅ 기존 데이터 삭제 완료")
            
            # 테이블에 데이터 삽입
            df.to_sql('elderly_pedestrian_accident_hotspots', 
                     con=self.engine, 
                     if_exists='append', 
                     index=False, 
                     method='multi',
                     chunksize=1000)
            
            print(f"✅ {len(df)}건의 데이터가 성공적으로 저장되었습니다.")
            
            # 저장된 데이터 확인
            with self.engine.connect() as connection:
                result = connection.execute(text("SELECT COUNT(*) as total FROM elderly_pedestrian_accident_hotspots"))
                total_count = result.fetchone()[0]
                print(f"📊 총 저장된 데이터: {total_count}건")
                
        except Exception as e:
            print(f"❌ 데이터베이스 저장 실패: {e}")
            raise

    def run_import(self, csv_path):
        """전체 임포트 프로세스 실행"""
        try:
            print("🚀 노인 보행자 사고 다발지 데이터 임포트 시작")
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
        csv_path = "csv/ElderlyPedestrianAccident.csv"
        
        # 임포터 인스턴스 생성 및 실행
        importer = ElderlyPedestrianAccidentImporter()
        importer.run_import(csv_path)
        
    except Exception as e:
        print(f"❌ 프로그램 실행 실패: {e}")
        return 1
    
    return 0

if __name__ == "__main__":
    exit(main())
