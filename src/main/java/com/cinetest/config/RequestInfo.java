package com.cinetest.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Resolves the rate limiting cache key per request.
 * Exposed as a Spring bean so it can be referenced from bucket4j SpEL expressions
 * (e.g. {@code @requestInfo.ip()} or {@code @requestInfo.username() ?: @requestInfo.ip()}).
 */
@Component
public class RequestInfo {

    /**
     * Client IP address, honoring the {@code X-Forwarded-For} header when present.
     */
    public String ip() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return "unknown";
        }
        HttpServletRequest request = attributes.getRequest();
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * Authenticated username, or {@code null} for anonymous requests.
     */
    public String username() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String name = authentication.getName();
            if (name != null && !name.equals("anonymousUser")) {
                return name;
            }
        }
        return null;
    }
}
