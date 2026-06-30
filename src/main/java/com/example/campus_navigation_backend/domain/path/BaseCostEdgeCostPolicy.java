package com.example.campus_navigation_backend.domain.path;

import org.springframework.stereotype.Component;

/**
 * 고도 차이를 무시하고 기본 비용만 반환하는 엣지 비용 정책이다.
 */
@Component
public class BaseCostEdgeCostPolicy implements EdgeCostPolicy {

    private static final double EPS = 1e-6;

    @Override
    public double calculate(String edgeType, String highway, double baseCost, double elevationDelta) {

        return baseCost;
    }
}
