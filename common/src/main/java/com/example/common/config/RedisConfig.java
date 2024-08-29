package com.example.common.config;

import com.example.common.util.LockUtil;
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
import com.example.common.util.RedisUtil;

@Configuration
@AutoConfigureAfter(RedisAutoConfiguration.class) //注解的作用是指示 Spring Boot 在自动配置时，确保 RedisConfig 类的配置在 RedisAutoConfiguration 类之后进行。这通常用于确保某些配置依赖于其他配置的完成
public class RedisConfig extends CachingConfigurerSupport {

    @Value("${spring.redis.key-prefix:}") // 注入前缀
    private  String keyPrefix;

    /**
     * RedisTemplate配置
     *
     * @param redisConnectionFactory Redis连接工厂，用于创建Redis连接
     * @return RedisTemplate<String, Object> 配置好的RedisTemplate实例
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();

        RedisUtil.setRedisTemplate(template); // 设置RedisUtil中的RedisTemplate实例
        RedisUtil.setKeyPrefix(keyPrefix); //设置前缀
        template.setConnectionFactory(redisConnectionFactory);// 设置连接工厂

        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);

        // ObjectMapper 将Json反序列化成Java对象,当java客户端调用当时候，会在直接转化成对象当java对象
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.WRAPPER_ARRAY);
        jackson2JsonRedisSerializer.setObjectMapper(om);// 设置序列化器的ObjectMapper

        // 序列化 值时使用此序列化方法
        template.setDefaultSerializer(jackson2JsonRedisSerializer);// 设置序列化方法
        template.setKeySerializer(new StringRedisSerializer());// 设置键的序列化器
        template.setValueSerializer(jackson2JsonRedisSerializer);// 设置值的序列化器
        template.setHashKeySerializer(new StringRedisSerializer());// 设置Hash键的序列化器
        template.setHashValueSerializer(jackson2JsonRedisSerializer);// 设置Hash值的序列化器
        template.afterPropertiesSet();// 初始化模板
        return template;// 返回配置好的RedisTemplate实例
    }

    /**
     * 选择redis作为默认缓存工具
     *
     * @param redisConnectionFactory Redis连接工厂，用于创建Redis连接
     * @return CacheManager 配置好的CacheManager实例
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()// 禁用缓存空值
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new Jackson2JsonRedisSerializer<>(Object.class)));// 设置值的序列化器
        return RedisCacheManager
                .builder(RedisCacheWriter.nonLockingRedisCacheWriter(redisConnectionFactory))// 创建RedisCacheWriter
                .cacheDefaults(redisCacheConfiguration).build();// 构建CacheManager
    }

    /**
     * 创建StringRedisTemplate实例
     *
     * @param redisConnectionFactory Redis连接工厂，用于创建Redis连接
     * @return StringRedisTemplate 配置好的StringRedisTemplate实例
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();
        stringRedisTemplate.setConnectionFactory(redisConnectionFactory);// 设置连接工厂
        return stringRedisTemplate;// 返回配置好的StringRedisTemplate实例
    }


    @Bean
    public RedissonClient redissonClient(@Value("${spring.redis.host}") String host,
                                         @Value("${spring.redis.port}") int port,
                                         @Value("${spring.redis.password}") String password,
                                         @Value("${spring.redis.database}") int database) {

        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://" + host + ":" + port) // 设置 Redis 服务器地址
                .setPassword(password) // 设置 Redis 服务器密码
                .setDatabase(database) // 设置默认数据库
                .setConnectionPoolSize(100) // 设置连接池大小
                .setTimeout(3000); // 设置连接超时时间

        RedissonClient redissonClient = Redisson.create(config);
        LockUtil.setKeyPrefix(keyPrefix);//设置前缀
        LockUtil.setRedissonClient(redissonClient);//设置RedissonClient实例
        return redissonClient;
    }

}
