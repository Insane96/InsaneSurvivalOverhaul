package insane96mcp.iguanatweaksreborn.module.misc;

import insane96mcp.iguanatweaksreborn.InsaneSurvivalOverhaul;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.module.farming.crops.integration.FarmersDelightIntegration;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import vectorwing.farmersdelight.common.Configuration;

@Label(name = "Data Packs & Integration", description = "Various data packs that can be enabled/disabled")
@LoadFeature(module = Modules.Ids.MISC)
public class DataPacks extends Feature {

    @Config
    @Label(name = "Disable ALL data packs", description = "If true, no integrated data pack will be loaded")
    public static Boolean disableAllDataPacks = false;

    @Config
    @Label(name = "Hardcore Torches", description = """
            Changes vanilla torch recipes.
            * Torches can be only made on Campfires early on in the game
            * With shears you can make 3 torches out of coal
            * With Fire Charges you can make them later in the game.""")
    public static Boolean hardcoreTorches = true;

    @Config
    @Label(name = "Cheaper Chains", description = "Changes vanilla chains recipe. Makes chains easily craftable with nuggets only.")
    public static Boolean cheaperChains = true;

    @Config
    @Label(name = "Misc tweaks", description = """
            Minor changes:
            * Cakes now drop when broken and not eaten
            * Dark Prismarine is made easier
            * Clay balls can be crafted from blocks
            * Leads and Sticky Pistons can also be made with Honey
            * Iron Doors can be blasted back to Iron Ingots
            * Dispensers can be made from droppers
            * Levers and glass can now be broken faster with pickaxes, cactus with hoes""")
    public static Boolean miscTweaks = true;

    @Config
    @Label(name = "Actual Redstone Components", description = "Makes redstone components require redstone in their recipe (e.g. Levers, pressure plates, etc).")
    public static Boolean actualRedstoneComponents = true;

    @Config
    @Label(name = "Copper Furnace", description = "If true a data pack will be enabled that makes furnaces require copper. Copper ingots can be obtained from raw copper on campfires.")
    public static Boolean copperFurnace = true;

    @Config
    @Label(name = "Disable Long Noses Structures", description = "If true a data pack will be enabled that disables villages and pillagers outpost generation.")
    public static Boolean disableLongNoses = true;

    @Config
    @Label(name = "Fishing Loot Changes", description = "If true a data pack will be enabled that changes fishing Loot.")
    public static Boolean fishingLootChanges = true;
    @Config
    @Label(name = "Increase End Cities", description = "If true, a data pack will be enabled that makes End Cities will be more common.")
    public static Boolean increaseEndCities = true;

    @Config
    @Label(name = "Better Structure Loot", description = "If true a data pack will be enabled that overhauls structure loot. Disables itself if iguanatweaksexpanded is present")
    public static Boolean betterStructureLoot = true;
    @Config
    @Label(name = "Less loot closer to spawn", description = "If true a data pack will be enabled that reduces loot from structures closer to spawn")
    public static Boolean lessLootCloserToSpawn = true;
    @Config
    @Label(name = "Mob loot changes", description = "Changes mobs loot and makes mobs drop reduced loot if not killed by a player")
    public static Boolean mobLootChanges = true;
    @Config
    @Label(name = "Advancements overhaul", description = """
            Overhauls vanilla advancements:
            * Merges them in a big giant advancement tree
            * Heavily increases experience reward
            * Removes some advancements (like villagers ones)""")
    public static Boolean advancements = true;
    @Config
    @Label(name = "Supplementaries integration", description = "Integrates the mod with Supplementaries. Check here for changes: https://github.com/Insane96/IguanaTweaksReborn/wiki/Farmer%27s-Delight-integration")
    public static Boolean supplementaries = true;
    @Config
    @Label(name = "Farmer's Delight integration", description = "Integrates the mod with Farmer's delight. Some config options are changed along with a data pack installed. Check here for changes: https://github.com/Insane96/IguanaTweaksReborn/wiki/Farmer%27s-Delight-integration")
    public static Boolean farmersDelight = true;
    @Config
    @Label(name = "Environmental integration", description = "Integrates the mod with Environmental. Changes animals loot to match the livestock changes")
    public static Boolean environmental = true;
    @Config
    @Label(name = "Quark integration", description = "Integrates the mod with Quark. Changes animals loot to match the livestock changes.")
    public static Boolean quark = true;
    @Config
    @Label(name = "Autumnity integration", description = "Integrates the mod with Autumnity. Changes animals loot to match the livestock changes.")
    public static Boolean autumnity = true;
    @Config
    @Label(name = "Caverns & Chasms integration", description = "Integrates the mod with Caverns & Chasms. Slightly alters ore generation and adds item stats.")
    public static Boolean cavernsAndChasms = true;
    @Config
    @Label(name = "Create integration", description = "Integrates the mod with Crate. Alters ore generation.")
    public static Boolean create = true;
    @Config
    @Label(name = "Tinkers' Construct integration", description = "Integrates the mod with Tinker's Construct. Disables vanilla items. Changes some stats and adds modifiers")
    public static Boolean tinkersConstruct = false;

