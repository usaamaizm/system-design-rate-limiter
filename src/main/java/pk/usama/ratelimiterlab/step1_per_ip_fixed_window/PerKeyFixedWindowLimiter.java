package pk.usama.ratelimiterlab.step1_per_ip_fixed_window;

import java.util.concurrent.ConcurrentHashMap;

public class PerKeyFixedWindowLimiter {

    private final long windowMs;
    private final int limit;

    private final ConcurrentHashMap<String, WindowState> stateMap = new ConcurrentHashMap<>();

    public PerKeyFixedWindowLimiter(long windowMs, int limit) {
        this.windowMs = windowMs;
        this.limit = limit;
    }


    public boolean allow(String key) {
        long now = System.currentTimeMillis();
        WindowState state = stateMap.computeIfAbsent(key, k -> new WindowState(0, now));
        synchronized (state) {
            if (now - state.windowStartMs >= windowMs) {
                state.windowStartMs = now;
                state.count = 0;
            }

            if (state.count >= limit) {
                return false;
            }
            state.count++;
        }
        return true;
    }

    public long retryAfterSecond(String key) {
        long now = System.currentTimeMillis();
        WindowState state = stateMap.get(key);
        if (state == null) {
            return 0;
        }

        synchronized (state) {
            long timeLeft = windowMs - (now - state.windowStartMs);
            if (timeLeft < 0) {
                timeLeft = 0;
            }
            return (long) Math.ceil(timeLeft / 1000);

        }
    }

    public RateLimitDecision check(String key) {
        long now = System.currentTimeMillis();
        WindowState state = stateMap.computeIfAbsent(key, k -> new WindowState(0, now));

        synchronized (state) {
            if (now - state.windowStartMs >= windowMs) {
                state.windowStartMs = now;
                state.count = 0;
            }

            long windowEndMs = state.windowStartMs + windowMs;
            long resetEpochSeconds = (long) Math.ceil(windowEndMs / 1000.0);

            if (state.count >= limit) {
                long msLeft = windowEndMs - now;
                long retryAfter = (long) Math.ceil(Math.max(0, msLeft) / 1000.0);
                return new RateLimitDecision(false, limit, 0, resetEpochSeconds, Math.max(1, retryAfter));
            }

            // allow and consume 1 request
            state.count++;
            int remaining = Math.max(0, limit - state.count);
            return new RateLimitDecision(true, limit, remaining, resetEpochSeconds, 0);
        }
    }
}
