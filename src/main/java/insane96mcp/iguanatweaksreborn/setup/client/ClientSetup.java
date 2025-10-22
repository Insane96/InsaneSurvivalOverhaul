package insane96mcp.iguanatweaksreborn.setup.client;

import com.google.common.collect.ImmutableList;
import insane96mcp.iguanatweaksreborn.InsaneSO;
import insane96mcp.iguanatweaksreborn.module.combat.bows.Bows;
import insane96mcp.iguanatweaksreborn.module.combat.bows.ShortbowItem;
import insane96mcp.iguanatweaksreborn.module.combat.fletching.Fletching;
import insane96mcp.iguanatweaksreborn.module.combat.fletching.client.FletchingScreen;
import insane96mcp.iguanatweaksreborn.module.combat.fletching.client.ISOArrowRenderer;
import insane96mcp.iguanatweaksreborn.module.experience.enchantments.EnchantmentsFeature;
import insane96mcp.iguanatweaksreborn.module.farming.bonemeal.BoneMeal;
import insane96mcp.iguanatweaksreborn.module.farming.crops.Crops;
import insane96mcp.iguanatweaksreborn.module.hungerhealth.fooddrinks.FoodDrinks;
import insane96mcp.iguanatweaksreborn.module.items.StoneToolsGone;
import insane96mcp.iguanatweaksreborn.module.items.altimeter.Altimeter;
import insane96mcp.iguanatweaksreborn.module.items.blinker.BlinkerFeature;
import insane96mcp.iguanatweaksreborn.module.items.copper.CopperEquipment;
import insane96mcp.iguanatweaksreborn.module.items.flintexpansion.FlintExpansion;
import insane96mcp.iguanatweaksreborn.module.items.pouch.ClientPouchTooltip;
import insane96mcp.iguanatweaksreborn.module.items.pouch.Pouch;
import insane96mcp.iguanatweaksreborn.module.items.pouch.PouchTooltip;
import insane96mcp.iguanatweaksreborn.module.items.repairkit.RepairKits;
import insane96mcp.iguanatweaksreborn.module.misc.beaconconduit.BeaconConduit;
import insane96mcp.iguanatweaksreborn.module.misc.beaconconduit.ISOBeaconRenderer;
import insane96mcp.iguanatweaksreborn.module.misc.beaconconduit.ISOBeaconScreen;
import insane96mcp.iguanatweaksreborn.module.mobs.spawning.Spawning;
import insane96mcp.iguanatweaksreborn.module.movement.minecarts.Minecarts;
import insane96mcp.iguanatweaksreborn.module.sleeprespawn.Cloth;
import insane96mcp.iguanatweaksreborn.module.sleeprespawn.death.Death;
import insane96mcp.iguanatweaksreborn.module.sleeprespawn.respawn.Respawn;
import insane96mcp.iguanatweaksreborn.module.world.CyanFlower;
import insane96mcp.iguanatweaksreborn.module.world.Sextant;
import insane96mcp.iguanatweaksreborn.module.world.coalfire.CoalFire;
import insane96mcp.iguanatweaksreborn.setup.ISORegistries;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.shieldsplus.setup.SPItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.FallingBlockRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterRecipeBookCategoriesEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.List;
import java.util.function.Supplier;

public class ClientSetup {

    static RecipeBookCategories FLETCHING_SEARCH = RecipeBookCategories.create("fletching_search", new ItemStack(Items.COMPASS));
    static RecipeBookCategories FLETCHING_MISC = RecipeBookCategories.create("fletching_misc", new ItemStack(Items.FLETCHING_TABLE));
    public static final List<RecipeBookCategories> FLETCHING_CATEGORIES = ImmutableList.of(FLETCHING_SEARCH, FLETCHING_MISC);

    public static void onBuildCreativeModeTabContents(final BuildCreativeModeTabContentsEvent event)
    {
        if (Feature.isEnabled(StoneToolsGone.class))
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
            if (Feature.isEnabled(Fletching.class)) {
                addAfter(event, Items.FLETCHING_TABLE, Fletching.FLETCHING_TABLE.item().get());
                remove(event, new ItemStack(Items.FLETCHING_TABLE));
            }
        }
        else if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            if (Feature.isEnabled(Death.class)) {
                addAfter(event, Items.CHAIN, Death.GRAVE.item());
            }
            if (Feature.isEnabled(CoalFire.class)) {
                addAfter(event, Items.COAL_BLOCK, CoalFire.BURNT_LOG.item().get());
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
            if (Feature.isEnabled(FlintExpansion.class) && FlintExpansion.enableGroundFlint)
                addAfter(event, Items.CACTUS, FlintExpansion.FLINT_ROCK.item().get());
        }
        else if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            if (Feature.isEnabled(CopperEquipment.class)) {
                addAfter(event, Items.WOODEN_HOE, CopperEquipment.HOE.get());
                addAfter(event, Items.WOODEN_HOE, CopperEquipment.AXE.get());
                addAfter(event, Items.WOODEN_HOE, CopperEquipment.PICKAXE.get());
                addAfter(event, Items.WOODEN_HOE, CopperEquipment.SHOVEL.get());
            }
            if (Feature.isEnabled(FlintExpansion.class) && FlintExpansion.enableTools) {
                addAfter(event, Items.WOODEN_HOE, FlintExpansion.HOE.get());
                addAfter(event, Items.WOODEN_HOE, FlintExpansion.AXE.get());
                addAfter(event, Items.WOODEN_HOE, FlintExpansion.PICKAXE.get());
                addAfter(event, Items.WOODEN_HOE, FlintExpansion.SHOVEL.get());
            }

