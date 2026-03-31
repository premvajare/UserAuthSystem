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

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtil jwtUtil;

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        int statusToLog = 200;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtil.validateToken(token)) {
                String username = jwtUtil.extractUsername(token);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                statusToLog = HttpServletResponse.SC_UNAUTHORIZED;
                response.setStatus(statusToLog);
                response.getWriter().write("Invalid or expired JWT token");
                logger.info("[{}] {} {} -> {}", request.getMethod(), request.getRequestURI(), request.getRemoteAddr(), statusToLog);
                return;
            }
        } else if (request.getRequestURI().startsWith("/users")) {
            statusToLog = HttpServletResponse.SC_UNAUTHORIZED;
            response.setStatus(statusToLog);
            response.getWriter().write("Missing JWT token");
            logger.info("[{}] {} {} -> {}", request.getMethod(), request.getRequestURI(), request.getRemoteAddr(), statusToLog);
            return;
        }
        filterChain.doFilter(request, response);
        // Log the status after filter chain (for successful requests)
        logger.info("[{}] {} {} -> {}", request.getMethod(), request.getRequestURI(), request.getRemoteAddr(), response.getStatus());
    }
}
