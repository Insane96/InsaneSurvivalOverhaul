package insane96mcp.iguanatweaksreborn.module.mining.blockdefinition;

import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.module.misc.DataPacks;
import insane96mcp.iguanatweaksreborn.setup.IntegratedPack;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;

@Label(name = "Block Definitions", description = "Change, through data packs, some blocks properties, from hardness to explosion resistance to speed and jump factors to bone meal chance to fail.")
@LoadFeature(module = Modules.Ids.MINING)
public class BlockDefinitionFeature extends Feature {
    public BlockDefinitionFeature(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
        IntegratedPack.addPack(new IntegratedPack(PackType.SERVER_DATA, "block_data", Component.literal("Insane's Survival Overhaul Block Definitions"), () -> !DataPacks.disableAllDataPacks && this.isEnabled()));
    }
}
