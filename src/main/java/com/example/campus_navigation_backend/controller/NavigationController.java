package com.example.campus_navigation_backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/navigation")
@RequiredArgsConstructor
public class NavigationController {

    private final CampusNavigationService navigationService;

    @PostMapping("/route")
    public RouteResponse route(@RequestBody RouteRequest request) {
        return navigationService.findRoute(request);
    }
}
