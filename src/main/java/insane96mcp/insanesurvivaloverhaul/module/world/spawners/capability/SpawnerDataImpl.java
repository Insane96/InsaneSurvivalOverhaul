package insane96mcp.insanesurvivaloverhaul.module.world.spawners.capability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class SpawnerDataImpl {

	public static final String SPAWNED_MOBS = "spawned_mobs";
	public static final String SPAWNER_DISABLED = "spawner_disabled";
	public static final String SPAWNER_EMPOWERED = "spawner_empowered";
	public static final String SPAWNER_REACTIVATED = "spawner_reactivated";

	public static final Codec<SpawnerDataImpl> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.optionalFieldOf(SPAWNED_MOBS, 0).forGetter(SpawnerDataImpl::getSpawnedMobs),
			Codec.BOOL.optionalFieldOf(SPAWNER_DISABLED, false).forGetter(SpawnerDataImpl::isDisabled),
			Codec.BOOL.optionalFieldOf(SPAWNER_EMPOWERED, true).forGetter(SpawnerDataImpl::isEmpowered),
			Codec.BOOL.optionalFieldOf(SPAWNER_REACTIVATED, false).forGetter(SpawnerDataImpl::hasBeenReactivated)
	).apply(instance, SpawnerDataImpl::new));

	public static final StreamCodec<ByteBuf, SpawnerDataImpl> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.INT, SpawnerDataImpl::getSpawnedMobs,
			ByteBufCodecs.BOOL, SpawnerDataImpl::isDisabled,
			ByteBufCodecs.BOOL, SpawnerDataImpl::isEmpowered,
			ByteBufCodecs.BOOL, SpawnerDataImpl::hasBeenReactivated,
			SpawnerDataImpl::new
	);

	private int spawnedMobs;
	private boolean disabled;
	private boolean empowered;
	private boolean hasBeenReactivated;

	public SpawnerDataImpl() {
		this(0, false, true, false);
	}

	public SpawnerDataImpl(int spawnedMobs, boolean disabled, boolean empowered, boolean hasBeenReactivated) {
		this.spawnedMobs = spawnedMobs;
		this.disabled = disabled;
		this.empowered = empowered;
		this.hasBeenReactivated = hasBeenReactivated;
	}

	public int getSpawnedMobs() {
		return this.spawnedMobs;
	}

	public void addSpawnedMobs(int spawnedMobs) {
		this.spawnedMobs += spawnedMobs;
	}

	public void setSpawnedMobs(int spawnedMobs) {
		this.spawnedMobs = spawnedMobs;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public boolean isDisabled() {
		return this.disabled;
	}

	public void setEmpowered(boolean isEmpowered) {
		this.empowered = isEmpowered;
	}

	public boolean isEmpowered() {
		return this.empowered;
	}

	public void setHasBeenReactivated(boolean hasBeenReactivated) {
		this.hasBeenReactivated = hasBeenReactivated;
	}

	public boolean hasBeenReactivated() {
		return this.hasBeenReactivated;
	}
}
