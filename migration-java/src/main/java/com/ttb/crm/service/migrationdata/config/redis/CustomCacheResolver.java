package com.ttb.crm.service.migrationdata.config.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.lang.NonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
public class CustomCacheResolver implements CacheResolver {
    private final CacheManager cacheManager;

    public CustomCacheResolver(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    private static final List<Class<? extends Annotation>> SUPPORTED_ANNOTATIONS = List.of(
            Cacheable.class, CacheEvict.class, CachePut.class, Caching.class
    );

    public List<String> extractCacheNames(Method method) {
        return SUPPORTED_ANNOTATIONS.stream()
                .map(method::getAnnotation)
                .filter(Objects::nonNull)
                .flatMap(this::extractAnnotationValues)
                .toList();
    }

    private Stream<String> extractAnnotationValues(Annotation annotation) {
        if (annotation instanceof Caching caching) {
            return Stream.of(
                            caching.cacheable(),
                            caching.put(),
                            caching.evict()
                    ).flatMap(Arrays::stream)
                    .flatMap(a -> Arrays.stream(getCacheAnnotationValues(a)));
        }
        return Arrays.stream(getCacheAnnotationValues(annotation));
    }

    public String[] getCacheAnnotationValues(Annotation annotation) {
        try {
            Method valueMethod = annotation.annotationType().getMethod("value");
            Object result = valueMethod.invoke(annotation);
            if (result instanceof String[] values) {
                return values;
            }
        } catch (Exception e) {
            log.warn("Failed to extract value() from annotation {}: {}",
                    annotation.annotationType().getSimpleName(), e.getMessage());
        }
        return new String[0];
    }

    @NonNull
    @Override
    public Collection<? extends Cache> resolveCaches(CacheOperationInvocationContext<?> context) {
        String groupName = Optional.of(context.getTarget())
                .map(Object::getClass)
                .map(cls -> cls.getAnnotation(CachingGroup.class))
                .map(CachingGroup::value)
                .orElse("default");

        return extractCacheNames(context.getMethod())
                .stream()
                .map(name -> groupName.equals("default") ?
                        cacheManager.getCache(name) :
                        cacheManager.getCache(groupName + ":" + name))
                .filter(Objects::nonNull)
                .toList();
    }
}
