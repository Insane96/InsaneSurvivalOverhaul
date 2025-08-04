package insane96mcp.iguanatweaksreborn.module.farming.plantsgrowth;

import insane96mcp.iguanatweaksreborn.InsaneSO;
import insane96mcp.iguanatweaksreborn.data.generator.ISOItemTagsProvider;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.module.misc.DataPacks;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.SaplingGrowTreeEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@LoadFeature(module = Modules.Ids.FARMING, description = "Slower Plants (non-crops) growing. Plants properties are controlled via data packs")
public class PlantsGrowth extends Feature {
	public static final TagKey<Item> MULBERRY = ISOItemTagsProvider.create("mulberry");

	@Config
	public static Boolean hugeMushroomsOnMyceliumOnly = true;
	@Config(min = 0, description = "If != 1, cave vines will grow this slower above sea level or if they can see the sky light")
	public static Double caveVinesUnderground = 3d;
	@Config(min = 0, description = "Prevent planting upgrade aquatic mulberries")
	public static Boolean preventPlantingUpgradeAquaticMulberries = true;

	@Config(description = "If true, a data pack is enabled that changes the growth of plants based off various factors, such as sunlight and biome")
	public static Boolean dataPack = true;

	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		InsaneSO.addServerPack("plant_growth_modifiers", "Insane's Survival Overhaul Plant Growth modifiers", () -> super.isEnabled() && !DataPacks.disableAllDataPacks && dataPack);
	}

	@SubscribeEvent
	public void onCropGrowEvent(BlockEvent.CropGrowEvent.Pre event) {
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
			event.setResult(Event.Result.DENY);
			return;
		}
		if (multiplier == 1d)
			return;
		double chance = 1d / multiplier;
		if (event.getLevel().getRandom().nextDouble() > chance)
			event.setResult(Event.Result.DENY);
	}

	@SubscribeEvent
	public void onMushroomGrow(SaplingGrowTreeEvent event) {
		if (!this.isEnabled()
				|| !hugeMushroomsOnMyceliumOnly
				|| event.getFeature() == null)
			return;

		if ((event.getFeature().is(TreeFeatures.HUGE_BROWN_MUSHROOM) || event.getFeature().is(TreeFeatures.HUGE_RED_MUSHROOM)) && !event.getLevel().getBlockState(event.getPos().below()).is(Blocks.MYCELIUM)) {
			event.setResult(Event.Result.DENY);
		}
	}

	@SubscribeEvent
	public void onMushroomGrow(BonemealEvent event) {
		if (!this.isEnabled()
				|| !hugeMushroomsOnMyceliumOnly
				|| (!event.getBlock().is(Blocks.BROWN_MUSHROOM) && !event.getBlock().is(Blocks.RED_MUSHROOM)))
			return;

		if (!event.getLevel().getBlockState(event.getPos().below()).is(Blocks.MYCELIUM)) {
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void onMulberryPlant(PlayerInteractEvent.RightClickBlock event) {
		if (!this.isEnabled()
				|| preventPlantingUpgradeAquaticMulberries
				|| !event.getItemStack().is(MULBERRY))
			return;

		BlockState state = event.getLevel().getBlockState(event.getPos());
		if (state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES))
			event.setCanceled(true);
	}
}
