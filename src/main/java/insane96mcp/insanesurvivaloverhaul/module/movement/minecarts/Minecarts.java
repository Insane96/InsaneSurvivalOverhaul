package insane96mcp.insanesurvivaloverhaul.module.movement.minecarts;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.module.misc.Packs;
import insane96mcp.insanesurvivaloverhaul.setup.SimpleBlockWithItem;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@LoadFeature(module = ISOModules.MOVEMENT)
public class Minecarts extends Feature {

	public static final SimpleBlockWithItem GOLDEN_POWERED_RAIL = SimpleBlockWithItem.register("golden_powered_rail", () -> new ISOPoweredRail(BlockBehaviour.Properties.ofFullCopy(Blocks.POWERED_RAIL), 1f, 0.18f));
	public static final SimpleBlockWithItem COPPER_POWERED_RAIL = SimpleBlockWithItem.register("copper_powered_rail", () -> new ISOPoweredRail(BlockBehaviour.Properties.ofFullCopy(Blocks.POWERED_RAIL).sound(SoundType.COPPER), 0.4f, 0.05f));

    @Config(description = "If true, minecarts with entities in them and chest minecarts will force load chunks, allowing them to travel in unloaded chunks.")
    public static Boolean entityAndChestMinecartsForceLoadChunks = true;

	@Config(description = "If true, enables a data pack that makes rails cheaper and adds recipes for new rail. Also adds a global loot modifier that replaces vanilla rails with golden powered rails")
	public static Boolean dataPack = true;

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		InsaneSO.addServerPack("better_rails", "Insane's Survival Overhaul Better Rails", () -> this.isEnabled() && !Packs.disableAllDataPacks && dataPack);
	}

	@SubscribeEvent
	public void onMinecartTick(EntityTickEvent.Post event) {
		if (!this.isEnabled()
				|| !entityAndChestMinecartsForceLoadChunks
				|| !(event.getEntity().level() instanceof ServerLevel serverLevel)
				|| !(event.getEntity() instanceof AbstractMinecart abstractMinecart)
				|| (abstractMinecart.getFirstPassenger() == null && abstractMinecart.getType() != EntityType.CHEST_MINECART))
			return;

		serverLevel.setChunkForced(abstractMinecart.chunkPosition().x, abstractMinecart.chunkPosition().z, true);
	}

	@SubscribeEvent
	public void onLevelTick(LevelTickEvent.Pre event) {
		if (!this.isEnabled()
				|| !entityAndChestMinecartsForceLoadChunks
				|| !(event.getLevel() instanceof ServerLevel serverLevel))
			return;

		LongSet longset = serverLevel.getForcedChunks();
		longset.forEach(p_137675_ -> serverLevel.setChunkForced(ChunkPos.getX(p_137675_), ChunkPos.getZ(p_137675_), false));
	}
}