package com.amit.ecommerce.product_service.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductController {

    @GetMapping("/products/health")
    public String health() {
        return "Product Service Running";
    }
}