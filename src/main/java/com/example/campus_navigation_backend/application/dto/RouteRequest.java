package com.example.campus_navigation_backend.application.dto;

public record RouteRequest(
        double longitude,
        double latitude,
        Double altitude,
        String destinationBuildingName
) {
}
