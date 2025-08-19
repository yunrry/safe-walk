# api_traffic_analyzer.py
import requests
import json
from typing import List, Dict, Optional
from TrafficAccidentAnalyzer import TrafficAccidentAnalyzer, analyze_new_data
from region_risk_analyzer import RegionRiskAnalyzer, analyze_region_risk

class ApiTrafficAccidentAnalyzer:
    """Spring Boot API와 연동하는 교통사고 분석기"""
    
    def __init__(self, base_url: str = "http://localhost:8080/api/v1"):
        self.base_url = base_url
        self.analyzer = None
        self.region_analyzer = None
    
    def fetch_emd_data_by_bounds(self, 
                                sw_lat: float = 33.0, sw_lng: float = 124.0,
                                ne_lat: float = 38.9, ne_lng: float = 132.0) -> List[Dict]:
        """
        지도 영역(bounds)으로 법정동 데이터 조회 - 대한민국 전체 범위
        
        Args:
            sw_lat: 남서쪽 위도 (기본값: 33.0 - 대한민국 최남단)
            sw_lng: 남서쪽 경도 (기본값: 124.0 - 대한민국 최서단)
            ne_lat: 북동쪽 위도 (기본값: 38.9 - 대한민국 최북단)
            ne_lng: 북동쪽 경도 (기본값: 132.0 - 대한민국 최동단)
        
        Returns:
            법정동 데이터 리스트
        """
        url = f"{self.base_url}/emd"
        params = {
            'swLat': sw_lat,
            'swLng': sw_lng,
            'neLat': ne_lat,
            'neLng': ne_lng
        }
        
        try:
            print(f"🌍 지역 데이터 조회 중...")
            print(f"   범위: SW({sw_lat}, {sw_lng}) ~ NE({ne_lat}, {ne_lng})")
            
            response = requests.get(url, params=params)
            response.raise_for_status()
            
            api_data = response.json()
            
            # API 응답을 분석기 형식으로 변환
            converted_data = []
            for item in api_data:
                converted_item = {
                    "name": item["name"],
                    "totalAccident": item["totalAccident"],
                    "EMD_CD": item["EMD_CD"],
                    "latitude": float(item["latitude"]),
                    "longitude": float(item["longitude"])
                }
                converted_data.append(converted_item)
            
            print(f"✅ 법정동 데이터 {len(converted_data)}개 조회 완료")
            return converted_data
            
        except requests.exceptions.RequestException as e:
            print(f"❌ API 요청 실패: {e}")
            return []
        except json.JSONDecodeError as e:
            print(f"❌ JSON 파싱 실패: {e}")
            return []
    
    def fetch_emd_data_by_sido(self, sido_code: str) -> List[Dict]:
        """
        시도 코드로 법정동 데이터 조회
        
        Args:
            sido_code: 시도 코드 (4자리, 예: "4713")
        
        Returns:
            법정동 데이터 리스트
        """
        url = f"{self.base_url}/emd/sido/{sido_code}"
        
        try:
            response = requests.get(url)
            response.raise_for_status()
            
            api_data = response.json()
            
            # API 응답을 분석기 형식으로 변환
            converted_data = []
            for item in api_data:
                converted_item = {
                    "name": item["name"],
                    "totalAccident": item["totalAccident"],
                    "EMD_CD": item["EMD_CD"],
                    "latitude": float(item["latitude"]),
                    "longitude": float(item["longitude"])
                }
                converted_data.append(converted_item)
            
            print(f"✅ {sido_code} 지역 데이터 {len(converted_data)}개 조회 완료")
            return converted_data
            
        except requests.exceptions.RequestException as e:
            print(f"❌ API 요청 실패: {e}")
            return []
        except json.JSONDecodeError as e:
            print(f"❌ JSON 파싱 실패: {e}")
            return []
    
    def analyze_region_with_custom_criteria(self, 
                                          data: List[Dict], 
                                          region_name: str,
                                          use_region_analyzer: bool = True) -> Dict:
        """
        지역 데이터를 사용하여 맞춤형 기준으로 분석
        
        Args:
            data: 지역 교통사고 데이터
            region_name: 지역명
            use_region_analyzer: True면 RegionRiskAnalyzer 사용, False면 기존 방식
        
        Returns:
            분석 결과
        """
        if not data:
            print("❌ 분석할 데이터가 없습니다.")
            return {}
        
        if use_region_analyzer:
            print(f"\n🎯 {region_name} 맞춤형 위험도 기준 분석")
            print("=" * 60)
            
            # RegionRiskAnalyzer 사용
            self.region_analyzer = analyze_region_risk(data, region_name)
            
            # 분석 결과 구성
            result = {
                'region_name': region_name,
                'analyzer_type': 'RegionRiskAnalyzer',
                'criteria': self.region_analyzer.criteria,
                'statistics': self.region_analyzer.stats,
                'classified_regions': self.region_analyzer.classify_regions(),
                'summary': self.region_analyzer.get_criteria_summary()
            }
            
            return result
        else:
            print(f"\n🚦 {region_name} 기존 방식 분석")
            print("=" * 60)
            
            # 기존 TrafficAccidentAnalyzer 사용
            self.analyzer = TrafficAccidentAnalyzer(data)
            results = analyze_new_data(data, 'adaptive')
            results['region_name'] = region_name
            results['analyzer_type'] = 'TrafficAccidentAnalyzer'
            
            return results
    
    def analyze_korea_nationwide_with_custom_criteria(self) -> Optional[Dict]:
        """대한민국 전국 맞춤형 기준 분석"""
        data = self.fetch_emd_data_by_bounds()
        return self.analyze_region_with_custom_criteria(data, "대한민국 전국", True)
    
    def analyze_sido_with_custom_criteria(self, sido_code: str, sido_name: str = None) -> Optional[Dict]:
        """시도별 맞춤형 기준 분석"""
        if not sido_name:
            sido_name = f"시도코드_{sido_code}"
        
        data = self.fetch_emd_data_by_sido(sido_code)
        return self.analyze_region_with_custom_criteria(data, sido_name, True)
    
    def analyze_custom_bounds_with_custom_criteria(self, 
                                                  sw_lat: float, sw_lng: float,
                                                  ne_lat: float, ne_lng: float,
                                                  region_name: str) -> Optional[Dict]:
        """사용자 정의 영역 맞춤형 기준 분석"""
        data = self.fetch_emd_data_by_bounds(sw_lat, sw_lng, ne_lat, ne_lng)
        return self.analyze_region_with_custom_criteria(data, region_name, True)
    
    def compare_analyzers(self, data: List[Dict], region_name: str) -> Dict:
        """
        기존 방식과 새로운 RegionRiskAnalyzer 방식 비교
        
        Args:
            data: 지역 데이터
            region_name: 지역명
        
        Returns:
            비교 결과
        """
        print(f"\n🔄 {region_name} 분석 방식 비교")
        print("=" * 70)
        
        # 기존 방식
        old_result = self.analyze_region_with_custom_criteria(data, region_name, False)
        
        # 새로운 방식
        new_result = self.analyze_region_with_custom_criteria(data, region_name, True)
        
        # 비교 결과 출력
        print(f"\n📊 분석 방식 비교 결과:")
        print(f"{'='*50}")
        
        if old_result and new_result:
            old_criteria = old_result.get('criteria', {})
            new_criteria = new_result.get('criteria', {})
            
            print(f"📈 기존 TrafficAccidentAnalyzer 기준:")
            if old_criteria:
                print(f"   저위험: {old_criteria['low']['min']} ~ {old_criteria['low']['max']}건")
                print(f"   중위험: {old_criteria['medium']['min']} ~ {old_criteria['medium']['max']}건")
                print(f"   고위험: {old_criteria['high']['min']}건 이상")
            
            print(f"\n🎯 새로운 RegionRiskAnalyzer 기준:")
            if new_criteria:
                print(f"   저위험: {new_criteria['low']['min']} ~ {new_criteria['low']['max']}건")
                print(f"   중위험: {new_criteria['medium']['min']} ~ {new_criteria['medium']['max']}건")
                print(f"   고위험: {new_criteria['high']['min']}건 이상")
            
            # 차이점 분석
            if old_criteria and new_criteria:
                print(f"\n🔍 주요 차이점:")
                old_low_max = old_criteria['low']['max']
                new_low_max = new_criteria['low']['max']
                old_high_min = old_criteria['high']['min']
                new_high_min = new_criteria['high']['min']
                
                print(f"   저위험 상한: {old_low_max} → {new_low_max} ({'+'if new_low_max > old_low_max else ''}{new_low_max - old_low_max})")
                print(f"   고위험 하한: {old_high_min} → {new_high_min} ({'+'if new_high_min > old_high_min else ''}{new_high_min - old_high_min})")
        
        return {
            'region_name': region_name,
            'old_result': old_result,
            'new_result': new_result
        }
    
    def save_analysis_result(self, result: Dict, filename: str = None):
        """분석 결과를 파일로 저장"""
        region_name = result.get('region_name', 'unknown')
        analyzer_type = result.get('analyzer_type', 'unknown')
        
        if not filename:
            filename = f"{analyzer_type}_{region_name.replace(' ', '_')}_analysis.json"
        
        # DataFrame을 dict로 변환 (있는 경우)
        if 'summary_df' in result:
            result['summary_data'] = result['summary_df'].to_dict('records')
            del result['summary_df']
        
        with open(filename, 'w', encoding='utf-8') as f:
            json.dump(result, f, ensure_ascii=False, indent=2, default=str)
        
        print(f"💾 분석 결과가 '{filename}'에 저장되었습니다.")


