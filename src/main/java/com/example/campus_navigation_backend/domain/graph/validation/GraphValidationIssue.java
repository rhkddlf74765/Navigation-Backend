package com.example.campus_navigation_backend.domain.graph.validation;

public record GraphValidationIssue(
        String ruleCode,
        GraphValidationSeverity severity,
        String entityType,
        Long entityId,
        String message
) {

    public GraphValidationIssue {

        if (ruleCode == null
                || ruleCode.isBlank()) {

            throw new IllegalArgumentException(
                    "Validation rule code is required."
            );
        }

        if (severity == null) {
            throw new IllegalArgumentException(
                    "Validation severity is required."
            );
        }

        if (message == null) {
            message = "";
        }
    }
}