package com.amit.ecommerce.auth_service.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.Map;

@Getter
@Setter
public class RegisterRequest {
    private String username;
    private String email;
    private String password;
    private Map<String, Object> metadata;
}