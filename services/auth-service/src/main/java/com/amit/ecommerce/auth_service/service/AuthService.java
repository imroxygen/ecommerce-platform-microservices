package com.amit.ecommerce.auth_service.service;

import com.amit.ecommerce.auth_service.dto.AuthResponse;
import com.amit.ecommerce.auth_service.dto.LoginRequest;
import com.amit.ecommerce.auth_service.dto.RegisterRequest;
import com.amit.ecommerce.auth_service.entity.Role;
import com.amit.ecommerce.auth_service.entity.User;
import com.amit.ecommerce.auth_service.repository.RoleRepository;
import com.amit.ecommerce.auth_service.repository.UserRepository;
import com.amit.ecommerce.auth_service.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /**
     * Handles new user account registrations
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // 1. Validate duplicates
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username is already taken!");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already in use!");
        }

        // 2. Assign default client authorization group
        Role defaultRole = roleRepository.findByName("ROLE_CUSTOMER")
                .orElseThrow(() -> new RuntimeException("Default Role 'ROLE_CUSTOMER' not found. Verify database seeds!"));

        Set<Role> roles = new HashSet<>();
        roles.add(defaultRole);

        // 3. Map values and encrypt raw password text
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // BCrypt hashing
                .userMetadata(request.getMetadata())
                .roles(roles)
                .build();

        User savedUser = userRepository.save(user);

        return generateAuthResponse(savedUser);
    }

    /**
     * Handles user validation checks and session minting
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        // 1. Lookup the target entity profile by username
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password!"));

        // 2. Match the incoming raw secret against the stored hashed matrix
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid username or password!");
        }

        // 3. Authenticated successfully, build token response
        return generateAuthResponse(user);
    }

    /**
     * Helper method to map entities and issue cryptographically signed JWT profiles
     */
    private AuthResponse generateAuthResponse(User user) {
        // Extract plain string names of roles
        Set<String> rolesSet = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        // Flatten all nested granular permissions from the user's roles
        Set<String> capabilitiesSet = user.getRoles().stream()
                .filter(role -> role.getCapabilities() != null)
                .flatMap(role -> role.getCapabilities().stream())
                .map(cap -> cap.getName())
                .collect(Collectors.toSet());

        // Generate the secure JWT token using the central configuration parameters
        String token = jwtService.generateToken(user.getUsername(), rolesSet, capabilitiesSet);

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(rolesSet)
                .capabilities(capabilitiesSet)
                .build();
    }
}