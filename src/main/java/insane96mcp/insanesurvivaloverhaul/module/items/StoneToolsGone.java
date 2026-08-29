package insane96mcp.insanesurvivaloverhaul.module.items;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.module.misc.Packs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

import java.util.ArrayList;
import java.util.List;

import static insane96mcp.insanesurvivaloverhaul.InsaneSO.*;

@LoadFeature(module = ISOModules.ITEMS, description = "If enabled, a data pack will be enabled that disables stone tools crafting and generation in chests will be replaced with copper ones. Also makes copper ore minable with wooden pickaxe")
public class StoneToolsGone extends Feature {

    @Config
    public static List<String> itemsToReplace = List.of("minecraft:stone_axe, minecraft:copper_axe", "minecraft:stone_pickaxe, minecraft:copper_pickaxe", "minecraft:stone_shovel, minecraft:copper_shovel", "minecraft:stone_hoe, minecraft:copper_hoe", "minecraft:stone_sword, minecraft:copper_sword", "insanesurvivaloverhaul:stone_dagger, insanesurvivaloverhaul:copper_dagger");
    private static final ArrayList<ToReplace> itemsToReplaceList = new ArrayList<>();

    @Config
    public static Boolean dataPack = true;

    @Override
    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        addServerPack("disable_stone_tools", "Insane's Survival Overhaul Disable Stone Tools", () -> this.isEnabled() && !Packs.disableAllDataPacks && dataPack);
        addClientPack("disable_stone_tools_rp", "Insane's Survival Overhaul Disable Stone Tools", () -> this.isEnabled() && !Packs.disableAllDataPacks && dataPack);
    }

    @Override
    public void readConfig(ModConfigEvent event) {
        super.readConfig(event);
        itemsToReplaceList.clear();
        for (String entry : itemsToReplace) {
            String[] split = entry.trim().split("\\s*,\\s*");
            ResourceLocation itemToReplaceId = ResourceLocation.tryParse(split[0]);
            if (itemToReplaceId == null) {
                LOGGER.warn("Unable to parse item ID for Stone Tools Gone: {}", split[0]);
                continue;
            }
            Item itemToReplace = BuiltInRegistries.ITEM.get(itemToReplaceId);
            if (itemToReplace == Items.AIR && !split[0].equals("minecraft:air")) {
                LOGGER.warn("Non-existent item ID for Stone Tools Gone: {}", split[0]);
                continue;
            }

            ResourceLocation newItemId = ResourceLocation.tryParse(split[1]);
            if (newItemId == null) {
                LOGGER.warn("Unable to parse item ID for Stone Tools Gone: {}", split[1]);
                continue;
            }
            Item newItem = BuiltInRegistries.ITEM.get(newItemId);
            if (newItem == Items.AIR && !split[1].equals("minecraft:air")) {
                LOGGER.warn("Non-existent item ID for Stone Tools Gone: {}", split[1]);
                continue;
            }
            if (newItem == itemToReplace) {
                LOGGER.warn("Same items for Stone Tools Gone: {}, {}", split[0], split[1]);
                continue;
            }
            boolean keepComponents = true;
            if (split.length > 2)
                keepComponents = Boolean.parseBoolean(split[2]);
            itemsToReplaceList.add(new ToReplace(itemToReplace, newItem, keepComponents));
        }
    }

    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || !(event.getEntity() instanceof LivingEntity livingEntity))
            return;

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stackInSlot = livingEntity.getItemBySlot(slot);
            for (ToReplace entry : itemsToReplaceList) {
                if (stackInSlot.is(entry.toReplace())) {
                    ItemStack newStack = entry.keepComponents() ? stackInSlot.transmuteCopy(entry.newItem()) : new ItemStack(entry.newItem(), stackInSlot.getCount());
                    livingEntity.setItemSlot(slot, newStack);
                    break;
                }
            }
        }
    }

    record ToReplace(Item toReplace, Item newItem, boolean keepComponents) {}
}