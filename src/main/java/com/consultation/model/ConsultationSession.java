package com.consultation.model;

import java.util.ArrayList;
import java.util.List;

public record ConsultationSession(
        String sessionId,
        List<Answer> answers
) {
    public ConsultationSession(String sessionId) {
        this(sessionId, new ArrayList<>());
    }

    public void addAnswer(Answer answer) {
        answers.add(answer);
    }

}