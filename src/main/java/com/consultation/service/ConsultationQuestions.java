package com.consultation.service;

import com.consultation.model.Question;

import java.util.List;

public class ConsultationQuestions {

    public static List<Question> defaultQuestions() {
        return List.of(
                new Question("q1", "Do you have any known allergies?", "yesno"),
                new Question("q2", "Are you currently taking any medication?", "yesno"),
                new Question("q3", "Describe your symptoms briefly.", "text")
        );
    }
}