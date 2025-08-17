import json
import os
from pathlib import Path

def extract_emd_codes_from_geojson(geojson_file_path, output_txt_path=None):
    """
    GeoJSON 파일에서 EMD_CD 값을 추출하여 txt 파일로 저장
    
    Parameters:
    geojson_file_path (str): GeoJSON 파일 경로
    output_txt_path (str): 출력 txt 파일 경로 (None이면 자동 생성)
    
    Returns:
    list: 추출된 EMD_CD 리스트
    """
    
    try:
        print(f"📂 GeoJSON 파일 읽는 중: {geojson_file_path}")
        
        # GeoJSON 파일 읽기
        with open(geojson_file_path, 'r', encoding='utf-8') as f:
            geojson_data = json.load(f)
        
        print(f"✅ GeoJSON 파일 로드 완료")
        
        # EMD_CD 추출
        emd_codes = []
        
        if 'features' in geojson_data:
            features = geojson_data['features']
            print(f"📊 총 피처 개수: {len(features)}")
            
            for i, feature in enumerate(features):
                if 'properties' in feature:
                    properties = feature['properties']
                    
                    # EMD_CD 값 추출
                    if 'EMD_CD' in properties and properties['EMD_CD']:
                        emd_cd = str(properties['EMD_CD']).strip()
                        if emd_cd not in emd_codes:
                            emd_codes.append(emd_cd)
                        
                        # 진행상황 표시 (100개마다)
                        if len(emd_codes) % 100 == 0:
                            print(f"   📝 EMD_CD {len(emd_codes)}개 추출 완료...")
        
        print(f"🎯 총 추출된 EMD_CD: {len(emd_codes)}개")
        
        # EMD_CD 정렬
        emd_codes.sort()
        
        # 출력 파일 경로 설정
        if output_txt_path is None:
            base_path = Path(geojson_file_path).stem
            output_txt_path = f"{base_path}_emd_codes.txt"
        
        # txt 파일로 저장
        with open(output_txt_path, 'w', encoding='utf-8') as f:
            f.write(f"# {Path(geojson_file_path).name}에서 추출된 EMD_CD 목록\n")
            f.write(f"# 총 {len(emd_codes)}개\n")
            f.write("=" * 50 + "\n\n")
            
            for i, emd_cd in enumerate(emd_codes, 1):
                f.write(f"{i:4d}. {emd_cd}\n")
        
        print(f"💾 EMD_CD 목록 저장됨: {output_txt_path}")
        
        # 샘플 출력
        print(f"\n📋 추출된 EMD_CD 샘플 (상위 10개):")
        for i, emd_cd in enumerate(emd_codes[:10], 1):
            print(f"   {i:2d}. {emd_cd}")
        
        if len(emd_codes) > 10:
            print(f"   ... (총 {len(emd_codes)}개)")
        
        return emd_codes
        
    except Exception as e:
        print(f"❌ 오류 발생: {str(e)}")
        return None

def main():
    """메인 실행 함수"""
    
    # GeoJSON 파일 경로
    geojson_file = "python/emd.json"
    
    # 파일 존재 확인
    if not os.path.exists(geojson_file):
        print(f"❌ 파일을 찾을 수 없습니다: {geojson_file}")
        return
    
    print("🚀 EMD_CD 추출 시작")
    print("=" * 50)
    
    # EMD_CD 추출 및 저장
    emd_codes = extract_emd_codes_from_geojson(geojson_file)
    
    if emd_codes:
        print(f"\n🎉 EMD_CD 추출 완료!")
        print(f"📊 총 {len(emd_codes)}개의 EMD_CD를 추출했습니다.")
    else:
        print(f"\n❌ EMD_CD 추출에 실패했습니다.")

if __name__ == "__main__":
    main()