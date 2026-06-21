package com.example.campus_navigation_backend.domain.path;

import org.springframework.stereotype.Component;

/**
 * 도로 유형, 엣지 유형, 고도 차이에 따라 경로 탐색 비용을 보정하는 정책 클래스이다.
 */
@Component
public class EdgeCostPolicy {

    /**
     * 오차 허용값
     */
    private static final double EPS = 1e-6;
    /**
     * 계단을 올라갈 때의 가중치
     */
    private static final double STEPS_UP_MULTIPLIER = 1.8;
    /**
     * 계단을 내려갈 때의 가중치
     */
    private static final double STEPS_DOWN_MULTIPLIER = 1.2;
    /**
     * 경사로를 올라갈 때의 가중치
     */
    private static final double RAMP_UPHILL_GRADE_WEIGHT = 8.0;
    /**
     * 경사로를 내려갈 때의 가중치
     */
    private static final double RAMP_DOWNHILL_GRADE_WEIGHT = 3.0;
    /**
     * 일반도로를 올라갈 때의 가중치
     */
    private static final double ROAD_UPHILL_GRADE_WEIGHT = 5.0;
    /**
     * 일반도로를 내려갈 때의 가중치
     */
    private static final double ROAD_DOWNHILL_GRADE_WEIGHT = 2.0;

    /**
     * 기본 거리 비용에 도로 유형과 이동 방향의 고도 차이를 반영한다.
     *
     * @param edgeType 그래프 엣지 유형
     * @param highway OSM highway 값
     * @param baseCost 보정 전 기본 거리 비용
     * @param elevationDelta 이동 방향 기준 도착 고도와 출발 고도의 차이
     * @return 보정된 탐색 비용
     */
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

    /**
     * 계단의 상행과 하행 비용을 다르게 계산한다.
     *
     * @param baseCost 보정 전 기본 거리 비용
     * @param elevationDelta 이동 방향 기준 고도 차이
     * @return 계단 이동 비용
     */
    private double calculateStepsCost(double baseCost, double elevationDelta) {
        if (elevationDelta > EPS) {
            return baseCost * STEPS_UP_MULTIPLIER;
        }

        if (elevationDelta < -EPS) {
            return baseCost * STEPS_DOWN_MULTIPLIER;
        }

        return baseCost;
    }

    /**
     * 경사도와 상행/하행 방향에 따라 일반 도로 또는 경사로 비용을 계산한다.
     *
     * @param baseCost 보정 전 기본 거리 비용
     * @param elevationDelta 이동 방향 기준 고도 차이
     * @param uphillGradeWeight 오르막 경사도 가중치
     * @param downhillGradeWeight 내리막 경사도 가중치
     * @return 경사 반영 이동 비용
     */
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
