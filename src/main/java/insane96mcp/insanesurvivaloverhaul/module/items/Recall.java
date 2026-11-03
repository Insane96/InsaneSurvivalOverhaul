package insane96mcp.insanesurvivaloverhaul.module.items;

import insane96mcp.insanelib.core.feature.ConfigType;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.world.effect.ILMobEffect;
import insane96mcp.insanelib.world.scheduled.ScheduledTasks;
import insane96mcp.insanelib.world.scheduled.ScheduledTickTask;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.setup.ISORegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.portal.DimensionTransition;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * In the common config as the server builds its brewing recipes before the server config is loaded.
 */
@ConfigType(ModConfig.Type.COMMON)
@LoadFeature(module = ISOModules.ITEMS, name = "Recall Potion", description = "Adds the Potion of Recall: when its effect ends, players and their pets are sent back to the player's respawn point. Brewed by adding a Wither Rose to a Potion of Regeneration II, then an Echo Shard.")
public class Recall extends Feature {
    public static final DeferredHolder<MobEffect, ILMobEffect> BACK_TO_SPAWN = ISORegistries.MOB_EFFECTS.register("back_to_spawn", () -> new ILMobEffect(MobEffectCategory.BENEFICIAL, 0x6600ff));
    public static final DeferredHolder<Potion, Potion> REGEN_WITHER = ISORegistries.POTIONS.register("regen_wither", () -> new Potion("regen_wither", new MobEffectInstance(MobEffects.REGENERATION, 450), new MobEffectInstance(MobEffects.WITHER, 450)));
    public static final DeferredHolder<Potion, Potion> RECALL = ISORegistries.POTIONS.register("recall", () -> new Potion("recall", new MobEffectInstance(BACK_TO_SPAWN, 300)));

    @SubscribeEvent
    public void onRegisterBrewingRecipes(RegisterBrewingRecipesEvent event) {
        if (!this.isEnabled())
            return;

        event.getBuilder().addMix(Potions.STRONG_REGENERATION, Items.WITHER_ROSE, REGEN_WITHER);
        event.getBuilder().addMix(REGEN_WITHER, Items.ECHO_SHARD, RECALL);
    }

    @SubscribeEvent
    public void onEffectExpired(MobEffectEvent.Expired event) {
        //Compares the values as effects read from items or saves have a different Holder than the DeferredHolder
        if (!this.isEnabled()
                || event.getEffectInstance().getEffect().value() != BACK_TO_SPAWN.get()
                || event.getEntity().level().isClientSide
                || event.getEntity().isDeadOrDying())
            return;

        //Teleporting while effects are being ticked would copy the still active effect on entities changing dimension
        LivingEntity entity = event.getEntity();
        ScheduledTasks.schedule(new ScheduledTickTask(0) {
            @Override
            public void run() {
                sendToRespawnPoint(entity);
            }
        });
    }

    private static void sendToRespawnPoint(LivingEntity entity) {
        ServerPlayer player;
        if (entity instanceof ServerPlayer serverPlayer)
            player = serverPlayer;
        else if (entity instanceof OwnableEntity ownableEntity && ownableEntity.getOwner() instanceof ServerPlayer owner)
            player = owner;
        else
            return;
        if (entity.isDeadOrDying() || player.getRespawnPosition() == null)
            return;

        //keepInventory prevents consuming a Respawn Anchor charge
        DimensionTransition transition = player.findRespawnPositionAndUseSpawnBlock(true, DimensionTransition.DO_NOTHING);
        if (transition.missingRespawnBlock())
            return;

        if (entity.isSleeping() && entity instanceof ServerPlayer sleepingPlayer)
            sleepingPlayer.stopSleepInBed(true, true);
        entity.stopRiding();
        Entity teleported = entity.changeDimension(transition);
        if (teleported == null)
            return;

        teleported.resetFallDistance();
        //Delayed to let the client receive the teleport first
        ScheduledTasks.schedule(new ScheduledTickTask(2) {
            @Override
            public void run() {
                teleported.level().playSound(null, teleported, SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 0.6f, 1f);
            }
        });
    }
}
