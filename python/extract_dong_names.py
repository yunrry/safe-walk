import json
import os
from pathlib import Path

def extract_dong_names_from_geojson(geojson_file_path, output_txt_path=None):
    """
    GeoJSON 파일에서 동이름을 추출하여 txt 파일로 저장
    
    Parameters:
    geojson_file_path (str): GeoJSON 파일 경로
    output_txt_path (str): 출력 txt 파일 경로 (None이면 자동 생성)
    
    Returns:
    list: 추출된 동이름 리스트
    """
    
    try:
        print(f"📂 GeoJSON 파일 읽는 중: {geojson_file_path}")
        
        # GeoJSON 파일 읽기
        with open(geojson_file_path, 'r', encoding='utf-8') as f:
            geojson_data = json.load(f)
        
        print(f"✅ GeoJSON 파일 로드 완료")
        
        # 동이름 추출
        dong_names = []
        
        if 'features' in geojson_data:
            features = geojson_data['features']
            print(f"📊 총 피처 개수: {len(features)}")
            
            for i, feature in enumerate(features):
                if 'properties' in feature:
                    properties = feature['properties']
                    
                    # 동이름이 포함된 가능한 컬럼들 확인
                    dong_name = None
                    
                    # 일반적인 동이름 컬럼들 (우선순위 순서대로)
                    possible_columns = [
                        'EMD_KOR_NM', 'emd_kor_nm',  # 한국어 읍면동명 (최우선)
                        'EMD_NM', 'emd_nm', 'EMDNM', 'emdnm',  # 읍면동명
                        'DONG_NM', 'dong_nm', 'DONGNM', 'dongnm',  # 동명
                        'ADM_NM', 'adm_nm', 'ADMNM', 'admnm',  # 행정명
                        'NAME', 'name', 'Name',  # 이름
                        # 'EMD_CD', 'emd_cd', 'EMDCD', 'emdcd'  # 읍면동코드 (제외)
                    ]
                    
                    for col in possible_columns:
                        if col in properties and properties[col]:
                            dong_name = str(properties[col]).strip()
                            break
                    
                    # 동이름을 찾지 못한 경우 모든 속성 출력하여 확인
                    if dong_name is None:
                        if i < 3:  # 처음 3개 피처만 상세 출력
                            print(f"🔍 피처 {i+1} 속성:")
                            for key, value in properties.items():
                                print(f"   {key}: {value}")
                    
                    if dong_name and dong_name not in dong_names:
                        dong_names.append(dong_name)
                        
                        # 진행상황 표시 (100개마다)
                        if len(dong_names) % 100 == 0:
                            print(f"   📝 동이름 {len(dong_names)}개 추출 완료...")
        
        print(f"🎯 총 추출된 동이름: {len(dong_names)}개")
        
        # 동이름 정렬
        dong_names.sort()
        
        # 출력 파일 경로 설정
        if output_txt_path is None:
            base_path = Path(geojson_file_path).stem
            output_txt_path = f"{base_path}_dong_names.txt"
        
        # txt 파일로 저장
        with open(output_txt_path, 'w', encoding='utf-8') as f:
            f.write(f"# {Path(geojson_file_path).name}에서 추출된 동이름 목록\n")
            f.write(f"# 총 {len(dong_names)}개\n")
            f.write("=" * 50 + "\n\n")
            
            for i, dong_name in enumerate(dong_names, 1):
                f.write(f"{i:4d}. {dong_name}\n")
        
        print(f"💾 동이름 목록 저장됨: {output_txt_path}")
        
        # 샘플 출력
        print(f"\n📋 추출된 동이름 샘플 (상위 10개):")
        for i, dong_name in enumerate(dong_names[:10], 1):
            print(f"   {i:2d}. {dong_name}")
        
        if len(dong_names) > 10:
            print(f"   ... (총 {len(dong_names)}개)")
        
        return dong_names
        
    except Exception as e:
        print(f"❌ 오류 발생: {str(e)}")
        return None

def analyze_geojson_structure(geojson_file_path):
    """
    GeoJSON 파일의 구조를 분석하여 동이름 컬럼 찾기
    
    Parameters:
    geojson_file_path (str): GeoJSON 파일 경로
    """
    
    try:
        print(f"🔍 GeoJSON 파일 구조 분석 중: {geojson_file_path}")
        
        with open(geojson_file_path, 'r', encoding='utf-8') as f:
            geojson_data = json.load(f)
        
        if 'features' in geojson_data and len(geojson_data['features']) > 0:
            first_feature = geojson_data['features'][0]
            
            print(f"📊 첫 번째 피처 분석:")
            print(f"   🗺️  지오메트리 타입: {first_feature.get('geometry', {}).get('type', 'N/A')}")
            
            if 'properties' in first_feature:
                properties = first_feature['properties']
                print(f"   📋 속성 컬럼 ({len(properties)}개):")
                
                for key, value in properties.items():
                    value_type = type(value).__name__
                    value_preview = str(value)[:50] + "..." if len(str(value)) > 50 else str(value)
                    print(f"      • {key}: {value_type} = {value_preview}")
            else:
                print("   ❌ properties가 없습니다.")
        else:
            print("❌ features를 찾을 수 없습니다.")
            
    except Exception as e:
        print(f"❌ 분석 오류: {str(e)}")

def main():
    """메인 실행 함수"""
    
    # GeoJSON 파일 경로
    geojson_file = "python/emd.json"
    
    # 파일 존재 확인
    if not os.path.exists(geojson_file):
        print(f"❌ 파일을 찾을 수 없습니다: {geojson_file}")
        print("💡 먼저 shp_to_jaon.py를 실행하여 emd.json 파일을 생성하세요.")
        return
    
    print("🚀 동이름 추출 시작")
    print("=" * 50)
    
    # 1. GeoJSON 구조 분석
    analyze_geojson_structure(geojson_file)
    
    print("\n" + "=" * 50)
    
    # 2. 동이름 추출 및 저장
    dong_names = extract_dong_names_from_geojson(geojson_file)
    
    if dong_names:
        print(f"\n🎉 동이름 추출 완료!")
        print(f"📊 총 {len(dong_names)}개의 동이름을 추출했습니다.")
    else:
        print(f"\n❌ 동이름 추출에 실패했습니다.")

if __name__ == "__main__":
    main()
