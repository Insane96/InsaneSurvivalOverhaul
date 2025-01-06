package insane96mcp.iguanatweaksreborn.mixin;

import insane96mcp.iguanatweaksreborn.module.mobs.VillagersNerfs;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
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
		if (villagerData.getProfession() != newVillagerData.getProfession()) {
			VillagersNerfs.lockTrades($this);
		}
	}
}
