package com.ecommerce.common.redis.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Distributed lock based on Redisson.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisLock {

    private static final String LOCK_PREFIX = "lock:";

    private final RedissonClient redissonClient;

    /**
     * Try to acquire a distributed lock.
     *
     * @param key       lock key
     * @param waitTime  maximum time to wait for the lock (seconds)
     * @param leaseTime automatic release time after acquisition (seconds)
     * @return true if the lock was acquired
     */
    public boolean tryLock(String key, long waitTime, long leaseTime) {
        RLock lock = redissonClient.getLock(LOCK_PREFIX + key);
        try {
            boolean acquired = lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
            if (acquired) {
                log.debug("Distributed lock acquired: {}", key);
            } else {
                log.warn("Failed to acquire distributed lock: {}", key);
            }
            return acquired;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while acquiring distributed lock: {}", key, e);
            return false;
        }
    }

    /**
     * Release a distributed lock.
     *
     * @param key lock key
     */
    public void unlock(String key) {
        RLock lock = redissonClient.getLock(LOCK_PREFIX + key);
        try {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("Distributed lock released: {}", key);
            }
        } catch (Exception e) {
            log.error("Failed to release distributed lock: {}", key, e);
        }
    }
}
