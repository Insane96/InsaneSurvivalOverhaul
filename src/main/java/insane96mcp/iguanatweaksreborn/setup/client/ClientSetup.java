package insane96mcp.iguanatweaksreborn.setup.client;

import insane96mcp.iguanatweaksreborn.module.combat.bows.Bows;
import insane96mcp.iguanatweaksreborn.module.combat.bows.ShortbowItem;
import insane96mcp.iguanatweaksreborn.module.experience.enchantments.EnchantmentsFeature;
import insane96mcp.iguanatweaksreborn.module.farming.bonemeal.BoneMeal;
import insane96mcp.iguanatweaksreborn.module.farming.crops.Crops;
import insane96mcp.iguanatweaksreborn.module.hungerhealth.fooddrinks.FoodDrinks;
import insane96mcp.iguanatweaksreborn.module.items.copper.CopperEquipment;
import insane96mcp.iguanatweaksreborn.module.items.flintexpansion.FlintExpansion;
import insane96mcp.iguanatweaksreborn.module.items.repairkit.RepairKits;
import insane96mcp.iguanatweaksreborn.module.misc.beaconconduit.BeaconConduit;
import insane96mcp.iguanatweaksreborn.module.misc.beaconconduit.ISOBeaconRenderer;
import insane96mcp.iguanatweaksreborn.module.misc.beaconconduit.ISOBeaconScreen;
import insane96mcp.iguanatweaksreborn.module.mobs.spawning.Spawning;
import insane96mcp.iguanatweaksreborn.module.movement.minecarts.Minecarts;
import insane96mcp.iguanatweaksreborn.module.sleeprespawn.Cloth;
import insane96mcp.iguanatweaksreborn.module.sleeprespawn.death.Death;
import insane96mcp.iguanatweaksreborn.module.sleeprespawn.respawn.Respawn;
import insane96mcp.iguanatweaksreborn.module.world.BiomeCompass;
import insane96mcp.iguanatweaksreborn.module.world.CyanFlower;
import insane96mcp.iguanatweaksreborn.module.world.coalfire.CoalFire;
import insane96mcp.iguanatweaksreborn.setup.ISORegistries;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.shieldsplus.setup.SPItems;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.FallingBlockRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.function.Supplier;

