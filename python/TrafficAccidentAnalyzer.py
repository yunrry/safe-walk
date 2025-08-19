"""
보행자 교통사고 데이터 위험도 분류 분석기 (Python 버전)
새로운 데이터가 추가되어도 자동으로 기준을 업데이트할 수 있는 클래스와 함수들
"""

import numpy as np
import pandas as pd
from typing import Dict, List, Tuple, Any
import json

class TrafficAccidentAnalyzer:
    """보행자 교통사고 위험도 분석 클래스"""
    
    def __init__(self, data: List[Dict]):
        """
        초기화
        Args:
            data: 지역별 교통사고 데이터 리스트
        """
        self.data = data
        self.df = pd.DataFrame(data)
        self.accidents = sorted([d['totalAccident'] for d in data])
        self.stats = self._calculate_statistics()
    
    def _calculate_statistics(self) -> Dict:
        """기본 통계값 계산"""
        accidents_array = np.array(self.accidents)
        
        stats = {
            'count': len(self.accidents),
            'min': int(np.min(accidents_array)),
            'max': int(np.max(accidents_array)),
            'mean': float(np.mean(accidents_array)),
            'median': float(np.median(accidents_array)),
            'std': float(np.std(accidents_array)),
            'q1': float(np.percentile(accidents_array, 25)),
            'q3': float(np.percentile(accidents_array, 75)),
            'p33': float(np.percentile(accidents_array, 33.33)),
            'p67': float(np.percentile(accidents_array, 66.67))
        }
        
        return stats
    
    def print_statistics(self):
        """기본 통계 정보 출력"""
        print('=' * 30 + ' 기본 통계 정보 ' + '=' * 30)
        print(f"데이터 개수: {self.stats['count']}개")
        print(f"최소값: {self.stats['min']}건, 최대값: {self.stats['max']}건")
        print(f"평균: {self.stats['mean']:.2f}건, 중앙값: {self.stats['median']}건")
        print(f"표준편차: {self.stats['std']:.2f}")
        print(f"1사분위: {self.stats['q1']}건, 3사분위: {self.stats['q3']}건")
        print(f"33백분위: {self.stats['p33']:.1f}건, 67백분위: {self.stats['p67']:.1f}건")
        print(f"사고 횟수 분포: {self.accidents}")
        print()
    
    def generate_risk_criteria(self, method: str = 'adaptive') -> Dict:
        """
        위험도 분류 기준 생성
        
        Args:
            method: 분류 방법 ('equal', 'percentile', 'quartile', 'adaptive')
        
        Returns:
            위험도별 범위 딕셔너리
        """
        criteria = {}
        
        if method == 'equal':
            # 방법 1: 균등 분할 (최대-최소를 3등분)
            range_val = self.stats['max'] - self.stats['min']
            step = range_val / 3
            
            criteria = {
                'low': {'min': self.stats['min'], 'max': int(self.stats['min'] + step)},
                'medium': {'min': int(self.stats['min'] + step) + 1, 'max': int(self.stats['min'] + 2 * step)},
                'high': {'min': int(self.stats['min'] + 2 * step) + 1, 'max': self.stats['max']}
            }
            
        elif method == 'percentile':
            # 방법 2: 백분위 기준 (33%, 67%)
            criteria = {
                'low': {'min': self.stats['min'], 'max': int(self.stats['p33'])},
                'medium': {'min': int(self.stats['p33']) + 1, 'max': int(self.stats['p67'])},
                'high': {'min': int(self.stats['p67']) + 1, 'max': self.stats['max']}
            }
            
        elif method == 'quartile':
            # 방법 3: 사분위수 기준
            criteria = {
                'low': {'min': self.stats['min'], 'max': int(self.stats['q1'])},
                'medium': {'min': int(self.stats['q1']) + 1, 'max': int(self.stats['q3'])},
                'high': {'min': int(self.stats['q3']) + 1, 'max': self.stats['max']}
            }
            
        elif method == 'adaptive':
            # 방법 4: 적응형 기준 (데이터 분포에 따라 자동 조정)
            low_threshold = max(int(self.stats['median']), int(self.stats['mean'] * 0.5))
            high_threshold = max(int(self.stats['mean'] * 1.3), int(self.stats['q3']))
            
            criteria = {
                'low': {'min': self.stats['min'], 'max': low_threshold},
                'medium': {'min': low_threshold + 1, 'max': high_threshold},
                'high': {'min': high_threshold + 1, 'max': self.stats['max']}
            }
        
        else:
            raise ValueError(f"Unknown method: {method}")
        
        return criteria
    
    def classify_regions(self, criteria: Dict) -> Dict:
        """
        지역별 위험도 분류
        
        Args:
            criteria: 위험도 분류 기준
        
        Returns:
            위험도별 분류된 지역 정보
        """
        classified = {'low': [], 'medium': [], 'high': []}
        
        for region in self.data:
            accidents = region['totalAccident']
            region_copy = region.copy()
            
            if criteria['low']['min'] <= accidents <= criteria['low']['max']:
                region_copy['riskLevel'] = '저위험'
                classified['low'].append(region_copy)
            elif criteria['medium']['min'] <= accidents <= criteria['medium']['max']:
                region_copy['riskLevel'] = '중위험'
                classified['medium'].append(region_copy)
            else:
                region_copy['riskLevel'] = '고위험'
                classified['high'].append(region_copy)
        
        return classified
    
    def print_analysis_results(self, method: str, criteria: Dict, classified: Dict):
        """분석 결과 출력"""
        method_names = {
            'equal': '균등분할',
            'percentile': '백분위',
            'quartile': '사분위수',
            'adaptive': '적응형'
        }
        
        print(f"\n{'=' * 20} {method_names[method]} 방식 분석 결과 {'=' * 20}")
        print(f"저위험: {criteria['low']['min']}-{criteria['low']['max']}건 ({len(classified['low'])}개 지역)")
        print(f"중위험: {criteria['medium']['min']}-{criteria['medium']['max']}건 ({len(classified['medium'])}개 지역)")
        print(f"고위험: {criteria['high']['min']}-{criteria['high']['max']}건 ({len(classified['high'])}개 지역)")
        
        print('\n각 지역별 분류:')
        level_names = {'low': '저위험', 'medium': '중위험', 'high': '고위험'}
        
        for level in ['low', 'medium', 'high']:
            if classified[level]:
                print(f"\n{level_names[level]}:")
                for region in classified[level]:
                    print(f"  {region['name']}: {region['totalAccident']}건")
    
    def compare_all_methods(self) -> Dict:
        """모든 분류 방법 비교 분석"""
        print('🚦 보행자 교통사고 위험도 분류 분석')
        print('=' * 70)
        
        self.print_statistics()
        
        methods = ['equal', 'percentile', 'quartile', 'adaptive']
        results = {}
        
        for method in methods:
            criteria = self.generate_risk_criteria(method)
            classified = self.classify_regions(criteria)
            results[method] = {'criteria': criteria, 'classified': classified}
            self.print_analysis_results(method, criteria, classified)
        
        # 권장 방법 제시
        print('\n' + '=' * 70)
        print('📊 권장 분석 방법: ADAPTIVE (적응형)')
        print('- 데이터 분포를 고려하여 자동으로 기준점 조정')
        print('- 중앙값과 평균값을 활용한 균형잡힌 분류')
        print('- 새로운 데이터 추가 시에도 안정적인 분류 가능')
        
        return results
    
    def get_summary_dataframe(self, method: str = 'adaptive') -> pd.DataFrame:
        """분석 결과를 DataFrame으로 반환"""
        criteria = self.generate_risk_criteria(method)
        classified = self.classify_regions(criteria)
        
        summary_data = []
        for region in self.data:
            accidents = region['totalAccident']
            if criteria['low']['min'] <= accidents <= criteria['low']['max']:
                risk_level = '저위험'
            elif criteria['medium']['min'] <= accidents <= criteria['medium']['max']:
                risk_level = '중위험'
            else:
                risk_level = '고위험'
            
            summary_data.append({
                '지역명': region['name'],
                '사고건수': region['totalAccident'],
                '위험도': risk_level,
                'EMD_CD': region.get('EMD_CD', ''),
                '위도': region.get('latitude', 0),
                '경도': region.get('longitude', 0)
            })
        
        return pd.DataFrame(summary_data).sort_values('사고건수', ascending=False)


