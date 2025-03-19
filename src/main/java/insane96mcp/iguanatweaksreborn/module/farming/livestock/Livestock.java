package insane96mcp.iguanatweaksreborn.module.farming.livestock;

import com.google.gson.annotations.SerializedName;
import insane96mcp.iguanatweaksreborn.InsaneSurvivalOverhaul;
import insane96mcp.iguanatweaksreborn.modifier.Modifier;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.module.experience.DroppedExperience;
import insane96mcp.iguanatweaksreborn.module.misc.DataPacks;
import insane96mcp.iguanatweaksreborn.network.message.ForgeDataIntSync;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.util.MCUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.ModList;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Label(name = "Livestock", description = "Slower breeding, Growing, Egging and Milking. Lower yield.")
@LoadFeature(module = Modules.Ids.FARMING)
public class Livestock extends Feature {
	public static final TagKey<EntityType<?>> MILKABLE = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(InsaneSurvivalOverhaul.MOD_ID, "milkable"));
	public static final TagKey<EntityType<?>> PREVENT_BREEDING = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(InsaneSurvivalOverhaul.MOD_ID, "prevent_breeding"));

	public static final String MILK_COOLDOWN_LANG = InsaneSurvivalOverhaul.MOD_ID + ".milk_cooldown";
	public static final String MILK_COOLDOWN = InsaneSurvivalOverhaul.RESOURCE_PREFIX + "milk_cooldown";

	public static final String LAST_FED = InsaneSurvivalOverhaul.RESOURCE_PREFIX + "last_fed";

	public static ResourceKey<DamageType> OLD_AGE = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(InsaneSurvivalOverhaul.MOD_ID, "old_age"));

	@Config
	@Label(name = "Chicken from egg chance", description = "Changes the chance for a chicken to come out from an egg (1 in this value). Vanilla is 8")
	public static Integer chickenFromEggChance = 8;

	@Config
	@Label(name = "Milk Cooldown", description = "Seconds until you can milk cows (or stew mooshrooms)")
	public static Integer milkingCooldown = 1200;

	@Config
	@Label(description = "If true, animals will no longer be able to be bred manually. Only animals in iguanatweaksreborn:prevent_breeding will be affected by this.")
	public static Boolean preventBreeding = true;

	@Config
	@Label(name = "Aging.Enable", description = "If true, animals will age and die of old age. Configurable via data packs. With the data pack enabled, adult will drop more goodies. With pehkui installed, animals will also be smaller/bigger.")
	public static Boolean agingEnable = true;

	@Config
	@Label(name = "Aging.Die of old age", description = "If true, animals die of old age.")
	public static Boolean agingDieOfOldAge = true;

	@Config
	@Label(name = "Aging.Stop at", description = "If Die of old age is disabled you can still make animals grow up to this age")
	public static Age agingStopAt = Age.ELDER;

	@Config(min = 0)
	@Label(name = "Aging.Starvation death", description = "If true, animals will die if not young and haven't been fed in a while.")
	public static Boolean agingStarvationDeath = false;

	@Config(min = 0)
	@Label(name = "Aging.Last fed duration", description = "How long (in minutes) will an animal remain marked as fed after eating?")
	public static Integer agingLastFedDuration = 60;

	@Config
	@Label(name = "Data Pack", description = "Enables a data pack that changes animal loot (reduced food drops) and slows down growing, breeding, egging etc")
	public static Boolean dataPack = true;

	public Livestock(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
		InsaneSurvivalOverhaul.addServerPack("livestock_changes", "Livestock Changes", () -> this.isEnabled() && !DataPacks.disableAllDataPacks && dataPack);
	}

	@SubscribeEvent
	public void onSheepJoinLevel(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
				|| !(event.getEntity() instanceof Sheep sheep))
			return;

		sheep.goalSelector.removeGoal(sheep.eatBlockGoal);
		sheep.eatBlockGoal = new ISOEatBlock(sheep);
		sheep.goalSelector.addGoal(5, sheep.eatBlockGoal);
	}

	@SubscribeEvent
	public void onLivingTick(LivingEvent.LivingTickEvent event) {
		if (!this.isEnabled())
			return;

		if (!event.getEntity().level().isClientSide) {
			slowdownAnimalGrowth(event);
			slowdownBreeding(event);
			aging(event);
			eggLay(event);
			tryAutoBreed(event);
			if (hasBeenFedRecently(event.getEntity()) && event.getEntity().getRandom().nextInt(200) == 0)
				((ServerLevel) event.getEntity().level()).sendParticles(ParticleTypes.HAPPY_VILLAGER, event.getEntity().getX(), event.getEntity().getY() + event.getEntity().getBbHeight() / 1.5f, event.getEntity().getZ(), 5, 0.25, 0.25, 0.25, 0.2f);
		}
		cowMilkTick(event);
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

	public static void tryAutoBreed(LivingEvent.LivingTickEvent event) {
		if (!(event.getEntity() instanceof Animal animal)
				|| !animal.canFallInLove()
				|| animal.getAge() != 0)
			return;

		if ((event.getEntity().tickCount + event.getEntity().getId()) % 600 != 0)
			return;
		if (event.getEntity().level().getEntities(event.getEntity(), animal.getBoundingBox().inflate(16d), entity -> entity.getType() == animal.getType()).size() > 16)
			return;
		List<Modifier> modifiersToApply = new ArrayList<>();
		LivestockDataReloadListener.LIVESTOCK_DATA.stream()
				.filter(data -> data.matches(animal))
				.forEach(data -> modifiersToApply.addAll(data.autoBreedChanceModifiers));
		float chance = Modifier.applyModifiers(0f, modifiersToApply, animal.level(), animal.blockPosition(), animal);
		if (chance <= 0d || animal.getRandom().nextFloat() >= chance)
			return;
        animal.setInLove(null);
    }

	public static boolean canSheepRegrowWool(Mob mob) {
		List<Modifier> modifiersToApply = new ArrayList<>();
		LivestockDataReloadListener.LIVESTOCK_DATA.stream()
				.filter(data -> data.matches(mob))
				.forEach(data -> modifiersToApply.addAll(data.sheepWoolGrowthChanceModifiers));
		float chance = Modifier.applyModifiers(1f, modifiersToApply, mob.level(), mob.blockPosition(), mob);
		if (chance >= 1d)
			return true;
		return mob.getRandom().nextFloat() < chance;
	}

	public static void aging(LivingEvent.LivingTickEvent event) {
        //noinspection DataFlowIssue
        if (!agingEnable
				|| !(event.getEntity() instanceof AgeableMob ageableMob)
				|| (ageableMob.level().getServer().getTickCount() + ageableMob.getId()) % 20 != 0
				|| ageableMob.isBaby())
			return;

		boolean forceUpdateScale = false;
		int age = ageableMob.getPersistentData().getInt(InsaneSurvivalOverhaul.RESOURCE_PREFIX + "age");
		int maxAge = ageableMob.getPersistentData().getInt(InsaneSurvivalOverhaul.RESOURCE_PREFIX + "max_age");
		//If a max age hasn't been set yet, calculate it
		if (maxAge == 0) {
			for (LivestockData data : LivestockDataReloadListener.LIVESTOCK_DATA) {
                if (data.matches(ageableMob) && data.livingDays != null) {
					maxAge = (int) (data.getLivingDays(ageableMob) * 20 * 60);
					ageableMob.getPersistentData().putInt(InsaneSurvivalOverhaul.RESOURCE_PREFIX + "max_age", maxAge);
                    break;
                }
			}
			if (maxAge == 0)
				return;
			forceUpdateScale = true;
		}
		Age prevAge = getAge(ageableMob);
		if (prevAge == agingStopAt && !agingDieOfOldAge)
			return;
		age++;
		Age newAge = getAge(ageableMob);
		if (agingStarvationDeath && newAge != Age.YOUNG && !hasBeenFedRecently(ageableMob))
			ageableMob.hurt(ageableMob.damageSources().starve(), Float.MAX_VALUE);
		if (newAge == Age.ELDER)
			MCUtils.applyModifier(ageableMob, Attributes.MOVEMENT_SPEED, UUID.fromString("e2083ae7-e37a-47c4-ab3e-84cf14fe6b6c"), "Old animal modifier", -0.35d, AttributeModifier.Operation.MULTIPLY_BASE, false);
		if (age >= maxAge) {
			if (agingDieOfOldAge)
				ageableMob.hurt(ageableMob.damageSources().source(OLD_AGE, null), Float.MAX_VALUE);
			return;
		}
        //noinspection DataFlowIssue - isClientSide is checked in caller method
        if (ModList.get().isLoaded("pehkui") && ((ageableMob.level().getServer().getTickCount() + ageableMob.getId()) % 100 == 0 || forceUpdateScale))
			PehkuiIntegration.setSize(ageableMob, newAge);
		ageableMob.getPersistentData().putInt(InsaneSurvivalOverhaul.RESOURCE_PREFIX + "age", age);
    }

	public static float getAgeRatio(AgeableMob mob) {
		return (float) mob.getPersistentData().getInt(InsaneSurvivalOverhaul.RESOURCE_PREFIX + "age") / (float) mob.getPersistentData().getInt(InsaneSurvivalOverhaul.RESOURCE_PREFIX + "max_age");
	}

	public static Age getAge(AgeableMob mob) {
		float ratio = getAgeRatio(mob);
		if (ratio < 0.30f)
			return Age.YOUNG;
		if (ratio < 0.70f)
			return Age.ADULT;
		return Age.ELDER;
	}

	public static void slowdownAnimalGrowth(LivingEvent.LivingTickEvent event) {
		if (!(event.getEntity() instanceof AgeableMob mob))
			return;

		int growingAge = mob.getAge();
		if (growingAge >= 0)
			return;

		List<Modifier> modifiersToApply = new ArrayList<>();
		LivestockDataReloadListener.LIVESTOCK_DATA.stream()
				.filter(data -> data.matches(mob))
				.forEach(data -> modifiersToApply.addAll(data.growthSpeedModifiers));
		float chanceToSlowDown = Modifier.applyModifiers(0f, modifiersToApply, mob.level(), mob.blockPosition(), mob);
		if (chanceToSlowDown <= 1d)
			return;

		double chance = 1d / chanceToSlowDown;
		if (mob.getRandom().nextFloat() > chance)
			mob.setAge(growingAge - 1);
	}

	public static void slowdownBreeding(LivingEvent.LivingTickEvent event) {
		if (!(event.getEntity() instanceof AgeableMob mob))
			return;

		int growingAge = mob.getAge();
		if (growingAge <= 0)
			return;

		List<Modifier> modifiersToApply = new ArrayList<>();
		LivestockDataReloadListener.LIVESTOCK_DATA.stream()
				.filter(data -> data.matches(mob))
				.forEach(data -> modifiersToApply.addAll(data.breedingCooldownModifiers));
		float chanceToSlowDown = Modifier.applyModifiers(0f, modifiersToApply, mob.level(), mob.blockPosition(), mob);
		if (chanceToSlowDown <= 1d)
			return;

		double chance = 1d / chanceToSlowDown;
		if (mob.getRandom().nextFloat() > chance)
			mob.setAge(growingAge + 1);
	}

	public static void eggLay(LivingEvent.LivingTickEvent event) {
		if (!(event.getEntity() instanceof Chicken chicken)
				|| chicken.isBaby())
			return;

		if (chicken.eggTime < 0)
			return;

		List<Modifier> modifiersToApply = new ArrayList<>();
		LivestockDataReloadListener.LIVESTOCK_DATA.stream()
				.filter(data -> data.matches(chicken))
				.forEach(data -> modifiersToApply.addAll(data.eggLayCooldownModifiers));
		float chanceToSlowDown = Modifier.applyModifiers(0f, modifiersToApply, chicken.level(), chicken.blockPosition(), chicken);
		if (chanceToSlowDown <= 1d)
			return;

		double chance = 1d / chanceToSlowDown;
		if (chicken.getRandom().nextFloat() > chance)
			chicken.eggTime++;
	}

	public void cowMilkTick(LivingEvent.LivingTickEvent event) {
		if (event.getEntity().tickCount % 20 != 0 ||
				!(event.getEntity() instanceof Animal animal))
			return;

		CompoundTag cowNBT = animal.getPersistentData();
		int milkCooldown = cowNBT.getInt(MILK_COOLDOWN);
		if (milkCooldown > 0)
			milkCooldown -= 20;
		cowNBT.putInt(MILK_COOLDOWN, milkCooldown);
	}

	@SubscribeEvent
	public void onAnimalFeed(PlayerInteractEvent.EntityInteract event) {
		if (event.isCanceled()
				|| !this.isEnabled()
				|| !(event.getTarget() instanceof Animal animal)
				|| !animal.getType().is(PREVENT_BREEDING)
				|| !animal.isFood(event.getItemStack()))
			return;

		if (!canBeFed(animal)) {
			event.setCanceled(true);
			return;
		}
		int age = animal.getAge();
		if (!animal.level().isClientSide && age == 0 && animal.canFallInLove()) {
			setLastEat(animal);
		}
	}

	public static void setLastEat(LivingEntity living) {
		living.getPersistentData().putLong(LAST_FED, living.level().getGameTime());
	}

	public static boolean hasBeenFedRecently(LivingEntity living) {
		long lastFed = living.getPersistentData().getLong(LAST_FED);
		return lastFed > 0 && living.level().getGameTime() - lastFed < (agingLastFedDuration * 60 * 20);
	}

	public static boolean canBeFed(LivingEntity living) {
		long lastFed = living.getPersistentData().getLong(LAST_FED);
		if (lastFed == 0)
			return true;
		return living.level().getGameTime() - lastFed > (agingLastFedDuration / 2f * 60 * 20);
	}

	public static void consumeFeed(LivingEntity living) {
		long lastFed = living.getPersistentData().getLong(LAST_FED);
		if (lastFed > 0)
			lastFed -= (long) (agingLastFedDuration / 3f * 60 * 20);
		living.getPersistentData().putLong(LAST_FED, lastFed);
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onAnimalMilk(PlayerInteractEvent.EntityInteract event) {
		if (!this.isEnabled()
				|| !event.getEntity().getType().is(MILKABLE)
				|| !(event.getTarget() instanceof Animal animal)
				|| animal.getAge() < 0)
			return;

		Player player = event.getEntity();
		InteractionHand hand = event.getHand();
		ItemStack equipped = player.getItemInHand(hand);
		if (equipped.isEmpty() || equipped.getItem() == Items.AIR)
			return;
		Item item = equipped.getItem();
		if ((!FluidUtil.getFluidHandler(equipped).isPresent() || !FluidStack.loadFluidStackFromNBT(equipped.getTag()).isEmpty()) && (!(animal instanceof MushroomCow) || item != Items.BOWL))
			return;
		CompoundTag cowNBT = animal.getPersistentData();
		int milkCooldown = cowNBT.getInt(MILK_COOLDOWN);
		if (milkCooldown > 0) {
			event.setCanceled(true);
			if (!player.level().isClientSide) {
				animal.playSound(SoundEvents.COW_HURT, 0.4F, (event.getEntity().level().random.nextFloat() - event.getEntity().level().random.nextFloat()) * 0.2F + 1.2F);
				MutableComponent message = Component.translatable(MILK_COOLDOWN_LANG, animal.getDisplayName());
				player.displayClientMessage(message, true);
			}
			else
				event.setCancellationResult(InteractionResult.SUCCESS);
		}
		else if (!animal.level().isClientSide) {
			List<Modifier> modifiersToApply = new ArrayList<>();
			LivestockDataReloadListener.LIVESTOCK_DATA.stream()
					.filter(data -> data.matches(animal))
					.forEach(data -> modifiersToApply.addAll(data.cowFluidCooldownModifiers));
			float cooldown = Modifier.applyModifiers(milkingCooldown, modifiersToApply, animal.level(), animal.blockPosition(), animal);
			DroppedExperience.tryGenerateMilkXp(animal);
			if (cooldown <= 0)
				return;

			milkCooldown = (int) (cooldown * 20);
			cowNBT.putInt(MILK_COOLDOWN, milkCooldown);
			ForgeDataIntSync.sync(animal, MILK_COOLDOWN, milkCooldown);
			//player.swing(event.getHand());
		}
	}

	@SubscribeEvent
	public void onEntityLoad(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
				|| event.getLevel().isClientSide
				|| !(event.getEntity() instanceof LivingEntity livingEntity)
				|| !livingEntity.getPersistentData().contains(MILK_COOLDOWN))
			return;

		ForgeDataIntSync.sync(livingEntity, MILK_COOLDOWN, livingEntity.getPersistentData().getInt(MILK_COOLDOWN));
	}

	@SubscribeEvent
	public void onPlayerJoinWorld(PlayerEvent.PlayerLoggedInEvent event) {
		if (!this.isEnabled()
				|| event.getEntity().level().isClientSide)
			return;

		for (Entity entity : ((ServerLevel) event.getEntity().level()).getEntities().getAll()) {
			if (entity instanceof LivingEntity livingEntity && livingEntity.getPersistentData().contains(MILK_COOLDOWN))
				ForgeDataIntSync.sync((ServerPlayer) event.getEntity(), livingEntity, MILK_COOLDOWN, livingEntity.getPersistentData().getInt(MILK_COOLDOWN));
		}
	}

	@SubscribeEvent
	public void onBreed(BabyEntitySpawnEvent event) {
		if (!this.isEnabled()
				|| !(event.getParentA() instanceof Animal parentA))
			return;

		if (failBreeding(event, parentA))
			return;
		twins(event, parentA);
		consumeFeed(parentA);
		consumeFeed(event.getParentB());
	}

	private boolean failBreeding(BabyEntitySpawnEvent event, Animal parentA) {
		List<Modifier> modifiersToApply = new ArrayList<>();
		LivestockDataReloadListener.LIVESTOCK_DATA.stream()
				.filter(data -> data.matches(parentA))
				.forEach(data -> modifiersToApply.addAll(data.breedingFailChanceModifiers));
		float failChance = Modifier.applyModifiers(0f, modifiersToApply, parentA.level(), parentA.blockPosition(), parentA);
		if (failChance <= 0)
			return false;
		if (failChance >= 1f || parentA.getRandom().nextFloat() < failChance) {
			event.setCanceled(true);
			return true;
		}
		return false;
	}

	private void twins(BabyEntitySpawnEvent event, Animal parentA) {
		List<Modifier> modifiersToApplyTwins = new ArrayList<>();
		LivestockDataReloadListener.LIVESTOCK_DATA.stream()
				.filter(data -> data.matches(parentA))
				.forEach(data -> modifiersToApplyTwins.addAll(data.twinsChanceModifiers));
		float twinsChance = Modifier.applyModifiers(0f, modifiersToApplyTwins, parentA.level(), parentA.blockPosition(), parentA);

		if (parentA.getRandom().nextFloat() < twinsChance && event.getParentB() instanceof Animal)
			parentA.spawnChildFromBreeding((ServerLevel) parentA.level(), (Animal) event.getParentB());
	}

	public enum Age {
		@SerializedName("young")
		YOUNG,
		@SerializedName("adult")
		ADULT,
		@SerializedName("elder")
		ELDER
	}
}