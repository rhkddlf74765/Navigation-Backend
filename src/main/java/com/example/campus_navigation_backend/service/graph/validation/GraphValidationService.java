package com.example.campus_navigation_backend.service.graph.validation;

import com.example.campus_navigation_backend.domain.graph.CampusGraph;
import com.example.campus_navigation_backend.domain.graph.validation.GraphDatabaseValidator;
import com.example.campus_navigation_backend.domain.graph.validation.GraphValidationIssue;
import com.example.campus_navigation_backend.domain.graph.validation.GraphValidationReport;
import com.example.campus_navigation_backend.repository.GraphValidationResultRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GraphValidationService {

    private final GraphDatabaseValidator
            databaseValidator;

    private final JavaGraphValidator
            javaGraphValidator;

    private final GraphValidationResultRepository
            resultRepository;

    public GraphValidationService(
            GraphDatabaseValidator databaseValidator,
            JavaGraphValidator javaGraphValidator,
            GraphValidationResultRepository resultRepository
    ) {
        this.databaseValidator =
                databaseValidator;

        this.javaGraphValidator =
                javaGraphValidator;

        this.resultRepository =
                resultRepository;
    }

    public GraphValidationReport validate(
            long graphVersionId,
            CampusGraph graph
    ) {

        List<GraphValidationIssue> issues =
                new ArrayList<>();

        issues.addAll(
                databaseValidator.validate(
                        graphVersionId
                )
        );

        issues.addAll(
                javaGraphValidator.validate(
                        graph
                )
        );

        resultRepository.replaceResults(
                graphVersionId,
                issues
        );

        return new GraphValidationReport(
                graphVersionId,
                issues
        );
    }
}