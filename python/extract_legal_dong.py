import pandas as pd
import re
import os

def extract_legal_dong(point_name):
    """
    지점명에서 법정동을 추출하는 함수
    
    예시:
    - "서울특별시 종로구 종로2가(종로2가교차로 부근)" -> "종로2가"
    - "서울특별시 동대문구 제기동(용두교 부근)" -> "제기동"
    - "서울특별시 강남구 역삼동(강남대로 부근)" -> "역삼동"
    """
    try:
        if pd.isna(point_name) or point_name == '':
            return None
        
        # 공백으로 단어를 분리
        words = point_name.strip().split()
        
        # 최소 3개 단어가 있어야 함
        if len(words) < 3:
            return None
        
        # 3번째 단어 (인덱스 2)에서 괄호 부분 제거
        third_word = words[2]
        
        # 괄호가 있는 경우 괄호 앞부분만 추출
        if '(' in third_word:
            legal_dong = third_word.split('(')[0]
        else:
            legal_dong = third_word
        
        return legal_dong.strip()
        
    except Exception as e:
        print(f"⚠️ 지점명 '{point_name}' 처리 중 오류: {e}")
        return None

def process_csv_file(csv_path, output_path=None):
    """
    CSV 파일을 읽어서 법정동 칼럼을 추가하고 저장하는 함수
    
    Args:
        csv_path (str): 입력 CSV 파일 경로
        output_path (str): 출력 CSV 파일 경로 (None이면 원본 파일 덮어쓰기)
    """
    try:
        print(f"📁 CSV 파일 로드 중: {csv_path}")
        
        # CSV 파일 읽기
        df = pd.read_csv(csv_path, encoding='utf-8')
        
        print(f"📊 원본 데이터 형태: {df.shape}")
        print(f"📋 원본 컬럼: {list(df.columns)}")
        
        # 지점명 칼럼이 있는지 확인
        if '지점명' not in df.columns:
            print("❌ '지점명' 칼럼을 찾을 수 없습니다.")
            return False
        
        print("🔍 법정동 추출 중...")
        
        # 법정동 칼럼 추가
        df['법정동'] = df['지점명'].apply(extract_legal_dong)
        
        # 추출 결과 확인
        print(f"✅ 법정동 추출 완료")
        print(f"📊 추출된 법정동 샘플:")
        sample_data = df[['지점명', '법정동']].head(10)
        for idx, row in sample_data.iterrows():
            print(f"   {row['지점명']} -> {row['법정동']}")
        
        # 출력 파일 경로 설정
        if output_path is None:
            output_path = csv_path
        
        # CSV 파일 저장
        df.to_csv(output_path, index=False, encoding='utf-8')
        print(f"💾 파일 저장 완료: {output_path}")
        
        # 통계 정보 출력
        total_count = len(df)
        valid_count = df['법정동'].notna().sum()
        print(f"📊 총 데이터: {total_count}건")
        print(f"📊 법정동 추출 성공: {valid_count}건")
        print(f"📊 법정동 추출 실패: {total_count - valid_count}건")
        
        return True
        
    except Exception as e:
        print(f"❌ 파일 처리 실패: {e}")
        return False

def main():
    """메인 실행 함수"""
    try:
        # CSV 파일 경로
        csv_path = "csv/PedestrianAccident.csv"
        
        # 백업 파일 경로 (선택사항)
        backup_path = "csv/PedestrianAccident_backup.csv"
        
        print("🚀 지점명에서 법정동 추출 시작")
        print("=" * 50)
        
        # 1. 백업 파일 생성
        print("📋 백업 파일 생성 중...")
        if os.path.exists(csv_path):
            import shutil
            shutil.copy2(csv_path, backup_path)
            print(f"✅ 백업 파일 생성 완료: {backup_path}")
        
        # 2. 법정동 추출 및 칼럼 추가
        success = process_csv_file(csv_path)
        
        if success:
            print("=" * 50)
            print("🎉 법정동 추출이 완료되었습니다!")
            print(f"📁 원본 파일: {csv_path}")
            print(f"📁 백업 파일: {backup_path}")
        else:
            print("❌ 법정동 추출에 실패했습니다.")
            return 1
        
    except Exception as e:
        print(f"❌ 프로그램 실행 실패: {e}")
        return 1
    
    return 0

if __name__ == "__main__":
    exit(main())
