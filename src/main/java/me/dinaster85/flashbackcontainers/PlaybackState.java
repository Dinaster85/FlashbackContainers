package me.dinaster85.flashbackcontainers;

import java.util.concurrent.atomic.AtomicLong;

// Written by the replay server thread, read by the render thread.
// Both are in the same JVM, so volatile is enough.
public class PlaybackState {

    // state == null means nothing is open, animatable == false when it came from seeking
    public record Snapshot(ContainerState state, int localPlayerId, long version, boolean animatable) {}

    // jumpId changes when the position must not be smoothed from the previous one (seeking)
    public record Mouse(float x, float y, long jumpId) {}

    private static final AtomicLong version = new AtomicLong();
    private static volatile Snapshot current;
    private static volatile Mouse mouse;
    private static volatile long jumps = 0;

    public static Snapshot get() { return current; }
    public static Mouse getMouse() { return mouse; }

    public static void set(ContainerState state, int localPlayerId, boolean animatable) {
        Snapshot previous = current;
        if (previous != null && previous.localPlayerId() == localPlayerId) {
            if (previous.state() == null && state == null) return;
            if (state != null && state.sameContents(previous.state())) return;
        }

        if (state == null) mouse = null;
        current = new Snapshot(state, localPlayerId, version.incrementAndGet(), animatable);
    }

    public static void setMouse(float x, float y, boolean jump) {
        if (jump) jumps++;
        mouse = new Mouse(x, y, jumps);
    }

    // Flashback rewound to a snapshot, whatever was open is not valid anymore
    public static void clear() {
        current = null;
        mouse = null;
        jumps++;
    }
}
