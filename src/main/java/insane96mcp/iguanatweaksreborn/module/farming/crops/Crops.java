package insane96mcp.iguanatweaksreborn.module.farming.crops;

import insane96mcp.iguanatweaksreborn.InsaneSO;
import insane96mcp.iguanatweaksreborn.data.generator.ISOBlockTagsProvider;
import insane96mcp.iguanatweaksreborn.data.generator.ISOItemTagsProvider;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.module.experience.enchantments.EnchantmentsFeature;
import insane96mcp.iguanatweaksreborn.module.farming.crops.integration.FarmersDelightIntegration;
import insane96mcp.iguanatweaksreborn.module.misc.DataPacks;
import insane96mcp.iguanatweaksreborn.setup.ISORegistries;
import insane96mcp.iguanatweaksreborn.setup.registry.SimpleBlockWithItem;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.RegistryObject;

@LoadFeature(module = Modules.Ids.FARMING, description = "Crops tweaks and less yield from crops")
public class Crops extends Feature {

	public static final TagKey<Item> CHICKEN_FOOD_ITEMS = ISOItemTagsProvider.create("chicken_food_items");

	public static final TagKey<Block> HARDER_CROPS_TAG = ISOBlockTagsProvider.create("harder_crops");

	public static final RegistryObject<BlockItem> ROOTED_POTATO = ISORegistries.ITEMS.register("rooted_potato", () -> new SeedsBlockItem(Blocks.POTATOES, new Item.Properties()));
	public static final RegistryObject<BlockItem> CARROT_SEEDS = ISORegistries.ITEMS.register("carrot_seeds", () -> new SeedsBlockItem(Blocks.CARROTS, new Item.Properties()));
	public static final RegistryObject<BlockItem> ROOTED_ONION = ISORegistries.ITEMS.register("rooted_onion", () -> new SeedsBlockItem(ModList.get().isLoaded("farmersdelight") ? FarmersDelightIntegration.getOnion() : Blocks.POTATOES, new Item.Properties()));
	public static final RegistryObject<BlockItem> RICE_SEEDS = ISORegistries.ITEMS.register("rice_seeds", () -> new SeedsBlockItem(ModList.get().isLoaded("farmersdelight") ? FarmersDelightIntegration.getRice() : Blocks.POTATOES, new Item.Properties()));
	public static final RegistryObject<WildCropBlock> WILD_WHEAT = ISORegistries.BLOCKS.register("wild_wheat", () -> new WildCropBlock(BlockBehaviour.Properties.copy(Blocks.WHEAT).strength(0.8f, 0f)));
	public static final RegistryObject<WildCropBlock> WILD_CARROTS = ISORegistries.BLOCKS.register("wild_carrots", () -> new WildCropBlock(BlockBehaviour.Properties.copy(Blocks.WHEAT).strength(0.8f, 0f)));
	public static final RegistryObject<WildCropBlock> WILD_POTATOES = ISORegistries.BLOCKS.register("wild_potatoes", () -> new WildCropBlock(BlockBehaviour.Properties.copy(Blocks.WHEAT).strength(0.8f, 0f)));
	public static final RegistryObject<WildCropBlock> WILD_BEETROOTS = ISORegistries.BLOCKS.register("wild_beetroots", () -> new WildCropBlock(BlockBehaviour.Properties.copy(Blocks.WHEAT).strength(0.8f, 0f)));

