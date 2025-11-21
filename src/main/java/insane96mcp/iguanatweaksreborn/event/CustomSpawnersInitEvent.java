package insane96mcp.iguanatweaksreborn.event;

import com.google.common.collect.ImmutableList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.CustomSpawner;
import net.minecraftforge.eventbus.api.Event;

import java.util.List;

public class CustomSpawnersInitEvent extends Event {
    private final ServerLevel serverLevel;
    private final List<CustomSpawner> customSpawners;

    public CustomSpawnersInitEvent(ServerLevel serverLevel, List<CustomSpawner> customSpawners)
    {
        this.serverLevel = serverLevel;
        this.customSpawners = customSpawners;
    }

    public ServerLevel getServerLevel() {
        return this.serverLevel;
    }

    /// Returns an immutable list of the custom spawners
    public List<CustomSpawner> getCustomSpawners() {
        return ImmutableList.copyOf(this.customSpawners);
    }

    public void addCustomSpawner(CustomSpawner customSpawner) {
        this.customSpawners.add(customSpawner);
    }

    public void removeCustomSpawner(Class<CustomSpawner> customSpawnerClass) {
        this.customSpawners.removeIf(customSpawner -> customSpawner.getClass() == customSpawnerClass);
    }
}
