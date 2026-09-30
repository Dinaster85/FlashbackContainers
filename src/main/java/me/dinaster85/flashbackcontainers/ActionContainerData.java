package me.dinaster85.flashbackcontainers;

import com.moulberry.flashback.action.Action;
import com.moulberry.flashback.playback.ReplayServer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;

// Only the numeric fields of the open container, written when just they changed.
// A burning furnace changes its arrow every tick, no need to write all the items for that.
public class ActionContainerData implements Action {

    public static final Identifier NAME = Identifier.fromNamespaceAndPath(FlashbackContainers.MOD_ID, "action/container_data_optional");
    public static final ActionContainerData INSTANCE = new ActionContainerData();

    @Override
    public Identifier name() { return NAME; }

    public static void encode(RegistryFriendlyByteBuf buf, int containerId, int[] data) {
        buf.writeVarInt(containerId);
        ContainerState.writeData(buf, data);
    }

    @Override
    public void handle(ReplayServer replayServer, RegistryFriendlyByteBuf buf) {
        try {
            int containerId = buf.readVarInt();
            int[] data = ContainerState.readData(buf);
            PlaybackState.setData(containerId, data, !replayServer.fastForwarding);
        } catch (Exception e) {
            FlashbackContainers.LOGGER.warn("Could not read container data, skipping it", e);
        }

        if (buf.readableBytes() > 0) buf.skipBytes(buf.readableBytes());
    }
}
