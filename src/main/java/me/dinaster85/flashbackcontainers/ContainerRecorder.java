package me.dinaster85.flashbackcontainers;

import com.moulberry.flashback.Flashback;
import com.moulberry.flashback.record.Recorder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ContainerRecorder {

    // full state every second even without changes, so seeking finds it fast
    private static final int HEARTBEAT_TICKS = 20;

    private static Recorder lastRecorder;
    private static ContainerState lastWritten;
    private static boolean wroteAnything = false;
    private static int ticksSinceWrite = 0;

    public static void tick(Minecraft minecraft) {
        Recorder recorder = Flashback.RECORDER;
        if (recorder == null || Flashback.isInReplay() || minecraft.player == null) {
            lastRecorder = null;
            return;
        }
        if (recorder != lastRecorder) {
            lastRecorder = recorder;
            lastWritten = null;
            wroteAnything = false;
            ticksSinceWrite = 0;
        }
        if (!recorder.readyToWrite()) return;

        AbstractContainerScreen<?> screen = findScreen(minecraft);
        ContainerState now = capture(minecraft, screen);
        ticksSinceWrite++;

        boolean changed;
        if (!wroteAnything) changed = true;
        else if (now == null) changed = lastWritten != null;
        else changed = !now.sameContents(lastWritten);

        if (changed || ticksSinceWrite >= HEARTBEAT_TICKS) {
            write(recorder, now);
            lastWritten = now;
            wroteAnything = true;
            ticksSinceWrite = 0;
        }

        // every tick, even if the mouse did not move: playback smooths between the last two
        if (now != null && screen != null) writeMouse(minecraft, recorder, screen);
    }

    private static void write(Recorder recorder, ContainerState state) {
        recorder.submitCustomTask(writer -> {
            writer.startAction(ActionContainerState.INSTANCE);
            ContainerState.encode(writer.friendlyByteBuf(), state);
            writer.finishAction(ActionContainerState.INSTANCE);
        });
    }

    private static void writeMouse(Minecraft minecraft, Recorder recorder, AbstractContainerScreen<?> screen) {
        float x = (float) (minecraft.mouseHandler.getScaledXPos(minecraft.getWindow()) - screen.leftPos);
        float y = (float) (minecraft.mouseHandler.getScaledYPos(minecraft.getWindow()) - screen.topPos);
        recorder.submitCustomTask(writer -> {
            writer.startAction(ActionContainerMouse.INSTANCE);
            ActionContainerMouse.encode(writer.friendlyByteBuf(), x, y);
            writer.finishAction(ActionContainerMouse.INSTANCE);
        });
    }

    private static AbstractContainerScreen<?> findScreen(Minecraft minecraft) {
        AbstractContainerMenu menu = minecraft.player.containerMenu;
        if (menu == null) menu = minecraft.player.inventoryMenu;

        if (minecraft.gui.screen() instanceof AbstractContainerScreen<?> screen && screen.getMenu() == menu) return screen;
        return null;
    }

    private static ContainerState capture(Minecraft minecraft, AbstractContainerScreen<?> screen) {
        AbstractContainerMenu menu = minecraft.player.containerMenu;

        // No server container open. Closing is caught here too: whoever closes it,
        // containerMenu goes back to inventoryMenu. The E inventory is client-only, so check the screen.
        if (menu == null || menu == minecraft.player.inventoryMenu || menu.containerId == 0) {
            if (screen instanceof InventoryScreen) {
                return snapshot(minecraft.player.inventoryMenu, 0, ContainerState.PLAYER_INVENTORY, screen.getTitle());
            }
            return null;
        }

        MenuType<?> type;
        try {
            type = menu.getType();
        } catch (UnsupportedOperationException e) {
            return null; // horse inventory and such, no MenuType
        }
        Identifier typeId = BuiltInRegistries.MENU.getKey(type);
        if (typeId == null) return null;

        Component title = screen != null ? screen.getTitle() : Component.empty();
        return snapshot(menu, menu.containerId, typeId, title);
    }

    private static ContainerState snapshot(AbstractContainerMenu menu, int containerId, Identifier typeId, Component title) {
        List<ItemStack> items = new ArrayList<>(menu.slots.size());
        for (Slot slot : menu.slots) {
            items.add(slot.getItem().copy());
        }
        return new ContainerState(containerId, typeId, title, List.copyOf(items), menu.getCarried().copy());
    }
}
