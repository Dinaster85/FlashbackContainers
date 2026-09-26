package me.dinaster85.flashbackcontainers;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

// Items flying between slots.
// Compares two states: a slot that lost an item + a slot that got the same item = one flight.
// The target slot keeps showing its old contents until the item lands.
public class ItemAnimator {

    private static final float DURATION_TICKS = 4.0f;
    private static final int MAX_FLIGHTS = 64; // bigger changes at once (sorting mods, /clear) are not animated

    private record Flight(int from, int to, ItemStack stack, float startTime) {}
    private record Change(int slot, ItemStack stack, int count) {}

    private final List<Flight> flights = new ArrayList<>();
    private final Map<Integer, ItemStack> landing = new HashMap<>(); // target slot -> contents after landing
    private int pickupSlot = -1; // with the mouse hidden: where the item on the cursor was taken from

    public boolean isEmpty() { return flights.isEmpty(); }

    public void reset() {
        flights.clear();
        landing.clear();
        pickupSlot = -1;
    }

    public void finishAll(AbstractContainerMenu menu) {
        for (Map.Entry<Integer, ItemStack> entry : landing.entrySet()) {
            menu.getSlot(entry.getKey()).set(entry.getValue());
        }
        flights.clear();
        landing.clear();
    }

    // called after the menu got the new state, oldItems/oldCarried is what was shown before
    public void plan(AbstractContainerMenu menu, List<ItemStack> oldItems, ItemStack oldCarried, ContainerState state, boolean mouseShown, float now) {
        List<ItemStack> newItems = state.items();
        int count = Math.min(Math.min(oldItems.size(), newItems.size()), menu.slots.size());

        List<Change> losses = new ArrayList<>();
        List<Change> gains = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            diff(i, oldItems.get(i), newItems.get(i), losses, gains);
        }

        List<Change> carriedLosses = new ArrayList<>();
        List<Change> carriedGains = new ArrayList<>();
        diff(-1, oldCarried, state.carried(), carriedLosses, carriedGains);

        // the cursor already shows moves that go through it
        if (mouseShown && (!carriedLosses.isEmpty() || !carriedGains.isEmpty())) return;

        int carriedFrom = pickupSlot;
        if (!carriedGains.isEmpty()) {
            pickupSlot = -1;
            ItemStack picked = carriedGains.getFirst().stack();
            for (Change loss : losses) {
                if (ItemStack.isSameItemSameComponents(loss.stack(), picked)) {
                    pickupSlot = loss.slot();
                    break;
                }
            }
        } else if (state.carried().isEmpty()) {
            pickupSlot = -1;
        }

        List<Flight> planned = new ArrayList<>();
        int[] left = losses.stream().mapToInt(Change::count).toArray();

        for (Change gain : gains) {
            int needed = gain.count();

            for (int i = 0; i < losses.size() && needed > 0; i++) {
                Change loss = losses.get(i);
                if (left[i] == 0 || loss.slot() == gain.slot()) continue;
                if (!ItemStack.isSameItemSameComponents(loss.stack(), gain.stack())) continue;

                int moved = Math.min(needed, left[i]);
                left[i] -= moved;
                needed -= moved;
                planned.add(new Flight(loss.slot(), gain.slot(), gain.stack().copyWithCount(moved), now));
            }

            // the rest came from the cursor, fly it from where it was picked up
            if (needed > 0 && carriedFrom >= 0 && carriedFrom < count && carriedFrom != gain.slot()) {
                for (Change loss : carriedLosses) {
                    if (ItemStack.isSameItemSameComponents(loss.stack(), gain.stack())) {
                        planned.add(new Flight(carriedFrom, gain.slot(), gain.stack().copyWithCount(needed), now));
                        break;
                    }
                }
            }
        }

        if (planned.isEmpty() || planned.size() > MAX_FLIGHTS) return;

        for (Flight flight : planned) {
            int to = flight.to();
            if (landing.containsKey(to)) continue;
            landing.put(to, newItems.get(to).copy());
            menu.getSlot(to).set(oldItems.get(to).copy());
        }
        flights.addAll(planned);
    }

    private static void diff(int slot, ItemStack before, ItemStack after, List<Change> losses, List<Change> gains) {
        if (ItemStack.isSameItemSameComponents(before, after)) {
            int delta = after.getCount() - before.getCount();
            if (delta < 0) losses.add(new Change(slot, before, -delta));
            else if (delta > 0) gains.add(new Change(slot, after, delta));
            return;
        }
        if (!before.isEmpty()) losses.add(new Change(slot, before, before.getCount()));
        if (!after.isEmpty()) gains.add(new Change(slot, after, after.getCount()));
    }

    // pose must already be moved to the window corner
    public void extract(GuiGraphicsExtractor graphics, Font font, AbstractContainerMenu menu, float now) {
        Iterator<Flight> it = flights.iterator();
        while (it.hasNext()) {
            Flight flight = it.next();
            float progress = (now - flight.startTime()) / DURATION_TICKS;
            if (progress < 0 || progress >= 1) {
                it.remove();
                continue;
            }

            float eased = 1 - (1 - progress) * (1 - progress) * (1 - progress);
            Slot from = menu.getSlot(flight.from());
            Slot to = menu.getSlot(flight.to());
            int x = Math.round(from.x + (to.x - from.x) * eased);
            int y = Math.round(from.y + (to.y - from.y) * eased);

            graphics.item(flight.stack(), x, y);
            graphics.itemDecorations(font, flight.stack(), x, y);
        }

        landing.entrySet().removeIf(entry -> {
            int slot = entry.getKey();
            for (Flight flight : flights) {
                if (flight.to() == slot) return false;
            }
            menu.getSlot(slot).set(entry.getValue());
            return true;
        });
    }
}
