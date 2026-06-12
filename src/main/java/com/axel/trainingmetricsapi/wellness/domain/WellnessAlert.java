package com.axel.trainingmetricsapi.wellness.domain;

public enum WellnessAlert {
    ABSOLUTE_LOW,    // current value at or below absolute low threshold
    WEEK_OVER_WEEK,  // sharp single-week drop
    TREND_DECLINING  // sustained declining slope over multiple weeks
}
