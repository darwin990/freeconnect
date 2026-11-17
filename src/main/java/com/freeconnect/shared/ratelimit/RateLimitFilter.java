package com.freeconnect.shared.ratelimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freeconnect.shared.api.ErrorResponse;
import com.freeconnect.shared.config.SecurityProperties;
import com.freeconnect.shared.security.AuthenticatedUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filtro HTTP de rate limiting aplicado a {@code /api/**}.
 *
 * <p>Devuelve 429 con el mismo formato de error uniforme de la API.</p>
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimiter rateLimiter;
    private final SecurityProperties properties;
    private final ObjectMapper objectMapper;

    public RateLimitFilter(RateLimiter rateLimiter, SecurityProperties properties, ObjectMapper objectMapper) {
        this.rateLimiter = rateLimiter;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !properties.rateLimit().enabled() || !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String key = resolveKey(request);
        if (!rateLimiter.tryAcquire(key)) {
            response.setStatus(429);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            ErrorResponse body = ErrorResponse.of(429, "RATE_LIMIT_EXCEEDED",
                    "Demasiadas solicitudes. Intente nuevamente mas tarde.", request.getRequestURI(), List.of());
            objectMapper.writeValue(response.getWriter(), body);
            return;
        }
        chain.doFilter(request, response);
    }

    private String resolveKey(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser user) {
            return "user:" + user.id();
        }
        return "ip:" + request.getRemoteAddr();
    }
}
