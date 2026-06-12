package com.axel.trainingmetricsapi.training.infrastructure.cache;

import com.axel.trainingmetricsapi.training.domain.AcwrReport;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisCacheAdapterTest {

    private static final long ATHLETE_ID = 5L;

    @Mock
    CacheManager cacheManager;

    @Mock
    private Cache cache;

    @InjectMocks
    RedisCacheAdapter redisCacheAdapter;

    @BeforeEach
    void setUp() {
        when(cacheManager.getCache(CacheConfig.ACWR_REPORT_CACHE)).thenReturn(cache);
    }

    @Test
    void evict_shouldEvictFromCache() {
        redisCacheAdapter.evict(ATHLETE_ID);

        verify(cache).evict(ATHLETE_ID);
    }

    @Test
    void put_shouldPutInCache() {
        AcwrReport report = Instancio.create(AcwrReport.class);

        redisCacheAdapter.put(ATHLETE_ID, report);

        verify(cache).put(ATHLETE_ID, report);
    }

    @Test
    void get_shouldReturnReport_whenCacheHit() {
        AcwrReport report = Instancio.create(AcwrReport.class);
        Cache.ValueWrapper wrapper = mock(Cache.ValueWrapper.class);
        when(cache.get(ATHLETE_ID)).thenReturn(wrapper);
        when(wrapper.get()).thenReturn(report);

        Optional<AcwrReport> result = redisCacheAdapter.get(ATHLETE_ID);

        assertThat(result).contains(report);
    }

    @Test
    void get_shouldReturnEmpty_whenCacheMiss() {
        when(cache.get(ATHLETE_ID)).thenReturn(null);

        Optional<AcwrReport> result = redisCacheAdapter.get(ATHLETE_ID);

        assertThat(result).isEmpty();
    }

    @Test
    void get_shouldThrow_whenCacheNotConfigured() {
        when(cacheManager.getCache(CacheConfig.ACWR_REPORT_CACHE)).thenReturn(null);

        assertThatThrownBy(() -> redisCacheAdapter.get(5L))
            .isInstanceOf(IllegalStateException.class);
    }

}
