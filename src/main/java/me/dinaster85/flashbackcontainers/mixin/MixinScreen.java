package me.dinaster85.flashbackcontainers.mixin;

import me.dinaster85.flashbackcontainers.PhantomContainerRenderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Only one blur per frame is allowed. If our screen blurred, a real one opened
// in the same frame (pause menu) would crash.
@Mixin(Screen.class)
public class MixinScreen {

    @Inject(method = "extractBlurredBackground(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V", at = @At("HEAD"), cancellable = true, require = 0)
    private void noBlur(GuiGraphicsExtractor graphics, CallbackInfo ci) {
        if (PhantomContainerRenderer.isRendering()) ci.cancel();
    }
}
