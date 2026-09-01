package com.example.campus_navigation_backend.domain.graph.cost;

import org.springframework.stereotype.Component;

@Component
public class GradeAdjustedEdgeCostPolicy
        implements EdgeCostPolicy {

    private static final double EPS = 1e-6;

    /*
     * Z 오차 또는 지나치게 짧은 edge 때문에
     * 비정상적으로 큰 grade가 발생하는 것을 방지한다.
     *
     * 0.35 = 35% grade
     */
    private static final double MAX_EFFECTIVE_GRADE = 0.35;


    /*
     * =========================================================
     * Steps
     * =========================================================
     */

    private static final double
            STEPS_UPHILL_BASE_MULTIPLIER = 1.45;

    private static final double
            STEPS_DOWNHILL_BASE_MULTIPLIER = 1.08;

    private static final double
            STEPS_FLAT_MULTIPLIER = 1.15;

    private static final double
            STEPS_UPHILL_GRADE_WEIGHT = 4.0;

    private static final double
            STEPS_DOWNHILL_GRADE_WEIGHT = 1.5;


    /*
     * =========================================================
     * Ramp
     * =========================================================
     */

    private static final double
            RAMP_UPHILL_BASE_MULTIPLIER = 1.05;

    private static final double
            RAMP_DOWNHILL_BASE_MULTIPLIER = 1.02;

    private static final double
            RAMP_UPHILL_GRADE_WEIGHT = 3.0;

    private static final double
            RAMP_DOWNHILL_GRADE_WEIGHT = 0.8;


    /*
     * =========================================================
     * Normal road / footway
     * =========================================================
     */

    private static final double
            ROAD_UPHILL_GRADE_WEIGHT = 2.0;

    private static final double
            ROAD_DOWNHILL_GRADE_WEIGHT = 0.3;


    @Override
    public double calculate(
            String highway,
            double distanceMeters,
            double elevationDelta
    ) {

//        if (distanceMeters <= EPS) {
//            return distanceMeters;
//        }
//
//        double grade =
//                calculateGrade(
//                        distanceMeters,
//                        elevationDelta
//                );
//
//        if ("steps".equalsIgnoreCase(highway)) {
//
//            return calculateStepsCost(
//                    distanceMeters,
//                    elevationDelta,
//                    grade
//            );
//        }
//
//        if ("ramp".equalsIgnoreCase(highway)) {
//
//            return calculateRampCost(
//                    distanceMeters,
//                    elevationDelta,
//                    grade
//            );
//        }
//
//        return calculateRoadCost(
//                distanceMeters,
//                elevationDelta,
//                grade
//        );
        return distanceMeters;
    }


    private double calculateStepsCost(
            double distanceMeters,
            double elevationDelta,
            double grade
    ) {

        if (elevationDelta > EPS) {

            return distanceMeters
                    * (
                    STEPS_UPHILL_BASE_MULTIPLIER
                            + grade
                            * STEPS_UPHILL_GRADE_WEIGHT
            );
        }

        if (elevationDelta < -EPS) {

            return distanceMeters
                    * (
                    STEPS_DOWNHILL_BASE_MULTIPLIER
                            + grade
                            * STEPS_DOWNHILL_GRADE_WEIGHT
            );
        }

        return distanceMeters
                * STEPS_FLAT_MULTIPLIER;
    }


    private double calculateRampCost(
            double distanceMeters,
            double elevationDelta,
            double grade
    ) {

        if (elevationDelta > EPS) {

            return distanceMeters
                    * (
                    RAMP_UPHILL_BASE_MULTIPLIER
                            + grade
                            * RAMP_UPHILL_GRADE_WEIGHT
            );
        }

        if (elevationDelta < -EPS) {

            return distanceMeters
                    * (
                    RAMP_DOWNHILL_BASE_MULTIPLIER
                            + grade
                            * RAMP_DOWNHILL_GRADE_WEIGHT
            );
        }

        return distanceMeters;
    }


    private double calculateRoadCost(
            double distanceMeters,
            double elevationDelta,
            double grade
    ) {

        if (elevationDelta > EPS) {

            return distanceMeters
                    * (
                    1.0
                            + grade
                            * ROAD_UPHILL_GRADE_WEIGHT
            );
        }

        if (elevationDelta < -EPS) {

            return distanceMeters
                    * (
                    1.0
                            + grade
                            * ROAD_DOWNHILL_GRADE_WEIGHT
            );
        }

        return distanceMeters;
    }


    private double calculateGrade(
            double distanceMeters,
            double elevationDelta
    ) {

        double rawGrade =
                Math.abs(
                        elevationDelta
                                / distanceMeters
                );

        return Math.min(
                rawGrade,
                MAX_EFFECTIVE_GRADE
        );
    }
}