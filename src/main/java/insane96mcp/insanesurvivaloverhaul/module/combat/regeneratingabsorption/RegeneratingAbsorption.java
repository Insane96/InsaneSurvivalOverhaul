package insane96mcp.insanesurvivaloverhaul.module.combat.regeneratingabsorption;

import insane96mcp.insanelib.core.ModNBTData;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.module.base.InCombat;
import insane96mcp.insanelib.world.effect.ILMobEffect;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.event.LivingDamageEventPreAbsorp;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.setup.ISORegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

@LoadFeature(module = ISOModules.COMBAT, description = "Adds a new attribute to add regenerating absorption hearts to the player. Please note that default mods' regenerating absorption is done through item definitions within the combat_rework data pack and via Absorption Armor feature.", canBeDisabled = false)
public class RegeneratingAbsorption extends Feature {
    public static final DeferredHolder<SoundEvent, SoundEvent> ABSORPTION_HIT = ISORegistries.SOUND_EVENTS.register("absorption_hit", () -> SoundEvent.createFixedRangeEvent(InsaneSO.id("absorption_hit"), 16f));

    public static ResourceLocation REGEN_ABSORPTION_TAG;
    public static ResourceLocation NO_HURT_SOUND_TAG;

    public static final DeferredHolder<Attribute, Attribute> ATTRIBUTE = ISORegistries.ATTRIBUTES.register("regenerating_absorption", () -> new RangedAttribute("attribute.name.regenerating_absorption", 0d, 0d, 1024d).setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> SPEED_ATTRIBUTE = ISORegistries.ATTRIBUTES.register("regenerating_absorption_speed", () -> new RangedAttribute("attribute.name.regenerating_absorption_speed", 0.250d, 0d, 20d).setSyncable(true));

    public static final DeferredHolder<MobEffect, MobEffect> EFFECT = ISORegistries.MOB_EFFECTS.register("regenerating_absorption", () -> new ILMobEffect(MobEffectCategory.BENEFICIAL, 0x818894)
            .addAttributeModifier(ATTRIBUTE, InsaneSO.id("regenerating_absorption_effect"), 4, AttributeModifier.Operation.ADD_VALUE)
            .addAttributeModifier(SPEED_ATTRIBUTE, InsaneSO.id("regenerating_absorption_effect"), 0.125d, AttributeModifier.Operation.ADD_VALUE));

    @Config(min = 0, description = "If the player has been in combat (attacked or attacking) since this seconds regen speed is multiplied by 'Regen speed'")
    public static Double inCombat$time = 8d;
    @Config(min = 0, max = 1, description = "If the player is in combat, regen speed is multiplied by this")
    public static Double inCombat$regenSpeed = 0d;
    @Config(description = "The amount of regenerating absorption hearts cannot go over the entity's current health.")
    public static Boolean capToHealth = true;
    @Config(description = "How many absorption hearts are lost each second when higher than the current maximum.")
    public static Double decaySpeed = 2d;
    @Config(name = "Absorbing bypasses_armor damage only", description = "If true, absorption hearts will not shield from damages in the bypasses_armor damage type tag.")
    public static Boolean absorbingDamageTypeTagOnly = true;
    @Config(description = "If true, a sound is played when the absorption is damaged.")
    public static Boolean soundOnAbsorptionHurt = true;

    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        REGEN_ABSORPTION_TAG = this.createDataKey("regen_absorption");
        NO_HURT_SOUND_TAG = this.createDataKey("no_hurt_sound");
    }

    public static void addAttribute(EntityAttributeModificationEvent event) {
        for (EntityType<? extends LivingEntity> entityType : event.getTypes()) {
            if (!event.has(entityType, ATTRIBUTE))
                event.add(entityType, ATTRIBUTE);
            if (!event.has(entityType, SPEED_ATTRIBUTE))
                event.add(entityType, SPEED_ATTRIBUTE);
        }
    }

