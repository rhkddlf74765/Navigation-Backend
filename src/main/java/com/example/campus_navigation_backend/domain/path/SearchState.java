package com.example.campus_navigation_backend.domain.path;

public record SearchState(
        long nodeId,
        double fScore
) {
}
