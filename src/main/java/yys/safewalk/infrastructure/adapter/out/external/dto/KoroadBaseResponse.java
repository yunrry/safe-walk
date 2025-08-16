// infrastructure/adapter/out/external/dto/KoroadBaseResponse.java
package yys.safewalk.infrastructure.adapter.out.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class KoroadBaseResponse {

    /**
     * API 호출 결과 코드
     * 00: 성공
     * 03: 데이터없음
     * 10: 파라미터오류
     * 99: 알수없는오류
     */
    @JsonProperty("resultCode")
    private String resultCode;

    /**
     * API 호출 결과 메시지
     */
    @JsonProperty("resultMsg")
    private String resultMsg;

    /**
     * 응답 수신 시간 (로컬)
     */
    private LocalDateTime responseTime = LocalDateTime.now();

    // ===========================================
    // 응답 상태 확인 메서드들
    // ===========================================

    /**
     * API 호출이 성공했는지 확인
     */
    public boolean isSuccess() {
        return "00".equals(resultCode);
    }

    /**
     * 데이터가 없는 상태인지 확인
     */
    public boolean isNoData() {
        return "03".equals(resultCode);
    }

    /**
     * 파라미터 오류인지 확인
     */
    public boolean isParameterError() {
        return "10".equals(resultCode);
    }

    /**
     * 알 수 없는 오류인지 확인
     */
    public boolean isUnknownError() {
        return "99".equals(resultCode);
    }

    /**
     * 에러 상태인지 확인 (성공이 아닌 모든 경우)
     */
    public boolean isError() {
        return !isSuccess();
    }

    /**
     * 재시도 가능한 오류인지 확인
     */
    public boolean isRetryableError() {
        // 파라미터 오류는 재시도해도 동일한 결과이므로 재시도 불가
        // 알 수 없는 오류는 재시도 가능
        // 데이터 없음은 재시도 불필요
        return isUnknownError();
    }

    // ===========================================
    // 결과 코드별 설명 메서드들
    // ===========================================

    /**
     * 결과 코드에 대한 한글 설명 반환
     */
    public String getResultCodeDescription() {
        if (resultCode == null) {
            return "알 수 없는 상태";
        }

        return switch (resultCode) {
            case "00" -> "성공";
            case "03" -> "데이터 없음";
            case "10" -> "파라미터 오류";
            case "99" -> "알 수 없는 오류";
            default -> "정의되지 않은 결과 코드: " + resultCode;
        };
    }

    /**
     * 결과 상태에 따른 로그 레벨 제안
     */
    public LogLevel getRecommendedLogLevel() {
        if (isSuccess()) {
            return LogLevel.INFO;
        } else if (isNoData()) {
            return LogLevel.WARN;
        } else if (isParameterError()) {
            return LogLevel.ERROR;
        } else {
            return LogLevel.ERROR;
        }
    }

    /**
     * 상태에 따른 HTTP 상태 코드 매핑
     */
    public int getMappedHttpStatusCode() {
        if (resultCode == null) {
            return 500; // Internal Server Error
        }

        return switch (resultCode) {
            case "00" -> 200; // OK
            case "03" -> 404; // Not Found
            case "10" -> 400; // Bad Request
            case "99" -> 500; // Internal Server Error
            default -> 500;
        };
    }

    // ===========================================
    // 응답 검증 메서드들
    // ===========================================

    /**
     * 응답 기본 필드 유효성 검증
     */
    public boolean isValidResponse() {
        return resultCode != null && !resultCode.trim().isEmpty() &&
                resultMsg != null && !resultMsg.trim().isEmpty();
    }

    /**
     * 정상적으로 처리 가능한 응답인지 확인
     */
    public boolean isProcessableResponse() {
        return isValidResponse() && (isSuccess() || isNoData());
    }

    /**
     * 즉시 에러 처리가 필요한 응답인지 확인
     */
    public boolean requiresImmediateErrorHandling() {
        return isParameterError() || isUnknownError();
    }

    // ===========================================
    // 디버깅 및 로깅 지원 메서드들
    // ===========================================

    /**
     * 응답 요약 정보 (로깅용)
     */
    public String getResponseSummary() {
        return String.format("코드:%s(%s), 메시지:%s, 시간:%s",
                resultCode,
                getResultCodeDescription(),
                resultMsg,
                responseTime);
    }

    /**
     * 상세 응답 정보 (디버깅용)
     */
    public String getDetailedInfo() {
        return String.format("""
                도로교통공단 API 응답 상세정보:
                - 결과 코드: %s (%s)
                - 결과 메시지: %s
                - 성공 여부: %s
                - 재시도 가능: %s
                - 권장 로그 레벨: %s
                - HTTP 매핑 코드: %d
                - 응답 시간: %s
                """,
                resultCode, getResultCodeDescription(),
                resultMsg,
                isSuccess() ? "예" : "아니오",
                isRetryableError() ? "예" : "아니오",
                getRecommendedLogLevel(),
                getMappedHttpStatusCode(),
                responseTime);
    }

    /**
     * 에러 응답인 경우 예외 메시지 생성
     */
    public String generateErrorMessage() {
        if (isSuccess()) {
            return null;
        }

        return String.format("도로교통공단 API 오류 - 코드:%s, 메시지:%s, 설명:%s",
                resultCode,
                resultMsg,
                getResultCodeDescription());
    }

    // ===========================================
    // 응답 시간 관련 메서드들
    // ===========================================

    /**
     * 응답이 오래된 것인지 확인 (기본: 1시간)
     */
    public boolean isStaleResponse() {
        return isStaleResponse(60); // 60분
    }

    /**
     * 응답이 오래된 것인지 확인 (사용자 정의 시간)
     */
    public boolean isStaleResponse(int minutes) {
        if (responseTime == null) {
            return true;
        }
        return responseTime.isBefore(LocalDateTime.now().minusMinutes(minutes));
    }

    /**
     * 응답 시간으로부터 경과된 시간 (분 단위)
     */
    public long getMinutesSinceResponse() {
        if (responseTime == null) {
            return Long.MAX_VALUE;
        }
        return java.time.Duration.between(responseTime, LocalDateTime.now()).toMinutes();
    }

    @Override
    public String toString() {
        return String.format("KoroadBaseResponse{resultCode='%s', resultMsg='%s', isSuccess=%s}",
                resultCode, resultMsg, isSuccess());
    }

    // ===========================================
    // 내부 열거형: 로그 레벨
    // ===========================================

    public enum LogLevel {
        TRACE, DEBUG, INFO, WARN, ERROR, FATAL
    }
}