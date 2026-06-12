package com.axel.trainingmetricsapi.training.infrastructure.cache;

import com.axel.trainingmetricsapi.training.application.port.out.AcwrCachePort;
import com.axel.trainingmetricsapi.training.domain.AcwrReport;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class RedisCacheAdapter implements AcwrCachePort {

    private final CacheManager cacheManager;

    public RedisCacheAdapter(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public void evict(long athleteId) {
        getCache().evict(athleteId);
    }

    @Override
    public void put(long athleteId, AcwrReport report) {
        getCache().put(athleteId, report);
    }

    @Override
    public Optional<AcwrReport> get(long athleteId) {
        return Optional.ofNullable(getCache().get(athleteId))
            .map(wrapper -> (AcwrReport) wrapper.get());
    }

    private Cache getCache() {
        Cache cache = cacheManager.getCache(CacheConfig.ACWR_REPORT_CACHE);
        if (cache == null) throw new IllegalStateException("Cache not found: " + CacheConfig.ACWR_REPORT_CACHE);
        return cache;
    }

}
