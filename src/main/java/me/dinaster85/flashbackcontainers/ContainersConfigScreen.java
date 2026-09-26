package me.dinaster85.flashbackcontainers;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

// Mod Menu settings, same as the "Containers" window in Flashback
public class ContainersConfigScreen extends Screen {

    private final Screen parent;

    public ContainersConfigScreen(Screen parent) {
        super(Component.translatable("flashback_containers.config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        ContainersConfig config = ContainersConfig.get();
        int x = width / 2 - 100;
        int y = height / 4 + 24;

        addToggle(x, y, "flashback_containers.config.mouse", "flashback_containers.visuals.mouse.tooltip",
                  config.showMouse, value -> config.showMouse = value);
        addToggle(x, y + 24, "flashback_containers.config.animation", "flashback_containers.visuals.animation.tooltip",
                  config.animateItems, value -> config.animateItems = value);
        addToggle(x, y + 48, "flashback_containers.config.tooltips", "flashback_containers.config.tooltips.tooltip",
                  config.showTooltips, value -> config.showTooltips = value);

        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onClose()).bounds(x, y + 72, 200, 20).build());
    }

    private void addToggle(int x, int y, String name, String tooltip, boolean value, Consumer<Boolean> setter) {
        CycleButton<Boolean> button = CycleButton.onOffBuilder(value).create(x, y, 200, 20, Component.translatable(name), (b, newValue) -> {
            setter.accept(newValue);
            ContainersConfig.save();
        });
        button.setTooltip(Tooltip.create(Component.translatable(tooltip)));
        addRenderableWidget(button);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);
        graphics.centeredText(font, title, width / 2, 20, 0xFFFFFFFF);
    }

    @Override
    public void onClose() {
        minecraft.gui.setScreen(parent);
    }
}
