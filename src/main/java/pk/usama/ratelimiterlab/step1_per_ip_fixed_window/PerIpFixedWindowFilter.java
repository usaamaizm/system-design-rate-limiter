package pk.usama.ratelimiterlab.step1_per_ip_fixed_window;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class PerIpFixedWindowFilter extends OncePerRequestFilter {

    private final PerKeyFixedWindowLimiter limiter = new PerKeyFixedWindowLimiter(100000, 10);

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (request.getRequestURI().startsWith("/actuator/health")) {
            filterChain.doFilter(request, response);
            return;
        }

        String ip = clientIp(request);
        RateLimitDecision d = limiter.check(ip);

        // add headers for both allowed and blocked
        response.setHeader("X-RateLimit-Limit", String.valueOf(d.limit()));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(d.remaining()));
        response.setHeader("X-RateLimit-Reset", String.valueOf(d.resetEpochSeconds()));

        if (!d.allowed()) {
            response.setStatus(429);
            response.setHeader("Retry-After", String.valueOf(d.retryAfterSeconds()));
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType("application/json");
            response.getWriter().print("""
                        {"error":"RATE_LIMITED","message":"Too many requests from this IP. Try again later."}
                    """);
            response.getWriter().flush();
            return;
        }
        filterChain.doFilter(request, response);

    }

    private String clientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
