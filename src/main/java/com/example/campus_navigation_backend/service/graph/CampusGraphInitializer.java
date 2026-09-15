package com.example.campus_navigation_backend.service.graph;

import com.example.campus_navigation_backend.domain.graph.CampusGraph;
import com.example.campus_navigation_backend.domain.graph.CampusGraphStore;
import com.example.campus_navigation_backend.domain.graph.validation.GraphValidationIssue;
import com.example.campus_navigation_backend.domain.graph.validation.GraphValidationReport;
import com.example.campus_navigation_backend.domain.graph.validation.GraphValidationSeverity;
import com.example.campus_navigation_backend.infrastructure.spatial.EdgeSpatialIndex;
import com.example.campus_navigation_backend.service.graph.validation.GraphValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

@Service
public class CampusGraphInitializer
        implements ApplicationRunner {

    private static final Logger log =
            LoggerFactory.getLogger(
                    CampusGraphInitializer.class
            );

    private final CampusGraphLoader
            campusGraphLoader;

    private final GraphValidationService
            graphValidationService;

    private final CampusGraphStore
            campusGraphStore;

    private final EdgeSpatialIndex
            edgeSpatialIndex;

    public CampusGraphInitializer(
            CampusGraphLoader campusGraphLoader,
            GraphValidationService graphValidationService,
            CampusGraphStore campusGraphStore,
            EdgeSpatialIndex edgeSpatialIndex
    ) {
        this.campusGraphLoader =
                campusGraphLoader;

        this.graphValidationService =
                graphValidationService;

        this.campusGraphStore =
                campusGraphStore;

        this.edgeSpatialIndex =
                edgeSpatialIndex;
    }

    @Override
    public void run(
            ApplicationArguments args
    ) {

        log.info(
                "Starting routing graph initialization."
        );

        /*
         * 1.
         * CampusGraph을 만들기 전에
         * DB graph source 자체를 검증한다.
         */
        GraphValidationReport databaseReport =
                graphValidationService
                        .validateDatabase();

        logValidationIssues(
                "DATABASE",
                databaseReport
        );

        if (databaseReport.hasErrors()) {

            throw new IllegalStateException(
                    "Routing graph database validation failed. "
                            + "errors="
                            + databaseReport.errorCount()
                            + ", warnings="
                            + databaseReport.warningCount()
            );
        }

        /*
         * 2.
         * 검증된 graph_nodes / graph_edges를 이용해
         * runtime CampusGraph을 생성한다.
         */
        CampusGraph graph =
                campusGraphLoader.load();

        /*
         * 3.
         * 메모리상의 실제 routing topology를 검증한다.
         */
        GraphValidationReport graphReport =
                graphValidationService
                        .validateGraph(
                                graph
                        );

        logValidationIssues(
                "JAVA_GRAPH",
                graphReport
        );

        if (graphReport.hasErrors()) {

            throw new IllegalStateException(
                    "Runtime routing graph validation failed. "
                            + "errors="
                            + graphReport.errorCount()
                            + ", warnings="
                            + graphReport.warningCount()
            );
        }

        /*
         * 4.
         * 모든 검증에 성공한 경우에만
         * 실제 서비스용 graph로 등록한다.
         */
        campusGraphStore.initialize(
                graph
        );

        edgeSpatialIndex.initialize(
                graph.getPhysicalEdges()
        );

        log.info(
                "Routing graph initialization completed successfully. "
                        + "nodes={}, edges={}",
                graph.getNodes().size(),
                graph.getPhysicalEdges().size()
        );
    }

    private void logValidationIssues(
            String stage,
            GraphValidationReport report
    ) {

        for (GraphValidationIssue issue
                : report.issues()) {

            if (issue.severity()
                    == GraphValidationSeverity.ERROR) {

                log.error(
                        "[Graph validation][{}] "
                                + "rule={}, "
                                + "entityType={}, "
                                + "entityId={}, "
                                + "message={}",
                        stage,
                        issue.ruleCode(),
                        issue.entityType(),
                        issue.entityId(),
                        issue.message()
                );

                continue;
            }

            if (issue.severity()
                    == GraphValidationSeverity.WARNING) {

                log.warn(
                        "[Graph validation][{}] "
                                + "rule={}, "
                                + "entityType={}, "
                                + "entityId={}, "
                                + "message={}",
                        stage,
                        issue.ruleCode(),
                        issue.entityType(),
                        issue.entityId(),
                        issue.message()
                );

                continue;
            }

            log.info(
                    "[Graph validation][{}] "
                            + "rule={}, "
                            + "entityType={}, "
                            + "entityId={}, "
                            + "message={}",
                    stage,
                    issue.ruleCode(),
                    issue.entityType(),
                    issue.entityId(),
                    issue.message()
            );
        }
    }
}