package com.example.common.util;

import com.example.common.config.RedisConfig;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Redis工具类（线程安全版）
 * 功能：封装Redis的常用操作，支持String、Hash、Set、List等多种数据结构
 * 设计原则：
 * 1. 基于Spring RedisTemplate实现，利用其线程安全特性
 * 2. 支持键前缀，避免不同业务的键冲突
 * 3. 提供友好的返回值和异常处理，简化业务层调用
 */
@Component
public class RedisUtil {

    /**
     * Redis核心操作模板
     * 由Spring容器注入，RedisTemplate内部通过连接池管理Redis连接，本身线程安全
     */
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 直接注入 Spring 提供的 StringRedisTemplate
     * 操作字符串
     */
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 键前缀（用于区分不同业务/环境的Redis键）
     * volatile修饰：保证多线程环境下的可见性，防止读取到旧值
     */
    private volatile String keyPrefix;

    /**
     * 注入Redis配置类（用于获取配置的键前缀）
     */
    @Resource
    private RedisConfig redisConfig;

    /**
     * 初始化方法（依赖注入完成后自动执行）
     * 作用：从配置类中获取键前缀，替代原有的静态setter方法，避免线程安全问题
     */
    @PostConstruct
    public void init() {
        this.keyPrefix = redisConfig.getKeyPrefix();
    }

    /**
     * 生成带前缀的键
     * 业务意义：通过前缀区分不同服务或环境的键（如"order:"、"user:"），避免键名冲突
     *
     * @param key 原始键（业务层传入的未加前缀的键）
     * @return 带前缀的完整键（如前缀"order:" + 原始键"1001" → "order:1001"）
     */
    private String getPrefixedKey(String key) {
        if (key == null) {
            return null;
        }
        // 前缀为空时直接返回原始键，否则拼接前缀
        return (keyPrefix != null && !keyPrefix.isEmpty()) ? keyPrefix + key : key;
    }

    // ============================= 通用操作 =============================

