package pk.usama.ratelimiterlab.step0_global_fixed_window;


public class GlobalFixedWindowLimiter {

    private long count;
    private long windowStartMs;
    private final long limit;
    private final long windowMs;

    public GlobalFixedWindowLimiter(long limit, long windowMs) {
        this.windowStartMs = System.currentTimeMillis();
        this.count = 0;
        this.limit = limit;
        this.windowMs = windowMs;

    }

    public synchronized boolean allow() {
        long now = System.currentTimeMillis();
        if (now - windowStartMs >= windowMs) {
            windowStartMs = now;
            count = 0;
        }
        count++;
        if (count <= limit) {
            return true;
        }
        return false;
    }


    public synchronized long retryAfterSeconds(){
        long now  = System.currentTimeMillis();
        long remainingTime = windowMs - (now - windowStartMs);

        if (remainingTime < 0){
            remainingTime = 0;
        }
        double secods = Math.ceil(remainingTime/1000);
        return (long) secods;
    }


}
