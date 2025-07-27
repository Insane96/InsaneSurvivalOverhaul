package insane96mcp.iguanatweaksreborn.module.experience.enchantments.enchantment;

import insane96mcp.iguanatweaksreborn.data.generator.ISOItemTagsProvider;
import insane96mcp.iguanatweaksreborn.module.experience.enchantments.EnchantmentsFeature;
import insane96mcp.insanelib.base.Feature;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class FireAspect extends Enchantment {
    public static final TagKey<Item> ACCEPTS_ENCHANTMENT = ISOItemTagsProvider.create("enchanting/accepts_fire_aspect");
    static final EnchantmentCategory CATEGORY = EnchantmentCategory.create("fire_aspect", item -> item.builtInRegistryHolder().is(ACCEPTS_ENCHANTMENT));
    public FireAspect() {
        super(Rarity.RARE, CATEGORY, new EquipmentSlot[] { EquipmentSlot.MAINHAND });
    }

    public int getMinCost(int pEnchantmentLevel) {
        return 10 + 20 * (pEnchantmentLevel - 1);
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
    public boolean isDiscoverable() {
        return super.isDiscoverable() && EnchantmentsFeature.replaceOtherEnchantments && Feature.isEnabled(EnchantmentsFeature.class);
    }

    @Override
    public boolean allowedInCreativeTab(Item book, Set<EnchantmentCategory> allowedCategories) {
        return this.isDiscoverable();
    }

    @Override
    public void doPostAttack(@NotNull LivingEntity attacker, @NotNull Entity target, int lvl) {
        int ticks = this.secondsOnFirePerLevel() * lvl * 20 + 10;
        if (attacker instanceof Player player) {
            float f = player.getAttackStrengthScale(0.5f);
            ticks = (int) (ticks * f * f);
        }
        if (target instanceof LivingEntity)
            ticks = ProtectionEnchantment.getFireAfterDampener((LivingEntity) target, ticks);

        if (target.getRemainingFireTicks() < ticks)
            target.setRemainingFireTicks(ticks);
    }

    public int secondsOnFirePerLevel() {
        return 3;
    }
}
