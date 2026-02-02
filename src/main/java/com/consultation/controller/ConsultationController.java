package com.consultation.controller;

import com.consultation.dto.AnswerDto;
import com.consultation.dto.PrescriptionDecisionDto;
import com.consultation.dto.QuestionDto;
import com.consultation.service.ConsultationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/consultation")
public class ConsultationController {

    private final ConsultationService consultationService;

    public ConsultationController(ConsultationService consultationService) {
        this.consultationService = consultationService;
    }

    @GetMapping("/questions")
    public ResponseEntity<List<QuestionDto>> getQuestions() {
        return ResponseEntity.ok(consultationService.getQuestions());
    }

    @PostMapping("/answers")
    public ResponseEntity<Void> submitAnswers(
            @RequestParam String sessionId,
            @RequestBody List<AnswerDto> answers
    ) {
        consultationService.submitAnswers(sessionId, answers);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/decision")
    public ResponseEntity<PrescriptionDecisionDto> getPrescriptionDecision(
            @RequestParam String sessionId
    ) {
        try{
            PrescriptionDecisionDto decision = consultationService.getPrescriptionDecision(sessionId);
            return ResponseEntity.ok(decision);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

    }
}
