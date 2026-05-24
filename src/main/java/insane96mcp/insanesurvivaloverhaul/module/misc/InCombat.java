package insane96mcp.insanesurvivaloverhaul.module.misc;

import insane96mcp.insanelib.core.ModNBTData;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

@LoadFeature(module = ISOModules.MISC, canBeDisabled = false)
public class InCombat extends Feature {
    public static final ResourceLocation LAST_TIMESTAMP = InsaneSO.id("in_combat/last_timestamp");

    public static boolean isInCombat(LivingEntity livingEntity, double seconds) {
        long last = ModNBTData.get(livingEntity, LAST_TIMESTAMP, Long.class);
        return last > 0 && livingEntity.level().getGameTime() - last < seconds * 20;
    }

    @SubscribeEvent
    public void onHurt(LivingDamageEvent.Post event) {
        ModNBTData.put(event.getEntity(), LAST_TIMESTAMP, event.getEntity().level().getGameTime());
        if (event.getSource().getEntity() != null)
            ModNBTData.put(event.getSource().getEntity(), LAST_TIMESTAMP, event.getEntity().level().getGameTime());
    }
}