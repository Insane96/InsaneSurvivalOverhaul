package insane96mcp.insanesurvivaloverhaul.module.animals;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.data.modifier.Modifier;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@LoadFeature(module = ISOModules.ANIMALS, description = "Slower breeding, breeding can fail, twins, and animals that fall in love on their own. Configurable via livestock data.")
public class Breeding extends Feature {
	public static final TagKey<EntityType<?>> PREVENT_BREEDING = TagKey.create(Registries.ENTITY_TYPE, InsaneSO.id("prevent_breeding"));

	@Config(description = "If true, animals will no longer be able to be bred manually. Only animals in insanesurvivaloverhaul:prevent_breeding will be affected by this.")
	public static Boolean preventBreeding = true;
	@Config(description = "If true, mobs can randomly fall in love and possibly breed")
	public static Boolean autoBreed = true;

	@SubscribeEvent
	public void onLivingTick(EntityTickEvent.Pre event) {
		if (!this.isEnabled()
				|| event.getEntity().level().isClientSide
				|| !(event.getEntity() instanceof AgeableMob mob))
			return;

		slowdownBreeding(mob);
		tryAutoBreed(mob);
	}

	public static void slowdownBreeding(AgeableMob mob) {
		int growingAge = mob.getAge();
		if (growingAge <= 0)
			return;

		float chanceToSlowDown = Modifier.applyModifiers(0f, LivestockData.getModifiers(mob, data -> data.breedingCooldownModifiers), mob.level(), mob.blockPosition(), mob);
		if (chanceToSlowDown <= 1d)
			return;

		double chance = 1d / chanceToSlowDown;
		if (mob.getRandom().nextFloat() > chance)
			mob.setAge(growingAge + 1);
	}

	public static void tryAutoBreed(AgeableMob mob) {
		if (!autoBreed
				|| !(mob instanceof Animal animal)
				|| !animal.canFallInLove()
				|| animal.getAge() != 0)
			return;

		if ((animal.tickCount + animal.getId()) % 600 != 0)
			return;
		if (animal.level().getEntities(animal, animal.getBoundingBox().inflate(16d), entity -> entity.getType() == animal.getType()).size() > 16)
			return;
		float chance = Modifier.applyModifiers(0f, LivestockData.getModifiers(animal, data -> data.autoBreedChanceModifiers), animal.level(), animal.blockPosition(), animal);
		if (chance <= 0d || animal.getRandom().nextFloat() >= chance)
			return;
		animal.setInLove(null);
	}

	@SubscribeEvent
	public void onBreed(BabyEntitySpawnEvent event) {
		if (event.isCanceled()
				|| !this.isEnabled()
				|| !(event.getParentA() instanceof Animal parentA))
			return;

		if (failBreeding(event, parentA))
			return;
		twins(event, parentA);
	}

	private boolean failBreeding(BabyEntitySpawnEvent event, Animal parentA) {
		float failChance = Modifier.applyModifiers(0f, LivestockData.getModifiers(parentA, data -> data.breedingFailChanceModifiers), parentA.level(), parentA.blockPosition(), parentA);
		if (failChance <= 0)
			return false;
		if (failChance >= 1f || parentA.getRandom().nextFloat() < failChance) {
			event.setCanceled(true);
			return true;
		}
		return false;
	}

	private void twins(BabyEntitySpawnEvent event, Animal parentA) {
		float twinsChance = Modifier.applyModifiers(0f, LivestockData.getModifiers(parentA, data -> data.twinsChanceModifiers), parentA.level(), parentA.blockPosition(), parentA);

		if (event.getChild() == null
				|| !(event.getParentB() instanceof Animal parentB)
				|| parentA.getRandom().nextFloat() >= twinsChance)
			return;

		//Spawned manually instead of with spawnChildFromBreeding, so BabyEntitySpawnEvent isn't fired again, and stats, advancements and xp aren't given twice
		ServerLevel level = (ServerLevel) parentA.level();
		AgeableMob twin = parentA.getBreedOffspring(level, parentB);
		if (twin == null)
			return;
		twin.setBaby(true);
		twin.moveTo(parentA.getX(), parentA.getY(), parentA.getZ(), 0.0F, 0.0F);
		level.addFreshEntityWithPassengers(twin);
	}

	public static boolean isBreedingDisabled(Animal animal) {
		return Feature.isEnabled(Breeding.class) && preventBreeding && animal.getType().is(PREVENT_BREEDING);
	}

	public static double getRandomXWithin(LivingEntity entity, double delta) {
		return getRandomValueInWidth(entity, delta) + entity.getX();
	}

	public static double getRandomZWithin(LivingEntity entity, double delta) {
		return getRandomValueInWidth(entity, delta) + entity.getZ();
	}

	public static double getRandomValueInWidth(LivingEntity entity, double delta) {
		return Mth.nextDouble(entity.getRandom(), -entity.getBbWidth() - delta, entity.getBbWidth() + delta);
	}
}
