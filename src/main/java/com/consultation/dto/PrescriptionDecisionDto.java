package com.consultation.dto;

import java.util.List;

public record PrescriptionDecisionDto(
        boolean likelyToPrescribe,
        List<String> reasons
) {}