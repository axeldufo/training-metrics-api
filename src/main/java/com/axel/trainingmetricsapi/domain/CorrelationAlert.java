package com.axel.trainingmetricsapi.domain;

public enum CorrelationAlert {
    NO_ALERT,                        // all metrics nominal
    INSUFFICIENT_DATA,               // no previous wellness week — delta unavailable
    STABLE_LOAD_RISING_FATIGUE,      // load in zone, fatigue delta positive → external factors
    OVERLOAD_RISK,                   // high ACWR and fatigue rising → injury risk
    GOOD_ADAPTATION,                 // ACWR rising moderately, fatigue stable or improving
    UNDERLOAD_DECLINING_MOTIVATION,  // low ACWR and motivation dropping → detraining risk
    POTENTIAL_OVERTRAINING           // stable load, fatigue rising and motivation declining
}
