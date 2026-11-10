package insane96mcp.insanesurvivaloverhaul.module.mobs.villager.trade;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

import javax.annotation.Nullable;

/**
 * The price of a trade (the 26.1 {@code wants} / {@code additional_wants}): an item, a count provider and the exact components the item must have.
 */
public record TradeCost(Holder<Item> item, NumberProvider count, DataComponentPredicate components) {
    public static final Codec<TradeCost> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.ITEM_NON_AIR_CODEC.fieldOf("id").forGetter(TradeCost::item),
            NumberProviders.CODEC.optionalFieldOf("count", ConstantValue.exactly(1)).forGetter(TradeCost::count),
            DataComponentPredicate.CODEC.optionalFieldOf("components", DataComponentPredicate.EMPTY).forGetter(TradeCost::components)
    ).apply(instance, TradeCost::new));

    public int rollCount(LootContext context) {
        return this.count.getInt(context);
    }

    /**
     * Builds the {@link ItemCost} with the given count, capped at the item's max stack size.
     * Returns null if the count is below 1, meaning the trade must not be offered.
     */
    @Nullable
    public ItemCost toItemCost(int count) {
        if (count < 1)
            return null;
        ItemCost itemCost = new ItemCost(this.item, count, this.components);
        int maxStackSize = itemCost.itemStack().getMaxStackSize();
        if (count > maxStackSize)
            itemCost = new ItemCost(this.item, maxStackSize, this.components);
        return itemCost;
    }
}
