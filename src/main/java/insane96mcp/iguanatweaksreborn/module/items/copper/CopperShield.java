package insane96mcp.iguanatweaksreborn.module.items.copper;

import insane96mcp.iguanatweaksreborn.setup.ISORegistries;
import insane96mcp.shieldsplus.setup.SPItems;
import insane96mcp.shieldsplus.world.item.SPShieldItem;
import insane96mcp.shieldsplus.world.item.SPShieldMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.RegistryObject;

public class CopperShield extends SPShieldItem {
	public static final SPShieldMaterial SHIELD_MATERIAL = new SPShieldMaterial("copper", 134, () -> Items.COPPER_INGOT, 10, Rarity.COMMON);
	public CopperShield(Properties p_43089_) {
		super(SHIELD_MATERIAL, p_43089_);
	}

	public static RegistryObject<SPShieldItem> registerShield(String id) {
		Properties properties = new Properties().durability(SHIELD_MATERIAL.durability).rarity(SHIELD_MATERIAL.rarity);
		RegistryObject<SPShieldItem> shield = ISORegistries.ITEMS.register(id, () -> new CopperShield(properties));
		SPItems.SHIELDS.add(shield);
		return shield;
	}
}
