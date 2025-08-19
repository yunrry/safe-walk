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

class PopularTouristSpotsImporter:
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
            '순위': 'rank',
            '관광지ID': 'tourist_spot_id',
            '관심지점명': 'spot_name',
            '구분': 'category',
            '연령대': 'age_group',
            '비율': 'ratio',
            '기준년월': 'base_year_month',
            '시도명': 'sido_name',
            '시군구명': 'sigungu_name',
            '성장율': 'growth_rate'
        }

    def create_table(self):
        """데이터베이스 테이블 생성"""
        try:
            print("🏗️ 인기관광지 테이블 생성 중...")
            
            create_table_sql = """
            CREATE TABLE IF NOT EXISTS popular_tourist_spots (
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
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                INDEX idx_sido (sido_name),
                INDEX idx_sigungu (sigungu_name),
                INDEX idx_spot_name (spot_name),
                INDEX idx_category (category),
                INDEX idx_tourist_spot_id (tourist_spot_id),
                INDEX idx_location (sido_name, sigungu_name)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='인기관광지 데이터';
            """
            
            with self.engine.connect() as connection:
                connection.execute(text(create_table_sql))
                connection.commit()
            
            print("✅ 인기관광지 테이블 생성 완료")
            
        except Exception as e:
            print(f"❌ 테이블 생성 실패: {e}")
            raise

    def load_csv_data(self, csv_path, source_file):
        """CSV 파일 로드 및 전처리"""
        try:
            print(f"📁 CSV 파일 로드 중: {csv_path}")
            
            # CSV 파일 읽기 (한글 인코딩 처리)
            df = pd.read_csv(csv_path, encoding='utf-8')
            
            print(f"📊 원본 데이터 형태: {df.shape}")
            print(f"📋 원본 컬럼: {list(df.columns)}")
            
            # 컬럼명 변경
            df = df.rename(columns=self.column_mapping)
            
            # 출처 파일 정보 추가
            df['source_file'] = source_file
            
            # 데이터 타입 변환
            try:
                # 숫자형 컬럼 처리
                numeric_columns = ['ratio', 'growth_rate']
                
                for col in numeric_columns:
                    if col in df.columns:
                        df[col] = pd.to_numeric(df[col], errors='coerce').fillna(0)
                
                # 문자열 컬럼 처리 (빈 값은 None으로 변환)
                string_columns = ['sido_name', 'sigungu_name', 'spot_name', 'tourist_spot_id', 
                                'category', 'age_group', 'base_year_month']
                for col in string_columns:
                    if col in df.columns:
                        df[col] = df[col].replace('', None)
                
            except Exception as e:
                print(f"⚠️ 데이터 타입 변환 중 오류: {e}")
            
            # NaN 값을 None으로 변환 (MySQL에서 오류 방지)
            df = df.where(pd.notnull(df), None)
            
            # 필요한 컬럼만 선택
            required_columns = [
                'sido_name', 'sigungu_name', 'spot_name', 'tourist_spot_id',
                'category', 'age_group', 'ratio', 'base_year_month', 
                'growth_rate', 'source_file'
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
                if 'sido_name' in df.columns and 'sigungu_name' in df.columns:
                    print(f"   {row['sido_name']} {row['sigungu_name']} | {row['spot_name']} | {row['category']}")
                else:
                    print(f"   {row['spot_name']} | {row['category']}")
            
            return df
            
        except Exception as e:
            print(f"❌ CSV 로드 실패: {e}")
            raise

    def merge_and_process_data(self, df1, df2):
        """두 CSV 데이터를 병합하고 처리"""
        try:
            print("🔄 데이터 병합 및 처리 중...")
            
            # 첫 번째 CSV (세대별 인기관광지)에 시도/시군구 정보 추가
            # 경상북도 경주시로 설정
            df1['sido_name'] = '제주특별자치도'
            df1['sigungu_name'] = '제주시'
            df1['base_year_month'] = None
            df1['growth_rate'] = None
            
            # 두 번째 CSV (세대별 핫플레이스)에 ratio 컬럼 추가 (기본값 0)
            df2['ratio'] = 0
            
            # 필요한 컬럼만 선택하여 통일 (존재하는 컬럼만)
            df1_columns = ['sido_name', 'sigungu_name', 'spot_name', 'tourist_spot_id', 
                          'category', 'age_group', 'ratio', 'base_year_month', 
                          'growth_rate', 'source_file']
            
            df2_columns = ['sido_name', 'sigungu_name', 'spot_name', 'tourist_spot_id', 
                          'category', 'age_group', 'ratio', 'base_year_month', 
                          'growth_rate', 'source_file']
            
            # 존재하는 컬럼만 선택
            df1_processed = df1[[col for col in df1_columns if col in df1.columns]]
            df2_processed = df2[[col for col in df2_columns if col in df2.columns]]
            
            # 두 데이터프레임 병합
            merged_df = pd.concat([df1_processed, df2_processed], ignore_index=True)
            
            print(f"📊 병합 후 데이터 형태: {merged_df.shape}")
            print(f"📊 병합 후 컬럼: {list(merged_df.columns)}")
            print(f"📊 병합 후 데이터 샘플:")
            sample_data = merged_df.head(10)
            for idx, row in sample_data.iterrows():
                print(f"   {row['sido_name']} {row['sigungu_name']} | {row['spot_name']} | {row['category']} | {row['source_file']}")
            
            return merged_df
            
        except Exception as e:
            print(f"❌ 데이터 병합 실패: {e}")
            raise

    def save_to_database(self, df, replace_all=False):
        """데이터를 데이터베이스에 저장"""
        try:
            print("💾 데이터베이스에 저장 중...")
            
            if replace_all:
                # 기존 데이터 삭제 (전체 교체 모드)
                print("🗑️ 기존 데이터 삭제 중...")
                with self.engine.connect() as connection:
                    connection.execute(text("DELETE FROM popular_tourist_spots"))
                    connection.commit()
                print("✅ 기존 데이터 삭제 완료")
            else:
                # 중복 데이터 확인 및 제거
                print("🔍 중복 데이터 확인 중...")
                with self.engine.connect() as connection:
                    for _, row in df.iterrows():
                        delete_sql = """
                        DELETE FROM popular_tourist_spots 
                        WHERE spot_name = :spot_name AND source_file = :source_file
                        """
                        connection.execute(text(delete_sql), 
                                         {
                                             'spot_name': row['spot_name'], 
                                             'source_file': row['source_file']
                                         })
                    connection.commit()
                print("✅ 중복 데이터 정리 완료")
            
            # 테이블에 데이터 삽입
            df.to_sql('popular_tourist_spots', 
                     con=self.engine, 
                     if_exists='append', 
                     index=False, 
                     method='multi',
                     chunksize=1000)
            
            print(f"✅ {len(df)}건의 데이터가 성공적으로 저장되었습니다.")
            
            # 저장된 데이터 확인
            with self.engine.connect() as connection:
                result = connection.execute(text("SELECT COUNT(*) as total FROM popular_tourist_spots"))
                total_count = result.fetchone()[0]
                print(f"📊 총 저장된 데이터: {total_count}건")
                
                # 시도별 데이터 수 확인
                sido_count_sql = """
                SELECT sido_name, COUNT(*) as count 
                FROM popular_tourist_spots 
                GROUP BY sido_name 
                ORDER BY count DESC
                """
                sido_result = connection.execute(text(sido_count_sql))
                print(f"📊 시도별 데이터 수:")
                for row in sido_result:
                    print(f"   {row[0]}: {row[1]}건")
                
                # 출처 파일별 데이터 수 확인
                source_count_sql = """
                SELECT source_file, COUNT(*) as count 
                FROM popular_tourist_spots 
                GROUP BY source_file 
                ORDER BY count DESC
                """
                source_result = connection.execute(text(source_count_sql))
                print(f"📊 출처 파일별 데이터 수:")
                for row in source_result:
                    print(f"   {row[0]}: {row[1]}건")
                
        except Exception as e:
            print(f"❌ 데이터베이스 저장 실패: {e}")
            raise

    def run_import(self, csv_path1, csv_path2):
        """전체 임포트 프로세스 실행"""
        try:
            print("🚀 인기관광지 데이터 임포트 시작")
            print("=" * 50)
            
            # # 1. 테이블 생성
            # self.create_table()
            
            # 2. 첫 번째 CSV 데이터 로드 (세대별 인기관광지)
            df1 = self.load_csv_data(csv_path1, "세대별 인기관광지(전체)")
            
            # 3. 두 번째 CSV 데이터 로드 (세대별 핫플레이스)
            df2 = self.load_csv_data(csv_path2, "세대별 핫플레이스(전체)")
            
            # 4. 데이터 병합 및 처리
            merged_df = self.merge_and_process_data(df1, df2)
            
            # 5. 데이터베이스에 저장 (추가 모드)
            self.save_to_database(merged_df, replace_all=False)
            
            print("=" * 50)
            print("🎉 모든 데이터 임포트가 완료되었습니다!")
            
        except Exception as e:
            print(f"❌ 임포트 실패: {e}")
            raise

    def run_import_replace_all(self, csv_path1, csv_path2):
        """전체 임포트 프로세스 실행 (전체 교체 모드)"""
        try:
            print("🚀 인기관광지 데이터 임포트 시작 (전체 교체 모드)")
            print("=" * 50)
            
            # 1. 테이블 생성
            self.create_table()
            
            # 2. 첫 번째 CSV 데이터 로드 (세대별 인기관광지)
            df1 = self.load_csv_data(csv_path1, "세대별 인기관광지(전체)")
            
            # 3. 두 번째 CSV 데이터 로드 (세대별 핫플레이스)
            df2 = self.load_csv_data(csv_path2, "세대별 핫플레이스(전체)")
            
            # 4. 데이터 병합 및 처리
            merged_df = self.merge_and_process_data(df1, df2)
            
            # 5. 데이터베이스에 저장 (전체 교체 모드)
            self.save_to_database(merged_df, replace_all=True)
            
            print("=" * 50)
            print("🎉 모든 데이터 임포트가 완료되었습니다! (전체 교체)")
            
        except Exception as e:
            print(f"❌ 임포트 실패: {e}")
            raise

def main():
    """메인 실행 함수"""
    try:
        # CSV 파일 경로
        csv_path1 = "csv/20250817000730_세대별 인기관광지(전체).csv"
        csv_path2 = "csv/20250817000736_세대별 핫플레이스(전체).csv"
        
        # 사용자 모드 선택
        print("📝 실행 모드를 선택하세요:")
        print("1. 추가 모드 (기존 데이터 유지하고 새 데이터 추가)")
        print("2. 전체 교체 모드 (기존 데이터 삭제 후 새 데이터로 교체)")
        
        choice = input("선택 (1 또는 2): ").strip()
        
        # 임포터 인스턴스 생성
        importer = PopularTouristSpotsImporter()
        
        if choice == "1":
            print("✅ 추가 모드로 실행합니다.")
            importer.run_import(csv_path1, csv_path2)
        elif choice == "2":
            print("✅ 전체 교체 모드로 실행합니다.")
            importer.run_import_replace_all(csv_path1, csv_path2)
        else:
            print("⚠️ 잘못된 선택입니다. 기본값(추가 모드)로 실행합니다.")
            importer.run_import(csv_path1, csv_path2)
        
    except Exception as e:
        print(f"❌ 프로그램 실행 실패: {e}")
        return 1
    
    return 0

if __name__ == "__main__":
    exit(main())
