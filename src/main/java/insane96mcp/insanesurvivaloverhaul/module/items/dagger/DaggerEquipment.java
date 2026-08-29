package insane96mcp.insanesurvivaloverhaul.module.items.dagger;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.module.items.copper.CopperEquipment;
import insane96mcp.insanesurvivaloverhaul.setup.ISORegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;
import net.neoforged.neoforge.registries.DeferredHolder;

@LoadFeature(module = ISOModules.ITEMS, name = "Daggers", description = "Adds daggers: a fast, low-damage weapon type available in every material tier. No mineable/* block-breaking ability, no sweep attack.")
public class DaggerEquipment extends Feature {
	// 4.0 base attack speed - 1.0 = 3.0 attacks/sec, flat across all materials.
	private static final float ATTACK_SPEED = -1.0F;
	private static final float ENTITY_REACH = -0.5F;

	public static final DeferredHolder<Item, DaggerItem> WOODEN_DAGGER = ISORegistries.ITEMS.register(
			"wooden_dagger", () -> new DaggerItem(Tiers.WOOD, new Item.Properties().attributes(DaggerItem.createDaggerAttributes(Tiers.WOOD, ATTACK_SPEED, ENTITY_REACH)))
	);
	public static final DeferredHolder<Item, DaggerItem> STONE_DAGGER = ISORegistries.ITEMS.register(
			"stone_dagger", () -> new DaggerItem(Tiers.STONE, new Item.Properties().attributes(DaggerItem.createDaggerAttributes(Tiers.STONE, ATTACK_SPEED, ENTITY_REACH)))
	);
	public static final DeferredHolder<Item, DaggerItem> IRON_DAGGER = ISORegistries.ITEMS.register(
			"iron_dagger", () -> new DaggerItem(Tiers.IRON, new Item.Properties().attributes(DaggerItem.createDaggerAttributes(Tiers.IRON, ATTACK_SPEED, ENTITY_REACH)))
	);
	public static final DeferredHolder<Item, DaggerItem> GOLDEN_DAGGER = ISORegistries.ITEMS.register(
			"golden_dagger", () -> new DaggerItem(Tiers.GOLD, new Item.Properties().attributes(DaggerItem.createDaggerAttributes(Tiers.GOLD, ATTACK_SPEED, ENTITY_REACH)))
	);
	public static final DeferredHolder<Item, DaggerItem> DIAMOND_DAGGER = ISORegistries.ITEMS.register(
			"diamond_dagger", () -> new DaggerItem(Tiers.DIAMOND, new Item.Properties().attributes(DaggerItem.createDaggerAttributes(Tiers.DIAMOND, ATTACK_SPEED, ENTITY_REACH)))
	);
	public static final DeferredHolder<Item, DaggerItem> NETHERITE_DAGGER = ISORegistries.ITEMS.register(
			"netherite_dagger", () -> new DaggerItem(Tiers.NETHERITE, new Item.Properties().attributes(DaggerItem.createDaggerAttributes(Tiers.NETHERITE, ATTACK_SPEED, ENTITY_REACH)).fireResistant())
	);
	public static final DeferredHolder<Item, DaggerItem> COPPER_DAGGER = ISORegistries.ITEMS.register(
			"copper_dagger", () -> new DaggerItem(CopperEquipment.ITEM_TIER, new Item.Properties().attributes(DaggerItem.createDaggerAttributes(CopperEquipment.ITEM_TIER, ATTACK_SPEED, ENTITY_REACH)))
	);
}
