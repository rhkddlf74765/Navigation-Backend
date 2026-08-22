package com.example.campus_navigation_backend.application.dto;

public record RouteExpectedTime(
        Double time
) {
    public static final RouteExpectedTime DEFAULT = new RouteExpectedTime(0.0);

    public static RouteExpectedTime fromDistance(Double totalDistance) {
        if (totalDistance == null) {
            return DEFAULT;
        }
        return new RouteExpectedTime(calculate(totalDistance));
    }

    private static Double calculate(Double totalDistance) {
        return totalDistance / 6.0;
    }
}
