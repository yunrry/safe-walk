import os
import pandas as pd
from sqlalchemy import create_engine, text
from dotenv import load_dotenv
import logging

# 로깅 설정
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

class AdministrativeLegalDongCoordinatesImporter:
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
            '법정동코드': 'code',
            '시도': 'sido',
            '시군구': 'sigungu',
            '읍면동': 'eup_myeon_dong',
            '하위': 'hawi',
            '위도': 'latitude',
            '경도': 'longitude',
            '코드종류': 'code_type'
        }

    def create_table(self):
        """데이터베이스 테이블 생성"""
        try:
            print("🏗️ 행정 법정동 중심좌표 테이블 생성 중...")
            
            create_table_sql = """
            CREATE TABLE IF NOT EXISTS administrative_legal_dong_coordinates (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                code VARCHAR(20) NOT NULL COMMENT '법정동 코드',
                sido VARCHAR(50) NOT NULL COMMENT '시도',
                sigungu VARCHAR(50) NOT NULL COMMENT '시군구',
                eup_myeon_dong VARCHAR(50) NOT NULL COMMENT '읍면동',
                hawi VARCHAR(50) COMMENT '하위',
                latitude DECIMAL(10, 8) NOT NULL COMMENT '위도',
                longitude DECIMAL(11, 8) NOT NULL COMMENT '경도',
                code_type VARCHAR(10) COMMENT '코드 종류',
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                INDEX idx_code (code),
                INDEX idx_sido (sido),
                INDEX idx_sigungu (sigungu),
                INDEX idx_eup_myeon_dong (eup_myeon_dong),
                INDEX idx_location (longitude, latitude),
                UNIQUE KEY uk_code (code)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='행정 법정동 중심좌표 데이터';
            """
            
            with self.engine.connect() as connection:
                connection.execute(text(create_table_sql))
                connection.commit()
            
            print("✅ 행정 법정동 중심좌표 테이블 생성 완료")
            
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
            
            # 데이터 타입 변환
            try:
                # 숫자형 컬럼 처리
                numeric_columns = ['latitude', 'longitude']
                
                for col in numeric_columns:
                    if col in df.columns:
                        df[col] = pd.to_numeric(df[col], errors='coerce').fillna(0)
                
            except Exception as e:
                print(f"⚠️ 데이터 타입 변환 중 오류: {e}")
            
            # NaN 값을 None으로 변환 (MySQL에서 오류 방지)
            df = df.where(pd.notnull(df), None)
            
            # 필요한 컬럼만 선택 (pandas에서 생성된 _m 접미사 컬럼 제거)
            required_columns = [
                'code', 'sido', 'sigungu', 'eup_myeon_dong', 'hawi', 'latitude', 'longitude', 'code_type'
            ]
            
            # 존재하는 컬럼만 선택하고 _m 접미사가 붙지 않은 컬럼만 사용
            existing_columns = [col for col in required_columns if col in df.columns and not col.endswith('_m')]
            df = df[existing_columns]
            
            # 컬럼명을 원래대로 복원 (pandas에서 _m 접미사가 붙은 경우)
            column_mapping = {}
            for col in df.columns:
                if col.endswith('_m'):
                    original_col = col[:-2]  # _m 제거
                    column_mapping[col] = original_col
            
            if column_mapping:
                df = df.rename(columns=column_mapping)
            
            # 최종적으로 필요한 컬럼만 선택
            final_columns = [col for col in required_columns if col in df.columns]
            df = df[final_columns]
            
            # 컬럼 순서를 required_columns 순서대로 정렬
            df = df[final_columns]
            
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
                connection.execute(text("DELETE FROM administrative_legal_dong_coordinates"))
                connection.commit()
            print("✅ 기존 데이터 삭제 완료")
            
            # 테이블에 데이터 삽입
            df.to_sql('administrative_legal_dong_coordinates', 
                     con=self.engine, 
                     if_exists='append', 
                     index=False, 
                     method='multi',
                     chunksize=1000)
            
            print(f"✅ {len(df)}건의 데이터가 성공적으로 저장되었습니다.")
            
            # 저장된 데이터 확인
            with self.engine.connect() as connection:
                result = connection.execute(text("SELECT COUNT(*) as total FROM administrative_legal_dong_coordinates"))
                total_count = result.fetchone()[0]
                print(f"📊 총 저장된 데이터: {total_count}건")
                
        except Exception as e:
            print(f"❌ 데이터베이스 저장 실패: {e}")
            raise

    def run_import(self, csv_path):
        """전체 임포트 프로세스 실행"""
        try:
            print("🚀 행정 법정동 중심좌표 데이터 임포트 시작")
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
        csv_path = "csv/행정 법정동 중심좌표.csv"
        
        # 임포터 인스턴스 생성 및 실행
        importer = AdministrativeLegalDongCoordinatesImporter()
        importer.run_import(csv_path)
        
    except Exception as e:
        print(f"❌ 프로그램 실행 실패: {e}")
        return 1
    
    return 0

if __name__ == "__main__":
    exit(main())
