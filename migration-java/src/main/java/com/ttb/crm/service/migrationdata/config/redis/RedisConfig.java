package com.ttb.crm.service.migrationdata.config.redis;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
@Slf4j
@ConditionalOnProperty(name = "spring.redis.enabled", havingValue = "true",  matchIfMissing = true)
public class RedisConfig implements CachingConfigurer {
    private final ObjectMapper objectMapper;
    private final CacheManager cacheManager;

    @Value("${cache.redis.ttl.caseMigrationBatch:600000}")
    private long defaultTimeToLiveMillis;

    @Value("${cache.redis.prefix:case-migration-batch}")
    private String prefix;

    @Value("${cache.redis.env:dev}")
    private String env;

    @Value("${cache.redis.ttl.masterDataByGroup:7200000}")
    private long masterDataTtl;

    @Value("${cache.redis.ttl.serviceTypeMatrixByCode:3600000}")
    private long serviceTypeTtl;

    @Value("${cache.redis.ttl.teamsByNameEn:1800000}")
    private long teamsTtl;

    @Value("${cache.redis.ttl.employeeUserByEmployeeId:900000}")
    private long employeeUsersTtl;

    @Value("${cache.redis.ttl.slaStartDate:900000}")
    private long slaCalculationTtl;

    @Value("${cache.redis.ttl.holidays:86400000}")
    private long holidaysTtl;

    @Value("${cache.redis.ttl.encryptionSecrets:7200000}")
    private long encryptionSecretsTtl;

    @Value("${cache.redis.ttl.teamsByIds:1800000}")
    private long teamsByIdsTtl;

    public RedisConfig(ObjectMapper objectMapper, @Lazy CacheManager cacheManager) {
        this.objectMapper = objectMapper.copy();

        SimpleModule module = new SimpleModule();
        BeanSerializerModifier modifier = new PersistentBagSerializerModifier();
        module.setSerializerModifier(modifier);
        this.objectMapper.registerModule(module);

        this.objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY);

        this.cacheManager = cacheManager;
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        return new ConcurrentMapCacheManager();
//        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);
//
//        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
//                .entryTtl(Duration.ofMillis(defaultTimeToLiveMillis))
//                .prefixCacheNameWith(env.concat(":").concat(prefix).concat(":"))
//                //.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));
//                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
//                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()));
//
//        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
//
//        cacheConfigurations.put("masterDataByGroup",
//                defaultConfig.entryTtl(Duration.ofMillis(masterDataTtl)));
//        cacheConfigurations.put("masterDataByCodeAndGroup",
//                defaultConfig.entryTtl(Duration.ofMillis(masterDataTtl)));
//        cacheConfigurations.put("serviceTypeMatrixByCode",
//                defaultConfig.entryTtl(Duration.ofMillis(serviceTypeTtl)));
//        cacheConfigurations.put("teams",
//                defaultConfig.entryTtl(Duration.ofMillis(teamsTtl)));
//        cacheConfigurations.put("teamsByNameEn",
//                defaultConfig.entryTtl(Duration.ofMillis(teamsTtl)));
//        cacheConfigurations.put("teamsByIds",
//                defaultConfig.entryTtl(Duration.ofMillis(teamsTtl)));
//        cacheConfigurations.put("employeeUserByEmployeeId",
//                defaultConfig.entryTtl(Duration.ofMillis(employeeUsersTtl)));
//        cacheConfigurations.put("employeeUserByEmployeeIdWithActiveStatus",
//                defaultConfig.entryTtl(Duration.ofMillis(employeeUsersTtl)));
//        cacheConfigurations.put("employeeUserByUserId",
//                defaultConfig.entryTtl(Duration.ofMillis(employeeUsersTtl)));
//        cacheConfigurations.put("teamIdListByEmployeeId",
//                defaultConfig.entryTtl(Duration.ofMillis(employeeUsersTtl)));
//        cacheConfigurations.put("slaStartDate",
//                defaultConfig.entryTtl(Duration.ofMillis(slaCalculationTtl)));
//        cacheConfigurations.put("slaCalculation",
//                defaultConfig.entryTtl(Duration.ofMillis(slaCalculationTtl)));
//        cacheConfigurations.put("holidays",
//                defaultConfig.entryTtl(Duration.ofMillis(holidaysTtl)));
//        cacheConfigurations.put("encryptionSecrets",
//                defaultConfig.entryTtl(Duration.ofMillis(encryptionSecretsTtl)));
//
//        log.info("Redis Cache Manager initialized with prefix: {}:{}", env, prefix);
//        cacheConfigurations.forEach((name, config) ->
//                log.info("Cache '{}' configured with TTL: {} minutes", name, config.getTtl().toMinutes()));
//
//        return RedisCacheManager.builder(connectionFactory)
//                .cacheDefaults(defaultConfig)
//                .withInitialCacheConfigurations(cacheConfigurations)
//                .transactionAware()
//                .build();
    }

    @Bean
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                log.error("Redis cache GET error - Cache: {}, Key: {}, Error: {}",
                        cache.getName(), key, exception.getMessage());
                // ไม่ throw exception ให้ fallback ไป database
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                log.error("Redis cache PUT error - Cache: {}, Key: {}, Error: {}",
                        cache.getName(), key, exception.getMessage());
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                log.error("Redis cache EVICT error - Cache: {}, Key: {}, Error: {}",
                        cache.getName(), key, exception.getMessage());
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                log.error("Redis cache CLEAR error - Cache: {}, Error: {}",
                        cache.getName(), exception.getMessage());
            }
        };
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Key serialization
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // Value serialization
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        template.setDefaultSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();

        return template;
    }

    @Override
    public CacheResolver cacheResolver() {
        return new CustomCacheResolver(cacheManager);
    }
}
