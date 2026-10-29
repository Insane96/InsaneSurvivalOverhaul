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
@LoadFeature(module = ISOModules.COMBAT, description = "Data packs of Piercing Damage.")
public class PiercingDamageCommon extends Feature {
	@Config(description = "Enables a data pack that adds the Armor Piercer enchantment (max level V). Each level grants bonus Piercing Damage scaled by the weapon's Attack Damage (see armorPiercerDamagePerAttackDamage). It's incompatible with other damage enchantments (Sharpness, Smite, Bane of Arthropods, Impaling, Density, Breach, Critical).")
	public static Boolean armorPiercerEnchantmentDataPack = true;

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		InsaneSO.addServerPack("armor_piercer_enchantment", "Insane's Survival Overhaul Armor Piercer Enchantment", () -> this.isEnabled() && !Packs.disableAllDataPacks && armorPiercerEnchantmentDataPack);
	}
}
