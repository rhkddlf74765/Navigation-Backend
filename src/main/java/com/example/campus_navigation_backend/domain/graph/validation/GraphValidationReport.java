package com.example.campus_navigation_backend.domain.graph.validation;

import java.util.List;

public record GraphValidationReport(
        List<GraphValidationIssue> issues
) {

    public GraphValidationReport {

        issues = issues == null
                ? List.of()
                : List.copyOf(
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

    public boolean hasWarnings() {

        return issues.stream()
                .anyMatch(
                        issue ->
                                issue.severity()
                                        == GraphValidationSeverity.WARNING
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

    public long infoCount() {

        return issues.stream()
                .filter(
                        issue ->
                                issue.severity()
                                        == GraphValidationSeverity.INFO
                )
                .count();
    }
}