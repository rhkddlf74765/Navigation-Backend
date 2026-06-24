package com.example.campus_navigation_backend.api.graphmap.dto;

import java.util.List;

/**
 * Full graph snapshot in WGS84.
 */
public record GraphMapGeoGraphResponse(
        List<GraphMapGeoNodeResponse> nodes,
        List<GraphMapGeoEdgeResponse> edges
) {
}
