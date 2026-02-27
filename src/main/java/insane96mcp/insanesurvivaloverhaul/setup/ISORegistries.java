package insane96mcp.insanesurvivaloverhaul.setup;

import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;

public class ISORegistries {
    public static final List<DeferredRegister<?>> REGISTRIES = new ArrayList<>();

    public static final DeferredRegister<Attribute> ATTRIBUTES = createRegistry(BuiltInRegistries.ATTRIBUTE);

    private static <R> DeferredRegister<R> createRegistry(Registry<R> registry) {
        DeferredRegister<R> register = DeferredRegister.create(registry, InsaneSO.MOD_ID);
        REGISTRIES.add(register);
        return register;
    }
}
