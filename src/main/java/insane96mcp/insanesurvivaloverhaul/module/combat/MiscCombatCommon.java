package insane96mcp.insanesurvivaloverhaul.module.combat;

import insane96mcp.insanelib.core.feature.ConfigType;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.module.misc.Packs;
import net.neoforged.fml.config.ModConfig;

@ConfigType(ModConfig.Type.COMMON)
@LoadFeature(module = ISOModules.COMBAT, description = "Data packs that rework armor, tools and weapons.")
public class MiscCombatCommon extends Feature {
    @Config(description = "Enables a data pack that reworks armor.")
    public static Boolean armorReworkDataPack = true;
    @Config(description = "Enables a data pack that reworks tools and weapons.")
    public static Boolean toolsAndWeaponsDataPack = true;

    @Override
    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        InsaneSO.addServerPack("armor_rework", "Insane's Survival Overhaul Armor Rework", () -> this.isEnabled() && !Packs.disableAllDataPacks && armorReworkDataPack);
        InsaneSO.addServerPack("tools_and_weapons_rework", "Insane's Survival Overhaul Tools and Weapons Rework", () -> this.isEnabled() && !Packs.disableAllDataPacks && toolsAndWeaponsDataPack);
    }
}
