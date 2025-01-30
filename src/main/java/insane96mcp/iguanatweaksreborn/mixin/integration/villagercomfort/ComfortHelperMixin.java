package insane96mcp.iguanatweaksreborn.mixin.integration.villagercomfort;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.ghen.villagercomfort.comfort.ComfortHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ComfortHelper.class)
public class ComfortHelperMixin {
    @ModifyExpressionValue(method = "getVillagerComfort", at = @At(value = "CONSTANT", args = "longValue=24000", ordinal = 0), remap = false)
    private static long iguanatweaksreborn$onCheckDaysWithoutSleep(long original) {
        return -1L;
    }

    /*@Definition(id = "villager", local = @Local(type = Villager.class, argsOnly = true))
    @Definition(id = "getBrain", method = "Lnet/minecraft/world/entity/npc/Villager;getBrain()Lnet/minecraft/world/entity/ai/Brain;")
    @Definition(id = "getMemory", method = "Lnet/minecraft/world/entity/ai/Brain;getMemory(Lnet/minecraft/world/entity/ai/memory/MemoryModuleType;)Ljava/util/Optional;")
    @Definition(id = "LAST_SLEPT", field = "Lnet/minecraft/world/entity/ai/memory/MemoryModuleType;LAST_SLEPT:Lnet/minecraft/world/entity/ai/memory/MemoryModuleType;")
    @Definition(id = "orElse", method = "Ljava/util/Optional;orElse(Ljava/lang/Object;)Ljava/lang/Object;")
    @Definition(id = "COMFORT_PER_DAY_WITHOUT_SLEEP", field = "Ldev/ghen/villagercomfort/core/config/CommonConfig;COMFORT_PER_DAY_WITHOUT_SLEEP:Lnet/minecraftforge/common/ForgeConfigSpec$ConfigValue;")
    @Definition(id = "get", method = "Lnet/minecraftforge/common/ForgeConfigSpec$ConfigValue;get()Ljava/lang/Object;")
    @Definition(id = "intValue", method = "Ljava/lang/Number;intValue()I")
    @Definition(id = "Number", type = Number.class)
    @Expression("(villager.getBrain().getMemory(LAST_SLEPT).orElse(0) / 24000) * ((Number) COMFORT_PER_DAY_WITHOUT_SLEEP.get()).intValue()")
    @WrapOperation(method = "getVillagerComfort", at = @At(value = "MIXINEXTRAS:EXPRESSION", ordinal = 0), remap = false)
    private static int iguanatweaksreborn$fixDaysWithoutSleepComfort(int original) {
        return original;
    }*/
}
