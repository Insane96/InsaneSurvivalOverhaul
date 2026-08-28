package insane96mcp.insanesurvivaloverhaul.mixin.module.misc.tweaks;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.module.misc.tweaks.Tweaks;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RepairItemRecipe;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(RepairItemRecipe.class)
public class RepairItemRecipeMixin_Tweaks {

    /**
     * Allows matching more than 2 identical, repairable items in the crafting grid, up to
     * {@link Tweaks#repairMergeMaxItems}, instead of vanilla's hardcoded limit of exactly 2.
     */
    @Inject(method = "matches", at = @At("HEAD"), cancellable = true)
    private void insanesurvivaloverhaul$matches(CraftingContainer pInv, Level pLevel, CallbackInfoReturnable<Boolean> cir) {
        if (!Feature.isEnabled(Tweaks.class) || Tweaks.repairMergeMaxItems <= 2)
            return;

        List<ItemStack> list = new ArrayList<>();
        for (int i = 0; i < pInv.getContainerSize(); i++) {
            ItemStack stack = pInv.getItem(i);
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
    private void insanesurvivaloverhaul$assemble(CraftingContainer pContainer, RegistryAccess pRegistryAccess, CallbackInfoReturnable<ItemStack> cir) {
        if (!Feature.isEnabled(Tweaks.class) || Tweaks.repairMergeMaxItems <= 2)
            return;

        List<ItemStack> list = new ArrayList<>();
        for (int i = 0; i < pContainer.getContainerSize(); i++) {
            ItemStack stack = pContainer.getItem(i);
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

        List<Map<Enchantment, Integer>> enchantMaps = new ArrayList<>();
        for (ItemStack stack : list) {
            enchantMaps.add(EnchantmentHelper.getEnchantments(stack));
        }

        Map<Enchantment, Integer> curses = new HashMap<>();
        BuiltInRegistries.ENCHANTMENT.stream().filter(Enchantment::isCurse).forEach(enchantment -> {
            int max = 0;
            for (Map<Enchantment, Integer> enchantMap : enchantMaps) {
                max = Math.max(max, enchantMap.getOrDefault(enchantment, 0));
            }
            if (max > 0)
                curses.put(enchantment, max);
        });
        if (!curses.isEmpty())
            EnchantmentHelper.setEnchantments(curses, result);

        cir.setReturnValue(result);
    }
}
