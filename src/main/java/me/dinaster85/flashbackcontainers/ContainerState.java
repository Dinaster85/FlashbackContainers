package me.dinaster85.flashbackcontainers;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public record ContainerState(int containerId, Identifier menuType, Component title, List<ItemStack> items, ItemStack carried) {

    // the E inventory has no MenuType, so it gets its own id
    public static final Identifier PLAYER_INVENTORY = Identifier.fromNamespaceAndPath(FlashbackContainers.MOD_ID, "player_inventory");

    private static final byte FORMAT_VERSION = 1;
    private static final byte CLOSED = 0;
    private static final byte OPEN = 1;

    public boolean sameWindow(ContainerState other) {
        return other != null
            && containerId == other.containerId
            && menuType.equals(other.menuType)
            && title.equals(other.title)
            && items.size() == other.items.size();
    }

    public boolean sameContents(ContainerState other) {
        if (!sameWindow(other)) return false;
        if (!ItemStack.matches(carried, other.carried)) return false;

        for (int i = 0; i < items.size(); i++) {
            if (!ItemStack.matches(items.get(i), other.items.get(i))) return false;
        }
        return true;
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

        return new ContainerState(containerId, menuType, title, List.copyOf(items), carried);
    }
}
