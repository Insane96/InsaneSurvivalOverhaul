package insane96mcp.iguanatweaksreborn.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import insane96mcp.iguanatweaksreborn.module.mobs.villager.villagers.Villagers;
import insane96mcp.insanelib.module.base.TagsFeature;
import insane96mcp.insanelib.util.ModNBTData;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ZombieVillager.class)
public class ZombieVillagerMixin {

    @Inject(method = "finishConversion", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;onReputationEvent(Lnet/minecraft/world/entity/ai/village/ReputationEventType;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/ReputationEventHandler;)V"))
    public void iguanatweaksreborn$onConversion(CallbackInfo ci, @Local Villager villager) {
        ModNBTData.put(villager, Villagers.WAS_CONVERTED_ZOMBIE, TagsFeature.isSpawnType(MobSpawnType.CONVERSION, (ZombieVillager) (Object) this));
    }
}
