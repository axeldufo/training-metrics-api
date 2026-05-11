package com.axel.trainingmetricsapi.domain;

public enum TargetZone {
    Z1(1, 3),
    Z2(3, 5),
    Z3(5, 7),
    Z4(6, 7),
    Z5(8, 9);

    private final int rpeMinExpected;
    private final int rpeMaxExpected;

    TargetZone(int rpeMinExpected, int rpeMaxExpected) {
        this.rpeMinExpected = rpeMinExpected;
        this.rpeMaxExpected = rpeMaxExpected;
    }

    public boolean isRpeTooHigh(int sessionRpe) {
        return sessionRpe > rpeMaxExpected;
    }

    public boolean isRpeTooLow(int sessionRpe) {
        return sessionRpe < rpeMinExpected;
    }
}
