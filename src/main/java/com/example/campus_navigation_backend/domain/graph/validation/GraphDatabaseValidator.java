package com.example.campus_navigation_backend.domain.graph.validation;

import com.example.campus_navigation_backend.config.NavigationProperties;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class GraphDatabaseValidator {

    private static final double
            ENDPOINT_TOLERANCE_METERS = 0.05;

    private static final double
            DISTANCE_TOLERANCE_METERS = 0.05;

    private final NamedParameterJdbcTemplate jdbc;

    private final NavigationProperties
            properties;

    public GraphDatabaseValidator(
            NamedParameterJdbcTemplate jdbc,
            NavigationProperties properties
    ) {
        this.jdbc = jdbc;
        this.properties = properties;
    }

    public List<GraphValidationIssue> validate() {

        List<GraphValidationIssue> issues =
                new ArrayList<>();

        validateGraphNotEmpty(
                issues
        );

        validateEntranceInvariant(
                issues
        );

        validateOperationalBuildings(
                issues
        );

        validateNodeGeometry(
                issues
        );

        validateEdgeGeometry(
                issues
        );

        validateEdgeEndpoints(
                issues
        );

        validateEdgeDistances(
                issues
        );

        return List.copyOf(
                issues
        );
    }

    private void validateGraphNotEmpty(
            List<GraphValidationIssue> issues
    ) {

        Integer nodeCount =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM routing.graph_nodes
                        """,
                        new MapSqlParameterSource(),
                        Integer.class
                );

        Integer edgeCount =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM routing.graph_edges
                        WHERE is_enabled = TRUE
                        """,
                        new MapSqlParameterSource(),
                        Integer.class
                );

        if (nodeCount == null
                || nodeCount == 0) {

            issues.add(
                    error(
                            "GRAPH_NO_NODES",
                            "GRAPH",
                            null,
                            "Graph contains no nodes."
                    )
            );
        }

        if (edgeCount == null
                || edgeCount == 0) {

            issues.add(
                    error(
                            "GRAPH_NO_EDGES",
                            "GRAPH",
                            null,
                            "Graph contains no enabled edges."
                    )
            );
        }
    }

    private void validateEntranceInvariant(
            List<GraphValidationIssue> issues
    ) {

        String sql = """
                SELECT
                    id,
                    node_type,
                    entrance_id

                FROM routing.graph_nodes

                WHERE (
                        node_type = 'ENTRANCE'
                        AND entrance_id IS NULL
                      )
                   OR (
                        node_type <> 'ENTRANCE'
                        AND entrance_id IS NOT NULL
                      )
                """;

        List<GraphValidationIssue> invalidNodes =
                jdbc.query(
                        sql,
                        new MapSqlParameterSource(),
                        (rs, rowNum) ->
                                error(
                                        "ENTRANCE_MAPPING_INVARIANT",
                                        "GRAPH_NODE",
                                        rs.getLong("id"),
                                        "Graph node type and entrance_id are inconsistent."
                                )
                );

        issues.addAll(
                invalidNodes
        );
    }

    private void validateOperationalBuildings(
            List<GraphValidationIssue> issues
    ) {

        String sql = """
                SELECT
                    b.id,
                    b.name

                FROM spatial.buildings b

                WHERE b.is_operational = TRUE

                  AND NOT EXISTS (
                      SELECT 1

                      FROM spatial.entrances e

                      JOIN routing.graph_nodes n
                        ON n.node_type = 'ENTRANCE'
                       AND n.entrance_id = e.id

                      WHERE e.building_id = b.id
                        AND e.is_operational = TRUE
                  )
                """;

        List<GraphValidationIssue> invalidBuildings =
                jdbc.query(
                        sql,
                        new MapSqlParameterSource(),
                        (rs, rowNum) ->
                                error(
                                        "BUILDING_NO_ROUTABLE_ENTRANCE",
                                        "BUILDING",
                                        rs.getLong("id"),
                                        "Operational building has no routable entrance: "
                                                + rs.getString(
                                                "name"
                                        )
                                )
                );

        issues.addAll(
                invalidBuildings
        );
    }

    private void validateNodeGeometry(
            List<GraphValidationIssue> issues
    ) {

        String sql = """
                SELECT id

                FROM routing.graph_nodes

                WHERE geom IS NULL
                   OR ST_IsEmpty(geom)
                   OR ST_SRID(geom)
                      <> :metricSrid
                """;

        List<GraphValidationIssue> invalidNodes =
                jdbc.query(
                        sql,
                        params(),
                        (rs, rowNum) ->
                                error(
                                        "INVALID_NODE_GEOMETRY",
                                        "GRAPH_NODE",
                                        rs.getLong("id"),
                                        "Node geometry is null, empty, or has an invalid SRID."
                                )
                );

        issues.addAll(
                invalidNodes
        );
    }

    private void validateEdgeGeometry(
            List<GraphValidationIssue> issues
    ) {

        String sql = """
                SELECT id

                FROM routing.graph_edges

                WHERE geom IS NULL
                   OR ST_IsEmpty(geom)
                   OR ST_SRID(geom)
                      <> :metricSrid
                """;

        List<GraphValidationIssue> invalidEdges =
                jdbc.query(
                        sql,
                        params(),
                        (rs, rowNum) ->
                                error(
                                        "INVALID_EDGE_GEOMETRY",
                                        "GRAPH_EDGE",
                                        rs.getLong("id"),
                                        "Edge geometry is null, empty, or has an invalid SRID."
                                )
                );

        issues.addAll(
                invalidEdges
        );
    }

    private void validateEdgeEndpoints(
            List<GraphValidationIssue> issues
    ) {

        String sql = """
                SELECT
                    e.id,

                    ST_Distance(
                        ST_Force2D(
                            ST_StartPoint(e.geom)
                        ),
                        ST_Force2D(s.geom)
                    ) AS source_error_m,

                    ST_Distance(
                        ST_Force2D(
                            ST_EndPoint(e.geom)
                        ),
                        ST_Force2D(t.geom)
                    ) AS target_error_m

                FROM routing.graph_edges e

                JOIN routing.graph_nodes s
                  ON s.id =
                     e.source_node_id

                JOIN routing.graph_nodes t
                  ON t.id =
                     e.target_node_id

                WHERE
                    ST_Distance(
                        ST_Force2D(
                            ST_StartPoint(e.geom)
                        ),
                        ST_Force2D(s.geom)
                    ) > :endpointTolerance

                    OR

                    ST_Distance(
                        ST_Force2D(
                            ST_EndPoint(e.geom)
                        ),
                        ST_Force2D(t.geom)
                    ) > :endpointTolerance
                """;

        MapSqlParameterSource params =
                params()
                        .addValue(
                                "endpointTolerance",
                                ENDPOINT_TOLERANCE_METERS
                        );

        List<GraphValidationIssue> invalidEdges =
                jdbc.query(
                        sql,
                        params,
                        (rs, rowNum) -> {

                            long edgeId =
                                    rs.getLong(
                                            "id"
                                    );

                            double sourceError =
                                    rs.getDouble(
                                            "source_error_m"
                                    );

                            double targetError =
                                    rs.getDouble(
                                            "target_error_m"
                                    );

                            return error(
                                    "EDGE_ENDPOINT_MISMATCH",
                                    "GRAPH_EDGE",
                                    edgeId,
                                    "Edge endpoint mismatch. "
                                            + "sourceError="
                                            + sourceError
                                            + "m, targetError="
                                            + targetError
                                            + "m"
                            );
                        }
                );

        issues.addAll(
                invalidEdges
        );
    }

    private void validateEdgeDistances(
            List<GraphValidationIssue> issues
    ) {

        String sql = """
                SELECT
                    id,
                    distance_m,

                    ST_Length(
                        ST_Force2D(geom)
                    ) AS geometry_distance_m

                FROM routing.graph_edges

                WHERE ABS(
                    distance_m
                    -
                    ST_Length(
                        ST_Force2D(geom)
                    )
                ) > :distanceTolerance
                """;

        MapSqlParameterSource params =
                params()
                        .addValue(
                                "distanceTolerance",
                                DISTANCE_TOLERANCE_METERS
                        );

        List<GraphValidationIssue> invalidEdges =
                jdbc.query(
                        sql,
                        params,
                        (rs, rowNum) -> {

                            long edgeId =
                                    rs.getLong(
                                            "id"
                                    );

                            double storedDistance =
                                    rs.getDouble(
                                            "distance_m"
                                    );

                            double geometryDistance =
                                    rs.getDouble(
                                            "geometry_distance_m"
                                    );

                            return error(
                                    "EDGE_DISTANCE_MISMATCH",
                                    "GRAPH_EDGE",
                                    edgeId,
                                    "Stored distance does not match geometry length. "
                                            + "stored="
                                            + storedDistance
                                            + "m, geometry="
                                            + geometryDistance
                                            + "m"
                            );
                        }
                );

        issues.addAll(
                invalidEdges
        );
    }

    private MapSqlParameterSource params() {

        return new MapSqlParameterSource()
                .addValue(
                        "metricSrid",
                        properties.metricSrid()
                );
    }

    private GraphValidationIssue error(
            String ruleCode,
            String entityType,
            Long entityId,
            String message
    ) {

        return new GraphValidationIssue(
                ruleCode,
                GraphValidationSeverity.ERROR,
                entityType,
                entityId,
                message
        );
    }
}