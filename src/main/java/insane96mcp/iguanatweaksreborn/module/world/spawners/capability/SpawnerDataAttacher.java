package insane96mcp.iguanatweaksreborn.module.world.spawners.capability;

import insane96mcp.iguanatweaksreborn.InsaneSO;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SpawnerDataAttacher {
    public static final String SPAWNER_DATA = InsaneSO.RESOURCE_PREFIX + "spawner_data";

    public static class SpawnerDataProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {

        public static final ResourceLocation IDENTIFIER = new ResourceLocation(SPAWNER_DATA);

        private final ISpawnerData backend = new SpawnerDataImpl();
        private final LazyOptional<ISpawnerData> optionalData = LazyOptional.of(() -> backend);

        @NotNull
        @Override
        public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            return SpawnerData.INSTANCE.orEmpty(cap, this.optionalData);
        }

        void invalidate() {
            this.optionalData.invalidate();
        }

        @Override
        public CompoundTag serializeNBT() {
            return this.backend.serializeNBT();
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            this.backend.deserializeNBT(nbt);
        }
    }

    @SubscribeEvent
    public static void attach(final AttachCapabilitiesEvent<BlockEntity> event) {
        if (event.getObject() instanceof SpawnerBlockEntity) {
            final SpawnerDataProvider provider = new SpawnerDataProvider();
            event.addCapability(SpawnerDataProvider.IDENTIFIER, provider);
        }
    }

    private SpawnerDataAttacher() {
    }
}
