package com.example.campus_navigation_backend.api.graphmap.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Redirects the application root to the graph viewer.
 */
@Controller
public class GraphMapRootController {

    @GetMapping("/")
    public String root() {
        return "forward:/graph-map";
    }
}
