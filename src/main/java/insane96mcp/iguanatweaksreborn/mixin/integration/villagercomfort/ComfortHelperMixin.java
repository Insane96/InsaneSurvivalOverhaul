package insane96mcp.iguanatweaksreborn.mixin.integration.villagercomfort;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.ghen.villagercomfort.comfort.ComfortHelper;
import dev.ghen.villagercomfort.core.config.CommonConfig;
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
    private static Object iguanatweaksreborn$fixDaysWithoutSleepComfort(Optional<Object> instance, Object other, Operation<Object> original, Villager villager) {
        long lastSlept = villager.getBrain().getMemory(MemoryModuleType.LAST_SLEPT).orElse(0L);
        if (lastSlept == 0)
            lastSlept = villager.level().getGameTime() - villager.tickCount;
        return (villager.level().getGameTime() - lastSlept) / 24000L * CommonConfig.COMFORT_PER_DAY_WITHOUT_SLEEP.get().intValue();
    }
}
