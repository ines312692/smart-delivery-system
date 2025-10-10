package com.example.monitoringservice.repository;


import com.example.monitoringservice.entity.ServiceHealth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceHealthRepository extends JpaRepository<ServiceHealth, Long> {

    Optional<ServiceHealth> findByServiceName(String serviceName);

    List<ServiceHealth> findByStatus(String status);

    List<ServiceHealth> findAllByOrderByServiceName();
}