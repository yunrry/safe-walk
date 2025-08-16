package yys.safewalk.domain.riskarea.model;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 위험도 레벨 열거형
 * 도로교통공단 API 및 서비스 전반에서 사용되는 위험도 등급 정의
 */
public enum RiskLevel {

    /**
     * 매우 위험 (최고 위험도)
     * - 도로위험지수 등급 4
     * - 사고다발지역 중 치사율 10% 이상
     * - 즉시 주의가 필요한 구간
     */
    VERY_DANGER(4, "매우위험", "#FF0000", "즉시 주의 필요", 90, 100),

    /**
     * 위험 (높은 위험도)
     * - 도로위험지수 등급 3
     * - 사고다발지역 중 치사율 5-10%
     * - 각별한 주의가 필요한 구간
     */
    DANGER(3, "위험", "#FF8000", "각별한 주의 필요", 70, 89),

    /**
     * 주의 (보통 위험도)
     * - 도로위험지수 등급 2
     * - 사고다발지역 중 치사율 2-5%
     * - 주의를 기울여야 하는 구간
     */
    CAUTION(2, "주의", "#FFFF00", "주의 필요", 40, 69),

    /**
     * 안전 (낮은 위험도)
     * - 도로위험지수 등급 1
     * - 사고다발지역이 아니거나 치사율 2% 미만
     * - 상대적으로 안전한 구간
     */
    SAFE(1, "안전", "#00FF00", "상대적으로 안전", 0, 39),

    /**
     * 알 수 없음 (데이터 없음)
     * - 위험도 평가 불가
     * - 데이터 부족 또는 오류
     */
    UNKNOWN(0, "알수없음", "#808080", "위험도 평가 불가", -1, -1);

    private final int level;
    private final String description;
    private final String colorCode;
    private final String message;
    private final int minScore;
    private final int maxScore;

    RiskLevel(int level, String description, String colorCode, String message, int minScore, int maxScore) {
        this.level = level;
        this.description = description;
        this.colorCode = colorCode;
        this.message = message;
        this.minScore = minScore;
        this.maxScore = maxScore;
    }

    // ===========================================
    // Getter 메서드들
    // ===========================================

    /**
     * 위험도 레벨 (숫자)
     */
    public int getLevel() {
        return level;
    }

    /**
     * 위험도 설명 (한글)
     */
    public String getDescription() {
        return description;
    }

    /**
     * UI 표시용 색상 코드
     */
    public String getColorCode() {
        return colorCode;
    }

    /**
     * 사용자 안내 메시지
     */
    public String getMessage() {
        return message;
    }

    /**
     * 최소 위험도 점수
     */
    public int getMinScore() {
        return minScore;
    }

    /**
     * 최대 위험도 점수
     */
    public int getMaxScore() {
        return maxScore;
    }

    // ===========================================
    // 정적 팩토리 메서드들
    // ===========================================

    /**
     * 도로교통공단 API 위험지수 등급으로부터 RiskLevel 변환
     * @param grade API 등급 (1:안전, 2:주의, 3:심각, 4:위험)
     */
    public static RiskLevel fromApiGrade(Integer grade) {
        if (grade == null) {
            return UNKNOWN;
        }

        return switch (grade) {
            case 1 -> SAFE;
            case 2 -> CAUTION;
            case 3 -> DANGER;
            case 4 -> VERY_DANGER;
            default -> UNKNOWN;
        };
    }

    /**
     * 위험도 점수로부터 RiskLevel 결정
     * @param score 위험도 점수 (0-100)
     */
    public static RiskLevel fromScore(double score) {
        if (score < 0) {
            return UNKNOWN;
        }

        return Arrays.stream(values())
                .filter(level -> level != UNKNOWN)
                .filter(level -> score >= level.minScore && score <= level.maxScore)
                .findFirst()
                .orElse(UNKNOWN);
    }

    /**
     * 치사율로부터 RiskLevel 결정
     * @param fatalityRate 치사율 (%)
     */
    public static RiskLevel fromFatalityRate(double fatalityRate) {
        if (fatalityRate >= 10.0) {
            return VERY_DANGER;
        } else if (fatalityRate >= 5.0) {
            return DANGER;
        } else if (fatalityRate >= 2.0) {
            return CAUTION;
        } else if (fatalityRate >= 0.0) {
            return SAFE;
        } else {
            return UNKNOWN;
        }
    }

    /**
     * 사고건수와 사상자수로부터 RiskLevel 결정
     * @param accidentCount 사고건수
     * @param casualtyCount 사상자수
     * @param deathCount 사망자수
     */
    public static RiskLevel fromAccidentData(int accidentCount, int casualtyCount, int deathCount) {
        if (accidentCount <= 0) {
            return SAFE;
        }

        // 치사율 계산
        double fatalityRate = casualtyCount > 0 ? (double) deathCount / casualtyCount * 100 : 0.0;

        // 사고 심각도 점수 계산 (사고건수 + 치사율 가중치)
        double severityScore = Math.min(accidentCount * 2 + fatalityRate * 5, 100);

        return fromScore(severityScore);
    }

