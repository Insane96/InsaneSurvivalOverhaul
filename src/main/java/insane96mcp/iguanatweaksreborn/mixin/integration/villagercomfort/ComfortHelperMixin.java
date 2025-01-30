package insane96mcp.iguanatweaksreborn.mixin.integration.villagercomfort;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.ghen.villagercomfort.comfort.ComfortHelper;
import dev.ghen.villagercomfort.common.capabilty.IComfortValuesCap;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(ComfortHelper.class)
public class ComfortHelperMixin {
    @ModifyExpressionValue(method = "getVillagerComfort", at = @At(value = "CONSTANT", args = "longValue=24000", ordinal = 0), remap = false)
    private static long iguanatweaksreborn$onCheckDaysWithoutSleep(long original) {
        return -1L;
    }

    @WrapOperation(method = "getVillagerComfort", at = @At(value = "INVOKE", target = "Ljava/util/Optional;orElse(Ljava/lang/Object;)Ljava/lang/Object;", ordinal = 1), remap = false)
    private static Object iguanatweaksreborn$fixDaysWithoutSleepComfort(Optional<Object> instance, Object other, Operation<Object> original, Villager villager, @Local IComfortValuesCap cap) {
        long lastSlept = villager.getBrain().getMemory(MemoryModuleType.LAST_SLEPT).orElse(0L);
        if (!cap.hasBed())
            lastSlept = 0;
        else if (lastSlept == 0)
            lastSlept = villager.level().getGameTime() - villager.tickCount;
        return (villager.level().getGameTime() - lastSlept);
    }
}
