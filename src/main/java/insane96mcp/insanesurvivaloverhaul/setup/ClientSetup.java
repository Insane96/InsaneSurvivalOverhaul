package insane96mcp.insanesurvivaloverhaul.setup;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.module.combat.bows.Bows;
import insane96mcp.insanesurvivaloverhaul.module.combat.bows.ShortbowItem;
import insane96mcp.insanesurvivaloverhaul.module.death.respawn.EchoPillar;
import insane96mcp.insanesurvivaloverhaul.module.farming.bonemeal.BoneMeal;
import insane96mcp.insanesurvivaloverhaul.module.farming.crops.Crops;
import insane96mcp.insanesurvivaloverhaul.module.hungerhealth.fooddrinks.FoodDrinks;
import insane96mcp.insanesurvivaloverhaul.module.items.StoneToolsGone;
import insane96mcp.insanesurvivaloverhaul.module.items.copper.CopperEquipment;
import insane96mcp.insanesurvivaloverhaul.module.items.pouch.ClientPouchTooltip;
import insane96mcp.insanesurvivaloverhaul.module.items.pouch.Pouch;
import insane96mcp.insanesurvivaloverhaul.module.items.pouch.PouchTooltip;
import insane96mcp.insanesurvivaloverhaul.module.mobs.spawning.Spawning;
import insane96mcp.insanesurvivaloverhaul.module.movement.minecarts.Minecarts;
import insane96mcp.insanesurvivaloverhaul.module.sleep.Cloth;
import insane96mcp.insanesurvivaloverhaul.module.world.CyanFlower;
import insane96mcp.insanesurvivaloverhaul.module.world.coalfire.CoalFire;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ItemLike;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

import java.util.function.Supplier;

public class ClientSetup {

