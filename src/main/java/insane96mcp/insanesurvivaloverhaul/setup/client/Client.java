package insane96mcp.insanesurvivaloverhaul.setup.client;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.module.combat.CriticalRework;
import insane96mcp.insanesurvivaloverhaul.module.combat.PiercingDamage;
import insane96mcp.insanesurvivaloverhaul.module.combat.bows.Bows;
import insane96mcp.insanesurvivaloverhaul.module.combat.bows.ShortbowItem;
import insane96mcp.insanesurvivaloverhaul.module.death.DeathMaxHealthPenalty;
import insane96mcp.insanesurvivaloverhaul.module.death.respawn.EchoPillar;
import insane96mcp.insanesurvivaloverhaul.module.farming.bonemeal.BoneMeal;
import insane96mcp.insanesurvivaloverhaul.module.farming.crops.Crops;
import insane96mcp.insanesurvivaloverhaul.module.hungerhealth.fooddrinks.FoodDrinks;
import insane96mcp.insanesurvivaloverhaul.module.items.StoneToolsGone;
import insane96mcp.insanesurvivaloverhaul.module.items.copper.CopperEquipment;
import insane96mcp.insanesurvivaloverhaul.module.items.pouch.ClientPouchTooltip;
import insane96mcp.insanesurvivaloverhaul.module.items.pouch.Pouch;
import insane96mcp.insanesurvivaloverhaul.module.items.pouch.PouchTooltip;
import insane96mcp.insanesurvivaloverhaul.module.mining.beegoreveins.BigOreVeins;
import insane96mcp.insanesurvivaloverhaul.module.misc.CreativeRemoval;
import insane96mcp.insanesurvivaloverhaul.module.mobs.spawning.Spawning;
import insane96mcp.insanesurvivaloverhaul.module.movement.minecarts.Minecarts;
import insane96mcp.insanesurvivaloverhaul.module.sleep.Cloth;
import insane96mcp.insanesurvivaloverhaul.module.world.CyanFlower;
import insane96mcp.insanesurvivaloverhaul.module.world.coalfire.CoalFire;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.GatherSkippedAttributeTooltipsEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.common.util.AttributeUtil;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

import static insane96mcp.insanelib.util.CreativeTabsUtils.*;

