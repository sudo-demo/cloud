package com.example.common.util;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 分布式锁工具类（基于Redisson实现）
 * 功能：提供分布式环境下的线程安全锁操作，解决分布式系统中的并发问题
 * 核心特性：
 * 1. 可重入锁：同一线程可多次获取同一把锁
 * 2. 自动过期：避免死锁（获取锁后未释放的情况）
 * 3. 线程安全：通过volatile和synchronized确保多线程环境下的正确性
 */
@Component
public class LockUtil {
    // 日志记录器（用于记录锁操作日志，便于问题排查）
    private static final Logger log = LoggerFactory.getLogger(LockUtil.class);

    // 锁的固定前缀（用于区分业务锁和其他缓存键）
    private final static String LOCK_PREFIX = "lock:";

    /**
     * Redisson客户端实例（用于操作Redis分布式锁）
     * volatile修饰：确保多线程环境下的可见性，避免读取到未初始化的null值
     */
    private static volatile RedissonClient redissonClient;

    /**
     * 业务前缀（用于区分不同服务/环境的锁，避免跨服务锁冲突）
     * 例如：服务A的前缀为"order:"，服务B的前缀为"user:"
     */
    private static volatile String keyPrefix;

    /**
     * 设置Redisson客户端（初始化方法）
     * 线程安全：通过synchronized确保初始化过程的原子性
     * @param client Redisson客户端实例（非null）
     * @throws IllegalArgumentException 当client为null时抛出
     */
    public static synchronized void setRedissonClient(RedissonClient client) {
        if (client == null) {
            throw new IllegalArgumentException("RedissonClient不能为null");
        }
        if (redissonClient == null) {
            redissonClient = client;
        }
    }

    /**
     * 设置业务前缀（初始化方法）
     * 线程安全：通过synchronized确保前缀设置的原子性
     * @param key 业务前缀（允许为空，为空时不添加额外前缀）
     */
    public static synchronized void setKeyPrefix(String key) {
        // 统一处理空字符串和null，避免后续拼接问题
        keyPrefix = (key == null || key.trim().isEmpty()) ? "" : key.trim();
    }

    /**
     * 生成带完整前缀的锁键
     * 格式：[业务前缀][固定锁前缀][原始键]
     * 示例：业务前缀"order:" + 固定前缀"lock:" + 原始键"1001" → "order:lock:1001"
     * @param key 原始锁键（非null）
     * @return 带完整前缀的锁键
     * @throws IllegalArgumentException 当原始键为null或空时抛出
     */
    private static String getPrefixedKey(String key) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("锁的原始键不能为null或空");
        }
        // 拼接前缀（业务前缀可能为空，固定锁前缀必存在）
        return keyPrefix + LOCK_PREFIX + key.trim();
    }

    /**
     * 尝试获取分布式锁
     * 特性：可重入锁，支持等待时间和自动释放时间，避免死锁
     * @param lockKey 原始锁键（用于标识具体资源，如"order:1001"）
     * @param waitTime 最大等待时间（秒）：获取锁时最多等待多久
     *                 若为0则立即返回，不等待
     * @param leaseTime 锁持有时间（秒）：超过此时间自动释放，防止死锁
     *                  若为-1则需手动释放（不建议，风险高）
     * @return true：获取锁成功；false：获取锁失败（超时或被中断）
     * @throws IllegalStateException 当RedissonClient未初始化时抛出
     */
    public static boolean tryLock(String lockKey, long waitTime, long leaseTime) {
        // 校验RedissonClient是否已初始化
        if (redissonClient == null) {
            throw new IllegalStateException("RedissonClient未初始化，请先调用setRedissonClient方法");
        }

        String prefixedKey = getPrefixedKey(lockKey);
        RLock lock = redissonClient.getLock(prefixedKey);

        try {
            // 尝试获取锁：waitTime为等待时间，leaseTime为持有时间
            boolean success = lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
            if (success) {
                log.debug("线程[{}]成功获取锁：{}", Thread.currentThread().getId(), prefixedKey);
            } else {
                log.debug("线程[{}]获取锁失败（超时）：{}", Thread.currentThread().getId(), prefixedKey);
            }
            return success;
        } catch (InterruptedException e) {
            // 线程被中断时，恢复中断状态并返回失败
            Thread.currentThread().interrupt();
            log.warn("线程[{}]获取锁时被中断：{}", Thread.currentThread().getId(), prefixedKey, e);
            return false;
        } catch (Exception e) {
            // 其他异常（如Redis连接失败）
            log.error("线程[{}]获取锁发生异常：{}", Thread.currentThread().getId(), prefixedKey, e);
            return false;
        }
    }

    /**
     * 释放分布式锁
     * 安全机制：仅释放当前线程持有的锁，避免释放其他线程的锁
     * @param lockKey 原始锁键（需与获取锁时的键一致）
     * @throws IllegalStateException 当RedissonClient未初始化时抛出
     */
    public static void unlock(String lockKey) {
        // 校验RedissonClient是否已初始化
        if (redissonClient == null) {
            throw new IllegalStateException("RedissonClient未初始化，请先调用setRedissonClient方法");
        }

        String prefixedKey = getPrefixedKey(lockKey);
        RLock lock = redissonClient.getLock(prefixedKey);

        try {
            // 检查当前线程是否持有该锁（防止误释放）
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("线程[{}]成功释放锁：{}", Thread.currentThread().getId(), prefixedKey);
            } else {
                // 非当前线程持有锁时，不执行释放操作
                log.warn("线程[{}]尝试释放非自身持有的锁：{}", Thread.currentThread().getId(), prefixedKey);
            }
        } catch (Exception e) {
            log.error("线程[{}]释放锁发生异常：{}", Thread.currentThread().getId(), prefixedKey, e);
        }
    }
}
