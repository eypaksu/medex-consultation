package controller;

import com.consultation.Consultation;
import com.consultation.controller.ConsultationController;
import com.consultation.dto.AnswerDto;
import com.consultation.dto.PrescriptionDecisionDto;
import com.consultation.dto.QuestionDto;
import com.consultation.service.ConsultationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static org.mockito.Mockito.when;

@WebFluxTest(ConsultationController.class)
@ContextConfiguration(classes = Consultation.class)
class ConsultationControllerTest {

    @Autowired
    private WebTestClient webClient;

    @MockitoBean
    private ConsultationService consultationService;

    @Test
    void shouldReturnQuestions() {
        when(consultationService.getQuestions()).thenReturn(
                List.of(
                        new QuestionDto("q1", "Allergies?", "yesno"),
                        new QuestionDto("q2", "Medication?", "yesno"),
                        new QuestionDto("q3", "Symptoms?", "text")
                )
        );

        webClient.get()
                .uri("/api/consultation/questions")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo("q1")
                .jsonPath("$[1].id").isEqualTo("q2")
                .jsonPath("$[2].id").isEqualTo("q3");
    }

    @Test
    void shouldAcceptAnswers() {
        webClient.post()
                .uri("/api/consultation/answers?sessionId=s1")
                .bodyValue(List.of(
                        new AnswerDto("q1", "yes"),
                        new AnswerDto("q2", "no")
                ))
                .exchange()
                .expectStatus().isAccepted();
    }

    @Test
    void shouldReturnDecisionWithReasons() {
        var reasons = List.of("Not all consultation questions have been answered.");
        var decision = new PrescriptionDecisionDto(false, reasons);

        when(consultationService.getPrescriptionDecision("s1"))
                .thenReturn(decision);

        webClient.get()
                .uri("/api/consultation/decision?sessionId=s1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.likelyToPrescribe").isEqualTo(false)
                .jsonPath("$.reasons[0]").isEqualTo("Not all consultation questions have been answered.");
    }

    @Test
    void shouldReturn400WhenSessionNotFound() {
        when(consultationService.getPrescriptionDecision("unknown"))
                .thenThrow(new IllegalArgumentException("Session not found: unknown"));

        webClient.get()
                .uri("/api/consultation/decision?sessionId=unknown")
                .exchange()
                .expectStatus().isBadRequest();
    }
}
