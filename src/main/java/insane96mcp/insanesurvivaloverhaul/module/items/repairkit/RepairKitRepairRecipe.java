package insane96mcp.insanesurvivaloverhaul.module.items.repairkit;

import insane96mcp.insanelib.data.ObjTag;
import insane96mcp.insanesurvivaloverhaul.setup.ISORegistries;
import insane96mcp.insanesurvivaloverhaul.setup.ModIds;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
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
                String material = stack.get(ISORegistries.REPAIR_KIT_MATERIAL.get());
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

        String materialId = repairKit.get(ISORegistries.REPAIR_KIT_MATERIAL.get());
        if (materialId == null)
            return ItemStack.EMPTY;
        ObjTag<Item> material = ObjTag.of(materialId, Registries.ITEM);

        // Copy every component of the item being repaired (enchantments, custom name, other mods' data, ...) and
        // only ever touch its damage below, so nothing about the item is lost through the repair kit.
        ItemStack resultStack = repairableItem.copy();

        boolean experienceTweaksLoaded = ModList.get().isLoaded(ModIds.EXPERIENCE_TWEAKS);
        // The material can be a tag (e.g. one kit valid for iron, gold and diamond tools): try every item it
        // covers and use the first one this specific item is repairable with, either vanilla or via Experience Tweaks.
        // Experience Tweaks' custom data is checked first so its per-material overrides (amount, max repair, cost
        // multiplier) still apply even when the candidate is also a valid vanilla repair item.
        Optional<ExperienceTweaksIntegration.RepairData> oRepairData = Optional.empty();
        boolean vanillaValid = false;
        for (Item candidate : material.getAllObjects()) {
            ItemStack candidateStack = new ItemStack(candidate);
            if (experienceTweaksLoaded) {
                oRepairData = ExperienceTweaksIntegration.getCustomRepairData(repairableItem, candidateStack);
                if (oRepairData.isPresent())
                    break;
            }
            if (resultStack.getItem().isValidRepairItem(resultStack, candidateStack)) {
                vanillaValid = true;
                break;
            }
        }
        if (!vanillaValid && oRepairData.isEmpty())
            return ItemStack.EMPTY;

        int materialRatio = repairKit.getOrDefault(ISORegistries.REPAIR_KIT_AMOUNT.get(), RepairKits.repairKitMaterialRatio);
        double kitMaxRepair = repairKit.getOrDefault(ISORegistries.REPAIR_KIT_MAX_REPAIR.get(), RepairKits.maxRepair);
        int repairCount = materialRatio * kitAmount;
        int repairItemCountCost;
        int maxPartialRepairDmg = Mth.ceil(resultStack.getMaxDamage() * (1f - kitMaxRepair));
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
