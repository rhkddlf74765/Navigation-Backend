package com.example.campus_navigation_backend.support;

import com.example.campus_navigation_backend.domain.graph.Point3D;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * 지리 좌표 JSON 파싱을 담당한다.
 * <p> DB에서 받은 geometry 문자열을 도메인 좌표 객체로 변환한다.
 */
@Component
@RequiredArgsConstructor
public class GeoJsonGeometryParser {
    /**
     * 공간 데이터베이스에서 받은 JSON 문자열을 파싱한다.
     * 그 결과로 Point3D, List<Point3D>를 생성한다.
     */
    private final ObjectMapper objectMapper;

    public List<Point3D> parseGeometry(String geoJson) {
        if (geoJson == null || geoJson.isBlank()) {
            return List.of();
        }

        JsonNode root = readTree(geoJson);
        String type = getType(root);

        return switch (type) {
            case "LineString" -> parseLineString(geoJson);
            case "Point" -> List.of(parsePoint(geoJson));
            default -> throw new IllegalArgumentException("지?�하지 ?�는 GeoJSON type?�니?? type=" + type);
        };
    }

    public List<Point3D> parseLineString(String geoJson) {
        if (geoJson == null || geoJson.isBlank()) {
            return List.of();
        }

        JsonNode root = readTree(geoJson);
        String type = getType(root);

        if (!"LineString".equalsIgnoreCase(type)) {
            throw new IllegalArgumentException("LineString GeoJSON???�닙?�다. type=" + type);
        }

        JsonNode coordinates = root.get("coordinates");
        if (coordinates == null || !coordinates.isArray()) {
            throw new IllegalArgumentException("LineString coordinates가 ?�바르�? ?�습?�다.");
        }

        List<Point3D> points = new ArrayList<>();
        for (JsonNode coordinate : coordinates) {
            points.add(toPoint3D(coordinate));
        }
        return points;
    }

    public Point3D parsePoint(String geoJson) {
        if (geoJson == null || geoJson.isBlank()) {
            throw new IllegalArgumentException("Point GeoJSON??비어 ?�습?�다.");
        }

        JsonNode root = readTree(geoJson);
        String type = getType(root);

        if (!"Point".equalsIgnoreCase(type)) {
            throw new IllegalArgumentException("Point GeoJSON???�닙?�다. type=" + type);
        }

        JsonNode coordinates = root.get("coordinates");
        if (coordinates == null || !coordinates.isArray()) {
            throw new IllegalArgumentException("Point coordinates가 ?�바르�? ?�습?�다.");
        }

        return toPoint3D(coordinates);
    }

    private JsonNode readTree(String geoJson) {
        try {
            return objectMapper.readTree(geoJson);
        } catch (Exception e) {
            throw new IllegalArgumentException("GeoJSON ?�싱???�패?�습?�다. geoJson=" + geoJson, e);
        }
    }

    private String getType(JsonNode root) {
        JsonNode typeNode = root.get("type");
        if (typeNode == null || typeNode.isNull()) {
            throw new IllegalArgumentException("GeoJSON??type ?�드가 ?�습?�다.");
        }
        return typeNode.asText();
    }

    private Point3D toPoint3D(JsonNode coordinate) {
        if (coordinate == null || !coordinate.isArray() || coordinate.size() < 2) {
            throw new IllegalArgumentException("좌표 ?�식???�바르�? ?�습?�다.");
        }

        double x = coordinate.get(0).asDouble();
        double y = coordinate.get(1).asDouble();
        double z = coordinate.size() >= 3 ? coordinate.get(2).asDouble() : 0.0;

        return new Point3D(x, y, z);
    }
}
