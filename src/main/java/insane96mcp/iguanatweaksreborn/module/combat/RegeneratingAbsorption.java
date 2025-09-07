package insane96mcp.iguanatweaksreborn.module.combat;

import com.mojang.blaze3d.systems.RenderSystem;
import insane96mcp.iguanatweaksreborn.InsaneSO;
import insane96mcp.iguanatweaksreborn.integration.NoHungerIntegration;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.network.message.RegenAbsorptionSync;
import insane96mcp.iguanatweaksreborn.setup.ISORegistries;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.util.ClientUtils;
import insane96mcp.insanelib.util.ModNBTData;
import insane96mcp.insanelib.world.effect.ILMobEffect;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.RegistryObject;

@LoadFeature(module = Modules.Ids.COMBAT, description = "Adds a new attribute to add regenerating absorption hearts to the player. Please note that default mods' regenerating absorption is done through item definitions within the combat_rework data pack, disabling this will only disable the regenerating absorption functionality.")
public class RegeneratingAbsorption extends Feature {

    public static final ResourceLocation GUI_ICONS = InsaneSO.location("textures/gui/absorption.png");
    public static ResourceLocation REGEN_ABSORPTION_TAG;
    public static ResourceLocation HURT_COOLDOWN_TAG;
    public static ResourceLocation NO_HURT_SOUND_TAG;

    public static final RegistryObject<Attribute> ATTRIBUTE = ISORegistries.ATTRIBUTES.register("regenerating_absorption", () -> new RangedAttribute("attribute.name.regenerating_absorption", 0d, 0d, 1024d));

    public static final RegistryObject<Attribute> SPEED_ATTRIBUTE = ISORegistries.ATTRIBUTES.register("regenerating_absorption_speed", () -> new RangedAttribute("attribute.name.regenerating_absorption_speed", 0.250d, 0d, 20d));

