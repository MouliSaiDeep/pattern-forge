package com.patternforge.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class ViewController {

    @GetMapping({"/", "/patterns/**"})
    public String forwardToReact(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path.contains("/swagger-ui") || path.contains("/v3/api-docs") || path.contains("/webjars") || path.contains("/swagger-resources")) {
            return "forward:" + path;
        }
        return "forward:/index.html";
    }
}
