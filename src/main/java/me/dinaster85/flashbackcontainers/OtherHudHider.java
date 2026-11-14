package me.dinaster85.flashbackcontainers;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screens.Screen;

// Once a recorded container has been drawn, Gui#screen() returns an empty stub screen for the rest of
// Gui#extractRenderState, so HUD overlays of other mods (MiniHUD, Jade...) hide themselves as if a screen
// were open. Vanilla Gui reads the field and our own code runs before that point, so they don't see it.
public class OtherHudHider {

    // only touched on the render thread
    private static boolean inGui = false; // inside Gui#extractRenderState
    private static boolean active = false;
    private static PhantomScreenStub stub;

    public static void begin() {
        inGui = true;
        active = false;
    }

    // called by PhantomContainerRenderer after the container was drawn without errors
    public static void enable() {
        active = inGui;
    }

    public static void end() {
        inGui = false;
        active = false;
    }

    public static Screen replace(Screen screen) {
        if (!active || !RenderSystem.isOnRenderThread() || screen != null) return screen;
        if (stub == null) stub = new PhantomScreenStub();
        return stub;
    }
}
