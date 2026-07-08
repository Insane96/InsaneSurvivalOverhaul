package insane96mcp.insanesurvivaloverhaul.module.items.copper;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.module.misc.Packs;
import insane96mcp.insanesurvivaloverhaul.setup.ISORegistries;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.SimpleTier;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.EnumMap;
import java.util.List;

@LoadFeature(module = ISOModules.ITEMS, description = "Basically backports copper equipment from latest MC versions.")
public class CopperEquipment extends Feature {
	public static final SimpleTier ITEM_TIER = new SimpleTier(BlockTags.INCORRECT_FOR_STONE_TOOL, 191, 5f, 1f, 8, () -> Ingredient.of(Items.COPPER_INGOT));

	public static final DeferredHolder<Item, SwordItem> SWORD = ISORegistries.MINECRAFT_ITEMS.register(
			"copper_sword", () -> new SwordItem(ITEM_TIER, new Item.Properties().attributes(SwordItem.createAttributes(ITEM_TIER, 3, -2.4F)))
	);
	public static final DeferredHolder<Item, ShovelItem> SHOVEL = ISORegistries.MINECRAFT_ITEMS.register(
			"copper_shovel", () -> new ShovelItem(ITEM_TIER, new Item.Properties().attributes(ShovelItem.createAttributes(ITEM_TIER, 1.5F, -3.0F)))
	);
	public static final DeferredHolder<Item, PickaxeItem> PICKAXE = ISORegistries.MINECRAFT_ITEMS.register(
			"copper_pickaxe", () -> new PickaxeItem(ITEM_TIER, new Item.Properties().attributes(PickaxeItem.createAttributes(ITEM_TIER, 1.0F, -2.8F)))
	);
	public static final DeferredHolder<Item, AxeItem> AXE = ISORegistries.MINECRAFT_ITEMS.register(
			"copper_axe", () -> new AxeItem(ITEM_TIER, new Item.Properties().attributes(AxeItem.createAttributes(ITEM_TIER, 6.0F, -3.1F)))
	);
	public static final DeferredHolder<Item, HoeItem> HOE = ISORegistries.MINECRAFT_ITEMS.register(
			"copper_hoe", () -> new HoeItem(ITEM_TIER, new Item.Properties().attributes(HoeItem.createAttributes(ITEM_TIER, -2.0F, -1.0F)))
	);
	public static final DeferredHolder<ArmorMaterial, ArmorMaterial> ARMOR_MATERIAL = ISORegistries.MINECRAFT_ARMOR_MATERIALS.register(
			"copper", () -> new ArmorMaterial(
					Util.make(new EnumMap<>(ArmorItem.Type.class), m -> {
						m.put(ArmorItem.Type.BOOTS, 1);
						m.put(ArmorItem.Type.LEGGINGS, 3);
						m.put(ArmorItem.Type.CHESTPLATE, 4);
						m.put(ArmorItem.Type.HELMET, 2);
						m.put(ArmorItem.Type.BODY, 5);
					}),
					8,
					SoundEvents.ARMOR_EQUIP_IRON,
					() -> Ingredient.of(Items.COPPER_INGOT),
					List.of(new ArmorMaterial.Layer(ResourceLocation.withDefaultNamespace("copper"))),
					0.0F,
					0.0F
			)
	);

	public static final DeferredHolder<Item, ArmorItem> HELMET = ISORegistries.MINECRAFT_ITEMS.register(
			"copper_helmet", () -> new ArmorItem(ARMOR_MATERIAL, ArmorItem.Type.HELMET, new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(11)))
	);
	public static final DeferredHolder<Item, ArmorItem> CHESTPLATE = ISORegistries.MINECRAFT_ITEMS.register(
			"copper_chestplate", () -> new ArmorItem(ARMOR_MATERIAL, ArmorItem.Type.CHESTPLATE, new Item.Properties().durability(ArmorItem.Type.CHESTPLATE.getDurability(11)))
	);
	public static final DeferredHolder<Item, ArmorItem> LEGGINGS = ISORegistries.MINECRAFT_ITEMS.register(
			"copper_leggings", () -> new ArmorItem(ARMOR_MATERIAL, ArmorItem.Type.LEGGINGS, new Item.Properties().durability(ArmorItem.Type.LEGGINGS.getDurability(11)))
	);
	public static final DeferredHolder<Item, ArmorItem> BOOTS = ISORegistries.MINECRAFT_ITEMS.register(
			"copper_boots", () -> new ArmorItem(ARMOR_MATERIAL, ArmorItem.Type.BOOTS, new Item.Properties().durability(ArmorItem.Type.BOOTS.getDurability(11)))
	);

	public static final DeferredHolder<Item, Item> NUGGET = ISORegistries.MINECRAFT_ITEMS.register(
			"copper_nugget", () -> new Item(new Item.Properties())
	);

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		InsaneSO.addServerPack("copper_equipment", "Insane's Survival Extra Copper Expansion", () -> this.isEnabled() && !Packs.disableAllDataPacks);
	}
}