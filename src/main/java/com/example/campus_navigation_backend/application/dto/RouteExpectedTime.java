package com.example.campus_navigation_backend.application.dto;

public record RouteExpectedTime(
        Double time
) {

    public static final RouteExpectedTime DEFAULT =
            new RouteExpectedTime(0.0);

    /*
     * 평균 도보 속도 5km/h
     * = 약 1.3889m/s
     */
    private static final double WALKING_SPEED_METERS_PER_SECOND =
            5.0 * 1000.0 / 3600.0;

    public RouteExpectedTime {

        if (time == null) {
            time = 0.0;
        }

        if (!Double.isFinite(time) || time < 0.0) {
            throw new IllegalArgumentException(
                    "Expected time must be finite and non-negative."
            );
        }
    }

    public static RouteExpectedTime fromDistance(
            Double totalDistanceMeters
    ) {

        if (totalDistanceMeters == null) {
            return DEFAULT;
        }

        if (!Double.isFinite(totalDistanceMeters)
                || totalDistanceMeters < 0.0) {

            throw new IllegalArgumentException(
                    "Distance must be finite and non-negative."
            );
        }

        double seconds =
                totalDistanceMeters
                        / WALKING_SPEED_METERS_PER_SECOND;

        return new RouteExpectedTime(seconds);
    }

    public long seconds() {
        return Math.round(time);
    }

    public double minutes() {
        return time / 60.0;
    }
}