package insane96mcp.iguanatweaksreborn.module.farming.plantsgrowth;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import insane96mcp.iguanatweaksreborn.InsaneSurvivalOverhaul;
import insane96mcp.iguanatweaksreborn.utils.ISOLogHelper;
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
	protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
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
				ISOLogHelper.error("Parsing error loading Plant Growth Multipliers %s: %s", entry.getKey(), e.getMessage());
			}
			catch (Exception e) {
				ISOLogHelper.error("Failed loading Plant Growth Multipliers %s: %s", entry.getKey(), e.getMessage());
			}
		}

		ISOLogHelper.info("Loaded %s Plant Growth Multipliers", GROWTH_MULTIPLIERS.size());
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
		return "Plants Growth Reload Listener";
	}
}
