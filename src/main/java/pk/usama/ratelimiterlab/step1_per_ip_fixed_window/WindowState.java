package pk.usama.ratelimiterlab.step1_per_ip_fixed_window;

public class WindowState {

    int count;
    long windowStartMs;

    public WindowState(int count, long windowStartMs) {
        this.count = count;
        this.windowStartMs = windowStartMs;
    }
}
