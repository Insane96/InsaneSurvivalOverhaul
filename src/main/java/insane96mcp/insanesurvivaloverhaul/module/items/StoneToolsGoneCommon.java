package insane96mcp.insanesurvivaloverhaul.module.items;

import insane96mcp.insanelib.core.feature.ConfigType;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.module.misc.Packs;
import net.neoforged.fml.config.ModConfig;

import static insane96mcp.insanesurvivaloverhaul.InsaneSO.addClientPack;
import static insane96mcp.insanesurvivaloverhaul.InsaneSO.addServerPack;

@ConfigType(ModConfig.Type.COMMON)
@LoadFeature(module = ISOModules.ITEMS, description = "Data and resource packs that disable stone tools crafting and make copper ore minable with wooden pickaxe.")
public class StoneToolsGoneCommon extends Feature {
    @Config
    public static Boolean dataPack = true;

    @Override
    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        addServerPack("disable_stone_tools", "Insane's Survival Overhaul Disable Stone Tools", () -> this.isEnabled() && !Packs.disableAllDataPacks && dataPack);
        addClientPack("disable_stone_tools_rp", "Insane's Survival Overhaul Disable Stone Tools", () -> this.isEnabled() && !Packs.disableAllDataPacks && dataPack);
    }
}
