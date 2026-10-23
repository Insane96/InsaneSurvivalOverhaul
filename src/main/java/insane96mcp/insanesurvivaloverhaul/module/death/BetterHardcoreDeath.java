package insane96mcp.insanesurvivaloverhaul.module.death;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

@LoadFeature(module = ISOModules.DEATH, description = "When you die in hardcore, your spawn point is set to where you died and a lightning strike is summoned")
public class BetterHardcoreDeath extends Feature {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlayerDeath(LivingDeathEvent event) {
        if (!this.isEnabled()
                || event.getEntity().level().isClientSide
                || !(event.getEntity() instanceof ServerPlayer player)
                || event.getEntity() instanceof FakePlayer
                || !event.getEntity().level().getLevelData().isHardcore()
                || player.gameMode.getGameModeForPlayer() == GameType.CREATIVE
                || player.gameMode.getGameModeForPlayer() == GameType.SPECTATOR)
            return;

        player.setRespawnPosition(player.level().dimension(), player.blockPosition(), player.getYRot(), true, false);
        LightningBolt lightningBolt = new LightningBolt(EntityType.LIGHTNING_BOLT, player.level());
        lightningBolt.setVisualOnly(true);
        lightningBolt.setPos(player.position());
        player.level().addFreshEntity(lightningBolt);
        //A forced respawn position requires feet and head blocks to not be liquids, otherwise the player respawns at world spawn
        clearLiquid(player.level(), player.blockPosition());
        clearLiquid(player.level(), player.blockPosition().above());
    }

    /**
     * Removes only liquid blocks, so no other block (doors, rails, containers, etc.) is destroyed
     */
    private static void clearLiquid(Level level, BlockPos pos) {
        if (level.getBlockState(pos).getBlock() instanceof LiquidBlock)
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
    }
}
