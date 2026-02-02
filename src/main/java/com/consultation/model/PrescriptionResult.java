package com.consultation.model;

import java.util.List;

public record PrescriptionResult(
        boolean likelyToPrescribe,
        List<String> reasons
) {}