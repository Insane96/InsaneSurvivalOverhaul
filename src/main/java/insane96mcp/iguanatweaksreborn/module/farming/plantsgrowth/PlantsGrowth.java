package insane96mcp.iguanatweaksreborn.module.farming.plantsgrowth;

import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.module.misc.DataPacks;
import insane96mcp.iguanatweaksreborn.setup.IntegratedPack;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.SaplingGrowTreeEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Plants Growth", description = "Slower Plants (non-crops) growing. Plants properties are controlled via json in this feature's folder")
@LoadFeature(module = Modules.Ids.FARMING)
public class PlantsGrowth extends Feature {
	@Config
	@Label(name = "Huge mushrooms on Mycelium only")
	public static Boolean hugeMushroomsOnMyceliumOnly = true;
	@Config(min = 0)
	@Label(name = "Cave vines underground", description = "If != 1, cave vines will grow this slower above sea level or if they can see the sky light")
	public static Double caveVinesUnderground = 3d;

	@Config
	@Label(name = "Plant growth multipliers data pack", description = "If true, a data pack is enabled that changes the growth of plants based off various factors, such as sunlight and biome")
	public static Boolean plantGrowthMultipliersDataPack = true;

	public PlantsGrowth(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
		IntegratedPack.addPack(new IntegratedPack(PackType.SERVER_DATA, "plant_growth_modifiers", Component.literal("Insane's Survival Tweaks Plant Growth modifiers"), () -> super.isEnabled() && !DataPacks.disableAllDataPacks && plantGrowthMultipliersDataPack));
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
}
