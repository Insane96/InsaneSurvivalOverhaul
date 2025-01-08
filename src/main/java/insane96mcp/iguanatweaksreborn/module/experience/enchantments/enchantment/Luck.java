package insane96mcp.iguanatweaksreborn.module.experience.enchantments.enchantment;

import insane96mcp.iguanatweaksreborn.data.generator.ISOItemTagsProvider;
import insane96mcp.iguanatweaksreborn.module.experience.enchantments.EnchantmentsFeature;
import insane96mcp.insanelib.base.Feature;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.LootBonusEnchantment;

import java.util.Set;

public class Luck extends LootBonusEnchantment {
    public static final TagKey<Item> ACCEPTS_ENCHANTMENT = ISOItemTagsProvider.create("enchanting/accepts_luck");
    static final EnchantmentCategory CATEGORY = EnchantmentCategory.create("luck", item -> item.builtInRegistryHolder().is(ACCEPTS_ENCHANTMENT));
    public Luck() {
        super(Rarity.RARE, CATEGORY, EquipmentSlot.MAINHAND);
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return super.canApplyAtEnchantingTable(stack) && EnchantmentsFeature.replaceBonusLootEnchantments && Feature.isEnabled(EnchantmentsFeature.class);
    }

    @Override
    public boolean isDiscoverable() {
        return super.isDiscoverable() && EnchantmentsFeature.replaceBonusLootEnchantments && Feature.isEnabled(EnchantmentsFeature.class);
    }

    @Override
    public boolean allowedInCreativeTab(Item book, Set<EnchantmentCategory> allowedCategories) {
        return this.isDiscoverable();
    }
}
