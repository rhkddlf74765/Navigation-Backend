package com.example.campus_navigation_backend.domain.graph.validation;

import java.util.List;

public record GraphValidationReport(
        long graphVersionId,
        List<GraphValidationIssue> issues
) {

    public GraphValidationReport {

        issues = List.copyOf(
                issues
        );
    }

    public boolean hasErrors() {

        return issues.stream()
                .anyMatch(
                        issue ->
                                issue.severity()
                                        == GraphValidationSeverity.ERROR
                );
    }

    public long errorCount() {

        return issues.stream()
                .filter(
                        issue ->
                                issue.severity()
                                        == GraphValidationSeverity.ERROR
                )
                .count();
    }

    public long warningCount() {

        return issues.stream()
                .filter(
                        issue ->
                                issue.severity()
                                        == GraphValidationSeverity.WARNING
                )
                .count();
    }
}