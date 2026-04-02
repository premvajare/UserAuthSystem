package com.app.authsystem.config;

import com.app.authsystem.filter.JwtAuthenticationFilter;
import com.app.authsystem.filter.RateLimitFilter;
import com.app.authsystem.filter.UsageTrackingFilter;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
    info = @Info(
        title = "Authsystem API",
        version = "1.0",
        description = "API documentation for Authsystem with JWT, rate limiting, and user management."
    ),
    security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "Enter JWT token without 'Bearer ' prefix"
)
@Configuration
public class RateLimitFilterConfig {
            @Bean
            public FilterRegistrationBean<com.app.authsystem.filter.TraceIdFilter> traceIdFilterRegistration(com.app.authsystem.filter.TraceIdFilter traceIdFilter) {
                FilterRegistrationBean<com.app.authsystem.filter.TraceIdFilter> registration = new FilterRegistrationBean<>();
                registration.setFilter(traceIdFilter);
                registration.addUrlPatterns("/*");
                registration.setOrder(-1); // TraceId filter runs first
                return registration;
            }
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtAuthFilterRegistration(JwtAuthenticationFilter jwtAuthenticationFilter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(jwtAuthenticationFilter);
        registration.addUrlPatterns("/users/*");
        registration.setOrder(0); // JWT filter runs first
        return registration;
    }

    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilterRegistration(RateLimitFilter rateLimitFilter) {
        FilterRegistrationBean<RateLimitFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(rateLimitFilter);
        registration.addUrlPatterns("/*");
        registration.setOrder(1); // Rate limiting runs after JWT
        return registration;
    }

    @Bean
    public FilterRegistrationBean<UsageTrackingFilter> usageTrackingFilterRegistration(UsageTrackingFilter usageTrackingFilter) {
        FilterRegistrationBean<UsageTrackingFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(usageTrackingFilter);
        registration.addUrlPatterns("/*");
        registration.setOrder(2); // Usage tracking runs after rate limiting
        return registration;
    }
}