    public static final RegistryObject<MobEffect> EFFECT = ISORegistries.MOB_EFFECTS.register("regenerating_absorption", () -> new ILMobEffect(MobEffectCategory.BENEFICIAL, 0x818894)
            .addAttributeModifier(ATTRIBUTE.get(), "704d7291-63ba-4346-8aa8-a08e90a13fdf", 4, AttributeModifier.Operation.ADDITION)
            .addAttributeModifier(SPEED_ATTRIBUTE.get(), "704d7291-63ba-4346-8aa8-a08e90a13fdf", 0.125d, AttributeModifier.Operation.ADDITION));

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
            if (!event.has(entityType, ATTRIBUTE.get()))
                event.add(entityType, ATTRIBUTE.get());
            if (!event.has(entityType, SPEED_ATTRIBUTE.get()))
                event.add(entityType, SPEED_ATTRIBUTE.get());
        }
    }

    @SubscribeEvent
    public void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (!this.isEnabled()
                || event.getEntity().level().isClientSide
                || event.getEntity().isDeadOrDying())
            return;

        LivingEntity entity = event.getEntity();
        int hurtCooldown = ModNBTData.get(entity, HURT_COOLDOWN_TAG, Integer.class);
        if (hurtCooldown > 0) {
            hurtCooldown--;
            ModNBTData.put(entity, HURT_COOLDOWN_TAG, hurtCooldown);
            return;
        }
        float maxAbsorption = (float) entity.getAttributeValue(ATTRIBUTE.get());
        float regenSpeed = (float) (entity.getAttributeValue(SPEED_ATTRIBUTE.get()) / 20f);

        float currentAbsorption = ModNBTData.get(entity, REGEN_ABSORPTION_TAG, Float.class);
        if (capToHealth)
            maxAbsorption = Math.min(maxAbsorption, Mth.ceil(entity.getHealth()));
        if (currentAbsorption < 0f || currentAbsorption == maxAbsorption)
            return;

        if (currentAbsorption > maxAbsorption)
            currentAbsorption = Math.max(currentAbsorption - decaySpeed.floatValue(), 0f);
        else
            currentAbsorption = Math.min(currentAbsorption + regenSpeed, maxAbsorption);

        if (entity instanceof ServerPlayer player)
            RegenAbsorptionSync.sync(player, currentAbsorption);
        ModNBTData.put(entity, REGEN_ABSORPTION_TAG, currentAbsorption);
    }

    @SubscribeEvent
    public void onEntityHurt(LivingHurtEvent event) {
        if (!this.isEnabled()
                || event.getEntity().level().isClientSide
                || unDamagedTimeToRegen == 0
                || event.getSource().is(DamageTypeTags.BYPASSES_ARMOR))
            return;

        double absorptionSpeed = event.getEntity().getAttributeValue(SPEED_ATTRIBUTE.get());
        ModNBTData.put(event.getEntity(), HURT_COOLDOWN_TAG, (int) Math.max(unDamagedTimeToRegen * (1f - absorptionSpeed), unDamagedTimeToRegenCap));
    }

    @SubscribeEvent
    public void onLivingHurtPreAbsorption(LivingHurtEvent event) {
        if (!this.isEnabled()
                || !canDamageAbsorption(event.getSource())
                || event.getAmount() <= 0)
            return;

        float currentAbsorption = ModNBTData.get(event.getEntity(), REGEN_ABSORPTION_TAG, Float.class);
        if (currentAbsorption <= 0)
            return;
        //if (currentAbsorption < event.getAmount())
            //event.getEntity().level().playSound(null, event.getEntity(), SoundEvents.GENERIC_EXPLODE, event.getEntity() instanceof Player ? SoundSource.PLAYERS : SoundSource.HOSTILE, 0.5f, 2f);
        //else
            //event.getEntity().getPersistentData().putBoolean(NO_HURT_SOUND_TAG, true);
        float toRemove = Math.min(currentAbsorption, event.getAmount());
        currentAbsorption -= toRemove;
        event.setAmount(event.getAmount() - toRemove);
        ModNBTData.put(event.getEntity(), REGEN_ABSORPTION_TAG, currentAbsorption);
        if (soundOnAbsorptionHurt)
            event.getEntity().level().playSound(null, event.getEntity(), ISORegistries.ABSORPTION_HIT.get(), event.getEntity() instanceof Player ? SoundSource.PLAYERS : SoundSource.HOSTILE, 1f, 2f);
        if (event.getEntity() instanceof ServerPlayer player)
            RegenAbsorptionSync.sync(player, currentAbsorption);
    }

    public static boolean canDamageAbsorption(DamageSource source) {
        if (!absorbingDamageTypeTagOnly)
            return true;
        return source.getEntity() != null && !source.is(DamageTypeTags.BYPASSES_ARMOR);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
        ResourceLocation aboveOverlay = VanillaGuiOverlay.PLAYER_HEALTH.id();
        if (ModList.get().isLoaded("stamina"))
            aboveOverlay = ResourceLocation.parse("stamina:stamina_overlay");
        if (renderOnTheRight) {
            if (ModList.get().isLoaded("nohunger") && NoHungerIntegration.doesRenderArmorAtHunger())
                aboveOverlay = InsaneSO.location("armor");
            else
                aboveOverlay = VanillaGuiOverlay.FOOD_LEVEL.id();
        }
        event.registerAbove(aboveOverlay, "regenerating_absorption", (gui, guiGraphics, partialTicks, screenWidth, screenHeight) -> {
            if (isEnabled(RegeneratingAbsorption.class) && gui.shouldDrawSurvivalElements() && gui.shouldDrawSurvivalElements())
                renderAbsorption(gui, guiGraphics, screenWidth, screenHeight);
        });
    }

    static int lastAbsorption = 0;
    static long lastAbsorptionTime = 0;
    static long absorptionBlinkTime = 0;
    static int displayAbsorption = 0;

    @OnlyIn(Dist.CLIENT)
    protected static void renderAbsorption(ForgeGui gui, GuiGraphics guiGraphics, int width, int height) {
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
                ClientUtils.blitVericallyMirrored(GUI_ICONS, guiGraphics,left, top, u, v, 9, 9, 18, 18);
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
