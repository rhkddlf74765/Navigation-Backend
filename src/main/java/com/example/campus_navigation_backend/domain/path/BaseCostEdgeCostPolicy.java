package com.example.campus_navigation_backend.domain.path;

import org.springframework.stereotype.Component;

/**
 * Edge cost policy that ignores elevation differences and returns only the base cost.
 */
@Component
public class BaseCostEdgeCostPolicy implements EdgeCostPolicy {

    private static final double EPS = 1e-6;

    @Override
    public double calculate(String edgeType, String highway, double baseCost, double elevationDelta) {

        return baseCost;
    }
}
