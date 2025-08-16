import pandas as pd
import os
from collections import Counter

def extract_legal_dong(point_name):
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

def process_csv_file(csv_path, output_file):
    """
    CSV 파일을 읽어서 법정동을 추출하고 텍스트 파일로 저장하는 함수
    
    Args:
        csv_path (str): CSV 파일 경로
        output_file (str): 출력 텍스트 파일 경로
    """
    try:
        print(f"📁 CSV 파일 처리 중: {csv_path}")
        
        # CSV 파일 읽기
        df = pd.read_csv(csv_path, encoding='utf-8')
        
        print(f"📊 데이터 형태: {df.shape}")
        
        # 지점명 칼럼이 있는지 확인
        if '지점명' not in df.columns:
            print(f"❌ '{csv_path}'에 '지점명' 칼럼을 찾을 수 없습니다.")
            return None
        
        # 법정동 추출
        print("🔍 법정동 추출 중...")
        legal_dongs = []
        
        for idx, row in df.iterrows():
            point_name = row['지점명']
            legal_dong = extract_legal_dong(point_name)
            if legal_dong:
                legal_dongs.append(legal_dong)
        
        print(f"✅ 총 {len(legal_dongs)}개의 법정동 추출 완료")
        
        # 중복 제거 및 정렬
        unique_legal_dongs = sorted(list(set(legal_dongs)))
        print(f"📊 고유 법정동 수: {len(unique_legal_dongs)}개")
        
        # 빈도수 계산
        legal_dong_counts = Counter(legal_dongs)
        
        # 텍스트 파일로 저장
        with open(output_file, 'w', encoding='utf-8') as f:
            f.write(f"# {os.path.basename(csv_path)} - 법정동 목록\n")
            f.write(f"# 총 데이터: {len(df)}건\n")
            f.write(f"# 고유 법정동: {len(unique_legal_dongs)}개\n")
            f.write("=" * 50 + "\n\n")
            
            # 고유 법정동 목록
            f.write("## 고유 법정동 목록 (알파벳 순)\n")
            for legal_dong in unique_legal_dongs:
                f.write(f"{legal_dong}\n")
            
            f.write("\n" + "=" * 50 + "\n\n")
            
            # 빈도수별 정렬
            f.write("## 법정동별 빈도수 (내림차순)\n")
            for legal_dong, count in legal_dong_counts.most_common():
                f.write(f"{legal_dong}: {count}건\n")
        
        print(f"💾 텍스트 파일 저장 완료: {output_file}")
        
        return {
            'total_records': len(df),
            'total_legal_dongs': len(legal_dongs),
            'unique_legal_dongs': len(unique_legal_dongs),
            'legal_dong_counts': legal_dong_counts
        }
        
    except Exception as e:
        print(f"❌ 파일 처리 실패: {e}")
        return None

def main():
    """메인 실행 함수"""
    try:
        print("🚀 모든 CSV 파일에서 법정동 추출 시작")
        print("=" * 50)
        
        # CSV 파일 목록
        csv_files = [
            "csv/AccidentStatistics.csv",
            "csv/ElderlyPedestrianAccident.csv",
            "csv/HolidayAccident.csv",
            "csv/LocalGovernmentAccident.csv",
            "csv/PedestrianAccident.csv"
        ]
        
        # 결과 저장 디렉토리 생성
        output_dir = "extracted_legal_dongs"
        os.makedirs(output_dir, exist_ok=True)
        
        # 전체 통계
        total_stats = {}
        
        # 각 CSV 파일 처리
        for csv_file in csv_files:
            if os.path.exists(csv_file):
                # 출력 파일명 생성
                base_name = os.path.splitext(os.path.basename(csv_file))[0]
                output_file = f"{output_dir}/{base_name}_legal_dongs.txt"
                
                print(f"\n📋 처리 중: {csv_file}")
                stats = process_csv_file(csv_file, output_file)
                
                if stats:
                    total_stats[base_name] = stats
            else:
                print(f"⚠️ 파일을 찾을 수 없음: {csv_file}")
        
        # 전체 통계 요약 파일 생성
        summary_file = f"{output_dir}/00_전체_법정동_통계.txt"
        with open(summary_file, 'w', encoding='utf-8') as f:
            f.write("# 전체 CSV 파일 법정동 통계 요약\n")
            f.write("=" * 50 + "\n\n")
            
            for file_name, stats in total_stats.items():
                f.write(f"## {file_name}\n")
                f.write(f"- 총 데이터: {stats['total_records']}건\n")
                f.write(f"- 총 법정동: {stats['total_legal_dongs']}개\n")
                f.write(f"- 고유 법정동: {stats['unique_legal_dongs']}개\n")
                f.write("\n")
            
            # 전체 고유 법정동 수집
            all_legal_dongs = set()
            for stats in total_stats.values():
                all_legal_dongs.update(stats['legal_dong_counts'].keys())
            
            f.write(f"## 전체 통계\n")
            f.write(f"- 총 고유 법정동: {len(all_legal_dongs)}개\n")
            f.write(f"- 전체 데이터: {sum(stats['total_records'] for stats in total_stats.values())}건\n")
            
            # 전체 고유 법정동 목록
            f.write(f"\n## 전체 고유 법정동 목록 (알파벳 순)\n")
            for legal_dong in sorted(all_legal_dongs):
                f.write(f"{legal_dong}\n")
        
        print(f"\n🎉 모든 법정동 추출 완료!")
        print(f"📁 결과 파일 위치: {output_dir}/")
        print(f"📊 처리된 파일 수: {len(total_stats)}개")
        
        # 결과 요약 출력
        print(f"\n📋 처리 결과 요약:")
        for file_name, stats in total_stats.items():
            print(f"   {file_name}: {stats['unique_legal_dongs']}개 고유 법정동")
        
        total_unique = len(set().union(*[stats['legal_dong_counts'].keys() for stats in total_stats.values()]))
        print(f"   전체 고유 법정동: {total_unique}개")
        
    except Exception as e:
        print(f"❌ 프로그램 실행 실패: {e}")
        return 1
    
    return 0

if __name__ == "__main__":
    exit(main())
