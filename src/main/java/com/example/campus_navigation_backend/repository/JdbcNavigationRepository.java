package com.example.campus_navigation_backend.repository;

import com.example.campus_navigation_backend.config.NavigationProperties;
import com.example.campus_navigation_backend.domain.graph.Point3D;
import com.example.campus_navigation_backend.repository.dto.BuildingPointRow;
import com.example.campus_navigation_backend.repository.dto.GraphEdgeRow;
import com.example.campus_navigation_backend.repository.dto.GraphNodeRow;
import com.example.campus_navigation_backend.repository.dto.TransformedPointRow;
import com.example.campus_navigation_backend.support.GeoJsonGeometryParser;
import com.example.campus_navigation_backend.visualizer.Wgs84PointRow;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class JdbcNavigationRepository implements NavigationRepository {

    private final NamedParameterJdbcTemplate jdbc;
    private final NavigationProperties properties;
    private final GeoJsonGeometryParser geoJsonGeometryParser;
    private final ObjectMapper objectMapper;

    /**
     * 메모리 그래프는 metric 좌표에서 동작하므로, PostGIS에서 그래프 노드를 읽어 설정된 metric SRID로 변환한다.
     */
    @Override
    public List<GraphNodeRow> findAllGraphNodes() {
        String sql = """
                SELECT
                    id,
                    node_type,
                    description,
                    ST_X(mgeom) AS x,
                    ST_Y(mgeom) AS y,
                    COALESCE(ST_Z(mgeom), 0) AS z
                FROM (
                    SELECT
                        id,
                        node_type,
                        description,
                        ST_Transform(geom, :metricSrid) AS mgeom
                    FROM public.final_nodes_3d
                    WHERE geom IS NOT NULL
                ) t
                ORDER BY id
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("metricSrid", properties.metricSrid());

        return jdbc.query(sql, params, (rs, rowNum) ->
                new GraphNodeRow(
                        rs.getLong("id"),
                        rs.getString("node_type"),
                        rs.getString("description"),
                        rs.getDouble("x"),
                        rs.getDouble("y"),
                        rs.getDouble("z")
                )
        );
    }

    /**
     * 경로 탐색 비용과 경로 시각화가 모두 그래프 좌표계의 엣지 polyline을 필요로 하므로,
     * 미터 단위 형상의 그래프 엣지를 조회한다.
     */
    @Override
    public List<GraphEdgeRow> findAllGraphEdges() {
        String sql = """
                SELECT
                    id,
                    original_edge_id,
                    highway,
                    source,
                    target,
                    cost,
                    ST_AsGeoJSON(ST_Transform(geom, :metricSrid)) AS geom_json
                FROM public.final_edges_split_3d
                WHERE geom IS NOT NULL
                ORDER BY id
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("metricSrid", properties.metricSrid());

        return jdbc.query(sql, params, (rs, rowNum) ->
                new GraphEdgeRow(
                        rs.getLong("id"),
                        rs.getLong("original_edge_id"),
                        rs.getString("highway"),
                        rs.getLong("source"),
                        rs.getLong("target"),
                        rs.getDouble("cost"),
                        geoJsonGeometryParser.parseLineString(rs.getString("geom_json"))
                )
        );
    }

    /**
     * 라우팅 끝점 해석 과정에서 {@link com.example.campus_navigation_backend.application.building.BuildingPointStore}에 캐싱할 수 있도록,
     * 이름이 있는 건물 지점을 읽어 metric 좌표로 변환한다.
     */
    @Override
    public List<BuildingPointRow> findAllBuildingPoints() {
        String sql = """
                SELECT
                    id,
                    description AS building_name,
                    ST_X(mgeom) AS x,
                    ST_Y(mgeom) AS y,
                    COALESCE(ST_Z(mgeom), 0) AS z
                FROM (
                    SELECT
                        id,
                        description,
                        ST_Transform(geom, :metricSrid) AS mgeom
                    FROM public.final_nodes_3d
                    WHERE geom IS NOT NULL
                      AND description IS NOT NULL
                      AND BTRIM(description) <> ''
                ) t
                ORDER BY building_name, id
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("metricSrid", properties.metricSrid());

        return jdbc.query(sql, params, (rs, rowNum) ->
                new BuildingPointRow(
                        rs.getLong("id"),
                        rs.getString("building_name"),
                        rs.getDouble("x"),
                        rs.getDouble("y"),
                        rs.getDouble("z")
                )
        );
    }

    /**
     * 투영 로직이 거리 비교를 수행할 수 있도록 하나의 WGS84 요청 좌표를 metric 그래프 SRID로 변환한다.
     */
    @Override
    public TransformedPointRow transformToMetric(double lon, double lat, double ele) {
        String sql = """
                SELECT
                    ST_X(g) AS x,
                    ST_Y(g) AS y,
                    COALESCE(ST_Z(g), 0) AS z
                FROM (
                    SELECT ST_Transform(
                        ST_SetSRID(ST_MakePoint(:lon, :lat, :ele), :requestSrid),
                        :metricSrid
                    ) AS g
                ) t
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("lon", lon)
                .addValue("lat", lat)
                .addValue("ele", ele)
                .addValue("requestSrid", properties.requestSrid())
                .addValue("metricSrid", properties.metricSrid());

        return jdbc.queryForObject(sql, params, (rs, rowNum) ->
                new TransformedPointRow(
                        rs.getDouble("x"),
                        rs.getDouble("y"),
                        rs.getDouble("z")
                )
        );
    }

    /**
     * 지도 클라이언트는 지리 좌표를 렌더링하므로 metric 그래프 경로 좌표를 WGS84로 다시 변환한다.
     */
    @Override
    public List<Wgs84PointRow> transformMetricPointsToWgs84(List<Point3D> metricPoints) {
        if (metricPoints == null || metricPoints.isEmpty()) {
            return List.of();
        }

        String pointsJson = toPointsJson(metricPoints);

        String sql = """
            WITH input AS (
                SELECT
                    ordinality AS seq,
                    ST_Transform(
                        ST_SetSRID(
                            ST_MakePoint(
                                (elem ->> 'x')::double precision,
                                (elem ->> 'y')::double precision,
                                COALESCE((elem ->> 'z')::double precision, 0)
                            ),
                            :metricSrid
                        ),
                        4326
                    ) AS geom
                FROM jsonb_array_elements(CAST(:pointsJson AS jsonb)) WITH ORDINALITY AS t(elem, ordinality)
            )
            SELECT
                seq,
                ST_X(geom) AS lon,
                ST_Y(geom) AS lat,
                COALESCE(ST_Z(geom), 0) AS ele
            FROM input
            ORDER BY seq
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("metricSrid", properties.metricSrid())
                .addValue("pointsJson", pointsJson);

        return jdbc.query(sql, params, (rs, rowNum) ->
                new Wgs84PointRow(
                        rs.getLong("seq"),
                        rs.getDouble("lon"),
                        rs.getDouble("lat"),
                        rs.getDouble("ele")
                )
        );
    }

    private String toPointsJson(List<Point3D> metricPoints) {
        try {
            List<Map<String, Double>> payload = metricPoints.stream()
                    .map(point -> Map.of(
                            "x", point.lon(),
                            "y", point.lat(),
                            "z", point.ele()
                    ))
                    .toList();

            return objectMapper.writeValueAsString(payload);
        } catch (JacksonException e) {
            throw new IllegalStateException("Failed to serialize route points to JSON.", e);
        }
    }
}
