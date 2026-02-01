package com.usg.apiAutomation.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SwaggerRedirectController {

    @GetMapping("/")
    public String redirectToSwagger() {
        // Springdoc 2.x uses this path
        return "redirect:/swagger-ui/index.html";
    }
}
