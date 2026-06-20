package com.amit.ecommerce.auth_service.repository;

import com.amit.ecommerce.auth_service.entity.Capability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CapabilityRepository extends JpaRepository<Capability, Long> {
    Optional<Capability> findByName(String name);
}