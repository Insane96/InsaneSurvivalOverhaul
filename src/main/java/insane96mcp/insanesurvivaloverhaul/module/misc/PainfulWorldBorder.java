package insane96mcp.insanesurvivaloverhaul.module.misc;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.border.WorldBorder;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

@LoadFeature(module = ISOModules.MISC, description = "The insanesurvivaloverhaul:painful_world_border game rule grows the world border every time a player takes damage.")
public class PainfulWorldBorder extends Feature {

    public static final GameRules.Key<GameRules.IntegerValue> RULE_PAINFUL_WORLD_BORDER = GameRules.register("insanesurvivaloverhaul:painful_world_border", GameRules.Category.MISC, GameRules.IntegerValue.create(0));

    @SubscribeEvent
    public void onDamageEvent(LivingDamageEvent.Pre event) {
        if (!this.isEnabled()
                || !(event.getEntity() instanceof ServerPlayer player))
            return;
        int painfulWorldBorder = player.level().getGameRules().getInt(RULE_PAINFUL_WORLD_BORDER);
        if (painfulWorldBorder == 0)
            return;

        //noinspection DataFlowIssue
        WorldBorder worldBorder = player.getServer().overworld().getWorldBorder();
        double currentSize = worldBorder.getLerpTarget();
        double newSize = currentSize + painfulWorldBorder * Math.min(event.getNewDamage(), player.getHealth());
        worldBorder.lerpSizeBetween(currentSize, newSize, 2000L);
    }
}
