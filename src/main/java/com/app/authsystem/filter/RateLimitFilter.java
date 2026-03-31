package com.app.authsystem.filter;

import com.app.authsystem.config.RateLimitConfig;
import io.github.bucket4j.Bucket;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RateLimitFilter implements Filter {

    @Autowired
    private RateLimitConfig rateLimitConfig;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        // Use X-Forwarded-For if present, else fallback to remote address
        String ip = req.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = req.getRemoteAddr();
        } else {
            ip = ip.split(",")[0].trim();
        }

        Bucket bucket = rateLimitConfig.resolveBucket(ip);

        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            res.setStatus(429);
            res.getWriter().write("Too Many Requests - Rate limit exceeded");
        }
    }
}