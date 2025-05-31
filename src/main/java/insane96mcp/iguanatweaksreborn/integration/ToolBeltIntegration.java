package insane96mcp.iguanatweaksreborn.integration;

import dev.gigaherz.toolbelt.ToolBelt;
import dev.gigaherz.toolbelt.belt.ToolBeltItem;
import dev.gigaherz.toolbelt.slot.BeltExtensionSlot;
import insane96mcp.iguanatweaksreborn.data.generator.ISOItemTagsProvider;
import insane96mcp.iguanatweaksreborn.module.sleeprespawn.death.GraveBlockEntity;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.common.util.LazyOptional;

import java.util.List;

public class ToolBeltIntegration {
    public static final TagKey<Item> TOOL_BELT_TICKABLE = ISOItemTagsProvider.create("toolbelt_tickable");
    public static void onDeath(List<GraveBlockEntity.SlottedStack> items, Player player) {
        LazyOptional<BeltExtensionSlot> oBeltExtensionSlot = BeltExtensionSlot.get(player);
        oBeltExtensionSlot.ifPresent(beltExtensionSlot -> {
            beltExtensionSlot.getSlots().forEach(extensionSlot -> {
                if (!extensionSlot.getContents().isEmpty())
                    items.add(new GraveBlockEntity.SlottedStack(extensionSlot.getContents()));
                extensionSlot.setContents(ItemStack.EMPTY);
            });
        });
    }

    //This is a workaround as ToolBelt only ticks items with ExtensionSlotItemCapability
    public static void tryTickItemsIn(LivingEntity entity) {
        LazyOptional<BeltExtensionSlot> oBeltExtensionSlot = BeltExtensionSlot.get(entity);
        oBeltExtensionSlot.ifPresent(beltExtensionSlot -> {
            beltExtensionSlot.getSlots().forEach(extensionSlot -> {
                ItemStack belt = extensionSlot.getContents();
                if (belt.is(ToolBelt.BELT.get())) {
                    belt.getCapability(ToolBeltItem.ITEM_HANDLER).ifPresent(cap -> {
                        int slots = cap.getSlots();
                        for (int i = 0; i < slots; i++) {
                            if (cap.getStackInSlot(i).is(TOOL_BELT_TICKABLE))
                                cap.getStackInSlot(i).inventoryTick(entity.level(), entity, -1, false);
                        }
                    });
                }
            });
        });
    }

    public static void removeCursedItemsOnDeath(LivingEntity entity) {
        LazyOptional<BeltExtensionSlot> oBeltExtensionSlot = BeltExtensionSlot.get(entity);
        oBeltExtensionSlot.ifPresent(beltExtensionSlot -> {
            beltExtensionSlot.getSlots().forEach(extensionSlot -> {
                ItemStack belt = extensionSlot.getContents();
                if (belt.is(ToolBelt.BELT.get())) {
                    belt.getCapability(ToolBeltItem.ITEM_HANDLER).ifPresent(cap -> {
                        int slots = cap.getSlots();
                        for (int i = 0; i < slots; i++) {
                            if (EnchantmentHelper.hasVanishingCurse(cap.getStackInSlot(i)))
                                cap.extractItem(i, 1, false);
                        }
                    });
                }
            });
        });
    }
}
