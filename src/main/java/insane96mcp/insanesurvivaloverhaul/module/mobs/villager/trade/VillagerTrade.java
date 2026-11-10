package insane96mcp.insanesurvivaloverhaul.module.mobs.villager.trade;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * A data driven trade, with the fields and semantics of the 26.1 {@code villager_trade} format.
 * Number providers are evaluated with a loot context where the merchant is {@code this} entity and its position is the origin.
 * <p>
 * Differences from 26.1: 1.21.1 has no {@code additional_trade_cost} component, so {@code double_trade_price_enchantments} doubles the whole {@code wants} count.
 */
public record VillagerTrade(TradeCost wants, Optional<TradeCost> additionalWants, ItemStack gives, List<LootItemFunction> givenItemModifiers,
                            NumberProvider maxUses, NumberProvider reputationDiscount, NumberProvider xp,
                            Optional<LootItemCondition> merchantPredicate, Optional<HolderSet<Enchantment>> doubleTradePriceEnchantments) implements VillagerTrades.ItemListing {
    public static final Codec<VillagerTrade> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TradeCost.CODEC.fieldOf("wants").forGetter(VillagerTrade::wants),
            TradeCost.CODEC.optionalFieldOf("additional_wants").forGetter(VillagerTrade::additionalWants),
            ItemStack.CODEC.fieldOf("gives").forGetter(VillagerTrade::gives),
            LootItemFunctions.ROOT_CODEC.listOf().optionalFieldOf("given_item_modifiers", List.of()).forGetter(VillagerTrade::givenItemModifiers),
            NumberProviders.CODEC.optionalFieldOf("max_uses", ConstantValue.exactly(4)).forGetter(VillagerTrade::maxUses),
            NumberProviders.CODEC.optionalFieldOf("reputation_discount", ConstantValue.exactly(0)).forGetter(VillagerTrade::reputationDiscount),
            NumberProviders.CODEC.optionalFieldOf("xp", ConstantValue.exactly(1)).forGetter(VillagerTrade::xp),
            LootItemCondition.DIRECT_CODEC.optionalFieldOf("merchant_predicate").forGetter(VillagerTrade::merchantPredicate),
            RegistryCodecs.homogeneousList(Registries.ENCHANTMENT).optionalFieldOf("double_trade_price_enchantments").forGetter(VillagerTrade::doubleTradePriceEnchantments)
    ).apply(instance, VillagerTrade::new));

    @Nullable
    @Override
    public MerchantOffer getOffer(Entity trader, RandomSource random) {
        if (!(trader.level() instanceof ServerLevel level))
            return null;
        LootContext context = createContext(level, trader, random);
        if (this.merchantPredicate.isPresent() && !this.merchantPredicate.get().test(context))
            return null;

        ItemStack result = this.gives.copy();
        for (LootItemFunction function : this.givenItemModifiers)
            result = function.apply(result, context);
        if (result.isEmpty())
            return null;

        int wantsCount = this.wants.rollCount(context);
        if (this.doubleTradePriceEnchantments.isPresent() && hasAnyStoredEnchantment(result, this.doubleTradePriceEnchantments.get()))
            wantsCount *= 2;
        ItemCost costA = this.wants.toItemCost(wantsCount);
        if (costA == null)
            return null;
        Optional<ItemCost> costB = Optional.empty();
        if (this.additionalWants.isPresent()) {
            ItemCost itemCost = this.additionalWants.get().toItemCost(this.additionalWants.get().rollCount(context));
            if (itemCost == null)
                return null;
            costB = Optional.of(itemCost);
        }

        int maxUses = Math.max(1, this.maxUses.getInt(context));
        int xp = Math.max(0, this.xp.getInt(context));
        float priceMultiplier = Math.max(0f, this.reputationDiscount.getFloat(context));
        return new MerchantOffer(costA, costB, result, maxUses, xp, priceMultiplier);
    }

    private static boolean hasAnyStoredEnchantment(ItemStack stack, HolderSet<Enchantment> enchantments) {
        ItemEnchantments stored = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        for (var enchantment : stored.keySet()) {
            if (enchantments.contains(enchantment))
                return true;
        }
        return false;
    }

    /**
     * Loot context used by trades: the merchant is {@code this} entity and its position is the origin.
     */
    public static LootContext createContext(ServerLevel level, Entity trader, RandomSource random) {
        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.THIS_ENTITY, trader)
                .withParameter(LootContextParams.ORIGIN, trader.position())
                .create(LootContextParamSets.ADVANCEMENT_ENTITY);
        return new LootContext.Builder(params).withOptionalRandomSource(random).create(Optional.empty());
    }
}
