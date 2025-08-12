package com.example.common.config;

import com.example.common.util.LockUtil;
import com.example.common.util.RedisUtil;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis配置类（线程安全优化版）
 * 功能：
 * 1. 配置RedisTemplate，自定义序列化方式
 * 2. 配置Spring缓存管理器（用于@Cacheable等注解）
 * 3. 配置Redisson客户端（用于分布式锁）
 * 4. 初始化RedisUtil和LockUtil的依赖
 */
@Configuration
@AutoConfigureAfter(RedisAutoConfiguration.class)
// 确保当前配置在Spring默认Redis配置之后加载，避免配置冲突
public class RedisConfig extends CachingConfigurerSupport {

    /**
     * 键前缀配置（从application.properties/yml中读取）
     * 示例：spring.redis.key-prefix=order-service:
     * 作用：区分不同服务的Redis键，避免跨服务键冲突
     */
    @Value("${spring.redis.key-prefix:}")
    private String keyPrefix;

    /**
     * 对外提供键前缀访问（供RedisUtil初始化使用）
     * @return 配置的键前缀
     */
    public String getKeyPrefix() {
        return keyPrefix;
    }

    /**
     * 配置RedisTemplate（核心操作模板）
     * 自定义序列化方式：
     * - 键（key）：使用StringRedisSerializer（字符串序列化）
     * - 值（value）：使用Jackson2JsonRedisSerializer（JSON序列化，支持对象类型）
     * @param redisConnectionFactory Redis连接工厂（由Spring自动配置）
     * @return 配置好的RedisTemplate实例
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory); // 设置连接工厂

        // 配置JSON序列化器（用于值的序列化）
        Jackson2JsonRedisSerializer<Object> jacksonSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper om = new ObjectMapper();
        // 配置ObjectMapper：允许访问所有字段，支持多态类型（反序列化时保留类型信息）
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance, // 类型验证器（允许所有子类型）
                ObjectMapper.DefaultTyping.NON_FINAL,   // 对非final类型启用多态支持
                JsonTypeInfo.As.WRAPPER_ARRAY          // 类型信息以数组形式包裹（避免JSON结构冲突）
        );
        jacksonSerializer.setObjectMapper(om);

        // 配置序列化器
        template.setKeySerializer(new StringRedisSerializer()); // 键序列化器（String）
        template.setValueSerializer(jacksonSerializer);         // 值序列化器（JSON）
        template.setHashKeySerializer(new StringRedisSerializer()); // Hash键序列化器
        template.setHashValueSerializer(jacksonSerializer);         // Hash值序列化器
        template.setDefaultSerializer(jacksonSerializer);           // 默认序列化器
        template.afterPropertiesSet(); // 初始化模板（必须调用，否则配置不生效）

        return template;
    }

    /**
     * 配置缓存管理器（用于Spring Cache注解）
     * 功能：将@Cacheable、@CachePut等注解的缓存操作映射到Redis
     * @param redisConnectionFactory Redis连接工厂
     * @return 配置好的CacheManager实例
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        // 配置缓存默认规则
        RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues() // 禁用缓存null值（避免缓存穿透）
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new Jackson2JsonRedisSerializer<>(Object.class))); // 值序列化

        // 构建缓存管理器
        return RedisCacheManager.builder(RedisCacheWriter.nonLockingRedisCacheWriter(redisConnectionFactory))
                .cacheDefaults(cacheConfig) // 应用默认配置
                .build();
    }

    /**
     * 配置StringRedisTemplate（专用于String类型操作的模板）
     * 用途：简化纯字符串类型的Redis操作（默认已配置String序列化器）
     * @param redisConnectionFactory Redis连接工厂
     * @return StringRedisTemplate实例
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }

    /**
     * 配置RedissonClient（分布式锁客户端）
     * 功能：提供分布式锁、分布式集合等高级功能，比原生Redis命令更易用
     * @param host     Redis主机地址（从配置文件读取）
     * @param port     Redis端口（从配置文件读取）
     * @param password Redis密码（从配置文件读取，默认为空）
     * @param database 数据库索引（从配置文件读取，默认为0）
     * @return RedissonClient实例
     */
    @Bean
    public RedissonClient redissonClient(
            @Value("${spring.redis.host}") String host,
            @Value("${spring.redis.port}") int port,
            @Value("${spring.redis.password:}") String password, // 允许密码为空
            @Value("${spring.redis.database:0}") int database) {

        Config config = new Config();
        // 配置单节点Redis（集群环境需修改为useClusterServers()）
        config.useSingleServer()
                .setAddress("redis://" + host + ":" + port) // Redis地址
                .setPassword(password.isEmpty() ? null : password) // 密码为空时设为null（避免连接失败）
                .setDatabase(database) // 选择数据库
                .setConnectionPoolSize(100) // 连接池大小（根据并发量调整）
                .setTimeout(3000); // 连接超时时间（毫秒）

        RedissonClient redissonClient = Redisson.create(config);
        // 初始化分布式锁工具类
        LockUtil.setRedissonClient(redissonClient);
        LockUtil.setKeyPrefix(keyPrefix);
        return redissonClient;
    }
}
