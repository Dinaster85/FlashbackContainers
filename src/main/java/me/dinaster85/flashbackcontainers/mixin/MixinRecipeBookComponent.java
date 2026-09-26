package me.dinaster85.flashbackcontainers.mixin;

import me.dinaster85.flashbackcontainers.PhantomContainerRenderer;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Recipe book state comes from the viewer's settings, not from the recording, so keep it closed
@Mixin(RecipeBookComponent.class)
public class MixinRecipeBookComponent {

    @Inject(method = "isVisible", at = @At("HEAD"), cancellable = true, require = 0)
    private void keepClosed(CallbackInfoReturnable<Boolean> cir) {
        if (PhantomContainerRenderer.isRendering()) cir.setReturnValue(false);
    }
}
