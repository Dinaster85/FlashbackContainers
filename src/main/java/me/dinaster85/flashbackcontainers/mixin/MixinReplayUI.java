package me.dinaster85.flashbackcontainers.mixin;

import com.moulberry.flashback.editor.ui.ReplayUI;
import me.dinaster85.flashbackcontainers.ContainersEditorWindow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ReplayUI.class, remap = false)
public class MixinReplayUI {

    @Inject(
        method = "drawOverlayInternal",
        at = @At(value = "INVOKE", target = "Lcom/moulberry/flashback/editor/ui/windows/WindowType;renderAll()V", shift = At.Shift.AFTER),
        require = 0
    )
    private static void renderWindow(CallbackInfo ci) {
        ContainersEditorWindow.render();
    }
}
