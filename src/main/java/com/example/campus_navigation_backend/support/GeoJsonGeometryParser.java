package com.example.campus_navigation_backend.support;

import com.example.campus_navigation_backend.domain.graph.MetricPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * PostGIS에서 GeoJSON 형태로 조회한 LineString을
 * 내부 metric 좌표인 MetricPoint 목록으로 변환한다.
 *
 * JdbcGraphDataRepository에서 ST_Transform(..., metricSrid)을
 * 수행한 이후의 GeoJSON만 입력받는 것을 전제로 한다.
 */
@Component
public class GeoJsonGeometryParser {

    private final ObjectMapper objectMapper;

    public GeoJsonGeometryParser(
            ObjectMapper objectMapper
    ) {
        this.objectMapper = objectMapper;
    }

    /**
     * GeoJSON LineString을 MetricPoint 목록으로 변환한다.
     *
     * 입력 좌표는 이미 내부 metric CRS로 변환되어 있어야 한다.
     */
    public List<MetricPoint> parseLineString(
            String geoJson
    ) {
        if (geoJson == null || geoJson.isBlank()) {
            throw new IllegalArgumentException(
                    "LineString GeoJSON must not be blank."
            );
        }

        JsonNode root = readTree(geoJson);

        validateLineString(root);

        JsonNode coordinates =
                root.get("coordinates");

        if (coordinates.size() < 2) {
            throw new IllegalArgumentException(
                    "LineString must contain at least two coordinates."
            );
        }

        List<MetricPoint> points =
                new ArrayList<>(
                        coordinates.size()
                );

        for (JsonNode coordinate : coordinates) {
            points.add(
                    toMetricPoint(
                            coordinate
                    )
            );
        }

        return List.copyOf(points);
    }

    private void validateLineString(
            JsonNode root
    ) {
        JsonNode typeNode =
                root.get("type");

        if (typeNode == null
                || typeNode.isNull()) {

            throw new IllegalArgumentException(
                    "GeoJSON type field is missing."
            );
        }

        String type =
                typeNode.asText();

        if (!"LineString"
                .equalsIgnoreCase(type)) {

            throw new IllegalArgumentException(
                    "GeoJSON type must be LineString. type="
                            + type
            );
        }

        JsonNode coordinates =
                root.get("coordinates");

        if (coordinates == null
                || !coordinates.isArray()) {

            throw new IllegalArgumentException(
                    "LineString coordinates must be an array."
            );
        }
    }

    /**
     * 그래프의 elevation 기반 cost 계산에 Z가 필요하므로
     * edge geometry는 반드시 XYZ 좌표를 가져야 한다.
     *
     * Z가 없는 데이터를 0으로 조용히 대체하지 않고
     * startup 과정에서 잘못된 그래프 데이터로 판단한다.
     */
    private MetricPoint toMetricPoint(
            JsonNode coordinate
    ) {
        if (coordinate == null
                || !coordinate.isArray()
                || coordinate.size() < 3) {

            throw new IllegalArgumentException(
                    "Graph edge coordinate must contain x, y and z."
            );
        }

        JsonNode xNode =
                coordinate.get(0);

        JsonNode yNode =
                coordinate.get(1);

        JsonNode zNode =
                coordinate.get(2);

        if (!xNode.isNumber()
                || !yNode.isNumber()
                || !zNode.isNumber()) {

            throw new IllegalArgumentException(
                    "Graph edge coordinate must contain numeric x, y and z values."
            );
        }

        return new MetricPoint(
                xNode.asDouble(),
                yNode.asDouble(),
                zNode.asDouble()
        );
    }

    private JsonNode readTree(
            String geoJson
    ) {
        try {
            return objectMapper.readTree(
                    geoJson
            );

        } catch (Exception exception) {

            throw new IllegalArgumentException(
                    "Failed to parse GeoJSON.",
                    exception
            );
        }
    }
}