def analyze_new_data(data: List[Dict], method: str = 'adaptive') -> Dict:
    """
    새로운 데이터 분석 함수
    
    Args:
        data: 분석할 데이터
        method: 분류 방법
    
    Returns:
        분석 결과
    """
    print('🔄 새로운 데이터 분석 중...')
    analyzer = TrafficAccidentAnalyzer(data)
    
    analyzer.print_statistics()
    criteria = analyzer.generate_risk_criteria(method)
    classified = analyzer.classify_regions(criteria)
    analyzer.print_analysis_results(method, criteria, classified)
    
    return {
        'criteria': criteria,
        'classified': classified,
        'stats': analyzer.stats,
        'summary_df': analyzer.get_summary_dataframe(method)
    }


def main():
    """메인 실행 함수"""
    # 경주시 보행자 교통사고 데이터
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
    
    # 분석기 생성 및 전체 분석 실행
    analyzer = TrafficAccidentAnalyzer(gyeongju_data)
    results = analyzer.compare_all_methods()
    
    # 요약 데이터프레임 생성
    print('\n📋 최종 분석 결과 (적응형 기준):')
    summary_df = analyzer.get_summary_dataframe('adaptive')
    print(summary_df.to_string(index=False))
    
    return analyzer, results


if __name__ == "__main__":
    # 사용 예시
    analyzer, results = main()
    
    # 새로운 데이터 추가 예시
    print('\n\n' + '=' * 70)
    print('📊 새로운 지역 데이터 추가 분석 예시')
    print('=' * 70)
    
    # 새로운 지역 데이터 추가 (예시)
    new_regions = [
        {"name": "신규지역A", "totalAccident": 25, "EMD_CD": "47130999", "latitude": 35.8, "longitude": 129.2},
        {"name": "신규지역B", "totalAccident": 15, "EMD_CD": "47130998", "latitude": 35.9, "longitude": 129.3}
    ]
    
    # 기존 데이터와 새 데이터 합치기
    updated_data = analyzer.data + new_regions
    
    # 새로운 데이터로 재분석
    new_results = analyze_new_data(updated_data, 'adaptive')
    
    print('\n새 데이터 추가 후 요약:')
    print(new_results['summary_df'].to_string(index=False))