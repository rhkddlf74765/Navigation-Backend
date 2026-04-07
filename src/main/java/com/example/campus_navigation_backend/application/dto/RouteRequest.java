package com.example.campus_navigation_backend.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RouteRequest(
        @NotNull Double longitude,
        @NotNull Double latitude,
        Double altitude,
        @NotBlank String destinationBuildingName
) {
}