    @SubscribeEvent
    public void onLivingTick(EntityTickEvent.Pre event) {
        if (!this.isEnabled()
                || event.getEntity().level().isClientSide
                || !(event.getEntity() instanceof LivingEntity livingEntity)
                || livingEntity.isDeadOrDying())
            return;

        float maxAbsorption = getCappedMaxAbsorption(livingEntity);
        float regenSpeed = getRegenSpeed(livingEntity) / 20f;
        if (InCombat.isInCombat(livingEntity, inCombat$time))
            regenSpeed *= inCombat$regenSpeed;

        float currentAbsorption = getCurrentAbsorption(livingEntity);
        if (currentAbsorption < 0f || currentAbsorption == maxAbsorption)
            return;

        if (currentAbsorption > maxAbsorption)
            currentAbsorption = Math.max(currentAbsorption - decaySpeed.floatValue() / 20f, 0f);
        else
            currentAbsorption = Math.min(currentAbsorption + regenSpeed, maxAbsorption);

        if (livingEntity instanceof ServerPlayer player)
            ClientboundRegenAbsorptionPacket.sync(player, currentAbsorption);
        ModNBTData.put(livingEntity, REGEN_ABSORPTION_TAG, currentAbsorption);
    }

    @SubscribeEvent
    public void onLivingHurtPreAbsorption(LivingDamageEventPreAbsorp event) {
        if (!this.isEnabled()
                || !canDamageAbsorption(event.getSource())
                || event.getNewDamage() <= 0)
            return;

        float currentAbsorption = getCurrentAbsorption(event.getEntity());
        if (currentAbsorption <= 0)
            return;
        //if (regenAbsorption < event.getAmount())
            //event.getEntity().level().playSound(null, event.getEntity(), SoundEvents.GENERIC_EXPLODE, event.getEntity() instanceof Player ? SoundSource.PLAYERS : SoundSource.HOSTILE, 0.5f, 2f);
        //else
            //event.getEntity().getPersistentData().putBoolean(NO_HURT_SOUND_TAG, true);
        float toRemove = Math.min(currentAbsorption, event.getNewDamage());
        currentAbsorption -= toRemove;
        event.setNewDamage(event.getNewDamage() - toRemove);
        ModNBTData.put(event.getEntity(), REGEN_ABSORPTION_TAG, currentAbsorption);
        if (soundOnAbsorptionHurt)
            event.getEntity().level().playSound(null, event.getEntity(), ABSORPTION_HIT.get(), event.getEntity() instanceof Player ? SoundSource.PLAYERS : SoundSource.HOSTILE, 1f, 2f);
        if (event.getEntity() instanceof ServerPlayer player)
            ClientboundRegenAbsorptionPacket.sync(player, currentAbsorption);
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        syncToClient(event.getEntity());
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        syncToClient(event.getEntity());
    }

    @SubscribeEvent
    public void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        syncToClient(event.getEntity());
    }

    /**
     * The absorption is synced only when it changes, so it has to be sent when the client player is (re)created
     */
    private void syncToClient(Player player) {
        if (this.isEnabled()
                && player instanceof ServerPlayer serverPlayer)
            ClientboundRegenAbsorptionPacket.sync(serverPlayer, getCurrentAbsorption(serverPlayer));
    }

    public static boolean canDamageAbsorption(DamageSource source) {
        if (!absorbingDamageTypeTagOnly)
            return true;
        return source.getEntity() != null && !source.is(DamageTypeTags.BYPASSES_ARMOR);
    }

    public static float getCurrentAbsorption(LivingEntity entity) {
        return ModNBTData.get(entity, REGEN_ABSORPTION_TAG, Float.class);
    }

    public static float getMaxAbsorption(LivingEntity entity) {
        return (float) entity.getAttributeValue(ATTRIBUTE);
    }

    public static float getCappedMaxAbsorption(LivingEntity entity) {
        float maxAbsorption = getMaxAbsorption(entity);
        if (capToHealth && maxAbsorption > Math.ceil(entity.getHealth()))
            maxAbsorption = (float) Math.ceil(entity.getHealth());
        return maxAbsorption;
    }

    public static float getRegenSpeed(LivingEntity entity) {
        return (float) entity.getAttributeValue(SPEED_ATTRIBUTE);
    }
}
