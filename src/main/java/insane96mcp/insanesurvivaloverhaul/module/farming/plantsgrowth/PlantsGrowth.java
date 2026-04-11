package insane96mcp.insanesurvivaloverhaul.module.farming.plantsgrowth;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.module.misc.Packs;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.BonemealEvent;
import net.neoforged.neoforge.event.level.BlockGrowFeatureEvent;
import net.neoforged.neoforge.event.level.block.CropGrowEvent;

@LoadFeature(module = ISOModules.FARMING, description = "Slower Plants (non-crops) growing. Plants properties are controlled via data packs")
public class PlantsGrowth extends Feature {
	@Config
	public static Boolean hugeMushroomsOnMyceliumOnly = true;
	@Config(min = 0, description = "If != 1, cave vines will grow this slower above sea level or if they can see the sky light. This is multiplied to the plant_growth_modifiers multipliers")
	public static Double caveVinesUnderground = 3d;

	@Config(description = "If true, a data pack is enabled that changes the growth of plants based off various factors, such as sunlight and biome")
	public static Boolean dataPack = true;

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		InsaneSO.addServerPack("plant_growth_modifiers", "Insane's Survival Overhaul Plant Growth modifiers", () -> super.isEnabled() && !Packs.disableAllDataPacks && dataPack);
	}

	@SubscribeEvent
	public void onCropGrowEvent(CropGrowEvent.Pre event) {
		if (!this.isEnabled()
				|| PlantsGrowthReloadListener.GROWTH_MULTIPLIERS.isEmpty())
			return;
		double multiplier = 1d;
		for (PlantGrowthMultiplier plantGrowthMultiplier : PlantsGrowthReloadListener.GROWTH_MULTIPLIERS) {
			multiplier *= plantGrowthMultiplier.getMultiplier(event.getState(), (Level) event.getLevel(), event.getPos());
		}
		if (caveVinesUnderground != 1 && event.getLevel().getBlockState(event.getPos().above()).is(BlockTags.CAVE_VINES)) {
            //noinspection deprecation
            if (event.getLevel().getSeaLevel() > event.getPos().getY()
					|| event.getLevel().getBrightness(LightLayer.SKY, event.getPos()) > 0)
				multiplier *= caveVinesUnderground;
		}

		if (multiplier == 0d) {
			event.setResult(CropGrowEvent.Pre.Result.DO_NOT_GROW);
			return;
		}
		if (multiplier == 1d)
			return;
		double chance = 1d / multiplier;
		if (event.getLevel().getRandom().nextDouble() > chance)
			event.setResult(CropGrowEvent.Pre.Result.DO_NOT_GROW);
	}

	@SubscribeEvent
	public void onMushroomGrow(BlockGrowFeatureEvent event) {
		if (!this.isEnabled()
				|| !hugeMushroomsOnMyceliumOnly
				|| event.getFeature() == null)
			return;

		if ((event.getFeature().is(TreeFeatures.HUGE_BROWN_MUSHROOM) || event.getFeature().is(TreeFeatures.HUGE_RED_MUSHROOM)) && !event.getLevel().getBlockState(event.getPos().below()).is(Blocks.MYCELIUM))
			event.setCanceled(true);
	}

	@SubscribeEvent
	public void onMushroomGrow(BonemealEvent event) {
		if (!this.isEnabled()
				|| !hugeMushroomsOnMyceliumOnly
				|| (!event.getState().is(Blocks.BROWN_MUSHROOM) && !event.getState().is(Blocks.RED_MUSHROOM)))
			return;

		if (!event.getLevel().getBlockState(event.getPos().below()).is(Blocks.MYCELIUM))
			event.setCanceled(true);
	}
}
