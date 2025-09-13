package insane96mcp.iguanatweaksreborn.module.misc.beaconconduit;

import insane96mcp.iguanatweaksreborn.InsaneSO;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.module.misc.Packs;
import insane96mcp.iguanatweaksreborn.setup.ISORegistries;
import insane96mcp.iguanatweaksreborn.setup.registry.SimpleBlockWithItem;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.JsonFeature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.data.IdTagMatcher;
import insane96mcp.insanelib.data.IdTagValue;
import insane96mcp.insanelib.world.effect.ILMobEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@LoadFeature(module = Modules.Ids.MISC, name = "Beacon & Conduit", description = "Beacon has been redesigned to have more effects and range based off blocks used for pyramid. Effects and blocks ranges are controlled via json config in this feature's folder.")
public class BeaconConduit extends JsonFeature {

    public static final SimpleBlockWithItem BEACON = SimpleBlockWithItem.register("beacon", () -> new ISOBeaconBlock(BlockBehaviour.Properties.copy(Blocks.BEACON)));
    public static final RegistryObject<BlockEntityType<ISOBeaconBlockEntity>> BEACON_BLOCK_ENTITY_TYPE = ISORegistries.BLOCK_ENTITY_TYPES.register("beacon", () -> BlockEntityType.Builder.of(ISOBeaconBlockEntity::new, BEACON.block().get()).build(null));
    public static final RegistryObject<MenuType<ISOBeaconMenu>> BEACON_MENU_TYPE = ISORegistries.MENU_TYPES.register("beacon", () -> new MenuType<>(ISOBeaconMenu::new, FeatureFlags.VANILLA_SET));

    @SuppressWarnings("unused")
	public static final RegistryObject<MobEffect> BLOCK_REACH = ISORegistries.MOB_EFFECTS.register("block_reach", () -> new ILMobEffect(MobEffectCategory.BENEFICIAL, 0x818894)
            .addAttributeModifier(ForgeMod.BLOCK_REACH.get(), "bd0c6709-4b67-43d5-ae51-c6180d848978", 0.5f, AttributeModifier.Operation.ADDITION));
    @SuppressWarnings("unused")
	public static final RegistryObject<MobEffect> ENTITY_REACH = ISORegistries.MOB_EFFECTS.register("entity_reach", () -> new ILMobEffect(MobEffectCategory.BENEFICIAL, 0x818894)
            .addAttributeModifier(ForgeMod.ENTITY_REACH.get(), "fb23063a-c676-4da0-8d75-574ab8f3ee30", 0.075f, AttributeModifier.Operation.MULTIPLY_BASE));

