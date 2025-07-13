package insane96mcp.iguanatweaksreborn.mixin.integration.simpleclouds;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.nonamecrackers2.simpleclouds.common.event.SimpleCloudsEvents;
import insane96mcp.iguanatweaksreborn.module.sleeprespawn.tiredness.Tiredness;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SimpleCloudsEvents.class)
public class SimpleCloudsEventsMixin {
	@Definition(id = "ServerLevel", type = ServerLevel.class)
	@Expression("? instanceof ServerLevel")
	@WrapOperation(method = "removeStormsAfterSleeping", at = @At("MIXINEXTRAS:EXPRESSION"), remap = false)
	private static boolean iguanatweaksreborn$onRemoveStormAfterSleep(Object object, Operation<Boolean> original) {
		if (!ModList.get().isLoaded("simpleclouds")
				|| !Tiredness.simpleCloudsIntegration)
			return original.call(object);
		return false;
	}
}