    /**
     * 문자열로부터 RiskLevel 파싱
     * @param value 문자열 값 (대소문자 무관)
     */
    public static RiskLevel fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return UNKNOWN;
        }

        String normalized = value.trim().toUpperCase();

        return switch (normalized) {
            case "VERY_DANGER", "매우위험", "VERY_DANGEROUS", "4" -> VERY_DANGER;
            case "DANGER", "위험", "DANGEROUS", "3" -> DANGER;
            case "CAUTION", "주의", "WARNING", "2" -> CAUTION;
            case "SAFE", "안전", "1" -> SAFE;
            default -> UNKNOWN;
        };
    }

    // ===========================================
    // 비교 및 검증 메서드들
    // ===========================================

    /**
     * 다른 위험도보다 더 위험한지 확인
     */
    public boolean isMoreDangerousThan(RiskLevel other) {
        return this.level > other.level;
    }

    /**
     * 다른 위험도보다 더 안전한지 확인
     */
    public boolean isSaferThan(RiskLevel other) {
        return this.level < other.level;
    }

    /**
     * 위험한 수준인지 확인 (DANGER 이상)
     */
    public boolean isDangerous() {
        return this.level >= DANGER.level;
    }

    /**
     * 주의가 필요한 수준인지 확인 (CAUTION 이상)
     */
    public boolean requiresCaution() {
        return this.level >= CAUTION.level;
    }

    /**
     * 안전한 수준인지 확인
     */
    public boolean isSafe() {
        return this == SAFE;
    }

    /**
     * 알림이 필요한 수준인지 확인 (CAUTION 이상)
     */
    public boolean requiresNotification() {
        return this.level >= CAUTION.level;
    }

    /**
     * 즉시 경고가 필요한 수준인지 확인 (DANGER 이상)
     */
    public boolean requiresImmediateWarning() {
        return this.level >= DANGER.level;
    }

    // ===========================================
    // 유틸리티 메서드들
    // ===========================================

    /**
     * 위험도에 따른 권장 속도 제한 (km/h)
     */
    public int getRecommendedSpeedLimit() {
        return switch (this) {
            case VERY_DANGER -> 20;
            case DANGER -> 30;
            case CAUTION -> 40;
            case SAFE -> 50;
            case UNKNOWN -> 30; // 보수적 접근
        };
    }

    /**
     * 위험도에 따른 알림 우선순위 (높을수록 우선)
     */
    public int getNotificationPriority() {
        return switch (this) {
            case VERY_DANGER -> 4; // 최우선
            case DANGER -> 3;      // 높음
            case CAUTION -> 2;     // 보통
            case SAFE -> 1;        // 낮음
            case UNKNOWN -> 0;     // 없음
        };
    }

    /**
     * 위험도에 따른 권장 행동
     */
    public String getRecommendedAction() {
        return switch (this) {
            case VERY_DANGER -> "우회 경로 이용 권장, 부득이한 경우 최대 주의";
            case DANGER -> "속도 감소, 보행자 및 주변 차량 주의";
            case CAUTION -> "안전거리 확보, 신호 준수";
            case SAFE -> "평상시 안전운전 수칙 준수";
            case UNKNOWN -> "주의 깊은 운전";
        };
    }

    /**
     * 보행자를 위한 안전 가이드
     */
    public String getPedestrianSafetyGuide() {
        return switch (this) {
            case VERY_DANGER -> "이 구간 보행 피하기, 대중교통 이용 권장";
            case DANGER -> "보행시 극도로 주의, 가능하면 다른 경로 이용";
            case CAUTION -> "신호 준수, 횡단보도 이용, 주변 살피기";
            case SAFE -> "기본 보행 안전수칙 준수";
            case UNKNOWN -> "항상 주의하며 보행";
        };
    }

    /**
     * 위험도 상승 여부 확인
     */
    public boolean isUpgradeFrom(RiskLevel previousLevel) {
        return this.level > previousLevel.level;
    }

    /**
     * 위험도 하락 여부 확인
     */
    public boolean isDowngradeFrom(RiskLevel previousLevel) {
        return this.level < previousLevel.level;
    }

    /**
     * 모든 위험한 레벨들 반환 (CAUTION 이상)
     */
    public static List<RiskLevel> getDangerousLevels() {
        return Arrays.stream(values())
                .filter(level -> level.level >= CAUTION.level)
                .collect(Collectors.toList());
    }

    /**
     * 레벨 순서대로 정렬된 리스트 반환 (위험한 순서)
     */
    public static List<RiskLevel> getOrderedByDanger() {
        return Arrays.stream(values())
                .filter(level -> level != UNKNOWN)
                .sorted((a, b) -> Integer.compare(b.level, a.level))
                .collect(Collectors.toList());
    }

    /**
     * JSON 직렬화를 위한 문자열 반환
     */
    public String toJsonValue() {
        return name();
    }

    /**
     * 사용자 친화적 문자열 반환
     */
    public String toDisplayString() {
        return String.format("%s (%s)", description, message);
    }

    @Override
    public String toString() {
        return String.format("RiskLevel{level=%d, description='%s', color='%s'}",
                level, description, colorCode);
    }
}