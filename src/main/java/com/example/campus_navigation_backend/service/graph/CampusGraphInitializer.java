package com.example.campus_navigation_backend.service.graph;

import com.example.campus_navigation_backend.domain.graph.CampusGraph;
import com.example.campus_navigation_backend.domain.graph.CampusGraphStore;
import com.example.campus_navigation_backend.domain.graph.validation.GraphValidationReport;
import com.example.campus_navigation_backend.infrastructure.spatial.EdgeSpatialIndex;
import com.example.campus_navigation_backend.repository.GraphDataRepository;
import com.example.campus_navigation_backend.service.graph.validation.GraphValidationService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

@Service
public class CampusGraphInitializer
        implements ApplicationRunner {

    private final GraphDataRepository
            graphDataRepository;

    private final CampusGraphLoader
            campusGraphLoader;

    private final GraphValidationService
            graphValidationService;

    private final CampusGraphStore
            campusGraphStore;

    private final EdgeSpatialIndex
            edgeSpatialIndex;

    public CampusGraphInitializer(
            GraphDataRepository graphDataRepository,
            CampusGraphLoader campusGraphLoader,
            GraphValidationService graphValidationService,
            CampusGraphStore campusGraphStore,
            EdgeSpatialIndex edgeSpatialIndex
    ) {
        this.graphDataRepository =
                graphDataRepository;

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

        long graphVersionId =
                graphDataRepository
                        .findActiveGraphVersionId();

        /*
         * DB에서 ACTIVE graph를 읽어
         * 아직 서비스에 등록하지 않은 임시 객체로 만든다.
         */
        CampusGraph graph =
                campusGraphLoader.load(
                        graphVersionId
                );

        /*
         * DB + Java graph 검증
         */
        GraphValidationReport report =
                graphValidationService
                        .validate(
                                graphVersionId,
                                graph
                        );

        /*
         * ERROR가 하나라도 있으면
         * 실제 CampusGraphStore에는 등록하지 않는다.
         */
        if (report.hasErrors()) {

            throw new IllegalStateException(
                    "Active graph validation failed. "
                            + "graphVersionId="
                            + graphVersionId
                            + ", errors="
                            + report.errorCount()
                            + ", warnings="
                            + report.warningCount()
            );
        }

        campusGraphStore.initialize(
                graph
        );

        edgeSpatialIndex.initialize(
                graph.getPhysicalEdges()
        );
    }
}