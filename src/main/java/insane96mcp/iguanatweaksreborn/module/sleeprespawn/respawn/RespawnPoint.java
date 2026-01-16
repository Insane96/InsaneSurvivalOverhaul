package insane96mcp.iguanatweaksreborn.module.sleeprespawn.respawn;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

public record RespawnPoint(Component name, BlockPos pos) {
    public CompoundTag writeToNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("name", Component.Serializer.toJson(this.name));
        tag.putLong("pos", this.pos.asLong());
        return tag;
    }

    public static RespawnPoint readFromNBT(CompoundTag tag) {
        return new RespawnPoint(Component.Serializer.fromJson(tag.getString("name")), BlockPos.of(tag.getLong("pos")));
    }
}
