package me.dinaster85.flashbackcontainers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;

// One rule: player data = the player the camera is in.
// Screens read minecraft.player, which in a replay is the invisible viewer. While the phantom
// screen is drawn, the viewer gets the possessed player's data, and gets its own back right after.
// Flashback keeps the xp of the possessed player up to date for the hotbar.
public class PossessedPlayerData {

    private final LocalPlayer viewer;
    private final int level;
    private final float progress;
    private final int total;
    private final boolean creative;

    private PossessedPlayerData(LocalPlayer viewer) {
        this.viewer = viewer;
        this.level = viewer.experienceLevel;
        this.progress = viewer.experienceProgress;
        this.total = viewer.totalExperience;
        this.creative = viewer.getAbilities().instabuild;
    }

    // returns what to restore, or null if there is nothing to swap
    public static PossessedPlayerData apply(Minecraft minecraft) {
        LocalPlayer viewer = minecraft.player;
        if (viewer == null || !(minecraft.getCameraEntity() instanceof Player possessed) || possessed == viewer) return null;

        PossessedPlayerData saved = new PossessedPlayerData(viewer);
        viewer.experienceLevel = possessed.experienceLevel;
        viewer.experienceProgress = possessed.experienceProgress;
        viewer.totalExperience = possessed.totalExperience;
        viewer.getAbilities().instabuild = isCreative(minecraft, possessed);
        return saved;
    }

    public void restore() {
        viewer.experienceLevel = level;
        viewer.experienceProgress = progress;
        viewer.totalExperience = total;
        viewer.getAbilities().instabuild = creative;
    }

    // a remote player's game mode is only known from the player list
    private static boolean isCreative(Minecraft minecraft, Player player) {
        ClientPacketListener connection = minecraft.getConnection();
        if (connection == null) return false;

        PlayerInfo info = connection.getPlayerInfo(player.getUUID());
        return info != null && info.getGameMode() == GameType.CREATIVE;
    }
}
