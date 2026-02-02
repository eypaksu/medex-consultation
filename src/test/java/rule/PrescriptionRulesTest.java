package rule;

import com.consultation.model.Answer;
import com.consultation.model.PrescriptionResult;
import com.consultation.rule.PrescriptionRules;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PrescriptionRulesTest {

    @Test
    void shouldReturnTrueWhenAllQuestionsAnsweredAndNoRisks() {
        var answers = List.of(
                new Answer("q1", "no"),
                new Answer("q2", "no"),
                new Answer("q3", "headache")
        );

        var rules = new PrescriptionRules(answers);
        PrescriptionResult result = rules.evaluate();

        assertThat(result.likelyToPrescribe()).isTrue();
        assertThat(result.reasons()).isEmpty();
    }

    @Test
    void shouldReturnFalseWhenNotAllQuestionsAnswered() {
        var answers = List.of(
                new Answer("q1", "no"),
                new Answer("q2", "no")
        );

        var rules = new PrescriptionRules(answers);
        PrescriptionResult result = rules.evaluate();

        assertThat(result.likelyToPrescribe()).isFalse();
        assertThat(result.reasons())
                .containsExactly("Not all consultation questions have been answered.");
    }

    @Test
    void shouldReturnFalseWhenHasAllergies() {
        var answers = List.of(
                new Answer("q1", "yes"),
                new Answer("q2", "no"),
                new Answer("q3", "headache")
        );

        var rules = new PrescriptionRules(answers);
        PrescriptionResult result = rules.evaluate();

        assertThat(result.likelyToPrescribe()).isFalse();
        assertThat(result.reasons())
                .containsExactly("Patient reports known allergies; prescribing is not safe.");
    }

    @Test
    void shouldReturnFalseWhenTakesMedication() {
        var answers = List.of(
                new Answer("q1", "no"),
                new Answer("q2", "yes"),
                new Answer("q3", "headache")
        );

        var rules = new PrescriptionRules(answers);
        PrescriptionResult result = rules.evaluate();

        assertThat(result.likelyToPrescribe()).isFalse();
        assertThat(result.reasons())
                .containsExactly("Patient is currently taking other medication; prescribing is not safe.");
    }

    @Test
    void shouldReturnFalseWhenAllergyAnswerIsInvalid() {
        var answers = List.of(
                new Answer("q1", "maybe"),
                new Answer("q2", "no"),
                new Answer("q3", "headache")
        );

        var rules = new PrescriptionRules(answers);
        PrescriptionResult result = rules.evaluate();

        assertThat(result.likelyToPrescribe()).isFalse();
        assertThat(result.reasons())
                .containsExactly("Answer to allergy question is invalid (must be 'yes' or 'no').");
    }

    @Test
    void shouldReturnFalseWhenMedicationAnswerIsInvalid() {
        var answers = List.of(
                new Answer("q1", "no"),
                new Answer("q2", "1"),
                new Answer("q3", "headache")
        );

        var rules = new PrescriptionRules(answers);
        PrescriptionResult result = rules.evaluate();

        assertThat(result.likelyToPrescribe()).isFalse();
        assertThat(result.reasons())
                .containsExactly("Answer to medication question is invalid (must be 'yes' or 'no').");
    }
}
