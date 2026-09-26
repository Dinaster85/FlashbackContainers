package me.dinaster85.flashbackcontainers;

import com.mojang.authlib.GameProfile;
import com.moulberry.flashback.Flashback;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

// Draws a real vanilla container screen over the HUD without opening it,
// so textures, titles and resource packs look exactly like in the game.
public class PhantomContainerRenderer {

    private static final int NO_MOUSE = -1000; // far off screen: no highlight, no tooltip

    private static AbstractContainerScreen<?> screen;
    private static ContainerState screenState;
    private static long appliedVersion = -1;
    private static int laidOutWidth = -1;
    private static int laidOutHeight = -1;
    private static final ItemAnimator animator = new ItemAnimator();
    private static final Set<Identifier> brokenMenuTypes = new HashSet<>();

    private static long ticks = 0; // only counts while the replay is playing, used for animations
    private static PlaybackState.Mouse prevMouse;
    private static PlaybackState.Mouse currentMouse;

    private static boolean rendering = false;
    private static boolean mouseShown = false;

    public static boolean isRendering() { return rendering; }
    public static boolean isMouseShown() { return rendering && mouseShown; }
    public static boolean isTooltipShown() { return isMouseShown() && ContainersConfig.get().showTooltips; }

    public static LivingEntity previewEntity() {
        return Minecraft.getInstance().getCameraEntity() instanceof LivingEntity living ? living : null;
    }

    public static void clientTick(Minecraft minecraft) {
        if (Flashback.isInReplay() && minecraft.level != null && minecraft.level.tickRateManager().runsNormally()) {
            ticks++;
        }

        // The replay server sets the mouse on its own ticks, which are out of phase with the
        // client's partial tick. Taking it here, like entity positions, keeps it from shaking.
        PlaybackState.Mouse latest = PlaybackState.getMouse();
        if (latest == null) {
            prevMouse = null;
            currentMouse = null;
        } else if (currentMouse == null || currentMouse.jumpId() != latest.jumpId()) {
            prevMouse = latest;
            currentMouse = latest;
        } else {
            prevMouse = currentMouse;
            currentMouse = latest;
        }
    }

    // called at the end of Hud#extractHotbarAndDecorations, so only when the hotbar is shown
    public static void extract(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        if (!Flashback.isInReplay()) {
            discard();
            return;
        }

        PlaybackState.Snapshot snapshot = PlaybackState.get();
        if (snapshot == null || snapshot.state() == null) {
            discard();
            return;
        }

        // we only know the containers of the player who recorded
        Minecraft minecraft = Minecraft.getInstance();
        Entity camera = minecraft.getCameraEntity();
        if (camera == null || camera.getId() != snapshot.localPlayerId()) return;

        ContainerState state = snapshot.state();
        if (brokenMenuTypes.contains(state.menuType())) return;

        boolean newWindow = screen == null || !state.sameWindow(screenState);
        if (newWindow) {
            discard();
            screen = createScreen(minecraft, state);
            screenState = state;
            if (screen == null) {
                brokenMenuTypes.add(state.menuType());
                return;
            }
        }

        ContainersConfig config = ContainersConfig.get();
        float partialTick = deltaTracker.getGameTimeDeltaPartialTick(false);
        float now = ticks + partialTick;

        rendering = true;
        try {
            if (graphics.guiWidth() != laidOutWidth || graphics.guiHeight() != laidOutHeight) {
                laidOutWidth = graphics.guiWidth();
                laidOutHeight = graphics.guiHeight();
                screen.init(laidOutWidth, laidOutHeight);
            }

            if (appliedVersion != snapshot.version()) {
                boolean animate = config.animateItems && snapshot.animatable() && !newWindow && appliedVersion != -1;
                updateItems(state, animate, config.showMouse, now);
                appliedVersion = snapshot.version();
            }

            int mouseX = NO_MOUSE;
            int mouseY = NO_MOUSE;
            PlaybackState.Mouse prev = prevMouse;
            PlaybackState.Mouse current = currentMouse;
            mouseShown = config.showMouse && prev != null && current != null;
            if (mouseShown) {
                mouseX = screen.leftPos + Math.round(prev.x() + (current.x() - prev.x()) * partialTick);
                mouseY = screen.topPos + Math.round(prev.y() + (current.y() - prev.y()) * partialTick);
            }

            screen.extractBackground(graphics, mouseX, mouseY, 0);
            screen.extractRenderState(graphics, mouseX, mouseY, 0);

            if (!animator.isEmpty()) {
                graphics.nextStratum();
                graphics.pose().pushMatrix();
                graphics.pose().translate(screen.leftPos, screen.topPos);
                animator.extract(graphics, minecraft.font, screen.getMenu(), now);
                graphics.pose().popMatrix();
            }

            // vanilla draws the tooltip only for a real open screen
            if (isTooltipShown()) graphics.extractDeferredElements(mouseX, mouseY, partialTick);
        }
        catch (Throwable t) {
            FlashbackContainers.LOGGER.error("Failed to draw container {} in replay, disabling it for this session", state.menuType(), t);
            brokenMenuTypes.add(state.menuType());
            discard();
        }
        finally {
            rendering = false;
            mouseShown = false;
        }
    }

