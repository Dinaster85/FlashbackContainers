package me.dinaster85.flashbackcontainers;

import com.moulberry.flashback.action.Action;
import com.moulberry.flashback.playback.ReplayServer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

// Mouse positions during one tick, relative to the window corner (leftPos/topPos),
// so they work with any window size and gui scale. Like Flashback's accurate player position:
// the points are spread evenly over the tick, as many as "local player updates per second" allows.
public class ActionContainerMouse implements Action {

    public static final Identifier NAME = Identifier.fromNamespaceAndPath(FlashbackContainers.MOD_ID, "action/container_mouse_optional");
    public static final ActionContainerMouse INSTANCE = new ActionContainerMouse();

    // 1 = one point per tick (0.2.0 - 0.3.0), 2 = list of points
    private static final byte FORMAT_VERSION = 2;

    public record Point(float x, float y) {}

    @Override
    public Identifier name() { return NAME; }

    public static void encode(RegistryFriendlyByteBuf buf, List<Point> points) {
        buf.writeByte(FORMAT_VERSION);
        buf.writeVarInt(points.size());
        for (Point point : points) {
            buf.writeFloat(point.x());
            buf.writeFloat(point.y());
        }
    }

    @Override
    public void handle(ReplayServer replayServer, RegistryFriendlyByteBuf buf) {
        try {
            byte version = buf.readByte();
            List<Point> points = new ArrayList<>();
            if (version == 1) {
                points.add(new Point(buf.readFloat(), buf.readFloat()));
            } else if (version == 2) {
                int count = buf.readVarInt();
                for (int i = 0; i < count; i++) {
                    points.add(new Point(buf.readFloat(), buf.readFloat()));
                }
            }
            if (!points.isEmpty()) PlaybackState.setMouse(points, replayServer.fastForwarding);
        } catch (Exception e) {
            FlashbackContainers.LOGGER.warn("Could not read container mouse position, skipping it", e);
        }

        if (buf.readableBytes() > 0) buf.skipBytes(buf.readableBytes());
    }
}