    public static final ArrayList<IdTagValue> BLOCKS_LIST_DEFAULT = new ArrayList<>(List.of(
            new IdTagValue(IdTagMatcher.Type.ID, "minecraft:iron_block", 1d),
            new IdTagValue(IdTagMatcher.Type.ID, "minecraft:emerald_block", 1d),
            new IdTagValue(IdTagMatcher.Type.ID, "minecraft:gold_block", 2d),
            new IdTagValue(IdTagMatcher.Type.ID, "minecraft:diamond_block", 3d),
            new IdTagValue(IdTagMatcher.Type.ID, "minecraft:netherite_block", 4d),
            new IdTagValue(IdTagMatcher.Type.ID, "iguanatweaksexpanded:durium_block", 3.0d),
            new IdTagValue(IdTagMatcher.Type.ID, "iguanatweaksexpanded:soul_steel_block", 1.5d),
            new IdTagValue(IdTagMatcher.Type.ID, "iguanatweaksexpanded:quaron_block", 1.5d),
            new IdTagValue(IdTagMatcher.Type.ID, "caverns_and_chasms:sanguine_block", 1.5d),
            new IdTagValue(IdTagMatcher.Type.ID, "iguanatweaksexpanded:keego_block", 3d),
            new IdTagValue(IdTagMatcher.Type.ID, "caverns_and_chasms:silver_block", 2d),
            new IdTagValue(IdTagMatcher.Type.ID, "caverns_and_chasms:necromium_block", 4d)
    ));
    public static final ArrayList<IdTagValue> blocksList = new ArrayList<>();
    public static final ArrayList<IdTagValue> PAYMENT_TIMES_DEFAULT = new ArrayList<>(List.of(
            new IdTagValue(IdTagMatcher.Type.ID, "minecraft:iron_ingot", 6000),
            new IdTagValue(IdTagMatcher.Type.ID, "minecraft:gold_ingot", 18000),
            new IdTagValue(IdTagMatcher.Type.ID, "minecraft:diamond", 72000),
            new IdTagValue(IdTagMatcher.Type.ID, "minecraft:emerald", 72000),
            new IdTagValue(IdTagMatcher.Type.ID, "minecraft:netherite_ingot", 115200),
            new IdTagValue(IdTagMatcher.Type.ID, "iguanatweaksexpanded:durium_ingot", 12000),
            new IdTagValue(IdTagMatcher.Type.ID, "iguanatweaksexpanded:keego", 96000),
            new IdTagValue(IdTagMatcher.Type.ID, "iguanatweaksexpanded:quaron_ingot", 96000),
            new IdTagValue(IdTagMatcher.Type.ID, "caverns_and_chasms:living_flesh", 96000),
            new IdTagValue(IdTagMatcher.Type.ID, "iguanatweaksexpanded:soul_steel_ingot", 96000),
            new IdTagValue(IdTagMatcher.Type.ID, "caverns_and_chasms:silver_ingot", 18000),
            new IdTagValue(IdTagMatcher.Type.ID, "caverns_and_chasms:necromium_ingot", 115200),
            new IdTagValue(IdTagMatcher.Type.ID, "minecraft:nether_star", 576000)
    ));
    public static final ArrayList<IdTagValue> paymentTimes = new ArrayList<>();
    public static final ArrayList<BeaconEffect> EFFECTS_DEFAULT = new ArrayList<>(List.of(
            new BeaconEffect(MobEffects.MOVEMENT_SPEED, new int[] {1, 2, 4}),
            new BeaconEffect(MobEffects.JUMP, new int[] {1, 2, 3}),
            new BeaconEffect("iguanatweaksreborn:block_reach", new int[] {1, 3, 9}),
            new BeaconEffect(MobEffects.DIG_SPEED, new int[] {1, 2, 4}, 2),
            new BeaconEffect(MobEffects.FIRE_RESISTANCE, new int[] {3}, 2),
            new BeaconEffect("stamina:vigour", new int[] {2, 5}, 2),
            new BeaconEffect(MobEffects.INVISIBILITY, new int[] {2}, 2),
            new BeaconEffect(MobEffects.DAMAGE_BOOST, new int[] {1, 3, 9}, 3),
            new BeaconEffect(MobEffects.DAMAGE_RESISTANCE, new int[] {1, 3, 9}, 3),
            new BeaconEffect(MobEffects.NIGHT_VISION, new int[] {3}, 3),
            new BeaconEffect(MobEffects.SLOW_FALLING, new int[] {2}, 3),
            new BeaconEffect(MobEffects.REGENERATION, new int[] {8}, 4),
            new BeaconEffect("iguanatweaksreborn:regenerating_absorption", new int[] {2, 4}, 4),
            new BeaconEffect("iguanatweaksreborn:entity_reach", new int[] {1, 3, 9}, 4)
    ));
    public static final ArrayList<BeaconEffect> effects = new ArrayList<>();

    @Config
    public static Boolean beacon$requiresPayment = false;
    @Config
    public static Integer beacon$baseRange = 16;

