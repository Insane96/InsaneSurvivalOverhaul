package insane96mcp.iguanatweaksreborn.setup;

import com.mojang.serialization.Codec;
import insane96mcp.iguanatweaksreborn.InsaneSurvivalTweaks;
import insane96mcp.iguanatweaksreborn.data.condition.*;
import insane96mcp.iguanatweaksreborn.data.lootmodifier.DisenchantModifier;
import insane96mcp.iguanatweaksreborn.data.lootmodifier.DropMultiplierModifier;
import insane96mcp.iguanatweaksreborn.data.lootmodifier.LootPurgerModifier;
import insane96mcp.iguanatweaksreborn.data.lootmodifier.ReplaceLootModifier;
import insane96mcp.iguanatweaksreborn.module.world.coalfire.PilableFallingLayerEntity;
import insane96mcp.iguanatweaksreborn.world.level.levelgen.structure.templatesystem.RandomBlockTagMatchTest;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTestType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;

public class ISTRegistries {
	public static final List<DeferredRegister<?>> REGISTRIES = new ArrayList<>();

	public static final DeferredRegister<Block> BLOCKS = createRegistry(ForgeRegistries.BLOCKS);
	public static final DeferredRegister<Item> ITEMS = createRegistry(ForgeRegistries.ITEMS);
	public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = createRegistry(ForgeRegistries.ENTITY_TYPES);
	public static final RegistryObject<EntityType<PilableFallingLayerEntity>> PILABLE_FALLING_LAYER = ENTITY_TYPES.register("pilable_falling_layer", () -> EntityType.Builder.<PilableFallingLayerEntity>of(PilableFallingLayerEntity::new, MobCategory.MISC)
			.sized(0.98f, 0.98f)
			.clientTrackingRange(10)
			.updateInterval(20)
			.build("pilable_falling_layer"));
	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = createRegistry(ForgeRegistries.BLOCK_ENTITY_TYPES);
	public static final DeferredRegister<MenuType<?>> MENU_TYPES = createRegistry(ForgeRegistries.MENU_TYPES);
	public static final DeferredRegister<Attribute> ATTRIBUTES = createRegistry(ForgeRegistries.ATTRIBUTES);
	public static final DeferredRegister<Enchantment> ENCHANTMENTS = createRegistry(ForgeRegistries.ENCHANTMENTS);
	public static final DeferredRegister<MobEffect> MOB_EFFECTS = createRegistry(ForgeRegistries.MOB_EFFECTS);
	public static final DeferredRegister<PoiType> POI_TYPES = createRegistry(ForgeRegistries.POI_TYPES);

	public static final DeferredRegister<SoundEvent> SOUND_EVENTS = createRegistry(ForgeRegistries.SOUND_EVENTS);
	public static final RegistryObject<SoundEvent> UNFAIR_ONE_SHOT = SOUND_EVENTS.register("unfair_one_shot", () -> SoundEvent.createFixedRangeEvent(new ResourceLocation(InsaneSurvivalTweaks.MOD_ID, "unfair_one_shot"), 16f));
	public static final RegistryObject<SoundEvent> ABSORPTION_HIT = SOUND_EVENTS.register("absorption_hit", () -> SoundEvent.createFixedRangeEvent(new ResourceLocation(InsaneSurvivalTweaks.MOD_ID, "absorption_hit"), 16f));

	public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> GLOBAL_LOOT_MODIFIER_SERIALIZERS = createRegistry(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS);
	public static final RegistryObject<Codec<? extends IGlobalLootModifier>> LOOT_PURGER_MODIFIER = GLOBAL_LOOT_MODIFIER_SERIALIZERS.register("loot_purger", LootPurgerModifier.CODEC);
	public static final RegistryObject<Codec<? extends IGlobalLootModifier>> DISENCHANT_MODIFIER = GLOBAL_LOOT_MODIFIER_SERIALIZERS.register("disenchant", DisenchantModifier.CODEC);
	public static final RegistryObject<Codec<? extends IGlobalLootModifier>> DROP_MULTIPLIER_MODIFIER = GLOBAL_LOOT_MODIFIER_SERIALIZERS.register("drop_multiplier", DropMultiplierModifier.CODEC);
	public static final RegistryObject<Codec<? extends IGlobalLootModifier>> REPLACE_DROP_MODIFIER = GLOBAL_LOOT_MODIFIER_SERIALIZERS.register("replace_drop", ReplaceLootModifier.CODEC);

	public static final DeferredRegister<LootItemConditionType> LOOT_CONDITION_TYPES = createRegistry(Registries.LOOT_CONDITION_TYPE.location());
	public static final RegistryObject<LootItemConditionType> BLOCK_TAG_MATCH = LOOT_CONDITION_TYPES.register("block_tag_match", () -> new LootItemConditionType(new BlockTagCondition.Serializer()));
	public static final RegistryObject<LootItemConditionType> KILLER_HAS_ADVANCEMENT = LOOT_CONDITION_TYPES.register("killer_has_advancement", () -> new LootItemConditionType(new KillerHasAdvancementCondition.Serializer()));
	public static final RegistryObject<LootItemConditionType> LIVING_ENTITY = LOOT_CONDITION_TYPES.register("living_entity", () -> new LootItemConditionType(new LivingEntityCondition.Serializer()));
	public static final RegistryObject<LootItemConditionType> NON_PLAYER_ARISED_DROP = LOOT_CONDITION_TYPES.register("non_player_arised_drop", () -> new LootItemConditionType(new NonPlayerArisedDropCondition.Serializer()));
	public static final RegistryObject<LootItemConditionType> KILLED_BY_KILLED_PLAYER = LOOT_CONDITION_TYPES.register("killed_by_killed_player", () -> new LootItemConditionType(new KilledByKilledPlayerCondition.Serializer()));
	public static final RegistryObject<LootItemConditionType> LIVESTOCK_AGE_CONDITION = LOOT_CONDITION_TYPES.register("livestock_age", () -> new LootItemConditionType(new LivestockAgeCondition.Serializer()));
	public static final RegistryObject<LootItemConditionType> CURRENT_SEASON = LOOT_CONDITION_TYPES.register("current_season", () -> new LootItemConditionType(new LootItemCurrentSeasonCondition.Serializer()));

	public static final DeferredRegister<RuleTestType<?>> RULE_TEST_TYPES = createRegistry(Registries.RULE_TEST);
	public static final RegistryObject<RuleTestType<RandomBlockTagMatchTest>> RANDOM_BLOCK_TAG_MATCH = RULE_TEST_TYPES.register("random_block_tag_match", RandomBlockTagMatchTest.Type::new);

	static <R> DeferredRegister<R> createRegistry(ResourceKey<? extends Registry<R>> key) {
		DeferredRegister<R> register = DeferredRegister.create(key, InsaneSurvivalTweaks.MOD_ID);
		REGISTRIES.add(register);
		return register;
	}

	static <R> DeferredRegister<R> createRegistry(IForgeRegistry<R> reg) {
		DeferredRegister<R> register = DeferredRegister.create(reg, InsaneSurvivalTweaks.MOD_ID);
		REGISTRIES.add(register);
		return register;
	}

	static <R> DeferredRegister<R> createRegistry(ResourceLocation registryName) {
		DeferredRegister<R> register = DeferredRegister.create(registryName, InsaneSurvivalTweaks.MOD_ID);
		REGISTRIES.add(register);
		return register;
	}
}
