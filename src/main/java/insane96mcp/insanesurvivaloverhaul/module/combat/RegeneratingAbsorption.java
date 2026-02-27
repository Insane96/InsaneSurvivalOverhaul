package insane96mcp.insanesurvivaloverhaul.module.combat;

import com.mojang.blaze3d.systems.RenderSystem;
import insane96mcp.insanelib.core.ModNBTData;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.util.ClientUtils;
import insane96mcp.insanelib.world.effect.ILMobEffect;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.network.message.RegenAbsorptionSyncMessage;
import insane96mcp.insanesurvivaloverhaul.setup.ISORegistries;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

@LoadFeature(module = ISOModules.COMBAT, description = "Adds a new attribute to add regenerating absorption hearts to the player. Please note that default mods' regenerating absorption is done through item definitions within the combat_rework data pack and via Absorption Armor feature, disabling this will only disable the regenerating absorption functionality.")
public class RegeneratingAbsorption extends Feature {
    public static final DeferredHolder<SoundEvent, SoundEvent> ABSORPTION_HIT = ISORegistries.SOUND_EVENTS.register("absorption_hit", () -> SoundEvent.createFixedRangeEvent(InsaneSO.location("absorption_hit"), 16f));

    public static final ResourceLocation GUI_ICONS = InsaneSO.location("textures/sprites/hud/regenerating_absorption.png");
    public static ResourceLocation REGEN_ABSORPTION_TAG;
    public static ResourceLocation HURT_COOLDOWN_TAG;
    public static ResourceLocation NO_HURT_SOUND_TAG;

    public static final DeferredHolder<Attribute, Attribute> ATTRIBUTE = ISORegistries.ATTRIBUTES.register("regenerating_absorption", () -> new RangedAttribute("attribute.name.regenerating_absorption", 0d, 0d, 1024d));

    public static final DeferredHolder<Attribute, Attribute> SPEED_ATTRIBUTE = ISORegistries.ATTRIBUTES.register("regenerating_absorption_speed", () -> new RangedAttribute("attribute.name.regenerating_absorption_speed", 0.250d, 0d, 20d));

    public static final DeferredHolder<MobEffect, MobEffect> EFFECT = ISORegistries.MOB_EFFECTS.register("regenerating_absorption", () -> new ILMobEffect(MobEffectCategory.BENEFICIAL, 0x818894)
            .addAttributeModifier(ATTRIBUTE, InsaneSO.location("regenerating_absorption_effect"), 4, AttributeModifier.Operation.ADD_VALUE)
            .addAttributeModifier(SPEED_ATTRIBUTE, InsaneSO.location("regenerating_absorption_effect"), 0.125d, AttributeModifier.Operation.ADD_VALUE));

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
            RegenAbsorptionSyncMessage.sync(player, currentAbsorption);
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
    public void onLivingHurtPreAbsorption(LivingIncomingDamageEvent event) {
        if (!this.isEnabled()
                || !canDamageAbsorption(event.getSource())
                || event.getAmount() <= 0)
            return;

        float currentAbsorption = ModNBTData.get(event.getEntity(), REGEN_ABSORPTION_TAG, Float.class);
        if (currentAbsorption <= 0)
            return;
        //if (regenAbsorption < event.getAmount())
            //event.getEntity().level().playSound(null, event.getEntity(), SoundEvents.GENERIC_EXPLODE, event.getEntity() instanceof Player ? SoundSource.PLAYERS : SoundSource.HOSTILE, 0.5f, 2f);
        //else
            //event.getEntity().getPersistentData().putBoolean(NO_HURT_SOUND_TAG, true);
        float toRemove = Math.min(currentAbsorption, event.getAmount());
        currentAbsorption -= toRemove;
        event.setAmount(event.getAmount() - toRemove);
        ModNBTData.put(event.getEntity(), REGEN_ABSORPTION_TAG, currentAbsorption);
        if (soundOnAbsorptionHurt)
            event.getEntity().level().playSound(null, event.getEntity(), ABSORPTION_HIT.get(), event.getEntity() instanceof Player ? SoundSource.PLAYERS : SoundSource.HOSTILE, 1f, 2f);
        if (event.getEntity() instanceof ServerPlayer player)
            RegenAbsorptionSyncMessage.sync(player, currentAbsorption);
    }

    public static boolean canDamageAbsorption(DamageSource source) {
        if (!absorbingDamageTypeTagOnly)
            return true;
        return source.getEntity() != null && !source.is(DamageTypeTags.BYPASSES_ARMOR);
    }

