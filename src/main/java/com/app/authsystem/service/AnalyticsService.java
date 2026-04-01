package com.app.authsystem.service;

import com.app.authsystem.dto.ApiUsageView;
import com.app.authsystem.entity.ApiUsage;
import com.app.authsystem.repository.ApiUsageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    @Autowired
    private ApiUsageRepository apiUsageRepository;

    public List<ApiUsageView> getRecentUsage() {
        return apiUsageRepository.findTop100ByOrderByTimestampDesc()
                .stream()
                .map(usage -> new ApiUsageView(usage.getIdentifier(), usage.getEndpoint(), usage.getTimestamp()))
                .collect(Collectors.toList());
    }

    public Map<String, Object> getUsageSummary() {
        Map<String, Object> summary = new HashMap<>();

        // Total API calls
        long totalCalls = apiUsageRepository.count();
        summary.put("total_calls", totalCalls);

        // Top endpoints
        List<Object[]> topEndpoints = apiUsageRepository.countByEndpoint();
        summary.put("top_endpoints", topEndpoints.stream()
                .limit(10)
                .map(item -> new HashMap<String, Object>() {{
                    put("endpoint", item[0]);
                    put("count", item[1]);
                }})
                .collect(Collectors.toList()));

        // Top identifiers (IPs/Users)
        List<Object[]> topIdentifiers = apiUsageRepository.countByIdentifier();
        summary.put("top_identifiers", topIdentifiers.stream()
                .limit(10)
                .map(item -> new HashMap<String, Object>() {{
                    put("identifier", item[0]);
                    put("count", item[1]);
                }})
                .collect(Collectors.toList()));

        return summary;
    }
}

