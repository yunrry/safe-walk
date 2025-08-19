# region_risk_analyzer.py
import numpy as np
import pandas as pd
from typing import Dict, List, Tuple, Any, Optional
import json

class RegionRiskAnalyzer:
    """지역별 맞춤형 교통사고 위험도 기준 생성기"""
    
    def __init__(self, region_data: List[Dict], region_name: str = "지역"):
        """
        초기화
        
        Args:
            region_data: 지역별 교통사고 데이터 리스트
            region_name: 지역명 (분석 결과 출력용)
        """
        self.region_data = region_data
        self.region_name = region_name
        self.df = pd.DataFrame(region_data)
        self.accidents = sorted([d['totalAccident'] for d in region_data])
        self.stats = self._calculate_comprehensive_statistics()
        self.criteria = self._generate_adaptive_criteria()
    
    def _calculate_comprehensive_statistics(self) -> Dict:
        """포괄적인 통계 분석"""
        accidents_array = np.array(self.accidents)
        
        # 기본 통계
        basic_stats = {
            'count': len(self.accidents),
            'min': int(np.min(accidents_array)),
            'max': int(np.max(accidents_array)),
            'mean': float(np.mean(accidents_array)),
            'median': float(np.median(accidents_array)),
            'std': float(np.std(accidents_array)),
            'variance': float(np.var(accidents_array))
        }
        
        # 분위수 통계
        percentile_stats = {
            'q1': float(np.percentile(accidents_array, 25)),
            'q3': float(np.percentile(accidents_array, 75)),
            'p10': float(np.percentile(accidents_array, 10)),
            'p90': float(np.percentile(accidents_array, 90)),
            'p33': float(np.percentile(accidents_array, 33.33)),
            'p67': float(np.percentile(accidents_array, 66.67)),
            'iqr': float(np.percentile(accidents_array, 75) - np.percentile(accidents_array, 25))
        }
        
        # 분포 특성 분석
        distribution_stats = {
            'skewness': self._calculate_skewness(accidents_array),
            'coefficient_of_variation': basic_stats['std'] / basic_stats['mean'] if basic_stats['mean'] > 0 else 0,
            'outlier_threshold_iqr': percentile_stats['q3'] + 1.5 * percentile_stats['iqr'],
            'outlier_threshold_std': basic_stats['mean'] + 2 * basic_stats['std']
        }
        
        # 모든 통계 합치기
        return {**basic_stats, **percentile_stats, **distribution_stats}
    
    def _calculate_skewness(self, data: np.ndarray) -> float:
        """왜도(비대칭도) 계산"""
        if len(data) < 3:
            return 0.0
        
        mean = np.mean(data)
        std = np.std(data)
        
        if std == 0:
            return 0.0
        
        skewness = np.mean(((data - mean) / std) ** 3)
        return float(skewness)
    
    def _generate_adaptive_criteria(self) -> Dict:
        """
        지역 특성을 반영한 적응형 기준 생성
        
        Returns:
            위험도별 범위 딕셔너리
        """
        stats = self.stats
        
        # 1단계: 기본 임계값 계산
        median_threshold = max(int(stats['median']), 1)
        mean_based_low = max(int(stats['mean'] * 0.4), 1)
        mean_based_high = max(int(stats['mean'] * 1.2), int(stats['q3']))
        
        # 2단계: 분포 특성에 따른 조정
        if stats['skewness'] > 1.0:  # 심하게 치우친 분포
            # 극값의 영향을 줄이고 중앙값 중심으로 조정
            low_threshold = max(median_threshold, int(stats['p33']))
            high_threshold = min(mean_based_high, int(stats['p90']))
        elif stats['coefficient_of_variation'] > 1.5:  # 변동성이 큰 경우
            # Q1, Q3 기준으로 안정적인 구간 설정
            low_threshold = max(int(stats['q1']), 1)
            high_threshold = int(stats['q3'])
        else:  # 일반적인 분포
            # 평균과 중앙값을 혼합한 기준
            low_threshold = max(median_threshold, mean_based_low)
            high_threshold = mean_based_high
        
        # 3단계: 지역 규모에 따른 미세 조정
        region_size = stats['count']
        if region_size < 20:  # 소규모 지역
            # 더 세밀한 구분을 위해 기준을 낮춤
            low_threshold = max(1, int(low_threshold * 0.8))
            high_threshold = max(low_threshold + 2, int(high_threshold * 0.9))
        elif region_size > 100:  # 대규모 지역
            # 더 넓은 범위로 기준을 높임
            low_threshold = int(low_threshold * 1.1)
            high_threshold = int(high_threshold * 1.1)
        
        # 4단계: 논리적 일관성 확보
        if high_threshold <= low_threshold:
            high_threshold = low_threshold + max(2, int(stats['std'] * 0.5))
        
        criteria = {
            'low': {'min': stats['min'], 'max': low_threshold},
            'medium': {'min': low_threshold + 1, 'max': high_threshold},
            'high': {'min': high_threshold + 1, 'max': stats['max']}
        }
        
        return criteria
    
    def classify_regions(self) -> Dict:
        """지역별 위험도 분류"""
        classified = {'low': [], 'medium': [], 'high': []}
        
        for region in self.region_data:
            accidents = region['totalAccident']
            region_copy = region.copy()
            
            if self.criteria['low']['min'] <= accidents <= self.criteria['low']['max']:
                region_copy['riskLevel'] = '저위험'
                classified['low'].append(region_copy)
            elif self.criteria['medium']['min'] <= accidents <= self.criteria['medium']['max']:
                region_copy['riskLevel'] = '중위험'
                classified['medium'].append(region_copy)
            else:
                region_copy['riskLevel'] = '고위험'
                classified['high'].append(region_copy)
        
        return classified
    
    def print_analysis_report(self):
        """상세 분석 보고서 출력"""
        classified = self.classify_regions()
        
        print(f"\n{'='*15} {self.region_name} 교통사고 위험도 분석 보고서 {'='*15}")
        
        # 1. 지역 개요
        print(f"\n📍 지역 개요:")
        print(f"   • 총 법정동 수: {self.stats['count']}개")
        print(f"   • 사고 범위: {self.stats['min']}건 ~ {self.stats['max']}건")
        print(f"   • 총 사고 건수: {sum(self.accidents)}건")
        
        # 2. 통계 분석
        print(f"\n📊 통계 분석:")
        print(f"   • 평균: {self.stats['mean']:.1f}건")
        print(f"   • 중앙값: {self.stats['median']:.1f}건")
        print(f"   • 표준편차: {self.stats['std']:.1f}건")
        print(f"   • 변동계수: {self.stats['coefficient_of_variation']:.2f}")
        print(f"   • 왜도: {self.stats['skewness']:.2f}")
        
        # 3. 분위수 정보
        print(f"\n📈 분위수 분석:")
        print(f"   • 1사분위(Q1): {self.stats['q1']:.1f}건")
        print(f"   • 3사분위(Q3): {self.stats['q3']:.1f}건")
        print(f"   • 90백분위: {self.stats['p90']:.1f}건")
        
        # 4. 적응형 기준
        print(f"\n🎯 {self.region_name} 맞춤형 위험도 기준:")
        print(f"   • 저위험: {self.criteria['low']['min']} ~ {self.criteria['low']['max']}건")
        print(f"   • 중위험: {self.criteria['medium']['min']} ~ {self.criteria['medium']['max']}건")
        print(f"   • 고위험: {self.criteria['high']['min']}건 이상")
        
        # 5. 분류 결과
        print(f"\n🏘️  위험도별 분류 결과:")
        for level in ['low', 'medium', 'high']:
            level_names = {'low': '저위험', 'medium': '중위험', 'high': '고위험'}
            count = len(classified[level])
            percentage = (count / self.stats['count']) * 100
            print(f"   • {level_names[level]}: {count}개 ({percentage:.1f}%)")
        
        # 6. 각 위험도별 지역 목록
        print(f"\n📋 위험도별 지역 목록:")
        for level in ['high', 'medium', 'low']:  # 고위험부터 출력
            level_names = {'low': '저위험', 'medium': '중위험', 'high': '고위험'}
            regions = sorted(classified[level], key=lambda x: x['totalAccident'], reverse=True)
            
            if regions:
                print(f"\n   {level_names[level]} 지역:")
                for region in regions:
                    print(f"     - {region['name']}: {region['totalAccident']}건")
    
    def get_criteria_summary(self) -> Dict:
        """기준값 요약 정보 반환"""
        return {
            'region_name': self.region_name,
            'total_regions': self.stats['count'],
            'criteria': self.criteria,
            'statistics': {
                'mean': self.stats['mean'],
                'median': self.stats['median'],
                'std': self.stats['std'],
                'min': self.stats['min'],
                'max': self.stats['max']
            },
            'distribution_analysis': {
                'skewness': self.stats['skewness'],
                'coefficient_of_variation': self.stats['coefficient_of_variation'],
                'distribution_type': self._get_distribution_type()
            }
        }
    
    def _get_distribution_type(self) -> str:
        """분포 유형 판단"""
        skew = self.stats['skewness']
        cv = self.stats['coefficient_of_variation']
        
        if abs(skew) < 0.5 and cv < 0.5:
            return "정규분포에 가까운 안정적 분포"
        elif skew > 1.0:
            return "심하게 치우친 분포 (극값 존재)"
        elif cv > 1.5:
            return "변동성이 큰 분포"
        elif skew > 0.5:
            return "우측으로 치우친 분포"
        else:
            return "일반적인 분포"
    
    def save_analysis_result(self, filename: str = None):
        """분석 결과를 JSON 파일로 저장"""
        if not filename:
            filename = f"region_risk_analysis_{self.region_name.replace(' ', '_')}.json"
        
        result = {
            'region_name': self.region_name,
            'analysis_date': pd.Timestamp.now().isoformat(),
            'criteria': self.criteria,
            'statistics': self.stats,
            'classified_regions': self.classify_regions(),
            'summary': self.get_criteria_summary()
        }
        
        with open(filename, 'w', encoding='utf-8') as f:
            json.dump(result, f, ensure_ascii=False, indent=2, default=str)
        
        print(f"\n💾 분석 결과가 '{filename}'에 저장되었습니다.")


