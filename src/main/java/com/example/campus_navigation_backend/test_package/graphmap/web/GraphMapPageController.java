package com.example.campus_navigation_backend.test_package.graphmap.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 그래프 지도 뷰어 페이지를 제공한다.
 */
@Controller
public class GraphMapPageController {

    @GetMapping("/tools/graph-map")
    public String page() {
        return "forward:/graph-map/index.html";
    }
}
