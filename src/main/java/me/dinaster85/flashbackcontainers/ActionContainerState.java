package me.dinaster85.flashbackcontainers;

import com.moulberry.flashback.action.Action;
import com.moulberry.flashback.playback.ReplayServer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;

// "optional" at the end: Flashback skips unknown optional actions,
// so replays still open after the mod is removed
public class ActionContainerState implements Action {

    public static final Identifier NAME = Identifier.fromNamespaceAndPath(FlashbackContainers.MOD_ID, "action/container_state_optional");
    public static final ActionContainerState INSTANCE = new ActionContainerState();

    @Override
    public Identifier name() { return NAME; }

    @Override
    public void handle(ReplayServer replayServer, RegistryFriendlyByteBuf buf) {
        ContainerState state = null;
        try {
            state = ContainerState.decode(buf);
        } catch (Exception e) {
            // e.g. an item from a mod that is not installed anymore
            FlashbackContainers.LOGGER.warn("Could not read container state, skipping it", e);
        }

        // Flashback throws if an action leaves unread bytes
        if (buf.readableBytes() > 0) buf.skipBytes(buf.readableBytes());

        PlaybackState.set(state, replayServer.getLocalPlayerId(), !replayServer.fastForwarding);
    }
}
