package insane96mcp.iguanatweaksreborn.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.iguanatweaksreborn.module.mobs.villager.villagers.Villagers;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Villager.class)
public class VillagerMixin {

	@Inject(at = @At(value = "HEAD"), method = "setVillagerData")
	private void onSetVillagerData(VillagerData newVillagerData, CallbackInfo ci) {
		Villager $this = (Villager)(Object) this;
		VillagerData villagerData = $this.getVillagerData();
		if (villagerData.getProfession() != newVillagerData.getProfession() && newVillagerData.getProfession() != VillagerProfession.NONE) {
			Villagers.lockTrades($this);
		}
	}

	@ModifyExpressionValue(method = "finalizeSpawn", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/npc/VillagerProfession;NONE:Lnet/minecraft/world/entity/npc/VillagerProfession;"))
	public VillagerProfession iguanatweaksreborn$trySpawnNitwit(VillagerProfession original) {
		if (Villagers.shouldSpawnAsNitwit((Villager) (Object) this))
			return VillagerProfession.NITWIT;
		return original;
	}
}
