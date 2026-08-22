package com.example.campus_navigation_backend.domain.graph;

public record EdgePosition(
        int segmentIndex,
        double fraction,
        double offsetMetersFromSource
) implements Comparable<EdgePosition> {

    public EdgePosition {
        if (segmentIndex < 0) {
            throw new IllegalArgumentException(
                    "segmentIndex must be non-negative."
            );
        }

        if (!Double.isFinite(fraction)
                || fraction < 0.0
                || fraction > 1.0) {
            throw new IllegalArgumentException(
                    "fraction must be between 0 and 1."
            );
        }

        if (!Double.isFinite(offsetMetersFromSource)
                || offsetMetersFromSource < 0.0) {
            throw new IllegalArgumentException(
                    "offsetMetersFromSource must be non-negative."
            );
        }
    }

    @Override
    public int compareTo(
            EdgePosition other
    ) {
        return Double.compare(
                offsetMetersFromSource,
                other.offsetMetersFromSource
        );
    }
}
