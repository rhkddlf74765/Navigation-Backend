package com.example.campus_navigation_backend.service.building;

import com.example.campus_navigation_backend.application.building.BuildingPointStore;
import com.example.campus_navigation_backend.domain.graph.Point3D;
import com.example.campus_navigation_backend.repository.NavigationRepository;
import com.example.campus_navigation_backend.repository.dto.BuildingPointRow;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 저장소에서 건물 지점 정보를 읽어 메모리 기반 건물 지점 저장소를 초기화하는 서비스이다.
 */
@Service
@RequiredArgsConstructor
public class BuildingPointInitializer {

    private final NavigationRepository navigationRepository;
    private final BuildingPointStore buildingPointStore;

    /**
     * 라우팅과 건물명 목록 조회가 데이터베이스를 반복 조회하지 않도록 시작 시점에 건물 지점을 메모리에 적재한다.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initialize() {
        Map<String, List<Point3D>> points = navigationRepository.findAllBuildingPoints().stream()
                .collect(Collectors.groupingBy(
                        BuildingPointRow::buildingName,
                        Collectors.mapping(BuildingPointRow::point, Collectors.toList())
                ));

        buildingPointStore.initialize(points);
    }
}
