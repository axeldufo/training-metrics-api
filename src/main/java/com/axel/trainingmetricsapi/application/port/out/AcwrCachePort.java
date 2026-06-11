package com.axel.trainingmetricsapi.application.port.out;

import com.axel.trainingmetricsapi.domain.AcwrReport;

import java.util.Optional;

public interface AcwrCachePort {
    void evict(long athleteId);
    void put(long athleteId, AcwrReport report);
    Optional<AcwrReport> get(long athleteId);
}
