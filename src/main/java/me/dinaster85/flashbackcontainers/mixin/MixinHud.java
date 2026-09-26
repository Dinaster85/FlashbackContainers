package me.dinaster85.flashbackcontainers.mixin;

import me.dinaster85.flashbackcontainers.PhantomContainerRenderer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Drawn together with the hotbar: Flashback already hides it when the camera is not a player,
// when "Hotbar" is off in the editor and when exporting without GUI.
@Mixin(Hud.class)
public class MixinHud {

    @Inject(method = "extractHotbarAndDecorations", at = @At("TAIL"))
    private void drawContainer(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        PhantomContainerRenderer.extract(graphics, deltaTracker);
    }
}
