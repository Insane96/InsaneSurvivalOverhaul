package insane96mcp.insanesurvivaloverhaul.module.mining.blockhardness;

import insane96mcp.insanelib.core.JsonFeature;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.util.SnapshotList;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.data.generator.ISOBlockTagsProvider;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.ArrayList;
import java.util.List;

@LoadFeature(module = ISOModules.MINING, description = "Change blocks hardness. Dimension Hardness and Depth Hardness are controlled via json in this feature's folder. Block hardness can be changed with Block Definitions via data packs")
public class BlockHardness extends JsonFeature {
	public static final TagKey<Block> HARDNESS_BLACKLIST = ISOBlockTagsProvider.create("hardness_blacklist");
	public static final TagKey<Block> DEPTH_MULTIPLIER_BLACKLIST = ISOBlockTagsProvider.create("depth_multiplier_blacklist");

	public static final ArrayList<DimensionHardnessMultiplier> DIMENSION_HARDNESS_MULTIPLIERS_DEFAULT = new ArrayList<>(List.of(
			new DimensionHardnessMultiplier("minecraft:the_nether", 1.5d)
	));
	public static final SnapshotList<DimensionHardnessMultiplier> dimensionHardnessMultiplier = new SnapshotList<>();
	public static final ResourceLocation DIMENSION_HARDNESS_SYNC_ID = InsaneSO.id("dimension_hardness");

	public static final ArrayList<DepthHardnessDimension> DEPTH_MULTIPLIER_DIMENSION_DEFAULT = new ArrayList<>(List.of(

	));
	public static final SnapshotList<DepthHardnessDimension> depthMultiplierDimension = new SnapshotList<>();
	public static final ResourceLocation DEPTH_MULTIPLIERS_SYNC_ID = InsaneSO.id("depth_multipliers");

	@Config(min = 0d, max = 128d, description = "Multiplier applied to the hardness of blocks. E.g. with this set to 2.0 blocks will take 2 times longer to break.")
	public static Double hardnessMultiplier = 1d;

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		addSyncType(DIMENSION_HARDNESS_SYNC_ID, new SyncType(json -> loadAndReadJson(json, dimensionHardnessMultiplier, DIMENSION_HARDNESS_MULTIPLIERS_DEFAULT, DimensionHardnessMultiplier.LIST_TYPE)));
		getJsonConfigs().add(new JsonConfig<>("dimension_hardness.json", dimensionHardnessMultiplier, DIMENSION_HARDNESS_MULTIPLIERS_DEFAULT, DimensionHardnessMultiplier.LIST_TYPE).syncToClient(DIMENSION_HARDNESS_SYNC_ID));
		addSyncType(DEPTH_MULTIPLIERS_SYNC_ID, new SyncType(json -> loadAndReadJson(json, depthMultiplierDimension, DEPTH_MULTIPLIER_DIMENSION_DEFAULT, DepthHardnessDimension.LIST_TYPE)));
		getJsonConfigs().add(new JsonConfig<>("depth_multipliers.json", depthMultiplierDimension, DEPTH_MULTIPLIER_DIMENSION_DEFAULT, DepthHardnessDimension.LIST_TYPE).syncToClient(DEPTH_MULTIPLIERS_SYNC_ID));
	}

	@Override
	public String getModConfigFolder() {
		return InsaneSO.CONFIG_FOLDER;
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void processGlobalHardness(PlayerEvent.BreakSpeed event) {
		if (!this.isEnabled()
				|| event.getPosition().isEmpty())
			return;

		BlockPos pos = event.getPosition().get();
		ResourceLocation dimensionId = event.getEntity().level().dimension().location();
		double blockHardnessMultiplier = getBlockHardnessMultiplier(event.getState(), dimensionId, pos);
		if (blockHardnessMultiplier == 1d)
			return;
		double multiplier = 1d / blockHardnessMultiplier;
		event.setNewSpeed((float) (event.getNewSpeed() * multiplier));
	}

	public static double getBlockHardnessMultiplier(BlockState state, ResourceLocation dimensionId, BlockPos pos) {
		double blockHardness = getBlockGlobalHardnessMultiplier(state, dimensionId);
		blockHardness += getDepthHardnessMultiplier(state, dimensionId, pos);
		return blockHardness;
	}

	/**
	 * Returns 1d when no changes must be made, else will return a multiplier for block hardness
	 */
	public static double getBlockGlobalHardnessMultiplier(BlockState state, ResourceLocation dimensionId) {
		if (state.is(HARDNESS_BLACKLIST))
			return 1d;

		//If there's a dimension multiplier present return that
		for (DimensionHardnessMultiplier dimensionHardnessMultiplier : dimensionHardnessMultiplier)
			if (dimensionId.equals(dimensionHardnessMultiplier.dimension))
				return dimensionHardnessMultiplier.multiplier;

		//Otherwise, return the global multiplier
		return hardnessMultiplier;
	}

	/**
	 * Returns an additive multiplier based off the depth of the block broken
	 */
	public static double getDepthHardnessMultiplier(BlockState state, ResourceLocation dimensionId, BlockPos pos) {
		if (!Feature.isEnabled(BlockHardness.class))
			return 0d;

		if (state.is(DEPTH_MULTIPLIER_BLACKLIST))
			return 0d;

		double hardness = 0d;
		for (DepthHardnessDimension depthHardnessDimension : depthMultiplierDimension) {
			if (dimensionId.equals(depthHardnessDimension.dimension)) {
				hardness += depthHardnessDimension.multiplier * Math.max(depthHardnessDimension.applyBelowY - Math.max(pos.getY(), depthHardnessDimension.stopAt), 0);
			}
		}
		return hardness;
	}
}
