package me.dinaster85.flashbackcontainers;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// data = the menu's numeric fields (furnace flame and arrow, brewing time, enchantment costs...)
// merchant = villager trades, null for everything else
public record ContainerState(int containerId, Identifier menuType, Component title, List<ItemStack> items, ItemStack carried, int[] data, MerchantState merchant) {

    // the E inventory has no MenuType, so it gets its own id
    public static final Identifier PLAYER_INVENTORY = Identifier.fromNamespaceAndPath(FlashbackContainers.MOD_ID, "player_inventory");

    private static final byte FORMAT_VERSION = 1;
    private static final byte CLOSED = 0;
    private static final byte OPEN = 1;

    public ContainerState withData(int[] data) {
        return new ContainerState(containerId, menuType, title, items, carried, data, merchant);
    }

    public boolean sameWindow(ContainerState other) {
        return other != null
            && containerId == other.containerId
            && menuType.equals(other.menuType)
            && title.equals(other.title)
            && items.size() == other.items.size();
    }

    // items, cursor and trades: everything except the numeric fields
    public boolean sameItems(ContainerState other) {
        if (!sameWindow(other)) return false;
        if (!ItemStack.matches(carried, other.carried)) return false;
        if (merchant == null ? other.merchant != null : !merchant.same(other.merchant)) return false;

        for (int i = 0; i < items.size(); i++) {
            if (!ItemStack.matches(items.get(i), other.items.get(i))) return false;
        }
        return true;
    }

    public boolean sameContents(ContainerState other) {
        return sameItems(other) && Arrays.equals(data, other.data);
    }

    // null state = container closed
    public static void encode(RegistryFriendlyByteBuf buf, ContainerState state) {
        buf.writeByte(FORMAT_VERSION);
        if (state == null) {
            buf.writeByte(CLOSED);
            return;
        }

        buf.writeByte(OPEN);
        buf.writeVarInt(state.containerId);
        buf.writeIdentifier(state.menuType);
        ComponentSerialization.STREAM_CODEC.encode(buf, state.title);
        buf.writeVarInt(state.items.size());
        for (ItemStack stack : state.items) {
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, stack);
        }
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, state.carried);

        // Added later, always at the very end: older versions just skip the extra bytes.
        // 0.3.2: numeric fields, 0.4.0: trades
        writeData(buf, state.data);
        buf.writeBoolean(state.merchant != null);
        if (state.merchant != null) MerchantState.encode(buf, state.merchant);
    }

    public static ContainerState decode(RegistryFriendlyByteBuf buf) {
        byte version = buf.readByte();
        if (version != FORMAT_VERSION) {
            buf.skipBytes(buf.readableBytes());
            return null;
        }
        if (buf.readByte() != OPEN) return null;

        int containerId = buf.readVarInt();
        Identifier menuType = buf.readIdentifier();
        Component title = ComponentSerialization.STREAM_CODEC.decode(buf);

        int count = buf.readVarInt();
        List<ItemStack> items = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            items.add(ItemStack.OPTIONAL_STREAM_CODEC.decode(buf));
        }
        ItemStack carried = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);

        int[] data = buf.readableBytes() > 0 ? readData(buf) : new int[0];
        MerchantState merchant = buf.readableBytes() > 0 && buf.readBoolean() ? MerchantState.decode(buf) : null;

        return new ContainerState(containerId, menuType, title, List.copyOf(items), carried, data, merchant);
    }

    public static void writeData(RegistryFriendlyByteBuf buf, int[] data) {
        buf.writeVarInt(data.length);
        for (int value : data) {
            buf.writeVarInt(value);
        }
    }

    public static int[] readData(RegistryFriendlyByteBuf buf) {
        int[] data = new int[buf.readVarInt()];
        for (int i = 0; i < data.length; i++) {
            data[i] = buf.readVarInt();
        }
        return data;
    }
}
