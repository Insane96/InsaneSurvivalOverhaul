package insane96mcp.iguanatweaksreborn.module.misc.tweaks;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import insane96mcp.iguanatweaksreborn.InsaneSO;
import insane96mcp.iguanatweaksreborn.data.generator.ISOBlockTagsProvider;
import insane96mcp.iguanatweaksreborn.data.generator.ISOItemTagsProvider;
import insane96mcp.iguanatweaksreborn.integration.TConstruct;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.network.message.SyncDiscreteNameTags;
import insane96mcp.iguanatweaksreborn.setup.ISORegistries;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.MinMax;
import insane96mcp.insanelib.util.MathHelper;
import insane96mcp.insanelib.util.ModNBTData;
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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
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
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.RegistryObject;

@LoadFeature(module = Modules.Ids.MISC, description = "Various stuff that doesn't fit in any other Feature.")
public class Tweaks extends Feature {

    public static ResourceLocation WAS_BREATHING;
    public static ResourceLocation TICK_SINCE_OUT_OF_WATER;
    public static ResourceLocation TIMES_DROWNED;

    public static final GameRules.Key<GameRules.IntegerValue> RULE_PAINFUL_WORLD_BORDER = GameRules.register("iguanatweaks:painfulWorldBorder", GameRules.Category.MISC, GameRules.IntegerValue.create(0));
    public static final GameRules.Key<GameRules.BooleanValue> RULE_DISCRETE_NAME_TAGS = GameRules.register("iguanatweaks:discreteNameTags", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true, (server, booleanValue) -> {
        for (ServerPlayer serverPlayer : server.getPlayerList().getPlayers()) {
            SyncDiscreteNameTags.sync(booleanValue.get(), serverPlayer);
        }
    }));

    public static final RegistryObject<Block> SCUTE = ISORegistries.BLOCKS.register("scute", () -> new ScuteBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GREEN).strength(0.2F, 0.5F).offsetType(BlockBehaviour.OffsetType.XZ).dynamicShape().sound(SoundType.BONE_BLOCK)));

    public static final TagKey<Block> BREAK_ON_FALL = ISOBlockTagsProvider.create("break_on_fall");
    public static final TagKey<Item> WORLD_IMMUNE = ISOItemTagsProvider.create("world_immune");

    public static ResourceKey<DamageType> COLLIDE_WITH_WALL = ResourceKey.create(Registries.DAMAGE_TYPE, InsaneSO.location("collide_with_wall"));

    @Config(description = "Falling on glass has a chance of breaking it. The higher the fall, the higher the chance. iguanatweaksreborn:fall_on_break block tag can be used to add more blocks that break when falling onto them.")
    public static Boolean fallingBreakingGlass = true;

    @Config(description = "The maximum amount of blocks a sponge can soak. (Vanilla is 64, disabled if quark is installed)")
    public static Integer sponge$maxSoakBlocks = 256;
    @Config(description = "The maximum range at which sponges will check for soakable blocks. (Vanilla is 5, disabled if quark is installed)")
    public static Integer sponge$maxSoakRange = 10;
    @Config(description = "If exposed to the sun sponges may dry and if exposed to rain sponges might get wet")
    public static Boolean sponges$dryWetWeather = true;

    @Config(description = "When you die in hardcore, your spawn point is set to where you died and a lightning strike is summoned")
    public static Boolean betterHardcoreDeath = true;

    @Config(min = 0, max = 100, description = "The amount of ticks the entities consumes when underwater. In vanilla it's 1 without Respiration enchantment. For non integer numbers the decimal part will count as a chance to have a +1")
    public static Double breathe$airTicksConsumed = 1.5d;

    @Config(min = 0, max = 100, description = "Every how many ticks will entities drown")
    public static Integer breathe$drownSpeed = 30;
    @Config(description = "The amount of air ticks the entities regains each tick when out of water. Min is the amount as soon as you exit water, Max is a few seconds out of water. For non integer numbers the decimal part will count as a chance to have a +1. Vanilla is 4.")
    public static MinMax breathe$airTicksRefilled = new MinMax(1, 2.5);
    @Config
    public static Boolean breathe$increaseDrownDamageTheMoreDrowning = true;
    @Config(description = "If enabled, the Totem of Undying will give Resistance IV for 5.5 seconds")
    public static Boolean totemResistance = true;

    @Config(description = "The ticks of Water Breathing given by the Turtle Helmet. Vanilla is 200")
    public static Integer turtle$helmetWaterBreathingTime = 900;
    @Config(description = "If true scutes will drop as a block and not as item")
    public static Boolean turtle$scuteDropsAsBlock = true;

    @Config(min = 0, description = "If set higher than 0 it will enable damage when colliding with walls at a high speed (e.g. with explosions or knockback). Higher = more damage. Set to 0 to disable. Please note that even if set to 0, might still show up in performance profilers.")
    public static Double collideWithWallsDamage = 3d;

    @Config(min = 0, description = "Vanilla is 10")
    public static Double leashMaxDistance = 16d;

    @Config(min = -1, max = 0, description = "The speed modifier when frozen. Vanilla is -0.05")
    public static Double frozenMovementSpeedModifier = -0.1d;

    @Config(description = "Plays a sound effect when a mob is hit at least from this distance.")
    public static Integer dingDistance = 40;

    @Config
    public static Boolean blindnessNoLongerPreventsSprinting = true;

    public static boolean discreteNameTags = true;

    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        WAS_BREATHING = this.createDataKey("was_breathing");
        TICK_SINCE_OUT_OF_WATER = this.createDataKey("tick_since_out_of_water");
        TIMES_DROWNED = this.createDataKey("times_drowned");
    }

    @Override
    public void readConfig(ModConfigEvent event) {
        super.readConfig(event);
        Blocks.WET_SPONGE.isRandomlyTicking = sponges$dryWetWeather;
        Blocks.SPONGE.isRandomlyTicking = sponges$dryWetWeather;
    }

    public static boolean doesBlindnessPreventSprint() {
        return Feature.isEnabled(Tweaks.class)  && Tweaks.blindnessNoLongerPreventsSprinting;
    }

    public static int changeMaxSpongeSoakBlocks(int soakableBlocks) {
        if (!isEnabled(Tweaks.class))
            return soakableBlocks;

        //Vanilla uses 65 and not 64
        return sponge$maxSoakBlocks + 1;
    }

	public static int changeSpongeMaxRange(int range) {
		if (!isEnabled(Tweaks.class))
			return range;

		//Vanilla uses < instead of <=
		return sponge$maxSoakRange + 1;
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
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        SyncDiscreteNameTags.sync(event.getEntity().level().getGameRules().getBoolean(RULE_DISCRETE_NAME_TAGS), (ServerPlayer) event.getEntity());
    }

    @SubscribeEvent
    public void onBreathe(LivingBreatheEvent event) {
        if (!this.isEnabled()
                || event.getEntity().level().isClientSide)
            return;

        boolean wasBreathing = ModNBTData.get(event.getEntity(), WAS_BREATHING, Boolean.class);
        long ticksSinceOutOfWater = event.getEntity().level().getGameTime() - ModNBTData.get(event.getEntity(), TICK_SINCE_OUT_OF_WATER, Long.class);
        if (!wasBreathing && event.canBreathe()) {
            ticksSinceOutOfWater = 0;
            ModNBTData.put(event.getEntity(), TICK_SINCE_OUT_OF_WATER, event.getEntity().level().getGameTime());
        }
        int airConsumed = MathHelper.getAmountWithDecimalChance(event.getEntity().getRandom(), breathe$airTicksConsumed);
        int respiration = EnchantmentHelper.getRespiration(event.getEntity());
        airConsumed = respiration > 0 && event.getEntity().getRandom().nextInt(respiration + 1) > 0 ? 0 : airConsumed;
        if (event.getEntity().getAirSupply() <= 0)
            airConsumed = 1;
        event.setConsumeAirAmount(airConsumed);

        int refillAmount = MathHelper.getAmountWithDecimalChance(event.getEntity().getRandom(), breathe$airTicksRefilled.min);
        if (ticksSinceOutOfWater > 75) {
            refillAmount = MathHelper.getAmountWithDecimalChance(event.getEntity().getRandom(), breathe$airTicksRefilled.max);
            if (event.canBreathe())
                setTimesDrowned(event.getEntity(), 0);
        }
        event.setRefillAirAmount(refillAmount);
        ModNBTData.put(event.getEntity(), WAS_BREATHING, event.canBreathe());
    }

    @SubscribeEvent
    public void onDrown(LivingDrownEvent event) {
        if (!this.isEnabled()
                || !breathe$increaseDrownDamageTheMoreDrowning
                || !event.getEntity().canDrownInFluidType(event.getEntity().getEyeInFluidType()))
            return;

        event.setDrowning(event.getEntity().getAirSupply() <= -breathe$drownSpeed);
        if (event.isDrowning()) {
            int timesDrowned = getTimesDrowned(event.getEntity());
            setTimesDrowned(event.getEntity(), ++timesDrowned);

            event.setDamageAmount(event.getDamageAmount() * timesDrowned * 0.5f);
            event.setBubbleCount((int) (event.getBubbleCount() * timesDrowned * 0.5f));
        }
    }

    public static void setTimesDrowned(LivingEntity entity, int timesDrowned) {
        ModNBTData.put(entity, TIMES_DROWNED, timesDrowned);
    }

    public static int getTimesDrowned(LivingEntity entity) {
        return ModNBTData.get(entity, TIMES_DROWNED, Integer.class);
    }

    public static Vec3 onCollideWithWall(LivingEntity living, Vec3 pTravelVector, float pFriction, Operation<Vec3> originalOperation) {
        if (!Feature.isEnabled(Tweaks.class)
                || collideWithWallsDamage == 0
                || (living instanceof Mob mob && mob.isLeashed())
                || (ModList.get().isLoaded("tconstruct") && TConstruct.hasBouncy(living)))
            return originalOperation.call(living, pTravelVector, pFriction);
        Vec3 oldDeltaMovement = living.getDeltaMovement();
        double horizontalDistance = oldDeltaMovement.horizontalDistance();
        Vec3 originalResult = originalOperation.call(living, pTravelVector, pFriction);
        if (living.horizontalCollision && !living.level().isClientSide) {
            double length = horizontalDistance - living.getDeltaMovement().horizontalDistance();
            if (length > 0.6f) {
                living.hurt(living.damageSources().source(Tweaks.COLLIDE_WITH_WALL, null), (float) ((length - 0.6f) * collideWithWallsDamage));

                if (!living.level().isClientSide) {
                    double x = living.getX();
                    double y = living.getY();
                    double z = living.getZ();
                    Direction direction = Direction.getNearest(oldDeltaMovement.x, oldDeltaMovement.y, oldDeltaMovement.z);
                    BlockPos pos = BlockPos.containing(x, y, z).relative(direction);
                    BlockState state = living.level().getBlockState(pos);
                    if (state.isAir()) {
                        int height = Mth.ceil(living.getBbHeight());
                        for (int i = 1; i < height; i++) {
                            pos = pos.above();
                            state = living.level().getBlockState(pos);
                            if (!state.isAir())
                                break;
                        }
                    }
                    if (state.isAir())
                        return originalResult;

                    int particleCount = 150;
                    living.playSound(living.getFallSounds().big(), 1.0F, 0.7F);
                    ((ServerLevel)living.level()).sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state).setPos(pos), x, living.getY() + living.getBbHeight() / 2f, z, particleCount, 0.0D, 0.0D, 0.0D, 0.15F);
                }
            }
        }
        return originalResult;
    }
}
