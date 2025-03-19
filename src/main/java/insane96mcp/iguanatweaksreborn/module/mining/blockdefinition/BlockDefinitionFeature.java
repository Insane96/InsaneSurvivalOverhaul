package insane96mcp.iguanatweaksreborn.module.mining.blockdefinition;

import insane96mcp.iguanatweaksreborn.InsaneSurvivalOverhaul;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.module.misc.DataPacks;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;

@Label(name = "Block Definitions", description = "Change, through data packs, some blocks properties, from hardness to explosion resistance to speed and jump factors to bone meal chance to fail.")
@LoadFeature(module = Modules.Ids.MINING)
public class BlockDefinitionFeature extends Feature {
    public BlockDefinitionFeature(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
        InsaneSurvivalOverhaul.addServerPack("block_data", "Insane's Survival Overhaul Block Definitions", () -> !DataPacks.disableAllDataPacks && this.isEnabled());
    }
}
