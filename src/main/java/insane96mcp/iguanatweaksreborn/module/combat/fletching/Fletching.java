package insane96mcp.iguanatweaksreborn.module.combat.fletching;

import insane96mcp.iguanatweaksreborn.InsaneSO;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.module.combat.fletching.block.ISEFletchingTableBlock;
import insane96mcp.iguanatweaksreborn.module.combat.fletching.crafting.FletchingRecipe;
import insane96mcp.iguanatweaksreborn.module.combat.fletching.data.FletchingRecipeSerializer;
import insane96mcp.iguanatweaksreborn.module.combat.fletching.entity.projectile.*;
import insane96mcp.iguanatweaksreborn.module.combat.fletching.inventory.FletchingMenu;
import insane96mcp.iguanatweaksreborn.module.combat.fletching.item.ISOArrowItem;
import insane96mcp.iguanatweaksreborn.module.misc.Packs;
import insane96mcp.iguanatweaksreborn.setup.ISORegistries;
import insane96mcp.iguanatweaksreborn.setup.registry.SimpleBlockWithItem;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.RegistryObject;

@LoadFeature(module = Modules.Ids.COMBAT, description = "Gives a use to the fletching table.")
public class Fletching extends Feature {
	public static final String INVALID_FLETCHING_LANG = InsaneSO.MOD_ID + ".invalid_fletching_table";
	public static final SimpleBlockWithItem FLETCHING_TABLE = SimpleBlockWithItem.register("fletching_table", () -> new ISEFletchingTableBlock(BlockBehaviour.Properties.copy(Blocks.FLETCHING_TABLE)));

	public static final RegistryObject<RecipeType<FletchingRecipe>> FLETCHING_RECIPE_TYPE = ISORegistries.RECIPE_TYPES.register("fletching", () -> new RecipeType<>() {
		@Override
		public String toString() {
			return "fletching";
		}
	});
	public static final RegistryObject<FletchingRecipeSerializer> FLETCHING_RECIPE_SERIALIZER = ISORegistries.RECIPE_SERIALIZERS.register("fletching", FletchingRecipeSerializer::new);
	public static final RegistryObject<MenuType<FletchingMenu>> FLETCHING_MENU_TYPE = ISORegistries.MENU_TYPES.register("fletching", () -> new MenuType<>(FletchingMenu::new, FeatureFlags.VANILLA_SET));

	public static final RegistryObject<EntityType<Arrow>> QUARTZ_ARROW = ISORegistries.ENTITY_TYPES.register("quartz_arrow", () ->
			EntityType.Builder.<Arrow>of(QuartzArrow::new, MobCategory.MISC)
					.sized(0.5F, 0.5F)
					.clientTrackingRange(4)
					.updateInterval(3)
					.build("quartz_arrow"));

	public static final RegistryObject<EntityType<Arrow>> DIAMOND_ARROW = ISORegistries.ENTITY_TYPES.register("diamond_arrow", () ->
			EntityType.Builder.<Arrow>of(DiamondArrow::new, MobCategory.MISC)
					.sized(0.5F, 0.5F)
					.clientTrackingRange(4)
					.updateInterval(3)
					.build("diamond_arrow"));

	public static final RegistryObject<EntityType<ExplosiveArrow>> EXPLOSIVE_ARROW = ISORegistries.ENTITY_TYPES.register("explosive_arrow", () ->
			EntityType.Builder.<ExplosiveArrow>of(ExplosiveArrow::new, MobCategory.MISC)
					.sized(0.5F, 0.5F)
					.clientTrackingRange(4)
					.updateInterval(3)
					.build("explosive_arrow"));

	public static final RegistryObject<EntityType<TorchArrow>> TORCH_ARROW = ISORegistries.ENTITY_TYPES.register("torch_arrow", () ->
			EntityType.Builder.<TorchArrow>of(TorchArrow::new, MobCategory.MISC)
					.sized(0.5F, 0.5F)
					.clientTrackingRange(4)
					.updateInterval(3)
					.build("torch_arrow"));

	public static final RegistryObject<EntityType<IceArrow>> ICE_ARROW = ISORegistries.ENTITY_TYPES.register("ice_arrow", () ->
			EntityType.Builder.of(IceArrow::new, MobCategory.MISC)
					.sized(0.5F, 0.5F)
					.clientTrackingRange(4)
					.updateInterval(3)
					.build("ice_arrow"));

	public static final RegistryObject<ISOArrowItem> QUARTZ_ARROW_ITEM = ISORegistries.ITEMS.register("quartz_arrow", () -> new ISOArrowItem(QUARTZ_ARROW::get, 0.5f, new Item.Properties()));
	public static final RegistryObject<ISOArrowItem> DIAMOND_ARROW_ITEM = ISORegistries.ITEMS.register("diamond_arrow", () -> new ISOArrowItem(DIAMOND_ARROW::get, 3f, new Item.Properties()));
	public static final RegistryObject<ISOArrowItem> EXPLOSIVE_ARROW_ITEM = ISORegistries.ITEMS.register("explosive_arrow", () -> new ISOArrowItem(EXPLOSIVE_ARROW::get, 0f, new Item.Properties()));
	public static final RegistryObject<ISOArrowItem> TORCH_ARROW_ITEM = ISORegistries.ITEMS.register("torch_arrow", () -> new ISOArrowItem(TORCH_ARROW::get, 1f, new Item.Properties()));
	public static final RegistryObject<ISOArrowItem> ICE_ARROW_ITEM = ISORegistries.ITEMS.register("ice_arrow", () -> new ISOArrowItem(ICE_ARROW::get, 1f, new Item.Properties()));

	@Config(description = "If disabled, torch arrows will only set mobs on fire")
	public static Boolean torchArrowsPlaceTorches = true;

	@Config(description = """
			Enables the following changes:
			* Replaces the vanilla fletching table recipe with the mod's one
			* Adds more arrows recipes""")
	public static Boolean dataPack = true;

	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		InsaneSO.addServerPack("fletching", "IguanaTweaks Expanded Fletching", () -> this.isEnabled() && !Packs.disableAllDataPacks && dataPack);
	}

	@SubscribeEvent
	public void onRightClickFletchingTable(PlayerInteractEvent.RightClickBlock event) {
		if (!this.isEnabled()
				|| event.getLevel().isClientSide()
				|| event.getHand() == InteractionHand.OFF_HAND
				|| !event.getLevel().getBlockState(event.getHitVec().getBlockPos()).is(Blocks.FLETCHING_TABLE)
				|| !dataPack)
			return;

		event.getEntity().sendSystemMessage(Component.translatable(INVALID_FLETCHING_LANG));
		event.setCanceled(true);
	}
}