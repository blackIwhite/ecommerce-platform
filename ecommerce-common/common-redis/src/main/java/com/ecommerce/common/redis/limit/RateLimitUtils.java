package com.ecommerce.common.redis.limit;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class RateLimitUtils {

    private final StringRedisTemplate redisTemplate;

    private static final String RATE_LIMIT_PREFIX = "rate:limit:";

    public boolean isAllowed(String key, int limit, long windowMs) {
        String redisKey = RATE_LIMIT_PREFIX + key;
        long now = Instant.now().toEpochMilli();
        long windowStart = now - windowMs;

        redisTemplate.opsForZSet().removeRangeByScore(redisKey, 0, windowStart);
        Long count = redisTemplate.opsForZSet().size(redisKey);

        if (count != null && count >= limit) {
            return false;
        }

        redisTemplate.opsForZSet().add(redisKey, now + ":" + Math.random(), now);
        redisTemplate.expire(redisKey, java.time.Duration.ofMillis(windowMs * 2));
        return true;
    }

    public boolean isAllowed(String key, int limit) {
        return isAllowed(key, limit, 60_000);
    }

    public long getRemaining(String key, long windowMs) {
        String redisKey = RATE_LIMIT_PREFIX + key;
        long now = Instant.now().toEpochMilli();
        long windowStart = now - windowMs;

        redisTemplate.opsForZSet().removeRangeByScore(redisKey, 0, windowStart);
        Long count = redisTemplate.opsForZSet().size(redisKey);
        return count != null ? count : 0;
    }
}
