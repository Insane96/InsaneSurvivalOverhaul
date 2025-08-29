package insane96mcp.iguanatweaksreborn.module.items;

import insane96mcp.iguanatweaksreborn.InsaneSO;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.module.misc.Packs;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;

@LoadFeature(module = Modules.Ids.ITEMS, description = "If enabled, a data pack will be enabled that disables stone tools crafting and generation in chests will be replaced with copper ones.")
public class StoneToolsGone extends Feature {
    @Override
    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        InsaneSO.addServerPack("disable_stone_tools", "Insane's Survival Overhaul Disable Stone Tools", () -> this.isEnabled() && !Packs.disableAllDataPacks);
    }
}