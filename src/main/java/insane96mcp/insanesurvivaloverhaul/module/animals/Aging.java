package insane96mcp.insanesurvivaloverhaul.module.animals;

import com.google.gson.annotations.SerializedName;
import insane96mcp.insanelib.core.ModNBTData;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.util.MCUtils;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@LoadFeature(module = ISOModules.ANIMALS, description = "Animals age, changing size and possibly dying of old age. Configurable via livestock data. Adult sheep drop more wool and, with the Livestock data pack enabled, adults drop more goodies.")
public class Aging extends Feature {
	public static ResourceLocation OLD_AGE_MODIFIER;
	public static ResourceLocation SCALE_MODIFIER_ID;

	public static ResourceLocation AGE;
	public static ResourceLocation MAX_AGE;
	public static ResourceLocation STOP_AGING;

	/**
	 * Where age, max age and stop aging were saved before Livestock was split into multiple features. Used only to migrate old data.
	 */
	public static final ResourceLocation LEGACY_AGE = InsaneSO.id("livestock/age");
	public static final ResourceLocation LEGACY_MAX_AGE = InsaneSO.id("livestock/max_age");
	public static final ResourceLocation LEGACY_STOP_AGING = InsaneSO.id("livestock/stop_aging");
	/**
	 * Id of the scale modifier before Livestock was split into multiple features. Used only to migrate old modifiers.
	 * The old age movement speed modifier is transient, so it doesn't need to be migrated.
	 */
	public static final ResourceLocation LEGACY_SCALE_MODIFIER_ID = InsaneSO.id("livestock/age_scale");

	public static ResourceKey<DamageType> OLD_AGE = ResourceKey.create(Registries.DAMAGE_TYPE, InsaneSO.id("old_age"));

	@Config(description = "If true, animals die of old age.")
	public static Boolean dieOfOldAge = true;

	@Config(description = "If Die of old age is disabled you can still make animals grow up to this age")
	public static Age stopAt = Age.ELDER;

	@Config(description = "If fed a poisonous potato, animals will stop aging.")
	public static Boolean stopAgingWithPoisonousPotato = true;

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		AGE = this.createDataKey("age");
		MAX_AGE = this.createDataKey("max_age");
		STOP_AGING = this.createDataKey("stop_aging");
		OLD_AGE_MODIFIER = this.createDataKey("old_age_modifier");
		SCALE_MODIFIER_ID = this.createDataKey("age_scale");
	}

	/**
	 * Moves the age data and scale modifier saved by the old Livestock feature to the new keys and id
	 */
	@SubscribeEvent
	public void migrateLegacyAge(EntityJoinLevelEvent event) {
		if (event.getLevel().isClientSide()
				|| !(event.getEntity() instanceof AgeableMob ageableMob))
			return;

		if (ModNBTData.contains(ageableMob, LEGACY_AGE)) {
			ModNBTData.put(ageableMob, AGE, ModNBTData.get(ageableMob, LEGACY_AGE, Integer.class));
			ModNBTData.remove(ageableMob, LEGACY_AGE);
		}
		if (ModNBTData.contains(ageableMob, LEGACY_MAX_AGE)) {
			ModNBTData.put(ageableMob, MAX_AGE, ModNBTData.get(ageableMob, LEGACY_MAX_AGE, Integer.class));
			ModNBTData.remove(ageableMob, LEGACY_MAX_AGE);
		}
		if (ModNBTData.contains(ageableMob, LEGACY_STOP_AGING)) {
			ModNBTData.put(ageableMob, STOP_AGING, ModNBTData.get(ageableMob, LEGACY_STOP_AGING, Boolean.class));
			ModNBTData.remove(ageableMob, LEGACY_STOP_AGING);
		}
		AttributeInstance scaleAttr = ageableMob.getAttribute(Attributes.SCALE);
		if (scaleAttr != null) {
			AttributeModifier legacyScale = scaleAttr.getModifier(LEGACY_SCALE_MODIFIER_ID);
			if (legacyScale != null) {
				scaleAttr.removeModifier(LEGACY_SCALE_MODIFIER_ID);
				scaleAttr.addOrReplacePermanentModifier(new AttributeModifier(SCALE_MODIFIER_ID, legacyScale.amount(), legacyScale.operation()));
			}
		}
	}

	@SubscribeEvent
	public void onLivingTick(EntityTickEvent.Pre event) {
		if (!this.isEnabled()
				|| event.getEntity().level().isClientSide
				|| !(event.getEntity() instanceof AgeableMob ageableMob))
			return;

		aging(ageableMob);
	}

	public static void aging(AgeableMob ageableMob) {
		//noinspection DataFlowIssue - isClientSide is checked in caller method
		if ((ageableMob.level().getServer().getTickCount() + ageableMob.getId()) % 20 != 0
				|| ageableMob.isBaby())
			return;

		if (stopAgingWithPoisonousPotato && ModNBTData.get(ageableMob, STOP_AGING, Boolean.class))
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
		if (currAge == stopAt && !dieOfOldAge)
			return;
		age++;
		ModNBTData.put(ageableMob, AGE, age);
		Age newAge = getAge(ageableMob);
		if (newAge == Age.ELDER)
			MCUtils.applyModifier(ageableMob, Attributes.MOVEMENT_SPEED, OLD_AGE_MODIFIER, -0.35d, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, false);
		if (age >= maxAge) {
			if (dieOfOldAge)
				ageableMob.hurt(ageableMob.damageSources().source(OLD_AGE, null), Float.MAX_VALUE);
			return;
		}
		//noinspection DataFlowIssue - isClientSide is checked in caller method
		if ((ageableMob.level().getServer().getTickCount() + ageableMob.getId()) % 100 == 0 || forceUpdateScale)
			setSize(ageableMob, newAge);
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

	@SubscribeEvent
	public void onAnimalFeed(PlayerInteractEvent.EntityInteract event) {
		if (event.isCanceled()
				|| !this.isEnabled()
				|| !(event.getTarget() instanceof Animal animal))
			return;

		poisonAnimal(event, animal);
	}

	public static void poisonAnimal(PlayerInteractEvent.EntityInteract event, Animal animal) {
		if (!stopAgingWithPoisonousPotato
				|| !event.getItemStack().is(Items.POISONOUS_POTATO)
				|| animal.isBaby()
				|| ModNBTData.get(animal, STOP_AGING, Boolean.class))
			return;
		animal.addEffect(new MobEffectInstance(MobEffects.POISON, 80));
		ModNBTData.put(animal, STOP_AGING, true);
		event.getItemStack().shrink(1);
		event.getEntity().swing(event.getHand());
	}

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
}
