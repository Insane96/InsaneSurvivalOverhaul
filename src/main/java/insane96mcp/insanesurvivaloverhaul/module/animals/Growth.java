package insane96mcp.insanesurvivaloverhaul.module.animals;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanesurvivaloverhaul.data.modifier.Modifier;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import net.minecraft.world.entity.AgeableMob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@LoadFeature(module = ISOModules.ANIMALS, description = "Baby animals take longer to grow up. Configurable via livestock data.")
public class Growth extends Feature {
	@SubscribeEvent
	public void onLivingTick(EntityTickEvent.Pre event) {
		if (!this.isEnabled()
				|| event.getEntity().level().isClientSide
				|| !(event.getEntity() instanceof AgeableMob mob))
			return;

		slowdownAnimalGrowth(mob);
	}

	public static void slowdownAnimalGrowth(AgeableMob mob) {
		int growingAge = mob.getAge();
		if (growingAge >= 0)
			return;

		float chanceToSlowDown = Modifier.applyModifiers(0f, LivestockData.getModifiers(mob, data -> data.growthSpeedModifiers), mob.level(), mob.blockPosition(), mob);
		if (chanceToSlowDown <= 1d)
			return;

		double chance = 1d / chanceToSlowDown;
		if (mob.getRandom().nextFloat() > chance)
			mob.setAge(growingAge - 1);
	}
}
