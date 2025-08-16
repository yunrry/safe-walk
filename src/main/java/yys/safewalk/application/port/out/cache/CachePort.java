package yys.safewalk.application.port.out.cache;

import org.springframework.data.redis.cache.CacheStatistics;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

public interface CachePort {
    <T> Optional<T> get(String key, Class<T> type);
    void put(String key, Object value);
    void put(String key, Object value, Duration ttl);
    void evict(String key);
    void evictAll(String pattern);
    boolean exists(String key);

    // 캐시 통계
    CacheStatistics getStatistics();
    Set<String> getKeys(String pattern);
}