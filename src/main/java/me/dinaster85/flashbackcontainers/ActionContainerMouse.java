package me.dinaster85.flashbackcontainers;

import com.moulberry.flashback.action.Action;
import com.moulberry.flashback.playback.ReplayServer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;

// Mouse position relative to the window corner (leftPos/topPos),
// so it works with any window size and gui scale
public class ActionContainerMouse implements Action {

    public static final Identifier NAME = Identifier.fromNamespaceAndPath(FlashbackContainers.MOD_ID, "action/container_mouse_optional");
    public static final ActionContainerMouse INSTANCE = new ActionContainerMouse();

    private static final byte FORMAT_VERSION = 1;

    @Override
    public Identifier name() { return NAME; }

    public static void encode(RegistryFriendlyByteBuf buf, float x, float y) {
        buf.writeByte(FORMAT_VERSION);
        buf.writeFloat(x);
        buf.writeFloat(y);
    }

    @Override
    public void handle(ReplayServer replayServer, RegistryFriendlyByteBuf buf) {
        try {
            if (buf.readByte() == FORMAT_VERSION) {
                float x = buf.readFloat();
                float y = buf.readFloat();
                PlaybackState.setMouse(x, y, replayServer.fastForwarding);
            }
        } catch (Exception e) {
            FlashbackContainers.LOGGER.warn("Could not read container mouse position, skipping it", e);
        }

        if (buf.readableBytes() > 0) buf.skipBytes(buf.readableBytes());
    }
}
