package insane96mcp.iguanatweaksreborn.module.experience.enchantments.enchantment.damage;

import insane96mcp.iguanatweaksreborn.data.generator.ISOItemTagsProvider;
import insane96mcp.iguanatweaksreborn.module.experience.enchantments.EnchantmentsFeature;
import insane96mcp.insanelib.base.Feature;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import org.jetbrains.annotations.NotNull;

public abstract class BonusDamageEnchantment extends Enchantment {

    public static final TagKey<Item> ACCEPTS_ENCHANTMENT = ISOItemTagsProvider.create("enchanting/accepts_damage_enchantments");
    static final EnchantmentCategory CATEGORY = EnchantmentCategory.create("damage_enchantment", item -> item.builtInRegistryHolder().is(ACCEPTS_ENCHANTMENT));
    protected BonusDamageEnchantment(Rarity pRarity, EquipmentSlot[] pApplicableSlots) {
        super(pRarity, CATEGORY, pApplicableSlots);
    }

    @Override
    public int getMinCost(int lvl) {
        return 4 + (lvl - 1) * 8;
    }

    @Override
    public int getMaxCost(int lvl) {
        return this.getMinCost(lvl) + 20;
    }

    @Override
    protected boolean checkCompatibility(@NotNull Enchantment other) {
        return super.checkCompatibility(other) && !(other instanceof BonusDamageEnchantment);
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    public float getDamageBonus(LivingEntity attacker, Entity target, ItemStack stack, int lvl) {
        if (!this.isAffectedByEnchantment(target))
            return 0f;
        return this.getDamageBonus(stack, lvl);
    }

    public float getDamageBonus(ItemStack stack, int lvl) {
        return this.getDamageBonusPerLevel() * lvl * getDamageBonusRatio(stack);
    }

    public float getDamageBonusPerLevel() {
        return 1f;
    }

    public boolean isAffectedByEnchantment(Entity target) {
        return true;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return (super.canApplyAtEnchantingTable(stack) || stack.is(ACCEPTS_ENCHANTMENT)) && EnchantmentsFeature.replaceDamagingEnchantments && Feature.isEnabled(EnchantmentsFeature.class);
    }

    public static float getDamageBonusRatio(ItemStack stack) {
        if (!(stack.getItem() instanceof TieredItem))
            return 0f;
        return (float) (stack.getAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.ATTACK_DAMAGE)
                        .stream()
                        .filter(attributeModifier -> attributeModifier.getOperation() == AttributeModifier.Operation.MULTIPLY_BASE)
                        .mapToDouble(AttributeModifier::getAmount)
                        .sum() + 1f);
    }
}
