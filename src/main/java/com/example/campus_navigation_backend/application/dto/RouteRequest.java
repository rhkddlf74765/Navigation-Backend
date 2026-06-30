package com.example.campus_navigation_backend.application.dto;

import jakarta.validation.constraints.NotNull;

public record RouteRequest(
        @NotNull Double longitude,
        @NotNull Double latitude,
        Double altitude,
        String destinationBuildingName,
        @NotNull Double destinationLongitude,
        @NotNull Double destinationLatitude,
        Double destinationAltitude
) {
    public RouteRequest(Double longitude, Double latitude, Double altitude, String destinationBuildingName) {
        this(longitude, latitude, altitude, destinationBuildingName, null, null, null);
    }
}
