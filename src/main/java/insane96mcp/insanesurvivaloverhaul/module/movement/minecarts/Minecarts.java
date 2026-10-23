package insane96mcp.insanesurvivaloverhaul.module.movement.minecarts;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.module.misc.Packs;
import insane96mcp.insanesurvivaloverhaul.setup.SimpleBlockWithItem;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.AbstractMinecartContainer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent;
import net.neoforged.neoforge.common.world.chunk.TicketController;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@LoadFeature(module = ISOModules.MOVEMENT)
public class Minecarts extends Feature {

	public static final SimpleBlockWithItem GOLDEN_POWERED_RAIL = SimpleBlockWithItem.register("golden_powered_rail", () -> new ISOPoweredRail(BlockBehaviour.Properties.ofFullCopy(Blocks.POWERED_RAIL), 1f, 0.18f));
	public static final SimpleBlockWithItem COPPER_POWERED_RAIL = SimpleBlockWithItem.register("copper_powered_rail", () -> new ISOPoweredRail(BlockBehaviour.Properties.ofFullCopy(Blocks.POWERED_RAIL).sound(SoundType.COPPER), 0.4f, 0.05f));

	@Config(description = "If true, minecarts with entities in them and chest minecarts will force load chunks, allowing them to travel in unloaded chunks. It's advised to not leave too many minecarts with entities and with chests lying around, as they will keep chunks loaded.")
	public static Boolean entityAndChestMinecartsForceLoadChunks = true;

	@Config(description = "If true, enables a data pack that makes rails cheaper and adds recipes for new rail. Also adds a global loot modifier that replaces vanilla rails with golden powered rails")
	public static Boolean dataPack = true;

	/**
	 * Tickets are owned by the minecart's UUID, so they never interfere with vanilla's forced chunks (e.g. /forceload).
	 * NeoForge saves them, but the tracking map below is in memory only, so all of them are removed when the level loads.
	 */
	private static final TicketController TICKET_CONTROLLER = new TicketController(InsaneSO.id("minecarts"),
			(level, ticketHelper) -> new ArrayList<>(ticketHelper.getEntityTickets().keySet()).forEach(ticketHelper::removeAllTickets));

	/**
	 * Tracks minecarts that are currently keeping a chunk force-loaded, and the chunk they're forcing.
	 */
	private static final Map<ResourceKey<Level>, Map<UUID, Long>> forcingMinecarts = new HashMap<>();

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		InsaneSO.addServerPack("better_rails", "Insane's Survival Overhaul Better Rails", () -> this.isEnabled() && !Packs.disableAllDataPacks && dataPack);
	}

	public static void registerTicketControllers(RegisterTicketControllersEvent event) {
		event.register(TICKET_CONTROLLER);
	}

	private boolean qualifies(AbstractMinecart minecart) {
		return this.isEnabled()
				&& entityAndChestMinecartsForceLoadChunks
				&& (minecart.getFirstPassenger() != null || minecart instanceof AbstractMinecartContainer);
	}

	/**
	 * After the minecart ticks (moves), force the chunk it is now in if it qualifies, releasing the previous one.
	 * Tickets are only changed when the minecart changes chunk or stops qualifying.
	 */
	@SubscribeEvent
	public void onMinecartTickPost(EntityTickEvent.Post event) {
		if (!(event.getEntity().level() instanceof ServerLevel serverLevel)
				|| !(event.getEntity() instanceof AbstractMinecart minecart))
			return;

		Map<UUID, Long> dimMap = forcingMinecarts.computeIfAbsent(serverLevel.dimension(), k -> new HashMap<>());
		Long forcedChunk = dimMap.get(minecart.getUUID());
		if (!qualifies(minecart)) {
			if (forcedChunk != null) {
				unforceChunk(serverLevel, minecart, forcedChunk);
				dimMap.remove(minecart.getUUID());
			}
			return;
		}

		ChunkPos chunk = minecart.chunkPosition();
		if (forcedChunk != null && forcedChunk == chunk.toLong())
			return;
		if (forcedChunk != null)
			unforceChunk(serverLevel, minecart, forcedChunk);
		TICKET_CONTROLLER.forceChunk(serverLevel, minecart, chunk.x, chunk.z, true, true);
		dimMap.put(minecart.getUUID(), chunk.toLong());
	}

	/**
	 * When a minecart leaves the level (removed/killed/dimension change/unloaded), unforce the chunk it was forcing.
	 */
	@SubscribeEvent
	public void onMinecartLeaveLevel(EntityLeaveLevelEvent event) {
		if (!(event.getLevel() instanceof ServerLevel serverLevel)
				|| !(event.getEntity() instanceof AbstractMinecart minecart))
			return;

		Map<UUID, Long> dimMap = forcingMinecarts.get(serverLevel.dimension());
		if (dimMap == null)
			return;

		Long forcedChunk = dimMap.remove(minecart.getUUID());
		if (forcedChunk != null)
			unforceChunk(serverLevel, minecart, forcedChunk);
	}

	private static void unforceChunk(ServerLevel level, AbstractMinecart minecart, long chunk) {
		TICKET_CONTROLLER.forceChunk(level, minecart, ChunkPos.getX(chunk), ChunkPos.getZ(chunk), false, true);
	}

	/**
	 * When a level unloads, clear its tracking map (chunks are gone with the level).
	 */
	@SubscribeEvent
	public void onLevelUnload(LevelEvent.Unload event) {
		if (!(event.getLevel() instanceof ServerLevel serverLevel))
			return;
		forcingMinecarts.remove(serverLevel.dimension());
	}
}
