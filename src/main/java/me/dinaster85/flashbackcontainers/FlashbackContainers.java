package me.dinaster85.flashbackcontainers;

import com.moulberry.flashback.action.ActionRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.MixinEnvironment;

public class FlashbackContainers implements ClientModInitializer {

    public static final String MOD_ID = "flashback_containers";
    public static final Logger LOGGER = LoggerFactory.getLogger("Flashback Containers");

    @Override
    public void onInitializeClient() {
        ActionRegistry.register(ActionContainerState.INSTANCE);
        ActionRegistry.register(ActionContainerMouse.INSTANCE);
        ActionRegistry.register(ActionContainerData.INSTANCE);

        ContainersConfig.load();

        ClientTickEvents.END_CLIENT_TICK.register(ContainerRecorder::tick);
        ClientTickEvents.END_CLIENT_TICK.register(PhantomContainerRenderer::clientTick);

        // dev only: loads every mixin target now, so mixin errors show up at startup, not in a replay
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            ClientLifecycleEvents.CLIENT_STARTED.register(client -> MixinEnvironment.getCurrentEnvironment().audit());
        }
    }
}
