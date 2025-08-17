import geopandas as gpd
import json
import os
from pathlib import Path

def shp_to_json(shp_file_path, output_json_path=None, encoding='utf-8'):
    """
    SHP 파일을 JSON (GeoJSON) 형식으로 변환
    
    Parameters:
    shp_file_path (str): SHP 파일 경로
    output_json_path (str): 출력 JSON 파일 경로 (None이면 자동 생성)
    encoding (str): 파일 인코딩 (기본값: utf-8, 한글의 경우 'cp949' 사용)
    
    Returns:
    dict: GeoJSON 형태의 딕셔너리
    """
    
    try:
        # SHP 파일 읽기
        print(f"📂 SHP 파일 읽는 중: {shp_file_path}")
        gdf = gpd.read_file(shp_file_path, encoding=encoding)
        
        # 기본 정보 출력
        print(f"✅ 성공적으로 로드됨!")
        print(f"📊 피처 개수: {len(gdf)}")
        print(f"🗺️ 좌표계: {gdf.crs}")
        print(f"📋 컬럼: {list(gdf.columns)}")
        
        # GeoJSON 형식으로 변환
        geojson = json.loads(gdf.to_json())
        
        # 출력 파일 경로 설정
        if output_json_path is None:
            output_json_path = str(Path(shp_file_path).with_suffix('.json'))
        
        # JSON 파일로 저장
        with open(output_json_path, 'w', encoding='utf-8') as f:
            json.dump(geojson, f, ensure_ascii=False, indent=2)
        
        print(f"💾 JSON 파일 저장됨: {output_json_path}")
        
        return geojson
        
    except Exception as e:
        print(f"❌ 오류 발생: {str(e)}")
        return None

def batch_shp_to_json(directory_path, encoding='utf-8'):
    """
    디렉토리 내의 모든 SHP 파일을 JSON으로 일괄 변환
    
    Parameters:
    directory_path (str): SHP 파일들이 있는 디렉토리 경로
    encoding (str): 파일 인코딩
    """
    
    directory = Path(directory_path)
    shp_files = list(directory.glob("*.shp"))
    
    if not shp_files:
        print("❌ SHP 파일을 찾을 수 없습니다.")
        return
    
    print(f"📁 {len(shp_files)}개의 SHP 파일을 발견했습니다.")
    
    for shp_file in shp_files:
        print(f"\n🔄 처리 중: {shp_file.name}")
        shp_to_json(str(shp_file), encoding=encoding)

def analyze_shp_file(shp_file_path, encoding='utf-8'):
    """
    SHP 파일의 구조와 내용을 분석
    
    Parameters:
    shp_file_path (str): SHP 파일 경로
    encoding (str): 파일 인코딩
    """
    
    try:
        gdf = gpd.read_file(shp_file_path, encoding=encoding)
        
        print("=" * 50)
        print(f"📊 SHP 파일 분석: {shp_file_path}")
        print("=" * 50)
        
        # 기본 정보
        print(f"🗺️  좌표계: {gdf.crs}")
        print(f"📐 지오메트리 타입: {gdf.geom_type.unique()}")
        print(f"📊 피처 개수: {len(gdf)}")
        print(f"📏 경계 범위: {gdf.total_bounds}")
        
        # 컬럼 정보
        print(f"\n📋 속성 컬럼 ({len(gdf.columns)-1}개):")
        for col in gdf.columns:
            if col != 'geometry':
                dtype = gdf[col].dtype
                null_count = gdf[col].isnull().sum()
                print(f"   • {col}: {dtype} (결측값: {null_count})")
        
        # 샘플 데이터
        print(f"\n📄 샘플 데이터 (상위 3개):")
        for col in gdf.columns:
            if col != 'geometry':
                print(f"   {col}: {list(gdf[col].head(3))}")
        
        return gdf
        
    except Exception as e:
        print(f"❌ 분석 오류: {str(e)}")
        return None

# 사용 예시
if __name__ == "__main__":
    # 방법 1: 단일 파일 변환
    shp_file = "python/emd.shp"  # 실제 파일 경로로 변경
    
    # 한글이 포함된 파일의 경우 encoding='cp949' 사용
    geojson_data = shp_to_json(shp_file, encoding='cp949')
    
    # 방법 2: 파일 분석 후 변환
    # analyze_shp_file(shp_file)
    # geojson_data = shp_to_json(shp_file)
    
    # 방법 3: 디렉토리 내 모든 SHP 파일 일괄 변환
    # batch_shp_to_json("./shapefiles/")
    
    print("🎉 변환 완료!")

# 필요한 라이브러리 설치 명령어:
# pip install geopandas
# 또는
# pip install fiona shapely pyproj

# fiona만 사용하는 간단한 버전
def simple_shp_to_json(shp_file_path, output_json_path=None):
    """
    fiona만 사용한 간단한 변환 함수
    """
    import fiona
    from fiona.crs import from_epsg
    
    features = []
    
    with fiona.open(shp_file_path) as src:
        for feature in src:
            features.append(feature)
    
    geojson = {
        "type": "FeatureCollection",
        "features": features
    }
    
    if output_json_path is None:
        output_json_path = shp_file_path.replace('.shp', '.json')
    
    with open(output_json_path, 'w', encoding='utf-8') as f:
        json.dump(geojson, f, ensure_ascii=False, indent=2)
    
    print(f"✅ 변환 완료: {output_json_path}")
    return geojson