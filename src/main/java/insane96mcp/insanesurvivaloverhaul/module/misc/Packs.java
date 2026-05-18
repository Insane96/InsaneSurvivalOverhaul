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
            * With shears you can make 3 torches out of coal
            * With Fire Charges you can make them later in the game.""")
    public static Boolean hardcoreTorches = true;

    @Config(description = "Changes vanilla chains recipe. Makes chains easily craftable with nuggets only.")
    public static Boolean cheaperChains = true;

    @Config(description = """
            Minor changes:
            * Cakes now drop when broken and not eaten
            * Dark Prismarine is made easier
            * Clay balls can be crafted from blocks
            * Leads and Sticky Pistons can also be made with Honey
            * Iron Doors can be blasted back to Iron Ingots
            * Dispensers can be made from droppers
            * Levers and glass can now be broken faster with pickaxes, cactus with hoes""")
    public static Boolean miscTweaks = true;

    @Config(description = "Makes redstone components require redstone in their recipe (e.g. Levers, pressure plates, etc).")
    public static Boolean actualRedstoneComponents = true;

    @Override
    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        InsaneSO.addServerPack("hardcore_torches", "Insane's Survival Overhaul Hardcore Torches", () -> this.isEnabled() && !Packs.disableAllDataPacks && hardcoreTorches);
        InsaneSO.addServerPack("cheaper_chains", "Insane's Survival Overhaul Cheaper Chains", () -> this.isEnabled() && !Packs.disableAllDataPacks && cheaperChains);
        InsaneSO.addServerPack("misc_tweaks", "Insane's Survival Overhaul Misc Tweaks", () -> this.isEnabled() && !Packs.disableAllDataPacks && miscTweaks);
        InsaneSO.addServerPack("actual_redstone_components", "Insane's Survival Overhaul Actual Redstone components", () -> this.isEnabled() && !Packs.disableAllDataPacks && actualRedstoneComponents);
        InsaneSO.addServerPack("disable_long_noses", "Insane's Survival Overhaul Disable Long Noses", () -> this.isEnabled() && !Packs.disableAllDataPacks && disableLongNosesStructures);
        InsaneSO.addServerPack("copper_furnace", "Insane's Survival Overhaul Copper Furnace", () -> this.isEnabled() && !Packs.disableAllDataPacks && copperFurnace);
    }
}
