package insane96mcp.insanesurvivaloverhaul.module.animals;

import com.mojang.serialization.Codec;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.data.modifier.Modifier;
import insane96mcp.insanesurvivaloverhaul.mixin.accessor.SheepAccessor;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.setup.ISORegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
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
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.Optional;

@LoadFeature(module = ISOModules.ANIMALS, description = "Slower egg laying and wool regrowth, cooldown on milking (and stewing) and changed chance for chickens from eggs. Configurable via livestock data.")
public class AnimalProducts extends Feature {
	public static final TagKey<EntityType<?>> MILKABLE = TagKey.create(Registries.ENTITY_TYPE, InsaneSO.id("milkable"));

	public static final String MILK_COOLDOWN_LANG = InsaneSO.lang("milk_cooldown");

	/**
	 * Ticks left until the entity can be milked (or stewed) again. Mutable so it can be ticked down on both sides without
	 * syncing every time; it's synced only when set and when a player starts tracking the entity.
	 */
	public static final DeferredHolder<AttachmentType<?>, AttachmentType<MutableInt>> FLUID_COOLDOWN =
			ISORegistries.ATTACHMENT_TYPES.register("fluid_cooldown", () -> AttachmentType.builder(() -> new MutableInt(0))
					.serialize(Codec.INT.xmap(MutableInt::new, MutableInt::intValue), cooldown -> cooldown.intValue() > 0)
					.sync(ByteBufCodecs.VAR_INT.map(MutableInt::new, MutableInt::intValue))
					.build());

	@Config(description = "Changes the chance for a chicken to come out from an egg (1 in this value). Vanilla is 8")
	public static Integer chickenFromEggChance = 8;

	@Config(min = 0, description = "Seconds until you can milk cows (or stew mooshrooms)")
	public static Integer fluidCooldown = 1200;

	@SubscribeEvent
	public void onSheepJoinLevel(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
				|| !(event.getEntity() instanceof Sheep sheep))
			return;

		SheepAccessor sheepAccessor = (SheepAccessor) sheep;
		sheep.goalSelector.removeGoal(sheepAccessor.getEatBlockGoal());
		sheepAccessor.setEatBlockGoal(new ISOEatBlock(sheep));
		sheep.goalSelector.addGoal(5, sheepAccessor.getEatBlockGoal());
	}

	@SubscribeEvent
	public void onLivingTick(EntityTickEvent.Pre event) {
		if (!this.isEnabled()
				|| !(event.getEntity() instanceof LivingEntity living))
			return;

		if (!living.level().isClientSide && living instanceof Chicken chicken)
			eggLay(chicken);
		fluidCooldownTick(living);
	}

	public static void eggLay(Chicken chicken) {
		if (chicken.isBaby()
				|| chicken.eggTime < 0)
			return;

		float chanceToSlowDown = Modifier.applyModifiers(0f, LivestockData.getModifiers(chicken, data -> data.eggLayCooldownModifiers), chicken.level(), chicken.blockPosition(), chicken);
		if (chanceToSlowDown <= 1d)
			return;

		double chance = 1d / chanceToSlowDown;
		if (chicken.getRandom().nextFloat() > chance)
			chicken.eggTime++;
	}

	public static boolean canSheepRegrowWool(Mob mob) {
		float chance = Modifier.applyModifiers(1f, LivestockData.getModifiers(mob, data -> data.sheepWoolGrowthChanceModifiers), mob.level(), mob.blockPosition(), mob);
		if (chance >= 1d)
			return true;
		return mob.getRandom().nextFloat() < chance;
	}

	/**
	 * Ticks down the fluid cooldown on both sides. Mutated in place, so it's not synced every second.
	 */
	public static void fluidCooldownTick(LivingEntity living) {
		if (living.tickCount % 20 != 0)
			return;
		MutableInt cooldown = living.getExistingDataOrNull(FLUID_COOLDOWN);
		if (cooldown == null || cooldown.intValue() <= 0)
			return;
		cooldown.setValue(Math.max(0, cooldown.intValue() - 20));
	}

	/**
	 * Returns the ticks left until the entity can be milked (or stewed) again
	 */
	public static int getFluidCooldown(LivingEntity living) {
		MutableInt cooldown = living.getExistingDataOrNull(FLUID_COOLDOWN);
		return cooldown == null ? 0 : cooldown.intValue();
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
		if (getFluidCooldown(living) > 0) {
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
			//DroppedExperience.tryGenerateMilkXp(living);

			float cooldown = Modifier.applyModifiers(fluidCooldown, LivestockData.getModifiers(living, data -> data.cowFluidCooldownModifiers), living.level(), living.blockPosition(), living);
			if (cooldown <= 0)
				return;
			living.setData(FLUID_COOLDOWN, new MutableInt((int) (cooldown * 20)));
		}
	}
}
