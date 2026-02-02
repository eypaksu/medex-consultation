package com.consultation.rule;

import com.consultation.model.Answer;
import com.consultation.model.PrescriptionResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PrescriptionRules {

    private final Map<String, Answer> answersByQuestion;

    public PrescriptionRules(List<Answer> answers) {
        this.answersByQuestion = answers.stream()
                .collect(Collectors.toMap(Answer::questionId, Function.identity()));
    }

    public PrescriptionResult evaluate() {
        List<String> reasons = new ArrayList<>();

        // Rule 1: must answer all questions
        if (!hasAllQuestions()) {
            reasons.add("Not all consultation questions have been answered.");
            return new PrescriptionResult(false, reasons);
        }

        // Rule 2: check allergies (q1)
        Boolean hasAllergy = hasAllergy();
        if (hasAllergy == null) {
            reasons.add("Answer to allergy question is invalid (must be 'yes' or 'no').");
            return new PrescriptionResult(false, reasons);
        }
        if (hasAllergy) {
            reasons.add("Patient reports known allergies; prescribing is not safe.");
            return new PrescriptionResult(false, reasons);
        }

        // Rule 3: check current medication (q2)
        Boolean takesMedication = takesMedication();
        if (takesMedication == null) {
            reasons.add("Answer to medication question is invalid (must be 'yes' or 'no').");
            return new PrescriptionResult(false, reasons);
        }
        if (takesMedication) {
            reasons.add("Patient is currently taking other medication; prescribing is not safe.");
            return new PrescriptionResult(false, reasons);
        }

        // If we get here, all checks pass
        return new PrescriptionResult(true, List.of());
    }

    private boolean hasAllQuestions() {
        return answersByQuestion.size() >= 3
                && answersByQuestion.containsKey("q1")
                && answersByQuestion.containsKey("q2")
                && answersByQuestion.containsKey("q3");
    }

    // q1: "Do you have any known allergies?"
    private Boolean hasAllergy() {
        Answer answer = answersByQuestion.get("q1");
        if (answer == null) return null;
        if (answer.isYes()) return true;
        if (answer.isNo()) return false;
        return null; // invalid value
    }

    // q2: "Are you currently taking any medication?"
    private Boolean takesMedication() {
        Answer answer = answersByQuestion.get("q2");
        if (answer == null) return null;
        if (answer.isYes()) return true;
        if (answer.isNo()) return false;
        return null; // invalid value
    }
}