package insane96mcp.insanesurvivaloverhaul.module.farming.crops;

import com.google.common.collect.ImmutableSet;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.data.generator.ISOBlockTagsProvider;
import insane96mcp.insanesurvivaloverhaul.data.generator.ISOItemTagsProvider;
import insane96mcp.insanesurvivaloverhaul.mixin.accessor.BlockStateBaseAccessor;
import insane96mcp.insanesurvivaloverhaul.mixin.accessor.VillagerAccessor;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.module.misc.Packs;
import insane96mcp.insanesurvivaloverhaul.setup.ISORegistries;
import insane96mcp.insanesurvivaloverhaul.setup.SimpleBlockWithItem;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.block.CropGrowEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.Set;

@LoadFeature(module = ISOModules.FARMING, description = "Crops tweaks and less yield from crops")
public class Crops extends Feature {

	public static final TagKey<Item> CHICKEN_FOOD_ITEMS = ISOItemTagsProvider.create("chicken_food_items");

	public static final TagKey<Block> HARDER_CROPS_TAG = ISOBlockTagsProvider.create("harder_crops");

	public static final DeferredHolder<Item, BlockItem> ROOTED_POTATO = ISORegistries.ITEMS.register("rooted_potato", () -> new SeedsBlockItem(Blocks.POTATOES, new Item.Properties()));
	public static final DeferredHolder<Item, BlockItem> CARROT_SEEDS = ISORegistries.ITEMS.register("carrot_seeds", () -> new SeedsBlockItem(Blocks.CARROTS, new Item.Properties()));

	public static final DeferredHolder<Block, WildCropBlock> WILD_WHEAT = ISORegistries.BLOCKS.register("wild_wheat", () -> new WildCropBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT).strength(0.8f, 0f)));
	public static final DeferredHolder<Block, WildCropBlock> WILD_CARROTS = ISORegistries.BLOCKS.register("wild_carrots", () -> new WildCropBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT).strength(0.8f, 0f)));
	public static final DeferredHolder<Block, WildCropBlock> WILD_POTATOES = ISORegistries.BLOCKS.register("wild_potatoes", () -> new WildCropBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT).strength(0.8f, 0f)));
	public static final DeferredHolder<Block, WildCropBlock> WILD_BEETROOTS = ISORegistries.BLOCKS.register("wild_beetroots", () -> new WildCropBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT).strength(0.8f, 0f)));

	public static final SimpleBlockWithItem SOLANUM_NEOROSSII = SimpleBlockWithItem.register("solanum_neorossii", () -> new FlowerBlock(MobEffects.MOVEMENT_SPEED, 10, BlockBehaviour.Properties.ofFullCopy(Blocks.POPPY)));
	public static final DeferredHolder<Block, Block> POTTED_SOLANUM_NEOROSSII = ISORegistries.BLOCKS.register("potted_solanum_neorossii", () -> new FlowerPotBlock(() -> (FlowerPotBlock) Blocks.FLOWER_POT, () -> SOLANUM_NEOROSSII.block().get(), BlockBehaviour.Properties.ofFullCopy(Blocks.POTTED_ALLIUM)));

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

	@Config(description = "If true, villagers will be able to pick up and (farmers) plant mod's seeds and rooted items. Also prevents villagers from trampling farmland")
	public static Boolean fixVillagers = true;

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		InsaneSO.addServerPack("crops", "Insane's Survival Overhaul Crops", () -> this.isEnabled() && !Packs.disableAllDataPacks && dataPack);
	}

	/** Default items a villager regardless of its profession can pick up. */
	private static final Set<Item> VANILLA_WANTED_ITEMS = ImmutableSet.of(
			Items.BREAD,
			Items.POTATO,
			Items.CARROT,
			Items.WHEAT,
			Items.WHEAT_SEEDS,
			Items.BEETROOT,
			Items.BEETROOT_SEEDS,
			Items.TORCHFLOWER_SEEDS,
			Items.PITCHER_POD
	);

	@Override
	public void readConfig(ModConfigEvent event) {
		super.readConfig(event);
		if (!fixVillagers) {
			VillagerAccessor.setWantedItems(ImmutableSet.copyOf(VANILLA_WANTED_ITEMS));
		}
		else {
			VillagerAccessor.setWantedItems(ImmutableSet.<Item>builder()
					.addAll(VANILLA_WANTED_ITEMS)
					.add(CARROT_SEEDS.get(), ROOTED_POTATO.get())
					.build());
		}
	}

	@SubscribeEvent
	public void onFarmlandTrample(BlockEvent.FarmlandTrampleEvent event) {
		if (!this.isEnabled()
				|| !fixVillagers
				|| !(event.getEntity() instanceof Villager))
			return;

		event.setCanceled(true);
	}

	@SubscribeEvent
	public void cropsRequireWater(CropGrowEvent.Pre event) {
		if (!this.isEnabled()
				|| cropsRequireWater.equals(CropsRequireWater.NO)
				|| event.getResult().equals(CropGrowEvent.Pre.Result.DO_NOT_GROW)
				|| !isAffectedByFarmland(event.getLevel(), event.getPos()))
			return;
		// Denies the growth if the crop is on farmland and the farmland is wet. If it's not on farmland the growth is not denied (e.g. Farmer's Delight rice)
		if (isCropOnFarmland(event.getLevel(), event.getPos()) && !isCropOnWetFarmland(event.getLevel(), event.getPos()))
			event.setResult(CropGrowEvent.Pre.Result.DO_NOT_GROW);
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
		if (!(event.getLevel().getBlockState(event.getHitVec().getBlockPos()).getBlock() instanceof FarmBlock)
				|| !dataPack)
			return;

		if (event.getItemStack().is(Items.POTATO) || event.getItemStack().is(Items.CARROT))
			event.setCanceled(true);
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

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onCropBreaking(PlayerEvent.BreakSpeed event) {
		if (!this.isEnabled()
				|| ((BlockStateBaseAccessor) event.getState()).getDestroySpeed() == 0f
				|| !event.getState().is(HARDER_CROPS_TAG))
			return;
		ItemStack heldStack = event.getEntity().getMainHandItem();
		if (!(heldStack.getItem() instanceof DiggerItem diggerItem))
			return;
		if (!diggerItem.canPerformAction(heldStack, ItemAbilities.HOE_DIG) && !diggerItem.canPerformAction(heldStack, ItemAbilities.AXE_DIG))
			return;
		float miningSpeed = diggerItem.getTier().getSpeed();
		if (miningSpeed > 1.0F)
			miningSpeed += (float) event.getEntity().getAttributeValue(Attributes.MINING_EFFICIENCY);

		if (diggerItem.canPerformAction(heldStack, ItemAbilities.HOE_DIG))
			event.setNewSpeed(event.getNewSpeed() * miningSpeed);
		else
			event.setNewSpeed(event.getNewSpeed() / miningSpeed);
	}

	public static int getWaterHydrationRadius() {
		return isEnabled(Crops.class) ? waterHydrationRadius : 4;
	}
}