package insane96mcp.iguanatweaksreborn.module.items;

import insane96mcp.iguanatweaksreborn.InsaneSO;
import insane96mcp.iguanatweaksreborn.data.generator.ISOItemTagsProvider;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.module.misc.DataPacks;
import insane96mcp.iguanatweaksreborn.network.message.StackSizesSync;
import insane96mcp.iguanatweaksreborn.utils.MCUtils;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.JsonFeature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

@LoadFeature(module = Modules.Ids.ITEMS, description = "Make food, items and blocks less stackable. Items and Blocks are disabled by default. Changing stuff requires a /reload, but might require a Minecraft restart.")
public class StackSizes extends Feature {
    public static final TagKey<Item> NO_STACK_SIZE_CHANGES = ISOItemTagsProvider.create("no_stack_size_changes");

    @Config(description = "The formula to calculate the stack size of a food item. Variables as hunger, saturation_modifier, effectiveness as numbers and fast_food as boolean can be used. Set to empty to disable this. This is evaluated with EvalEx https://ezylang.github.io/EvalEx/concepts/parsing_evaluation.html.")
    public static String foodStackReductionFormula = "ROUND(MAX(64 / MAX(hunger, 1) * 0.4, 1), 0)";
    @Config(min = 0.01d, max = 64d, name = "Item Stack Multiplier", description = "Items max stack sizes (excluding blocks) will be multiplied by this value. Foods will be overridden by 'Food Stack Reduction' or 'Food Stack Multiplier' if are active. Setting to 1 will disable this feature.")
    public static Double itemStackMultiplier = 1d;
    @Config(min = 0.01d, max = 64d, description = "All the blocks max stack sizes will be multiplied by this value to increase / decrease them.")
    public static Double blockStackMultiplier = 1.0d;

    @Config(description = "Enables a data pack that changes some item stacks.")
    public static Boolean dataPack = true;

	public StackSizes(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
        InsaneSO.addServerPack("item_stacks", "Insane's Survival Overhaul Item Stacks", () -> this.isEnabled() && !DataPacks.disableAllDataPacks && dataPack);
    }

    public static void processStackSizes(boolean isClientSide) {
        synchronized (mutex) {
            resetStackSizes();
            processItemStackSizes(isClientSide);
            processBlockStackSizes(isClientSide);
            processFoodStackSizes(isClientSide);
        }
    }

    private static final Object mutex = new Object();

    static HashMap<Item, Integer> originalStackSizes = new HashMap<>();
    public static void resetStackSizes() {
        if (originalStackSizes.isEmpty()) {
            for (Item item : ForgeRegistries.ITEMS.getValues()) {
                originalStackSizes.put(item, item.maxStackSize);
            }
        }
        else {
            for (Map.Entry<Item, Integer> entry : originalStackSizes.entrySet()) {
                entry.getKey().maxStackSize = entry.getValue();
            }
        }
    }

    //Items
    public static void processItemStackSizes(boolean isClientSide) {
        if (itemStackMultiplier == 1d)
            return;

        for (Map.Entry<Item, Integer> entry : originalStackSizes.entrySet()) {
            Item item = entry.getKey();
            if (item instanceof BlockItem
                    || item.maxStackSize == 1
                    || JsonFeature.isItemInTag(item, NO_STACK_SIZE_CHANGES, isClientSide))
                continue;

            double stackSize = entry.getValue() * itemStackMultiplier;
            stackSize = Mth.clamp(stackSize, 1, 64);
            item.maxStackSize = (int) Math.round(stackSize);
        }
    }

    //Blocks
    public static void processBlockStackSizes(boolean isClientSide) {
        if (blockStackMultiplier == 1d)
            return;

        for (Map.Entry<Item, Integer> entry : originalStackSizes.entrySet()) {
            Item item = entry.getKey();
            if (!(item instanceof BlockItem)
                    || item.maxStackSize == 1
                    || JsonFeature.isItemInTag(item, NO_STACK_SIZE_CHANGES, isClientSide))
                continue;

            double stackSize = entry.getValue() * blockStackMultiplier;
            stackSize = Mth.clamp(stackSize, 1, 64);
            item.maxStackSize = (int) Math.round(stackSize);
        }

    }

    //Food
    @SuppressWarnings("deprecation")
    public static void processFoodStackSizes(boolean isClientSide) {
        if (foodStackReductionFormula.isEmpty())
            return;

        for (Map.Entry<Item, Integer> entry : originalStackSizes.entrySet()) {
            Item item = entry.getKey();
            if (item.getFoodProperties() == null
                    || JsonFeature.isItemInTag(item, NO_STACK_SIZE_CHANGES, isClientSide))
                continue;

            FoodProperties food = item.getFoodProperties();
            int stackSize = (int) MCUtils.computeFoodFormula(food, foodStackReductionFormula);
            item.maxStackSize = Mth.clamp(stackSize, 1, 64);
        }
    }

    //Sync before json
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void syncFeatureConfig(OnDatapackSyncEvent event) {
        if (!this.isEnabled())
            return;

        if (event.getPlayer() == null)
            event.getPlayerList().getPlayers().forEach(player -> StackSizesSync.sync(foodStackReductionFormula, itemStackMultiplier, blockStackMultiplier, player));
        else
            StackSizesSync.sync(foodStackReductionFormula, itemStackMultiplier, blockStackMultiplier, event.getPlayer());
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void applyFeature(TagsUpdatedEvent event) {
        if (!this.isEnabled())
            return;
        processStackSizes(event.getUpdateCause() == TagsUpdatedEvent.UpdateCause.CLIENT_PACKET_RECEIVED);
    }

    /**
     * Fixes soups, potions, etc. consuming that don't work properly when stacked
     */
    @SubscribeEvent
    public void fixUnstackableItemsEat(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof Player player))
            return;

        ItemStack original = event.getItem();
        ItemStack result = event.getResultStack();
        if (original.getCount() <= 1
                || (result.getItem() != Items.BOWL && result.getItem() != Items.BUCKET && result.getItem() != Items.GLASS_BOTTLE))
            return;

        event.setResultStack(original.copyWithCount(original.getCount() - result.getCount()));
        if (!player.addItem(result))
            player.drop(result, true);
    }
}