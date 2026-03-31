package com.app.authsystem.repository;

import com.app.authsystem.entity.ApiUsage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiUsageRepository extends JpaRepository<ApiUsage, Long> {
}