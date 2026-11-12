package me.dinaster85.flashbackcontainers;

// The editor window uses the imgui bundled inside Flashback. If an update changes it, the window
// and the menu item are switched off instead of crashing. No ImGui imports here on purpose.
public class EditorUiGuard {

    private static volatile boolean disabled = false;

    public static boolean isDisabled() {
        return disabled;
    }

    public static void fail(String where, LinkageError e) {
        if (disabled) return;
        disabled = true;
        FlashbackContainers.LOGGER.error("Failed to draw the {} in the editor, disabling the Containers window for this session", where, e);
    }
}
