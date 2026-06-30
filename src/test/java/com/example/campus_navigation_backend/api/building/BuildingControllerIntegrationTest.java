package com.example.campus_navigation_backend.api.building;

import com.example.campus_navigation_backend.support.PostgisTestContainerSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 건물명 목록 API가 DB에서 로딩된 건물명을 HTTP 응답으로 정확히 반환하는지 검증하는 통합 테스트이다.
 */
@SpringBootTest
@AutoConfigureMockMvc
class BuildingControllerIntegrationTest extends PostgisTestContainerSupport {

    @Autowired
    private MockMvc mockMvc;

    /**
     * 테스트 DB의 입구 노드 description에서 로딩된 건물명이 /api/buildings 응답에 포함되는지 확인한다.
     */
    @Test
    void returnsLoadedBuildingNames() throws Exception {
        mockMvc.perform(get("/api/buildings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.buildings", contains("Test Building")));
    }
}
