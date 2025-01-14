package insane96mcp.iguanatweaksreborn.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.TickTask;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.fml.LogicalSide;

public class ClientNetworkHandler {
    public static void handleSyncInvulnerableTimeMessage(int entityId, int invulnerableTime) {
        BlockableEventLoop<? super TickTask> executor = LogicalSidedProvider.WORKQUEUE.get(LogicalSide.CLIENT);
        executor.tell(new TickTask(0, () -> {
            Level level = Minecraft.getInstance().level;
            LivingEntity entity = (LivingEntity) level.getEntity(entityId);
            if (entity == null)
                return;
            entity.invulnerableTime = invulnerableTime;
            entity.hurtTime = invulnerableTime;
        }));
    }

    public static void handleBreakWithNoSound(BlockPos pos, int state) {
        if (Minecraft.getInstance().level != null)
            Minecraft.getInstance().level.addDestroyBlockEffect(pos, Block.stateById(state));
    }

    public static void handleExplosionParticles(double x, double y, double z, float radius, boolean hasBrokenBlocks, Explosion.BlockInteraction blockInteraction) {
        Level level = Minecraft.getInstance().level;
        if (level == null)
            return;
        if (hasBrokenBlocks && radius >= 2 && blockInteraction != Explosion.BlockInteraction.KEEP) {
            int particleCount = (int) (radius * 100);
            for (int i = 0; i < particleCount; i++) {
                double r = radius;
                double v = r / 2f;
                double x1 = x + level.random.nextFloat() * r - v;
                double y1 = y + level.random.nextFloat() * r - v;
                double z1 = z + level.random.nextFloat() * r - v;
                Vec3 dir = new Vec3(x1 - x, y1 - y, z1 - z).normalize().scale(0.4f);
                level.addParticle(ParticleTypes.POOF, x1, y1, z1, dir.x, dir.y, dir.z);
                r = radius * 1.25;
                v = r / 2f;
                x1 = x + level.random.nextFloat() * r - v;
                y1 = y + level.random.nextFloat() * r - v;
                z1 = z + level.random.nextFloat() * r - v;
                dir = new Vec3(x1 - x, y1 - y, z1 - z).normalize().scale(0.7f);
                level.addParticle(ParticleTypes.SMOKE, x1, y1, z1, dir.x, dir.y, dir.z);
            }
        }
        else if (radius < 2) {
            level.addParticle(ParticleTypes.EXPLOSION, x, y, z, 0.0D, 0.0D, 0.0D);
        }
        else {
            level.addParticle(ParticleTypes.EXPLOSION_EMITTER, x, y, z, 0.0D, 0.0D, 0.0D);
        }
    }
}
