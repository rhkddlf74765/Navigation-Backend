package com.example.campus_navigation_backend.domain.path;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Edge cost policy that adjusts base cost by uphill/downhill grade and stairs.
 */
@Primary
@Component
public class GradeAdjustedEdgeCostPolicy implements EdgeCostPolicy {

    private static final double EPS = 1e-6;
    private static final double STEPS_BASE_MULTIPLIER = 3.0;
    private static final double STEPS_UPHILL_GRADE_WEIGHT = 18.0;
    private static final double STEPS_DOWNHILL_GRADE_WEIGHT = 10.0;
    private static final double RAMP_UPHILL_GRADE_WEIGHT = 8.0;
    private static final double RAMP_DOWNHILL_GRADE_WEIGHT = 3.0;
    private static final double ROAD_UPHILL_GRADE_WEIGHT = 5.0;
    private static final double ROAD_DOWNHILL_GRADE_WEIGHT = 2.0;

    @Override
    public double calculate(String edgeType, String highway, double baseCost, double elevationDelta) {
        if (baseCost <= EPS) {
            return baseCost;
        }

        if ("steps".equalsIgnoreCase(highway)) {
            return calculateStepsCost(baseCost, elevationDelta);
        }

        if ("ramp".equalsIgnoreCase(highway) || "ramp".equalsIgnoreCase(edgeType)) {
            return calculateSlopeCost(baseCost, elevationDelta, RAMP_UPHILL_GRADE_WEIGHT, RAMP_DOWNHILL_GRADE_WEIGHT);
        }

        return calculateSlopeCost(baseCost, elevationDelta, ROAD_UPHILL_GRADE_WEIGHT, ROAD_DOWNHILL_GRADE_WEIGHT);
    }

    private double calculateStepsCost(double baseCost, double elevationDelta) {
        double grade = Math.abs(elevationDelta / baseCost);

        if (elevationDelta > EPS) {
            return baseCost * (STEPS_BASE_MULTIPLIER + grade * STEPS_UPHILL_GRADE_WEIGHT);
        }

        if (elevationDelta < -EPS) {
            return baseCost * (STEPS_BASE_MULTIPLIER + grade * STEPS_DOWNHILL_GRADE_WEIGHT);
        }

        return baseCost * STEPS_BASE_MULTIPLIER;
    }

    private double calculateSlopeCost(double baseCost,
                                      double elevationDelta,
                                      double uphillGradeWeight,
                                      double downhillGradeWeight) {
        double grade = Math.abs(elevationDelta / baseCost);

        if (elevationDelta > EPS) {
            return baseCost * (1.0 + grade * uphillGradeWeight);
        }

        if (elevationDelta < -EPS) {
            return baseCost * (1.0 + grade * downhillGradeWeight);
        }

        return baseCost;
    }
}
