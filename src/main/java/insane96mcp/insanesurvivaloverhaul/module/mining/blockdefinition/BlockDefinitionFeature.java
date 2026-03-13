package insane96mcp.insanesurvivaloverhaul.module.mining.blockdefinition;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.module.misc.Packs;

@LoadFeature(module = ISOModules.MINING, description = "Change, through data packs, some blocks properties, from hardness to explosion resistance to speed and jump factors to bone meal chance to fail.")
public class BlockDefinitionFeature extends Feature {

    @Config(description = """
            Enables the built-in block definitions data pack, which changes the following:
            Makes Ancient Debris, Ender Chests, Obsidian, powder snow, spawners, wooden trapdoors easier to break.
            Budding Amethyst, Leaves, Netherrack, nylium harder to break.
            Makes Ores harder to break.
            Makes Copper, Gold, Iron ores, Amethyst clusters drop xp. Sculk Catalyst and Spawners drop moar xp.""")
    public static Boolean dataPack = true;

    @Override
    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        InsaneSO.addServerPack("block_data", "Insane's Survival Overhaul Block Definitions", () -> !Packs.disableAllDataPacks && this.isEnabled() && dataPack);
    }
}
