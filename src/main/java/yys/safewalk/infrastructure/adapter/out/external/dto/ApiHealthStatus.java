package yys.safewalk.infrastructure.adapter.out.external.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// ApiHealthStatus.java
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiHealthStatus {
    private boolean available;
    private long lastChecked;
    private long responseTime;
}