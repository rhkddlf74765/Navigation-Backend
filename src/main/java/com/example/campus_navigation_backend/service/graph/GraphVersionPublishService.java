package com.example.campus_navigation_backend.service.graph;

import com.example.campus_navigation_backend.domain.graph.CampusGraph;
import com.example.campus_navigation_backend.domain.graph.validation.GraphDatabaseValidator;
import com.example.campus_navigation_backend.domain.graph.validation.GraphValidationReport;
import com.example.campus_navigation_backend.repository.GraphVersionRepository;
import com.example.campus_navigation_backend.service.graph.validation.JavaGraphValidator;
import com.example.campus_navigation_backend.repository.GraphValidationResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class GraphVersionPublishService {

    private final GraphVersionRepository
            graphVersionRepository;

    private final GraphDatabaseValidator
            databaseValidator;

    private final CampusGraphLoader
            campusGraphLoader;

    private final JavaGraphValidator
            javaGraphValidator;

    private final GraphValidationResultRepository
            validationResultRepository;

    @Transactional
    public GraphValidationReport publish(
            long graphVersionId
    ) {

        String status =
                graphVersionRepository
                        .findStatus(
                                graphVersionId
                        );

        if (!"BUILDING".equals(status)) {
            throw new IllegalStateException(
                    "Only BUILDING graph can be published. "
                            + "graphVersionId="
                            + graphVersionId
                            + ", status="
                            + status
            );
        }

        graphVersionRepository
                .markValidating(
                        graphVersionId
                );

        var issues =
                new ArrayList<>(
                        databaseValidator
                                .validate(
                                        graphVersionId
                                )
                );

        /*
         * DB 단계에서 이미 치명적 오류가 있으면
         * CampusGraph 자체를 만들 필요가 없다.
         */
        boolean databaseHasErrors =
                issues.stream()
                        .anyMatch(
                                issue ->
                                        issue.severity()
                                                .name()
                                                .equals(
                                                        "ERROR"
                                                )
                        );

        if (!databaseHasErrors) {

            CampusGraph graph =
                    campusGraphLoader.load(
                            graphVersionId
                    );

            issues.addAll(
                    javaGraphValidator
                            .validate(
                                    graph
                            )
            );
        }

        validationResultRepository
                .replaceResults(
                        graphVersionId,
                        issues
                );

        GraphValidationReport report =
                new GraphValidationReport(
                        graphVersionId,
                        issues
                );

        if (report.hasErrors()) {

            graphVersionRepository
                    .markFailed(
                            graphVersionId
                    );

            return report;
        }

        graphVersionRepository
                .activateReplacingCurrent(
                        graphVersionId
                );

        return report;
    }
}