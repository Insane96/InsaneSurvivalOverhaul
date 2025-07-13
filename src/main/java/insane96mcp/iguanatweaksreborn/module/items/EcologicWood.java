package insane96mcp.iguanatweaksreborn.module.items;

import insane96mcp.iguanatweaksreborn.data.generator.ISOItemTagsProvider;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.event.HurtItemStackEvent;
import insane96mcp.insanelib.util.MCUtils;
import insane96mcp.insanelib.util.MathHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.UUID;

@LoadFeature(module = Modules.Ids.ITEMS, description = "Wooden items have bonuses when used in sunlight.")
public class EcologicWood extends Feature {
    public static final TagKey<Item> WOODEN_HAND_EQUIPMENT = ISOItemTagsProvider.create("equipment/hand/wooden");
    public static final TagKey<Item> WOODEN_SHIELD = ISOItemTagsProvider.create("equipment/shield/wooden");
    public static final UUID ATTACK_SPEED_MODIFIER_UUID = UUID.fromString("435317e9-0146-4f1b-bc21-67f466ee5f9c");

    @Config(min = 0, max = 1, description = "Chance for the wooden item to not consume durability when >= 'Max sunlight'.")
    public static Double chanceAtMaxSunlight = 0.5d;
    @Config(description = "Bonus attack speed when >= 'Max sunlight'.")
    public static Double bonusAttackSpeed = 0.2d;
    @Config(description = "Percentage efficiency bonus when >= 'Max sunlight'.")
    public static Double bonusEfficiency = 1d;
    @Config(description = "Lower cooldown by this many ticks when >= 'Max sunlight'.")
    public static Double shieldCooldownReduction = 10d;
    @Config(min = 0, max = 15)
    public static Integer maxSunlight = 12;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void processItemDamaging(HurtItemStackEvent event) {
        if (!this.isEnabled()
                || event.getPlayer() == null
                || (!event.getStack().is(WOODEN_HAND_EQUIPMENT) && !event.getStack().is(WOODEN_SHIELD)))
            return;

        float skyLightRatio = getCalculatedSkyLightRatio(event.getPlayer());
        float ratio = 1f - (chanceAtMaxSunlight.floatValue() * skyLightRatio);
        float amount = event.getAmount() * ratio;
        event.setAmount(MathHelper.getAmountWithDecimalChance(event.getRandom(), amount));
    }

    @SubscribeEvent
    public void addAttackSpeed(LivingEvent.LivingTickEvent event) {
        if (!this.isEnabled()
                || bonusAttackSpeed == 0
                || event.getEntity().tickCount % 2 != 1)
            return;

        Attribute attr = Attributes.ATTACK_SPEED;
        AttributeInstance attributeInstance = event.getEntity().getAttribute(attr);
        if (attributeInstance == null)
            return;

        float calculatedSkyLightRatio = getCalculatedSkyLightRatio(event.getEntity());
        float amount = bonusAttackSpeed.floatValue() * calculatedSkyLightRatio;
        AttributeModifier modifier = attributeInstance.getModifier(ATTACK_SPEED_MODIFIER_UUID);
        if (!event.getEntity().getMainHandItem().is(WOODEN_HAND_EQUIPMENT)
                || amount == 0f) {
            attributeInstance.removeModifier(ATTACK_SPEED_MODIFIER_UUID);
            return;
        }
        else if (modifier != null)
            return;
        MCUtils.applyModifier(event.getEntity(), attr, ATTACK_SPEED_MODIFIER_UUID, "Ecologic wood boost", amount, AttributeModifier.Operation.MULTIPLY_BASE, false);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEfficiency(PlayerEvent.BreakSpeed event) {
        if (!this.isEnabled()
                || bonusEfficiency == 0
                || !event.getEntity().getMainHandItem().is(WOODEN_HAND_EQUIPMENT)
                || !event.getEntity().getMainHandItem().isCorrectToolForDrops(event.getState()))
            return;

        float calculatedSkyLightRatio = getCalculatedSkyLightRatio(event.getEntity());
        float efficiencyMult = bonusEfficiency.floatValue() * calculatedSkyLightRatio;
        if (efficiencyMult == 0f)
            return;
        event.setNewSpeed(event.getOriginalSpeed() * (1f + efficiencyMult));
    }

    /*@SubscribeEvent
    public void onShieldCooldown(ShieldCooldownEvent event) {
        if (!this.isEnabled()
                || shieldCooldownReduction == 0
                || !event.getEntity().getMainHandItem().is(WOODEN_SHIELD))
            return;

        event.setNewCooldown(event.getOriginalCooldown() - shieldCooldownReduction.intValue());
    }*/

    public static float getCalculatedSkyLight(Entity entity) {
        return getCalculatedSkyLight(entity.level(), entity.blockPosition());
    }

    public static float getCalculatedSkyLight(Level level, BlockPos pos) {
        if (!level.isDay()
                || level.isThundering())
            return 0f;
        float skyLight = level.getBrightness(LightLayer.SKY, pos) - level.getSkyDarken();
        if (level.isRaining())
            skyLight /= 3f;
        return skyLight;
    }

    /**
     * Returns a value between 0 and 1 where 0 is total darkness and 1 is 'Max sunlight' light level
     */
    public static float getCalculatedSkyLightRatio(Entity entity) {
        return getCalculatedSkyLightRatio(entity.level(), entity.blockPosition());
    }

    public static float getCalculatedSkyLightRatio(Level level, BlockPos pos) {
        return Math.min(getCalculatedSkyLight(level, pos), maxSunlight) / (float) maxSunlight;
    }
}