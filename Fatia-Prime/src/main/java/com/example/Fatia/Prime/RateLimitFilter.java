package com.example.Fatia.Prime;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.filter.OncePerRequestFilter;

/** Applies per-client request limits to sensitive public endpoints. */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnProperty(name = "app.rate-limit.enabled", havingValue = "true", matchIfMissing = true)
public class RateLimitFilter extends OncePerRequestFilter {

    private static final long WINDOW_NANOS = TimeUnit.MINUTES.toNanos(1);
    private final Cache<String, Window> windows = Caffeine.newBuilder()
        .maximumSize(10_000)
        .expireAfterAccess(2, TimeUnit.MINUTES)
        .build();

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        Limit limit = Limit.forRequest(request.getMethod(), request.getRequestURI());
        if (limit == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = request.getRemoteAddr();
        Window window = windows.get(clientIp + ":" + request.getRequestURI(), ignored -> new Window());
        long retryAfter = window.tryAcquire(limit.requests());
        if (retryAfter > 0) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Retry-After", Long.toString(retryAfter));
            response.getWriter().write("{\"message\":\"Muitas tentativas. Aguarde um minuto e tente novamente.\"}");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private record Limit(int requests) {
        private static Limit forRequest(String method, String path) {
            if ("GET".equals(method) && "/api/frete/consulta".equals(path)) return new Limit(20);
            if ("POST".equals(method) && "/api/pedidos".equals(path)) return new Limit(5);
            if ("POST".equals(method) && "/api/auth/login".equals(path)) return new Limit(5);
            if ("GET".equals(method) && "/api/pedidos/consulta".equals(path)) return new Limit(15);
            return null;
        }
    }

    private static final class Window {
        private long startedAt = System.nanoTime();
        private int requests;

        private synchronized long tryAcquire(int maxRequests) {
            long now = System.nanoTime();
            if (now - startedAt >= WINDOW_NANOS) {
                startedAt = now;
                requests = 0;
            }
            if (requests >= maxRequests) {
                return Math.max(1, (WINDOW_NANOS - (now - startedAt) + 999_999_999L) / 1_000_000_000L);
            }
            requests++;
            return 0;
        }
    }
}