    public static void onBuildCreativeModeTabContents(final BuildCreativeModeTabContentsEvent event) {
        if (Feature.isEnabled(StoneToolsGone.class))
        {
            remove(event, Items.STONE_SWORD);
            remove(event, Items.STONE_AXE);
            remove(event, Items.STONE_SHOVEL);
            remove(event, Items.STONE_PICKAXE);
            remove(event, Items.STONE_HOE);
        }
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            if (Feature.isEnabled(Spawning.class)) {
                addAfter(event, Items.SOUL_TORCH, Spawning.ECHO_LANTERN.item());
            }
            if (Feature.isEnabled(EchoPillar.class)) {
                addBefore(event, Items.RESPAWN_ANCHOR, EchoPillar.ECHO_PILLAR.item().get());
            }
        }
        else if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            if (Feature.isEnabled(CopperEquipment.class)) {
                addAfter(event, Items.WOODEN_HOE, CopperEquipment.HOE.get());
                addAfter(event, Items.WOODEN_HOE, CopperEquipment.AXE.get());
                addAfter(event, Items.WOODEN_HOE, CopperEquipment.PICKAXE.get());
                addAfter(event, Items.WOODEN_HOE, CopperEquipment.SHOVEL.get());
            }
            if (Feature.isEnabled(CoalFire.class)) {
                addBefore(event, Items.FLINT_AND_STEEL, CoalFire.FIRESTARTER.get());
            }
            if (Feature.isEnabled(Minecarts.class)) {
                addAfter(event, Items.RAIL, Minecarts.GOLDEN_POWERED_RAIL.item().get());
                addAfter(event, Items.RAIL, Minecarts.COPPER_POWERED_RAIL.item().get());
                remove(event, Items.POWERED_RAIL);
            }
            if (Feature.isEnabled(Pouch.class)) {
                addAfter(event, Items.LEAD, Pouch.ITEM.get());
            }
            if (Feature.isEnabled(Cloth.class)
                    && !event.getParameters().enabledFeatures().contains(FeatureFlags.BUNDLE))
                    addAfter(event, Items.LEAD, Items.BUNDLE);
        }
        else if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            if (Feature.isEnabled(CopperEquipment.class)) {
                addAfter(event, Items.WOODEN_SWORD, CopperEquipment.SWORD.get());
                addAfter(event, Items.WOODEN_AXE, CopperEquipment.AXE.get());
                addAfter(event, Items.LEATHER_BOOTS, CopperEquipment.BOOTS.get());
                addAfter(event, Items.LEATHER_BOOTS, CopperEquipment.LEGGINGS.get());
                addAfter(event, Items.LEATHER_BOOTS, CopperEquipment.CHESTPLATE.get());
                addAfter(event, Items.LEATHER_BOOTS, CopperEquipment.HELMET.get());
            }
            if (Feature.isEnabled(Bows.class)) {
                addAfter(event, Items.BOW, Bows.SHORTBOW.get());
            }
        }
        else if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            if (Feature.isEnabled(CoalFire.class)) {
                addAfter(event, Items.COAL_BLOCK, CoalFire.BURNT_LOG.item().get());
            }
        }
        else if (event.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS) {
            if (Feature.isEnabled(Minecarts.class)) {
                addAfter(event, Items.RAIL, Minecarts.GOLDEN_POWERED_RAIL.item().get());
                addAfter(event, Items.RAIL, Minecarts.COPPER_POWERED_RAIL.item().get());
                remove(event, Items.POWERED_RAIL);
            }
        }
        else if (event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) {
            if (Feature.isEnabled(Crops.class)) {
                addAfter(event, Items.POPPY, Crops.SOLANUM_NEOROSSII.item());
                addAfter(event, Items.WHEAT_SEEDS, Crops.CARROT_SEEDS);
                addAfter(event, Items.BEETROOT_SEEDS, Crops.ROOTED_POTATO);
            }
            if (Feature.isEnabled(BoneMeal.class)) {
                addAfter(event, Items.FARMLAND, BoneMeal.RICH_FARMLAND.item());
            }
            if (Feature.isEnabled(CyanFlower.class)) {
                addAfter(event, Items.POPPY, CyanFlower.FLOWER.item());
            }
        }
        else if (event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS) {
            if (Feature.isEnabled(FoodDrinks.class)) {
                addAfter(event, Items.MUSHROOM_STEW, FoodDrinks.NETHERIZED_STEW.get());
                addAfter(event, Items.MUSHROOM_STEW, FoodDrinks.BROWN_MUSHROOM_STEW.get());
                addAfter(event, Items.MUSHROOM_STEW, FoodDrinks.RED_MUSHROOM_STEW.get());
                addAfter(event, Items.COOKIE, FoodDrinks.OVER_EASY_EGG.get());
                addBefore(event, Items.PUMPKIN_PIE, FoodDrinks.PUMPKIN_PULP.get());
            }
        }
        else if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            if (Feature.isEnabled(Cloth.class)) {
                addBefore(event, Items.LEATHER, Cloth.ITEM.get());
            }
        }
    }

    public static void addBefore(BuildCreativeModeTabContentsEvent event, Item before, ItemLike itemToAdd) {
        event.insertBefore(new ItemStack(before), new ItemStack(itemToAdd), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
    }

    public static void addAfter(BuildCreativeModeTabContentsEvent event, Item after, ItemLike itemToAdd) {
        addAfter(event, after, new ItemStack(itemToAdd));
    }

    public static void addAfter(BuildCreativeModeTabContentsEvent event, Item after, ItemStack stackToAdd) {
        event.insertAfter(new ItemStack(after), stackToAdd, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
    }

    public static void addBefore(BuildCreativeModeTabContentsEvent event, Item before, Supplier<? extends ItemLike> itemToAdd) {
        addBefore(event, before, itemToAdd.get());
    }

    public static void addAfter(BuildCreativeModeTabContentsEvent event, Item after, Supplier<? extends ItemLike> itemToAdd) {
        addAfter(event, after, itemToAdd.get());
    }

    public static void remove(BuildCreativeModeTabContentsEvent event, Item itemToRemove) {
        event.remove(new ItemStack(itemToRemove), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
    }

    public static void init(FMLClientSetupEvent event) {
        /*event.enqueueWork(() ->
                ItemProperties.register(Altimeter.ITEM.get(), InsaneSO.location("y"), (stack, clientLevel, livingEntity, entityId) -> {
                    if (livingEntity == null)
                        return 96f;
                    return (float) livingEntity.getY();
                }));*/
        event.enqueueWork(() -> {
            ItemProperties.register(Bows.SHORTBOW.get(), ResourceLocation.parse("pull"), (stack, clientLevel, livingEntity, seed) -> {
                if (livingEntity == null
                        || livingEntity.getUseItem() != stack)
                    return 0.0F;
                else
                    return (float) (stack.getUseDuration(livingEntity) - livingEntity.getUseItemRemainingTicks()) / (float) ShortbowItem.getFullChargeTicks();
            });
            ItemProperties.register(Bows.SHORTBOW.get(), ResourceLocation.parse("pulling"),
                    (stack, clientLevel, livingEntity, seed)
                            -> livingEntity != null && livingEntity.isUsingItem() && livingEntity.getUseItem() == stack ? 1.0F : 0.0F);
        });
        //MenuScreens.register(BeaconConduit.BEACON_MENU_TYPE.get(), ISOBeaconScreen::new);
        //MenuScreens.register(Fletching.FLETCHING_MENU_TYPE.get(), FletchingScreen::new);
    }

    public static void registerTooltips(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(PouchTooltip.class, ClientPouchTooltip::new);
    }
}
