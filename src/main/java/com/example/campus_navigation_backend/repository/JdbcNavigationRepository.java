package com.example.campus_navigation_backend.repository;

import com.example.campus_navigation_backend.config.NavigationProperties;
import com.example.campus_navigation_backend.domain.graph.Point3D;
import com.example.campus_navigation_backend.repository.dto.CurrentProjectionRow;
import com.example.campus_navigation_backend.repository.dto.EntranceProjectionRow;
import com.example.campus_navigation_backend.repository.dto.LineSegmentRow;
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

    /*
        검증을 위해 추가한 필드
     */
    private final ObjectMapper objectMapper;


    @Override
    public List<LineSegmentRow> findAllWalkableLineSegments() {
        String sql = """
                SELECT
                    id,
                    highway,
                    ST_X(ST_StartPoint(mgeom)) AS sx,
                    ST_Y(ST_StartPoint(mgeom)) AS sy,
                    COALESCE(ST_Z(ST_StartPoint(mgeom)), 0) AS sz,
                    ST_X(ST_EndPoint(mgeom)) AS ex,
                    ST_Y(ST_EndPoint(mgeom)) AS ey,
                    COALESCE(ST_Z(ST_EndPoint(mgeom)), 0) AS ez,
                    ST_Length(mgeom) AS cost,
                    ST_AsGeoJSON(mgeom) AS geom_json
                FROM (
                    SELECT id, highway, ST_Transform(geom, :metricSrid) AS mgeom
                    FROM raw_schemas_osm.all_lines_split
                    WHERE geom IS NOT NULL
                      AND highway IN (:walkableHighways)
                ) t
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("metricSrid", properties.metricSrid())
                .addValue("walkableHighways", properties.walkableHighways());

        return jdbc.query(sql, params, (rs, rowNum) ->
                new LineSegmentRow(
                        rs.getLong("id"),
                        rs.getString("highway"),
                        rs.getDouble("sx"),
                        rs.getDouble("sy"),
                        rs.getDouble("sz"),
                        rs.getDouble("ex"),
                        rs.getDouble("ey"),
                        rs.getDouble("ez"),
                        rs.getDouble("cost"),
                        geoJsonGeometryParser.parseLineString(rs.getString("geom_json"))
                )
        );
    }

    @Override
    public List<EntranceProjectionRow> findAllEntranceProjections() {
        String sql = """
                WITH entrances AS (
                    SELECT
                        id,
                        description,
                        node_type,
                        ST_Transform(geom_3d, :metricSrid) AS entrance_geom
                    FROM raw_schemas_osm.entrance_3d
                    WHERE geom_3d IS NOT NULL
                ),
                matched AS (
                    SELECT
                        e.id AS entrance_id,
                        e.description,
                        e.node_type,
                        e.entrance_geom,
                        l.id AS line_id,
                        ST_Transform(l.geom, :metricSrid) AS line_geom
                    FROM entrances e
                    JOIN LATERAL (
                        SELECT id, geom
                        FROM raw_schemas_osm.all_lines_split
                        WHERE geom IS NOT NULL
                          AND highway IN (:walkableHighways)
                        ORDER BY ST_Distance(ST_Transform(geom, :metricSrid), e.entrance_geom)
                        LIMIT 1
                    ) l ON TRUE
                )
                SELECT
                    entrance_id,
                    description,
                    node_type,
                    line_id,
                    ST_X(entrance_geom) AS ex,
                    ST_Y(entrance_geom) AS ey,
                    COALESCE(ST_Z(entrance_geom), 0) AS ez,
                    frac,
                    ST_X(proj_geom) AS px,
                    ST_Y(proj_geom) AS py,
                    COALESCE(ST_Z(proj_geom), 0) AS pz,
                    ST_Distance(entrance_geom, proj_geom) AS connector_cost,
                
                    -- 비용(Length) 계산 시에도 안전하게 CASE 처리를 해주는 것이 좋습니다.
                    CASE WHEN frac <= 1e-9 THEN 0 ELSE ST_Length(ST_LineSubstring(line_geom, 0, frac)) END AS left_cost,
                    CASE WHEN frac >= 1.0 - 1e-9 THEN 0 ELSE ST_Length(ST_LineSubstring(line_geom, frac, 1)) END AS right_cost,
                
                    ST_AsGeoJSON(ST_MakeLine(entrance_geom, proj_geom)) AS connector_geom_json,
                
                    -- [요청하신 코드 삽입 위치]
                    CASE
                        WHEN frac <= 1e-9 THEN NULL
                        ELSE ST_AsGeoJSON(ST_LineSubstring(line_geom, 0, frac))
                    END AS left_geom_json,
                
                    CASE
                        WHEN frac >= 1.0 - 1e-9 THEN NULL
                        ELSE ST_AsGeoJSON(ST_LineSubstring(line_geom, frac, 1))
                    END AS right_geom_json
                FROM (
                    SELECT
                        entrance_id,
                        description,
                        node_type,
                        line_id,
                        entrance_geom,
                        line_geom,
                        ST_LineLocatePoint(line_geom, entrance_geom) AS frac,
                        ST_LineInterpolatePoint(line_geom, ST_LineLocatePoint(line_geom, entrance_geom)) AS proj_geom
                    FROM matched
                ) x;
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("metricSrid", properties.metricSrid())
                .addValue("walkableHighways", properties.walkableHighways());

        return jdbc.query(sql, params, (rs, rowNum) ->
                new EntranceProjectionRow(
                        rs.getLong("entrance_id"),
                        rs.getString("description"),
                        rs.getString("node_type"),
                        rs.getLong("line_id"),
                        rs.getDouble("ex"),
                        rs.getDouble("ey"),
                        rs.getDouble("ez"),
                        rs.getDouble("frac"),
                        rs.getDouble("px"),
                        rs.getDouble("py"),
                        rs.getDouble("pz"),
                        rs.getDouble("connector_cost"),
                        rs.getDouble("left_cost"),
                        rs.getDouble("right_cost"),
                        geoJsonGeometryParser.parseGeometry(rs.getString("connector_geom_json")),
                        geoJsonGeometryParser.parseGeometry(rs.getString("left_geom_json")),
                        geoJsonGeometryParser.parseGeometry(rs.getString("right_geom_json"))
                )
        );
    }

    @Override
    public TransformedPointRow transformToMetric(double longitude, double latitude, double altitude) {
        String sql = """
                SELECT
                    ST_X(g) AS x,
                    ST_Y(g) AS y,
                    COALESCE(ST_Z(g), 0) AS z
                FROM (
                    SELECT ST_Transform(
                        ST_SetSRID(ST_MakePoint(:lon, :lat, :alt), :requestSrid),
                        :metricSrid
                    ) AS g
                ) t
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("lon", longitude)
                .addValue("lat", latitude)
                .addValue("alt", altitude)
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

    @Override
    public CurrentProjectionRow projectCurrentLocationToNearestLine(double x, double y, double z) {
        String sql = """
                WITH p AS (
                    SELECT ST_SetSRID(ST_MakePoint(:x, :y, :z), :metricSrid) AS pt
                )
                SELECT
                    line_id,
                    frac,
                    ST_X(proj_geom) AS px,
                    ST_Y(proj_geom) AS py,
                    COALESCE(ST_Z(proj_geom), 0) AS pz,
                    ST_Distance(pt, proj_geom) AS connector_cost,
                    ST_Length(ST_LineSubstring(line_geom, 0, frac)) AS left_cost,
                    ST_Length(ST_LineSubstring(line_geom, frac, 1)) AS right_cost,
                    ST_AsGeoJSON(ST_MakeLine(pt, proj_geom)) AS connector_geom_json,
                    ST_AsGeoJSON(ST_LineSubstring(line_geom, 0, frac)) AS left_geom_json,
                    ST_AsGeoJSON(ST_LineSubstring(line_geom, frac, 1)) AS right_geom_json
                FROM (
                    SELECT
                        l.id AS line_id,
                        pt,
                        ST_Transform(l.geom, :metricSrid) AS line_geom,
                        ST_LineLocatePoint(ST_Transform(l.geom, :metricSrid), pt) AS frac,
                        ST_LineInterpolatePoint(
                            ST_Transform(l.geom, :metricSrid),
                            ST_LineLocatePoint(ST_Transform(l.geom, :metricSrid), pt)
                        ) AS proj_geom
                    FROM p
                    JOIN LATERAL (
                        SELECT id, geom
                        FROM raw_schemas_osm.all_lines_split
                        WHERE geom IS NOT NULL
                          AND highway IN (:walkableHighways)
                        ORDER BY ST_Distance(ST_Transform(geom, :metricSrid), pt)
                        LIMIT 1
                    ) l ON TRUE
                ) q
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("x", x)
                .addValue("y", y)
                .addValue("z", z)
                .addValue("metricSrid", properties.metricSrid())
                .addValue("walkableHighways", properties.walkableHighways());

        return jdbc.queryForObject(sql, params, (rs, rowNum) ->
                new CurrentProjectionRow(
                        rs.getLong("line_id"),
                        rs.getDouble("frac"),
                        rs.getDouble("px"),
                        rs.getDouble("py"),
                        rs.getDouble("pz"),
                        rs.getDouble("connector_cost"),
                        rs.getDouble("left_cost"),
                        rs.getDouble("right_cost"),
                        geoJsonGeometryParser.parseLineString(rs.getString("connector_geom_json")),
                        geoJsonGeometryParser.parseLineString(rs.getString("left_geom_json")),
                        geoJsonGeometryParser.parseLineString(rs.getString("right_geom_json"))
                )
        );
    }

    /*
        ################################################################################
        ################################################################################
        검증을 위해 추가한 메서드
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
                ST_X(geom) AS longitude,
                ST_Y(geom) AS latitude,
                COALESCE(ST_Z(geom), 0) AS altitude
            FROM input
            ORDER BY seq
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("metricSrid", properties.metricSrid())
                .addValue("pointsJson", pointsJson);

        return jdbc.query(sql, params, (rs, rowNum) ->
                new Wgs84PointRow(
                        rs.getLong("seq"),
                        rs.getDouble("longitude"),
                        rs.getDouble("latitude"),
                        rs.getDouble("altitude")
                )
        );
    }

    private String toPointsJson(List<Point3D> metricPoints) {
        try {
            List<Map<String, Double>> payload = metricPoints.stream()
                    .map(point -> Map.of(
                            "x", point.x(),
                            "y", point.y(),
                            "z", point.z()
                    ))
                    .toList();

            return objectMapper.writeValueAsString(payload);
        } catch (JacksonException e) {
            throw new IllegalStateException("경로 좌표를 JSON으로 직렬화하는 데 실패했습니다.", e);
        }
    }
    /*
        ################################################################################
        ################################################################################
     */

}
