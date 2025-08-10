package insane96mcp.iguanatweaksreborn.module.world.seasons;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import insane96mcp.iguanatweaksreborn.InsaneSO;
import insane96mcp.iguanatweaksreborn.data.generator.ISOBlockTagsProvider;
import insane96mcp.iguanatweaksreborn.event.HookTickToHookLureEvent;
import insane96mcp.iguanatweaksreborn.event.TideHookTickToHookLureEvent;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.module.misc.DataPacks;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.JsonFeature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import sereneseasons.api.season.Season;
import sereneseasons.api.season.SeasonChangedEvent;
import sereneseasons.api.season.SeasonHelper;
import sereneseasons.init.ModConfig;
import sereneseasons.season.SeasonHandler;
import sereneseasons.season.SeasonSavedData;
import sereneseasons.season.SeasonTime;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@LoadFeature(module = Modules.Ids.WORLD, requiresMods = {"sereneseasons"}, description = "Change a few things relative to Serene Seasons. Grass and tall grass decays in Winter and regrows back in Spring. Saplings are also transformed into Dead Bushes. This can be disabled with the iguanatweaks:doSeasonGrassGrowDeath game rule", enabledByDefault = false)
public class Seasons extends JsonFeature {

	public static final GameRules.Key<GameRules.BooleanValue> RULE_SEASONGRASSGROWDEATH = GameRules.register("iguanatweaks:doSeasonGrassGrowDeath", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true));

	public static final TagKey<Block> PLANTS_TO_DECAY = ISOBlockTagsProvider.create("plants_to_decay");
	public static final TagKey<Block> PLANTS_TO_DEAD_BUSH = ISOBlockTagsProvider.create("plants_to_dead_bush");

	@Config(description = """
			Makes the following changes to Serene Seasons config:
			* seasonal_crops is set to false, as it's controlled by Plants Growth
			* Sets the starting season to the one in 'Starting season'""")
	public static Boolean changeSereneSeasonsConfig = true;

	@Config(description = "Removes greenhouse glass.")
	public static Boolean noGreenHouseGlass = true;

	@Config(description = """
            Changes what leaves can drop in different seasons.
            Sapling no longer drop in winter
            Apples only drop in spring""")
	public static Boolean leavesDrops = true;

	@Config(description = "Has no effect if 'Serene Seasons changes' is disabled")
	public static Season.SubSeason startingSeason = Season.SubSeason.EARLY_SUMMER;

	@Config(min = 0, description = "How many minutes will day and night duration be (with this set to 10 the day will last 10 minutes and the night 10 minutes for a grand total of 20 minutes). This also adjusts the day_duration config option in Serene Seasons. Vanilla is 10. Requires Time Control mod")
	public static Double timeControlDayNightDuration = 15d;

	@Config(min = 0, description = "How many minutes will day and night duration be shifted based off seasons? E.g. in Mid spring / autumn the duration of day and night is vanilla, when moving off those seasons day and night will last this many minutes more/less. In mid summer / winter the duration of day and night duration will be more / less by 3 times this value. Set to 0 to disable. Requires Time Control mod")
	public static Double timeControlDayNightShift = 1.5d;
	@Config
	public static Boolean slowdownGrassSpreading = true;

	@Config
	public static Boolean seasonBasedFishingTime = true;

	@Config
	public static Boolean growDoubleTallGrass = true;
	@Config
	public static Boolean growFlowersFromGrass = false;

	public static final ArrayList<GrassTickingData> GRASS_GROWTH_DECAY_DEFAULT = new ArrayList<>(List.of(
			new GrassTickingData(Season.SubSeason.EARLY_SPRING, 1000, GrassTickingType.GROW, 12, false, false),
			new GrassTickingData(Season.SubSeason.MID_SPRING, 1000, GrassTickingType.GROW, 12, false, true),
			new GrassTickingData(Season.SubSeason.LATE_SPRING, 1000, GrassTickingType.GROW, 12, false, true),
			new GrassTickingData(Season.SubSeason.EARLY_SUMMER, 2000, GrassTickingType.GROW, 10, true, true),
			new GrassTickingData(Season.SubSeason.MID_SUMMER, 2000, GrassTickingType.GROW, 8, true, false),
			new GrassTickingData(Season.SubSeason.LATE_SUMMER, 2000, GrassTickingType.GROW, 7, true, false),
			new GrassTickingData(Season.SubSeason.EARLY_AUTUMN, 2000, GrassTickingType.DECAY, 0, false, false),
			new GrassTickingData(Season.SubSeason.MID_AUTUMN, 1000, GrassTickingType.DECAY, 0, false, false),
			new GrassTickingData(Season.SubSeason.LATE_AUTUMN, 500, GrassTickingType.DECAY, 0, false, false),
			new GrassTickingData(Season.SubSeason.EARLY_WINTER, 200, GrassTickingType.DECAY, 0, false, false),
			new GrassTickingData(Season.SubSeason.MID_WINTER, 100, GrassTickingType.DECAY, 0, false, false),
			new GrassTickingData(Season.SubSeason.LATE_WINTER, 400, GrassTickingType.DECAY, 0, false, false)
	));
	public static final ArrayList<GrassTickingData> grassGrowthDecay = new ArrayList<>();

	public Seasons(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
		InsaneSO.addServerPack("serene_seasons_changes", "Insane's Survival Overhaul Serene Seasons Changes", () -> this.isEnabled() && !DataPacks.disableAllDataPacks && noGreenHouseGlass);
		InsaneSO.addServerPack("leaves_drops", "Insane's Survival Overhaul Leaves Drops", () -> this.isEnabled() && !DataPacks.disableAllDataPacks && leavesDrops);
		if (ModList.get().isLoaded("tide"))
			MinecraftForge.EVENT_BUS.addListener(Seasons::shouldTideSlowdownFishing);
		JSON_CONFIGS.add(new JsonConfig<>("grass_growth_decay.json", grassGrowthDecay, GRASS_GROWTH_DECAY_DEFAULT, GrassTickingData.LIST_TYPE));
	}

	@Override
	public String getModConfigFolder() {
		return InsaneSO.CONFIG_FOLDER;
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

	public static boolean shouldSlowdownGrassSpreading(LevelReader level) {
		if (!Feature.isEnabled(Seasons.class)
                || !slowdownGrassSpreading)
            return false;

        Season season = SeasonHelper.getSeasonState((Level) level).getSeason();
        double failChance = switch (season) {
            case SPRING, SUMMER -> 0d;
            case AUTUMN -> 0.3d;
            case WINTER -> 0.75d;
        };
        return ((Level) level).random.nextDouble() < failChance;
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

	public static void tickPlantLifeDeath(BlockState state, BlockPos pos, ServerLevel level) {
		Season.SubSeason subSeason = SeasonHelper.getSeasonState(level).getSubSeason();
		Optional<GrassTickingData> oData = GrassTickingData.get(subSeason);
		if (oData.isEmpty() || !oData.get().shouldTick(level))
			return;
		GrassTickingData data = oData.get();

		if (level.getBrightness(LightLayer.SKY, pos) < data.lightLevel)
			return;
		if (data.grassTickingType == GrassTickingType.DECAY
				&& (state.is(PLANTS_TO_DECAY) || state.is(PLANTS_TO_DEAD_BUSH))
				&& level.getRandom().nextInt(data.chance) == 0) {
			if (state.is(PLANTS_TO_DECAY))
				level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
			else if (state.is(PLANTS_TO_DEAD_BUSH))
				level.setBlock(pos, Blocks.DEAD_BUSH.defaultBlockState(), 3);
		}
		else if (data.grassTickingType == GrassTickingType.GROW) {
			if (state.isAir()
					&& level.getBlockState(pos.below()).is(Blocks.GRASS_BLOCK)
					&& level.getRandom().nextInt(data.chance) == 0) {
				Optional<Holder.Reference<PlacedFeature>> oPlacedFeature = level.registryAccess().registryOrThrow(Registries.PLACED_FEATURE).getHolder(VegetationPlacements.GRASS_BONEMEAL);
				oPlacedFeature.ifPresent(placedFeatureReference ->
						placedFeatureReference.value().place(level, level.getChunkSource().getGenerator(), level.random, pos));
			}
			else if (state.is(Blocks.GRASS) || state.is(Blocks.FERN)) {
				if (growDoubleTallGrass && data.canGrowTall
						&& level.getBlockState(pos.above()).canBeReplaced()
						&& level.getRandom().nextInt(data.chance) == 0) {
					if (state.is(Blocks.GRASS))
						DoublePlantBlock.placeAt(level, Blocks.TALL_GRASS.defaultBlockState(), pos, 3);
					else if (state.is(Blocks.FERN))
						DoublePlantBlock.placeAt(level, Blocks.LARGE_FERN.defaultBlockState(), pos, 3);
				}
				else if (growFlowersFromGrass && data.canGrowFlower
						&& level.getRandom().nextInt(data.chance * 10) == 0) {
					List<ConfiguredFeature<?, ?>> list = level.getBiome(pos).value().getGenerationSettings().getFlowerFeatures();
					if (!list.isEmpty()) {
						Holder<PlacedFeature> holder = ((RandomPatchConfiguration)list.get(0).config()).feature();
						level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
						holder.value().place(level, level.getChunkSource().getGenerator(), level.random, pos);
					}
				}
			}
		}
	}

	@JsonAdapter(GrassTickingData.Serializer.class)
	public record GrassTickingData(Season.SubSeason subSeason, int chance, GrassTickingType grassTickingType, int lightLevel, boolean canGrowTall, boolean canGrowFlower) {
		public static final Type LIST_TYPE = new TypeToken<ArrayList<GrassTickingData>>(){}.getType();

		public boolean shouldTick(Level level) {
			return chance > 0 && (grassTickingType == GrassTickingType.DECAY || level.isDay());
		}

		public static Optional<GrassTickingData> get(Season.SubSeason subSeason) {
			for (GrassTickingData grassTickingData : grassGrowthDecay) {
				if (grassTickingData.subSeason == subSeason)
					return Optional.of(grassTickingData);
			}
			return Optional.empty();
		}

		public static class Serializer implements JsonDeserializer<GrassTickingData>, JsonSerializer<GrassTickingData> {
			@Override
			public GrassTickingData deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
				JsonObject jObject = json.getAsJsonObject();
				String subSeasonStr = jObject.get("sub_season").getAsString().toUpperCase();
				Season.SubSeason subSeason = null;
				try {
					subSeason = Season.SubSeason.valueOf(subSeasonStr);
				} catch (IllegalArgumentException e) {
					throw new JsonParseException("Unknown sub_season: " + subSeasonStr, e);
				}

				int chance = GsonHelper.getAsInt(jObject, "chance");
				GrassTickingType grassTickingType = context.deserialize(jObject.get("grass_ticking_type"), GrassTickingType.class);
				int lightLevel = GsonHelper.getAsInt(jObject, "light_level", 0);
				boolean canGrowTall = GsonHelper.getAsBoolean(jObject, "can_grow_tall", false);
				boolean canGrowFlower = GsonHelper.getAsBoolean(jObject, "can_grow_flower", false);
				return new GrassTickingData(subSeason, chance, grassTickingType, lightLevel, canGrowTall, canGrowFlower);
			}

			@Override
			public JsonElement serialize(GrassTickingData src, java.lang.reflect.Type typeOfSrc, JsonSerializationContext context) {
				JsonObject jsonObject = new JsonObject();
				jsonObject.addProperty("sub_season", src.subSeason.name().toLowerCase(Locale.ROOT));
				jsonObject.addProperty("chance", src.chance);
				jsonObject.add("grass_ticking_type", context.serialize(src.grassTickingType));
				if (src.lightLevel > 0)
					jsonObject.addProperty("light_level", src.lightLevel);
				if (src.canGrowTall)
					jsonObject.addProperty("can_grow_tall", true);
				if (src.canGrowFlower)
					jsonObject.addProperty("can_grow_flower", true);

				return jsonObject;
			}
		}
	}
	public enum GrassTickingType {
		@SerializedName("grow")
		GROW,
		@SerializedName("decay")
		DECAY
	}

	@SubscribeEvent
	public void shouldSlowdownFishing(HookTickToHookLureEvent event) {
		if (event.getType() != HookTickToHookLureEvent.Type.LURE)
			return;
		int slowdown = slowdownFishing(event.getHookEntity().level());
		event.setTick(event.getTick() + slowdown);
	}

	public static void shouldTideSlowdownFishing(TideHookTickToHookLureEvent event) {
		if (event.getType() != TideHookTickToHookLureEvent.Type.LURE)
			return;
		int slowdown = slowdownFishing(event.getHookEntity().level());
		event.setTick(event.getTick() + slowdown);
	}

	public static int slowdownFishing(Level level) {
		if (!Feature.isEnabled(Seasons.class)
				|| !seasonBasedFishingTime)
			return 0;

		Season season = SeasonHelper.getSeasonState(level).getSeason();
		//Ticks added to slowdown fishing
		return switch (season) {
			case SPRING -> 0;
			case SUMMER -> 200;
			case AUTUMN -> 100;
			case WINTER -> 600;
		};
	}

	public static float getDayNightCycleModifier() {
		return Feature.isEnabled(Seasons.class) && ModList.get().isLoaded("timecontrol") ? timeControlDayNightDuration.floatValue() / 10f : 1f;
	}

	public static float getDayDuration() {
		return Feature.isEnabled(Seasons.class) && ModList.get().isLoaded("timecontrol") ? timeControlDayNightDuration.floatValue() * 2f : 20f;
	}
}