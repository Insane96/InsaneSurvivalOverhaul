package insane96mcp.iguanatweaksreborn.setup;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.BooleanSupplier;

public class IntegratedPack implements Comparable<IntegratedPack> {

    public static final List<IntegratedPack> INTEGRATED_PACKS = new ArrayList<>();

    int priority;
    PackType packType;
    String modId;
    String path;
    MutableComponent description;
    BooleanSupplier enabled;

    public IntegratedPack(int priority, PackType packType, String modId, String path, MutableComponent description, BooleanSupplier enabled) {
        this.priority = priority;
        this.packType = packType;
        this.modId = modId;
        this.path = path;
        this.description = description;
        this.enabled = enabled;
    }

    public IntegratedPack(PackType packType, String modId, String path, MutableComponent description, BooleanSupplier enabled) {
        this(0, packType, modId, path, description, enabled);
    }

    public int getPriority() {
        return this.priority;
    }

    public PackType getPackType() {
        return this.packType;
    }

    public String getPath() {
        return this.path;
    }

    public MutableComponent getDescription() {
        return this.description;
    }

    public boolean shouldBeEnabled() {
        return this.enabled.getAsBoolean();
    }

    @Override
    public int compareTo(@NotNull IntegratedPack o) {
        return Integer.compare(this.priority, o.priority);
    }

    @Override
    public String toString() {
        return "[%d] %s".formatted(this.priority, this.description.getString());
    }

    public static void addPack(IntegratedPack integratedPack) {
        int index = Collections.binarySearch(INTEGRATED_PACKS, integratedPack, Comparator.comparingInt(IntegratedPack::getPriority));
        if (index < 0)
            index = -(index + 1);
        INTEGRATED_PACKS.add(index, integratedPack);
    }

    public static void onAddPackFinders(AddPackFindersEvent event) {
        for (var integratedPack : INTEGRATED_PACKS) {
            if (event.getPackType() != integratedPack.getPackType()
                    || !integratedPack.shouldBeEnabled())
                continue;

            Path resourcePath = ModList.get().getModFileById(integratedPack.modId).getFile().findResource("integrated_packs/" + integratedPack.getPath());
            var pack = Pack.readMetaAndCreate(integratedPack.modId + ":" + integratedPack.getPath(), integratedPack.getDescription(), integratedPack.shouldBeEnabled() && integratedPack.getPackType() != PackType.CLIENT_RESOURCES,
                    (path) -> new PathPackResources(path, resourcePath, false), PackType.SERVER_DATA, Pack.Position.TOP, integratedPack.shouldBeEnabled() ? PackSource.DEFAULT : ISOPackSource.DISABLED);
            event.addRepositorySource((packConsumer) -> packConsumer.accept(pack));
        }
    }
}
