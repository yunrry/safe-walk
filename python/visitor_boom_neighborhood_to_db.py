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

class VisitorBoomNeighborhoodImporter:
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
            '순위': 'ranking',
            '시도명': 'sido_name',
            '시군구명': 'sigungu_name',
            '행정동명': 'administrative_dong',
            '관광객수': 'visitor_count',
            '전년동기관광객수': 'last_year_visitor_count',
            '증감율': 'growth_rate',
            '기준년월': 'base_year_month',
            '조회일자': 'search_date'
        }

    def create_table(self):
        """데이터베이스 테이블 생성"""
        try:
            print("🏗️ 방문자 급등동네(내국인) 테이블 생성 중...")
            
            create_table_sql = """
            CREATE TABLE IF NOT EXISTS visitor_boom_neighborhoods (
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
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                INDEX idx_ranking (ranking),
                INDEX idx_sido (sido_name),
                INDEX idx_sigungu (sigungu_name),
                INDEX idx_administrative_dong (administrative_dong),
                INDEX idx_location (sido_name, sigungu_name),
                INDEX idx_growth_rate (growth_rate),
                INDEX idx_base_year_month (base_year_month)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='방문자 급등동네(내국인) 데이터';
            """
            
            with self.engine.connect() as connection:
                connection.execute(text(create_table_sql))
                connection.commit()
            
            print("✅ 방문자 급등동네(내국인) 테이블 생성 완료")
            
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
                numeric_columns = ['ranking', 'visitor_count', 'last_year_visitor_count', 'growth_rate']
                
                for col in numeric_columns:
                    if col in df.columns:
                        df[col] = pd.to_numeric(df[col], errors='coerce').fillna(0)
                
                # 문자열 컬럼 처리 (빈 값은 None으로 변환)
                string_columns = ['sido_name', 'sigungu_name', 'administrative_dong', 'base_year_month', 'search_date']
                for col in string_columns:
                    if col in df.columns:
                        df[col] = df[col].replace('', None)
                
            except Exception as e:
                print(f"⚠️ 데이터 타입 변환 중 오류: {e}")
            
            # NaN 값을 None으로 변환 (MySQL에서 오류 방지)
            df = df.where(pd.notnull(df), None)
            
            # 필요한 컬럼만 선택
            required_columns = [
                'ranking', 'sido_name', 'sigungu_name', 'administrative_dong',
                'visitor_count', 'last_year_visitor_count', 'growth_rate',
                'base_year_month', 'search_date'
            ]
            
            # 존재하는 컬럼만 선택
            existing_columns = [col for col in required_columns if col in df.columns]
            df = df[existing_columns]
            
            print(f"🔍 전처리 후 데이터 형태: {df.shape}")
            print(f"🔍 전처리 후 컬럼: {list(df.columns)}")
            
            # 데이터 샘플 출력
            print(f"📊 전처리 후 데이터 샘플:")
            sample_data = df.head(5)
            for idx, row in sample_data.iterrows():
                print(f"   {row['ranking']}위 | {row['sido_name']} {row['sigungu_name']} {row['administrative_dong']} | 관광객수: {row['visitor_count']:,} | 증감율: {row['growth_rate']}%")
            
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
                connection.execute(text("DELETE FROM visitor_boom_neighborhoods"))
                connection.commit()
            print("✅ 기존 데이터 삭제 완료")
            
            # 테이블에 데이터 삽입
            df.to_sql('visitor_boom_neighborhoods', 
                     con=self.engine, 
                     if_exists='append', 
                     index=False, 
                     method='multi',
                     chunksize=1000)
            
            print(f"✅ {len(df)}건의 데이터가 성공적으로 저장되었습니다.")
            
            # 저장된 데이터 확인
            with self.engine.connect() as connection:
                result = connection.execute(text("SELECT COUNT(*) as total FROM visitor_boom_neighborhoods"))
                total_count = result.fetchone()[0]
                print(f"📊 총 저장된 데이터: {total_count}건")
                
                # 시도별 데이터 수 확인
                sido_count_sql = """
                SELECT sido_name, COUNT(*) as count 
                FROM visitor_boom_neighborhoods 
                GROUP BY sido_name 
                ORDER BY count DESC
                """
                sido_result = connection.execute(text(sido_count_sql))
                print(f"📊 시도별 데이터 수:")
                for row in sido_result:
                    print(f"   {row[0]}: {row[1]}건")
                
                # 증감율 상위 5개 동네 확인
                top_growth_sql = """
                SELECT ranking, sido_name, sigungu_name, administrative_dong, growth_rate, visitor_count
                FROM visitor_boom_neighborhoods 
                ORDER BY growth_rate DESC 
                LIMIT 5
                """
                top_result = connection.execute(text(top_growth_sql))
                print(f"📊 증감율 상위 5개 동네:")
                for row in top_result:
                    print(f"   {row[0]}위 | {row[1]} {row[2]} {row[3]} | 증감율: {row[4]}% | 관광객수: {row[5]:,}")
                
        except Exception as e:
            print(f"❌ 데이터베이스 저장 실패: {e}")
            raise

    def run_import(self, csv_path):
        """전체 임포트 프로세스 실행"""
        try:
            print("🚀 방문자 급등동네(내국인) 데이터 임포트 시작")
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
        csv_path = "csv/20250817000715_방문자 급등동네(내국인).csv"
        
        # 임포터 인스턴스 생성 및 실행
        importer = VisitorBoomNeighborhoodImporter()
        importer.run_import(csv_path)
        
    except Exception as e:
        print(f"❌ 프로그램 실행 실패: {e}")
        return 1
    
    return 0

if __name__ == "__main__":
    exit(main())
