package com.example.campus_navigation_backend.repository;

import com.example.campus_navigation_backend.config.NavigationProperties;
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

    private final NavigationProperties properties;

    private final GeoJsonGeometryParser
            geoJsonGeometryParser;

    public JdbcGraphDataRepository(
            NamedParameterJdbcTemplate jdbc,
            NavigationProperties properties,
            GeoJsonGeometryParser
                    geoJsonGeometryParser
    ) {
        this.jdbc = jdbc;
        this.properties = properties;
        this.geoJsonGeometryParser =
                geoJsonGeometryParser;
    }

    @Override
    public List<GraphNodeRow>
    findAllGraphNodes() {

        String sql = """
                SELECT
                    n.id,
                    n.node_type,
                    n.description,
                    b.id AS building_id,
                    b.name AS building_name,
                    ST_X(
                        ST_Transform(
                            n.geom,
                            :metricSrid
                        )
                    ) AS x,
                    ST_Y(
                        ST_Transform(
                            n.geom,
                            :metricSrid
                        )
                    ) AS y,
                    COALESCE(
                        ST_Z(
                            ST_Transform(
                                n.geom,
                                :metricSrid
                            )
                        ),
                        0
                    ) AS z
                FROM public.final_nodes_3d n
                LEFT JOIN public.entrances e
                  ON LOWER(BTRIM(n.node_type)) = 'entrance'
                 AND e.id = n.id
                LEFT JOIN public.buildings b
                  ON b.id = e.building_id
                WHERE n.geom IS NOT NULL
                ORDER BY n.id
                """;

        MapSqlParameterSource params =
                new MapSqlParameterSource()
                        .addValue(
                                "metricSrid",
                                properties
                                        .metricSrid()
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
    public List<GraphEdgeRow>
    findAllGraphEdges() {

        String sql = """
                SELECT
                    id,
                    highway,
                    source,
                    target,
                    dist,
                    ST_AsGeoJSON(
                        ST_Transform(
                            geom,
                            :metricSrid
                        )
                    ) AS geom_json
                FROM public.final_edges_split_3d
                WHERE geom IS NOT NULL
                  AND source IS NOT NULL
                  AND target IS NOT NULL
                  AND dist IS NOT NULL
                ORDER BY id
                """;

        MapSqlParameterSource params =
                new MapSqlParameterSource()
                        .addValue(
                                "metricSrid",
                                properties
                                        .metricSrid()
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
