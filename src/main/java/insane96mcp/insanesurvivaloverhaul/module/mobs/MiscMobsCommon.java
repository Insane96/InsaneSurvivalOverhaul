package insane96mcp.insanesurvivaloverhaul.module.mobs;

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
@LoadFeature(module = ISOModules.MOBS, description = "Data and resource packs for mobs loot.")
public class MiscMobsCommon extends Feature {
    @Config(description = "Reworks the loot tables of several mobs (blaze, cave spider, creeper, enderman, ghast, magma cube, phantom, piglin brute, pillager, skeleton, slime, spider, stray, witch, wither skeleton, zombified piglin), gives bogged extra drops (glowstone dust, mushrooms), makes snow golems drop their carved pumpkin on death if they still have it, and makes mobs drop reduced loot if not killed by a player")
    public static Boolean lootChanges = true;
    @Config(description = "If true, renames Heart of the Sea to Divine Fragment and retextures it accordingly")
    public static Boolean renameHeartOfTheSeaToDivineFragment = true;

    @Override
    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        InsaneSO.addServerPack("mob_loot_changes", "Insane's Survival Overhaul Mob Loot Changes", () -> this.isEnabled() && !Packs.disableAllDataPacks && lootChanges);
        InsaneSO.addClientPack("divine_fragment_rp", "Insane's Survival Overhaul Divine Fragment", () -> this.isEnabled() && lootChanges && renameHeartOfTheSeaToDivineFragment);
    }
}
