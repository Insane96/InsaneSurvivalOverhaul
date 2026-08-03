package insane96mcp.insanesurvivaloverhaul.module.misc;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;

@LoadFeature(module = ISOModules.MISC, description = "Resource and data packs")
public class Packs extends Feature {

    @Config(description = "If true, no integrated data pack will be loaded")
    public static Boolean disableAllDataPacks = false;

    @Config(description = "If true a data pack will be enabled that disables villages and pillagers outpost generation.")
    public static Boolean disableLongNosesStructures = true;
    @Config(description = "If true a data pack will be enabled that makes furnaces require copper. Copper ingots can be obtained from raw copper on campfires.")
    public static Boolean copperFurnace = true;

    @Config(description = """
            Changes vanilla torch recipes.
            * Torches can be only made on Campfires early on in the game
            * With shears you can make 4 torches out of coal
            * With Fire Charges you can make more of them later in the game without needing grass.""")
    public static Boolean hardcoreTorches = true;

    @Config(description = """
            Minor changes:
            * Cakes now drop when broken and not eaten
            * Clay balls can be crafted from blocks
            * Leads and Sticky Pistons can also be made with Honey
            * Iron Doors can be blasted back to Iron Ingots
            * Dispensers can be made from droppers
            * Levers and glass can now be broken faster with pickaxes, cactus with hoes
            * Chains are easier to craft with just nuggets
            * Wool can be crafted back to strings
            * Carpets, Bricks, Banners, Targets and Tnt recipes now outputs more
            * Tnt recipe now also accepts paper instead of sand
            * Moss carpets can be crafted back to moss blocks""")
    public static Boolean miscTweaks = true;

    @Config(description = """
            Reworks Prismarine, Dark Prismarine and Sea Lantern recipes to no longer need Prismarine Shards/Crystals:
            * Prismarine is now made with Copper and Calcite
            * Dark Prismarine is made easier, out of Prismarine blocks instead of shards
            * Sea Lantern is now made out of Prismarine and Glowstone Dust
            Guardians no longer drop Prismarine Shards/Crystals (they drop Wet Sponge and (Cooked) Cod instead), Elder Guardians roll the fishing loot table instead, and Sea Lanterns always drop themselves when broken.
            Any leftover Prismarine Shard drop from other sources is replaced with 40% as many Turtle Scutes, and any leftover Prismarine Crystals drop is replaced with 40% as many Nautilus Shells.""")
    public static Boolean prismarineRework = true;

    @Config(description = "Makes redstone components require redstone in their recipe (e.g. Levers, pressure plates, etc).")
    public static Boolean actualRedstoneComponents = true;
    @Config(description = "Fixes the drop rate of some saplings, such as Jungle and Dark oak dropping too rarely")
    public static Boolean saplingDropFix = true;
    @Config(description = "Lowers the giant mushrooms generated in a Roofed forests forests as well as removes the non-dark oak trees")
    public static Boolean darkForestVegetation = true;
    @Config(description = "If true, a data pack will be enabled that makes End Cities more common.")
    public static Boolean increaseEndCities = true;

    @Override
    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        InsaneSO.addServerPack("hardcore_torches", "Insane's Survival Overhaul Hardcore Torches", () -> this.isEnabled() && !Packs.disableAllDataPacks && hardcoreTorches);
        InsaneSO.addServerPack("misc_tweaks", "Insane's Survival Overhaul Misc Tweaks", () -> this.isEnabled() && !Packs.disableAllDataPacks && miscTweaks);
        InsaneSO.addServerPack("prismarine_rework", "Insane's Survival Overhaul Prismarine Rework", () -> this.isEnabled() && !Packs.disableAllDataPacks && prismarineRework);
        InsaneSO.addServerPack("actual_redstone_components", "Insane's Survival Overhaul Actual Redstone components", () -> this.isEnabled() && !Packs.disableAllDataPacks && actualRedstoneComponents);
        InsaneSO.addServerPack("disable_long_noses", "Insane's Survival Overhaul Disable Long Noses", () -> this.isEnabled() && !Packs.disableAllDataPacks && disableLongNosesStructures);
        InsaneSO.addServerPack("copper_furnace", "Insane's Survival Overhaul Copper Furnace", () -> this.isEnabled() && !Packs.disableAllDataPacks && copperFurnace);
        InsaneSO.addServerPack("sapling_drop_fix", "Insane's Survival Overhaul Sapling Drop Fix", () -> this.isEnabled() && !Packs.disableAllDataPacks && saplingDropFix);
        InsaneSO.addServerPack("dark_forest_vegetation", "Insane's Survival Overhaul Roofed Forest Vegetation", () -> this.isEnabled() && !Packs.disableAllDataPacks && darkForestVegetation);
        InsaneSO.addServerPack("increased_end_cities", "Insane's Survival Overhaul Increased End Cities", () -> this.isEnabled() && !Packs.disableAllDataPacks && increaseEndCities);
    }
}