    private static void updateItems(ContainerState state, boolean animate, boolean mouseShown, float now) {
        AbstractContainerMenu menu = screen.getMenu();
        animator.finishAll(menu);

        List<ItemStack> oldItems = new ArrayList<>(menu.slots.size());
        for (int i = 0; i < menu.slots.size(); i++) {
            oldItems.add(menu.getSlot(i).getItem().copy());
        }
        ItemStack oldCarried = menu.getCarried().copy();

        List<ItemStack> items = state.items();
        int count = Math.min(items.size(), menu.slots.size());
        for (int i = 0; i < count; i++) {
            menu.getSlot(i).set(items.get(i).copy());
        }
        menu.setCarried(state.carried().copy());

        if (animate) animator.plan(menu, oldItems, oldCarried, state, mouseShown, now);
        else animator.reset();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static AbstractContainerScreen<?> createScreen(Minecraft minecraft, ContainerState state) {
        if (minecraft.player == null || minecraft.level == null) return null;

        laidOutWidth = -1;
        laidOutHeight = -1;
        appliedVersion = -1;

        // A separate player / inventory, so filling the slots never touches anyone's real inventory
        if (state.menuType().equals(ContainerState.PLAYER_INVENTORY)) {
            RemotePlayer owner = new RemotePlayer(minecraft.level, new GameProfile(UUID.randomUUID(), "replay_inventory"));
            return new InventoryScreen(owner);
        }

        MenuType<?> type = BuiltInRegistries.MENU.getValue(state.menuType());
        if (type == null) {
            FlashbackContainers.LOGGER.warn("Unknown menu type {} in replay", state.menuType());
            return null;
        }
        MenuScreens.ScreenConstructor constructor = MenuScreens.getConstructor(type);
        if (constructor == null) {
            FlashbackContainers.LOGGER.warn("No screen registered for menu type {}", state.menuType());
            return null;
        }

        try {
            Inventory inventory = new Inventory(minecraft.player, new EntityEquipment());
            AbstractContainerMenu menu = type.create(state.containerId(), inventory);
            Screen created = constructor.create(menu, inventory, state.title());
            if (created instanceof AbstractContainerScreen<?> containerScreen) return containerScreen;

            FlashbackContainers.LOGGER.warn("Screen for {} is not a container screen: {}", state.menuType(), created.getClass().getName());
        } catch (Throwable t) {
            FlashbackContainers.LOGGER.error("Could not create screen for menu type {}", state.menuType(), t);
        }
        return null;
    }

    public static void discard() {
        // no screen.removed(): it was never opened, and removed() would touch the viewer's player
        screen = null;
        screenState = null;
        appliedVersion = -1;
        laidOutWidth = -1;
        laidOutHeight = -1;
        animator.reset();
    }
}