    @Config
    @Label(name = "Force Reload world Data Packs", description = "When you add a new mod the game automatically sets the data pack of the mod at the bottom of all the data packs, making the data packs loaded from this mod not work. If this is set to true the enabled and disabled Data Packs of the world are reset and reloaded. WARNING: you'll lose disabled data packs!")
    public static Boolean forceReloadWorldDataPacks = false;

    public DataPacks(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
        InsaneSurvivalOverhaul.addServerPack("hardcore_torches", "Insane's Survival Overhaul Hardcore Torches", () -> this.isEnabled() && !DataPacks.disableAllDataPacks && hardcoreTorches);
        InsaneSurvivalOverhaul.addServerPack("cheaper_chains", "Insane's Survival Overhaul Cheaper Chains", () -> this.isEnabled() && !DataPacks.disableAllDataPacks && cheaperChains);
        InsaneSurvivalOverhaul.addServerPack("misc_tweaks", "Insane's Survival Overhaul Misc Tweaks", () -> this.isEnabled() && !DataPacks.disableAllDataPacks && miscTweaks);
        InsaneSurvivalOverhaul.addServerPack("actual_redstone_components", "Insane's Survival Overhaul Actual Redstone components", () -> this.isEnabled() && !DataPacks.disableAllDataPacks && actualRedstoneComponents);
        InsaneSurvivalOverhaul.addServerPack("copper_furnace", "Insane's Survival Overhaul Copper Furnace", () -> this.isEnabled() && !DataPacks.disableAllDataPacks && copperFurnace);
        InsaneSurvivalOverhaul.addServerPack("disable_long_noses", "Insane's Survival Overhaul Disable Long Noses", () -> this.isEnabled() && !DataPacks.disableAllDataPacks && disableLongNoses);
        InsaneSurvivalOverhaul.addServerPack("fishing_loot_changes", "Insane's Survival Overhaul Fishing Loot Changes", () -> this.isEnabled() && !DataPacks.disableAllDataPacks && fishingLootChanges);
        InsaneSurvivalOverhaul.addServerPack("increased_end_cities", "Insane's Survival Overhaul Increased End Cities", () -> this.isEnabled() && !DataPacks.disableAllDataPacks && increaseEndCities);
        InsaneSurvivalOverhaul.addServerPack("better_loot", "Insane's Survival Overhaul Better Loot", () -> this.isEnabled() && !DataPacks.disableAllDataPacks && betterStructureLoot && !ModList.get().isLoaded("iguanatweaksexpanded"));
        InsaneSurvivalOverhaul.addServerPack("hardcore_loot", "Insane's Survival Overhaul Less Loot Closer to Spawn", () -> this.isEnabled() && !DataPacks.disableAllDataPacks && lessLootCloserToSpawn);
        InsaneSurvivalOverhaul.addServerPack("mob_loot_changes", "Insane's Survival Overhaul Mob Loot Changes", () -> this.isEnabled() && !DataPacks.disableAllDataPacks && mobLootChanges);
        InsaneSurvivalOverhaul.addServerPack("advancements", "Insane's Survival Overhaul Advancements", () -> this.isEnabled() && !DataPacks.disableAllDataPacks && advancements);
        InsaneSurvivalOverhaul.addServerPack("supplementaries_integration", "Insane's Survival Overhaul Supplementaries Integration", () -> this.isEnabled() && !DataPacks.disableAllDataPacks && supplementaries && ModList.get().isLoaded("supplementaries"));
        InsaneSurvivalOverhaul.addServerPack("environmental_integration", "Insane's Survival Overhaul Environmental Integration", () -> this.isEnabled() && !DataPacks.disableAllDataPacks && environmental && ModList.get().isLoaded("environmental"));
        InsaneSurvivalOverhaul.addServerPack("quark_integration", "Insane's Survival Overhaul Quark Integration", () -> this.isEnabled() && !DataPacks.disableAllDataPacks && quark && ModList.get().isLoaded("quark"));
        InsaneSurvivalOverhaul.addServerPack("autumnity_integration", "Insane's Survival Overhaul Autumnity Integration", () -> this.isEnabled() && !DataPacks.disableAllDataPacks && autumnity && ModList.get().isLoaded("autumnity"));
        InsaneSurvivalOverhaul.addServerPack("farmers_delight_integration", "Insane's Survival Overhaul Farmer's Delight Integration", () -> this.isEnabled() && !DataPacks.disableAllDataPacks && farmersDelight && ModList.get().isLoaded("farmersdelight"));
        InsaneSurvivalOverhaul.addServerPack(1, "caverns_and_chasms_integration", "Insane's Survival Overhaul Caverns & Chasms Integration", () -> this.isEnabled() && !DataPacks.disableAllDataPacks && cavernsAndChasms && ModList.get().isLoaded("caverns_and_chasms"));
        InsaneSurvivalOverhaul.addServerPack(1, "create_integration", "Insane's Survival Overhaul Create Integration", () -> this.isEnabled() && !DataPacks.disableAllDataPacks && create && ModList.get().isLoaded("create"));
        InsaneSurvivalOverhaul.addServerPack(1, "tconstruct_integration", "Insane's Survival Overhaul Tinkers' Construct Integration", () -> this.isEnabled() && !DataPacks.disableAllDataPacks && tinkersConstruct);

        InsaneSurvivalOverhaul.addClientPack("assets_override", "Insane's Survival Overhaul Assets Override", () -> true);
    }