	public static final SimpleBlockWithItem SOLANUM_NEOROSSII = SimpleBlockWithItem.register("solanum_neorossii", () -> new FlowerBlock(() -> MobEffects.MOVEMENT_SPEED, 10, BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).pushReaction(PushReaction.DESTROY).noCollission().instabreak().sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XZ)));
	public static final RegistryObject<Block> POTTED_SOLANUM_NEOROSSII = ISORegistries.BLOCKS.register("potted_solanum_neorossii", () -> new FlowerPotBlock(() -> (FlowerPotBlock) Blocks.FLOWER_POT, () -> SOLANUM_NEOROSSII.block().get(), BlockBehaviour.Properties.copy(Blocks.POTTED_ALLIUM)));

	@Config(description = """
						Set if crops require wet farmland to grow.
						Valid Values:
						NO: Crops will not require water to grow
						BONE_MEAL_ONLY: Crops will grow on dry farmland by only using bone meal
						ANY_CASE: Will make Crops not grow in any case when on dry farmland""")
	public static CropsRequireWater cropsRequireWater = CropsRequireWater.ANY_CASE;

	@Config(min = 1, description = "Radius where water hydrates farmland, vanilla is 4.")
	public static Integer waterHydrationRadius = 2;

	@Config(description = """
		Makes potatoes and carrots not plantable and also enables a data pack that makes the following changes:
		* Makes all vanilla crops drop only one seed (and makes carrots and potatoes drop the new seed item)
		* Makes crops hard to break, requiring an hoe as correct tool
		* Makes melon seeds and pumpkin seeds harder to obtain
		* Removes carrots and potato drops from zombies
		* Removes wheat seeds from tall grass
		* Makes wild crops generate in the world""")
	public static Boolean dataPack = true;

	public Crops(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
		InsaneSO.addServerPack("crops", "Insane's Survival Overhaul Crops", () -> this.isEnabled() && !DataPacks.disableAllDataPacks && dataPack);
	}

	@SubscribeEvent
	public void cropsRequireWater(BlockEvent.CropGrowEvent.Pre event) {
		if (!this.isEnabled()
				|| cropsRequireWater.equals(CropsRequireWater.NO)
				|| event.getResult().equals(Event.Result.DENY)
				|| !isAffectedByFarmland(event.getLevel(), event.getPos()))
			return;
		// Denies the growth if the crop is on farmland and the farmland is wet. If it's not on farmland the growth is not denied (e.g. Farmer's Delight rice)
		if (isCropOnFarmland(event.getLevel(), event.getPos()) && !isCropOnWetFarmland(event.getLevel(), event.getPos()))
			event.setResult(Event.Result.DENY);
	}

	public static boolean requiresWetFarmland(Level level, BlockPos blockPos) {
		return isEnabled(Crops.class)
				&& !cropsRequireWater.equals(CropsRequireWater.NO)
				&& isAffectedByFarmland(level, blockPos);
	}

	public static boolean hasWetFarmland(Level level, BlockPos blockPos) {
		return isCropOnFarmland(level, blockPos) && isCropOnWetFarmland(level, blockPos);
	}

	public enum CropsRequireWater {
		NO,
		BONE_MEAL_ONLY,
		ANY_CASE
	}

	/**
	 * @return true if the block is affected by the block below
	 */
	public static boolean isAffectedByFarmland(LevelAccessor levelAccessor, BlockPos cropPos) {
		BlockState state = levelAccessor.getBlockState(cropPos);
		Block block = state.getBlock();
		return block instanceof CropBlock || block instanceof StemBlock;
	}

	/**
	 * @return true if the block is on wet farmland
	 */
	public static boolean isCropOnWetFarmland(LevelAccessor levelAccessor, BlockPos cropPos) {
		BlockState sustainState = levelAccessor.getBlockState(cropPos.below());
		if (!(sustainState.getBlock() instanceof FarmBlock))
			return false;
		int moisture = sustainState.getValue(FarmBlock.MOISTURE);
		return moisture >= 7;
	}


	/**
	 * @return true if the block is on farmland
	 */
	public static boolean isCropOnFarmland(LevelAccessor levelAccessor, BlockPos cropPos) {
		BlockState sustainState = levelAccessor.getBlockState(cropPos.below());
		return sustainState.getBlock() instanceof FarmBlock;
	}

	@SubscribeEvent
	public void onTryToPlant(PlayerInteractEvent.RightClickBlock event) {
		if (!this.isEnabled())
			return;
		if (ModList.get().isLoaded("farmersdelight"))
			FarmersDelightIntegration.tryAlertRice(event);
		if (!(event.getLevel().getBlockState(event.getHitVec().getBlockPos()).getBlock() instanceof FarmBlock)
				|| !dataPack)
			return;

		if (event.getItemStack().is(Items.POTATO) || event.getItemStack().is(Items.CARROT))
			event.setCanceled(true);
		if (ModList.get().isLoaded("farmersdelight")) {
			if (FarmersDelightIntegration.preventPlanting(event.getItemStack()))
				event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void onChickenJoinLevel(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
				|| !(event.getEntity() instanceof Chicken chicken))
			return;

		chicken.goalSelector.addGoal(3, new TemptGoal(chicken, 1.0D, Ingredient.of(CARROT_SEEDS.get()), false));
	}

	@SubscribeEvent
	public void onTryToSeedChickens(PlayerInteractEvent.EntityInteract event) {
		if (!this.isEnabled()
				|| !dataPack
				|| !(event.getTarget() instanceof Chicken chicken)
				|| !chicken.isFood(event.getItemStack())
				|| event.getItemStack().is(CHICKEN_FOOD_ITEMS))
			return;

		event.setCanceled(true);
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public void onPickBlock(InputEvent.InteractionKeyMappingTriggered event) {
		if (!this.isEnabled()
				|| !event.getKeyMapping().same(Minecraft.getInstance().options.keyPickItem)
				|| Minecraft.getInstance().player == null
				|| Minecraft.getInstance().level == null
				|| Minecraft.getInstance().gameMode == null
				|| Minecraft.getInstance().hitResult == null
				|| Minecraft.getInstance().hitResult.getType() != HitResult.Type.BLOCK)
			return;
		Minecraft mc = Minecraft.getInstance();
		boolean isCreative = mc.player.getAbilities().instabuild;
		HitResult hitResult = mc.hitResult;

		BlockPos blockpos = ((BlockHitResult) hitResult).getBlockPos();
		BlockState blockstate = mc.level.getBlockState(blockpos);
		if (blockstate.isAir())
			return;

		Block block = blockstate.getBlock();
		ItemStack stack = null;
		if (block == Blocks.CARROTS)
			stack = new ItemStack(CARROT_SEEDS.get());
		else if (block == Blocks.POTATOES)
			stack = new ItemStack(ROOTED_POTATO.get());
		else if (ModList.get().isLoaded("farmersdelight"))
			stack = FarmersDelightIntegration.tryPickBlock(block);
		if (stack == null)
			return;
		Inventory inventory = mc.player.getInventory();

		int i = inventory.findSlotMatchingItem(stack);
		if (isCreative) {
			inventory.setPickedItem(stack);
			mc.gameMode.handleCreativeModeItemAdd(mc.player.getItemInHand(InteractionHand.MAIN_HAND), 36 + inventory.selected);
		}
		else if (i != -1) {
			if (Inventory.isHotbarSlot(i))
				inventory.selected = i;
			else
				mc.gameMode.handlePickItem(i);
		}
		event.setCanceled(true);
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onCropBreaking(PlayerEvent.BreakSpeed event) {
		if (!this.isEnabled()
				|| event.getState().destroySpeed == 0f
				|| !event.getState().is(HARDER_CROPS_TAG))
			return;
		ItemStack heldStack = event.getEntity().getMainHandItem();
		if (!(heldStack.getItem() instanceof DiggerItem diggerItem))
			return;
		if (!diggerItem.canPerformAction(heldStack, ToolActions.HOE_DIG) && !diggerItem.canPerformAction(heldStack, ToolActions.AXE_DIG))
			return;
		float miningSpeed = diggerItem.speed;
		if (miningSpeed > 1.0F) {
			int efficiencyLevel = EnchantmentHelper.getBlockEfficiency(event.getEntity());
			ItemStack itemstack = event.getEntity().getMainHandItem();
			if (efficiencyLevel > 0 && !itemstack.isEmpty()) {
				miningSpeed += EnchantmentsFeature.getEfficiencyBonus(miningSpeed, efficiencyLevel);
			}
		}
		if (diggerItem.canPerformAction(heldStack, ToolActions.HOE_DIG))
			event.setNewSpeed(event.getNewSpeed() * miningSpeed);
		else
			event.setNewSpeed(event.getNewSpeed() / 5f);
	}

	public static int getWaterHydrationRadius() {
		return isEnabled(Crops.class) ? waterHydrationRadius : 4;
	}
}