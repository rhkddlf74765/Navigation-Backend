package com.example.campus_navigation_backend.service.graph.validation;

import com.example.campus_navigation_backend.domain.graph.CampusGraph;
import com.example.campus_navigation_backend.domain.graph.validation.GraphDatabaseValidator;
import com.example.campus_navigation_backend.domain.graph.validation.GraphValidationIssue;
import com.example.campus_navigation_backend.domain.graph.validation.GraphValidationReport;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GraphValidationService {

    private final GraphDatabaseValidator
            databaseValidator;

    private final JavaGraphValidator
            javaGraphValidator;

    public GraphValidationService(
            GraphDatabaseValidator databaseValidator,
            JavaGraphValidator javaGraphValidator
    ) {
        this.databaseValidator =
                databaseValidator;

        this.javaGraphValidator =
                javaGraphValidator;
    }

    /**
     * routing.graph_nodes / graph_edges 자체의
     * DB 정합성을 검사한다.
     *
     * CampusGraph 생성 전에 호출해야 한다.
     */
    public GraphValidationReport validateDatabase() {

        List<GraphValidationIssue> issues =
                databaseValidator.validate();

        return new GraphValidationReport(
                issues
        );
    }

    /**
     * DB 데이터를 이용해 생성된 CampusGraph의
     * topology 수준 정합성을 검사한다.
     */
    public GraphValidationReport validateGraph(
            CampusGraph graph
    ) {

        List<GraphValidationIssue> issues =
                javaGraphValidator.validate(
                        graph
                );

        return new GraphValidationReport(
                issues
        );
    }
}