def main():
    """메인 실행 함수 - 다양한 분석 방식 시연"""
    api_analyzer = ApiTrafficAccidentAnalyzer()
    
    print("🚀 API 연동 교통사고 위험도 분석 시스템")
    print("=" * 70)
    
    # 1. 경주시 분석 (시도 코드 사용)
    print("\n1️⃣ 경주시 맞춤형 기준 분석")
    gyeongju_result = api_analyzer.analyze_sido_with_custom_criteria("4713", "경주시")
    if gyeongju_result:
        api_analyzer.save_analysis_result(gyeongju_result)
    
    # # 2. 서울 지역 분석 (좌표 범위 사용)
    # print("\n2️⃣ 서울특별시 맞춤형 기준 분석")
    # seoul_result = api_analyzer.analyze_custom_bounds_with_custom_criteria(
    #     sw_lat=37.413, sw_lng=126.734,
    #     ne_lat=37.715, ne_lng=127.269,
    #     region_name="서울특별시"
    # )
    # if seoul_result:
    #     api_analyzer.save_analysis_result(seoul_result)
    
    # # 3. 부산 지역 분석 및 방식 비교
    # print("\n3️⃣ 부산광역시 분석 방식 비교")
    # busan_data = api_analyzer.fetch_emd_data_by_bounds(
    #     sw_lat=35.0, sw_lng=128.8,
    #     ne_lat=35.4, ne_lng=129.3
    # )
    # if busan_data:
    #     comparison_result = api_analyzer.compare_analyzers(busan_data, "부산광역시")
        
    #     # 비교 결과 저장
    #     api_analyzer.save_analysis_result(comparison_result['new_result'])
    
    print(f"\n✅ 모든 분석이 완료되었습니다!")
    print(f"📁 생성된 파일들을 확인해보세요.")


if __name__ == "__main__":
    main()