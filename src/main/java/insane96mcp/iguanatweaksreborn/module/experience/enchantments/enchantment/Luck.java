package insane96mcp.iguanatweaksreborn.module.experience.enchantments.enchantment;

import insane96mcp.iguanatweaksreborn.data.generator.ISOItemTagsProvider;
import insane96mcp.iguanatweaksreborn.module.experience.enchantments.EnchantmentsFeature;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.LootBonusEnchantment;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class Luck extends LootBonusEnchantment {
    public static final TagKey<Item> ACCEPTS_ENCHANTMENT = ISOItemTagsProvider.create("enchanting/accepts_luck");
    static final EnchantmentCategory CATEGORY = EnchantmentCategory.create("luck", item -> item.builtInRegistryHolder().is(ACCEPTS_ENCHANTMENT));
    public Luck() {
        super(Rarity.VERY_RARE, CATEGORY, EquipmentSlot.MAINHAND);
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public int getMinCost(int pEnchantmentLevel) {
        return super.getMinCost(2);
    }

    @Override
    public int getMaxCost(int pEnchantmentLevel) {
        return super.getMaxCost(2);
    }

    @Override
    public boolean canApplyAtEnchantingTable(@NotNull ItemStack stack) {
        return super.canApplyAtEnchantingTable(stack) && EnchantmentsFeature.isBonusLootEnchantmentReworkEnabled();
    }

    @Override
    public boolean isDiscoverable() {
        return super.isDiscoverable() && EnchantmentsFeature.isBonusLootEnchantmentReworkEnabled();
    }

    @Override
    public boolean allowedInCreativeTab(Item book, Set<EnchantmentCategory> allowedCategories) {
        return this.isDiscoverable();
    }
}
