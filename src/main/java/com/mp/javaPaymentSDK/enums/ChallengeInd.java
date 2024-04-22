package com.mp.javaPaymentSDK.enums;

public enum ChallengeInd {
    _01("01"),
    _02("02"),
    _03("01"),
    _04("01"),
    _05("01"),
    _06("01"),
    _07("01"),
    _08("01"),
    _09("01");

    private final String value;

    ChallengeInd(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ChallengeInd getChallengeInd(String stringValue) {
        for (ChallengeInd challengeInd : ChallengeInd.values()) {
            if (stringValue.equalsIgnoreCase(challengeInd.value)) {
                return challengeInd;
            }
        }

        return null;
    }
}
