package service;

import com.consultation.dto.AnswerDto;
import com.consultation.dto.PrescriptionDecisionDto;
import com.consultation.dto.QuestionDto;
import com.consultation.model.Answer;
import com.consultation.model.ConsultationSession;
import com.consultation.repository.InMemoryConsultationSessionRepository;
import com.consultation.service.ConsultationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class ConsultationServiceTest {

    private final InMemoryConsultationSessionRepository sessionRepository =
            mock(InMemoryConsultationSessionRepository.class);

    private ConsultationService service;

    @BeforeEach
    void setUp() {
        service = new ConsultationService(sessionRepository);
    }

    @Test
    void shouldReturnQuestions() {
        List<QuestionDto> questions = service.getQuestions();

        assertThat(questions).hasSize(3);
        assertThat(questions.get(0).id()).isEqualTo("q1");
        assertThat(questions.get(1).id()).isEqualTo("q2");
        assertThat(questions.get(2).id()).isEqualTo("q3");
    }

    @Test
    void shouldSubmitAnswersAndSaveSession() {
        var answers = List.of(
                new AnswerDto("q1", "no"),
                new AnswerDto("q2", "no")
        );

        var sessionCaptor = ArgumentCaptor.forClass(ConsultationSession.class);

        when(sessionRepository.findById("s1"))
                .thenReturn(Optional.empty());

        service.submitAnswers("s1", answers);

        verify(sessionRepository).save(sessionCaptor.capture());

        ConsultationSession saved = sessionCaptor.getValue();
        assertThat(saved.sessionId()).isEqualTo("s1");
        assertThat(saved.answers()).hasSize(2);
        assertThat(saved.answers().get(0).questionId()).isEqualTo("q1");
        assertThat(saved.answers().get(1).questionId()).isEqualTo("q2");
    }

    @Test
    void shouldReturnTrueWhenAllQuestionsAnsweredAndNoRisks() {
        var answers = List.of(
                new Answer("q1", "no"),
                new Answer("q2", "no"),
                new Answer("q3", "headache")
        );

        var session = new ConsultationSession("s1");
        for (Answer a : answers) {
            session.addAnswer(a);
        }

        when(sessionRepository.findById("s1"))
                .thenReturn(Optional.of(session));

        PrescriptionDecisionDto decision = service.getPrescriptionDecision("s1");

        assertThat(decision.likelyToPrescribe()).isTrue();
        assertThat(decision.reasons()).isEmpty();
    }

    @Test
    void shouldReturnFalseWhenSessionNotFound() {
        when(sessionRepository.findById("unknown"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getPrescriptionDecision("unknown"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Session not found: unknown");
    }

    @Test
    void shouldReturnFalseWhenHasAllergies() {
        var answers = List.of(
                new Answer("q1", "yes"),
                new Answer("q2", "no"),
                new Answer("q3", "headache")
        );

        var session = new ConsultationSession("s1");
        for (Answer a : answers) {
            session.addAnswer(a);
        }

        when(sessionRepository.findById("s1"))
                .thenReturn(Optional.of(session));

        PrescriptionDecisionDto decision = service.getPrescriptionDecision("s1");

        assertThat(decision.likelyToPrescribe()).isFalse();
        assertThat(decision.reasons())
                .containsExactly("Patient reports known allergies; prescribing is not safe.");
    }

    @Test
    void shouldReturnFalseWhenTakesMedication() {
        var answers = List.of(
                new Answer("q1", "no"),
                new Answer("q2", "yes"),
                new Answer("q3", "headache")
        );

        var session = new ConsultationSession("s1");
        for (Answer a : answers) {
            session.addAnswer(a);
        }

        when(sessionRepository.findById("s1"))
                .thenReturn(Optional.of(session));

        PrescriptionDecisionDto decision = service.getPrescriptionDecision("s1");

        assertThat(decision.likelyToPrescribe()).isFalse();
        assertThat(decision.reasons())
                .containsExactly("Patient is currently taking other medication; prescribing is not safe.");
    }

    @Test
    void shouldReturnFalseWhenAllergyAnswerIsInvalid() {
        var answers = List.of(
                new Answer("q1", "maybe"),
                new Answer("q2", "no"),
                new Answer("q3", "headache")
        );

        var session = new ConsultationSession("s1");
        for (Answer a : answers) {
            session.addAnswer(a);
        }

        when(sessionRepository.findById("s1"))
                .thenReturn(Optional.of(session));

        PrescriptionDecisionDto decision = service.getPrescriptionDecision("s1");

        assertThat(decision.likelyToPrescribe()).isFalse();
        assertThat(decision.reasons())
                .containsExactly("Answer to allergy question is invalid (must be 'yes' or 'no').");
    }
}
