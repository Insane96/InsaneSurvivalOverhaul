package insane96mcp.iguanatweaksreborn.module.farming.livestock;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import insane96mcp.iguanatweaksreborn.InsaneSurvivalOverhaul;
import insane96mcp.iguanatweaksreborn.utils.ITRLogHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = InsaneSurvivalOverhaul.MOD_ID)
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
				ITRLogHelper.error("Parsing error loading Livestock Data %s: %s", entry.getKey(), e.getMessage());
			} catch (Exception e) {
				ITRLogHelper.error("Failed loading Livestock Data %s: %s", entry.getKey(), e.getMessage());
			}
		}
		LIVESTOCK_DATA = list;

		ITRLogHelper.info("Loaded %s Livestock Data", LIVESTOCK_DATA.size());
	}

	/*@SubscribeEvent
	public static void onDataPackSync(OnDatapackSyncEvent event) {
		if (event.getPlayer() == null) {
			event.getPlayerList().getPlayers().forEach(player -> PlantGrowthMultiplierSync.sync(GROWTH_MULTIPLIERS, player));
		}
		else {
			PlantGrowthMultiplierSync.sync(GROWTH_MULTIPLIERS, event.getPlayer());
		}
	}*/

	@Override
	public @NotNull String getName() {
		return "Livestock Data Listener";
	}
}
