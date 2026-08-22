package com.example.campus_navigation_backend.service.building;

import com.example.campus_navigation_backend.domain.graph.CampusGraphStore;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BuildingQueryService {

    private final CampusGraphStore
            campusGraphStore;

    public BuildingQueryService(
            CampusGraphStore
                    campusGraphStore
    ) {
        this.campusGraphStore =
                campusGraphStore;
    }

    public List<String>
    findBuildingNames() {
        return campusGraphStore
                .findBuildingNames();
    }
}
