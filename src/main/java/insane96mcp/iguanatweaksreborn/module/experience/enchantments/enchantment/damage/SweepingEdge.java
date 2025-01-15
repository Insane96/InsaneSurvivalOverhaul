package insane96mcp.iguanatweaksreborn.module.experience.enchantments.enchantment.damage;

import insane96mcp.iguanatweaksreborn.data.generator.ISOItemTagsProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class SweepingEdge extends Enchantment {

    public static final TagKey<Item> ACCEPTS_ENCHANTMENT = ISOItemTagsProvider.create("enchanting/accepts_sweeping_edge");
    static final EnchantmentCategory CATEGORY = EnchantmentCategory.create("sweeping_edge", item -> item.builtInRegistryHolder().is(ACCEPTS_ENCHANTMENT));
    public SweepingEdge(Rarity pRarity) {
        super(pRarity, CATEGORY, new EquipmentSlot[]{ EquipmentSlot.MAINHAND });
    }

    /**
     * Returns the minimal value of enchantability needed on the enchantment level passed.
     */
    public int getMinCost(int pEnchantmentLevel) {
        return 5 + (pEnchantmentLevel - 1) * 9;
    }

    public int getMaxCost(int pEnchantmentLevel) {
        return this.getMinCost(pEnchantmentLevel) + 15;
    }

    /**
     * Returns the maximum level that the enchantment can have.
     */
    public int getMaxLevel() {
        return 3;
    }

    public static float getSweepingDamageRatio(int pLevel) {
        return 1.0F - 1.0F / (float)(pLevel + 1);
    }
}
