package com.app.authsystem.filter;

import com.app.authsystem.entity.ApiUsage;
import com.app.authsystem.repository.ApiUsageRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class UsageTrackingFilter implements Filter {

    @Autowired
    private ApiUsageRepository repository;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;

        String uri = req.getRequestURI();
        if (uri.startsWith("/swagger") || uri.startsWith("/v3/api-docs") || uri.startsWith("/swagger-ui") || uri.startsWith("/swagger-resources") || uri.startsWith("/webjars")) {
            chain.doFilter(request, response);
            return;
        }

        try {
            ApiUsage usage = new ApiUsage();
            usage.setIdentifier(req.getRemoteAddr());
            usage.setEndpoint(req.getRequestURI());
            usage.setTimestamp(LocalDateTime.now());
            repository.save(usage);
        } catch (Exception ignored) {
            // Tracking must not fail the request path.
        }

        chain.doFilter(request, response);
    }
}