package insane96mcp.insanesurvivaloverhaul.module.combat;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.module.misc.Packs;

@LoadFeature(module = ISOModules.COMBAT)
public class MiscCombat extends Feature {
    @Config(description = "If enabled, tools will not take 2 damage when used to hurt entities")
    public static Boolean oneDamageForToolAttacking = true;
    @Config(description = "In vanilla, if you attack as soon as you just attacked you already deal 20% of the full damage. This changes that to 0%.")
    public static Boolean noDamageWhenSpamming = true;

    @Config(description = "If true, tridents damage will be calculated based off the item thrown, instead of a fixed 8")
    public static Boolean thrownTridentItemBasedDamage = true;

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

    public static boolean noDamageWhenSpamming() {
        return isEnabled(MiscCombat.class) && noDamageWhenSpamming;
    }
}