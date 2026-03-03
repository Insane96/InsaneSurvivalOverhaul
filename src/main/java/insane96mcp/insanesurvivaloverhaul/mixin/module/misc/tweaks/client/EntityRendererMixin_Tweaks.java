package insane96mcp.insanesurvivaloverhaul.mixin.module.misc.tweaks.client;

import insane96mcp.insanesurvivaloverhaul.module.misc.tweaks.Tweaks;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.entity.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin_Tweaks {
	/**
	 * Switches name tag rendering to {@link Font.DisplayMode#NORMAL} when discrete name tags
	 * are enabled, making tags visible only when the player looks directly at the entity
	 * rather than through walls.
	 */
	@ModifyArg(method = "renderNameTag", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;drawInBatch(Lnet/minecraft/network/chat/Component;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)I", ordinal = 0), index = 7)
    public Font.DisplayMode insanesurvivaloverhaul$modifyNameTagDisplayMode(Font.DisplayMode pDisplayMode) {
        return Tweaks.discreteNameTags ? Font.DisplayMode.NORMAL : pDisplayMode;
    }
}
