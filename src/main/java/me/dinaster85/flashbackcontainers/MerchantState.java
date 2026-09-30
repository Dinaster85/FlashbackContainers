package me.dinaster85.flashbackcontainers;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

// Villager trades. The server sends them in their own packet, which Flashback doesn't record.
// selected and scroll are the clicked trade and the list position on the screen.
public record MerchantState(MerchantOffers offers, int xp, int level, boolean showProgress, boolean canRestock, int selected, int scroll) {

    public boolean same(MerchantState other) {
        if (other == null) return false;
        if (xp != other.xp || level != other.level || showProgress != other.showProgress || canRestock != other.canRestock) return false;
        if (selected != other.selected || scroll != other.scroll || offers.size() != other.offers.size()) return false;

        for (int i = 0; i < offers.size(); i++) {
            if (!sameOffer(offers.get(i), other.offers.get(i))) return false;
        }
        return true;
    }

    private static boolean sameOffer(MerchantOffer a, MerchantOffer b) {
        return ItemStack.matches(a.getCostA(), b.getCostA())
            && ItemStack.matches(a.getCostB(), b.getCostB())
            && ItemStack.matches(a.getResult(), b.getResult())
            && a.isOutOfStock() == b.isOutOfStock()
            && a.getUses() == b.getUses()
            && a.getMaxUses() == b.getMaxUses()
            && a.getXp() == b.getXp()
            && a.getSpecialPriceDiff() == b.getSpecialPriceDiff()
            && a.getPriceMultiplier() == b.getPriceMultiplier()
            && a.getDemand() == b.getDemand();
    }

    public static void encode(RegistryFriendlyByteBuf buf, MerchantState state) {
        MerchantOffers.STREAM_CODEC.encode(buf, state.offers);
        buf.writeVarInt(state.xp);
        buf.writeVarInt(state.level);
        buf.writeBoolean(state.showProgress);
        buf.writeBoolean(state.canRestock);
        buf.writeVarInt(state.selected);
        buf.writeVarInt(state.scroll);
    }

    public static MerchantState decode(RegistryFriendlyByteBuf buf) {
        MerchantOffers offers = MerchantOffers.STREAM_CODEC.decode(buf);
        int xp = buf.readVarInt();
        int level = buf.readVarInt();
        boolean showProgress = buf.readBoolean();
        boolean canRestock = buf.readBoolean();
        int selected = buf.readVarInt();
        int scroll = buf.readVarInt();
        return new MerchantState(offers, xp, level, showProgress, canRestock, selected, scroll);
    }
}
