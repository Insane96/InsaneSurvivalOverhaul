package insane96mcp.iguanatweaksreborn.module.experience.anvils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import insane96mcp.iguanatweaksreborn.InsaneSurvivalOverhaul;
import insane96mcp.iguanatweaksreborn.network.message.AnvilRepairSync;
import insane96mcp.iguanatweaksreborn.utils.ITRLogHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = InsaneSurvivalOverhaul.MOD_ID)
public class AnvilRepairReloadListener extends SimpleJsonResourceReloadListener {
	public static HashMap<ResourceLocation, AnvilRepair> REPAIRS = new HashMap<>();
	public static final AnvilRepairReloadListener INSTANCE;
	private static final Gson GSON = new GsonBuilder().create();
	public AnvilRepairReloadListener() {
		super(GSON, "anvil_repairs");
	}

	static {
		INSTANCE = new AnvilRepairReloadListener();
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
		REPAIRS.clear();
		for (var entry : map.entrySet()) {
			try {
				AnvilRepair anvilRepair = GSON.fromJson(entry.getValue(), AnvilRepair.class);
				REPAIRS.put(entry.getKey(), anvilRepair);
			}
			catch (JsonSyntaxException e) {
				ITRLogHelper.error("Parsing error loading Anvil Recipe %s: %s", entry.getKey(), e.getMessage());
			}
			catch (Exception e) {
				ITRLogHelper.error("Failed loading Anvil Recipe %s: %s", entry.getKey(), e.getMessage());
			}
		}

		ITRLogHelper.info("Loaded %s Anvil Recipes", REPAIRS.size());
	}

	@SubscribeEvent
	public static void onDataPackSync(OnDatapackSyncEvent event) {
		if (event.getPlayer() == null) {
			event.getPlayerList().getPlayers().forEach(player -> AnvilRepairSync.sync(REPAIRS, player));
		}
		else {
			AnvilRepairSync.sync(REPAIRS, event.getPlayer());
		}
	}

	@Override
	public String getName() {
		return "Anvil Repair Reload Listener";
	}
}
