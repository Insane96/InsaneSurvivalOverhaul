package insane96mcp.insanesurvivaloverhaul.module.mining.miningcharge;

import insane96mcp.insanesurvivaloverhaul.mixin.accessor.PrimedTntAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Primed Mining Charge. Doesn't move and, when the fuse runs out, digs a tunnel in the direction it's facing instead of
 * exploding. Extends {@link PrimedTnt} so it's rendered by the vanilla TNT renderer (using its block state), saves its
 * fuse and block state, and explosions caused by it are attributed to its owner.
 */
public class PrimedMiningCharge extends PrimedTnt {
	public static final int DEFAULT_FUSE = 50;

	public PrimedMiningCharge(EntityType<? extends PrimedMiningCharge> entityType, Level level) {
		super(entityType, level);
	}

	public PrimedMiningCharge(Level level, BlockPos pos, Direction facing, @Nullable LivingEntity owner) {
		this(MiningCharge.PRIMED_MINING_CHARGE.get(), level);
		this.setPos(pos.getX() + 0.5d, pos.getY(), pos.getZ() + 0.5d);
		this.xo = this.getX();
		this.yo = this.getY();
		this.zo = this.getZ();
		this.setFuse(DEFAULT_FUSE);
		this.setBlockState(MiningCharge.BLOCK.block().get().defaultBlockState().setValue(MiningChargeBlock.FACING, facing));
		((PrimedTntAccessor) this).insanesurvivaloverhaul$setOwner(owner);
	}

	/**
	 * Direction of the tunnel. Not named getDirection to not override {@link Entity#getDirection()}.
	 */
	public Direction getFacing() {
		BlockState state = this.getBlockState();
		return state.hasProperty(MiningChargeBlock.FACING) ? state.getValue(MiningChargeBlock.FACING) : Direction.DOWN;
	}

	/**
	 * Unlike TNT, the charge stays attached to where it was placed: no gravity, movement nor portals.
	 */
	@Override
	public void tick() {
		int fuse = this.getFuse() - 1;
		this.setFuse(fuse);
		if (fuse <= 0) {
			this.discard();
			if (!this.level().isClientSide)
				this.explode();
		}
		else if (this.level().isClientSide) {
			this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.5d, this.getZ(), 0d, 0d, 0d);
		}
	}

	/**
	 * Destroys the blocks with low enough explosion resistance in the tunnel in front of the charge and hurts the
	 * entities in it. An {@link Explosion} is still used so protection mods can cancel or filter it through the
	 * explosion events, and blocks are destroyed like in an explosion (drops are merged and TNT gets primed).
	 * With {@link Explosion.BlockInteraction#DESTROY} all the blocks are dropped.
	 * No explosion particles are spawned, only the particles of the destroyed blocks.
	 */
	@Override
	protected void explode() {
		Level level = this.level();
		Direction facing = this.getFacing();
		BlockPos origin = this.blockPosition();
		//The charge only releases its blast into the block it's attached to: if that block is gone (or is air, a fluid,
		//grass, etc.), it just fizzles out
		if (level.getBlockState(origin.relative(facing)).canBeReplaced()) {
			level.levelEvent(LevelEvent.LAVA_FIZZ, origin, 0);
			return;
		}
		int radius = MiningCharge.tunnelRadius;
		int length = MiningCharge.tunnelLength;
		int rx = facing.getAxis() == Direction.Axis.X ? 0 : radius;
		int ry = facing.getAxis() == Direction.Axis.Y ? 0 : radius;
		int rz = facing.getAxis() == Direction.Axis.Z ? 0 : radius;
		BlockPos from = origin.relative(facing).offset(-rx, -ry, -rz);
		BlockPos to = origin.relative(facing, length).offset(rx, ry, rz);
		Vec3 center = Vec3.atCenterOf(origin);

		DamageSource damageSource = Explosion.getDefaultDamageSource(level, this);
		Explosion explosion = new Explosion(level, this, damageSource, null, center.x, center.y, center.z, length / 2f, false, Explosion.BlockInteraction.DESTROY,
				ParticleTypes.EXPLOSION, ParticleTypes.EXPLOSION_EMITTER, MiningCharge.EXPLODE_SOUND);
		if (EventHooks.onExplosionStart(level, explosion))
			return;

		level.gameEvent(this, GameEvent.EXPLODE, center);
		for (BlockPos pos : BlockPos.betweenClosed(from, to)) {
			if (!level.isInWorldBounds(pos))
				continue;
			BlockState state = level.getBlockState(pos);
			if (!state.isAir() && state.getExplosionResistance(level, pos, explosion) < MiningCharge.maxBlockResistance)
				explosion.getToBlow().add(pos.immutable());
		}
		List<Entity> entities = level.getEntities(this, AABB.encapsulatingFullBlocks(from, to));
		EventHooks.onExplosionDetonate(level, explosion, entities, length);

		float damage = MiningCharge.entityDamage.floatValue();
		if (damage > 0f) {
			for (Entity entity : entities) {
				if (!entity.ignoreExplosion(explosion))
					entity.hurt(damageSource, damage);
			}
		}

		if (level instanceof ServerLevel serverLevel) {
			for (BlockPos pos : explosion.getToBlow()) {
				serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, level.getBlockState(pos)), pos.getX() + 0.5d, pos.getY() + 0.5d, pos.getZ() + 0.5d, 12, 0.25d, 0.25d, 0.25d, 0.15d);
			}
		}
		//On the server this only destroys the blocks and spawns the drops: sound and particles are client only
		explosion.finalizeExplosion(false);

		level.playSound(null, center.x, center.y, center.z, MiningCharge.EXPLODE_SOUND.get(), SoundSource.BLOCKS, 4f, (1f + (this.random.nextFloat() - this.random.nextFloat()) * 0.2f) * 1.25f);
	}
}
