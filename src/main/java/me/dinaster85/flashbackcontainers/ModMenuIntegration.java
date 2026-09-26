package me.dinaster85.flashbackcontainers;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

// loaded only by Mod Menu itself, the mod works without it
public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ContainersConfigScreen::new;
    }
}
