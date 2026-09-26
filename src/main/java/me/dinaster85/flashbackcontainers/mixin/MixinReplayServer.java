package me.dinaster85.flashbackcontainers.mixin;

import com.moulberry.flashback.io.ReplayReader;
import com.moulberry.flashback.playback.ReplayServer;
import me.dinaster85.flashbackcontainers.PlaybackState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Rewinding plays a snapshot and fast-forwards from it. Forget the open container,
// otherwise a window "from the future" stays on screen until the next heartbeat.
@Mixin(value = ReplayServer.class, remap = false)
public class MixinReplayServer {

    @Inject(method = "playSnapshot(Lcom/moulberry/flashback/io/ReplayReader;)V", at = @At("HEAD"), require = 0)
    private void clearOnSnapshot(ReplayReader replayReader, CallbackInfo ci) {
        PlaybackState.clear();
    }
}
