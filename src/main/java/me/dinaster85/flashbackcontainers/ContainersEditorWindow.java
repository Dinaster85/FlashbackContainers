package me.dinaster85.flashbackcontainers;

import imgui.moulberry90.ImGui;
import imgui.moulberry90.type.ImBoolean;
import net.minecraft.client.resources.language.I18n;

// "Containers" window in the Flashback editor, can be docked as a tab like the other windows
public class ContainersEditorWindow {

    private static final int FIRST_USE_EVER = 4; // ImGuiCond_FirstUseEver
    private static final ImBoolean open = new ImBoolean();

    public static void toggle() {
        ContainersConfig config = ContainersConfig.get();
        config.editorWindowOpen = !config.editorWindowOpen;
        ContainersConfig.save();
    }

    public static void render() {
        ContainersConfig config = ContainersConfig.get();
        if (!config.editorWindowOpen) return;

        open.set(true);
        ImGui.setNextWindowSize(320, 135, FIRST_USE_EVER);
        boolean changed = false;
        if (ImGui.begin(I18n.get("flashback_containers.window") + "###FlashbackContainers", open)) {
            if (checkbox("flashback_containers.config.mouse", "flashback_containers.visuals.mouse.tooltip", config.showMouse)) {
                config.showMouse = !config.showMouse;
                changed = true;
            }
            if (checkbox("flashback_containers.config.animation", "flashback_containers.visuals.animation.tooltip", config.animateItems)) {
                config.animateItems = !config.animateItems;
                changed = true;
            }
            if (checkbox("flashback_containers.config.tooltips", "flashback_containers.config.tooltips.tooltip", config.showTooltips)) {
                config.showTooltips = !config.showTooltips;
                changed = true;
            }
        }
        ImGui.end();

        if (!open.get()) {
            config.editorWindowOpen = false;
            changed = true;
        }
        if (changed) ContainersConfig.save();
    }

    private static boolean checkbox(String name, String tooltip, boolean value) {
        boolean clicked = ImGui.checkbox(I18n.get(name), value);
        ImGui.setItemTooltip(I18n.get(tooltip));
        return clicked;
    }
}
