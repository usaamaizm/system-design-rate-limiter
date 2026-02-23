package pk.usama.ratelimiterlab.step1_per_ip_fixed_window;

public record RateLimitDecision(
        boolean allowed,
        int limit,
        int remaining,
        long resetEpochSeconds,
        long retryAfterSeconds
) {}
