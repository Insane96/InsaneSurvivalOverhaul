package insane96mcp.iguanatweaksreborn.module.items.flintexpansion;

import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.module.misc.DataPacks;
import insane96mcp.iguanatweaksreborn.setup.ISORegistries;
import insane96mcp.iguanatweaksreborn.setup.IntegratedPack;
import insane96mcp.iguanatweaksreborn.setup.registry.SimpleBlockWithItem;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.item.ILItemTier;
import insane96mcp.shieldsplus.world.item.SPShieldItem;
import insane96mcp.shieldsplus.world.item.SPShieldMaterial;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.RegistryObject;

@Label(name = "Flint Expansion", description = "Add flint tools and shield.")
@LoadFeature(module = Modules.Ids.ITEMS, canBeDisabled = false)
public class FlintExpansion extends Feature {

	public static final SimpleBlockWithItem FLINT_ROCK = SimpleBlockWithItem.register("flint_rock", () -> new GroundFlintBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).strength(0.5F, 1F).offsetType(BlockBehaviour.OffsetType.XZ).dynamicShape()));

	public static final ILItemTier ITEM_TIER = new ILItemTier(1, 99, 6f, 1.5f, 7, () -> Ingredient.of(Items.FLINT));

	public static final RegistryObject<Item> SWORD = ISORegistries.ITEMS.register("flint_sword", () -> new SwordItem(ITEM_TIER, 3, -2.4F, new Item.Properties()));
	public static final RegistryObject<Item> SHOVEL = ISORegistries.ITEMS.register("flint_shovel", () -> new ShovelItem(ITEM_TIER, 1.5F, -3.0F, new Item.Properties()));
	public static final RegistryObject<Item> PICKAXE = ISORegistries.ITEMS.register("flint_pickaxe", () -> new PickaxeItem(ITEM_TIER, 1, -2.8F, new Item.Properties()));
	public static final RegistryObject<Item> AXE = ISORegistries.ITEMS.register("flint_axe", () -> new AxeItem(ITEM_TIER, 7.0F, -3.1F, new Item.Properties()));
	public static final RegistryObject<Item> HOE = ISORegistries.ITEMS.register("flint_hoe", () -> new HoeItem(ITEM_TIER, -1, -2.0F, new Item.Properties()));

	@Config
	@Label(name = "Disable Stone Tools", description = "If true, a data pack will be enabled that disables stone tools crafting and generation in chests will be replaced with flint ones")
	public static Boolean disableStoneTools = true;

	public FlintExpansion(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
		IntegratedPack.addPack(new IntegratedPack(PackType.SERVER_DATA, "disable_stone_tools", Component.literal("Insane's Survival Overhaul Disable Stone Tools"), () -> this.isEnabled() && !DataPacks.disableAllDataPacks && disableStoneTools));
		IntegratedPack.addPack(new IntegratedPack(PackType.SERVER_DATA, "flint_expansion", Component.literal("Insane's Survival Overhaul Flint Expansion"), () -> this.isEnabled() && !DataPacks.disableAllDataPacks));
	}

	public static boolean areStoneToolsDisabled() {
		return Feature.isEnabled(FlintExpansion.class) && disableStoneTools;
	}

	public static class ShieldsPlusIntegration {
		public static final SPShieldMaterial SHIELD_MATERIAL = new SPShieldMaterial("flint", 33, () -> Items.FLINT, 9, Rarity.COMMON);
		public static final RegistryObject<SPShieldItem> SHIELD = ISORegistries.registerShield("flint_shield", SHIELD_MATERIAL);

		public static void init() {
			//Here just to load the class if the mod is present
		}
	}
}