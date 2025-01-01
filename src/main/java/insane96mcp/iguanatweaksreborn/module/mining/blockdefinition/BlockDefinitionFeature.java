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

@Label(name = "Block Definitions", description = "Change, through data packs, the properties of blocks, from hardness to explosion resistance to speed and jump factors. If enabled, a data pack is enabled that changes various blocks hardness, like ores and crops, and some blocks slowdown.")
@LoadFeature(module = Modules.Ids.MINING)
public class BlockDefinitionFeature extends Feature {
    public BlockDefinitionFeature(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
        IntegratedPack.addPack(new IntegratedPack(PackType.SERVER_DATA, "block_data", Component.literal("IguanaTweaks Reborn Block Definitions"), () -> !DataPacks.disableAllDataPacks && this.isEnabled()));
    }
}
