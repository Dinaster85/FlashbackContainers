package me.dinaster85.flashbackcontainers.mixin;

import me.dinaster85.flashbackcontainers.ContainerRecorder;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// once per frame, right after the ticks and before rendering (same place Flashback tracks the player)
@Mixin(Minecraft.class)
public class MixinMinecraft {

    @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Window;setErrorSection(Ljava/lang/String;)V", ordinal = 1), require = 0)
    private void trackMouse(boolean advanceGameTime, CallbackInfo ci) {
        Minecraft minecraft = (Minecraft) (Object) this;
        ContainerRecorder.trackFrame(minecraft, minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(true));
    }
}
