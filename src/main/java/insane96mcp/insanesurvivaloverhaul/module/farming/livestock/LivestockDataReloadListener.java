package insane96mcp.insanesurvivaloverhaul.module.farming.livestock;

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

public class LivestockDataReloadListener extends SimpleJsonResourceReloadListener {
	public static List<LivestockData> LIVESTOCK_DATA;
	public static final LivestockDataReloadListener INSTANCE;
	private static final Gson GSON = new GsonBuilder().create();
	public LivestockDataReloadListener() {
		super(GSON, "livestock_data");
	}

	static {
		INSTANCE = new LivestockDataReloadListener();
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
		List<LivestockData> list = new ArrayList<>();
		for (var entry : map.entrySet()) {
			try {
				ResourceLocation name = entry.getKey();
				String[] split = name.getPath().split("/");
				if (split[split.length - 1].startsWith("_"))
					continue;

				LivestockData livestockData = GSON.fromJson(entry.getValue(), LivestockData.class);
				list.add(livestockData);
			} catch (JsonSyntaxException e) {
				InsaneSO.LOGGER.error("Parsing error loading Livestock Data {}: {}", entry.getKey(), e.getMessage());
			} catch (Exception e) {
				InsaneSO.LOGGER.error("Failed loading Livestock Data {}: {}", entry.getKey(), e.getMessage());
			}
		}
		LIVESTOCK_DATA = list;

		InsaneSO.LOGGER.info("Loaded {} Livestock Data", LIVESTOCK_DATA.size());
	}

	@Override
	public @NotNull String getName() {
		return "Livestock Data Listener";
	}
}
