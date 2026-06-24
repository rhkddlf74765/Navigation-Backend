package com.example.campus_navigation_backend.api.graphmap.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Serves the graph-map viewer page.
 */
@Controller
public class GraphMapPageController {

    @GetMapping("/graph-map")
    public String page() {
        return "forward:/graph-map/index.html";
    }
}
