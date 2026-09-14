package com.example.campus_navigation_backend.repository;

import com.example.campus_navigation_backend.domain.graph.validation.GraphValidationIssue;

import java.util.List;

public interface GraphValidationResultRepository {

    void replaceResults(
            long graphVersionId,
            List<GraphValidationIssue> issues
    );
}