    @Override
    public void readConfig(ModConfigEvent event) {
        super.readConfig(event);
        if (this.isEnabled() && ModList.get().isLoaded("farmersdelight") && farmersDelight) {
            Configuration.ENABLE_STACKABLE_SOUP_ITEMS.set(false);
            Configuration.CHANCE_WILD_BEETROOTS.set(Integer.MAX_VALUE);
            Configuration.CHANCE_WILD_CARROTS.set(Integer.MAX_VALUE);
            Configuration.CHANCE_WILD_POTATOES.set(Integer.MAX_VALUE);
        }
    }

    @SubscribeEvent
    public void onTryRichSoilFarmland(PlayerInteractEvent.RightClickBlock event) {
        if (!ModList.get().isLoaded("farmersdelight")
                || !this.isEnabled()
                || !DataPacks.farmersDelight
                || !event.getItemStack().canPerformAction(ToolActions.HOE_TILL))
            return;

        if (FarmersDelightIntegration.preventRichSoilFarmland(event.getLevel().getBlockState(event.getHitVec().getBlockPos())))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public void onEffectApplicable(MobEffectEvent.Applicable event) {
        if (!ModList.get().isLoaded("farmersdelight")
                || !this.isEnabled()
                || !DataPacks.farmersDelight)
            return;

        FarmersDelightIntegration.onEffectApplicable(event);
    }

    public static CompoundTag forceReloadWorldDataPacks(CompoundTag levelTag) {
        if (!forceReloadWorldDataPacks)
            return levelTag;

        Feature.get(DataPacks.class).getConfigOption("Force Reload world Data Packs").set(false);
        CompoundTag dataTag = levelTag.getCompound("Data");
        dataTag.remove("DataPacks");
        return levelTag;
    }
}
