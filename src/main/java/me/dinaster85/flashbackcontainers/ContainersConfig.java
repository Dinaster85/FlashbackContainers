package me.dinaster85.flashbackcontainers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

// Only changes how replays are drawn, everything is always recorded
public class ContainersConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("flashback_containers.json");

    private static ContainersConfig instance = new ContainersConfig();

    public boolean showMouse = true;
    public boolean animateItems = true;
    public boolean showTooltips = false;
    public boolean editorWindowOpen = false;

    public static ContainersConfig get() { return instance; }

    public static void load() {
        if (!Files.exists(PATH)) {
            save();
            return;
        }

        try (Reader reader = Files.newBufferedReader(PATH)) {
            ContainersConfig loaded = GSON.fromJson(reader, ContainersConfig.class);
            if (loaded != null) instance = loaded;
        } catch (Exception e) {
            FlashbackContainers.LOGGER.warn("Could not read {}, using defaults", PATH, e);
        }
    }

    public static void save() {
        try {
            Files.createDirectories(PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(PATH)) {
                GSON.toJson(instance, writer);
            }
        } catch (Exception e) {
            FlashbackContainers.LOGGER.warn("Could not save {}", PATH, e);
        }
    }
}
