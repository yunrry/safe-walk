import os
import json
import mysql.connector
from sqlalchemy import create_engine, text
from dotenv import load_dotenv
import logging

# 로깅 설정
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

class EMDImporter:
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
        
        # SQLAlchemy 엔진 생성 (연결 풀 및 타임아웃 설정)
        connection_string = f"mysql+mysqlconnector://{self.db_config['user']}:{self.db_config['password']}@{self.db_config['host']}:{self.db_config['port']}/{self.db_config['database']}?charset={self.db_config['charset']}"
        self.engine = create_engine(
            connection_string,
            pool_size=10,
            max_overflow=20,
            pool_pre_ping=True,
            pool_recycle=3600,
            connect_args={
                'connect_timeout': 60,
                'read_timeout': 300,
                'write_timeout': 300
            }
        )

    def create_table(self):
        """데이터베이스 테이블 생성"""
        try:
            print("🏗️ EMD 테이블 생성 중...")
            
            create_table_sql = """
            CREATE TABLE IF NOT EXISTS emd_data (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                EMD_CD VARCHAR(20) NOT NULL COMMENT '읍면동코드',
                EMD_ENG_NM VARCHAR(200) NOT NULL COMMENT '읍면동영문명',
                EMD_KOR_NM VARCHAR(200) NOT NULL COMMENT '읍면동한글명',
                Polygon JSON NOT NULL COMMENT '폴리곤좌표',
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                INDEX idx_emd_cd (EMD_CD),
                INDEX idx_emd_kor_nm (EMD_KOR_NM),
                INDEX idx_emd_eng_nm (EMD_ENG_NM),
                UNIQUE KEY uk_emd_cd (EMD_CD)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='읍면동 경계 데이터';
            """
            
            with self.engine.connect() as connection:
                connection.execute(text(create_table_sql))
                connection.commit()
            
            print("✅ EMD 테이블 생성 완료")
            
        except Exception as e:
            print(f"❌ 테이블 생성 실패: {e}")
            raise

    def load_json_data(self, json_path):
        """JSON 파일 로드 및 전처리"""
        try:
            print(f"📁 JSON 파일 로드 중: {json_path}")
            
            with open(json_path, 'r', encoding='utf-8') as f:
                data = json.load(f)
            
            print(f"📊 JSON 데이터 타입: {data.get('type')}")
            print(f"📊 Features 개수: {len(data.get('features', []))}")
            
            # GeoJSON features에서 필요한 데이터 추출
            processed_data = []
            
            for feature in data.get('features', []):
                properties = feature.get('properties', {})
                geometry = feature.get('geometry', {})
                
                # 필요한 컬럼 추출
                emd_record = {
                    'EMD_CD': properties.get('EMD_CD'),
                    'EMD_ENG_NM': properties.get('EMD_ENG_NM'),
                    'EMD_KOR_NM': properties.get('EMD_KOR_NM'),
                    'Polygon': json.dumps(geometry) if geometry else None
                }
                
                # 필수 데이터가 있는지 확인
                if all([emd_record['EMD_CD'], emd_record['EMD_ENG_NM'], emd_record['EMD_KOR_NM']]):
                    processed_data.append(emd_record)
                else:
                    print(f"⚠️ 불완전한 데이터 건너뛰기: {properties}")
            
            print(f"🔍 처리된 데이터 건수: {len(processed_data)}")
            
            # 샘플 데이터 출력
            if processed_data:
                print("📊 샘플 데이터:")
                sample = processed_data[0]
                print(f"   EMD_CD: {sample['EMD_CD']}")
                print(f"   EMD_ENG_NM: {sample['EMD_ENG_NM']}")
                print(f"   EMD_KOR_NM: {sample['EMD_KOR_NM']}")
                print(f"   Polygon: {sample['Polygon'][:100]}..." if sample['Polygon'] else "   Polygon: None")
            
            return processed_data
            
        except Exception as e:
            print(f"❌ JSON 로드 실패: {e}")
            raise

    def save_to_database(self, data):
        """데이터를 데이터베이스에 저장"""
        try:
            print("💾 데이터베이스에 저장 중...")
            
            # 기존 데이터 삭제 (테이블 초기화)
            print("🗑️ 기존 데이터 삭제 중...")
            with self.engine.connect() as connection:
                connection.execute(text("DELETE FROM emd_data"))
                connection.commit()
            print("✅ 기존 데이터 삭제 완료")
            
            # 배치 크기 설정
            batch_size = 100
            total_batches = len(data) // batch_size + (1 if len(data) % batch_size else 0)
            
            print(f"📦 총 {len(data)}건을 {batch_size}개씩 {total_batches}개 배치로 처리합니다.")
            
            # 데이터 삽입
            insert_sql = """
            INSERT INTO emd_data (EMD_CD, EMD_ENG_NM, EMD_KOR_NM, Polygon)
            VALUES (:EMD_CD, :EMD_ENG_NM, :EMD_KOR_NM, :Polygon)
            """
            
            # 배치별로 데이터 삽입
            for i in range(0, len(data), batch_size):
                batch_data = data[i:i + batch_size]
                batch_num = i // batch_size + 1
                
                print(f"📝 배치 {batch_num}/{total_batches} 처리 중... ({len(batch_data)}건)")
                
                try:
                    with self.engine.connect() as connection:
                        connection.execute(text(insert_sql), batch_data)
                        connection.commit()
                    
                    print(f"✅ 배치 {batch_num} 완료")
                    
                except Exception as batch_error:
                    print(f"❌ 배치 {batch_num} 실패: {batch_error}")
                    # 개별 레코드로 재시도
                    print(f"🔄 배치 {batch_num}를 개별 레코드로 재시도 중...")
                    
                    for record in batch_data:
                        try:
                            with self.engine.connect() as connection:
                                connection.execute(text(insert_sql), [record])
                                connection.commit()
                        except Exception as record_error:
                            print(f"⚠️ 레코드 건너뛰기 - EMD_CD: {record.get('EMD_CD')}, 오류: {record_error}")
                            continue
            
            print(f"✅ 데이터 저장 프로세스가 완료되었습니다.")
            
            # 저장된 데이터 확인
            with self.engine.connect() as connection:
                result = connection.execute(text("SELECT COUNT(*) as total FROM emd_data"))
                total_count = result.fetchone()[0]
                print(f"📊 총 저장된 데이터: {total_count}건")
                
        except Exception as e:
            print(f"❌ 데이터베이스 저장 실패: {e}")
            raise

    def run_import(self, json_path):
        """전체 임포트 프로세스 실행"""
        try:
            print("🚀 EMD 데이터 임포트 시작")
            print("=" * 50)
            
            # 1. 테이블 생성
            self.create_table()
            
            # 2. JSON 데이터 로드 및 전처리
            data = self.load_json_data(json_path)
            
            # 3. 데이터베이스에 저장
            self.save_to_database(data)
            
            print("=" * 50)
            print("🎉 모든 데이터 임포트가 완료되었습니다!")
            
        except Exception as e:
            print(f"❌ 임포트 실패: {e}")
            raise

def main():
    """메인 실행 함수"""
    try:
        # JSON 파일 경로
        json_path = "python/emd.json"
        
        # 임포터 인스턴스 생성 및 실행
        importer = EMDImporter()
        importer.run_import(json_path)
        
    except Exception as e:
        print(f"❌ 프로그램 실행 실패: {e}")
        return 1
    
    return 0

if __name__ == "__main__":
    exit(main())