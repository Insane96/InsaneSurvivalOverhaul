package insane96mcp.insanesurvivaloverhaul.module.mining.blockdefinition;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = InsaneSO.MOD_ID)
public class BlockDefinitionReloadListener extends SimpleJsonResourceReloadListener {
	public static final List<BlockDefinition> DEFINITIONS = new ArrayList<>();
	public static final BlockDefinitionReloadListener INSTANCE;
	private static final Gson GSON = new GsonBuilder().create();
	public BlockDefinitionReloadListener() {
		super(GSON, "block_definitions");
	}

	static {
		INSTANCE = new BlockDefinitionReloadListener();
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
		//Definitions are applied in onTagsUpdatedEvent, as tags are not bound yet
		DEFINITIONS.clear();
		for (var entry : map.entrySet()) {
			try {
				ResourceLocation name = entry.getKey();
				String[] split = name.getPath().split("/");
				if (split[split.length - 1].startsWith("_"))
					continue;

				BlockDefinition blockDefinition = GSON.fromJson(entry.getValue(), BlockDefinition.class);
				//Serializer can return null in case the block doesn't exist (e.g. from other optional mods)
				if (blockDefinition == null)
					continue;
				//blockData.apply(false);
				DEFINITIONS.add(blockDefinition);
			}
			catch (JsonSyntaxException e) {
				InsaneSO.LOGGER.error("Parsing error loading Block Definition {}: {}", entry.getKey(), e.getMessage());
			}
			catch (Exception e) {
				InsaneSO.LOGGER.error("Failed loading Block Definition {}: {}", entry.getKey(), e.getMessage());
			}
		}

		InsaneSO.LOGGER.info("Loaded {} Block Definitions", DEFINITIONS.size());
	}

	/**
	 * Restores the original block values and applies the current definitions
	 */
	public static void reapplyDefinitions() {
		BlockDefinition.restoreOriginals();
		for (BlockDefinition definition : DEFINITIONS)
			definition.apply();
	}

	@SubscribeEvent
	public static void onDataPackSync(OnDatapackSyncEvent event) {
		if (event.getPlayer() == null) {
			event.getPlayerList().getPlayers().forEach(player -> ClientboundBlockDefinitionPacket.sync(DEFINITIONS, player));
		}
		else {
			ClientboundBlockDefinitionPacket.sync(DEFINITIONS, event.getPlayer());
		}
	}

	@SubscribeEvent
	public static void onTagsUpdatedEvent(TagsUpdatedEvent event) {
		//In single player blocks are shared between client and server, so only the server updates them
		if (!event.shouldUpdateStaticData())
			return;
		//On a dedicated client this re-applies the definitions received from the server, as tag based ones might have changed
		reapplyDefinitions();
	}

	@Override
	public String getName() {
		return "Block Definitions Reload Listener";
	}
}
