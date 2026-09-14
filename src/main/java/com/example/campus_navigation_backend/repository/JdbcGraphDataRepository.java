package com.example.campus_navigation_backend.repository;

import com.example.campus_navigation_backend.repository.dto.GraphEdgeRow;
import com.example.campus_navigation_backend.repository.dto.GraphNodeRow;
import com.example.campus_navigation_backend.support.GeoJsonGeometryParser;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class JdbcGraphDataRepository
        implements GraphDataRepository {

    private final NamedParameterJdbcTemplate jdbc;

    private final GeoJsonGeometryParser
            geoJsonGeometryParser;

    public JdbcGraphDataRepository(
            NamedParameterJdbcTemplate jdbc,
            GeoJsonGeometryParser geoJsonGeometryParser
    ) {
        this.jdbc = jdbc;
        this.geoJsonGeometryParser =
                geoJsonGeometryParser;
    }

    @Override
    public long findActiveGraphVersionId() {

        String sql = """
                SELECT id
                FROM routing.graph_versions
                WHERE status = 'ACTIVE'
                ORDER BY id
                """;

        List<Long> activeVersionIds =
                jdbc.query(
                        sql,
                        new MapSqlParameterSource(),
                        (rs, rowNum) ->
                                rs.getLong("id")
                );

        if (activeVersionIds.isEmpty()) {
            throw new IllegalStateException(
                    "Active graph version does not exist."
            );
        }

        if (activeVersionIds.size() > 1) {
            throw new IllegalStateException(
                    "Multiple active graph versions exist."
            );
        }

        return activeVersionIds.get(0);
    }

    @Override
    public List<GraphNodeRow> findAllGraphNodes(
            long graphVersionId
    ) {

        String sql = """
                SELECT
                    n.id,
                    n.node_type,

                    NULL::text AS description,

                    n.entrance_id,

                    b.id AS building_id,
                    b.name AS building_name,

                    ST_X(n.geom) AS x,
                    ST_Y(n.geom) AS y,

                    COALESCE(
                        ST_Z(n.geom),
                        0
                    ) AS z

                FROM routing.graph_nodes n

                LEFT JOIN spatial.entrances e
                  ON e.id = n.entrance_id
                 AND e.is_operational = TRUE

                LEFT JOIN spatial.buildings b
                  ON b.id = e.building_id
                 AND b.is_operational = TRUE

                WHERE n.graph_version_id =
                      :graphVersionId

                ORDER BY n.id
                """;

        MapSqlParameterSource params =
                new MapSqlParameterSource()
                        .addValue(
                                "graphVersionId",
                                graphVersionId
                        );

        return jdbc.query(
                sql,
                params,
                (rs, rowNum) ->
                        new GraphNodeRow(
                                rs.getLong("id"),
                                rs.getString(
                                        "node_type"
                                ),
                                rs.getString(
                                        "description"
                                ),
                                rs.getObject(
                                        "entrance_id",
                                        Long.class
                                ),
                                rs.getObject(
                                        "building_id",
                                        Long.class
                                ),
                                rs.getString(
                                        "building_name"
                                ),
                                rs.getDouble("x"),
                                rs.getDouble("y"),
                                rs.getDouble("z")
                        )
        );
    }

    @Override
    public List<GraphEdgeRow> findAllGraphEdges(
            long graphVersionId
    ) {

        String sql = """
                SELECT
                    id,

                    movement_type AS highway,

                    source_node_id AS source,
                    target_node_id AS target,

                    distance_m AS dist,

                    ST_AsGeoJSON(
                        geom
                    ) AS geom_json

                FROM routing.graph_edges

                WHERE graph_version_id =
                      :graphVersionId

                  AND is_enabled = TRUE

                ORDER BY id
                """;

        MapSqlParameterSource params =
                new MapSqlParameterSource()
                        .addValue(
                                "graphVersionId",
                                graphVersionId
                        );

        return jdbc.query(
                sql,
                params,
                (rs, rowNum) ->
                        new GraphEdgeRow(
                                rs.getLong("id"),
                                rs.getString(
                                        "highway"
                                ),
                                rs.getLong(
                                        "source"
                                ),
                                rs.getLong(
                                        "target"
                                ),
                                rs.getDouble(
                                        "dist"
                                ),
                                geoJsonGeometryParser
                                        .parseLineString(
                                                rs.getString(
                                                        "geom_json"
                                                )
                                        )
                        )
        );
    }
}