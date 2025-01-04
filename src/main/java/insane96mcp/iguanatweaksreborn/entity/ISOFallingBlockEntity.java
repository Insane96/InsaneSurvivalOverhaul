package insane96mcp.iguanatweaksreborn.entity;

import insane96mcp.insanelib.util.LogHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public class ISOFallingBlockEntity extends FallingBlockEntity {
	public Entity source;
	public Direction directionFalling;
	public Direction movedFrom;

	public ISOFallingBlockEntity(Level level, BlockPos pos, BlockState fallingBlockState, Direction directionFalling) {
		this(level, pos.getCenter().x, pos.getCenter().y, pos.getCenter().z, fallingBlockState);
		this.directionFalling = directionFalling;
	}

	public ISOFallingBlockEntity(Level level, double x, double y, double z, BlockState fallingBlockState) {
		super(EntityType.FALLING_BLOCK, level);
		this.blockState = fallingBlockState;
		this.blocksBuilding = true;
		this.setPos(x, y, z);
		this.setDeltaMovement(Vec3.ZERO);
		this.xo = x;
		this.yo = y;
		this.zo = z;
		this.setStartPos(this.blockPosition());
	}

	public void tick() {
		//Fixes duping exploit through dimensions
		if (this.isRemoved())
			return;
		if (this.blockState.isAir()) {
			this.discard();
		} else {
			Block block = this.blockState.getBlock();
			++this.time;
			if (!this.isNoGravity()) {
				this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.04D, 0.0D));
			}

			this.move(MoverType.SELF, this.getDeltaMovement());
			//Fixes duping exploit through dimensions
			if (this.isRemoved())
				return;
			if (!this.level().isClientSide) {
				BlockPos blockpos = this.blockPosition();
				boolean isConcretePowder = this.blockState.getBlock() instanceof ConcretePowderBlock;
				boolean canBeHydrated = isConcretePowder && this.blockState.canBeHydrated(this.level(), blockpos, this.level().getFluidState(blockpos), blockpos);
				double d0 = this.getDeltaMovement().lengthSqr();
				if (isConcretePowder && d0 > 1.0D) {
					BlockHitResult blockhitresult = this.level().clip(new ClipContext(new Vec3(this.xo, this.yo, this.zo), this.position(), ClipContext.Block.COLLIDER, ClipContext.Fluid.SOURCE_ONLY, this));
					if (blockhitresult.getType() != HitResult.Type.MISS && this.blockState.canBeHydrated(this.level(), blockpos, this.level().getFluidState(blockhitresult.getBlockPos()), blockhitresult.getBlockPos())) {
						blockpos = blockhitresult.getBlockPos();
						canBeHydrated = true;
					}
				}

				if (!this.onGround() && !canBeHydrated) {
					if (!this.level().isClientSide && (this.time > 100 && (blockpos.getY() <= this.level().getMinBuildHeight() || blockpos.getY() > this.level().getMaxBuildHeight()) || this.time > 600)) {
						if (this.dropItem && this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
							this.spawnAtLocation(block);
						}

						this.discard();
					}
				}
				else {
					BlockState blockstate = this.level().getBlockState(blockpos);
					this.setDeltaMovement(this.getDeltaMovement().multiply(0.7D, -0.5D, 0.7D));
					if (!blockstate.is(Blocks.MOVING_PISTON)) {
						if (!this.cancelDrop) {
							boolean canBeReplaced = blockstate.canBeReplaced(new DirectionalPlaceContext(this.level(), blockpos, Direction.DOWN, ItemStack.EMPTY, Direction.UP));
							//boolean isHarderThanInside = blockstate.getDestroySpeed(this.level, blockpos) < this.blockState.getDestroySpeed(this.level, blockpos);
							boolean isFree = FallingBlock.isFree(this.level().getBlockState(blockpos.below())) && (!isConcretePowder || !canBeHydrated);
							boolean canSurviveAndIsNotFree = this.blockState.canSurvive(this.level(), blockpos) && !isFree;
							if (canBeReplaced && canSurviveAndIsNotFree)
								this.place(blockstate, block, blockpos, true);
							else
								this.tryStackAboveOrMove(blockpos);
						}
						else {
							this.discard();
							this.callOnBrokenAfterFall(block, blockpos);
						}
					}
				}
			}

			this.setDeltaMovement(this.getDeltaMovement().scale(0.98D));
		}
	}

	public void tryStackAboveOrMove(BlockPos pos) {
		BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos(pos.getX(), pos.getY(), pos.getZ());
		boolean maxStackReached = false;
		while (true) {
			blockPos.set(blockPos.above());
			if (blockPos.getY() - pos.getY() > 6) {
				maxStackReached = true;
				/*if (this.dropItem && this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
					this.discard();
					this.callOnBrokenAfterFall(this.blockState.getBlock(), pos);
					this.spawnAtLocation(this.blockState.getBlock());
				}*/
				break;
			}
			if (this.tryPlace(blockPos))
				break;
		}
		if (maxStackReached) {
			Direction dir;
			if (this.directionFalling != null)
				dir = this.random.nextBoolean() ? this.directionFalling.getClockWise() : this.directionFalling.getCounterClockWise();
			else
				dir = Arrays.stream(Direction.values()).filter((direction) -> direction.getAxis().isHorizontal() && direction != this.movedFrom).skip(this.random.nextInt(4)).findFirst().get();
			//blockPos.set(pos.relative(dir));
			this.directionFalling = dir;
			//TODO prevent moving back from where it came
			this.movedFrom = dir.getOpposite();
			this.setPos(this.position().relative(dir, 1d).relative(Direction.UP, 2));
		}
	}

	public boolean tryPlace(BlockPos blockPos) {
		BlockState stateAt = this.level().getBlockState(blockPos);
		BlockState stateOn = this.level().getBlockState(blockPos.below());
		boolean canBeReplaced = stateAt.canBeReplaced(new DirectionalPlaceContext(this.level(), blockPos, Direction.DOWN, ItemStack.EMPTY, Direction.UP));
		boolean isHarderThanInside = !stateAt.getFluidState().isEmpty() || stateAt.getDestroySpeed(this.level(), blockPos) < this.blockState.getDestroySpeed(this.level(), blockPos);
		boolean canSurvive = this.blockState.canSurvive(this.level(), blockPos) && !stateOn.isAir();
		if (canBeReplaced && isHarderThanInside && canSurvive) {
			this.place(stateOn, this.blockState.getBlock(), blockPos, true);
			return true;
		}
		return false;
	}

	public void place(BlockState state, Block block, BlockPos pos, boolean breakBlock) {
			if (this.blockState.hasProperty(BlockStateProperties.WATERLOGGED) && this.level().getFluidState(pos).getType() == Fluids.WATER) {
				this.blockState = this.blockState.setValue(BlockStateProperties.WATERLOGGED, Boolean.TRUE);
			}

			if (breakBlock)
				this.level().destroyBlock(pos, false);
			if (this.level().setBlockAndUpdate(pos, this.blockState)) {
				Block.updateFromNeighbourShapes(this.blockState, this.level(), pos);
				((ServerLevel)this.level()).getChunkSource().chunkMap.broadcast(this, new ClientboundBlockUpdatePacket(pos, this.level().getBlockState(pos)));
				this.discard();
				if (block instanceof Fallable) {
					((Fallable)block).onLand(this.level(), pos, this.blockState, state, this);
				}

				if (this.blockData != null && this.blockState.hasBlockEntity()) {
					BlockEntity blockentity = this.level().getBlockEntity(pos);
					if (blockentity != null) {
						CompoundTag compoundtag = blockentity.saveWithoutMetadata();

						for(String s : this.blockData.getAllKeys()) {
							compoundtag.put(s, this.blockData.get(s).copy());
						}

						try {
							blockentity.load(compoundtag);
						}
						catch (Exception exception) {
							LogHelper.error("Failed to load block entity from falling block", exception);
						}

						blockentity.setChanged();
					}
				}
			}
			else if (this.dropItem && this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
				this.discard();
				this.callOnBrokenAfterFall(block, pos);
				this.spawnAtLocation(block);
			}
	}

	@Nullable
	@Override
	public ItemEntity spawnAtLocation(ItemLike itemIn) {
		if (!(itemIn instanceof Block))
			return super.spawnAtLocation(itemIn);

		if (this.level().isClientSide)
			return null;

		LootParams.Builder lootParams$Builder = (new LootParams.Builder((ServerLevel) this.level())).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(this.blockPosition())).withParameter(LootContextParams.TOOL, ItemStack.EMPTY).withOptionalParameter(LootContextParams.THIS_ENTITY, this.source);

		List<ItemStack> drops = this.getBlockState().getDrops(lootParams$Builder);

		if (drops.isEmpty())
			return null;
		for (ItemStack stack : drops) {
			ItemEntity itemEntity = new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), stack);
			itemEntity.setDefaultPickUpDelay();
			if (captureDrops() != null)
				captureDrops().add(itemEntity);
			else
				this.level().addFreshEntity(itemEntity);
		}
		return null;
	}

	/*@Override
	public boolean causeFallDamage(float pFallDistance, float pMultiplier, DamageSource pSource) {
		if (!this.hurtEntities)
			return false;
		int i = Mth.ceil(pFallDistance - 1.0F);
		if (i < 0)
			return false;

		Predicate<Entity> predicate = EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(EntitySelector.LIVING_ENTITY_STILL_ALIVE);
		Block block = this.blockState.getBlock();
		DamageSource damagesource1;
		if (block instanceof Fallable fallable)
			damagesource1 = fallable.getFallDamageSource(this);
		else
			damagesource1 = this.damageSources().fallingBlock(this);

		DamageSource damagesource = damagesource1;
		float f = (float)Math.min(Mth.floor((float)i * this.fallDamagePerDistance), this.fallDamageMax);
		this.level().getEntities(this, this.getBoundingBox(), predicate).forEach((p_149649_) -> {
			p_149649_.hurt(damagesource, f);
		});
		boolean flag = this.blockState.is(BlockTags.ANVIL);
		if (flag && f > 0.0F && this.random.nextFloat() < 0.05F + (float)i * 0.05F) {
			BlockState blockstate = AnvilBlock.damage(this.blockState);
			if (blockstate == null) {
				this.cancelDrop = true;
			} else {
				this.blockState = blockstate;
			}
		}

		return false;
	}*/
}
