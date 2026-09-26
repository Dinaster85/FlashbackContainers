package me.dinaster85.flashbackcontainers.mixin;

import com.moulberry.flashback.editor.ui.windows.MainMenuBar;
import imgui.moulberry90.ImGui;
import me.dinaster85.flashbackcontainers.ContainersEditorWindow;
import net.minecraft.client.resources.language.I18n;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// "Containers" in the top menu, right after "Keybinds" (before the 3rd separator)
@Mixin(value = MainMenuBar.class, remap = false)
public class MixinMainMenuBar {

    @Inject(method = "renderInner", at = @At(value = "INVOKE", target = "Limgui/moulberry90/ImGui;separator()V", ordinal = 2), require = 0)
    private static void addMenuItem(CallbackInfo ci) {
        if (ImGui.menuItem(I18n.get("flashback_containers.window") + "##FlashbackContainers")) {
            ContainersEditorWindow.toggle();
        }
    }
}
