package insane96mcp.iguanatweaksreborn.module.experience.enchantments.enchantment;

import insane96mcp.iguanatweaksreborn.data.generator.ISOItemTagsProvider;
import insane96mcp.iguanatweaksreborn.module.experience.enchantments.EnchantmentsFeature;
import insane96mcp.insanelib.base.Feature;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

import java.util.Set;

public class Knockback extends Enchantment {
    public static TagKey<Item> ACCEPTS_ENCHANTMENT = ISOItemTagsProvider.create("enchanting/accepts_knockback");
    public static EnchantmentCategory CATEGORY = EnchantmentCategory.create("accepts_knockback", item -> item.builtInRegistryHolder().is(ACCEPTS_ENCHANTMENT));
    public Knockback() {
        super(Rarity.UNCOMMON, CATEGORY, new EquipmentSlot[] { EquipmentSlot.MAINHAND });
    }

    public int getMinCost(int pEnchantmentLevel) {
        return 5 + 20 * (pEnchantmentLevel - 1);
    }

    public int getMaxCost(int pEnchantmentLevel) {
        return super.getMinCost(pEnchantmentLevel) + 50;
    }

    public int getMaxLevel() {
        return 2;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return super.canApplyAtEnchantingTable(stack) && EnchantmentsFeature.replaceOtherEnchantments && Feature.isEnabled(EnchantmentsFeature.class);
    }

    @Override
    public boolean allowedInCreativeTab(Item book, Set<EnchantmentCategory> allowedCategories) {
        return this.isDiscoverable();
    }

    @Override
    public boolean isDiscoverable() {
        return super.isDiscoverable() && EnchantmentsFeature.replaceOtherEnchantments && Feature.isEnabled(EnchantmentsFeature.class);
    }

    public static float getKnockback(LivingEntity living) {
        return living.getMainHandItem().getEnchantmentLevel(EnchantmentsFeature.KNOCKBACK.get()) * 0.4f;
    }
}
