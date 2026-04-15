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
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@LoadFeature(module = ISOModules.MOVEMENT)
public class Minecarts extends Feature {

	public static final SimpleBlockWithItem GOLDEN_POWERED_RAIL = SimpleBlockWithItem.register("golden_powered_rail", () -> new ISOPoweredRail(BlockBehaviour.Properties.ofFullCopy(Blocks.POWERED_RAIL), 1f, 0.18f));
	public static final SimpleBlockWithItem COPPER_POWERED_RAIL = SimpleBlockWithItem.register("copper_powered_rail", () -> new ISOPoweredRail(BlockBehaviour.Properties.ofFullCopy(Blocks.POWERED_RAIL).sound(SoundType.COPPER), 0.4f, 0.05f));

	@Config(description = "If true, minecarts with entities in them and chest minecarts will force load chunks, allowing them to travel in unloaded chunks.")
	public static Boolean entityAndChestMinecartsForceLoadChunks = true;

	@Config(description = "If true, enables a data pack that makes rails cheaper and adds recipes for new rail. Also adds a global loot modifier that replaces vanilla rails with golden powered rails")
	public static Boolean dataPack = true;

	/**
	 * Tracks minecarts that are currently keeping a chunk force-loaded.
	 * Value is {@code true} if we called setChunkForced(true) ourselves,
	 * {@code false} if the chunk was already forced by something else (e.g. /forceload).
	 * We only call setChunkForced(false) when the value is {@code true}.
	 */
	private static final Map<ResourceKey<Level>, Map<UUID, Boolean>> forcingMinecarts = new HashMap<>();

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		InsaneSO.addServerPack("better_rails", "Insane's Survival Overhaul Better Rails", () -> this.isEnabled() && !Packs.disableAllDataPacks && dataPack);
	}

	private static boolean qualifies(AbstractMinecart minecart) {
		return entityAndChestMinecartsForceLoadChunks
				&& (minecart.getFirstPassenger() != null || minecart instanceof AbstractMinecartContainer);
	}

	/**
	 * Before the minecart ticks (moves), unforce the chunk it was in (if we forced it)
	 * so the forced state can be refreshed at the new position in Post.
	 */
	@SubscribeEvent
	public void onMinecartTickPre(EntityTickEvent.Pre event) {
		if (!this.isEnabled()
				|| !(event.getEntity().level() instanceof ServerLevel serverLevel)
				|| !(event.getEntity() instanceof AbstractMinecart minecart))
			return;

		Map<UUID, Boolean> dimMap = forcingMinecarts.get(serverLevel.dimension());
		if (dimMap == null)
			return;

		Boolean weForced = dimMap.remove(minecart.getUUID());
		if (weForced == null)
			return;

		if (weForced) {
			ChunkPos chunk = minecart.chunkPosition();
			serverLevel.setChunkForced(chunk.x, chunk.z, false);
		}
	}

	/**
	 * After the minecart ticks (moves), force the chunk it is now in if it qualifies.
	 * Records whether we actually forced it or if it was already forced (e.g. /forceload).
	 */
	@SubscribeEvent
	public void onMinecartTickPost(EntityTickEvent.Post event) {
		if (!this.isEnabled()
				|| !(event.getEntity().level() instanceof ServerLevel serverLevel)
				|| !(event.getEntity() instanceof AbstractMinecart minecart)
				|| !qualifies(minecart))
			return;

		ChunkPos chunk = minecart.chunkPosition();
		boolean alreadyForced = serverLevel.getForcedChunks().contains(chunk.toLong());
		if (!alreadyForced)
			serverLevel.setChunkForced(chunk.x, chunk.z, true);
		forcingMinecarts.computeIfAbsent(serverLevel.dimension(), k -> new HashMap<>()).put(minecart.getUUID(), !alreadyForced);
	}

	/**
	 * When a minecart leaves the level (removed/killed/dimension change), unforce
	 * the chunk it was occupying if we were the one who forced it.
	 */
	@SubscribeEvent
	public void onMinecartLeaveLevel(EntityLeaveLevelEvent event) {
		if (!(event.getLevel() instanceof ServerLevel serverLevel)
				|| !(event.getEntity() instanceof AbstractMinecart minecart))
			return;

		Map<UUID, Boolean> dimMap = forcingMinecarts.get(serverLevel.dimension());
		if (dimMap == null)
			return;

		Boolean weForced = dimMap.remove(minecart.getUUID());
		if (weForced != null && weForced) {
			ChunkPos chunk = minecart.chunkPosition();
			serverLevel.setChunkForced(chunk.x, chunk.z, false);
		}
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
