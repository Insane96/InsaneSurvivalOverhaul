package insane96mcp.insanesurvivaloverhaul.module.combat.regeneratingabsorption;

import insane96mcp.insanelib.core.ModNBTData;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
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
import net.minecraft.util.Mth;
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
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

@LoadFeature(module = ISOModules.COMBAT, description = "Adds a new attribute to add regenerating absorption hearts to the player. Please note that default mods' regenerating absorption is done through item definitions within the combat_rework data pack and via Absorption Armor feature.", canBeDisabled = false)
public class RegeneratingAbsorption extends Feature {
    public static final DeferredHolder<SoundEvent, SoundEvent> ABSORPTION_HIT = ISORegistries.SOUND_EVENTS.register("absorption_hit", () -> SoundEvent.createFixedRangeEvent(InsaneSO.id("absorption_hit"), 16f));

    public static ResourceLocation REGEN_ABSORPTION_TAG;
    public static ResourceLocation HURT_COOLDOWN_TAG;
    public static ResourceLocation NO_HURT_SOUND_TAG;

    public static final DeferredHolder<Attribute, Attribute> ATTRIBUTE = ISORegistries.ATTRIBUTES.register("regenerating_absorption", () -> new RangedAttribute("attribute.name.regenerating_absorption", 0d, 0d, 1024d));

    public static final DeferredHolder<Attribute, Attribute> SPEED_ATTRIBUTE = ISORegistries.ATTRIBUTES.register("regenerating_absorption_speed", () -> new RangedAttribute("attribute.name.regenerating_absorption_speed", 0.250d, 0d, 20d));

    public static final DeferredHolder<MobEffect, MobEffect> EFFECT = ISORegistries.MOB_EFFECTS.register("regenerating_absorption", () -> new ILMobEffect(MobEffectCategory.BENEFICIAL, 0x818894)
            .addAttributeModifier(ATTRIBUTE, InsaneSO.id("regenerating_absorption_effect"), 4, AttributeModifier.Operation.ADD_VALUE)
            .addAttributeModifier(SPEED_ATTRIBUTE, InsaneSO.id("regenerating_absorption_effect"), 0.125d, AttributeModifier.Operation.ADD_VALUE));

    @Config(min = 0, name = "Un-damaged time to regen", description = "Ticks that must pass from the last hit to regen absorption hearts. This is affected by regenerating absorption speed (absorp regen speed * this)")
    public static Integer unDamagedTimeToRegen = 150;
    @Config(min = 0, name = "Un-damaged time to regen cap", description = "Min Un-damaged time to regen")
    public static Integer unDamagedTimeToRegenCap = 60;
    @Config(description = "The amount of regenerating absorption hearts cannot go over the entity's current health.")
    public static Boolean capToHealth = true;
    @Config(description = "How fast (each tick) will absorption hearts decay when higher than the current maximum.")
    public static Double decaySpeed = 0.1d;
    @Config(name = "Absorbing bypasses_armor damage only", description = "If true, absorption hearts will not shield from damages in the bypasses_armor damage type tag.")
    public static Boolean absorbingDamageTypeTagOnly = true;
    @Config(description = "If true, a sound is played when the absorption is damaged.")
    public static Boolean soundOnAbsorptionHurt = true;
    //TODO Config option to replace vanilla absorption with this
    @Config(description = "(Client only) If true, regenerating absorption hearts are rendered on the right instead on top of hearts.")
    public static Boolean renderOnTheRight = false;

    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        REGEN_ABSORPTION_TAG = this.createDataKey("regen_absorption");
        HURT_COOLDOWN_TAG = this.createDataKey("regen_absorption_hurt_cooldown");
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

        int hurtCooldown = ModNBTData.get(livingEntity, HURT_COOLDOWN_TAG, Integer.class);
        if (hurtCooldown > 0) {
            hurtCooldown--;
            ModNBTData.put(livingEntity, HURT_COOLDOWN_TAG, hurtCooldown);
            return;
        }
        float maxAbsorption = (float) livingEntity.getAttributeValue(ATTRIBUTE);
        float regenSpeed = (float) (livingEntity.getAttributeValue(SPEED_ATTRIBUTE) / 20f);

        float currentAbsorption = ModNBTData.get(livingEntity, REGEN_ABSORPTION_TAG, Float.class);
        if (capToHealth)
            maxAbsorption = Math.min(maxAbsorption, Mth.ceil(livingEntity.getHealth()));
        if (currentAbsorption < 0f || currentAbsorption == maxAbsorption)
            return;

        if (currentAbsorption > maxAbsorption)
            currentAbsorption = Math.max(currentAbsorption - decaySpeed.floatValue(), 0f);
        else
            currentAbsorption = Math.min(currentAbsorption + regenSpeed, maxAbsorption);

        if (livingEntity instanceof ServerPlayer player)
            ClientboundRegenAbsorptionPacket.sync(player, currentAbsorption);
        ModNBTData.put(livingEntity, REGEN_ABSORPTION_TAG, currentAbsorption);
    }

    @SubscribeEvent
    public void onEntityHurt(LivingDamageEvent.Post event) {
        if (!this.isEnabled()
                || event.getEntity().level().isClientSide
                || unDamagedTimeToRegen == 0
                || event.getSource().is(DamageTypeTags.BYPASSES_ARMOR))
            return;

        double absorptionSpeed = event.getEntity().getAttributeValue(SPEED_ATTRIBUTE);
        ModNBTData.put(event.getEntity(), HURT_COOLDOWN_TAG, (int) Math.max(unDamagedTimeToRegen * (1f - absorptionSpeed), unDamagedTimeToRegenCap));
    }

    @SubscribeEvent
    public void onLivingHurtPreAbsorption(LivingDamageEventPreAbsorp event) {
        if (!this.isEnabled()
                || !canDamageAbsorption(event.getSource())
                || event.getNewDamage() <= 0)
            return;

        float currentAbsorption = ModNBTData.get(event.getEntity(), REGEN_ABSORPTION_TAG, Float.class);
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

    public static boolean canDamageAbsorption(DamageSource source) {
        if (!absorbingDamageTypeTagOnly)
            return true;
        return source.getEntity() != null && !source.is(DamageTypeTags.BYPASSES_ARMOR);
    }
}
