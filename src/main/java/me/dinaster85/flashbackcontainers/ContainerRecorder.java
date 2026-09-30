package me.dinaster85.flashbackcontainers;

import com.moulberry.flashback.Flashback;
import com.moulberry.flashback.record.Recorder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ContainerRecorder {

    // full state every second even without changes, so seeking finds it fast
    private static final int HEARTBEAT_TICKS = 20;

    private static Recorder lastRecorder;
    private static ContainerState lastWritten;
    private static boolean wroteAnything = false;
    private static int ticksSinceWrite = 0;

    private static final TreeMap<Float, ActionContainerMouse.Point> frameMouse = new TreeMap<>(); // partial tick -> mouse
    private static ActionContainerMouse.Point lastTickMouse;

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

        boolean itemsChanged;
        if (!wroteAnything) itemsChanged = true;
        else if (now == null) itemsChanged = lastWritten != null;
        else itemsChanged = !now.sameItems(lastWritten);

        if (itemsChanged || ticksSinceWrite >= HEARTBEAT_TICKS) {
            write(recorder, now);
            lastWritten = now;
            wroteAnything = true;
            ticksSinceWrite = 0;
        } else if (now != null && !Arrays.equals(now.data(), lastWritten.data())) {
            writeData(recorder, now);
            lastWritten = now;
        }

        // every tick, even if the mouse did not move: playback smooths between the points
        if (now != null && screen != null) {
            writeMouse(recorder, mousePosition(minecraft, screen));
        } else {
            frameMouse.clear();
            lastTickMouse = null;
        }
    }

    // called every frame, remembers where the mouse was at which moment of the tick
    public static void trackFrame(Minecraft minecraft, float partialTick) {
        Recorder recorder = Flashback.RECORDER;
        if (recorder == null || Flashback.isInReplay() || minecraft.player == null || !recorder.readyToWrite()) return;

        AbstractContainerScreen<?> screen = findScreen(minecraft);
        if (screen != null) frameMouse.put(partialTick, mousePosition(minecraft, screen));
    }

    private static void write(Recorder recorder, ContainerState state) {
        recorder.submitCustomTask(writer -> {
            writer.startAction(ActionContainerState.INSTANCE);
            ContainerState.encode(writer.friendlyByteBuf(), state);
            writer.finishAction(ActionContainerState.INSTANCE);
        });
    }

    private static void writeData(Recorder recorder, ContainerState state) {
        recorder.submitCustomTask(writer -> {
            writer.startAction(ActionContainerData.INSTANCE);
            ActionContainerData.encode(writer.friendlyByteBuf(), state.containerId(), state.data());
            writer.finishAction(ActionContainerData.INSTANCE);
        });
    }

    // Same as Flashback does for the player: "local player updates per second" / 20 steps per tick,
    // each point taken from the frames around it. 20 updates = start and end of the tick only.
    private static void writeMouse(Recorder recorder, ActionContainerMouse.Point end) {
        ActionContainerMouse.Point start = lastTickMouse != null ? lastTickMouse : end;
        int steps = Math.max(1, Flashback.getConfig().recording.localPlayerUpdatesPerSecond / 20);

        List<ActionContainerMouse.Point> points = new ArrayList<>(steps + 1);
        for (int i = 0; i <= steps; i++) {
            float time = (float) i / steps;

            float fromTime = 0;
            ActionContainerMouse.Point from = start;
            Map.Entry<Float, ActionContainerMouse.Point> before = frameMouse.floorEntry(time);
            if (before != null) {
                fromTime = before.getKey();
                from = before.getValue();
            }

            float toTime = 1;
            ActionContainerMouse.Point to = end;
            Map.Entry<Float, ActionContainerMouse.Point> after = frameMouse.ceilingEntry(Math.nextUp(time));
            if (after != null) {
                toTime = after.getKey();
                to = after.getValue();
            }

            float t = toTime > fromTime ? (time - fromTime) / (toTime - fromTime) : 1;
            t = Math.clamp(t, 0, 1);
            points.add(new ActionContainerMouse.Point(from.x() + (to.x() - from.x()) * t, from.y() + (to.y() - from.y()) * t));
        }

        lastTickMouse = end;
        frameMouse.clear();

        recorder.submitCustomTask(writer -> {
            writer.startAction(ActionContainerMouse.INSTANCE);
            ActionContainerMouse.encode(writer.friendlyByteBuf(), points);
            writer.finishAction(ActionContainerMouse.INSTANCE);
        });
    }

    private static ActionContainerMouse.Point mousePosition(Minecraft minecraft, AbstractContainerScreen<?> screen) {
        float x = (float) (minecraft.mouseHandler.getScaledXPos(minecraft.getWindow()) - screen.leftPos);
        float y = (float) (minecraft.mouseHandler.getScaledYPos(minecraft.getWindow()) - screen.topPos);
        return new ActionContainerMouse.Point(x, y);
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
        ContainerState state = snapshot(menu, menu.containerId, typeId, title);
        if (!(menu instanceof MerchantMenu merchantMenu)) return state;

        // the selected trade and the scroll position only exist on the screen
        int selected = screen instanceof MerchantScreen merchantScreen ? merchantScreen.shopItem : 0;
        int scroll = screen instanceof MerchantScreen merchantScreen ? merchantScreen.scrollOff : 0;
        MerchantState merchant = new MerchantState(merchantMenu.getOffers().copy(), merchantMenu.getTraderXp(), merchantMenu.getTraderLevel(),
                                                   merchantMenu.showProgressBar(), merchantMenu.canRestock(), selected, scroll);
        return new ContainerState(state.containerId(), typeId, title, state.items(), state.carried(), state.data(), merchant);
    }

    private static ContainerState snapshot(AbstractContainerMenu menu, int containerId, Identifier typeId, Component title) {
        List<ItemStack> items = new ArrayList<>(menu.slots.size());
        for (Slot slot : menu.slots) {
            items.add(slot.getItem().copy());
        }

        int[] data = new int[menu.dataSlots.size()];
        for (int i = 0; i < data.length; i++) {
            data[i] = menu.dataSlots.get(i).get();
        }

        return new ContainerState(containerId, typeId, title, List.copyOf(items), menu.getCarried().copy(), data, null);
    }
}
