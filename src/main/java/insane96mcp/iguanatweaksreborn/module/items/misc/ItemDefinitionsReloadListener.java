package insane96mcp.iguanatweaksreborn.module.items.misc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import insane96mcp.iguanatweaksreborn.InsaneSurvivalOverhaul;
import insane96mcp.iguanatweaksreborn.network.message.ItemDefinitionsSync;
import insane96mcp.iguanatweaksreborn.utils.ISOLogHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = InsaneSurvivalOverhaul.MOD_ID)
public class ItemDefinitionsReloadListener extends SimpleJsonResourceReloadListener {
	private static final List<ItemDefinition> DEFINITIONS = new ArrayList<>();
	public static final Map<Item, ItemDefinition.Durability> DURABILITY_MAP = new HashMap<>();
	public static final Map<Item, Integer> ORIGINAL_DURABILITY = new HashMap<>();
	public static final ItemDefinitionsReloadListener INSTANCE;
	private static final Gson GSON = new GsonBuilder().create();
	public ItemDefinitionsReloadListener() {
		super(GSON, "item_definitions");
	}

	static {
		INSTANCE = new ItemDefinitionsReloadListener();
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
		getDefinitions().clear();
		DURABILITY_MAP.clear();
		for (var entry : map.entrySet()) {
			try {
				ResourceLocation name = entry.getKey();
				String[] split = name.getPath().split("/");
				if (split[split.length - 1].startsWith("_"))
					continue;

				ItemDefinition itemDefinition = GSON.fromJson(entry.getValue(), ItemDefinition.class);
				//itemStatistics.applyStats(false);
				getDefinitions().add(itemDefinition);
			}
			catch (JsonSyntaxException e) {
				ISOLogHelper.error("Parsing error loading Item Definition %s: %s", entry.getKey(), e.getMessage());
			}
			catch (Exception e) {
				ISOLogHelper.error("Failed loading Item Definition %s: %s", entry.getKey(), e.getMessage());
			}
		}

		ISOLogHelper.info("Loaded %s Item Definition", getDefinitions().size());

		/*for (var entry : Durability.entrySet()) {
			entry.getValue().apply(entry.getKey());
		}*/
	}

	@SubscribeEvent
	public static void onDataPackSync(OnDatapackSyncEvent event) {
		if (event.getPlayer() == null) {
			event.getPlayerList().getPlayers().forEach(player -> ItemDefinitionsSync.sync(DEFINITIONS, player));
		}
		else {
			ItemDefinitionsSync.sync(DEFINITIONS, event.getPlayer());
		}
	}


	@SubscribeEvent
	public static void onTagsUpdatedEvent(TagsUpdatedEvent event) {
		for (ItemDefinition itemDefinition : ItemDefinitionsReloadListener.DEFINITIONS) {
			itemDefinition.applyStats(event.getUpdateCause() == TagsUpdatedEvent.UpdateCause.CLIENT_PACKET_RECEIVED);
		}
		ISOLogHelper.info("Applied %s Item Definitions (Client side: %s)", DEFINITIONS.size(), event.getUpdateCause() == TagsUpdatedEvent.UpdateCause.CLIENT_PACKET_RECEIVED);
		for (var entry : DURABILITY_MAP.entrySet()) {
			entry.getValue().apply(entry.getKey());
		}
	}

	public synchronized static List<ItemDefinition> getDefinitions() {
		return DEFINITIONS;
	}

	@Override
	public String getName() {
		return "Item Definitions Reload Listener";
	}
}
