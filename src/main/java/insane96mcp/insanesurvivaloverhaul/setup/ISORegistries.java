package insane96mcp.insanesurvivaloverhaul.setup;

import com.mojang.serialization.Codec;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.data.condition.FedCondition;
import insane96mcp.insanesurvivaloverhaul.data.condition.LivestockAgeCondition;
import insane96mcp.insanesurvivaloverhaul.module.mining.beegoreveins.BigOreVeinFeature;
import insane96mcp.insanesurvivaloverhaul.module.mining.beegoreveins.OreWithRandomPatchConfiguration;
import insane96mcp.insanesurvivaloverhaul.world.level.levelgen.structure.templatesystem.RandomBlockTagMatchTest;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTestType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ISORegistries {
    public static final List<DeferredRegister<?>> REGISTRIES = new ArrayList<>();

    public static final DeferredRegister<Item> ITEMS = createRegistry(BuiltInRegistries.ITEM);
    public static final DeferredRegister<Item> MINECRAFT_ITEMS = createRegistryForNamespace(BuiltInRegistries.ITEM, "minecraft");
    public static final DeferredRegister<ArmorMaterial> MINECRAFT_ARMOR_MATERIALS = createRegistryForNamespace(BuiltInRegistries.ARMOR_MATERIAL, "minecraft");
    public static final DeferredRegister<Block> BLOCKS = createRegistry(BuiltInRegistries.BLOCK);
    public static final DeferredRegister<Attribute> ATTRIBUTES = createRegistry(BuiltInRegistries.ATTRIBUTE);
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = createRegistry(BuiltInRegistries.SOUND_EVENT);
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = createRegistry(BuiltInRegistries.MOB_EFFECT);
    @SuppressWarnings("rawtypes")
    private static final DeferredRegister CRITERION_TRIGGERS = createRegistry(BuiltInRegistries.TRIGGER_TYPES);
    public static final DeferredRegister<LootItemConditionType> LOOT_CONDITIONS = createRegistry(BuiltInRegistries.LOOT_CONDITION_TYPE);

    public static final DeferredHolder<LootItemConditionType, LootItemConditionType> HAS_BEEN_FED_RECENTLY =
            LOOT_CONDITIONS.register("has_been_fed_recently", () -> new LootItemConditionType(FedCondition.CODEC));
    public static final DeferredHolder<LootItemConditionType, LootItemConditionType> LIVESTOCK_AGE_CONDITION =
            LOOT_CONDITIONS.register("livestock_age", () -> new LootItemConditionType(LivestockAgeCondition.CODEC));

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = createRegistry(BuiltInRegistries.RECIPE_SERIALIZER);

    public static final DeferredRegister<Feature<?>> FEATURES = createRegistry(BuiltInRegistries.FEATURE);
    public static final DeferredRegister<RuleTestType<?>> RULE_TEST_TYPES = createRegistry(BuiltInRegistries.RULE_TEST);

    public static final DeferredHolder<RuleTestType<?>, RuleTestType<RandomBlockTagMatchTest>> RANDOM_BLOCK_TAG_MATCH =
            RULE_TEST_TYPES.register("random_block_tag_match", RandomBlockTagMatchTest.Type::new);
    public static final DeferredHolder<Feature<?>, BigOreVeinFeature> BIG_ORE_VEIN_FEATURE =
            FEATURES.register("ore_with_surface_feature", () -> new BigOreVeinFeature(OreWithRandomPatchConfiguration.CODEC));

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = createAttachmentTypesRegistry();

    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = createDataComponentsRegistry();
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> SCYTHE_RADIUS =
            DATA_COMPONENTS.register("scythe_radius", () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.INT)
                    .build());

    private static DeferredRegister<AttachmentType<?>> createAttachmentTypesRegistry() {
        DeferredRegister<AttachmentType<?>> register = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, InsaneSO.MOD_ID);
        REGISTRIES.add(register);
        return register;
    }

    private static DeferredRegister<DataComponentType<?>> createDataComponentsRegistry() {
        DeferredRegister<DataComponentType<?>> register = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, InsaneSO.MOD_ID);
        REGISTRIES.add(register);
        return register;
    }

    private static <R> DeferredRegister<R> createRegistry(Registry<R> registry) {
        DeferredRegister<R> register = DeferredRegister.create(registry, InsaneSO.MOD_ID);
        REGISTRIES.add(register);
        return register;
    }

    private static <R> DeferredRegister<R> createRegistryForNamespace(Registry<R> registry, String namespace) {
        DeferredRegister<R> register = DeferredRegister.create(registry, namespace);
        REGISTRIES.add(register);
        return register;
    }

    @SuppressWarnings({"unchecked"})
    public static <T extends CriterionTrigger<?>> Supplier<T> registerTrigger(String name, Supplier<T> factory) {
        return CRITERION_TRIGGERS.register(name, factory);
    }
}
