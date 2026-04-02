package com.app.authsystem.filter;

import com.app.authsystem.config.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtil jwtUtil;

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String uri = request.getRequestURI();
        // Skip JWT check for Swagger/OpenAPI endpoints
        if (uri.startsWith("/swagger") || uri.startsWith("/swagger-ui") || uri.startsWith("/v3/api-docs") || uri.startsWith("/swagger-resources") || uri.startsWith("/webjars")) {
            filterChain.doFilter(request, response);
            return;
        }
        String authHeader = request.getHeader("Authorization");
        int statusToLog = 200;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtil.validateToken(token)) {
                String username = jwtUtil.extractUsername(token);
                String role = jwtUtil.extractRole(token);
                List<SimpleGrantedAuthority> authorities = role != null ? List.of(new SimpleGrantedAuthority("ROLE_" + role)) : List.of();
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(username, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                statusToLog = HttpServletResponse.SC_UNAUTHORIZED;
                response.setStatus(statusToLog);
                response.setContentType("application/json");
                String traceId = (String) request.getAttribute("traceId");
                String json = String.format("{\"timestamp\":\"%s\",\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Invalid or expired JWT token\",\"path\":\"%s\",\"traceId\":\"%s\"}",
                        java.time.LocalDateTime.now(), request.getRequestURI(), traceId != null ? traceId : "");
                response.getWriter().write(json);
                logger.info("[{}] {} {} -> {}", request.getMethod(), request.getRequestURI(), request.getRemoteAddr(), statusToLog);
                return;
            }
        } else if (request.getRequestURI().startsWith("/api/v1/users") || request.getRequestURI().startsWith("/api/v1/analytics")) {
            statusToLog = HttpServletResponse.SC_UNAUTHORIZED;
            response.setStatus(statusToLog);
            response.setContentType("application/json");
            String traceId = (String) request.getAttribute("traceId");
            String json = String.format("{\"timestamp\":\"%s\",\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Missing JWT token\",\"path\":\"%s\",\"traceId\":\"%s\"}",
                    java.time.LocalDateTime.now(), request.getRequestURI(), traceId != null ? traceId : "");
            response.getWriter().write(json);
            logger.info("[{}] {} {} -> {}", request.getMethod(), request.getRequestURI(), request.getRemoteAddr(), statusToLog);
            return;
        }
        filterChain.doFilter(request, response);
        // Log the status after filter chain (for successful requests)
        logger.info("[{}] {} {} -> {}", request.getMethod(), request.getRequestURI(), request.getRemoteAddr(), response.getStatus());
    }
}
