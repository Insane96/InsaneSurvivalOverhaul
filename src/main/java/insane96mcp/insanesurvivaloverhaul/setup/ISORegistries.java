package insane96mcp.insanesurvivaloverhaul.setup;

import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;

public class ISORegistries {
    public static final List<DeferredRegister<?>> REGISTRIES = new ArrayList<>();

    public static final DeferredRegister<Item> ITEMS = createRegistry(BuiltInRegistries.ITEM);
    public static final DeferredRegister<Attribute> ATTRIBUTES = createRegistry(BuiltInRegistries.ATTRIBUTE);
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = createRegistry(BuiltInRegistries.SOUND_EVENT);
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = createRegistry(BuiltInRegistries.MOB_EFFECT);

    private static <R> DeferredRegister<R> createRegistry(Registry<R> registry) {
        DeferredRegister<R> register = DeferredRegister.create(registry, InsaneSO.MOD_ID);
        REGISTRIES.add(register);
        return register;
    }
}
