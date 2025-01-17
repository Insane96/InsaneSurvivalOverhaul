package insane96mcp.iguanatweaksreborn.mixin.client;

import insane96mcp.iguanatweaksreborn.module.misc.tweaks.Tweaks;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.entity.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {
	@ModifyArg(method = "renderNameTag", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;drawInBatch(Lnet/minecraft/network/chat/Component;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)I", ordinal = 0), index = 7)
    public Font.DisplayMode injected(Font.DisplayMode pDisplayMode) {
        return Tweaks.discreteNameTags ? Font.DisplayMode.NORMAL : pDisplayMode;
    }
}
