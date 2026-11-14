package me.dinaster85.flashbackcontainers;

import com.moulberry.flashback.playback.ReplayServer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;

import java.lang.ref.WeakReference;


// JEI starts from the recipes packet, which Flashback never plays back. Its fallback then starts it,
// synchronously, when the first container screen is initialised: the first recorded container shown in a
// replay froze the game for seconds. So init one invisible container screen while the level loading screen
// is still shown, which triggers that fallback where a freeze looks like normal loading.
// JEI stops itself only when the level becomes null (GAME_STOP in Minecraft#updateLevelInEngines), i.e. on
// leaving the replay; a dimension change keeps it running. So once per connection: a new replay, or a new
// connection inside one, comes after such a stop. 

public class JeiWarmup {

    private static final boolean JEI = FabricLoader.getInstance().isModLoaded("jei");

    // only touched on the render thread
    private static WeakReference<ClientPacketListener> warmedConnection = new WeakReference<>(null);
    private static boolean disabled = false;

    // end of ClientPacketListener#startWaitingForNewLevel: the new level is set and a LevelLoadingScreen is shown
    public static void onLevelLoadStarted(Minecraft minecraft, ClientPacketListener connection) {
        if (!JEI || disabled) return;
        // normal play gets the recipes packet
        if (!(minecraft.getSingleplayerServer() instanceof ReplayServer)) return;
        if (minecraft.player == null || minecraft.level == null) return;

        // respawn / dimension change: JEI is still running
        if (warmedConnection.get() == connection) return;
        warmedConnection = new WeakReference<>(connection);

        long start = System.nanoTime();
        try {
            PhantomContainerRenderer.warmUp(minecraft);
        } catch (RuntimeException | LinkageError e) {
            disabled = true;
            FlashbackContainers.LOGGER.error("JEI warm-up failed, JEI will start on the first container shown instead", e);
            return;
        }
        FlashbackContainers.LOGGER.info("JEI warm-up done in {} ms", (System.nanoTime() - start) / 1_000_000);
    }
}
