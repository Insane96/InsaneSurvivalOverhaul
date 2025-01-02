package insane96mcp.iguanatweaksreborn.module.mining.blockdefinition;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import insane96mcp.iguanatweaksreborn.InsaneSurvivalTweaks;
import insane96mcp.iguanatweaksreborn.network.message.BlockDefinitionSync;
import insane96mcp.iguanatweaksreborn.utils.ITRLogHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = InsaneSurvivalTweaks.MOD_ID)
public class BlockDefinitionReloadListener extends SimpleJsonResourceReloadListener {
	public static final List<BlockDefinition> DEFINITIONS = new ArrayList<>();
	public static final List<BlockDefinition> ORIGINAL_DEFINITIONS = new ArrayList<>();
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
		//Restore original data
		restoreOriginalDefinitionsAndClear();
		//Load new data
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
					return;
				//blockData.apply(false);
				DEFINITIONS.add(blockDefinition);
			}
			catch (JsonSyntaxException e) {
				ITRLogHelper.error("Parsing error loading Block Definition %s: %s", entry.getKey(), e.getMessage());
			}
			catch (Exception e) {
				ITRLogHelper.error("Failed loading Block Definition %s: %s", entry.getKey(), e.getMessage());
			}
		}

		ITRLogHelper.info("Loaded %s Block Data", DEFINITIONS.size());
	}

	public static void restoreOriginalDefinitionsAndClear() {
		for (BlockDefinition definition : ORIGINAL_DEFINITIONS)
			definition.apply(true);
		ITRLogHelper.info("Restored %s Block Definitions", ORIGINAL_DEFINITIONS.size());
		ORIGINAL_DEFINITIONS.clear();
	}

	@SubscribeEvent
	public static void onDataPackSync(OnDatapackSyncEvent event) {
		if (event.getPlayer() == null) {
			event.getPlayerList().getPlayers().forEach(player -> BlockDefinitionSync.sync(DEFINITIONS, player));
		}
		else {
			BlockDefinitionSync.sync(DEFINITIONS, event.getPlayer());
		}
	}

	@SubscribeEvent
	public static void onTagsUpdatedEvent(TagsUpdatedEvent event) {
		for (BlockDefinition definition : BlockDefinitionReloadListener.DEFINITIONS) {
			definition.apply(false);
		}
	}

	@Override
	public String getName() {
		return "Block Definitions Reload Listener";
	}
}
