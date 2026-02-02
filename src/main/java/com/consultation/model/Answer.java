package com.consultation.model;

public record Answer(
        String questionId,
        String value
) {
    public boolean isYes() {
        return "yes".equalsIgnoreCase(value);
    }

    public boolean isNo() {
        return "no".equalsIgnoreCase(value);
    }
}
