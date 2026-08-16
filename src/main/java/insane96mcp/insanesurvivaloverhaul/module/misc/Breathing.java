package insane96mcp.insanesurvivaloverhaul.module.misc;

import insane96mcp.insanelib.core.ModNBTData;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.core.feature.config.MinMaxConfig;
import insane96mcp.insanelib.util.MathHelper;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingBreatheEvent;
import net.neoforged.neoforge.event.entity.living.LivingDrownEvent;

@LoadFeature(module = ISOModules.MISC, description = "Changes how fast entities consume and refill air, and how drowning damage scales.")
public class Breathing extends Feature {

    public static ResourceLocation WAS_BREATHING;
    public static ResourceLocation TICK_SINCE_OUT_OF_WATER;
    public static ResourceLocation TIMES_DROWNED;

    @Config(min = 0, max = 100, description = "The amount of ticks the entities consumes when underwater. In vanilla it's 1 without Respiration enchantment. For non integer numbers the decimal part will count as a chance to have a +1")
    public static Double airTicksConsumed = 1.5d;

    @Config(min = 0, max = 100, description = "Every how many ticks will entities drown")
    public static Integer drownSpeed = 30;
    @Config(description = "The amount of air ticks the entities regains each tick when out of water. Min is the amount as soon as you exit water, Max is a few seconds out of water. For non integer numbers the decimal part will count as a chance to have a +1. Vanilla is 4.")
    public static MinMaxConfig airTicksRefilled = new MinMaxConfig(1, 2.5);
    @Config
    public static Boolean increaseDrownDamageTheMoreDrowning = true;

    @Override
    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        WAS_BREATHING = this.createDataKey("was_breathing");
        TICK_SINCE_OUT_OF_WATER = this.createDataKey("tick_since_out_of_water");
        TIMES_DROWNED = this.createDataKey("times_drowned");
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
        double airConsumed = airTicksConsumed / (1d + event.getEntity().getAttributeValue(Attributes.OXYGEN_BONUS));
        //If drowning, drown at vanilla speed
        if (event.getEntity().getAirSupply() <= 0)
            airConsumed = 1;
        event.setConsumeAirAmount(MathHelper.getAmountWithDecimalChance(event.getEntity().getRandom(), airConsumed));

        int refillAmount = MathHelper.getAmountWithDecimalChance(event.getEntity().getRandom(), airTicksRefilled.min);
        if (ticksSinceOutOfWater > 75) {
            refillAmount = MathHelper.getAmountWithDecimalChance(event.getEntity().getRandom(), airTicksRefilled.max);
            if (event.canBreathe())
                setTimesDrowned(event.getEntity(), 0);
        }
        event.setRefillAirAmount(refillAmount);
        ModNBTData.put(event.getEntity(), WAS_BREATHING, event.canBreathe());
    }

    @SubscribeEvent
    public void onDrown(LivingDrownEvent event) {
        if (!this.isEnabled()
                || !increaseDrownDamageTheMoreDrowning
                || !event.getEntity().canDrownInFluidType(event.getEntity().getEyeInFluidType()))
            return;

        event.setDrowning(event.getEntity().getAirSupply() <= -drownSpeed);
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
}
