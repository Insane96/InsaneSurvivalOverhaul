package insane96mcp.iguanatweaksreborn.module.world.seasons;

import com.google.common.collect.Lists;
import insane96mcp.iguanatweaksreborn.InsaneSurvivalOverhaul;
import insane96mcp.iguanatweaksreborn.data.generator.ISOBlockTagsProvider;
import insane96mcp.iguanatweaksreborn.event.HookTickToHookLureEvent;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.module.misc.DataPacks;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.DistanceManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import sereneseasons.api.season.Season;
import sereneseasons.api.season.SeasonChangedEvent;
import sereneseasons.api.season.SeasonHelper;
import sereneseasons.init.ModConfig;
import sereneseasons.season.SeasonHandler;
import sereneseasons.season.SeasonSavedData;
import sereneseasons.season.SeasonTime;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Label(name = "Seasons", description = "Change a few things relative to Serene Seasons")
@LoadFeature(module = Modules.Ids.WORLD, requiresMods = {"sereneseasons"})
public class Seasons extends Feature {

	public static final GameRules.Key<GameRules.BooleanValue> RULE_SEASONGRASSGROWDEATH = GameRules.register("iguanatweaks:doSeasonGrassGrowDeath", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true));

	public static final TagKey<Block> PLANTS_TO_DECAY = ISOBlockTagsProvider.create("plants_to_decay");
	public static final TagKey<Block> PLANTS_TO_DEAD_BUSH = ISOBlockTagsProvider.create("plants_to_dead_bush");

	@Config
	@Label(name = "Serene Seasons changes", description = """
			Makes the following changes to Serene Seasons config:
			* seasonal_crops is set to false, as it's controlled by Plants Growth
			* Sets the starting season to the one in 'Starting season'""")
	public static Boolean changeSereneSeasonsConfig = true;

	@Config
	@Label(name = "No greenhouse glass", description = "Removes greenhouse glass.")
	public static Boolean noGreenHouseGlass = true;

	@Config
	@Label(name = "No Saplings in Winter", description = "Saplings no longer drop in Winter.")
	public static Boolean noSaplingsInWinter = true;

	@Config
	@Label(name = "Grass Decay and Growth", description = "Grass and tall grass decays in Winter and regrows back in Spring. Saplings are also transformed into Dead Bushes.")
	public static Boolean grassDecayAndGrowth = true;

	@Config
	@Label(name = "Starting season", description = "Has no effect if 'Serene Seasons changes' is disabled")
	public static Season.SubSeason startingSeason = Season.SubSeason.EARLY_SUMMER;

	@Config(min = 0)
	@Label(name = "Time Control day night duration", description = "How many minutes will day and night duration be (with this set to 10 the day will last 10 minutes and the night 10 minutes for a grand total of 20 minutes). This also adjusts the day_duration config option in Serene Seasons. Vanilla is 10. Requires Time Control mod")
	public static Double timeControlDayNightDuration = 15d;

	@Config(min = 0)
	@Label(name = "Time Control day night shift", description = "How many minutes will day and night duration be shifted based off seasons? E.g. in Mid spring / autumn the duration of day and night is vanilla, when moving off those seasons day and night will last this many minutes more/less. In mid summer / winter the duration of day and night duration will be more / less by 3 times this value. Set to 0 to disable. Requires Time Control mod")
	public static Double timeControlDayNightShift = 1.5d;

	@Config
	@Label(name = "Season based fishing time")
	public static Boolean seasonBasedFishingTime = true;

	public Seasons(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
		InsaneSurvivalOverhaul.addServerPack("serene_seasons_changes", "Insane's Survival Overhaul Serene Seasons Changes", () -> this.isEnabled() && !DataPacks.disableAllDataPacks && noGreenHouseGlass);
		InsaneSurvivalOverhaul.addServerPack("no_saplings_in_winter", "Insane's Survival Overhaul No Saplings in Winter", () -> this.isEnabled() && !DataPacks.disableAllDataPacks && noSaplingsInWinter);
	}

	@Override
	public void readConfig(ModConfigEvent event) {
		super.readConfig(event);

		if (this.isEnabled() && changeSereneSeasonsConfig)
			ModConfig.fertility.set("general.seasonal_crops", false);
	}

	@Override
	public boolean isEnabled() {
		return super.isEnabled() && ModList.get().isLoaded("sereneseasons");
	}

	public static void onSeasonChanged(SeasonChangedEvent.Standard event) {
		if (!Feature.isEnabled(Seasons.class)
				|| timeControlDayNightShift == 0
				|| !ModConfig.seasons.isDimensionWhitelisted(event.getLevel().dimension()))
			return;

		if (ModList.get().isLoaded("timecontrol"))
			TimeControlIntegration.updateDayNightLength(event.getNewSeason());
	}

	@SubscribeEvent
	public void onServerStart(ServerStartedEvent event) {
		if (changeSereneSeasonsConfig) {
			//ServerConfig.startingSubSeason.set(startingSeason.ordinal() + 1);
			ModConfig.seasons.set("time_settings.progress_season_while_offline", false);
		}
		if (ModList.get().isLoaded("timecontrol") && timeControlDayNightDuration != 10) {
			ModConfig.seasons.set("time_settings.day_duration", (int) (timeControlDayNightDuration * 60 * 20 * 2d));
			//I must set it on the field too otherwise when setting the starting seasons still uses the default 24000 duration
			ModConfig.seasons.dayDuration = (int) (timeControlDayNightDuration * 60 * 20 * 2d);
		}
	}

	@SubscribeEvent
	public void onPreLevelTick(TickEvent.LevelTickEvent event) {
		if (!event.level.isClientSide && event.level.getGameTime() == 0 && changeSereneSeasonsConfig) {
			SeasonSavedData seasonData = SeasonHandler.getSeasonSavedData(event.level);
			seasonData.seasonCycleTicks = SeasonTime.ZERO.getSubSeasonDuration() * startingSeason.ordinal();
			seasonData.setDirty();
			SeasonHandler.sendSeasonUpdate(event.level);

			//Force TimeControl update
			if (ModList.get().isLoaded("timecontrol"))
				TimeControlIntegration.updateDayNightLength(startingSeason);
		}
	}

	@SubscribeEvent
	public void onLevelTick(TickEvent.LevelTickEvent event) {
        if (!this.isEnabled()
				|| !grassDecayAndGrowth
				|| event.phase != TickEvent.Phase.END
				|| event.side != LogicalSide.SERVER
				|| !event.level.getGameRules().getBoolean(RULE_SEASONGRASSGROWDEATH))
            return;

        Season.SubSeason subSeason = SeasonHelper.getSeasonState(event.level).getSubSeason();
		Optional<GrassTickingData> data = GrassTickingData.get(subSeason);
		if (data.isEmpty() || !data.get().shouldTick(event.level))
			return;

        ServerLevel level = (ServerLevel)event.level;
		level.getProfiler().push("tallGrassRandomTick");
        ChunkMap chunkMap = level.getChunkSource().chunkMap;
        DistanceManager distanceManager = chunkMap.getDistanceManager();
        int naturalSpawnChunkCount = distanceManager.getNaturalSpawnChunkCount();
        List<ChunkAndHolder> list = Lists.newArrayListWithCapacity(naturalSpawnChunkCount);
        chunkMap.getChunks().forEach(chunkHolder -> {
            LevelChunk levelChunk = chunkHolder.getTickingChunk();
            if (levelChunk != null)
				list.add(new ChunkAndHolder(levelChunk, chunkHolder));
        });

        Collections.shuffle(list);
		for (ChunkAndHolder chunkAndHolder : list) {
			ChunkPos chunkPos = chunkAndHolder.chunk.getPos();
			if (level.shouldTickBlocksAt(chunkPos.toLong()) && (chunkMap.anyPlayerCloseEnoughForSpawning(chunkPos) || distanceManager.shouldForceTicks(chunkPos.toLong()))) {
				tickPlantsLifeDeath(chunkMap, chunkAndHolder.chunk, data.get());
			}
		}
		level.getProfiler().pop();
    }

	private static void tickPlantsLifeDeath(ChunkMap chunkMap, LevelChunk levelChunk, GrassTickingData data) {
		ServerLevel level = chunkMap.level;
		ChunkPos chunkpos = levelChunk.getPos();
		int x = chunkpos.getMinBlockX();
		int z = chunkpos.getMinBlockZ();

		LevelChunkSection[] levelChunkSections = levelChunk.getSections();
		for (int s = 0; s < levelChunkSections.length; s++) {
			LevelChunkSection levelchunksection = levelChunkSections[s];
			if (levelchunksection.isRandomlyTicking()) {
				int sectionY = levelChunk.getSectionYFromSectionIndex(s);
				int y = SectionPos.sectionToBlockCoord(sectionY);

				int randomTickSpeed = level.getGameRules().getInt(GameRules.RULE_RANDOMTICKING);
				for (int t = 0; t < randomTickSpeed; t++) {
					BlockPos pos = level.getBlockRandomPos(x, y, z, 15);
					BlockState state = levelchunksection.getBlockState(pos.getX() - x, pos.getY() - y, pos.getZ() - z);
					BlockPos abovePos = pos.above();
					if (level.getBrightness(LightLayer.SKY, abovePos) < data.lightLevel)
						continue;
					if (state.is(BlockTags.DIRT)) {
						BlockState stateUp = level.getBlockState(abovePos);
						if (level.getRandom().nextInt(data.chance) == 0) {
							if (stateUp.is(PLANTS_TO_DECAY))
								level.setBlock(abovePos, Blocks.AIR.defaultBlockState(), 3);
							else if (stateUp.is(PLANTS_TO_DEAD_BUSH))
								level.setBlock(abovePos, Blocks.DEAD_BUSH.defaultBlockState(), 3);
						}
						else if (state.is(Blocks.GRASS_BLOCK) && level.getRandom().nextInt(data.chance) == 0 && stateUp.isAir()) {
							Optional<Holder.Reference<PlacedFeature>> oPlacedFeature = level.registryAccess().registryOrThrow(Registries.PLACED_FEATURE).getHolder(VegetationPlacements.GRASS_BONEMEAL);
							oPlacedFeature.ifPresent(placedFeatureReference ->
									placedFeatureReference.value().place(level, level.getChunkSource().getGenerator(), level.random, abovePos));
						}
					}
					else if (data.canGrowTall && level.getRandom().nextInt(data.chance) == 0) {
						if (state.is(Blocks.GRASS))
							DoublePlantBlock.placeAt(level, Blocks.TALL_GRASS.defaultBlockState(), pos, 2);
						else if (state.is(Blocks.FERN))
							DoublePlantBlock.placeAt(level, Blocks.LARGE_FERN.defaultBlockState(), pos, 2);
					}
				}
			}
		}
	}

	public static void tickPlantLifeDeath(BlockState state, BlockPos pos, ServerLevel level) {
		Season.SubSeason subSeason = SeasonHelper.getSeasonState(level).getSubSeason();
		Optional<GrassTickingData> oData = GrassTickingData.get(subSeason);
		if (oData.isEmpty() || !oData.get().shouldTick(level))
			return;
		GrassTickingData data = oData.get();

		BlockPos abovePos = pos.above();
		if (level.getBrightness(LightLayer.SKY, abovePos) < data.lightLevel)
			return;
		if (state.is(BlockTags.DIRT)) {
			BlockState stateUp = level.getBlockState(abovePos);
			if (level.getRandom().nextInt(data.chance) == 0) {
				if (stateUp.is(PLANTS_TO_DECAY))
					level.setBlock(abovePos, Blocks.AIR.defaultBlockState(), 3);
				else if (stateUp.is(PLANTS_TO_DEAD_BUSH))
					level.setBlock(abovePos, Blocks.DEAD_BUSH.defaultBlockState(), 3);
			}
			else if (state.is(Blocks.GRASS_BLOCK) && level.getRandom().nextInt(data.chance) == 0 && stateUp.isAir()) {
				Optional<Holder.Reference<PlacedFeature>> oPlacedFeature = level.registryAccess().registryOrThrow(Registries.PLACED_FEATURE).getHolder(VegetationPlacements.GRASS_BONEMEAL);
				oPlacedFeature.ifPresent(placedFeatureReference ->
						placedFeatureReference.value().place(level, level.getChunkSource().getGenerator(), level.random, abovePos));
			}
		}
		else if (data.canGrowTall && level.getRandom().nextInt(data.chance) == 0) {
			if (state.is(Blocks.GRASS))
				DoublePlantBlock.placeAt(level, Blocks.TALL_GRASS.defaultBlockState(), pos, 2);
			else if (state.is(Blocks.FERN))
				DoublePlantBlock.placeAt(level, Blocks.LARGE_FERN.defaultBlockState(), pos, 2);
		}
	}

	record ChunkAndHolder(LevelChunk chunk, ChunkHolder holder) {}

	public static final List<GrassTickingData> GRASS_TICKING_DATA = List.of(
			new GrassTickingData(Season.SubSeason.EARLY_SPRING, 1100, GrassTickingType.GROW, 12, false),
			new GrassTickingData(Season.SubSeason.MID_SPRING, 1100, GrassTickingType.GROW, 12, false),
			new GrassTickingData(Season.SubSeason.LATE_SPRING, 550, GrassTickingType.GROW, 10, false),
			new GrassTickingData(Season.SubSeason.EARLY_SUMMER, 275, GrassTickingType.GROW, 8, true),
			new GrassTickingData(Season.SubSeason.MID_SUMMER, 275, GrassTickingType.GROW, 7, true),
			new GrassTickingData(Season.SubSeason.LATE_SUMMER, 275, GrassTickingType.GROW, 7, true),
			new GrassTickingData(Season.SubSeason.EARLY_AUTUMN, 2000, GrassTickingType.DECAY, 0, false),
			new GrassTickingData(Season.SubSeason.MID_AUTUMN, 1000, GrassTickingType.DECAY, 0, false),
			new GrassTickingData(Season.SubSeason.LATE_AUTUMN, 500, GrassTickingType.DECAY, 0, false),
			new GrassTickingData(Season.SubSeason.EARLY_WINTER, 200, GrassTickingType.DECAY, 0, false),
			new GrassTickingData(Season.SubSeason.MID_WINTER, 100, GrassTickingType.DECAY, 0, false),
			new GrassTickingData(Season.SubSeason.LATE_WINTER, 400, GrassTickingType.DECAY, 0, false)
	);

	public record GrassTickingData(Season.SubSeason subSeason, int chance, GrassTickingType grassTickingType, int lightLevel, boolean canGrowTall) {
		public boolean shouldTick(Level level) {
			return chance > 0 && (grassTickingType == GrassTickingType.DECAY || level.isDay());
		}

		public static Optional<GrassTickingData> get(Season.SubSeason subSeason) {
			for (GrassTickingData grassTickingData : GRASS_TICKING_DATA) {
				if (grassTickingData.subSeason == subSeason)
					return Optional.of(grassTickingData);
			}
			return Optional.empty();
		}
	}
	public enum GrassTickingType {
		GROW,
		DECAY
	}

	@SubscribeEvent
	public void shouldSlowdownFishing(HookTickToHookLureEvent event) {
		if (!Feature.isEnabled(Seasons.class)
				|| !seasonBasedFishingTime)
			return;

		Level level = event.getHookEntity().level();
		Season season = SeasonHelper.getSeasonState(level).getSeason();
		//Chance to slowdown fishing
		float rng = switch (season) {
			case SPRING -> 0.1F;
			case SUMMER -> 0.0F;
			case AUTUMN -> 0.2F;
			case WINTER -> 0.5F;
		};
		if (level.getRandom().nextFloat() < rng)
			event.setTick(event.getTick() - 1);
	}

	public static float getDayNightCycleModifier() {
		return Feature.isEnabled(Seasons.class) && ModList.get().isLoaded("timecontrol") ? timeControlDayNightDuration.floatValue() / 10f : 1f;
	}

	public static float getDayDuration() {
		return Feature.isEnabled(Seasons.class) && ModList.get().isLoaded("timecontrol") ? timeControlDayNightDuration.floatValue() * 2f : 20f;
	}
}