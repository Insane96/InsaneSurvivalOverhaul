package insane96mcp.iguanatweaksreborn.module.misc.tweaks;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import insane96mcp.iguanatweaksreborn.IguanaTweaksReborn;
import insane96mcp.iguanatweaksreborn.data.generator.ITRBlockTagsProvider;
import insane96mcp.iguanatweaksreborn.data.generator.ITRItemTagsProvider;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.module.mining.MiningMisc;
import insane96mcp.iguanatweaksreborn.setup.ITRRegistries;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.MinMax;
import insane96mcp.insanelib.util.MathHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.RegistryObject;

@Label(name = "Tweaks", description = "Various stuff that doesn't fit in any other Feature.")
@LoadFeature(module = Modules.Ids.MISC)
public class Tweaks extends Feature {

    public static final GameRules.Key<GameRules.IntegerValue> RULE_PAINFUL_WORLD_BORDER = GameRules.register("iguanatweaks:painful_world_border", GameRules.Category.MISC, GameRules.IntegerValue.create(0));

    public static final RegistryObject<Block> SCUTE = ITRRegistries.BLOCKS.register("scute", () -> new ScuteBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GREEN).strength(0.2F, 0.5F).offsetType(BlockBehaviour.OffsetType.XZ).dynamicShape().sound(SoundType.BONE_BLOCK)));

    public static final TagKey<Block> BREAK_ON_FALL = ITRBlockTagsProvider.create("break_on_fall");
    public static final TagKey<Item> WORLD_IMMUNE = ITRItemTagsProvider.create("world_immune");

    public static ResourceKey<DamageType> COLLIDE_WITH_WALL = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(IguanaTweaksReborn.MOD_ID, "collide_with_wall"));

    @Config
    @Label(name = "Prevent fire with resistance", description = "If true, entities will no longer be set on fire if have Fire Resistance (like bedrock edition)")
    public static Boolean preventFireWithResistance = true;

    @Config
    @Label(name = "Falling breaking glass", description = "Falling on glass has a chance of breaking it. The higher the fall, the higher the chance. iguanatweaksreborn:fall_on_break block tag can be used to add more blocks that break when falling onto them.")
    public static Boolean fallingBreakingGlass = true;
    @Config(min = 1)
    @Label(name = "Poison damage speed", description = "Poison will damage the player every this ticks at level I. Vanilla is 25.")
    public static Integer poisonDamageSpeed = 60;

    @Config
    @Label(name = "Maximum Sponge Soak Blocks", description = "The maximum amount of blocks a sponge can soak. (Vanilla is 64, disabled if quark is installed)")
    public static Integer maxSpongeSoakBlocks = 256;
    @Config
    @Label(name = "Maximum Sponge Soak Range", description = "The maximum range at which sponges will check for soakable blocks. (Vanilla is 5, disabled if quark is installed)")
    public static Integer maxSpongeSoakRange = 10;
    @Config
    @Label(name = "Sponges dry in the sun and wet in rain", description = "If exposed to the sun sponges may dry and if exposed to rain sponges might get wet. Requires a Minecraft restart if disabled")
    public static Boolean spongesDryInTheSun = true;

    @Config
    @Label(name = "Better hardcore death", description = "When you die in hardcore, your spawn point is set to where you died and a lightning strike is summoned")
    public static Boolean betterHardcoreDeath = true;

    @Config(min = 0, max = 100)
    @Label(name = "Players air ticks consumed", description = "The amount of ticks the player consumes when underwater. In vanilla it's 1 without Respiration enchantment. For non integer numbers the decimal part will count as a chance to have a +1")
    public static Double playerConsumeAirAmount = 1.5d;
    @Config
    @Label(name = "Players air ticks refilled", description = "The amount of air ticks the player regains each tick when out of water. For non integer numbers the decimal part will count as a chance to have a +1. Vanilla is 4. Min is the amount as soon as you exit water, Max is a few seconds out of water.")
    public static MinMax playerRefillAirAmount = new MinMax(1, 2.5);
    @Config
    @Label(name = "Increase drown damage the more drowning")
    public static Boolean increaseDrownDamageTheMoreDrowning = true;
    @Config
    @Label(name = "Totem resistance", description = "If enabled, the Totem of Undying will give Resistance IV for 5.5 seconds")
    public static Boolean totemResistance = true;

    @Config
    @Label(name = "Turtle.Helmet water breathing time", description = "The ticks of Water Breathing given by the Turtle Helmet. Vanilla is 200")
    public static Integer turtleHelmetWaterBreathingTime = 900;
    @Config
    @Label(name = "Turtle.Scute drop as block", description = "If true scutes will drop as a block and not as item")
    public static Boolean scuteDropAsBlock = true;

    @Config
    @Label(name = "Splash potions throw strength", description = "The strength used to throw splash potions. Vanilla is 0.5")
    public static Double splashPotionThrowStrength = 1d;

    @Config(min = 0)
    @Label(name = "Collide with walls damage", description = "If set higher than 0 it will enable damage when colliding with walls at a high speed (e.g. with explosions or knockback). Higher = more damage. Set to 0 to disable")
    public static Double collideWithWallsDamage = 2.5d;

    @Config(min = -1, max = 0)
    @Label(name = "Frozen Movement Speed modifier", description = "The speed modifier when frozen. Vanilla is -0.05")
    public static Double frozenMovementSpeedModifier = -0.1d;

    @Config
    @Label(name = "Ding on mob hit at distance", description = "Plays a sound effect when a mob is hit at least from this distance.")
    public static Integer dingDistance = 40;

    public Tweaks(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }

    @Override
    public void readConfig(ModConfigEvent event) {
        super.readConfig(event);
        if (spongesDryInTheSun) {
            Blocks.WET_SPONGE.isRandomlyTicking = true;
            Blocks.SPONGE.isRandomlyTicking = true;
        }
    }

    public static boolean isFireImmune(Entity entity) {
        if (!isEnabled(Tweaks.class)
                || !preventFireWithResistance
                || !(entity instanceof LivingEntity livingEntity))
            return false;

       return livingEntity.hasEffect(MobEffects.FIRE_RESISTANCE);
    }

    public static int changeMaxSpongeSoakBlocks(int soakableBlocks) {
        if (!isEnabled(Tweaks.class))
            return soakableBlocks;

        //Vanilla uses 65 and not 64
        return maxSpongeSoakBlocks + 1;
    }

	public static int changeSpongeMaxRange(int range) {
		if (!isEnabled(Tweaks.class))
			return range;

		//Vanilla uses < instead of <=
		return maxSpongeSoakRange + 1;
	}

    public static void onSpongeTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!Feature.isEnabled(Tweaks.class))
            return;

        if (!level.canSeeSky(pos.above()))
            return;
        if (state.is(Blocks.SPONGE) && level.isRaining())
                level.setBlockAndUpdate(pos, Blocks.WET_SPONGE.defaultBlockState());
        else if (state.is(Blocks.WET_SPONGE) && !level.isRaining() && level.isDay()) {
            int chance = 5;
            for (Direction direction : Direction.values()) {
                if (level.getBlockState(pos.relative(direction)).is(Blocks.WET_SPONGE)) {
                    chance *= 4;
                    break;
                }
            }
            if (random.nextInt(chance) == 0)
                level.setBlockAndUpdate(pos, Blocks.SPONGE.defaultBlockState());
        }
    }

	public static int getPoisonDamageSpeed(int original) {
		return isEnabled(Tweaks.class) ? poisonDamageSpeed : original;
	}

    public static int destroyDelay(ItemStack stack, DiggerItem item, BlockState state) {
        return isEnabled(MiningMisc.class) && MiningMisc.efficiencyBasedDestroyDelay ? Math.max(5 - (int) (item.speed / 2f), 1) : 5;
    }

	@SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlayerDeath(LivingDeathEvent event) {
        if (!this.isEnabled()
                || !betterHardcoreDeath
                || event.getEntity().level().isClientSide
                || !(event.getEntity() instanceof ServerPlayer player)
                || event.getEntity() instanceof FakePlayer
                || !event.getEntity().level().getLevelData().isHardcore()
                || player.gameMode.getGameModeForPlayer() == GameType.CREATIVE
                || player.gameMode.getGameModeForPlayer() == GameType.SPECTATOR)
            return;

        //player.serverLevel().getGameRules().getRule(GameRules.RULE_DO_IMMEDIATE_RESPAWN).set(true, player.server);
        player.setRespawnPosition(player.level().dimension(), player.blockPosition(), player.getXRot(), true, false);
        LightningBolt lightningBolt = new LightningBolt(EntityType.LIGHTNING_BOLT, player.level());
        lightningBolt.setVisualOnly(true);
        lightningBolt.setPos(player.position());
        player.level().addFreshEntity(lightningBolt);
        player.level().setBlock(player.blockPosition(), Blocks.AIR.defaultBlockState(), 2);
        /*if (player.serverLevel().getGameRules().getRule(GameRules.RULE_DO_IMMEDIATE_RESPAWN).get()) {
            event.setCanceled(true);
            player.setHealth(player.getMaxHealth());
            Component component = player.getCombatTracker().getDeathMessage();
            player.server.getPlayerList().broadcastSystemMessage(component, false);
        }*/
    }

    boolean appliedResistance = false;
    @SubscribeEvent
    public void onTotemUse(LivingUseTotemEvent event) {
        if (!this.isEnabled()
                || !totemResistance)
            return;

        event.getEntity().addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 110, 3));
        appliedResistance = true;
    }

    @SubscribeEvent
    public void onDamageEvent(LivingDamageEvent event) {
        if (!this.isEnabled()
                || !(event.getEntity() instanceof ServerPlayer player))
            return;
        int painfulWorldBorder = player.level().getGameRules().getInt(RULE_PAINFUL_WORLD_BORDER);
        if (painfulWorldBorder == 0)
            return;

        //noinspection DataFlowIssue
        WorldBorder worldBorder = player.getServer().overworld().getWorldBorder();
        double currentSize = worldBorder.getLerpTarget();
        double newSize = currentSize + painfulWorldBorder * Math.min(event.getAmount(), player.getHealth());
        worldBorder.lerpSizeBetween(currentSize, newSize, 2000L);
    }

    @SubscribeEvent
    public void onEffectRemoved(MobEffectEvent.Remove event) {
        if (!this.isEnabled()
                || !appliedResistance
                || !event.getEffect().equals(MobEffects.DAMAGE_RESISTANCE))
            return;
        event.setCanceled(true);
        appliedResistance = false;
    }

    //Lowest priority so other mods can change/cancel fall damage
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onFalling(LivingFallEvent event) {
        if (!this.isEnabled()
                || !fallingBreakingGlass
                || event.getEntity().level().isClientSide)
            return;

        LivingEntity entity = event.getEntity();
        AABB bb = entity.getBoundingBox();
        int mX = Mth.floor(bb.minX);
        int mZ = Mth.floor(bb.minZ);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        float distance = event.getDistance() - 3f;
        if (distance <= 0)
            return;
        float chance = (float) (Math.pow(distance, 1.25f) * 0.05f);
        if (entity.getRandom().nextFloat() >= chance)
            return;
        for (int x2 = mX; x2 < bb.maxX; x2++) {
            for (int z2 = mZ; z2 < bb.maxZ; z2++) {
                pos.set(x2, entity.position().y - 1.0E-5F, z2);
                BlockState state = entity.level().getBlockState(pos);
                if (state.is(BREAK_ON_FALL)) {
                    BlockEntity blockEntity = state.hasBlockEntity() ? entity.level().getBlockEntity(pos) : null;
                    LootParams.Builder lootcontext$builder = (new LootParams.Builder((ServerLevel) entity.level())).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos)).withParameter(LootContextParams.TOOL, ItemStack.EMPTY).withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockEntity).withOptionalParameter(LootContextParams.THIS_ENTITY, entity);
                    state.getDrops(lootcontext$builder).forEach(stack ->
                        entity.level().addFreshEntity(new ItemEntity(entity.level(), pos.getX() + 0.5d, pos.getY() + 0.5d, pos.getZ() + 0.5d, stack))
                    );
                    entity.level().destroyBlock(pos, false);
                }
            }
        }
    }

    @SubscribeEvent
    public void onBreathe(LivingBreatheEvent event) {
        if (!this.isEnabled()
                || event.getEntity().level().isClientSide)
            return;

        boolean wasBreathing = event.getEntity().getPersistentData().getBoolean(IguanaTweaksReborn.RESOURCE_PREFIX + "was_breathing");
        long ticksSinceOutOfWater = event.getEntity().level().getGameTime() - event.getEntity().getPersistentData().getLong(IguanaTweaksReborn.RESOURCE_PREFIX + "tick_since_out_of_water");
        if (!wasBreathing && event.canBreathe()) {
            ticksSinceOutOfWater = 0;
            event.getEntity().getPersistentData().putLong(IguanaTweaksReborn.RESOURCE_PREFIX + "tick_since_out_of_water", event.getEntity().level().getGameTime());
        }
        int airConsumed = MathHelper.getAmountWithDecimalChance(event.getEntity().getRandom(), playerConsumeAirAmount);
        int respiration = EnchantmentHelper.getRespiration(event.getEntity());
        airConsumed = respiration > 0 && event.getEntity().getRandom().nextInt(respiration + 1) > 0 ? 0 : airConsumed;
        event.setConsumeAirAmount(airConsumed);

        int refillAmount = MathHelper.getAmountWithDecimalChance(event.getEntity().getRandom(), playerRefillAirAmount.min);
        if (ticksSinceOutOfWater > 75) {
            refillAmount = MathHelper.getAmountWithDecimalChance(event.getEntity().getRandom(), playerRefillAirAmount.max);
            if (event.canBreathe())
                event.getEntity().getPersistentData().remove(IguanaTweaksReborn.RESOURCE_PREFIX + "times_drowned");
        }
        event.setRefillAirAmount(refillAmount);
        event.getEntity().getPersistentData().putBoolean(IguanaTweaksReborn.RESOURCE_PREFIX + "was_breathing", event.canBreathe());
    }

    @SubscribeEvent
    public void onDrown(LivingDrownEvent event) {
        if (!this.isEnabled()
                || !increaseDrownDamageTheMoreDrowning)
            return;

        if (event.isDrowning()) {
            int timesDrowned = event.getEntity().getPersistentData().getInt(IguanaTweaksReborn.RESOURCE_PREFIX + "times_drowned");
            timesDrowned++;
            event.getEntity().getPersistentData().putInt(IguanaTweaksReborn.RESOURCE_PREFIX + "times_drowned", timesDrowned);

            event.setDamageAmount(event.getDamageAmount() * timesDrowned * 0.5f);
            event.setBubbleCount((int) (event.getBubbleCount() * timesDrowned * 0.5f));
        }
    }

    public static Vec3 onCollideWithWall(LivingEntity instance, Vec3 pTravelVector, float pFriction, Operation<Vec3> originalOperation) {
        if (!Feature.isEnabled(Tweaks.class)
                || collideWithWallsDamage == 0)
            return originalOperation.call(instance, pTravelVector, pFriction);
        Vec3 oldDeltaMovement = instance.getDeltaMovement();
        double horizontalDistance = oldDeltaMovement.horizontalDistance();
        Vec3 originalResult = originalOperation.call(instance, pTravelVector, pFriction);
        if (instance.horizontalCollision && !instance.level().isClientSide) {
            double length = horizontalDistance - instance.getDeltaMovement().horizontalDistance();
            if (length > 0.41f) {
                instance.hurt(instance.damageSources().source(Tweaks.COLLIDE_WITH_WALL, null), (float) ((length - 0.2f) * collideWithWallsDamage));

                if (!instance.level().isClientSide) {
                    double x = instance.getX();
                    double y = instance.getY();
                    double z = instance.getZ();
                    Direction direction = Direction.getNearest(oldDeltaMovement.x, oldDeltaMovement.y, oldDeltaMovement.z);
                    BlockPos pos = BlockPos.containing(x, y, z).relative(direction);
                    BlockState state = instance.level().getBlockState(pos);
                    if (state.isAir()) {
                        int height = Mth.ceil(instance.getBbHeight());
                        for (int i = 1; i < height; i++) {
                            pos = pos.above();
                            state = instance.level().getBlockState(pos);
                            if (!state.isAir())
                                break;
                        }
                    }
                    if (state.isAir())
                        return originalResult;

                    int particleCount = 150;
                    instance.playSound(instance.getFallSounds().big(), 1.0F, 0.7F);
                    ((ServerLevel)instance.level()).sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state).setPos(pos), x, instance.getY() + instance.getBbHeight() / 2f, z, particleCount, 0.0D, 0.0D, 0.0D, 0.15F);
                }
            }
        }
        return originalResult;
    }
}
