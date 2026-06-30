package com.example.campus_navigation_backend.service.building;

import com.example.campus_navigation_backend.application.building.BuildingPointStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 건물 endpoint 선택에 필요한 건물명 조회 use case를 처리하는 서비스이다.
 */
@Service
@RequiredArgsConstructor
public class BuildingPointQueryService {

    private final BuildingPointStore buildingPointStore;

    /**
     * 프론트가 출발지 또는 목적지 후보를 표시할 수 있도록 메모리에 로딩된 건물명 목록을 반환한다.
     */
    public List<String> findBuildingNames() {
        return buildingPointStore.findBuildingNames();
    }
}
