package com.amit.ecommerce.auth_service.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    @GetMapping("/auth/health")
    public String health() {
        return "Auth Service Running";
    }
}