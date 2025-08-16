import pandas as pd
from sqlalchemy import text

def extract_legal_dong(point_name):
    """
    지점명에서 법정동을 추출하는 공통 함수
    
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

def add_legal_dong_column_to_table(engine, table_name, database_name):
    """
    기존 테이블에 법정동 칼럼을 추가하는 공통 함수
    
    Args:
        engine: SQLAlchemy 엔진
        table_name: 테이블명
        database_name: 데이터베이스명
    """
    try:
        print(f"🔧 {table_name} 테이블에 법정동 칼럼 추가 확인 중...")
        
        # 테이블 구조 확인
        check_column_sql = """
        SELECT COLUMN_NAME 
        FROM INFORMATION_SCHEMA.COLUMNS 
        WHERE TABLE_SCHEMA = :database 
        AND TABLE_NAME = :table_name 
        AND COLUMN_NAME = 'legal_dong'
        """
        
        with engine.connect() as connection:
            result = connection.execute(text(check_column_sql), 
                                     {"database": database_name, "table_name": table_name})
            column_exists = result.fetchone() is not None
            
            if not column_exists:
                print(f"➕ {table_name} 테이블에 법정동 칼럼 추가 중...")
                
                add_column_sql = f"""
                ALTER TABLE {table_name} 
                ADD COLUMN legal_dong VARCHAR(50) COMMENT '법정동' AFTER point_name
                """
                
                connection.execute(text(add_column_sql))
                connection.commit()
                
                # 인덱스 추가
                add_index_sql = f"""
                ALTER TABLE {table_name} 
                ADD INDEX idx_legal_dong (legal_dong)
                """
                
                connection.execute(text(add_index_sql))
                connection.commit()
                
                print(f"✅ {table_name} 테이블에 법정동 칼럼 추가 완료")
            else:
                print(f"✅ {table_name} 테이블에 법정동 칼럼이 이미 존재합니다")
                
    except Exception as e:
        print(f"⚠️ {table_name} 테이블에 법정동 칼럼 추가 중 오류: {e}")

def process_legal_dong_for_dataframe(df, point_name_column='point_name'):
    """
    데이터프레임에 법정동 칼럼을 추가하는 공통 함수
    
    Args:
        df: pandas DataFrame
        point_name_column: 지점명 칼럼명 (기본값: 'point_name')
    
    Returns:
        법정동 칼럼이 추가된 DataFrame
    """
    try:
        if point_name_column not in df.columns:
            print(f"⚠️ '{point_name_column}' 칼럼을 찾을 수 없습니다.")
            return df
        
        print("🔍 법정동 추출 중...")
        
        # 법정동 칼럼 추가
        df['legal_dong'] = df[point_name_column].apply(extract_legal_dong)
        
        # 추출 결과 샘플 출력
        print(f"📊 추출된 법정동 샘플:")
        sample_data = df[[point_name_column, 'legal_dong']].head(5)
        for idx, row in sample_data.iterrows():
            print(f"   {row[point_name_column]} -> {row['legal_dong']}")
        
        # 통계 정보 출력
        total_count = len(df)
        valid_count = df['legal_dong'].notna().sum()
        print(f"📊 총 데이터: {total_count}건")
        print(f"📊 법정동 추출 성공: {valid_count}건")
        print(f"📊 법정동 추출 실패: {total_count - valid_count}건")
        
        return df
        
    except Exception as e:
        print(f"⚠️ 법정동 처리 중 오류: {e}")
        return df

def get_legal_dong_sample_data():
    """
    법정동 추출 테스트를 위한 샘플 데이터 반환
    """
    sample_data = [
        "서울특별시 종로구 종로2가(종로2가교차로 부근)",
        "서울특별시 동대문구 제기동(용두교 부근)",
        "서울특별시 강남구 역삼동(강남대로 부근)",
        "서울특별시 서초구 서초동(서초역 부근)",
        "서울특별시 송파구 문정동(문정역 부근)"
    ]
    
    print("🧪 법정동 추출 테스트:")
    for point_name in sample_data:
        legal_dong = extract_legal_dong(point_name)
        print(f"   '{point_name}' -> '{legal_dong}'")
    
    return sample_data
