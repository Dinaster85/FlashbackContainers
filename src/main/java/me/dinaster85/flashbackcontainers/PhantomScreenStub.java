package me.dinaster85.flashbackcontainers;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

// What Gui#screen() returns to the HUD under a recorded container (see OtherHudHider).
// Never opened, initialized or drawn, and not a container screen, so nobody treats it as a real menu.
public class PhantomScreenStub extends Screen {

    PhantomScreenStub() {
        super(Component.empty());
    }

    // like a real container screen: subtitles are still drawn, deferred to the end of the GUI
    @Override
    public boolean isInGameUi() {
        return true;
    }
}
