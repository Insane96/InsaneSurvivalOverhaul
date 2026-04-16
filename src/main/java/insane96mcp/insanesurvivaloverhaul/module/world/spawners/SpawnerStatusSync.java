package insane96mcp.insanesurvivaloverhaul.module.world.spawners;

import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.mixin.accessor.BaseSpawnerAccessor;
import insane96mcp.insanesurvivaloverhaul.module.world.spawners.capability.SpawnerDataImpl;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SpawnerStatusSync(BlockPos pos, SpawnerDataImpl spawnerData, int delay, int requiredPlayerRange) implements CustomPacketPayload {
	public static final Type<SpawnerStatusSync> TYPE =
			new Type<>(InsaneSO.location("spawner_status_sync"));

	public static final StreamCodec<ByteBuf, SpawnerStatusSync> STREAM_CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, SpawnerStatusSync::pos,
			SpawnerDataImpl.STREAM_CODEC, SpawnerStatusSync::spawnerData,
			ByteBufCodecs.INT, SpawnerStatusSync::delay,
			ByteBufCodecs.INT, SpawnerStatusSync::requiredPlayerRange,
			SpawnerStatusSync::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handle(final SpawnerStatusSync payload, final IPayloadContext context) {
		context.enqueueWork(() -> {
			if (context.player().level().getBlockEntity(payload.pos()) instanceof SpawnerBlockEntity spawnerBlockEntity) {
				SpawnerDataImpl spawnerData = spawnerBlockEntity.getData(Spawners.SPAWNER_DATA.get());
				spawnerData.setSpawnedMobs(payload.spawnerData().getSpawnedMobs());
				spawnerData.setDisabled(payload.spawnerData().isDisabled());
				spawnerData.setEmpowered(payload.spawnerData().isEmpowered());
				spawnerData.setHasBeenReactivated(payload.spawnerData().hasBeenReactivated());
				BaseSpawnerAccessor baseSpawner = (BaseSpawnerAccessor) spawnerBlockEntity.getSpawner();
				baseSpawner.setSpawnDelay(payload.delay());
				baseSpawner.setRequiredPlayerRange(payload.requiredPlayerRange());
			}
		});
	}
}
