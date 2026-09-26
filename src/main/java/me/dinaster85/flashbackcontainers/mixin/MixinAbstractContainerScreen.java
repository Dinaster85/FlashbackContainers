package me.dinaster85.flashbackcontainers.mixin;

import me.dinaster85.flashbackcontainers.PhantomContainerRenderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public class MixinAbstractContainerScreen {

    @Inject(method = "extractTooltip", at = @At("HEAD"), cancellable = true, require = 0)
    private void hideTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY, CallbackInfo ci) {
        if (PhantomContainerRenderer.isRendering() && !PhantomContainerRenderer.isTooltipShown()) ci.cancel();
    }
}
