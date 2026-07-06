package insane96mcp.insanesurvivaloverhaul.module.mining.anvilcrafting;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.JsonOps;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = InsaneSO.MOD_ID)
public class AnvilRecipeReloadListener extends SimpleJsonResourceReloadListener {
	public static List<AnvilRecipe> RECIPES = new ArrayList<>();
	public static final AnvilRecipeReloadListener INSTANCE;
	private static final Gson GSON = new GsonBuilder().create();
	public AnvilRecipeReloadListener() {
		super(GSON, "anvil_recipe");
	}

	static {
		INSTANCE = new AnvilRecipeReloadListener();
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
		List<AnvilRecipe> list = new ArrayList<>();
		for (var entry : map.entrySet()) {
			try {
				ResourceLocation name = entry.getKey();
				String[] split = name.getPath().split("/");
				if (split[split.length - 1].startsWith("_"))
					continue;

				if (!ICondition.conditionsMatched(JsonOps.INSTANCE, entry.getValue()))
					continue;

				AnvilRecipe anvilRecipe = GSON.fromJson(entry.getValue(), AnvilRecipe.class);
				list.add(anvilRecipe);
			} catch (JsonSyntaxException e) {
				InsaneSO.LOGGER.error("Parsing error loading Anvil Recipe {}: {}", entry.getKey(), e.getMessage());
			} catch (Exception e) {
				InsaneSO.LOGGER.error("Failed loading Anvil Recipe {}: {}", entry.getKey(), e.getMessage());
			}
		}
		RECIPES = list;

		InsaneSO.LOGGER.info("Loaded {} Anvil Recipe", RECIPES.size());
	}

	@SubscribeEvent
	public static void onDataPackSync(OnDatapackSyncEvent event) {
		if (event.getPlayer() == null)
			event.getPlayerList().getPlayers().forEach(player -> ClientboundAnvilRecipeSyncPacket.sync(RECIPES, player));
		else
			ClientboundAnvilRecipeSyncPacket.sync(RECIPES, event.getPlayer());
	}

	@Override
	public @NotNull String getName() {
		return "Anvil Recipe Listener";
	}
}