    @OnlyIn(Dist.CLIENT)
    public static void registerGuiOverlays(RegisterGuiLayersEvent event) {
        ResourceLocation aboveOverlay = VanillaGuiLayers.PLAYER_HEALTH;
        if (ModList.get().isLoaded("stamina"))
            aboveOverlay = ResourceLocation.parse("stamina:stamina_overlay");
        if (renderOnTheRight) {
            //if (ModList.get().isLoaded("nohunger") && NoHungerIntegration.doesRenderArmorAtHunger())
            //    aboveOverlay = InsaneSO.location("armor");
            //else
                aboveOverlay = VanillaGuiLayers.FOOD_LEVEL;
        }
        Minecraft mc = Minecraft.getInstance();
        Gui gui = mc.gui;
        event.registerAbove(aboveOverlay, InsaneSO.location("regenerating_absorption"), (guiGraphics, partialTicks) -> {
            if (isEnabled(RegeneratingAbsorption.class) && mc.gameMode != null && mc.gameMode.canHurtPlayer())
                renderAbsorption(gui, guiGraphics, guiGraphics.guiWidth(), guiGraphics.guiHeight());
        });
    }

    static int lastAbsorption = 0;
    static long lastAbsorptionTime = 0;
    static long absorptionBlinkTime = 0;
    static int displayAbsorption = 0;

    @OnlyIn(Dist.CLIENT)
    protected static void renderAbsorption(Gui gui, GuiGraphics guiGraphics, int width, int height) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null)
            return;
        mc.getProfiler().push("regen_absorption");

        RenderSystem.enableBlend();
        int left = width / 2 + (!renderOnTheRight ? -91 : 82);
        int top = height - (!renderOnTheRight ? gui.leftHeight : gui.rightHeight);

        int absorption = Mth.ceil(ModNBTData.get(mc.player, REGEN_ABSORPTION_TAG, Float.class));
        boolean highlight = absorptionBlinkTime > (long) gui.getGuiTicks() && (absorptionBlinkTime - (long) gui.getGuiTicks()) / 3L % 2L == 1L;
        int v = highlight ? 9 : 0;

        if (absorption < lastAbsorption && player.invulnerableTime > 0)
        {
            lastAbsorptionTime = Util.getMillis();
            displayAbsorption = lastAbsorption;
            absorptionBlinkTime = gui.getGuiTicks() + 20;
        }
        else if (absorption > lastAbsorption)
        {
            //lastAbsorptionTime = Util.getMillis();
            displayAbsorption = absorption;
            absorptionBlinkTime = gui.getGuiTicks() + 10;
        }

        if (Util.getMillis() - lastAbsorptionTime > 1000L)
        {
            lastAbsorption = absorption;
            displayAbsorption = absorption;
            lastAbsorptionTime = Util.getMillis();
        }
        //player.displayClientMessage(Component.literal("Util.getMillis(): %s, lastAbsorption: %s, absorption: %s, absorptionBlinkTime: %s, displayAbsorption: %s".formatted(Util.getMillis() - lastAbsorptionTime, lastAbsorption, absorption, absorptionBlinkTime, displayAbsorption)), true);

        lastAbsorption = absorption;
        for (int i = 1; i <= displayAbsorption; i++)
        {
            if (i > absorption)
                ClientUtils.setRenderColor(1, 0, 0, 1f);
            //ClientUtils.blitVericallyMirrored(GUI_ICONS, guiGraphics, left, top, 9, v, 9, 9, 18, 18);
            int u = i % 2 == 0 ? 0 : 9;
            if (!renderOnTheRight)
                guiGraphics.blit(GUI_ICONS, left, top, u, v, 9, 9, 18, 18);
            else
                ClientUtils.blitVerticallyMirrored(GUI_ICONS, guiGraphics,left, top, u, v, 9, 9, 18, 18);
            if (i % 20 == 0 && i != displayAbsorption) {
                left = width / 2 + (!renderOnTheRight ? -91 : 82);
                top -= 10;
                if (!renderOnTheRight)
                    gui.leftHeight += 10;
                else
                    gui.rightHeight += 10;
            }
            else if (i % 2 == 0)
                left += renderOnTheRight ? -8 : 8;
            if (i > absorption)
                ClientUtils.resetRenderColor();
        }
        if (displayAbsorption > 0)
            if (!renderOnTheRight)
                gui.leftHeight += 10;
            else
                gui.rightHeight += 10;

        RenderSystem.disableBlend();
        mc.getProfiler().pop();
    }
}