public class ClientSetup {
    public static void onBuildCreativeModeTabContents(final BuildCreativeModeTabContentsEvent event)
    {
        if (FlintExpansion.areStoneToolsDisabled())
        {
            event.getEntries().remove(new ItemStack(Items.STONE_SWORD));
            event.getEntries().remove(new ItemStack(Items.STONE_AXE));
            event.getEntries().remove(new ItemStack(Items.STONE_SHOVEL));
            event.getEntries().remove(new ItemStack(Items.STONE_PICKAXE));
            event.getEntries().remove(new ItemStack(Items.STONE_HOE));

            //if (ModList.get().isLoaded("shieldsplus"))
                //event.getEntries().remove(new ItemStack(SPItems.STONE_SHIELD.get()));
        }
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            if (Feature.isEnabled(BeaconConduit.class)) {
                addAfter(event, Items.BEACON, BeaconConduit.BEACON.item());
                event.getEntries().remove(new ItemStack(Items.BEACON));
            }
            if (Feature.isEnabled(Spawning.class)) {
                addAfter(event, Items.SOUL_TORCH, Spawning.ECHO_LANTERN.item());
            }
            if (Feature.isEnabled(Cloth.class)) {
                addBefore(event, Items.RESPAWN_ANCHOR, Respawn.RESPAWN_OBELISK.item().get());
            }
        }
        else if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            if (Feature.isEnabled(Death.class)) {
                addAfter(event, Items.CHAIN, Death.GRAVE.item());
            }
            if (Feature.isEnabled(CoalFire.class)) {
                addAfter(event, Items.COAL_BLOCK, CoalFire.CHARCOAL_LAYER.item().get());
            }
        }
        else if (event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) {
            if (Feature.isEnabled(CyanFlower.class)) {
                addAfter(event, Items.POPPY, CyanFlower.FLOWER.item());
            }
            if (Feature.isEnabled(Crops.class)) {
                addAfter(event, Items.POPPY, Crops.SOLANUM_NEOROSSII.item());
                addAfter(event, Items.WHEAT_SEEDS, Crops.CARROT_SEEDS);
                addAfter(event, Items.BEETROOT_SEEDS, Crops.ROOTED_POTATO);
            }
            if (ModList.get().isLoaded("farmersdelight")) {
                addAfter(event, Crops.ROOTED_POTATO.get(), Crops.RICE_SEEDS);
                addAfter(event, Crops.ROOTED_POTATO.get(), Crops.ROOTED_ONION);
            }
            if (Feature.isEnabled(BoneMeal.class)) {
                addAfter(event, Items.FARMLAND, BoneMeal.RICH_FARMLAND.item());
            }
            if (Feature.isEnabled(CoalFire.class)) {
                addAfter(event, Items.NETHER_GOLD_ORE, CoalFire.SOUL_SOIL_HELLISH_COAL_ORE.item().get());
                addAfter(event, Items.NETHER_GOLD_ORE, CoalFire.SOUL_SAND_HELLISH_COAL_ORE.item().get());
            }
            if (Feature.isEnabled(FlintExpansion.class) && !FlintExpansion.dontEnableFlintTools)
                addAfter(event, Items.CACTUS, FlintExpansion.FLINT_ROCK.item().get());
        }
        else if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            if (Feature.isEnabled(CopperEquipment.class)) {
                addAfter(event, Items.WOODEN_HOE, CopperEquipment.HOE.get());
                addAfter(event, Items.WOODEN_HOE, CopperEquipment.AXE.get());
                addAfter(event, Items.WOODEN_HOE, CopperEquipment.PICKAXE.get());
                addAfter(event, Items.WOODEN_HOE, CopperEquipment.SHOVEL.get());
            }
            if (Feature.isEnabled(FlintExpansion.class) && !FlintExpansion.dontEnableFlintTools) {
                addAfter(event, Items.WOODEN_HOE, FlintExpansion.HOE.get());
                addAfter(event, Items.WOODEN_HOE, FlintExpansion.AXE.get());
                addAfter(event, Items.WOODEN_HOE, FlintExpansion.PICKAXE.get());
                addAfter(event, Items.WOODEN_HOE, FlintExpansion.SHOVEL.get());
            }

            if (Feature.isEnabled(BiomeCompass.class)) {
                addAfter(event, Items.CLOCK, BiomeCompass.COMPASS);
            }
            if (Feature.isEnabled(CoalFire.class)) {
                addBefore(event, Items.FLINT_AND_STEEL, CoalFire.FIRESTARTER.get());
            }

            if (Feature.isEnabled(Minecarts.class)) {
                addAfter(event, Items.RAIL, Minecarts.GOLDEN_POWERED_RAIL.item().get());
                addAfter(event, Items.RAIL, Minecarts.COPPER_POWERED_RAIL.item().get());
            }
        }
        else if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            if (Feature.isEnabled(CopperEquipment.class)) {
                addAfter(event, Items.WOODEN_SWORD, CopperEquipment.SWORD.get());
                addAfter(event, Items.WOODEN_AXE, CopperEquipment.AXE.get());
                if (ModList.get().isLoaded("shieldsplus"))
                    addAfter(event, SPItems.WOODEN_SHIELD.get(), CopperEquipment.ShieldsPlusIntegration.SHIELD.get());
                addAfter(event, Items.LEATHER_BOOTS, CopperEquipment.BOOTS.get());
                addAfter(event, Items.LEATHER_BOOTS, CopperEquipment.LEGGINGS.get());
                addAfter(event, Items.LEATHER_BOOTS, CopperEquipment.CHESTPLATE.get());
                addAfter(event, Items.LEATHER_BOOTS, CopperEquipment.HELMET.get());
            }
            if (Feature.isEnabled(FlintExpansion.class) && !FlintExpansion.dontEnableFlintTools) {
                addAfter(event, Items.WOODEN_SWORD, FlintExpansion.SWORD.get());
                addAfter(event, Items.WOODEN_AXE, FlintExpansion.AXE.get());

                if (ModList.get().isLoaded("shieldsplus"))
                    addAfter(event, SPItems.WOODEN_SHIELD.get(), FlintExpansion.ShieldsPlusIntegration.SHIELD.get());
            }

            if (Feature.isEnabled(Bows.class)) {
                addAfter(event, Items.BOW, Bows.SHORTBOW.get());
            }
        }
        else if (event.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS) {
            if (Feature.isEnabled(Minecarts.class)) {
                addAfter(event, Items.RAIL, Minecarts.GOLDEN_POWERED_RAIL.item().get());
                addAfter(event, Items.RAIL, Minecarts.COPPER_POWERED_RAIL.item().get());
            }
        }
        else if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            if (Feature.isEnabled(EnchantmentsFeature.class)) {
                if (EnchantmentsFeature.replaceProtectionEnchantments) {
                    removeBookWithEnchantment(event, Enchantments.ALL_DAMAGE_PROTECTION);
                    removeBookWithEnchantment(event, Enchantments.FIRE_PROTECTION);
                    removeBookWithEnchantment(event, Enchantments.FALL_PROTECTION);
                    removeBookWithEnchantment(event, Enchantments.PROJECTILE_PROTECTION);
                    removeBookWithEnchantment(event, Enchantments.BLAST_PROTECTION);
                }
                if (EnchantmentsFeature.replaceDamagingEnchantments) {
                    removeBookWithEnchantment(event, Enchantments.SHARPNESS);
                    removeBookWithEnchantment(event, Enchantments.SMITE);
                    removeBookWithEnchantment(event, Enchantments.BANE_OF_ARTHROPODS);
                }
                if (EnchantmentsFeature.isBonusLootEnchantmentReworkEnabled()) {
                    removeBookWithEnchantment(event, Enchantments.FISHING_LUCK);
                    removeBookWithEnchantment(event, Enchantments.BLOCK_FORTUNE);
                    removeBookWithEnchantment(event, Enchantments.MOB_LOOTING);
                }
                if (EnchantmentsFeature.replaceOtherEnchantments) {
                    removeBookWithEnchantment(event, Enchantments.FIRE_ASPECT);
                    removeBookWithEnchantment(event, Enchantments.KNOCKBACK);
                }
            }
            if (Feature.isEnabled(Cloth.class)) {
                addBefore(event, Items.LEATHER, Cloth.ITEM.get());
            }
            if (Feature.isEnabled(CoalFire.class)) {
                addAfter(event, Items.CHARCOAL, CoalFire.HELLISH_COAL.get());
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
    }

    public static void addBefore(BuildCreativeModeTabContentsEvent event, Item before, ItemLike itemToAdd) {
        event.getEntries().putBefore(new ItemStack(before), new ItemStack(itemToAdd), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
    }

    public static void addAfter(BuildCreativeModeTabContentsEvent event, Item after, ItemLike itemToAdd) {
        event.getEntries().putAfter(new ItemStack(after), new ItemStack(itemToAdd), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
    }

    public static void addBefore(BuildCreativeModeTabContentsEvent event, Item before, Supplier<? extends ItemLike> itemToAdd) {
        addBefore(event, before, itemToAdd.get());
    }

    public static void addAfter(BuildCreativeModeTabContentsEvent event, Item after, Supplier<? extends ItemLike> itemToAdd) {
        addAfter(event, after, itemToAdd.get());
    }

    public static void removeBookWithEnchantment(BuildCreativeModeTabContentsEvent event, Enchantment enchantment) {
        ItemStack stack = new ItemStack(Items.ENCHANTED_BOOK);
        EnchantedBookItem.addEnchantment(stack, new EnchantmentInstance(enchantment, enchantment.getMaxLevel()));
        event.getEntries().remove(stack);
    }

    public static void remove(BuildCreativeModeTabContentsEvent event, ItemStack itemToRemove) {
        event.getEntries().remove(itemToRemove);
    }

    public static void init(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemProperties.register(Bows.SHORTBOW.get(), ResourceLocation.parse("pull"), (stack, clientLevel, livingEntity, seed) -> {
                if (livingEntity == null
                        || livingEntity.getUseItem() != stack)
                    return 0.0F;
                else
                    return (float) (stack.getUseDuration() - livingEntity.getUseItemRemainingTicks()) / (float) ShortbowItem.getFullChargeTicks();
            });
            ItemProperties.register(Bows.SHORTBOW.get(), ResourceLocation.parse("pulling"),
                    (stack, clientLevel, livingEntity, seed)
                            -> livingEntity != null && livingEntity.isUsingItem() && livingEntity.getUseItem() == stack ? 1.0F : 0.0F);
        });
        MenuScreens.register(BeaconConduit.BEACON_MENU_TYPE.get(), ISOBeaconScreen::new);
    }

    public static void registerItemColorHandlers(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> {
            if (stack.getTag() == null)
                return -1;
            return stack.getTag().getInt("color");
        }, RepairKits.ITEM.get());
    }

    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ISORegistries.PILABLE_FALLING_LAYER.get(), FallingBlockRenderer::new);
        event.registerBlockEntityRenderer(BeaconConduit.BEACON_BLOCK_ENTITY_TYPE.get(), ISOBeaconRenderer::new);
    }
}
