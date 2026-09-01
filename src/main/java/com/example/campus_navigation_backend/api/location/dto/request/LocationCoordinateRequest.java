package com.example.campus_navigation_backend.api.location.dto.request;

import jakarta.validation.constraints.NotNull;


public record LocationCoordinateRequest(
        @NotNull Double lon,
        @NotNull Double lat,
        Double ele
) {
    @Override
    public String toString() {
        return "lon: " + lon + "\nlat: " + lat + "\nele: " + ele;
    }
}