    /**
     * 设置键的过期时间
     *
     * @param key  原始键（无需带前缀）
     * @param time 过期时间（秒），<=0时表示不设置过期时间
     * @return 操作是否成功（true=成功，false=失败）
     */
    public boolean expire(String key, long time) {
        try {
            if (time > 0) {
                // 调用RedisTemplate的expire方法设置过期时间
                redisTemplate.expire(getPrefixedKey(key), time, TimeUnit.SECONDS);
            }
            return true;
        } catch (Exception e) {
            // 实际生产环境建议使用日志框架（如SLF4J）记录异常
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取键的剩余过期时间
     *
     * @param key 原始键
     * @return 剩余时间（秒），0表示永久有效
     */
    public long getExpire(String key) {
        return redisTemplate.getExpire(getPrefixedKey(key), TimeUnit.SECONDS);
    }

    /**
     * 判断键是否存在
     *
     * @param key 原始键
     * @return true=存在，false=不存在
     */
    public boolean hasKey(String key) {
        try {
            return redisTemplate.hasKey(getPrefixedKey(key));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除缓存（支持批量删除）
     *
     * @param key 可变参数，传入一个或多个原始键
     */
    public void del(String... key) {
        if (key != null && key.length > 0) {
            if (key.length == 1) {
                // 单个键删除
                redisTemplate.delete(getPrefixedKey(key[0]));
            } else {
                // 批量删除：修复类型转换问题
                // 使用Collectors.toList()确保类型正确
                Collection<String> prefixedKeys = new ArrayList<>();
                for (String k : key) {
                    prefixedKeys.add(getPrefixedKey(k));
                }
                redisTemplate.delete(prefixedKeys);
            }
        }
    }

    // ============================= String类型操作 =============================

    /**
     * 获取String类型的缓存值
     *
     * @param key 原始键
     * @return 缓存值（未命中返回null）
     */
    public Object get(String key) {
        return key == null ? null : redisTemplate.opsForValue().get(getPrefixedKey(key));
    }

    /**
     * 存储String类型的缓存
     *
     * @param key   原始键
     * @param value 值（支持任意可序列化的对象，如String、实体类等）
     * @return 操作是否成功
     */
    public boolean set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(getPrefixedKey(key), value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 存储String类型的缓存并设置过期时间
     *
     * @param key   原始键
     * @param value 值
     * @param time  过期时间（秒），<=0时永久有效
     * @return 操作是否成功
     */
    public boolean set(String key, Object value, long time) {
        try {
            if (time > 0) {
                redisTemplate.opsForValue().set(getPrefixedKey(key), value, time, TimeUnit.SECONDS);
            } else {
                set(key, value); // 调用无过期时间的set方法
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 自增操作（原子操作，线程安全）
     * 应用场景：计数器（如文章阅读量、订单编号生成）
     *
     * @param key   原始键
     * @param delta 递增步长（必须>0）
     * @return 递增后的值
     * @throws RuntimeException 当步长<=0时抛出
     */
    public long incr(String key, long delta) {
        if (delta <= 0) {
            throw new RuntimeException("递增因子必须大于0");
        }
        return redisTemplate.opsForValue().increment(getPrefixedKey(key), delta);
    }

    /**
     * 自减操作（原子操作，线程安全）
     * 应用场景：库存扣减等
     *
     * @param key   原始键
     * @param delta 递减步长（必须>0）
     * @return 递减后的值
     * @throws RuntimeException 当步长<=0时抛出
     */
    public long decr(String key, long delta) {
        if (delta <= 0) {
            throw new RuntimeException("递减因子必须大于0");
        }
        // Redis的increment方法支持负数步长（等价于递减）
        return redisTemplate.opsForValue().increment(getPrefixedKey(key), -delta);
    }

    // ============================= Hash类型操作 =============================

    /**
     * 获取Hash类型中指定字段的值
     * 应用场景：存储对象的多个属性（如用户信息：id、name、age）
     *
     * @param key  原始键（Hash表的键）
     * @param item 字段名（Hash表中的字段）
     * @param <T>  返回值类型
     * @return 字段值（未命中返回null）
     */
    public <T> T hget(String key, String item) {
        HashOperations<String, String, T> opsForHash = redisTemplate.opsForHash();
        return opsForHash.get(getPrefixedKey(key), item);
    }

    /**
     * 获取Hash类型中所有字段和值
     *
     * @param key 原始键
     * @return 包含所有字段和值的Map
     */
    public Map<Object, Object> hmget(String key) {
        return redisTemplate.opsForHash().entries(getPrefixedKey(key));
    }

    /**
     * 批量存储Hash类型的字段和值
     *
     * @param key 原始键
     * @param map 包含多个字段和值的Map
     * @return 操作是否成功
     */
    public boolean hmset(String key, Map<String, Object> map) {
        try {
            redisTemplate.opsForHash().putAll(getPrefixedKey(key), map);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 批量存储Hash类型的字段和值并设置过期时间
     *
     * @param key  原始键
     * @param map  字段和值的Map
     * @param time 过期时间（秒）
     * @return 操作是否成功
     */
    public boolean hmset(String key, Map<String, Object> map, long time) {
        try {
            redisTemplate.opsForHash().putAll(getPrefixedKey(key), map);
            if (time > 0) {
                expire(key, time); // 调用通用的过期时间设置方法
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 存储Hash类型中单个字段和值
     *
     * @param key   原始键
     * @param item  字段名
     * @param value 字段值
     * @param <T>   字段值类型
     * @return 操作是否成功
     */
    public <T> boolean hset(String key, String item, T value) {
        try {
            redisTemplate.opsForHash().put(getPrefixedKey(key), item, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 存储Hash类型中单个字段和值并设置过期时间
     *
     * @param key   原始键
     * @param item  字段名
     * @param value 字段值
     * @param time  过期时间（秒）
     * @param <T>   字段值类型
     * @return 操作是否成功
     */
    public <T> boolean hset(String key, String item, T value, long time) {
        try {
            redisTemplate.opsForHash().put(getPrefixedKey(key), item, value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除Hash类型中的指定字段
     *
     * @param key  原始键
     * @param item 可变参数，传入一个或多个字段名
     */
    public void hdel(String key, Object... item) {
        redisTemplate.opsForHash().delete(getPrefixedKey(key), item);
    }

    /**
     * 判断Hash类型中是否存在指定字段
     *
     * @param key  原始键
     * @param item 字段名
     * @return true=存在，false=不存在
     */
    public boolean hHasKey(String key, String item) {
        return redisTemplate.opsForHash().hasKey(getPrefixedKey(key), item);
    }

    /**
     * Hash字段值自增（原子操作）
     *
     * @param key  原始键
     * @param item 字段名
     * @param by   递增步长（>0）
     * @return 递增后的值
     */
    public double hincr(String key, String item, double by) {
        return redisTemplate.opsForHash().increment(getPrefixedKey(key), item, by);
    }

    /**
     * Hash字段值自减（原子操作）
     *
     * @param key  原始键
     * @param item 字段名
     * @param by   递减步长（>0）
     * @return 递减后的值
     */
    public double hdecr(String key, String item, double by) {
        return redisTemplate.opsForHash().increment(getPrefixedKey(key), item, -by);
    }

    // ============================= Set类型操作 =============================

    /**
     * 获取Set类型中的所有元素
     * 应用场景：存储不重复的集合（如用户标签、好友列表）
     *
     * @param key 原始键
     * @return 包含所有元素的Set集合
     */
    public Set<Object> sGet(String key) {
        try {
            return redisTemplate.opsForSet().members(getPrefixedKey(key));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 判断Set类型中是否包含指定元素
     *
     * @param key   原始键
     * @param value 元素值
     * @return true=包含，false=不包含
     */
    public boolean sHasKey(String key, Object value) {
        try {
            return redisTemplate.opsForSet().isMember(getPrefixedKey(key), value);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 向Set类型中添加元素
     *
     * @param key    原始键
     * @param values 可变参数，传入一个或多个元素
     * @return 成功添加的元素个数（已存在的元素不会重复添加）
     */
    public long sSet(String key, Object... values) {
        try {
            return redisTemplate.opsForSet().add(getPrefixedKey(key), values);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 向Set类型中添加元素并设置过期时间
     *
     * @param key    原始键
     * @param time   过期时间（秒）
     * @param values 可变参数，传入一个或多个元素
     * @return 成功添加的元素个数
     */
    public long sSetAndTime(String key, long time, Object... values) {
        try {
            Long count = redisTemplate.opsForSet().add(getPrefixedKey(key), values);
            if (time > 0) {
                expire(key, time);
            }
            return count;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 获取Set类型的元素个数
     *
     * @param key 原始键
     * @return 元素个数
     */
    public long sGetSetSize(String key) {
        try {
            return redisTemplate.opsForSet().size(getPrefixedKey(key));
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 从Set类型中移除指定元素
     *
     * @param key    原始键
     * @param values 可变参数，传入一个或多个元素
     * @return 成功移除的元素个数
     */
    public long setRemove(String key, Object... values) {
        try {
            return redisTemplate.opsForSet().remove(getPrefixedKey(key), values);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    // ============================= List类型操作 =============================

    /**
     * 获取List类型中指定范围的元素
     * 应用场景：消息队列、排行榜等
     *
     * @param key   原始键
     * @param start 起始索引（0表示第一个元素）
     * @param end   结束索引（-1表示最后一个元素）
     * @return 包含指定范围元素的List
     */
    public List<Object> lGet(String key, long start, long end) {
        try {
            return redisTemplate.opsForList().range(getPrefixedKey(key), start, end);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取List类型的长度
     *
     * @param key 原始键
     * @return 列表长度
     */
    public long lGetListSize(String key) {
        try {
            return redisTemplate.opsForList().size(getPrefixedKey(key));
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 通过索引获取List类型中的元素
     *
     * @param key   原始键
     * @param index 索引（正数：从头部开始；负数：从尾部开始，-1表示最后一个）
     * @return 对应索引的元素
     */
    public Object lGetIndex(String key, long index) {
        try {
            return redisTemplate.opsForList().index(getPrefixedKey(key), index);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 向List类型的尾部添加一个元素（右压栈）
     *
     * @param key   原始键
     * @param value 元素值
     * @return 操作是否成功
     */
    public boolean lSet(String key, Object value) {
        try {
            redisTemplate.opsForList().rightPush(getPrefixedKey(key), value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 向List类型的尾部添加一个元素并设置过期时间
     *
     * @param key   原始键
     * @param value 元素值
     * @param time  过期时间（秒）
     * @return 操作是否成功
     */
    public boolean lSet(String key, Object value, long time) {
        try {
            redisTemplate.opsForList().rightPush(getPrefixedKey(key), value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 向List类型的尾部批量添加元素
     *
     * @param key   原始键
     * @param value 包含多个元素的List
     * @return 操作是否成功
     */
    public boolean lSet(String key, List<Object> value) {
        try {
            redisTemplate.opsForList().rightPushAll(getPrefixedKey(key), value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 向List类型的尾部批量添加元素并设置过期时间
     *
     * @param key   原始键
     * @param value 包含多个元素的List
     * @param time  过期时间（秒）
     * @return 操作是否成功
     */
    public boolean lSet(String key, List<Object> value, long time) {
        try {
            redisTemplate.opsForList().rightPushAll(getPrefixedKey(key), value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 根据索引修改List类型中的元素
     *
     * @param key   原始键
     * @param index 索引
     * @param value 新值
     * @return 操作是否成功
     */
    public boolean lUpdateIndex(String key, long index, Object value) {
        try {
            redisTemplate.opsForList().set(getPrefixedKey(key), index, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 从List类型中移除指定数量的元素
     *
     * @param key   原始键
     * @param count 移除数量（正数：从头部开始；负数：从尾部开始）
     * @param value 要移除的元素值
     * @return 成功移除的元素个数
     */
    public long lRemove(String key, long count, Object value) {
        try {
            return redisTemplate.opsForList().remove(getPrefixedKey(key), count, value);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 动态修改键前缀（线程安全）
     * 注意：修改后新的键会使用新前缀，但已存在的键不受影响
     *
     * @param newPrefix 新的前缀
     */
    public synchronized void updateKeyPrefix(String newPrefix) {
        this.keyPrefix = newPrefix;
    }


    // Lua脚本：加锁（原子操作）
    private static final String LOCK_SCRIPT =
            "if redis.call('setnx', KEYS[1], ARGV[1]) == 1 then " +
                    "   return redis.call('pexpire', KEYS[1], ARGV[2]) " +
                    "else " +
                    "   return 0 " +
                    "end";


    private static final RedisScript<Long> LOCK_SCRIPT_INSTANCE =
            new DefaultRedisScript<>(LOCK_SCRIPT, Long.class);

    /**
     * 尝试加锁（原子操作）
     * 业务意义：在分布式环境中实现互斥访问，防止多个实例同时操作同一资源
     *
     * @param key        锁的键（业务标识，如"order:lock:1001"）
     * @param value      锁的值（建议使用UUID，用于安全解锁）
     * @param expireTime 锁的过期时间（防止死锁）
     * @param timeUnit   时间单位
     * @return true-加锁成功，false-加锁失败（锁已被其他客户端持有）
     */
    public boolean tryLock(String key, String value, long expireTime, TimeUnit timeUnit) {
        try {
            Long result = redisTemplate.execute(LOCK_SCRIPT_INSTANCE,
                    Collections.singletonList(getPrefixedKey(key)),
                    value,
                    timeUnit.toMillis(expireTime)
            );
            return Objects.equals(result, 1L);
        } catch (Exception e) {
            e.printStackTrace();
            // 记录日志，但不要抛出异常影响业务
            return false;
        }
    }

    // Lua脚本：解锁（原子操作，只有值匹配时才删除）
    private static final String UNLOCK_SCRIPT =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                    "   return redis.call('del', KEYS[1]) " +
                    "else " +
                    "   return 0 " +
                    "end";

    private static final RedisScript<Long> UNLOCK_SCRIPT_INSTANCE =
            new DefaultRedisScript<>(UNLOCK_SCRIPT, Long.class);

    /**
     * 解锁（原子操作）
     * 业务意义：安全释放分布式锁，避免误删其他客户端的锁
     *
     * @param key   锁的键
     * @param value 锁的值（必须与加锁时的值一致）
     * @return true-解锁成功，false-解锁失败（锁不存在或值不匹配）
     */
    public boolean unlock(String key, String value) {
        try {
            Long result = stringRedisTemplate.execute(UNLOCK_SCRIPT_INSTANCE,
                    Collections.singletonList(getPrefixedKey(key)),
                    value);
            return result == 1;
        } catch (Exception e) {
            // 记录日志
            return false;
        }
    }
}
