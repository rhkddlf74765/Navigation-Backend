package com.example.campus_navigation_backend.application.building;

import com.example.campus_navigation_backend.domain.graph.Point3D;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * 건물명과 건물 지점 목록의 대응 관계를 애플리케이션 전체에서 공유하는 불변 조회 테이블로 보관한다.
 * <p>
 * 라우팅에서는 정규화된 건물명으로 지점을 찾고, 건물 목록 API에서는 클라이언트에 보여줄 원본 건물명을 반환한다.
 */
@Component
public class BuildingPointStore {

    private final AtomicReference<Map<String, BuildingPointGroup>> pointsByBuildingName = new AtomicReference<>();

    /**
     * 런타임 라우팅과 건물명 목록 조회가 안정적인 스냅샷을 읽도록 로딩된 건물 지점을 불변 map으로 복사한다.
     */
    public void initialize(Map<String, List<Point3D>> source) {
        if (source == null) {
            throw new IllegalArgumentException("Building points must not be null.");
        }

        Map<String, BuildingPointGroup> copied = source.entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(
                        entry -> normalize(entry.getKey()),
                        entry -> new BuildingPointGroup(entry.getKey(), List.copyOf(entry.getValue())),
                        this::merge
                ));

        if (!pointsByBuildingName.compareAndSet(null, copied)) {
            throw new IllegalStateException("BuildingPointStore has already been initialized.");
        }
    }

    /**
     * 라우팅 요청에서 전달된 건물명으로 해당 건물의 모든 출입구 또는 대표 지점을 조회한다.
     */
    public List<Point3D> findByBuildingName(String buildingName) {
        BuildingPointGroup group = points().get(normalize(buildingName));
        return group == null ? List.of() : group.points();
    }

    /**
     * 프론트가 출발지 또는 도착지 선택 UI를 구성할 수 있도록 표시용 건물명 목록을 정렬해서 반환한다.
     */
    public List<String> findBuildingNames() {
        return points().values().stream()
                .map(BuildingPointGroup::displayName)
                .distinct()
                .sorted(Comparator.naturalOrder())
                .toList();
    }

    /**
     * 같은 정규화 key로 묶이는 건물명이 중복될 경우 먼저 로딩된 표시명을 유지하고 지점 목록만 합친다.
     */
    private BuildingPointGroup merge(BuildingPointGroup left, BuildingPointGroup right) {
        List<Point3D> merged = new ArrayList<>(left.points());
        merged.addAll(right.points());
        return new BuildingPointGroup(left.displayName(), List.copyOf(merged));
    }

    /**
     * 시작 시점에 건물 데이터가 로드되기 전에는 라우팅과 건물명 조회가 불가능하므로 초기화 여부를 검증한다.
     */
    private Map<String, BuildingPointGroup> points() {
        Map<String, BuildingPointGroup> points = pointsByBuildingName.get();
        if (points == null) {
            throw new IllegalStateException("BuildingPointStore has not been initialized yet.");
        }
        return points;
    }

    /**
     * 공백과 대소문자 차이 때문에 건물명 조회가 실패하지 않도록 건물명을 정규화한다.
     */
    private String normalize(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
