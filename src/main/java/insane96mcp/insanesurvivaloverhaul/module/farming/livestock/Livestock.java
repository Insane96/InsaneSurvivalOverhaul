package insane96mcp.insanesurvivaloverhaul.module.farming.livestock;

import com.google.gson.annotations.SerializedName;
import insane96mcp.insanelib.core.ModNBTData;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.util.MCUtils;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.data.modifier.Modifier;
import insane96mcp.insanesurvivaloverhaul.mixin.accessor.SheepAccessor;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.module.misc.Packs;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
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
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@LoadFeature(module = ISOModules.FARMING, description = "Slower breeding, Growing, Egging and Milking. Lower yield.")
public class Livestock extends Feature {
	public static final TagKey<EntityType<?>> MILKABLE = TagKey.create(Registries.ENTITY_TYPE, InsaneSO.location("milkable"));
	public static final TagKey<EntityType<?>> PREVENT_BREEDING = TagKey.create(Registries.ENTITY_TYPE, InsaneSO.location("prevent_breeding"));

	public static final ResourceLocation OLD_AGE_MODIFIER = InsaneSO.location("livestock/old_age_modifier");
	public static final String MILK_COOLDOWN_LANG = InsaneSO.lang("milk_cooldown");

	public static ResourceLocation AGE;
	public static ResourceLocation MAX_AGE;
	public static ResourceLocation STOP_AGING;
	public static ResourceLocation LAST_FED;
	public static ResourceLocation MILK_COOLDOWN;

	public static ResourceKey<DamageType> OLD_AGE = ResourceKey.create(Registries.DAMAGE_TYPE, InsaneSO.location("old_age"));

	@Config(description = "Changes the chance for a chicken to come out from an egg (1 in this value). Vanilla is 8")
	public static Integer chickenFromEggChance = 8;

	@Config(min = 0,description = "Seconds until you can milk cows (or stew mooshrooms)")
	public static Integer fluidCooldown = 1200;

    @Config(description = "If true, animals will no longer be able to be bred manually. Only animals in iguanatweaksreborn:prevent_breeding will be affected by this.")
    public static Boolean preventBreeding = true;
    @Config(description = "If true, mobs can randomly fall in love and possibly breed")
    public static Boolean autoBreed = true;
    @Config(min = 0, description = "If true, animals will be 'well fed' for this ticks when fed. During this time they have an higher chance (given by the livestock data) to breed. Set to 0 to disable.")
    public static Integer wellFed$duration = 60;
    @Config(min = 0, description = "How much well fed time must be left to the entity to be able to feed it again.")
    public static Integer wellFed$leftToFeedAgain = 30;
    @Config(min = 0, description = "How much well fed time is consumed when animals breed.")
    public static Integer wellFed$consumedOnBreed = 20;

	@Config(description = "If true, animals will age and die of old age. Configurable via data packs. With the data pack enabled, adult will drop more goodies. With pehkui installed, animals will also be smaller/bigger.")
	public static Boolean aging$enable = true;

	@Config(description = "If true, animals die of old age.")
	public static Boolean aging$dieOfOldAge = true;

	@Config(description = "If Die of old age is disabled you can still make animals grow up to this age")
	public static Age aging$stopAt = Age.ELDER;

	@Config(description = "If fed a poisonous potato, animals will stop aging at a 'Stop At With Poisonous Potato' age.")
	public static Boolean aging$stopAgingWithPoisonousPotato = true;

	@Config(description = "If true, animals will die if not young and haven't been fed in a while.")
	public static Boolean aging$starvationDeath = false;


