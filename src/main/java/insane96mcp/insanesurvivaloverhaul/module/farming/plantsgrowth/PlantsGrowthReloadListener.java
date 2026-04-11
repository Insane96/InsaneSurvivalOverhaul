package insane96mcp.insanesurvivaloverhaul.module.farming.plantsgrowth;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PlantsGrowthReloadListener extends SimpleJsonResourceReloadListener {
	public static List<PlantGrowthMultiplier> GROWTH_MULTIPLIERS = new ArrayList<>();
	public static final PlantsGrowthReloadListener INSTANCE;
	private static final Gson GSON = new GsonBuilder().create();
	public PlantsGrowthReloadListener() {
		super(GSON, "plant_growth_modifiers");
	}

	static {
		INSTANCE = new PlantsGrowthReloadListener();
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profiler) {
		GROWTH_MULTIPLIERS.clear();
		for (var entry : map.entrySet()) {
			try {
				ResourceLocation name = entry.getKey();
				String[] split = name.getPath().split("/");
				if (split[split.length - 1].startsWith("_"))
					continue;

				PlantGrowthMultiplier plantGrowthMultiplier = GSON.fromJson(entry.getValue(), PlantGrowthMultiplier.class);
				GROWTH_MULTIPLIERS.add(plantGrowthMultiplier);
			}
			catch (JsonSyntaxException e) {
				InsaneSO.LOGGER.error("Parsing error loading Plant Growth Multipliers {}: {}", entry.getKey(), e.getMessage());
			}
			catch (Exception e) {
				InsaneSO.LOGGER.error("Failed loading Plant Growth Multipliers {}: {}", entry.getKey(), e.getMessage());
			}
		}

		InsaneSO.LOGGER.info("Loaded {} Plant Growth Multipliers", GROWTH_MULTIPLIERS.size());
	}

	@Override
	public @NotNull String getName() {
		return "Plants Growth Reload Listener";
	}
}
