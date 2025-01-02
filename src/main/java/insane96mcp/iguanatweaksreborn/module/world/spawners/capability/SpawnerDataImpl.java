package insane96mcp.iguanatweaksreborn.module.world.spawners.capability;

import insane96mcp.iguanatweaksreborn.InsaneSurvivalOverhaul;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public class SpawnerDataImpl implements ISpawnerData {

    public static final String SPAWNED_MOBS = InsaneSurvivalOverhaul.RESOURCE_PREFIX + "spawned_mobs";
	public static final String SPAWNER_DISABLED = InsaneSurvivalOverhaul.RESOURCE_PREFIX + "spawner_disabled";
	public static final String SPAWNER_EMPOWERED = InsaneSurvivalOverhaul.RESOURCE_PREFIX + "spawner_empowered";
    private int spawnedMobs;
	private boolean disabled;
	private boolean empowered = true;

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
	public CompoundTag serializeNBT() {
		CompoundTag nbt = new CompoundTag();
		nbt.putInt(SPAWNED_MOBS, this.getSpawnedMobs());
		nbt.putBoolean(SPAWNER_DISABLED, this.isDisabled());
		nbt.putBoolean(SPAWNER_EMPOWERED, this.isEmpowered());
		return nbt;
	}

	@Override
	public void deserializeNBT(CompoundTag nbt) {
		this.setSpawnedMobs(nbt.getInt(SPAWNED_MOBS));
		this.setDisabled(nbt.getBoolean(SPAWNER_DISABLED));
		if (!nbt.contains(SPAWNER_EMPOWERED))
			this.setEmpowered(true);
		else
			this.setEmpowered(nbt.getBoolean(SPAWNER_EMPOWERED));
	}

	public void toNetwork(FriendlyByteBuf buf) {
		buf.writeInt(this.spawnedMobs);
		buf.writeBoolean(this.disabled);
		buf.writeBoolean(this.empowered);
	}

	public static SpawnerDataImpl fromNetwork(FriendlyByteBuf buf) {
		SpawnerDataImpl spawnerData = new SpawnerDataImpl();
		spawnerData.spawnedMobs = buf.readInt();
		spawnerData.disabled = buf.readBoolean();
		spawnerData.empowered = buf.readBoolean();
		return spawnerData;
	}
}