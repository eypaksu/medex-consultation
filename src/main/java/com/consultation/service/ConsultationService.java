package com.consultation.service;

import com.consultation.dto.AnswerDto;
import com.consultation.dto.PrescriptionDecisionDto;
import com.consultation.dto.QuestionDto;
import com.consultation.model.Answer;
import com.consultation.model.ConsultationSession;
import com.consultation.model.PrescriptionResult;
import com.consultation.model.Question;
import com.consultation.repository.InMemoryConsultationSessionRepository;
import com.consultation.rule.PrescriptionRules;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConsultationService {

    private final List<Question> questions = ConsultationQuestions.defaultQuestions();

    private final InMemoryConsultationSessionRepository sessionRepository;

    public ConsultationService(InMemoryConsultationSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    public List<QuestionDto> getQuestions() {
        return questions.stream()
                .map(q -> new QuestionDto(q.id(), q.text(), q.type()))
                .toList();
    }

    public void submitAnswers(String sessionId, List<AnswerDto> answers) {
        ConsultationSession session = sessionRepository.findById(sessionId)
                .orElse(new ConsultationSession(sessionId));

        for (AnswerDto dto : answers) {
            session.addAnswer(new Answer(dto.questionId(), dto.value()));
        }

        sessionRepository.save(session);
    }

    public PrescriptionDecisionDto getPrescriptionDecision(String sessionId) {
        ConsultationSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        PrescriptionRules rules = new PrescriptionRules(session.answers());
        PrescriptionResult result = rules.evaluate();

        return new PrescriptionDecisionDto(
                result.likelyToPrescribe(),
                result.reasons()
        );
    }

}