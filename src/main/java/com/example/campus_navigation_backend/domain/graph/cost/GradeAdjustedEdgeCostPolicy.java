package com.example.campus_navigation_backend.domain.graph.cost;

import org.springframework.stereotype.Component;

@Component
public class GradeAdjustedEdgeCostPolicy
        implements EdgeCostPolicy {

    private static final double EPS = 1e-6;

    private static final double
            STEPS_BASE_MULTIPLIER = 3.0;

    private static final double
            STEPS_UPHILL_GRADE_WEIGHT = 18.0;

    private static final double
            STEPS_DOWNHILL_GRADE_WEIGHT = 10.0;

    private static final double
            RAMP_UPHILL_GRADE_WEIGHT = 8.0;

    private static final double
            RAMP_DOWNHILL_GRADE_WEIGHT = 3.0;

    private static final double
            ROAD_UPHILL_GRADE_WEIGHT = 5.0;

    private static final double
            ROAD_DOWNHILL_GRADE_WEIGHT = 2.0;

    @Override
    public double calculate(
            String highway,
            double distanceMeters,
            double elevationDelta
    ) {
        if (distanceMeters <= EPS) {
            return distanceMeters;
        }

        if ("steps".equalsIgnoreCase(highway)) {
            return calculateStepsCost(
                    distanceMeters,
                    elevationDelta
            );
        }

        if ("ramp".equalsIgnoreCase(highway)) {
            return calculateSlopeCost(
                    distanceMeters,
                    elevationDelta,
                    RAMP_UPHILL_GRADE_WEIGHT,
                    RAMP_DOWNHILL_GRADE_WEIGHT
            );
        }

        return calculateSlopeCost(
                distanceMeters,
                elevationDelta,
                ROAD_UPHILL_GRADE_WEIGHT,
                ROAD_DOWNHILL_GRADE_WEIGHT
        );
    }

    private double calculateStepsCost(
            double distanceMeters,
            double elevationDelta
    ) {
        double grade =
                Math.abs(
                        elevationDelta
                                / distanceMeters
                );

        if (elevationDelta > EPS) {
            return distanceMeters
                    * (
                    STEPS_BASE_MULTIPLIER
                            + grade
                            * STEPS_UPHILL_GRADE_WEIGHT
            );
        }

        if (elevationDelta < -EPS) {
            return distanceMeters
                    * (
                    STEPS_BASE_MULTIPLIER
                            + grade
                            * STEPS_DOWNHILL_GRADE_WEIGHT
            );
        }

        return distanceMeters
                * STEPS_BASE_MULTIPLIER;
    }

    private double calculateSlopeCost(
            double distanceMeters,
            double elevationDelta,
            double uphillGradeWeight,
            double downhillGradeWeight
    ) {
        double grade =
                Math.abs(
                        elevationDelta
                                / distanceMeters
                );

        if (elevationDelta > EPS) {
            return distanceMeters
                    * (
                    1.0
                            + grade
                            * uphillGradeWeight
            );
        }

        if (elevationDelta < -EPS) {
            return distanceMeters
                    * (
                    1.0
                            + grade
                            * downhillGradeWeight
            );
        }

        return distanceMeters;
    }
}