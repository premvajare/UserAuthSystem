package com.app.authsystem.controller;

import com.app.authsystem.dto.ApiUsageView;
import com.app.authsystem.service.AnalyticsService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/analytics")
@SecurityRequirement(name = "bearerAuth")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping("/summary")
    public Map<String, Object> getSummary() {
        return analyticsService.getUsageSummary();
    }

    @GetMapping("/recent-usage")
    public List<ApiUsageView> getRecentUsage() {
        return analyticsService.getRecentUsage();
    }
}
