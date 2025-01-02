package insane96mcp.iguanatweaksreborn.module.items;

import insane96mcp.iguanatweaksreborn.data.generator.ISTItemTagsProvider;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.event.HurtItemStackEvent;
import insane96mcp.insanelib.util.MathHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Ecologic Wood", description = "Wooden items have a lower chance to break in sunlight.")
@LoadFeature(module = Modules.Ids.ITEMS)
public class EcologicWood extends Feature {
    public static final TagKey<Item> WOODEN_HAND_EQUIPMENT = ISTItemTagsProvider.create("equipment/hand/wooden");

    @Config(min = 0, max = 1)
    @Label(name = "Chance at 'Max sunlight'", description = "Chance for the wooden item to not consume durability at 'Max sunlight'.")
    public static Double chanceAtMaxSunlight = 0.75d;
    @Config(min = 0, max = 15)
    @Label(name = "Max sunlight")
    public static Integer maxSunlight = 12;

    public EcologicWood(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void processItemDamaging(HurtItemStackEvent event) {
        if (!this.isEnabled()
                || event.getPlayer() == null
                || !event.getStack().is(WOODEN_HAND_EQUIPMENT))
            return;

        float skyLightRatio = getCalculatedSkyLightRatio(event.getPlayer());
        float ratio = 1f - (chanceAtMaxSunlight.floatValue() * skyLightRatio);
        float amount = event.getAmount() * ratio;
        event.setAmount(MathHelper.getAmountWithDecimalChance(event.getRandom(), amount));
    }

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