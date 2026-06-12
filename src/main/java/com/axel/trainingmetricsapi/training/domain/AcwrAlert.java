package com.axel.trainingmetricsapi.training.domain;

public enum AcwrAlert {
    NO_DATA,
    LOW,
    OK,
    HIGH;

    public static AcwrAlert from(double acwr, boolean reliable) {
        if (!reliable) return NO_DATA;
        if (acwr < 0.8) return LOW;
        if (acwr > 1.3) return HIGH;
        return OK;
    }
}
