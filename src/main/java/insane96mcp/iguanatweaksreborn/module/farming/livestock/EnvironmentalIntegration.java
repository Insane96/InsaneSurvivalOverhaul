package insane96mcp.iguanatweaksreborn.module.farming.livestock;

import com.teamabnormals.environmental.common.entity.animal.Duck;
import com.teamabnormals.environmental.core.other.tags.EnvironmentalItemTags;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public class EnvironmentalIntegration {
    public static void onTryToFeedDucks(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof Duck duck)
                || !event.getItemStack().is(EnvironmentalItemTags.DUCK_FOOD))
            return;

        event.setCanceled(true);
        if (duck.getPersistentData().getInt(Livestock.FED_TIME) <= 0) {
            duck.getPersistentData().putInt(Livestock.FED_TIME, Livestock.fasterEggTime * 20);
            event.getEntity().swing(event.getHand());
            duck.level().addParticle(ParticleTypes.HAPPY_VILLAGER, duck.getX(), duck.getY() + duck.getBbHeight() + 0.5D, duck.getZ(), 0.0D, 0.0D, 0.0D);
            if (!event.getEntity().getAbilities().instabuild)
                event.getEntity().getItemInHand(event.getHand()).shrink(1);
        }
    }
}
