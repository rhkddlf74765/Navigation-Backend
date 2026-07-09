package com.example.campus_navigation_backend.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Routing request DTO that carries the requesting user and the raw start/destination endpoints.
 * Endpoint interpretation is handled by RouteRequestResolver.
 *
 * @param userId user requesting the route
 * @param start raw start endpoint supplied by the client
 * @param destination raw destination endpoint supplied by the client
 */
public record RouteRequest(
        @NotNull
        UUID userId,

        @Valid
        @NotNull
        RouteEndpointRequest start,

        @Valid
        @NotNull
        RouteEndpointRequest destination
) {
}
