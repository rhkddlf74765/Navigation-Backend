package com.example.campus_navigation_backend.domain.path;

/**
 * Strategy for calculating edge traversal cost.
 */
public interface EdgeCostPolicy {

    /**
     * Calculates edge cost with the policy-specific adjustment.
     *
     * @param edgeType edge type
     * @param highway OSM highway value
     * @param baseCost base geometric cost
     * @param elevationDelta elevation difference along travel direction
     * @return adjusted traversal cost
     */
    double calculate(String edgeType, String highway, double baseCost, double elevationDelta);
}
