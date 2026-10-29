package insane96mcp.insanesurvivaloverhaul.module.animals;

import insane96mcp.insanelib.core.ModNBTData;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@LoadFeature(module = ISOModules.ANIMALS, description = "Animals in insanesurvivaloverhaul:prevent_breeding become 'well fed' when fed. While well fed they have an higher chance (given by the livestock data) to breed.")
public class WellFed extends Feature {
	public static ResourceLocation LAST_FED;

	/**
	 * Where last fed was saved before Livestock was split into multiple features. Used only to migrate old data.
	 */
	public static final ResourceLocation LEGACY_LAST_FED = InsaneSO.id("livestock/last_fed");

	@Config(min = 1, description = "Animals will be 'well fed' for this minutes when fed.")
	public static Integer duration = 60;
	@Config(min = 0, description = "How many minutes of well fed time must be left to the entity to be able to feed it again.")
	public static Integer leftToFeedAgain = 30;
	@Config(min = 0, description = "How many minutes of well fed time are consumed when animals breed.")
	public static Integer consumedOnBreed = 20;

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		LAST_FED = this.createDataKey("last_fed");
	}

	/**
	 * Moves the last fed time saved by the old Livestock feature to the new key
	 */
	@SubscribeEvent
	public void migrateLegacyLastFed(EntityJoinLevelEvent event) {
		if (event.getLevel().isClientSide()
				|| !(event.getEntity() instanceof LivingEntity living)
				|| !ModNBTData.contains(living, LEGACY_LAST_FED))
			return;

		setLastEat(living, ModNBTData.get(living, LEGACY_LAST_FED, Long.class));
		ModNBTData.remove(living, LEGACY_LAST_FED);
	}

	@SubscribeEvent
	public void onLivingTick(EntityTickEvent.Pre event) {
		if (!this.isEnabled()
				|| event.getEntity().level().isClientSide
				|| !(event.getEntity() instanceof LivingEntity living))
			return;

		if (hasBeenFedRecently(living) && living.getRandom().nextInt(200) == 0)
			((ServerLevel) living.level()).sendParticles(ParticleTypes.HAPPY_VILLAGER, living.getX(), living.getY() + living.getBbHeight() / 1.5f, living.getZ(), 5, 0.25, 0.25, 0.25, 0.2f);
	}

	@SubscribeEvent
	public void onAnimalFeed(PlayerInteractEvent.EntityInteract event) {
		if (event.isCanceled()
				|| !this.isEnabled()
				|| !(event.getTarget() instanceof Animal animal))
			return;

		feedToBreed(event, animal);
	}

	public static void feedToBreed(PlayerInteractEvent.EntityInteract event, Animal animal) {
		if (!animal.isFood(event.getItemStack())
				|| !animal.getType().is(Breeding.PREVENT_BREEDING)
				|| animal.isBaby()
				|| animal instanceof TamableAnimal tamableAnimal && !tamableAnimal.isTame())
			return;
		if (!canBeFed(animal)) {
			event.setCanceled(true);
			return;
		}
		int age = animal.getAge();
		if (!animal.level().isClientSide && age == 0 && animal.canFallInLove())
			setLastEat(animal);
	}

	/**
	 * Low priority so it runs after Breeding, which can cancel the event if breeding fails
	 */
	@SubscribeEvent(priority = EventPriority.LOW)
	public void onBreed(BabyEntitySpawnEvent event) {
		if (event.isCanceled()
				|| !this.isEnabled()
				|| !(event.getParentA() instanceof Animal parentA))
			return;

		consumeFeed(parentA);
		consumeFeed(event.getParentB());
	}

	public static void setLastEat(LivingEntity living, long lastFed) {
		ModNBTData.put(living, LAST_FED, lastFed);
	}

	public static void setLastEat(LivingEntity living) {
		setLastEat(living, living.level().getGameTime());
	}

	public static long getLastFed(LivingEntity living) {
		return ModNBTData.get(living, LAST_FED, Long.class);
	}

	public static boolean hasBeenFedRecently(LivingEntity living) {
		if (!Feature.isEnabled(WellFed.class))
			return false;
		long lastFed = getLastFed(living);
		return lastFed > 0 && living.level().getGameTime() - lastFed < ((long) duration * 60 * 20);
	}

	public static boolean canBeFed(LivingEntity living) {
		long lastFed = getLastFed(living);
		if (lastFed == 0)
			return true;
		return living.level().getGameTime() - lastFed > ((long) leftToFeedAgain * 60 * 20);
	}

	public static void consumeFeed(LivingEntity living) {
		long lastFed = getLastFed(living);
		if (lastFed > 0)
			lastFed -= (long) consumedOnBreed * 60 * 20;
		setLastEat(living, lastFed);
	}
}
