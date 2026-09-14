package com.example.campus_navigation_backend.domain.graph.validation;

import com.example.campus_navigation_backend.config.NavigationProperties;
import com.example.campus_navigation_backend.domain.graph.validation.GraphValidationIssue;
import com.example.campus_navigation_backend.domain.graph.validation.GraphValidationSeverity;
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

    public List<GraphValidationIssue> validate(
            long graphVersionId
    ) {

        List<GraphValidationIssue> issues =
                new ArrayList<>();

        validateGraphNotEmpty(
                graphVersionId,
                issues
        );

        validateEntranceInvariant(
                graphVersionId,
                issues
        );

        validateOperationalBuildings(
                graphVersionId,
                issues
        );

        validateNodeSrid(
                graphVersionId,
                issues
        );

        validateEdgeSrid(
                graphVersionId,
                issues
        );

        validateEdgeEndpoints(
                graphVersionId,
                issues
        );

        validateEdgeDistances(
                graphVersionId,
                issues
        );

        return List.copyOf(
                issues
        );
    }

    private void validateGraphNotEmpty(
            long graphVersionId,
            List<GraphValidationIssue> issues
    ) {

        MapSqlParameterSource params =
                params(graphVersionId);

        Integer nodeCount =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM routing.graph_nodes
                        WHERE graph_version_id =
                              :graphVersionId
                        """,
                        params,
                        Integer.class
                );

        Integer edgeCount =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM routing.graph_edges
                        WHERE graph_version_id =
                              :graphVersionId
                          AND is_enabled = TRUE
                        """,
                        params,
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
            long graphVersionId,
            List<GraphValidationIssue> issues
    ) {

        String sql = """
            SELECT
                id,
                node_type,
                entrance_id
            FROM routing.graph_nodes
            WHERE graph_version_id =
                  :graphVersionId
              AND (
                    (
                        node_type = 'ENTRANCE'
                        AND entrance_id IS NULL
                    )
                    OR
                    (
                        node_type <> 'ENTRANCE'
                        AND entrance_id IS NOT NULL
                    )
              )
            """;

        List<GraphValidationIssue> invalidNodes =
                jdbc.query(
                        sql,
                        params(graphVersionId),
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
            long graphVersionId,
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
                    ON n.graph_version_id =
                       :graphVersionId
                   AND n.node_type = 'ENTRANCE'
                   AND n.entrance_id = e.id

                  WHERE e.building_id = b.id
                    AND e.is_operational = TRUE
              )
            """;

        List<GraphValidationIssue> invalidBuildings =
                jdbc.query(
                        sql,
                        params(graphVersionId),
                        (rs, rowNum) ->
                                error(
                                        "BUILDING_NO_ROUTABLE_ENTRANCE",
                                        "BUILDING",
                                        rs.getLong("id"),
                                        "Operational building has no routable entrance: "
                                                + rs.getString("name")
                                )
                );

        issues.addAll(
                invalidBuildings
        );
    }

    private void validateNodeSrid(
            long graphVersionId,
            List<GraphValidationIssue> issues
    ) {

        String sql = """
            SELECT id
            FROM routing.graph_nodes
            WHERE graph_version_id =
                  :graphVersionId
              AND (
                  geom IS NULL
                  OR ST_IsEmpty(geom)
                  OR ST_SRID(geom)
                     <> :metricSrid
              )
            """;

        List<GraphValidationIssue> invalidNodes =
                jdbc.query(
                        sql,
                        params(graphVersionId),
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

    private void validateEdgeSrid(
            long graphVersionId,
            List<GraphValidationIssue> issues
    ) {

        String sql = """
            SELECT id
            FROM routing.graph_edges
            WHERE graph_version_id =
                  :graphVersionId
              AND (
                  geom IS NULL
                  OR ST_IsEmpty(geom)
                  OR ST_SRID(geom)
                     <> :metricSrid
              )
            """;

        List<GraphValidationIssue> invalidEdges =
                jdbc.query(
                        sql,
                        params(graphVersionId),
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
            long graphVersionId,
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
                  ON s.graph_version_id =
                     e.graph_version_id
                 AND s.id =
                     e.source_node_id

                JOIN routing.graph_nodes t
                  ON t.graph_version_id =
                     e.graph_version_id
                 AND t.id =
                     e.target_node_id

                WHERE e.graph_version_id =
                      :graphVersionId

                  AND (
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
                  )
                """;

        MapSqlParameterSource params =
                params(graphVersionId)
                        .addValue(
                                "endpointTolerance",
                                ENDPOINT_TOLERANCE_METERS
                        );

        jdbc.query(
                sql,
                params,
                rs -> {

                    long edgeId =
                            rs.getLong("id");

                    double sourceError =
                            rs.getDouble(
                                    "source_error_m"
                            );

                    double targetError =
                            rs.getDouble(
                                    "target_error_m"
                            );

                    issues.add(
                            error(
                                    "EDGE_ENDPOINT_MISMATCH",
                                    "GRAPH_EDGE",
                                    edgeId,
                                    "Edge endpoint mismatch. sourceError="
                                            + sourceError
                                            + "m, targetError="
                                            + targetError
                                            + "m"
                            )
                    );
                }
        );
    }

    private void validateEdgeDistances(
            long graphVersionId,
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

                WHERE graph_version_id =
                      :graphVersionId

                  AND ABS(
                      distance_m
                      -
                      ST_Length(
                          ST_Force2D(geom)
                      )
                  ) > :distanceTolerance
                """;

        MapSqlParameterSource params =
                params(graphVersionId)
                        .addValue(
                                "distanceTolerance",
                                DISTANCE_TOLERANCE_METERS
                        );

        jdbc.query(
                sql,
                params,
                rs -> {

                    long edgeId =
                            rs.getLong("id");

                    issues.add(
                            error(
                                    "EDGE_DISTANCE_MISMATCH",
                                    "GRAPH_EDGE",
                                    edgeId,
                                    "Stored distance does not match geometry length."
                            )
                    );
                }
        );
    }

    private MapSqlParameterSource params(
            long graphVersionId
    ) {

        return new MapSqlParameterSource()
                .addValue(
                        "graphVersionId",
                        graphVersionId
                )
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