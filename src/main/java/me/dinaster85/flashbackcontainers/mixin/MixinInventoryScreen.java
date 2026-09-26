package me.dinaster85.flashbackcontainers.mixin;

import me.dinaster85.flashbackcontainers.PhantomContainerRenderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(InventoryScreen.class)
public class MixinInventoryScreen {

    // init() swaps to the creative screen if the viewer is in creative
    @Redirect(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;hasInfiniteMaterials()Z"), require = 0)
    private boolean neverCreative(LocalPlayer player) {
        return !PhantomContainerRenderer.isRendering() && player.hasInfiniteMaterials();
    }

    // show the player who recorded instead of the viewer, looking at the recorded mouse (or straight ahead)
    @Redirect(
        method = "extractBackground",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/InventoryScreen;extractEntityInInventoryFollowsMouse(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIIIIFFFLnet/minecraft/world/entity/LivingEntity;)V"),
        require = 0
    )
    private void showRecordedPlayer(GuiGraphicsExtractor graphics, int x0, int y0, int x1, int y1, int size, float offsetY, float mouseX, float mouseY, LivingEntity entity) {
        if (PhantomContainerRenderer.isRendering()) {
            entity = PhantomContainerRenderer.previewEntity();
            if (entity == null) return;

            if (!PhantomContainerRenderer.isMouseShown()) {
                mouseX = (x0 + x1) / 2.0f;
                mouseY = (y0 + y1) / 2.0f;
            }
        }
        InventoryScreen.extractEntityInInventoryFollowsMouse(graphics, x0, y0, x1, y1, size, offsetY, mouseX, mouseY, entity);
    }
}
