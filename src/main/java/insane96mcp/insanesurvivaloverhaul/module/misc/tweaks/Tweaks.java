package insane96mcp.insanesurvivaloverhaul.module.misc.tweaks;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.data.generator.ISOBlockTagsProvider;
import insane96mcp.insanesurvivaloverhaul.data.generator.ISOItemTagsProvider;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.CatVariantTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@LoadFeature(module = ISOModules.MISC, description = "Various stuff that doesn't fit in any other Feature.")
public class Tweaks extends Feature {

    public static final GameRules.Key<GameRules.BooleanValue> RULE_DISCRETE_NAME_TAGS = GameRules.register("insanesurvivaloverhaul:discrete_name_tags", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true, (server, booleanValue) -> {
        for (ServerPlayer serverPlayer : server.getPlayerList().getPlayers()) {
            ClientboundDiscreteNameTagsPacket.sync(booleanValue.get(), serverPlayer);
        }
    }));

    public static final TagKey<Block> BREAK_ON_FALL = ISOBlockTagsProvider.create("break_on_fall");
    public static final TagKey<Item> WORLD_IMMUNE = ISOItemTagsProvider.create("world_immune");

    @Config(description = "Falling on glass has a chance of breaking it. The higher the fall, the higher the chance. insanesurvivaloverhaul:fall_on_break block tag can be used to add more blocks that break when falling onto them.")
    public static Boolean fallingBreakingGlass = true;

    @Config(description = "When you die in hardcore, your spawn point is set to where you died and a lightning strike is summoned")
    public static Boolean betterHardcoreDeath = true;

    @Config(description = "If enabled, the Totem of Undying will give Resistance IV for 5.5 seconds")
    public static Boolean totemResistance = true;

    @Config(min = 0, description = "Vanilla is 5")
    public static Integer leashMaxDistance = 16;

    @Config(min = -1, max = 0, description = "The speed modifier when frozen. Vanilla is -0.05")
    public static Double frozenMovementSpeedModifier = -0.1d;

    @Config(description = "Plays a sound effect when a mob is hit at least from this distance.")
    public static Integer dingDistance = 40;

    @Config
    public static Boolean blindnessNoLongerPreventsSprinting = true;

	@Config(min = 0d, description = "The speed divider when off ground. Vanilla is 5")
	public static Double offGroundSpeedDivider = 3d;

    @Config(description = "How much damage vehicles require to be broken, vanilla is 4")
    public static Integer damageToBreakVehicles = 2;

    @Config(min = 0d, description = "The fall distance (in blocks) needed before parrots (and other shoulder entities) get knocked off the player's shoulder. Vanilla is 0.5")
    public static Double parrot$shoulderDismountFallDistance = 4d;

    @Config(description = "If true, makes ocelots transform into cats when tamed. Like pre ocelot-cat split.")
    public static Boolean ocelotToCat = true;

    @Config(min = 2, max = 9, description = "Max amount of matching, repairable items that can be merged together in a crafting grid to combine their durability (vanilla's repair recipe). Vanilla is 2.")
    public static Integer repairMergeMaxItems = 9;

    public static boolean discreteNameTags = true;

    public static boolean doesBlindnessPreventSprint() {
        return Feature.isEnabled(Tweaks.class) && Tweaks.blindnessNoLongerPreventsSprinting;
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
    public void onEffectRemoved(MobEffectEvent.Remove event) {
        if (!this.isEnabled()
                || !appliedResistance
                || !event.getEffect().equals(MobEffects.DAMAGE_RESISTANCE))
            return;
        event.setCanceled(true);
        appliedResistance = false;
    }

    @SubscribeEvent
    public void onAnimalTame(AnimalTameEvent event) {
        if (!this.isEnabled()
                || !ocelotToCat
                || !(event.getAnimal() instanceof Ocelot ocelot)
                || ocelot.level().isClientSide)
            return;

        event.setCanceled(true);

        Cat cat = ocelot.convertTo(EntityType.CAT, true);
        if (cat == null)
            return;

        BuiltInRegistries.CAT_VARIANT.getRandomElementOf(CatVariantTags.DEFAULT_SPAWNS, cat.getRandom()).ifPresent(cat::setVariant);
        cat.tame(event.getTamer());

        ServerLevel level = (ServerLevel) cat.level();
        level.sendParticles(ParticleTypes.HEART, cat.getX(), cat.getY() + cat.getBbHeight() / 2d, cat.getZ(), 7, 0.3, 0.3, 0.3, 0.0);
        level.playSound(null, cat.blockPosition(), SoundEvents.CAT_PURR, SoundSource.NEUTRAL, 1f, 1f);
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
        ClientboundDiscreteNameTagsPacket.sync(event.getEntity().level().getGameRules().getBoolean(RULE_DISCRETE_NAME_TAGS), (ServerPlayer) event.getEntity());
    }

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onBreakSpeed(PlayerEvent.BreakSpeed event) {
		if (!this.isEnabled()
				|| offGroundSpeedDivider == 5d
				|| event.getEntity().onGround())
			return;

		event.setNewSpeed(event.getNewSpeed() * 5f / offGroundSpeedDivider.floatValue());
	}
}
