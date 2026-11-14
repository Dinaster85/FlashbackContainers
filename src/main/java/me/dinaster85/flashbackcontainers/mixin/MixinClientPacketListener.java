package me.dinaster85.flashbackcontainers.mixin;

import me.dinaster85.flashbackcontainers.JeiWarmup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// handleLogin and handleRespawn: the new level is set and the LevelLoadingScreen is shown at this point
@Mixin(ClientPacketListener.class)
public class MixinClientPacketListener {

    @Inject(
        method = "startWaitingForNewLevel(Lnet/minecraft/client/player/LocalPlayer;Lnet/minecraft/client/multiplayer/ClientLevel;Lnet/minecraft/client/gui/screens/LevelLoadingScreen$Reason;)V",
        at = @At("TAIL"),
        require = 1
    )
    private void warmUpJei(CallbackInfo ci) {
        JeiWarmup.onLevelLoadStarted(Minecraft.getInstance(), (ClientPacketListener) (Object) this);
    }
}
