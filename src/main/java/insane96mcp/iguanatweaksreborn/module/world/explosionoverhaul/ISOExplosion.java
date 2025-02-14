package insane96mcp.iguanatweaksreborn.module.world.explosionoverhaul;

import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import insane96mcp.iguanatweaksreborn.InsaneSurvivalOverhaul;
import insane96mcp.iguanatweaksreborn.entity.ISOFallingBlockEntity;
import insane96mcp.iguanatweaksreborn.event.ISOEventFactory;
import insane96mcp.iguanatweaksreborn.module.experience.enchantments.EnchantmentsFeature;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.MovingPistonBlock;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.PartEntity;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ISOExplosion extends Explosion {
	public static final String KNOCKBACK_MULTIPLIER_TAG = InsaneSurvivalOverhaul.RESOURCE_PREFIX + "explosion_knockback_multiplier";
	public static final String BASE_RESISTANCE_ADD_TAG = InsaneSurvivalOverhaul.RESOURCE_PREFIX + "explosion_base_resistance_add";
	public static final String RAY_STRENGTH_MULTIPLIER_TAG = InsaneSurvivalOverhaul.RESOURCE_PREFIX + "explosion_ray_strength_multiplier";
	public static final String DAMAGE_MULTIPLIER_TAG = InsaneSurvivalOverhaul.RESOURCE_PREFIX + "explosion_damage_multiplier";
	ObjectArrayList<Pair<ItemStack, BlockPos>> droppedItems = new ObjectArrayList<>();
	boolean creeperCollateral;
	public final boolean poofParticles;
	private float baseResistanceAdd = 0.3f;
	private float rayStrengthMultiplier = 0.3f;

	private List<Entity> affectedEntities = new ArrayList<>();

	public ISOExplosion(Level level, @Nullable Entity source, @Nullable DamageSource damageSource, @Nullable ExplosionDamageCalculator damageCalculator, double x, double y, double z, float radius, boolean fire, BlockInteraction blockInteraction, boolean creeperCollateral) {
		this(level, source, damageSource, damageCalculator, x, y, z, radius, fire, blockInteraction, creeperCollateral, true);
	}

	public ISOExplosion(Level level, @Nullable Entity source, @Nullable DamageSource damageSource, @Nullable ExplosionDamageCalculator damageCalculator, double x, double y, double z, float radius, boolean fire, BlockInteraction blockInteraction, boolean creeperCollateral, boolean poofParticles) {
		super(level, source, damageSource, damageCalculator, x, y, z, radius, fire, blockInteraction);
		this.creeperCollateral = creeperCollateral;
		this.poofParticles = poofParticles;
		if (source != null) {
			if (source.getPersistentData().contains(BASE_RESISTANCE_ADD_TAG))
				this.baseResistanceAdd = source.getPersistentData().getFloat(BASE_RESISTANCE_ADD_TAG);
			if (source.getPersistentData().contains(RAY_STRENGTH_MULTIPLIER_TAG))
				this.rayStrengthMultiplier = Math.max(0.01f,source.getPersistentData().getFloat(RAY_STRENGTH_MULTIPLIER_TAG));
		}
		if (ExplosionOverhaul.limitExplosionSize != 0)
			this.radius = Math.min(ExplosionOverhaul.limitExplosionSize, this.radius);
	}

	public ISOExplosion setBaseResistanceAdd(float baseResistanceAdd) {
		this.baseResistanceAdd = baseResistanceAdd;
		return this;
	}

	public ISOExplosion rayStrengthMultiplier(float rayStrengthMultiplier) {
		this.rayStrengthMultiplier = rayStrengthMultiplier;
		return this;
	}

	public void gatherAffectedBlocks(boolean randomize) {
		Set<BlockPos> set = Sets.newHashSet();
		for(int j = 0; j < 16; ++j) {
			for(int k = 0; k < 16; ++k) {
				for(int l = 0; l < 16; ++l) {
					if (j == 0 || j == 15 || k == 0 || k == 15 || l == 0 || l == 15) {
						double d0 = (float)j / 15.0F * 2.0F - 1.0F;
						double d1 = (float)k / 15.0F * 2.0F - 1.0F;
						double d2 = (float)l / 15.0F * 2.0F - 1.0F;
						double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
						d0 = d0 / d3;
						d1 = d1 / d3;
						d2 = d2 / d3;
						float rayStrength;
						if (!randomize)
							rayStrength = this.radius;
						else
							rayStrength = this.radius * (0.7F + this.level.getRandom().nextFloat() * 0.6F);
						double x = this.getPosition().x();
						double y = this.getPosition().y();
						double z = this.getPosition().z();
						for (;rayStrength > 0.0F; rayStrength -= 0.22500001F) {
							BlockPos blockpos = BlockPos.containing(x, y, z);
							BlockState blockstate = this.level.getBlockState(blockpos);
							FluidState fluidstate = this.level.getFluidState(blockpos);
							Optional<Float> optional = this.damageCalculator.getBlockExplosionResistance(this, this.level, blockpos, blockstate, fluidstate);
							if (optional.isPresent()) {
								float resistance = optional.get();
								rayStrength -= (resistance + baseResistanceAdd) * rayStrengthMultiplier;
							}
							if (rayStrength > 0.0F && this.damageCalculator.shouldBlockExplode(this, this.level, blockpos, blockstate, rayStrength))
								set.add(blockpos);
							x += d0 * (double) 0.3F;
							y += d1 * (double) 0.3F;
							z += d2 * (double) 0.3F;
						}
					}
				}
			}
		}
		this.getToBlow().addAll(set);
		float affectedEntitiesRadius = this.radius * 2.0F;
		gatherAffectedEntities(affectedEntitiesRadius);
		net.minecraftforge.event.ForgeEventFactory.onExplosionDetonate(this.level, this, this.affectedEntities, affectedEntitiesRadius);

	}

	public void fallingBlocks() {
		if (!this.interactsWithBlocks())
			return;
		for (BlockPos blockpos : this.getToBlow()) {
			BlockState blockstate = this.level.getBlockState(blockpos);
			Block block = blockstate.getBlock();
			if (blockstate.isAir())
				continue;
			if (block instanceof TntBlock) {
				block.wasExploded(this.level, blockpos, this);
				this.level.setBlockAndUpdate(blockpos, Blocks.AIR.defaultBlockState());
				continue;
			}
			if (block instanceof MovingPistonBlock) {
				PistonMovingBlockEntity tileEntity = (PistonMovingBlockEntity) this.level.getBlockEntity(blockpos);
				blockstate = tileEntity.getMovedState();
				block = blockstate.getBlock();
			}
			block.wasExploded(this.level, BlockPos.containing(this.getPosition()), this);
			BlockPos blockpos1 = blockpos.immutable();
			this.level.setBlockAndUpdate(blockpos1, Blocks.AIR.defaultBlockState());
			ISOFallingBlockEntity fallingBlockEntity = new ISOFallingBlockEntity(this.level, blockpos1.getX() + 0.5f, blockpos1.getY() + 2.0625f, blockpos1.getZ() + 0.5f, blockstate);
			fallingBlockEntity.time = 1;
			fallingBlockEntity.source = this.source;
			this.level.addFreshEntity(fallingBlockEntity);
		}
		this.clearToBlow();
	}

	public void processEntities(boolean knockbackScaleWithSize) {
		float affectedEntitiesRadius = this.radius * 2.0F;
		for (Entity entity : this.affectedEntities) {
			if (entity.tickCount == 0 && !(entity instanceof PartEntity<?>)  && !(entity instanceof ISOFallingBlockEntity)  && !ExplosionOverhaul.affectJustSpawnedEntities)
				continue;
			if (entity.ignoreExplosion())
				continue;
			double distanceRatio = Mth.sqrt((float) entity.distanceToSqr(this.getPosition())) / affectedEntitiesRadius;
			if (distanceRatio > 1.0D)
				continue;
			double xDistance = entity.getX() - this.getPosition().x;
			double yDistance = (entity.getEyeY() - this.getPosition().y);// * 0.6667d;
			double zDistance = entity.getZ() - this.getPosition().z;
			double d13 = Mth.sqrt((float) (xDistance * xDistance + yDistance * yDistance + zDistance * zDistance));
			if (d13 == 0.00)
				continue;
			//xDistance = xDistance / d13;
			if (!(entity instanceof ISOFallingBlockEntity))
				yDistance = yDistance / d13;
			//zDistance = zDistance / d13;
			double blockDensity = getSeenPercent(this.getPosition(), entity);
			double d10 = (1.0D - distanceRatio) * blockDensity;
			//Damage Entities in the explosion radius
			float damageAmount = (float) ((int) ((d10 * d10 + d10) / 2.0D * ExplosionOverhaul.explosionDamageCalculationMultiplier * (double) affectedEntitiesRadius + 1.0D));
			damageAmount *= getDamageMultiplier(this.source);
			if (blockDensity > 0d) {
				DamageSource source = this.getDamageSource();
				boolean isBlocking = false;
				if (entity instanceof LivingEntity living)
					isBlocking = living.isDamageSourceBlocked(source) && living.isBlocking();
				boolean isLiving = entity instanceof LivingEntity;
				if (entity.hurt(source, damageAmount) || isBlocking || !isLiving) {
					double d11 = d10;
					if (isLiving) {
						d11 = getKnockbackReduction((LivingEntity) entity, d11);
					}
					if (knockbackScaleWithSize)
						d11 *= this.radius;
					d11 = Math.max(d11, this.radius * 0.05d);
 					if (entity instanceof ISOFallingBlockEntity || ExplosionOverhaul.shouldTakeReducedKnockback(entity))
						d11 *= 0.2d;
					d11 *= getKnockbackMultiplier(this.source);
					d11 = Math.min(d11, 10f);
					if (entity instanceof ISOFallingBlockEntity) {
						d11 = Math.min(d11, 1f);
						xDistance += this.level.getRandom().nextFloat() - 0.5f;
						zDistance += this.level.getRandom().nextFloat() - 0.5f;
					}
					double y = yDistance * d11;
					if (isLiving)
						y = Math.max(y, 0.1f * this.radius);
					entity.setDeltaMovement(entity.getDeltaMovement().add(xDistance * d11, y, zDistance * d11));
					if (entity instanceof Player player) {
						if (!player.isSpectator() && (!player.isCreative() || !player.getAbilities().flying)) {
							this.getHitPlayers().put(player, new Vec3(xDistance * d11, Math.max(yDistance * d11, 0.1f * this.radius), zDistance * d11));
						}
					}
				}
			}
		}
	}

	public void destroyBlocks() {
		this.level.gameEvent(this.source, GameEvent.EXPLODE, new Vec3(this.getPosition().x, this.getPosition().y, this.getPosition().z));
		if (!this.interactsWithBlocks())
			return;
		Util.shuffle(this.toBlow, this.level.getRandom());
		for(BlockPos blockpos : this.getToBlow()) {
			BlockState blockstate = this.level.getBlockState(blockpos);
			FluidState fluidState = this.level.getFluidState(blockpos);
			if (!blockstate.isAir()) {
				BlockPos immutableBlockPos = blockpos.immutable();
				this.level.getProfiler().push("explosion_blocks");
				if (!doesCreeperCollateralApply() && blockstate.canDropFromExplosion(this.level, blockpos, this) && this.level instanceof ServerLevel) {
					BlockEntity blockEntity = blockstate.hasBlockEntity() ? this.level.getBlockEntity(blockpos) : null;
					LootParams.Builder lootparams$builder = (new LootParams.Builder((ServerLevel) this.level)).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(blockpos)).withParameter(LootContextParams.TOOL, ItemStack.EMPTY).withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockEntity).withOptionalParameter(LootContextParams.THIS_ENTITY, this.source);
					if (this.blockInteraction == BlockInteraction.DESTROY_WITH_DECAY) {
						lootparams$builder.withParameter(LootContextParams.EXPLOSION_RADIUS, this.radius);
					}
					blockstate.getDrops(lootparams$builder).forEach((stack) -> addBlockDrops(droppedItems, stack, immutableBlockPos));
				}
				blockstate.onBlockExploded(this.level, blockpos, this);
				if (!fluidState.isEmpty()) {
					this.level.setBlock(blockpos, fluidState.createLegacyBlock(), 3);
				}
				this.level.getProfiler().pop();
			}
		}
	}

	public static void addBlockDrops(ObjectArrayList<Pair<ItemStack, BlockPos>> pDropPositionArray, ItemStack stack, BlockPos pos) {
		int pairCount = pDropPositionArray.size();

		if (!ExplosionOverhaul.dontStackDrops) {
			for (int i = 0; i < pairCount; ++i) {
				Pair<ItemStack, BlockPos> pair = pDropPositionArray.get(i);
				ItemStack pairStack = pair.getFirst();
				if (ItemEntity.areMergable(pairStack, stack)) {
					ItemStack mergedStack = ItemEntity.merge(pairStack, stack, 16);
					pDropPositionArray.set(i, Pair.of(mergedStack, pair.getSecond()));
					if (stack.isEmpty())
						return;
				}
			}
		}

		pDropPositionArray.add(Pair.of(stack, pos));
	}

	public boolean doesCreeperCollateralApply() {
		return this.creeperCollateral && this.getExploder() instanceof Creeper;
	}

	public void dropItems() {
		for(Pair<ItemStack, BlockPos> pair : droppedItems) {
			Block.popResource(this.level, pair.getSecond(), pair.getFirst());
		}
	}

	public void processFire() {
		if (this.fire) {
			for(BlockPos blockPos : this.getToBlow()) {
				if (this.random.nextInt(3) == 0 && this.level.getBlockState(blockPos).isAir() && this.level.getBlockState(blockPos.below()).isSolidRender(this.level, blockPos.below())) {
					this.level.setBlockAndUpdate(blockPos, BaseFireBlock.getState(this.level, blockPos));
				}
			}
		}
	}

	private void gatherAffectedEntities(float affectedRadius) {
		int x1 = Mth.floor(this.getPosition().x() - (double)affectedRadius - 1.0D);
		int x2 = Mth.floor(this.getPosition().x() + (double)affectedRadius + 1.0D);
		int y1 = Mth.floor(this.getPosition().y() - (double)affectedRadius - 1.0D);
		int y2 = Mth.floor(this.getPosition().y() + (double)affectedRadius + 1.0D);
		int z1 = Mth.floor(this.getPosition().z() - (double)affectedRadius - 1.0D);
		int z2 = Mth.floor(this.getPosition().z() + (double)affectedRadius + 1.0D);
		this.affectedEntities = this.level.getEntities(this.source, new AABB(x1, y1, z1, x2, y2, z2));
	}

	/*
	   Since Mojang decided to round down the damage and used the same method for both Knockback and Damage, the latter doesn't work as it's always rounded down to 0
	 */
	public static double getKnockbackReduction(LivingEntity livingEntity, double knockback) {
		double knockbackReduction = 0d;
		int blastProtLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.BLAST_PROTECTION, livingEntity) + EnchantmentHelper.getEnchantmentLevel(EnchantmentsFeature.BLAST_PROTECTION.get(), livingEntity);
		if (blastProtLevel > 0)
			knockbackReduction += blastProtLevel * 0.15d;

		if (livingEntity.getAttribute(Attributes.KNOCKBACK_RESISTANCE) != null)
			//noinspection ConstantConditions
			knockbackReduction += livingEntity.getAttribute(Attributes.KNOCKBACK_RESISTANCE).getValue();

		return knockback - knockback * Mth.clamp(knockbackReduction, 0d, 1d);
	}

	@Nullable
	public static ISOExplosion explode(Level level, @Nullable Entity source, @Nullable DamageSource damageSource, @Nullable ExplosionDamageCalculator damageCalculator, double x, double y, double z, float radius, boolean fire, Level.ExplosionInteraction explosionInteraction, boolean poofParticles) {
		if (!(level instanceof ServerLevel serverLevel))
			return null;
		BlockInteraction blockInteraction = switch (explosionInteraction) {
			case NONE -> BlockInteraction.KEEP;
			case BLOCK ->
					level.getGameRules().getBoolean(GameRules.RULE_BLOCK_EXPLOSION_DROP_DECAY) ? BlockInteraction.DESTROY_WITH_DECAY : BlockInteraction.DESTROY;
			case MOB ->
					net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(level, source) ? level.getGameRules().getBoolean(GameRules.RULE_MOB_EXPLOSION_DROP_DECAY) ? BlockInteraction.DESTROY_WITH_DECAY : BlockInteraction.DESTROY : BlockInteraction.KEEP;
			case TNT ->
					level.getGameRules().getBoolean(GameRules.RULE_TNT_EXPLOSION_DROP_DECAY) ? BlockInteraction.DESTROY_WITH_DECAY : BlockInteraction.DESTROY;
		};
		return explode(serverLevel, source, damageSource, damageCalculator, x, y, z, radius, fire, blockInteraction, poofParticles);
	}

	public static ISOExplosion explode(ServerLevel level, @Nullable Entity source, @Nullable DamageSource damageSource, @Nullable ExplosionDamageCalculator damageCalculator, double x, double y, double z, float radius, boolean fire, BlockInteraction blockInteraction, boolean poofParticles) {
		ISOExplosion explosion = new ISOExplosion(level, source, damageSource, damageCalculator, x, y, z, radius, fire, blockInteraction, ExplosionOverhaul.creeperCollateral, poofParticles);
		if (ISOEventFactory.onITRExplosionCreated(explosion)) return explosion;
		if (level.getGameRules().getBoolean(ExplosionOverhaul.RULE_MOBGRIEFING))
			explosion.gatherAffectedBlocks(!ExplosionOverhaul.disableExplosionRandomness);
		if (ExplosionOverhaul.enableFlyingBlocks)
			explosion.fallingBlocks();
		explosion.destroyBlocks();
		explosion.processEntities(ExplosionOverhaul.knockbackScalesWithSize);
		explosion.dropItems();
		explosion.processFire();
		if (explosion.blockInteraction == BlockInteraction.KEEP) {
			explosion.clearToBlow();
		}
		for (ServerPlayer serverPlayer : level.players()) {
			if (serverPlayer.distanceToSqr(explosion.getPosition().x, explosion.getPosition().y, explosion.getPosition().z) < 4096.0D) {
				serverPlayer.connection.send(new ClientboundExplodePacket(explosion.getPosition().x, explosion.getPosition().y, explosion.getPosition().z, explosion.radius, explosion.getToBlow(), explosion.getHitPlayers().get(serverPlayer)));
			}
		}
		return explosion;
	}

	public static float getKnockbackMultiplier(@Nullable Entity entity) {
		return entity != null && entity.getPersistentData().contains(KNOCKBACK_MULTIPLIER_TAG) ? entity.getPersistentData().getFloat(KNOCKBACK_MULTIPLIER_TAG) : 1f;
	}

	public static float getDamageMultiplier(@Nullable Entity entity) {
		return entity != null && entity.getPersistentData().contains(DAMAGE_MULTIPLIER_TAG) ? entity.getPersistentData().getFloat(DAMAGE_MULTIPLIER_TAG) : 1f;
	}

	/// Vanilla is 0.3
	public static void setBaseResistanceAdd(Entity entity, float baseResistanceAdd) {
		entity.getPersistentData().putFloat(BASE_RESISTANCE_ADD_TAG, baseResistanceAdd);
	}

	/// Vanilla is 0.3
	public static void setRayStrengthMultiplier(Entity entity, float rayStrengthMultiplier) {
		entity.getPersistentData().putFloat(RAY_STRENGTH_MULTIPLIER_TAG, rayStrengthMultiplier);
	}

	public static void setKnockbackMultiplier(Entity entity, float knockbackMultiplier) {
		entity.getPersistentData().putFloat(KNOCKBACK_MULTIPLIER_TAG, knockbackMultiplier);
	}

	public static void setDamageMultiplierTag(Entity entity, float damageMultiplier) {
		entity.getPersistentData().putFloat(DAMAGE_MULTIPLIER_TAG, damageMultiplier);
	}
}
