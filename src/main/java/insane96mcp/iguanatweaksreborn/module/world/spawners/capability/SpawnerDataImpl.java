package insane96mcp.iguanatweaksreborn.module.world.spawners.capability;

import insane96mcp.iguanatweaksreborn.InsaneSO;
import insane96mcp.iguanatweaksreborn.module.world.spawners.Spawners;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public class SpawnerDataImpl implements ISpawnerData {

    public static final String SPAWNED_MOBS = InsaneSO.RESOURCE_PREFIX + "spawned_mobs";
	public static final String SPAWNER_DISABLED = InsaneSO.RESOURCE_PREFIX + "spawner_disabled";
	public static final String SPAWNER_EMPOWERED = InsaneSO.RESOURCE_PREFIX + "spawner_empowered";
	public static final String SPAWNER_REACTIVATED = InsaneSO.RESOURCE_PREFIX + "spawner_reactivated";
    private int spawnedMobs;
	private boolean disabled;
	private boolean empowered = true;
	private boolean hasBeenReactivated;

	@Override
	public int getSpawnedMobs() {
		return this.spawnedMobs;
	}

	@Override
	public void addSpawnedMobs(int spawnedMobs) {
		this.spawnedMobs += spawnedMobs;
	}

	@Override
	public void setSpawnedMobs(int spawnedMobs) {
		this.spawnedMobs = spawnedMobs;
	}

	@Override
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	@Override
	public boolean isDisabled() {
		return this.disabled;
	}

	@Override
	public void setEmpowered(boolean isEmpowered) {
		this.empowered = isEmpowered;
	}

	@Override
	public boolean isEmpowered() {
		return this.empowered;
	}

	@Override
	public void setHasBeenReactivated(boolean hasBeenReactivated) {
		this.hasBeenReactivated = hasBeenReactivated;
	}

	@Override
	public boolean hasBeenReactivated() {
		return this.hasBeenReactivated;
	}

	@Override
	public CompoundTag serializeNBT() {
		CompoundTag nbt = new CompoundTag();
		nbt.putInt(SPAWNED_MOBS, this.getSpawnedMobs());
		nbt.putBoolean(SPAWNER_DISABLED, this.isDisabled());
		nbt.putBoolean(SPAWNER_EMPOWERED, this.isEmpowered());
		nbt.putBoolean(SPAWNER_REACTIVATED, this.hasBeenReactivated());
		return nbt;
	}

	@Override
	public void deserializeNBT(CompoundTag nbt) {
		this.setSpawnedMobs(nbt.getInt(SPAWNED_MOBS));
		this.setDisabled(nbt.getBoolean(SPAWNER_DISABLED));
		if (!nbt.contains(SPAWNER_EMPOWERED))
			this.setEmpowered(Spawners.empowered$enabled);
		else
			this.setEmpowered(nbt.getBoolean(SPAWNER_EMPOWERED));
		this.setHasBeenReactivated(nbt.getBoolean(SPAWNER_REACTIVATED));
	}

	public void toNetwork(FriendlyByteBuf buf) {
		buf.writeInt(this.spawnedMobs);
		buf.writeBoolean(this.disabled);
		buf.writeBoolean(this.empowered);
		buf.writeBoolean(this.hasBeenReactivated);
	}

	public static SpawnerDataImpl fromNetwork(FriendlyByteBuf buf) {
		SpawnerDataImpl spawnerData = new SpawnerDataImpl();
		spawnerData.spawnedMobs = buf.readInt();
		spawnerData.disabled = buf.readBoolean();
		spawnerData.empowered = buf.readBoolean();
		spawnerData.hasBeenReactivated = buf.readBoolean();
		return spawnerData;
	}
}