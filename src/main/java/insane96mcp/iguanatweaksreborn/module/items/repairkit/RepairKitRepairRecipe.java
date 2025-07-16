package insane96mcp.iguanatweaksreborn.module.items.repairkit;

import insane96mcp.iguanatweaksreborn.module.experience.anvils.AnvilRepair;
import insane96mcp.iguanatweaksreborn.module.experience.anvils.Anvils;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Optional;

public class RepairKitRepairRecipe extends CustomRecipe {
    public RepairKitRepairRecipe(ResourceLocation pId, CraftingBookCategory pCategory) {
        super(pId, pCategory);
    }

    @Override
    public boolean matches(CraftingContainer container, Level pLevel) {
        ItemStack repairableItem = null;
        ItemStack repairKit = null;
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack itemStack = container.getItem(i);
            if (itemStack.isEmpty())
                continue;
            if (itemStack.isDamageableItem() && itemStack.isDamaged()) {
                //Don't go further if there's more than 1 repairable item
                if (repairableItem != null)
                    return false;
                repairableItem = itemStack;
            }
            if (itemStack.getItem().equals(RepairKits.ITEM.get())) {
                if (repairKit != null
                        && (itemStack.getTag() == null
                                || !repairKit.getOrCreateTag().getString("repair_material").equals(itemStack.getTag().getString("repair_material"))))
                    return false;
                repairKit = itemStack;
            }
        }

        return repairableItem != null && repairKit != null;
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess pRegistryAccess) {
        ItemStack repairableItem = null;
        ItemStack repairKit = null;
        int kitAmount = 0;
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack itemStack = container.getItem(i);
            if (itemStack.isEmpty())
                continue;
            if (itemStack.isDamageableItem())
                repairableItem = itemStack;
            if (itemStack.getItem().equals(RepairKits.ITEM.get())) {
                if (repairKit == null)
                    repairKit = itemStack;
                kitAmount++;
            }
        }
        if (repairableItem != null && repairKit != null) {
            Item repairItem = ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(repairKit.getOrCreateTag().getString("repair_material")));
            if (repairItem == null)
                return ItemStack.EMPTY;
            ItemStack repairItemStack = new ItemStack(repairItem);

            Optional<AnvilRepair.RepairData> oRepairData = Anvils.getCustomAnvilRepair(repairableItem, repairItemStack);

            ItemStack resultStack = repairableItem.copy();
            if (!resultStack.getItem().isValidRepairItem(resultStack, repairItemStack) && oRepairData.isEmpty())
                return ItemStack.EMPTY;
            int repairCount = RepairKits.repairKitMaterialRatio * kitAmount;
            int repairItemCountCost;
            int maxPartialRepairDmg = Mth.ceil(resultStack.getMaxDamage() * (1f - RepairKits.maxRepair));
            float amountRequired = 4;
            if (oRepairData.isPresent()) {
                AnvilRepair.RepairData repairData = oRepairData.get();
                maxPartialRepairDmg = Math.max(maxPartialRepairDmg, Mth.ceil(resultStack.getMaxDamage() * (1f - repairData.maxRepair())));
                amountRequired = repairData.amountRequired();
            }
            if (Anvils.materialCost$increaseMaterialsRequiredWithEnchantments > 0f && repairableItem.isEnchanted()) {
                float increase = 0f;
                for (Integer lvl : EnchantmentHelper.getEnchantments(repairableItem).values()) {
                    increase += Anvils.materialCost$increaseMaterialsRequiredWithEnchantments.floatValue() * lvl;
                }
                amountRequired *= 1 + increase;
            }
            if (Anvils.materialCost$increaseMaterialsRequiredWithEnchantmentsFlat > 0f && repairableItem.isEnchanted()) {
                float increase = 0f;
                for (Integer lvl : EnchantmentHelper.getEnchantments(repairableItem).values()) {
                    increase += Anvils.materialCost$increaseMaterialsRequiredWithEnchantmentsFlat.floatValue() * lvl;
                }
                amountRequired += (increase * oRepairData.get().costMultiplier());
            }
            float repairSteps = Math.min(resultStack.getDamageValue(), resultStack.getMaxDamage() / amountRequired);
            if (repairSteps <= 0 || resultStack.getDamageValue() <= maxPartialRepairDmg)
                return ItemStack.EMPTY;

            float damageValue = resultStack.getDamageValue();
            for (repairItemCountCost = 0; repairSteps > 0 && repairItemCountCost < repairCount && damageValue > maxPartialRepairDmg; ++repairItemCountCost) {
                damageValue -= repairSteps;
                repairSteps = Math.min(damageValue, resultStack.getMaxDamage() / amountRequired);
            }
            resultStack.setDamageValue((int) Math.max(maxPartialRepairDmg, damageValue));

            return resultStack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return pWidth * pHeight >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RepairKits.REPAIR_RECIPE_SERIALIZER.get();
    }
}
