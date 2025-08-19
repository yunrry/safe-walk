# traffic_accident_visualizer.py
import json
import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
import numpy as np
from typing import Dict, List, Any
import plotly.express as px
import plotly.graph_objects as go
from plotly.subplots import make_subplots
import folium
from datetime import datetime
import os
import platform

class TrafficAccidentVisualizer:
    """교통사고 분석 결과 시각화 및 보고서 생성 클래스"""
    
    def __init__(self, analysis_file: str):
        """
        초기화
        
        Args:
            analysis_file: 분석 결과 JSON 파일 경로
        """
        self._setup_korean_font()  # 한글 폰트 설정을 가장 먼저 실행
        self.analysis_file = analysis_file
        self.data = self._load_analysis_data()
        self.region_name = self.data.get('region_name', '지역')
        self.output_dir = f"reports_{self.region_name.replace(' ', '_')}"
        self._create_output_directory()
    
    def _setup_korean_font(self):
        """운영체제별 한글 폰트 설정"""
        system = platform.system()
        
        try:
            if system == "Darwin":  # macOS
                # macOS용 한글 폰트들
                korean_fonts = [
                    'AppleGothic',
                    'Apple SD Gothic Neo',
                    'NanumGothic',
                    'Malgun Gothic'
                ]
            elif system == "Windows":  # Windows
                korean_fonts = [
                    'Malgun Gothic',
                    'NanumGothic',
                    'Gulim',
                    'Dotum'
                ]
            else:  # Linux
                korean_fonts = [
                    'NanumGothic',
                    'NanumBarunGothic',
                    'DejaVu Sans'
                ]
            
            # 사용 가능한 폰트 찾기
            import matplotlib.font_manager as fm
            available_fonts = [f.name for f in fm.fontManager.ttflist]
            
            selected_font = None
            for font in korean_fonts:
                if font in available_fonts:
                    selected_font = font
                    break
            
            # 폰트 설정
            if selected_font:
                plt.rcParams['font.family'] = selected_font
                print(f"✅ 한글 폰트 설정: {selected_font}")
            else:
                # 폰트를 찾지 못한 경우 기본 설정
                plt.rcParams['font.family'] = 'DejaVu Sans'
                print("⚠️ 한글 폰트를 찾지 못했습니다. 기본 폰트를 사용합니다.")
                
                # 한글 폰트 다운로드 가이드 출력
                print("📥 한글 폰트 설치 방법:")
                if system == "Darwin":
                    print("   brew install font-nanum-gothic")
                elif system == "Linux":
                    print("   sudo apt-get install fonts-nanum")
                
        except Exception as e:
            print(f"⚠️ 폰트 설정 중 오류: {e}")
            plt.rcParams['font.family'] = 'DejaVu Sans'
        
        # 마이너스 기호 깨짐 방지
        plt.rcParams['axes.unicode_minus'] = False
        
        # 폰트 크기 기본 설정
        plt.rcParams.update({
            'font.size': 12,
            'axes.titlesize': 14,
            'axes.labelsize': 12,
            'xtick.labelsize': 10,
            'ytick.labelsize': 10,
            'legend.fontsize': 11,
            'figure.titlesize': 16
        })
    
    def _load_analysis_data(self) -> Dict:
        """분석 데이터 로드"""
        try:
            with open(self.analysis_file, 'r', encoding='utf-8') as f:
                return json.load(f)
        except Exception as e:
            print(f"❌ 파일 로드 실패: {e}")
            return {}
    
    def _create_output_directory(self):
        """출력 디렉토리 생성"""
        if not os.path.exists(self.output_dir):
            os.makedirs(self.output_dir)
        
        # 이미지 하위 디렉토리 생성
        img_dir = os.path.join(self.output_dir, 'images')
        if not os.path.exists(img_dir):
            os.makedirs(img_dir)
    
    def create_risk_distribution_chart(self) -> str:
        """위험도별 분포 차트 생성"""
        classified = self.data.get('classified_regions', {})
        
        # 데이터 준비
        risk_counts = {
            '저위험': len(classified.get('low', [])),
            '중위험': len(classified.get('medium', [])),
            '고위험': len(classified.get('high', []))
        }
        
        # 색상 설정
        colors = ['#326CF9', '#FF7607', '#EF4136']  # 초록, 노랑, 빨강
        
        # 파이 차트 생성
        fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(16, 7))
        
        # 파이 차트 - textprops 간소화
        wedges, texts, autotexts = ax1.pie(
            risk_counts.values(), 
            labels=risk_counts.keys(),
            colors=colors,
            autopct='%1.1f%%',
            startangle=90,
            textprops={'fontsize': 13}  # fontweight 제거
        )
        
        # 파이 차트 텍스트 수동으로 굵게 설정
        for text in texts:
            text.set_fontsize(13)
            text.set_weight('bold')
        for autotext in autotexts:
            autotext.set_fontsize(12)
            autotext.set_weight('bold')
            autotext.set_color('white')
        
        ax1.set_title(f'{self.region_name} 위험도별 분포', 
                     fontsize=16, weight='bold', pad=20)
        
        # 막대 차트
        bars = ax2.bar(risk_counts.keys(), risk_counts.values(), 
                      color=colors, alpha=0.8, width=0.6)
        ax2.set_title(f'{self.region_name} 위험도별 지역 수', 
                     fontsize=16, weight='bold', pad=20)
        ax2.set_ylabel('지역 수 (개)', fontsize=13, weight='bold')
        ax2.set_xlabel('위험도', fontsize=13, weight='bold')
        
        # 막대 위에 값 표시
        for bar in bars:
            height = bar.get_height()
            ax2.text(bar.get_x() + bar.get_width()/2., height + max(risk_counts.values()) * 0.01,
                    f'{int(height)}개', ha='center', va='bottom', 
                    fontsize=12, weight='bold')
        
        # 축 레이블 폰트 크기 조정
        ax2.tick_params(axis='both', which='major', labelsize=12)
        
        plt.tight_layout()
        chart_path = os.path.join(self.output_dir, 'images', 'risk_distribution.png')
        plt.savefig(chart_path, dpi=300, bbox_inches='tight', facecolor='white')
        plt.close()
        
        return chart_path
    
    def create_accident_histogram(self) -> str:
        """사고 건수 히스토그램 생성"""
        classified = self.data.get('classified_regions', {})
        
        # 모든 지역의 사고 건수 수집
        all_accidents = []
        risk_levels = []
        
        for level, regions in classified.items():
            for region in regions:
                all_accidents.append(region['totalAccident'])
                risk_levels.append(level)
        
        # DataFrame 생성
        df = pd.DataFrame({
            'accidents': all_accidents,
            'risk_level': risk_levels
        })
        
        # 히스토그램 생성
        fig, ax = plt.subplots(figsize=(13, 7))
        # 위험도별 색상
        level_colors = {'low': '#326CF9', 'medium': '#FF7607', 'high': '#EF4136'}
        level_names = {'low': '저위험', 'medium': '중위험', 'high': '고위험'}
        
        for level in ['low', 'medium', 'high']:
            level_data = df[df['risk_level'] == level]['accidents']
            if not level_data.empty:
                ax.hist(level_data, bins=20, alpha=0.7, 
                       label=level_names[level], color=level_colors[level],
                       edgecolor='black', linewidth=0.5)
        
        # 통계선 추가
        stats = self.data.get('statistics', {})
        if stats:
            ax.axvline(stats.get('mean', 0), color='red', linestyle='--', linewidth=2,
                      label=f"평균: {stats.get('mean', 0):.1f}건")
            ax.axvline(stats.get('median', 0), color='blue', linestyle='--', linewidth=2,
                      label=f"중앙값: {stats.get('median', 0):.1f}건")
        
        ax.set_xlabel('사고 건수 (건)', fontsize=13, weight='bold')
        ax.set_ylabel('지역 수 (개)', fontsize=13, weight='bold')
        ax.set_title(f'{self.region_name} 사고 건수 분포', 
                    fontsize=16, weight='bold', pad=20)
        
        # 범례 설정
        legend = ax.legend(fontsize=12, frameon=True, fancybox=True, shadow=True)
        legend.get_frame().set_facecolor('white')
        legend.get_frame().set_alpha(0.9)
        
        ax.grid(True, alpha=0.3)
        ax.tick_params(axis='both', which='major', labelsize=11)
        
        hist_path = os.path.join(self.output_dir, 'images', 'accident_histogram.png')
        plt.savefig(hist_path, dpi=300, bbox_inches='tight', facecolor='white')
        plt.close()
        
        return hist_path
    
    def create_top_regions_chart(self, top_n: int = 10) -> str:
        """상위 위험 지역 차트 생성"""
        classified = self.data.get('classified_regions', {})
        
        # 모든 지역 데이터 수집 및 정렬
        all_regions = []
        for regions in classified.values():
            all_regions.extend(regions)
        
        # 사고 건수 기준으로 정렬
        top_regions = sorted(all_regions, key=lambda x: x['totalAccident'], reverse=True)[:top_n]
        
        # 데이터 준비
        names = [region['name'] for region in top_regions]
        accidents = [region['totalAccident'] for region in top_regions]
        risk_levels = [region['riskLevel'] for region in top_regions]

        # 색상 매핑
        color_map = {'저위험': '#326CF9', '중위험': '#FF7607', '고위험': '#EF4136'}
        colors = [color_map[level] for level in risk_levels]
        
        # 수평 막대 차트 생성
        fig, ax = plt.subplots(figsize=(14, max(8, len(names) * 0.6)))
        
        y_pos = np.arange(len(names))
        bars = ax.barh(y_pos, accidents, color=colors, alpha=0.8, height=0.7,
                      edgecolor='black', linewidth=0.5)
        
        # 막대 끝에 값 표시
        for i, (bar, accident) in enumerate(zip(bars, accidents)):
            ax.text(bar.get_width() + max(accidents) * 0.01, 
                   bar.get_y() + bar.get_height()/2,
                   f'{accident}건', ha='left', va='center', 
                   fontsize=11, weight='bold')
        
        ax.set_yticks(y_pos)
        ax.set_yticklabels(names, fontsize=11, weight='bold')
        ax.set_xlabel('사고 건수 (건)', fontsize=13, weight='bold')
        ax.set_title(f'{self.region_name} 상위 {top_n}개 지역 (사고 건수 기준)', 
                    fontsize=16, weight='bold', pad=20)
        ax.grid(True, alpha=0.3, axis='x')
        
        # 범례 추가
        from matplotlib.patches import Patch
        legend_elements = [Patch(facecolor=color, label=level, edgecolor='black') 
                          for level, color in color_map.items()]
        legend = ax.legend(handles=legend_elements, loc='lower right', 
                          fontsize=12, frameon=True, fancybox=True, shadow=True)
        legend.get_frame().set_facecolor('white')
        legend.get_frame().set_alpha(0.9)
        
        # y축을 뒤집어서 높은 값이 위에 오도록
        ax.invert_yaxis()
        
        plt.tight_layout()
        top_chart_path = os.path.join(self.output_dir, 'images', 'top_regions.png')
        plt.savefig(top_chart_path, dpi=300, bbox_inches='tight', facecolor='white')
        plt.close()
        
        return top_chart_path
    
    def create_criteria_comparison_chart(self) -> str:
        """기준값 비교 차트 생성"""
        criteria = self.data.get('criteria', {})
        
        if not criteria:
            return ""

        # 데이터 준비
        categories = ['저위험\n상한', '중위험\n상한', '고위험\n하한']
        values = [
            criteria.get('low', {}).get('max', 0),
            criteria.get('medium', {}).get('max', 0),
            criteria.get('high', {}).get('min', 0)
        ]
        colors = ['#326CF9', '#FF7607', '#EF4136']
        
        # 막대 차트 생성
        fig, ax = plt.subplots(figsize=(11, 7))
        
        bars = ax.bar(categories, values, color=colors, alpha=0.8, width=0.6,
                     edgecolor='black', linewidth=1)
        
        # 막대 위에 값 표시
        for bar, value in zip(bars, values):
            ax.text(bar.get_x() + bar.get_width()/2, 
                   bar.get_height() + max(values) * 0.02,
                   f'{value}건', ha='center', va='bottom', 
                   fontsize=13, weight='bold')
        
        ax.set_ylabel('사고 건수 (건)', fontsize=13, weight='bold')
        ax.set_xlabel('기준 구분', fontsize=13, weight='bold')
        ax.set_title(f'{self.region_name} 위험도 분류 기준값', 
                    fontsize=16, weight='bold', pad=20)
        ax.grid(True, alpha=0.3, axis='y')
        
        # 기준 범위 표시 박스
        textstr = f"""위험도 분류 기준
        
저위험: 0 ~ {values[0]}건
중위험: {values[0]+1} ~ {values[1]}건  
고위험: {values[2]}건 이상"""
        
        props = dict(boxstyle='round,pad=0.5', facecolor='lightblue', alpha=0.8)
        ax.text(0.98, 0.98, textstr, transform=ax.transAxes, 
               fontsize=11, weight='bold',
               verticalalignment='top', horizontalalignment='right',
               bbox=props)
        
        ax.tick_params(axis='both', which='major', labelsize=11)
        
        plt.tight_layout()
        criteria_path = os.path.join(self.output_dir, 'images', 'criteria_comparison.png')
        plt.savefig(criteria_path, dpi=300, bbox_inches='tight', facecolor='white')
        plt.close()
        
        return criteria_path
    
    def create_interactive_map(self) -> str:
        """인터랙티브 지도 생성"""
        classified = self.data.get('classified_regions', {})
        
        # 모든 지역 데이터 수집
        all_regions = []
        for regions in classified.values():
            all_regions.extend(regions)
        
        if not all_regions:
            return ""
        
        # 중심점 계산
        center_lat = np.mean([region['latitude'] for region in all_regions])
        center_lon = np.mean([region['longitude'] for region in all_regions])
        
        # 지도 생성
        m = folium.Map(location=[center_lat, center_lon], zoom_start=10)

        # 색상 매핑
        color_map = {'저위험': '#326CF9', '중위험': '#FF7607', '고위험': '#EF4136'}
        
        # 마커 추가
        for region in all_regions:
            folium.CircleMarker(
                location=[region['latitude'], region['longitude']],
                radius=max(5, min(20, region['totalAccident'] / 2)),  # 크기 조정
                popup=f"<b>{region['name']}</b><br>"
                      f"사고 건수: {region['totalAccident']}건<br>"
                      f"위험도: {region['riskLevel']}",
                color=color_map[region['riskLevel']],
                fill=True,
                fillColor=color_map[region['riskLevel']],
                fillOpacity=0.7
            ).add_to(m)
        
        # 범례 추가
        legend_html = '''
        <div style="position: fixed; 
                    bottom: 50px; left: 50px; width: 150px; height: 90px; 
                    background-color: white; border:2px solid grey; z-index:9999; 
                    font-size:14px; padding: 10px">
        <p><b>위험도 범례</b></p>
        <p><i class="fa fa-circle" style="color:green"></i> 저위험</p>
        <p><i class="fa fa-circle" style="color:orange"></i> 중위험</p>
        <p><i class="fa fa-circle" style="color:red"></i> 고위험</p>
        </div>
        '''
        m.get_root().html.add_child(folium.Element(legend_html))
        
        map_path = os.path.join(self.output_dir, 'interactive_map.html')
        m.save(map_path)
        
        return map_path
    
    def generate_markdown_report(self) -> str:
        """마크다운 보고서 생성"""
        
        # 이미지 생성
        risk_dist_chart = self.create_risk_distribution_chart()
        accident_hist = self.create_accident_histogram()
        top_regions_chart = self.create_top_regions_chart()
        criteria_chart = self.create_criteria_comparison_chart()
        interactive_map = self.create_interactive_map()
        
        # 통계 데이터 추출
        stats = self.data.get('statistics', {})
        criteria = self.data.get('criteria', {})
        classified = self.data.get('classified_regions', {})
        
        # 현재 시간
        current_time = datetime.now().strftime("%Y년 %m월 %d일 %H시 %M분")
        
        # 마크다운 내용 생성
        markdown_content = f"""# {self.region_name} 교통사고 위험도 분석 보고서

📅 **생성일시**: {current_time}  
🔍 **분석 대상**: {self.region_name}  
📊 **분석 방법**: RegionRiskAnalyzer (적응형 기준)

---

## 📋 요약 (Executive Summary)

{self.region_name}의 교통사고 데이터를 분석하여 지역별 위험도를 분류하였습니다. 총 **{stats.get('count', 0)}개 지역**을 대상으로 적응형 알고리즘을 사용하여 저위험, 중위험, 고위험으로 분류하였습니다.

### 🎯 주요 결과
- **평균 사고 건수**: {stats.get('mean', 0):.1f}건
- **최대 사고 건수**: {stats.get('max', 0)}건
- **고위험 지역**: {len(classified.get('high', []))}개 ({len(classified.get('high', []))/stats.get('count', 1)*100:.1f}%)
- **중위험 지역**: {len(classified.get('medium', []))}개 ({len(classified.get('medium', []))/stats.get('count', 1)*100:.1f}%)
- **저위험 지역**: {len(classified.get('low', []))}개 ({len(classified.get('low', []))/stats.get('count', 1)*100:.1f}%)

---

## 📊 통계 분석

### 기본 통계
| 항목 | 값 |
|------|-----|
| 총 지역 수 | {stats.get('count', 0)}개 |
| 평균 사고 건수 | {stats.get('mean', 0):.2f}건 |
| 중앙값 | {stats.get('median', 0):.1f}건 |
| 표준편차 | {stats.get('std', 0):.2f}건 |
| 최소값 | {stats.get('min', 0)}건 |
| 최대값 | {stats.get('max', 0)}건 |

### 분위수 분석
| 분위수 | 값 |
|--------|-----|
| 1사분위 (Q1) | {stats.get('q1', 0):.1f}건 |
| 3사분위 (Q3) | {stats.get('q3', 0):.1f}건 |
| 90백분위 | {stats.get('p90', 0):.1f}건 |

---

## 🎯 위험도 분류 기준

{self.region_name}의 데이터 분포 특성을 반영하여 다음과 같은 적응형 기준을 설정하였습니다:

| 위험도 | 범위 | 지역 수 | 비율 |
|--------|------|---------|------|
| 🟢 저위험 | {criteria.get('low', {}).get('min', 0)} ~ {criteria.get('low', {}).get('max', 0)}건 | {len(classified.get('low', []))}개 | {len(classified.get('low', []))/stats.get('count', 1)*100:.1f}% |
| 🟡 중위험 | {criteria.get('medium', {}).get('min', 0)} ~ {criteria.get('medium', {}).get('max', 0)}건 | {len(classified.get('medium', []))}개 | {len(classified.get('medium', []))/stats.get('count', 1)*100:.1f}% |
| 🔴 고위험 | {criteria.get('high', {}).get('min', 0)}건 이상 | {len(classified.get('high', []))}개 | {len(classified.get('high', []))/stats.get('count', 1)*100:.1f}% |

![위험도 분류 기준](images/criteria_comparison.png)

---

## 📈 시각화 분석

### 위험도별 분포
![위험도별 분포](images/risk_distribution.png)

### 사고 건수 히스토그램
![사고 건수 분포](images/accident_histogram.png)

### 상위 위험 지역
![상위 위험 지역](images/top_regions.png)

---

## 🚨 고위험 지역 상세 분석

"""

        # 고위험 지역 목록 추가
        high_risk_regions = sorted(classified.get('high', []), 
                                 key=lambda x: x['totalAccident'], reverse=True)
        
        if high_risk_regions:
            markdown_content += "### 고위험 지역 목록\n\n"
            markdown_content += "| 순위 | 지역명 | 사고건수 | EMD 코드 |\n"
            markdown_content += "|------|--------|----------|----------|\n"
            
            for i, region in enumerate(high_risk_regions[:10], 1):
                markdown_content += f"| {i} | {region['name']} | {region['totalAccident']}건 | {region['EMD_CD']} |\n"
        
        # 중위험 지역 요약
        medium_risk_regions = classified.get('medium', [])
        if medium_risk_regions:
            markdown_content += f"\n## ⚠️ 중위험 지역 ({len(medium_risk_regions)}개)\n\n"
            
            # 상위 5개만 표시
            top_medium = sorted(medium_risk_regions, key=lambda x: x['totalAccident'], reverse=True)[:5]
            markdown_content += "### 주요 중위험 지역\n\n"
            for region in top_medium:
                markdown_content += f"- **{region['name']}**: {region['totalAccident']}건\n"
        
        # 권장사항 추가
        markdown_content += f"""

---

## 💡 권장사항 및 결론

### 🎯 핵심 권장사항
1. **고위험 지역 집중 관리**: {len(classified.get('high', []))}개 고위험 지역에 대한 집중적인 안전대책 수립
2. **중위험 지역 예방 조치**: {len(classified.get('medium', []))}개 중위험 지역의 사고 예방을 위한 선제적 조치
3. **지속적인 모니터링**: 정기적인 데이터 업데이트를 통한 위험도 재평가

### 📊 분석 방법론의 특징
- **적응형 기준**: 지역 특성을 반영한 맞춤형 위험도 기준 적용
- **통계적 근거**: 평균, 중앙값, 분위수를 종합적으로 고려한 과학적 분류
- **실용성**: 정책 결정에 활용 가능한 명확한 기준 제시

---

## 📁 첨부 자료

### 📊 차트 및 그래프
- [위험도별 분포 차트](images/risk_distribution.png)
- [사고 건수 히스토그램](images/accident_histogram.png)
- [상위 위험 지역 차트](images/top_regions.png)
- [분류 기준 차트](images/criteria_comparison.png)

### 🗺️ 인터랙티브 지도
- [교통사고 위험도 지도](interactive_map.html)

### 📄 원본 데이터
- 분석 원본 파일: `{os.path.basename(self.analysis_file)}`

---

*본 보고서는 RegionRiskAnalyzer를 사용하여 자동 생성되었습니다.*
*분석 기준일: {current_time}*
"""

        # 마크다운 파일 저장
        report_path = os.path.join(self.output_dir, f'{self.region_name}_교통사고_분석보고서.md')
        with open(report_path, 'w', encoding='utf-8') as f:
            f.write(markdown_content)
        
        return report_path
    
    def generate_full_report(self):
        """전체 보고서 생성 (시각화 + 마크다운)"""
        print(f"📊 {self.region_name} 교통사고 분석 보고서 생성 중...")
        print("=" * 60)
        
        # 마크다운 보고서 생성
        report_path = self.generate_markdown_report()
        
        print(f"✅ 보고서 생성 완료!")
        print(f"📁 출력 디렉토리: {self.output_dir}")
        print(f"📄 보고서 파일: {report_path}")
        print(f"🗺️ 인터랙티브 지도: {os.path.join(self.output_dir, 'interactive_map.html')}")
        print(f"🖼️ 생성된 이미지: {len(os.listdir(os.path.join(self.output_dir, 'images')))}개")
        
        return self.output_dir


# 사용 예시
def create_visualization_report(analysis_file: str):
    """분석 파일을 받아서 시각화 보고서 생성"""
    visualizer = TrafficAccidentVisualizer(analysis_file)
    return visualizer.generate_full_report()


if __name__ == "__main__":
    # 경주시 보고서 생성 예시
    gyeongju_file = "RegionRiskAnalyzer_경주시_analysis.json"
    
    if os.path.exists(gyeongju_file):
        print("🏛️ 경주시 교통사고 분석 보고서 생성")
        report_dir = create_visualization_report(gyeongju_file)
        print(f"\n📋 보고서가 '{report_dir}' 디렉토리에 생성되었습니다.")
    else:
        print(f"❌ 파일을 찾을 수 없습니다: {gyeongju_file}")