            if (Feature.isEnabled(Sextant.class)) {
                addAfter(event, Items.CLOCK, Sextant.ITEM);
            }
            if (Feature.isEnabled(CoalFire.class)) {
                addBefore(event, Items.FLINT_AND_STEEL, CoalFire.FIRESTARTER.get());
            }

            if (Feature.isEnabled(Minecarts.class)) {
                addAfter(event, Items.RAIL, Minecarts.GOLDEN_POWERED_RAIL.item().get());
                addAfter(event, Items.RAIL, Minecarts.COPPER_POWERED_RAIL.item().get());
            }
            if (Feature.isEnabled(BlinkerFeature.class)) {
                addBefore(event, Items.ENDER_PEARL, BlinkerFeature.ITEM);
            }
            if (Feature.isEnabled(RepairKits.class)) {
                if (Minecraft.getInstance().level != null) {
                    for (Recipe<?> recipe : Minecraft.getInstance().level.getRecipeManager().getRecipes()) {
                        try {
                            ItemStack stack = recipe.getResultItem(null);
                            if (stack.is(RepairKits.ITEM.get()))
                                addAfter(event, Items.SHEARS, stack);
                        }
                        catch (Exception ignored) {}
                    }
                }
            }
            if (Feature.isEnabled(Pouch.class)) {
                addAfter(event, Items.LEAD, Pouch.ITEM.get());
            }
            if (Feature.isEnabled(Altimeter.class))
                addAfter(event, Items.RECOVERY_COMPASS, Altimeter.ITEM.get());
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
            if (Feature.isEnabled(FlintExpansion.class) && FlintExpansion.enableTools) {
                addAfter(event, Items.WOODEN_SWORD, FlintExpansion.SWORD.get());
                addAfter(event, Items.WOODEN_AXE, FlintExpansion.AXE.get());

                if (ModList.get().isLoaded("shieldsplus"))
                    addAfter(event, SPItems.WOODEN_SHIELD.get(), FlintExpansion.ShieldsPlusIntegration.SHIELD.get());
            }

            if (Feature.isEnabled(Bows.class)) {
                addAfter(event, Items.BOW, Bows.SHORTBOW.get());
            }
            if (Feature.isEnabled(Fletching.class)) {
                addAfter(event, Items.ARROW, Fletching.TORCH_ARROW_ITEM.get());
                addAfter(event, Items.ARROW, Fletching.EXPLOSIVE_ARROW_ITEM.get());
                addAfter(event, Items.ARROW, Fletching.DIAMOND_ARROW_ITEM.get());
                addAfter(event, Items.ARROW, Fletching.QUARTZ_ARROW_ITEM.get());
                addAfter(event, Items.ARROW, Fletching.ICE_ARROW_ITEM.get());
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
        addAfter(event, after, new ItemStack(itemToAdd));
    }

    public static void addAfter(BuildCreativeModeTabContentsEvent event, Item after, ItemStack stackToAdd) {
        event.getEntries().putAfter(new ItemStack(after), stackToAdd, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
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
        event.enqueueWork(() ->
                ItemProperties.register(Altimeter.ITEM.get(), InsaneSO.location("y"), (stack, clientLevel, livingEntity, entityId) -> {
                    if (livingEntity == null)
                        return 96f;
                    return (float) livingEntity.getY();
                }));
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
        MenuScreens.register(Fletching.FLETCHING_MENU_TYPE.get(), FletchingScreen::new);
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
        event.registerEntityRenderer(Fletching.QUARTZ_ARROW.get(), ISOArrowRenderer::new);
        event.registerEntityRenderer(Fletching.DIAMOND_ARROW.get(), ISOArrowRenderer::new);
        event.registerEntityRenderer(Fletching.EXPLOSIVE_ARROW.get(), ISOArrowRenderer::new);
        event.registerEntityRenderer(Fletching.TORCH_ARROW.get(), ISOArrowRenderer::new);
        event.registerEntityRenderer(Fletching.ICE_ARROW.get(), ISOArrowRenderer::new);
    }

    public static void registerRecipeBookCategories(RegisterRecipeBookCategoriesEvent event) {
        event.registerBookCategories(InsaneSO.FLETCHING_RECIPE_BOOK_TYPE, FLETCHING_CATEGORIES);
        event.registerAggregateCategory(FLETCHING_SEARCH, ImmutableList.of(FLETCHING_MISC));
        event.registerRecipeCategoryFinder(Fletching.FLETCHING_RECIPE_TYPE.get(), r -> FLETCHING_MISC);
    }

    public static void registerTooltips(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(PouchTooltip.class, ClientPouchTooltip::new);
    }
}