    @Config(description = "Greatly increases the range and damage of the conduit")
    public static Boolean conduit$betterProtection = true;
    @Config(min = 0d, max = 64d, description = "Distance multiplier (formula is `blocks_around / 7 * this_multiplier`) from the conduit at which it will deal damage to enemies.")
    public static Double conduit$protectionDistanceMultiplier = 8d;
    @Config(min = 0d, max = 96d, description = "If a mob is within this radius from the conduit, it will be dealt the maximum amount of damage.")
    public static Double conduit$protectionMaxDamageDistance = 8d;
    @Config(description = "If true, conduit effect will no longer speed up mining speed.")
    public static Boolean conduit$removeHaste = true;

    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);

        JSON_CONFIGS.add(new JsonConfig<>("beacon_blocks_ranges.json", blocksList, BLOCKS_LIST_DEFAULT, IdTagValue.LIST_TYPE));
        JSON_CONFIGS.add(new JsonConfig<>("beacon_payment_times.json", paymentTimes, PAYMENT_TIMES_DEFAULT, IdTagValue.LIST_TYPE));
        addSyncType(InsaneSO.location("beacon_effects"), new SyncType(json -> loadAndReadJson(json, effects, EFFECTS_DEFAULT, BeaconEffect.LIST_TYPE)));
        JSON_CONFIGS.add(new JsonConfig<>("beacon_effects.json", effects, EFFECTS_DEFAULT, BeaconEffect.LIST_TYPE, (list, isClientSide) -> list.removeIf(beaconEffect -> beaconEffect.getEffect() == null), true, InsaneSO.location("beacon_effects")));
        InsaneSO.addServerPack("better_beacon", "Insane's Survival Overhaul Better Beacon", () -> this.isEnabled() && !Packs.disableAllDataPacks);
    }

    @Override
    public String getModConfigFolder() {
        return InsaneSO.CONFIG_FOLDER;
    }

    public static int getPaymentTime(ItemStack stack) {
        for (IdTagValue idTagValue : paymentTimes) {
            if (idTagValue.id.matchesItem(stack.getItem()))
                return (int) idTagValue.value;
        }
        return 0;
    }

    @Nullable
    static BeaconEffect cachedBeaconEffect;

    public static int getEffectTimeScale(@Nullable MobEffect mobEffect, int amplifier) {
        if (cachedBeaconEffect != null && Objects.equals(mobEffect, cachedBeaconEffect.getEffect()))
            return cachedBeaconEffect.getTimeCostForAmplifier(amplifier);
        for (BeaconEffect beaconEffect : effects) {
            if (beaconEffect.location.equals(ForgeRegistries.MOB_EFFECTS.getKey(mobEffect))) {
                cachedBeaconEffect = beaconEffect;
                return beaconEffect.getTimeCostForAmplifier(amplifier);
            }
        }
        return 1;
    }

    public static boolean isValidEffect(MobEffect mobEffect) {
        for (BeaconEffect beaconEffect : effects) {
            if (beaconEffect.location.equals(ForgeRegistries.MOB_EFFECTS.getKey(mobEffect)))
                return true;
        }
        return false;
    }

    /*
     * CONDUIT
     */

    static float MIN_DAMAGE = 2f;
    static float MAX_DAMAGE = 6f;

    public static boolean conduitUpdateDestroyEnemies(Level level, BlockPos blockPos, List<BlockPos> blocks) {
        if (!isEnabled(BeaconConduit.class)
                || !conduit$betterProtection)
            return false;

        LivingEntity nearestEntity = level.getNearestEntity(LivingEntity.class, TargetingConditions.forNonCombat().selector(livingEntity -> livingEntity instanceof Enemy && livingEntity.isInWaterOrRain()), null, blockPos.getX(), blockPos.getY(), blockPos.getZ(), getDamageAABB(blockPos, blocks));
        if (nearestEntity == null)
            return true;

        level.playSound(null, nearestEntity.getX(), nearestEntity.getY(), nearestEntity.getZ(), SoundEvents.CONDUIT_ATTACK_TARGET, SoundSource.BLOCKS, 1.0F, 1.0F);
        double distance = nearestEntity.position().distanceTo(new Vec3(blockPos.getX(), blockPos.getY(), blockPos.getZ()));
        float damage;
        if (distance < conduit$protectionMaxDamageDistance)
            damage = MAX_DAMAGE;
        else
            damage = (float) (1 - (distance - conduit$protectionMaxDamageDistance) / (maxRangeRadius() - conduit$protectionMaxDamageDistance)) * (MAX_DAMAGE - MIN_DAMAGE) + MIN_DAMAGE;
        nearestEntity.hurt(nearestEntity.damageSources().magic(), damage);
        return true;
    }

    private static AABB getDamageAABB(BlockPos blockPos, List<BlockPos> blocks) {
        double range = blocks.size() / 7d * conduit$protectionDistanceMultiplier;
        int x = blockPos.getX();
        int y = blockPos.getY();
        int z = blockPos.getZ();
        return (new AABB(x, y, z, x + 1, y + 1, z + 1)).inflate(range);
    }

    private static double maxRange() {
        return 42 / 7d * conduit$protectionDistanceMultiplier;
    }

    private static double maxRangeRadius() {
        return Math.sqrt(maxRange() * maxRange() + maxRange() * maxRange());
    }

    public static boolean shouldRemoveConduitHaste() {
        return Feature.isEnabled(BeaconConduit.class) && BeaconConduit.conduit$removeHaste;
    }
}