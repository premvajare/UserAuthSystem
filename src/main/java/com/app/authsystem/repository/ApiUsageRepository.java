package com.app.authsystem.repository;

import com.app.authsystem.entity.ApiUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ApiUsageRepository extends JpaRepository<ApiUsage, Long> {
	List<ApiUsage> findTop100ByOrderByTimestampDesc();

	@Query("SELECT a.endpoint, COUNT(a) FROM ApiUsage a GROUP BY a.endpoint ORDER BY COUNT(a) DESC")
	List<Object[]> countByEndpoint();

	@Query("SELECT a.identifier, COUNT(a) FROM ApiUsage a GROUP BY a.identifier ORDER BY COUNT(a) DESC")
	List<Object[]> countByIdentifier();
}