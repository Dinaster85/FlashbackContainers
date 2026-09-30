package me.dinaster85.flashbackcontainers;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

// Written by the replay server thread, read by the render thread.
// Both are in the same JVM, so volatile is enough.
public class PlaybackState {

    // state == null means nothing is open, animatable == false when it came from seeking
    public record Snapshot(ContainerState state, int localPlayerId, long version, boolean animatable) {}

    // mouse points of one tick, jumpId changes when they must not be smoothed from the previous ones (seeking)
    public record Mouse(List<ActionContainerMouse.Point> points, long jumpId) {}

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

    public static void setData(int containerId, int[] data, boolean animatable) {
        Snapshot previous = current;
        if (previous == null || previous.state() == null || previous.state().containerId() != containerId) return;

        current = new Snapshot(previous.state().withData(data), previous.localPlayerId(), version.incrementAndGet(), animatable);
    }

    public static void setMouse(List<ActionContainerMouse.Point> points, boolean jump) {
        if (jump) jumps++;
        mouse = new Mouse(List.copyOf(points), jumps);
    }

    // Flashback rewound to a snapshot, whatever was open is not valid anymore
    public static void clear() {
        current = null;
        mouse = null;
        jumps++;
    }
}