	@Config(description = "Enables a data pack that changes animal loot (reduced food drops) and slows down growing, breeding, egging etc")
	public static Boolean dataPack = true;

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		InsaneSO.addServerPack("livestock_changes", "Livestock Changes", () -> this.isEnabled() && !Packs.disableAllDataPacks && dataPack);
		AGE = this.createDataKey("age");
		MAX_AGE = this.createDataKey("max_age");
		STOP_AGING = this.createDataKey("stop_aging");
		LAST_FED = this.createDataKey("last_fed");
		MILK_COOLDOWN = this.createDataKey("milk_cooldown");
	}

	@SubscribeEvent
	public void onSheepJoinLevel(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
				|| !(event.getEntity() instanceof Sheep sheep))
			return;

		SheepAccessor sheepAccessor = (SheepAccessor)(Object) sheep;
		sheep.goalSelector.removeGoal(sheepAccessor.getEatBlockGoal());
		sheepAccessor.setEatBlockGoal(new ISOEatBlock(sheep));
		sheep.goalSelector.addGoal(5, sheepAccessor.getEatBlockGoal());
	}

	@SubscribeEvent
	public void onLivingTick(EntityTickEvent.Pre event) {
		if (!this.isEnabled()
				|| !(event.getEntity() instanceof LivingEntity living))
			return;

		if (!living.level().isClientSide) {
			slowdownAnimalGrowth(event);
			slowdownBreeding(event);
			aging(event);
			eggLay(event);
			tryAutoBreed(event);
			if (hasBeenFedRecently(living) && living.getRandom().nextInt(200) == 0)
				((ServerLevel) event.getEntity().level()).sendParticles(ParticleTypes.HAPPY_VILLAGER, living.getX(), living.getY() + living.getBbHeight() / 1.5f, living.getZ(), 5, 0.25, 0.25, 0.25, 0.2f);
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

	public static void tryAutoBreed(EntityTickEvent.Pre event) {
		if (!autoBreed
				|| !(event.getEntity() instanceof Animal animal)
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

	public static void aging(EntityTickEvent.Pre event) {
        //noinspection DataFlowIssue
        if (!aging$enable
				|| !(event.getEntity() instanceof AgeableMob ageableMob)
				|| (ageableMob.level().getServer().getTickCount() + ageableMob.getId()) % 20 != 0
				|| ageableMob.isBaby())
			return;

		if (aging$stopAgingWithPoisonousPotato && ModNBTData.get(ageableMob, STOP_AGING, Boolean.class))
			return;

		boolean forceUpdateScale = false;
		int age = ModNBTData.get(ageableMob, AGE, Integer.class);
		int maxAge = ModNBTData.get(ageableMob, MAX_AGE, Integer.class);
		//If a max age hasn't been set yet, calculate it
		if (maxAge == 0) {
			for (LivestockData data : LivestockDataReloadListener.LIVESTOCK_DATA) {
                if (data.matches(ageableMob) && data.livingDays != null) {
					maxAge = (int) (data.getLivingDays(ageableMob) * 20 * 60);
					ModNBTData.put(ageableMob, MAX_AGE, maxAge);
                    break;
                }
			}
			if (maxAge == 0)
				return;
			forceUpdateScale = true;
		}
		Age currAge = getAge(ageableMob);
		if (currAge == aging$stopAt && !aging$dieOfOldAge)
			return;
		age++;
		Age newAge = getAge(ageableMob);
		if (aging$starvationDeath && newAge != Age.YOUNG && !hasBeenFedRecently(ageableMob))
			ageableMob.hurt(ageableMob.damageSources().starve(), Float.MAX_VALUE);
		if (newAge == Age.ELDER)
			MCUtils.applyModifier(ageableMob, Attributes.MOVEMENT_SPEED, OLD_AGE_MODIFIER, -0.35d, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, false);
		if (age >= maxAge) {
			if (aging$dieOfOldAge)
				ageableMob.hurt(ageableMob.damageSources().source(OLD_AGE, null), Float.MAX_VALUE);
			return;
		}
        //noinspection DataFlowIssue - isClientSide is checked in caller method
        if ((ageableMob.level().getServer().getTickCount() + ageableMob.getId()) % 100 == 0 || forceUpdateScale)
			setSize(ageableMob, newAge);
		ModNBTData.put(ageableMob, AGE, age);
    }

	public static float getAgeRatio(AgeableMob mob) {
		return (float) ModNBTData.get(mob, AGE, Integer.class) / (float) ModNBTData.get(mob, MAX_AGE, Integer.class);
	}

	public static Age getAge(AgeableMob mob) {
		float ratio = getAgeRatio(mob);
		if (ratio < 0.30f)
			return Age.YOUNG;
		if (ratio < 0.70f)
			return Age.ADULT;
		return Age.ELDER;
	}

	public static void slowdownAnimalGrowth(EntityTickEvent.Pre event) {
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

	public static void slowdownBreeding(EntityTickEvent.Pre event) {
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

	public static void eggLay(EntityTickEvent.Pre event) {
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

	@SubscribeEvent
	public void onAnimalFeed(PlayerInteractEvent.EntityInteract event) {
		if (event.isCanceled()
				|| !this.isEnabled()
				|| !(event.getTarget() instanceof Animal animal))
			return;

		feedToBreed(event, animal);
		if (event.isCanceled())
			return;
		poisonAnimal(event, animal);
	}

	public static void feedToBreed(PlayerInteractEvent.EntityInteract event, Animal animal) {
		if (wellFed$duration <= 0
                || !animal.isFood(event.getItemStack())
				|| !animal.getType().is(PREVENT_BREEDING)
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

	public static void poisonAnimal(PlayerInteractEvent.EntityInteract event, Animal animal) {
		if (!aging$stopAgingWithPoisonousPotato
				|| !event.getItemStack().is(Items.POISONOUS_POTATO)
				|| animal.isBaby()
				|| ModNBTData.get(animal, STOP_AGING, Boolean.class))
			return;
		animal.addEffect(new MobEffectInstance(MobEffects.POISON, 80));
		ModNBTData.put(animal, STOP_AGING, true);
		event.getItemStack().shrink(1);
		event.getEntity().swing(event.getHand());
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
		long lastFed = getLastFed(living);
		return lastFed > 0 && living.level().getGameTime() - lastFed < ((long) wellFed$duration * 60 * 20);
	}

	public static boolean canBeFed(LivingEntity living) {
		long lastFed = getLastFed(living);;
		if (lastFed == 0)
			return true;
		return living.level().getGameTime() - lastFed > ((long) wellFed$leftToFeedAgain * 60 * 20);
	}

	public static void consumeFeed(LivingEntity living) {
		long lastFed = getLastFed(living);;
		if (lastFed > 0)
			lastFed -= (long) wellFed$consumedOnBreed * 60 * 20;
		setLastEat(living, lastFed);
	}

	public void cowMilkTick(EntityTickEvent.Pre event) {
		if (event.getEntity().tickCount % 20 != 0
				|| !event.getEntity().getType().is(MILKABLE)
				|| !(event.getEntity() instanceof Animal animal))
			return;

		int milkCooldown = ModNBTData.get(animal, MILK_COOLDOWN, Integer.class);
		if (milkCooldown > 0)
			milkCooldown -= 20;
		ModNBTData.put(animal, MILK_COOLDOWN, milkCooldown);
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onAnimalMilk(PlayerInteractEvent.EntityInteract event) {
		if (!this.isEnabled()
				|| !event.getTarget().getType().is(MILKABLE)
				|| (event.getTarget() instanceof Animal animal && animal.getAge() < 0)
				|| !(event.getTarget() instanceof LivingEntity living))
			return;

		Player player = event.getEntity();
		ItemStack equipped = player.getItemInHand(event.getHand());
		if (equipped.isEmpty())
			return;
		Item item = equipped.getItem();
		Optional<IFluidHandlerItem> fluidHandler = FluidUtil.getFluidHandler(equipped);
		if ((fluidHandler.isEmpty() || !fluidHandler.get().getFluidInTank(0).isEmpty()) && (!(living instanceof MushroomCow) || item != Items.BOWL))
			return;
		int milkCooldown = ModNBTData.get(living, MILK_COOLDOWN, Integer.class);
		if (milkCooldown > 0) {
			event.setCanceled(true);
			if (!player.level().isClientSide) {
				living.playSound(SoundEvents.COW_HURT, 0.4F, (event.getEntity().level().random.nextFloat() - event.getEntity().level().random.nextFloat()) * 0.2F + 1.2F);
				MutableComponent message = Component.translatable(MILK_COOLDOWN_LANG, living.getDisplayName());
				player.displayClientMessage(message, true);
			}
			else
				event.setCancellationResult(InteractionResult.SUCCESS);
		}
		else if (!living.level().isClientSide) {
			List<Modifier> modifiersToApply = new ArrayList<>();
			LivestockDataReloadListener.LIVESTOCK_DATA.stream()
					.filter(data -> data.matches(living))
					.forEach(data -> modifiersToApply.addAll(data.cowFluidCooldownModifiers));
			//DroppedExperience.tryGenerateMilkXp(living);

			float cooldown = Modifier.applyModifiers(fluidCooldown, modifiersToApply, living.level(), living.blockPosition(), living);
			if (cooldown <= 0)
				return;
			milkCooldown = (int) (cooldown * 20);
			ModNBTData.put(living, MILK_COOLDOWN, milkCooldown);
			ClientboundMilkCooldownPacket.sync(living, milkCooldown);
		}
	}

	@SubscribeEvent
	public void onEntityLoad(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
				|| event.getLevel().isClientSide
				|| !(event.getEntity() instanceof LivingEntity livingEntity)
				|| ModNBTData.contains(livingEntity, MILK_COOLDOWN))
			return;

		ClientboundMilkCooldownPacket.sync(livingEntity, ModNBTData.get(livingEntity, MILK_COOLDOWN, Integer.class));
	}

	@SubscribeEvent
	public void onPlayerJoinWorld(PlayerEvent.PlayerLoggedInEvent event) {
		if (!this.isEnabled()
				|| event.getEntity().level().isClientSide)
			return;

		for (Entity entity : ((ServerLevel) event.getEntity().level()).getEntities().getAll()) {
			if (entity instanceof LivingEntity livingEntity && ModNBTData.contains(livingEntity, MILK_COOLDOWN))
				ClientboundMilkCooldownPacket.syncToPlayer((ServerPlayer) event.getEntity(), livingEntity);
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

	public enum Age implements net.minecraft.util.StringRepresentable {
		@SerializedName("young")
		YOUNG,
		@SerializedName("adult")
		ADULT,
		@SerializedName("elder")
		ELDER;

		public static final com.mojang.serialization.Codec<Age> CODEC = net.minecraft.util.StringRepresentable.fromEnum(Age::values);

		@Override
		public String getSerializedName() {
			return switch (this) {
				case YOUNG -> "young";
				case ADULT -> "adult";
				case ELDER -> "elder";
			};
		}
	}

	public static boolean isBreedingDisabled(Animal animal) {
		return Feature.isEnabled(Livestock.class) && preventBreeding && animal.getType().is(PREVENT_BREEDING);
	}

	public static final ResourceLocation SCALE_MODIFIER_ID = InsaneSO.location("livestock/age_scale");

	public static void setSize(LivingEntity entity, Age age) {
		AttributeInstance scaleAttr = entity.getAttribute(Attributes.SCALE);
		if (scaleAttr == null)
			return;
		scaleAttr.removeModifier(SCALE_MODIFIER_ID);
		float scale = switch (age) {
			case YOUNG -> 0.85f;
			case ADULT -> 1.15f;
			case ELDER -> 1.0f;
		};
		if (scale != 1.0f)
			scaleAttr.addPermanentModifier(new AttributeModifier(SCALE_MODIFIER_ID, scale - 1.0f, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
	}

}
