package com.consultation.repository;

import com.consultation.model.ConsultationSession;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class InMemoryConsultationSessionRepository {
    private final Map<String, ConsultationSession> sessions = new HashMap<>();

    public ConsultationSession save(ConsultationSession session) {
        sessions.put(session.sessionId(), session);
        return session;
    }

    public Optional<ConsultationSession> findById(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }
}
