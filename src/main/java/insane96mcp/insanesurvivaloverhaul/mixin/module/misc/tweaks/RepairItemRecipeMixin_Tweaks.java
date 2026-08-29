package insane96mcp.insanesurvivaloverhaul.mixin.module.misc.tweaks;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.module.misc.tweaks.Tweaks;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RepairItemRecipe;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(RepairItemRecipe.class)
public class RepairItemRecipeMixin_Tweaks {

    /**
     * Allows matching more than 2 identical, repairable items in the crafting grid, up to
     * {@link Tweaks#repairMergeMaxItems}, instead of vanilla's hardcoded limit of exactly 2.
     */
    @Inject(method = "matches", at = @At("HEAD"), cancellable = true)
    private void insanesurvivaloverhaul$matches(CraftingInput pInput, Level pLevel, CallbackInfoReturnable<Boolean> cir) {
        if (!Feature.isEnabled(Tweaks.class) || Tweaks.repairMergeMaxItems <= 2)
            return;

        List<ItemStack> list = new ArrayList<>();
        for (int i = 0; i < pInput.size(); i++) {
            ItemStack stack = pInput.getItem(i);
            if (stack.isEmpty())
                continue;

            list.add(stack);
            if (list.size() <= 1)
                continue;

            ItemStack first = list.get(0);
            if (stack.getItem() != first.getItem() || first.getCount() != 1 || stack.getCount() != 1 || !first.isRepairable()) {
                cir.setReturnValue(false);
                return;
            }
        }

        cir.setReturnValue(list.size() >= 2 && list.size() <= Tweaks.repairMergeMaxItems);
    }

    /**
     * Merges the durability (and curse enchantments) of every matched item instead of just the
     * first 2, mirroring vanilla's formula extended to N items.
     * @see #insanesurvivaloverhaul$matches
     */
    @Inject(method = "assemble", at = @At("HEAD"), cancellable = true)
    private void insanesurvivaloverhaul$assemble(CraftingInput pInput, HolderLookup.Provider pRegistries, CallbackInfoReturnable<ItemStack> cir) {
        if (!Feature.isEnabled(Tweaks.class) || Tweaks.repairMergeMaxItems <= 2)
            return;

        List<ItemStack> list = new ArrayList<>();
        for (int i = 0; i < pInput.size(); i++) {
            ItemStack stack = pInput.getItem(i);
            if (stack.isEmpty())
                continue;

            list.add(stack);
            if (list.size() <= 1)
                continue;

            ItemStack first = list.get(0);
            if (stack.getItem() != first.getItem() || first.getCount() != 1 || stack.getCount() != 1 || !first.isRepairable()) {
                cir.setReturnValue(ItemStack.EMPTY);
                return;
            }
        }

        if (list.size() < 2 || list.size() > Tweaks.repairMergeMaxItems)
            return;

        ItemStack first = list.get(0);
        int maxDamage = first.getMaxDamage();
        int totalRemaining = maxDamage * 5 / 100;
        for (ItemStack stack : list) {
            totalRemaining += maxDamage - stack.getDamageValue();
        }
        int newDamage = Math.max(0, maxDamage - totalRemaining);

        ItemStack result = new ItemStack(first.getItem());
        result.setDamageValue(newDamage);

        List<ItemEnchantments> enchantMaps = new ArrayList<>();
        for (ItemStack stack : list) {
            enchantMaps.add(EnchantmentHelper.getEnchantmentsForCrafting(stack));
        }

        ItemEnchantments.Mutable curses = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        for (ItemEnchantments enchantments : enchantMaps) {
            for (Holder<Enchantment> enchantment : enchantments.keySet()) {
                if (enchantment.is(EnchantmentTags.CURSE))
                    curses.upgrade(enchantment, enchantments.getLevel(enchantment));
            }
        }
        ItemEnchantments curseEnchantments = curses.toImmutable();
        if (!curseEnchantments.isEmpty())
            EnchantmentHelper.setEnchantments(result, curseEnchantments);

        cir.setReturnValue(result);
    }
}
