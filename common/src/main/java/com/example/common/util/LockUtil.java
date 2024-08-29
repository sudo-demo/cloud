package com.example.common.util;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class LockUtil {

    private final static String LOCK_PREFIX = "lock:";

    private static RedissonClient redissonClient;// Redisson 客户端实例
    private static String keyPrefix;// 锁的前缀

    // 设置 Redisson 客户端
    public static void setRedissonClient(RedissonClient redissonClient) {
        LockUtil.redissonClient = redissonClient;
    }

    // 设置锁的前缀
    public static void setKeyPrefix(String key) {
        LockUtil.keyPrefix = key;
    }

    /**
     * 获取带上前缀的键
     *
     * @param key 原始键
     * @return 带前缀的键
     */
    private static String getPrefixedKey(String key) {
        return keyPrefix + LOCK_PREFIX + key; // 添加前缀
    }

    /**
     * 获取分布式锁
     *
     * @param lockKey   锁的键
     * @param waitTime  等待时间
     * @param leaseTime 锁的持有时间
     * @return 是否成功获取锁
     */
    public static boolean tryLock(String lockKey, long waitTime, long leaseTime) {
        RLock lock = redissonClient.getLock(getPrefixedKey(lockKey));
        try {
            // 尝试获取锁，等待时间不超过 waitTime 秒，锁的持有时间不超过 leaseTime 秒
            return lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);//TimeUnit.SECONDS 表示时间以秒为单位。
        } catch (InterruptedException e) {
            // 如果线程被中断，恢复中断状态并返回 false
            Thread.currentThread().interrupt(); // 恢复中断状态
            return false;
        }
    }

    /**
     * 释放分布式锁
     *
     * @param lockKey 锁的键
     */
    public static void unlock(String lockKey) {
        // 获取分布式锁实例
        RLock lock = redissonClient.getLock(getPrefixedKey(lockKey));
        // 检查当前线程是否持有锁
        if (lock.isHeldByCurrentThread()) {
            // 释放锁
            lock.unlock();
        }
    }
}
