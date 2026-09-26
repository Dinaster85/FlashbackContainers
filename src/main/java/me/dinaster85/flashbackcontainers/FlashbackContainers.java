package me.dinaster85.flashbackcontainers;

import com.moulberry.flashback.action.ActionRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlashbackContainers implements ClientModInitializer {

    public static final String MOD_ID = "flashback_containers";
    public static final Logger LOGGER = LoggerFactory.getLogger("Flashback Containers");

    @Override
    public void onInitializeClient() {
        ActionRegistry.register(ActionContainerState.INSTANCE);
        ActionRegistry.register(ActionContainerMouse.INSTANCE);

        ContainersConfig.load();

        ClientTickEvents.END_CLIENT_TICK.register(ContainerRecorder::tick);
        ClientTickEvents.END_CLIENT_TICK.register(PhantomContainerRenderer::clientTick);
    }
}