public class Client {

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
                addBefore(event, Items.RESPAWN_ANCHOR, EchoPillar.ECHO_PILLAR.item());
            }
        }
        else if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            if (Feature.isEnabled(CopperEquipment.class)) {
                addAfter(event, Items.WOODEN_HOE, CopperEquipment.HOE);
                addAfter(event, Items.WOODEN_HOE, CopperEquipment.AXE);
                addAfter(event, Items.WOODEN_HOE, CopperEquipment.PICKAXE);
                addAfter(event, Items.WOODEN_HOE, CopperEquipment.SHOVEL);
            }
            if (Feature.isEnabled(CoalFire.class)) {
                addBefore(event, Items.FLINT_AND_STEEL, CoalFire.FIRESTARTER);
            }
            if (Feature.isEnabled(Minecarts.class)) {
                addAfter(event, Items.RAIL, Minecarts.GOLDEN_POWERED_RAIL.item());
                addAfter(event, Items.RAIL, Minecarts.COPPER_POWERED_RAIL.item());
                remove(event, Items.POWERED_RAIL);
            }
            if (Feature.isEnabled(Pouch.class)) {
                addAfter(event, Items.LEAD, Pouch.ITEM);
            }
            if (Feature.isEnabled(Cloth.class)
                    && !event.getParameters().enabledFeatures().contains(FeatureFlags.BUNDLE))
                    addAfter(event, Items.LEAD, Items.BUNDLE);
            if (Feature.isEnabled(DeathMaxHealthPenalty.class)) {
                addAfter(event, Items.ELYTRA, DeathMaxHealthPenalty.CRYSTAL_HEART);
            }
        }
        else if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            if (Feature.isEnabled(CopperEquipment.class)) {
                addAfter(event, Items.WOODEN_SWORD, CopperEquipment.SWORD);
                addAfter(event, Items.WOODEN_AXE, CopperEquipment.AXE);
                addAfter(event, Items.LEATHER_BOOTS, CopperEquipment.BOOTS);
                addAfter(event, Items.LEATHER_BOOTS, CopperEquipment.LEGGINGS);
                addAfter(event, Items.LEATHER_BOOTS, CopperEquipment.CHESTPLATE);
                addAfter(event, Items.LEATHER_BOOTS, CopperEquipment.HELMET);
            }
            if (Feature.isEnabled(Bows.class)) {
                addAfter(event, Items.BOW, Bows.SHORTBOW);
            }
        }
        else if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            if (Feature.isEnabled(CoalFire.class)) {
                addAfter(event, Items.COAL_BLOCK, CoalFire.BURNT_LOG.item());
            }
        }
        else if (event.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS) {
            if (Feature.isEnabled(Minecarts.class)) {
                addAfter(event, Items.RAIL, Minecarts.GOLDEN_POWERED_RAIL.item());
                addAfter(event, Items.RAIL, Minecarts.COPPER_POWERED_RAIL.item());
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
            if (Feature.isEnabled(BigOreVeins.class)) {
                addAfter(event, Items.IRON_ORE, BigOreVeins.IRON_ORE_ROCK.item());
                addAfter(event, Items.GOLD_ORE, BigOreVeins.GOLD_ORE_ROCK.item());
                addAfter(event, Items.COPPER_ORE, BigOreVeins.COPPER_ORE_ROCK.item());
            }
        }
        else if (event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS) {
            if (Feature.isEnabled(FoodDrinks.class)) {
                addAfter(event, Items.MUSHROOM_STEW, FoodDrinks.FUNGI_STEW);
                addAfter(event, Items.COOKIE, FoodDrinks.OVER_EASY_EGG);
                addBefore(event, Items.PUMPKIN_PIE, FoodDrinks.PUMPKIN_PULP);
                addAfter(event, Items.PUMPKIN_PIE, FoodDrinks.APPLE_PIE);
                addAfter(event, Items.COOKED_COD, FoodDrinks.COD_CHOWDER);
                addAfter(event, Items.COOKED_COD, FoodDrinks.PUFFERFISH_CHOWDER);
            }
        }
        else if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            if (Feature.isEnabled(Cloth.class)) {
                addBefore(event, Items.LEATHER, Cloth.ITEM);
            }
            if (Feature.isEnabled(CopperEquipment.class)) {
                addAfter(event, Items.GOLD_NUGGET, CopperEquipment.NUGGET);
            }
        }
        BuiltInRegistries.ITEM.getTag(CreativeRemoval.ITEM_TAG).ifPresent(holders -> {
            for (Holder<Item> holder : holders) {
                remove(event, holder.value());
            }
        });
    }

    public static void init(FMLClientSetupEvent event) {
        /*event.enqueueWork(() ->
                ItemProperties.register(Altimeter.ITEM, InsaneSO.location("y"), (stack, clientLevel, livingEntity, entityId) -> {
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
        //MenuScreens.register(BeaconConduit.BEACON_MENU_TYPE, ISOBeaconScreen::new);
        //MenuScreens.register(Fletching.FLETCHING_MENU_TYPE, FletchingScreen::new);
    }

    public static void registerTooltips(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(PouchTooltip.class, ClientPouchTooltip::new);
    }

    public static void onGatherSkippedAttributeTooltips(GatherSkippedAttributeTooltipsEvent event) {
        if (isUnchangedFromDefault(event, CriticalRework.CHANCE_ATTRIBUTE)) {
            event.skipId(CriticalRework.BASE_CHANCE_ATTRIBUTE_ID);
            // Critical Damage is meaningless without any Critical Chance, so hide it too regardless of its own value.
            event.skipId(CriticalRework.BASE_DAMAGE_ATTRIBUTE_ID);
        }
        if (isUnchangedFromDefault(event, PiercingDamage.PIERCING_DAMAGE))
            event.skipId(PiercingDamage.BASE_PIERCING_DAMAGE_ID);
    }

    /**
     * Checks whether the item's and enchantments' bonuses for {@code attribute} merge into the same value
     * as the attribute's own default (i.e. nothing is actually being changed).
     */
    private static boolean isUnchangedFromDefault(GatherSkippedAttributeTooltipsEvent event, Holder<Attribute> attribute) {
        double defaultValue = attribute.value().getDefaultValue();
        double amt = defaultValue;
        for (AttributeModifier modifier : AttributeUtil.getSortedModifiers(event.getStack(), EquipmentSlotGroup.MAINHAND).get(attribute)) {
            switch (modifier.operation()) {
                case ADD_VALUE -> amt += modifier.amount();
                case ADD_MULTIPLIED_BASE -> amt += modifier.amount() * defaultValue;
                case ADD_MULTIPLIED_TOTAL -> amt *= 1 + modifier.amount();
            }
        }
        return amt == defaultValue;
    }
}