def analyze_region_risk(region_data: List[Dict], region_name: str = "분석지역") -> RegionRiskAnalyzer:
    """
    지역 데이터를 분석하여 맞춤형 위험도 기준을 생성하는 함수
    
    Args:
        region_data: 지역별 교통사고 데이터
        region_name: 지역명
    
    Returns:
        RegionRiskAnalyzer 객체
    """
    analyzer = RegionRiskAnalyzer(region_data, region_name)
    analyzer.print_analysis_report()
    return analyzer


# 사용 예시
if __name__ == "__main__":
    # 경주시 데이터 예시
    gyeongju_data = [
        {"name": "동부동", "totalAccident": 4, "EMD_CD": "47130101", "latitude": 35.845893, "longitude": 129.212176},
        {"name": "외동읍", "totalAccident": 2, "EMD_CD": "47130259", "latitude": 35.738707, "longitude": 129.284858},
        {"name": "시래동", "totalAccident": 5, "EMD_CD": "47130130", "latitude": 35.77228, "longitude": 129.303108},
        {"name": "황오동", "totalAccident": 38, "EMD_CD": "47130105", "latitude": 35.842537, "longitude": 129.22005},
        {"name": "성건동", "totalAccident": 58, "EMD_CD": "47130108", "latitude": 35.850957, "longitude": 129.207231},
        {"name": "감포읍", "totalAccident": 3, "EMD_CD": "47130250", "latitude": 35.80601, "longitude": 129.501105},
        {"name": "성동동", "totalAccident": 53, "EMD_CD": "47130104", "latitude": 35.847318, "longitude": 129.217713},
        {"name": "안강읍", "totalAccident": 8, "EMD_CD": "47130253", "latitude": 35.91722, "longitude": 129.229519},
        {"name": "현곡면", "totalAccident": 3, "EMD_CD": "47130360", "latitude": 35.880682, "longitude": 129.202557},
        {"name": "노동동", "totalAccident": 4, "EMD_CD": "47130106", "latitude": 35.842643, "longitude": 129.212062},
        {"name": "황성동", "totalAccident": 7, "EMD_CD": "47130124", "latitude": 35.867202, "longitude": 129.216789},
        {"name": "사정동", "totalAccident": 13, "EMD_CD": "47130109", "latitude": 35.834371, "longitude": 129.207049},
        {"name": "노서동", "totalAccident": 12, "EMD_CD": "47130107", "latitude": 35.841499, "longitude": 129.20614}
    ]
    
    # 경주시 분석
    print("🏛️ 경주시 교통사고 위험도 분석")
    gyeongju_analyzer = analyze_region_risk(gyeongju_data, "경주시")
    
    # 분석 결과 저장
    gyeongju_analyzer.save_analysis_result()
    
    # 기준값만 간단히 출력
    print(f"\n🎯 경주시 최종 기준값:")
    criteria = gyeongju_analyzer.criteria
    print(f"저위험: {criteria['low']['min']} ~ {criteria['low']['max']}건")
    print(f"중위험: {criteria['medium']['min']} ~ {criteria['medium']['max']}건")
    print(f"고위험: {criteria['high']['min']}건 이상")