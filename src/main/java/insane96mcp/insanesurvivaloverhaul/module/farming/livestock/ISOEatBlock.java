package insane96mcp.insanesurvivaloverhaul.module.farming.livestock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.EatBlockGoal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;
import net.neoforged.neoforge.event.EventHooks;

import java.util.EnumSet;
import java.util.function.Predicate;

public class ISOEatBlock extends EatBlockGoal {
    private static final int EAT_ANIMATION_TICKS = 40;
    private static final Predicate<BlockState> IS_SHORT_GRASS = BlockStatePredicate.forBlock(Blocks.SHORT_GRASS);
    private final Mob mob;
    private final Level level;
    private int eatAnimationTick;

    public ISOEatBlock(Mob mob) {
        super(mob);
        this.mob = mob;
        this.level = mob.level();
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    public boolean canUse() {
        if (this.mob.getRandom().nextInt(this.mob.isBaby() ? 50 : 1000) != 0) {
            return false;
        } else {
            BlockPos blockpos = this.mob.blockPosition();
            if (IS_SHORT_GRASS.test(this.level.getBlockState(blockpos))) {
                return true;
            } else {
                return this.level.getBlockState(blockpos.below()).is(Blocks.GRASS_BLOCK);
            }
        }
    }

    public void start() {
        this.eatAnimationTick = this.adjustedTickDelay(EAT_ANIMATION_TICKS);
        this.level.broadcastEntityEvent(this.mob, (byte)10);
        this.mob.getNavigation().stop();
    }

    public void stop() {
        this.eatAnimationTick = 0;
    }

    public boolean canContinueToUse() {
        return this.eatAnimationTick > 0;
    }

    public int getEatAnimationTick() {
        return this.eatAnimationTick;
    }

    public void tick() {
        this.eatAnimationTick = Math.max(0, this.eatAnimationTick - 1);
        if (this.eatAnimationTick == this.adjustedTickDelay(4)) {
            BlockPos blockpos = this.mob.blockPosition();
            if (IS_SHORT_GRASS.test(this.level.getBlockState(blockpos))) {
                if (EventHooks.canEntityGrief(this.level, this.mob)) {
                    this.level.destroyBlock(blockpos, false);
                }

                if (Livestock.canSheepRegrowWool(this.mob))
                    this.mob.ate();
            } else {
                BlockPos posBelow = blockpos.below();
                if (this.level.getBlockState(posBelow).is(Blocks.GRASS_BLOCK)) {
                    if (EventHooks.canEntityGrief(this.level, this.mob)) {
                        this.level.levelEvent(2001, posBelow, Block.getId(Blocks.GRASS_BLOCK.defaultBlockState()));
                        this.level.setBlock(posBelow, Blocks.DIRT.defaultBlockState(), 2);
                    }

                    if (Livestock.canSheepRegrowWool(this.mob))
                        this.mob.ate();
                }
            }

        }
    }
}
