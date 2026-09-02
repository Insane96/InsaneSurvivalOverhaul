package insane96mcp.insanesurvivaloverhaul.module.items.repairkit;

import insane96mcp.insanesurvivaloverhaul.setup.ISORegistries;
import insane96mcp.insanesurvivaloverhaul.setup.ModIds;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;

import java.util.Objects;
import java.util.Optional;

public class RepairKitRepairRecipe extends CustomRecipe {
    public RepairKitRepairRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        ItemStack repairableItem = null;
        ItemStack repairKit = null;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty())
                continue;

            if (stack.isDamageableItem() && stack.isDamaged()) {
                //Don't go further if there's more than 1 repairable item
                if (repairableItem != null)
                    return false;
                repairableItem = stack;
            }
            if (stack.is(RepairKits.ITEM.get())) {
                ResourceLocation material = stack.get(ISORegistries.REPAIR_KIT_MATERIAL.get());
                if (repairKit != null && !Objects.equals(repairKit.get(ISORegistries.REPAIR_KIT_MATERIAL.get()), material))
                    return false;
                repairKit = stack;
            }
        }

        return repairableItem != null && repairKit != null;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack repairableItem = null;
        ItemStack repairKit = null;
        int kitAmount = 0;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty())
                continue;

            if (stack.isDamageableItem())
                repairableItem = stack;
            if (stack.is(RepairKits.ITEM.get())) {
                if (repairKit == null)
                    repairKit = stack;
                kitAmount++;
            }
        }
        if (repairableItem == null || repairKit == null)
            return ItemStack.EMPTY;

        ResourceLocation materialId = repairKit.get(ISORegistries.REPAIR_KIT_MATERIAL.get());
        if (materialId == null)
            return ItemStack.EMPTY;
        Item repairItem = BuiltInRegistries.ITEM.get(materialId);
        ItemStack repairItemStack = new ItemStack(repairItem);

        boolean experienceTweaksLoaded = ModList.get().isLoaded(ModIds.EXPERIENCE_TWEAKS);
        Optional<ExperienceTweaksIntegration.RepairData> oRepairData = experienceTweaksLoaded
                ? ExperienceTweaksIntegration.getCustomRepairData(repairableItem, repairItemStack)
                : Optional.empty();

        // Copy every component of the item being repaired (enchantments, custom name, other mods' data, ...) and
        // only ever touch its damage below, so nothing about the item is lost through the repair kit.
        ItemStack resultStack = repairableItem.copy();
        if (!resultStack.getItem().isValidRepairItem(resultStack, repairItemStack) && oRepairData.isEmpty())
            return ItemStack.EMPTY;

        int repairCount = RepairKits.repairKitMaterialRatio * kitAmount;
        int repairItemCountCost;
        int maxPartialRepairDmg = Mth.ceil(resultStack.getMaxDamage() * (1f - RepairKits.maxRepair));
        float amountRequired = 4f;
        if (oRepairData.isPresent()) {
            ExperienceTweaksIntegration.RepairData repairData = oRepairData.get();
            maxPartialRepairDmg = Math.max(maxPartialRepairDmg, Mth.ceil(resultStack.getMaxDamage() * (1f - repairData.maxRepair())));
            amountRequired = repairData.amountRequired();
        }

        if (experienceTweaksLoaded && resultStack.isEnchanted()) {
            ItemEnchantments enchantments = resultStack.getEnchantments();
            float increaseMultiplier = ExperienceTweaksIntegration.getIncreaseMaterialsRequiredWithEnchantments();
            if (increaseMultiplier > 0f) {
                float increase = 0f;
                for (Holder<Enchantment> holder : enchantments.keySet())
                    increase += increaseMultiplier * enchantments.getLevel(holder);
                amountRequired *= 1f + increase;
            }
            float increaseFlat = ExperienceTweaksIntegration.getIncreaseMaterialsRequiredWithEnchantmentsFlat();
            if (increaseFlat > 0f && oRepairData.isPresent()) {
                float increase = 0f;
                for (Holder<Enchantment> holder : enchantments.keySet())
                    increase += increaseFlat * enchantments.getLevel(holder);
                amountRequired += increase * oRepairData.get().costMultiplier();
            }
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

    /**
     * Used to determine if this recipe can fit in a grid of the given width/height
     */
    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RepairKits.REPAIR_RECIPE_SERIALIZER.get();
    }
}
