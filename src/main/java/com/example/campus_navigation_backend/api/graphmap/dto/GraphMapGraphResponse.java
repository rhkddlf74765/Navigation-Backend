package com.example.campus_navigation_backend.api.graphmap.dto;

import java.util.List;

/**
 * Full graph snapshot for visualizing the campus graph on the web page.
 */
public record GraphMapGraphResponse(
        List<GraphMapNodeResponse> nodes,
        List<GraphMapEdgeResponse> edges
) {
}
