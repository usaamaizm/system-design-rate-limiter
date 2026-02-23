package pk.usama.ratelimiterlab.step0_global_fixed_window;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

//@Component
public class NaiveGlobalFixedWindowFilter extends OncePerRequestFilter {

    private final GlobalFixedWindowLimiter limiter = new GlobalFixedWindowLimiter(10, 10_0000);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        if (request.getRequestURI().startsWith("/actuator/health")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!limiter.allow()) {
            long retryAfter = limiter.retryAfterSeconds();
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("Retry-After", String.valueOf(retryAfter));
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType("application/json");
            String json = """
                        {"error":"RATE_LIMITED","message":"Too many requests. Try again later."}
                    """;
            response.getWriter().print(json);
            response.getWriter().flush();
            return;
        }
        filterChain.doFilter(request, response);
    }
}
