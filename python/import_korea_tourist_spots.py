import os
import pandas as pd
import mysql.connector
from sqlalchemy import create_engine, text
from dotenv import load_dotenv
import logging
import re
import glob

# 로깅 설정
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

class KoreaTouristSpotsImporter:
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
        
        # CSV 컬럼 매핑
        self.column_mapping = {
            '순위': 'rank',
            '관광지ID': 'tourist_spot_id',
            '관광지명': 'spot_name',
            '분류': 'category'
        }
        # self.column_mapping = {
        #     '순위': 'rank',
        #     '관광지ID': 'tourist_spot_id',
        #     '업소명': 'spot_name',
        #     '분류': 'category'
        # }


    def parse_filename(self, filename):
        """파일명에서 시도명, 모드, 년월을 추출"""
        try:
            # 파일명 패턴: "2025MMDDHHMMSS_{sido_name}_{mode}_전체.csv"
            # 예: "20250821191938_강원도_인기관광지_전체.csv"
            
            # 파일명에서 확장자 제거
            name_without_ext = os.path.splitext(filename)[0]
            
            # 언더스코어로 분리
            parts = name_without_ext.split('_')
            
            if len(parts) >= 4:
                # 타임스탬프에서 년월 추출 (첫 번째 부분)
                timestamp = parts[0]
                if len(timestamp) >= 6:
                    year = timestamp[:4]  # 2025
                    month = timestamp[4:6]  # 08
                    base_year_month = f"{year}{month}"  # 202508
                else:
                    base_year_month = None
                
                # 시도명은 두 번째 부분 (인덱스 1)
                sido_name = parts[1]
                # 모드는 세 번째 부분 (인덱스 2)
                mode = parts[2]
                
                return sido_name, mode, base_year_month
            else:
                logger.warning(f"파일명 형식이 올바르지 않습니다: {filename}")
                return None, None, None
                
        except Exception as e:
            logger.error(f"파일명 파싱 중 오류 발생: {filename}, 오류: {e}")
            return None, None, None

    def load_csv_data(self, csv_path):
        """CSV 파일 로드 및 전처리"""
        try:
            filename = os.path.basename(csv_path)
            sido_name, mode, base_year_month = self.parse_filename(filename)
            
            if not sido_name or not mode:
                logger.error(f"파일명에서 시도명 또는 모드를 추출할 수 없습니다: {filename}")
                return None
            
            logger.info(f"📁 CSV 파일 로드 중: {csv_path}")
            logger.info(f"📍 시도명: {sido_name}, 모드: {mode}, 기준년월: {base_year_month}")
            
            # CSV 파일 읽기 (한글 인코딩 처리)
            df = pd.read_csv(csv_path, encoding='utf-8')
            
            logger.info(f"📊 원본 데이터 형태: {df.shape}")
            logger.info(f"📋 원본 컬럼: {list(df.columns)}")
            
            # 컬럼명 변경
            df = df.rename(columns=self.column_mapping)
            
            # 추가 컬럼 설정
            df['sido_name'] = sido_name
            df['mode'] = mode
            df['source_file'] = f"{mode}(전체)"
            df['base_year_month'] = base_year_month
            
            # 데이터 타입 변환
            try:
                # rank 컬럼을 정수형으로 변환
                if 'rank' in df.columns:
                    df['rank'] = pd.to_numeric(df['rank'], errors='coerce').fillna(0).astype(int)
                
                # 문자열 컬럼 처리 (빈 값은 None으로 변환)
                string_columns = ['spot_name', 'tourist_spot_id', 'category', 'sido_name', 'mode', 'source_file']
                for col in string_columns:
                    if col in df.columns:
                        df[col] = df[col].replace('', None)
                
            except Exception as e:
                logger.warning(f"⚠️ 데이터 타입 변환 중 오류: {e}")
            
            # NaN 값을 None으로 변환 (MySQL에서 오류 방지)
            df = df.where(pd.notnull(df), None)
            
            # 필요한 컬럼만 선택
            required_columns = [
                'rank', 'tourist_spot_id', 'spot_name', 'category',
                'sido_name', 'mode', 'source_file'
            ]
            
            # 존재하는 컬럼만 선택
            existing_columns = [col for col in required_columns if col in df.columns]
            df = df[existing_columns]
            
            logger.info(f"🔍 전처리 후 데이터 형태: {df.shape}")
            logger.info(f"📋 전처리 후 컬럼: {list(df.columns)}")
            
            # 데이터 샘플 출력
            logger.info(f"📊 전처리 후 데이터 샘플:")
            sample_data = df.head(5)
            for idx, row in sample_data.iterrows():
                logger.info(f"   {row['rank']} | {row['spot_name']} | {row['category']} | {row['sido_name']}")
            
            return df
            
        except Exception as e:
            logger.error(f"❌ CSV 로드 실패: {csv_path}, 오류: {e}")
            return None

    def save_to_database(self, df, replace_all=False):
        """데이터를 데이터베이스에 저장"""
        try:
            if df is None or df.empty:
                logger.warning("저장할 데이터가 없습니다.")
                return
            
            logger.info("�� 데이터베이스에 저장 중...")
            
            if replace_all:
                # 기존 데이터 삭제 (전체 교체 모드)
                logger.info("��️ 기존 데이터 삭제 중...")
                with self.engine.connect() as connection:
                    connection.execute(text("DELETE FROM popular_tourist_spots"))
                    connection.commit()
                logger.info("✅ 기존 데이터 삭제 완료")
            else:
                # 중복 데이터 확인 및 제거
                logger.info("🔍 중복 데이터 확인 중...")
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
                logger.info("✅ 중복 데이터 정리 완료")
            
            # 테이블에 데이터 삽입
            df.to_sql('popular_tourist_spots', 
                     con=self.engine, 
                     if_exists='append', 
                     index=False, 
                     method='multi',
                     chunksize=1000)
            
            logger.info(f"✅ {len(df)}건의 데이터가 성공적으로 저장되었습니다.")
            
            # 저장된 데이터 확인
            with self.engine.connect() as connection:
                result = connection.execute(text("SELECT COUNT(*) as total FROM popular_tourist_spots"))
                total_count = result.fetchone()[0]
                logger.info(f"📊 총 저장된 데이터: {total_count}건")
                
                # 시도별 데이터 수 확인
                sido_count_sql = """
                SELECT sido_name, COUNT(*) as count 
                FROM popular_tourist_spots 
                GROUP BY sido_name 
                ORDER BY count DESC
                """
                sido_result = connection.execute(text(sido_count_sql))
                logger.info(f"📊 시도별 데이터 수:")
                for row in sido_result:
                    logger.info(f"   {row[0]}: {row[1]}건")
                
                # 모드별 데이터 수 확인
                mode_count_sql = """
                SELECT mode, COUNT(*) as count 
                FROM popular_tourist_spots 
                GROUP BY mode 
                ORDER BY count DESC
                """
                mode_result = connection.execute(text(mode_count_sql))
                logger.info(f"📊 모드별 데이터 수:")
                for row in mode_result:
                    logger.info(f"   {row[0]}: {row[1]}건")
                
        except Exception as e:
            logger.error(f"❌ 데이터베이스 저장 실패: {e}")
            raise

    def process_all_csv_files(self, csv_directory, replace_all=False):
        """지정된 디렉토리의 모든 CSV 파일을 처리"""
        try:
            logger.info(f"�� 한국관광데이터랩 인기관광지 데이터 임포트 시작")
            logger.info(f"📁 처리할 디렉토리: {csv_directory}")
            logger.info("=" * 50)
            
            # CSV 파일 목록 가져오기
            csv_pattern = os.path.join(csv_directory, "*.csv")
            csv_files = glob.glob(csv_pattern)
            
            if not csv_files:
                logger.warning(f"⚠️ 지정된 디렉토리에서 CSV 파일을 찾을 수 없습니다: {csv_directory}")
                return
            
            logger.info(f"�� 발견된 CSV 파일 수: {len(csv_files)}")
            
            # 각 CSV 파일 처리
            total_processed = 0
            total_records = 0
            
            for csv_file in csv_files:
                try:
                    logger.info(f"\n📁 파일 처리 중: {os.path.basename(csv_file)}")
                    
                    # CSV 데이터 로드
                    df = self.load_csv_data(csv_file)
                    
                    if df is not None and not df.empty:
                        # 데이터베이스에 저장
                        self.save_to_database(df, replace_all=False)  # 개별 파일은 추가 모드
                        total_processed += 1
                        total_records += len(df)
                        logger.info(f"✅ 파일 처리 완료: {os.path.basename(csv_file)}")
                    else:
                        logger.warning(f"⚠️ 파일에서 유효한 데이터를 추출할 수 없습니다: {csv_file}")
                        
                except Exception as e:
                    logger.error(f"❌ 파일 처리 실패: {csv_file}, 오류: {e}")
                    continue
            
            logger.info("=" * 50)
            logger.info(f"🎉 모든 CSV 파일 처리 완료!")
            logger.info(f"📊 처리된 파일 수: {total_processed}")
            logger.info(f"📊 총 저장된 레코드 수: {total_records}")
            
        except Exception as e:
            logger.error(f"❌ 전체 처리 실패: {e}")
            raise

