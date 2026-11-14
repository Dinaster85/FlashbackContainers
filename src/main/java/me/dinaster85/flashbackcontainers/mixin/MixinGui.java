package me.dinaster85.flashbackcontainers.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import me.dinaster85.flashbackcontainers.OtherHudHider;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Gui.class)
public class MixinGui {

    // The whole GUI, with other mods' callbacks at its end (MiniHUD via malilib) and the deferred
    // subtitles where Fabric draws the "last" HUD elements (Jade), see OtherHudHider
    @WrapMethod(method = "extractRenderState", require = 1)
    private void hideOtherHud(DeltaTracker deltaTracker, boolean shouldRenderLevel, boolean resourcesLoaded, Operation<Void> original) {
        OtherHudHider.begin();
        try {
            original.call(deltaTracker, shouldRenderLevel, resourcesLoaded);
        } finally {
            OtherHudHider.end();
        }
    }

    @ModifyReturnValue(method = "screen()Lnet/minecraft/client/gui/screens/Screen;", at = @At("RETURN"), require = 1)
    private Screen stubScreen(Screen screen) {
        return OtherHudHider.replace(screen);
    }
}
