package insane96mcp.insanesurvivaloverhaul.mixin.module.client.misc;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import insane96mcp.insanesurvivaloverhaul.mixin.accessor.BlockStateBaseAccessor;
import insane96mcp.insanesurvivaloverhaul.module.client.Misc;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import javax.annotation.Nullable;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin_Misc {

    @Shadow
    @Nullable
    private ClientLevel level;

    /**
     * Tints the block-selection outline red when the player does not have the correct tool
     * to mine the targeted block, giving a visual indicator of tool requirements.
     */
    @ModifyExpressionValue(method = "renderHitOutline", at = @At(value = "CONSTANT", args = "floatValue=0.0", ordinal = 0))
    private float insanesurvivaloverhaul$redOutlineForWrongTool(float value, PoseStack pPoseStack, VertexConsumer pConsumer, Entity entity, double pCamX, double pCamY, double pCamZ, BlockPos pPos, BlockState state) {
        if (!(entity instanceof Player player)
                || player.getAbilities().instabuild
                || (player.hasCorrectToolForDrops(state, this.level, pPos) && ((BlockStateBaseAccessor) state).getDestroySpeed() >= 0f))
            return value;
        return Misc.getRedOutlineAmount(value);
    }
}