def main():
    """메인 실행 함수"""
    try:
        # CSV 파일이 있는 디렉토리 경로
        csv_directory = "한국관광데이터랩/관광지데이터/인기관광지"
        csv_directory_center = "한국관광데이터랩/관광지데이터/중심관광지"
        csv_directory_food = "한국관광데이터랩/관광지데이터/지역맛집"
        
        # 사용자 모드 선택
        print("📝 실행 모드를 선택하세요:")
        print("1. 추가 모드 (기존 데이터 유지하고 새 데이터 추가)")
        print("2. 전체 교체 모드 (기존 데이터 삭제 후 새 데이터로 교체)")
        
        choice = input("선택 (1 또는 2): ").strip()
        
        # 임포터 인스턴스 생성
        importer = KoreaTouristSpotsImporter()
        
        if choice == "1":
            print("✅ 추가 모드로 실행합니다.")
            importer.process_all_csv_files(csv_directory, replace_all=False)
            importer.process_all_csv_files(csv_directory_center, replace_all=False)
            # importer.process_all_csv_files(csv_directory_food, replace_all=False)
        elif choice == "2":
            print("✅ 전체 교체 모드로 실행합니다.")
            importer.process_all_csv_files(csv_directory, replace_all=True)
        else:
            print("⚠️ 잘못된 선택입니다. 기본값(추가 모드)로 실행합니다.")
            importer.process_all_csv_files(csv_directory, replace_all=False)
        
    except Exception as e:
        print(f"❌ 프로그램 실행 실패: {e}")
        return 1
    
    return 0

if __name__ == "__main__":
    exit(main())
