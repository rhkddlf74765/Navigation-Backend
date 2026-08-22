package com.example.campus_navigation_backend.api.building;

import com.example.campus_navigation_backend.api.building.dto.BuildingNameListResponse;
import com.example.campus_navigation_backend.service.building.BuildingQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 프론트엔드가 건물 선택 UI를 구성할 수 있도록 건물명 목록을 제공하는 API 컨트롤러이다.
 */
@RestController
@RequestMapping("/api/buildings")
@RequiredArgsConstructor
public class BuildingController {

    private final BuildingQueryService buildingQueryService;

    /**
     * 메모리에 로딩된 표시용 건물명 목록을 조회해 클라이언트에 반환한다.
     */
    @GetMapping
    public BuildingNameListResponse findBuildingNames() {
        return new BuildingNameListResponse(buildingQueryService.findBuildingNames());
    }
}
