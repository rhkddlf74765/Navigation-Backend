package com.example.campus_navigation_backend.api.graphmap.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 애플리케이션 루트 요청을 그래프 뷰어로 리다이렉트한다.
 */
@Controller
public class GraphMapRootController {

    @GetMapping("/")
    public String root() {
        return "forward:/graph-map";
    }
}
