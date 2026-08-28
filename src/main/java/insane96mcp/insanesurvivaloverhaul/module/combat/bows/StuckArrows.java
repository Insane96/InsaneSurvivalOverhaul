package insane96mcp.insanesurvivaloverhaul.module.combat.bows;

import insane96mcp.insanelib.core.ModNBTData;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.mixin.accessor.AbstractArrowAccessor;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

@LoadFeature(module = ISOModules.COMBAT,
		description = "Mobs hit by pickupable arrows remember them, and drop a percentage of those arrows on death.")
public class StuckArrows extends Feature {
	public static ResourceLocation STUCK_ARROWS;

	@Config(min = 0d, max = 1d, description = "Chance, for each arrow stuck in the mob, to be dropped on death.")
	public static Double dropAmount = 0.5d;

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		STUCK_ARROWS = this.createDataKey("stuck_arrows");
	}

	@SubscribeEvent
	public void onDamage(LivingDamageEvent.Post event) {
		if (!this.isEnabled()
				|| event.getEntity().level().isClientSide
				|| !(event.getEntity() instanceof Mob mob)
				|| !(event.getSource().getDirectEntity() instanceof AbstractArrow arrow))
			return;

		AbstractArrowAccessor accessor = (AbstractArrowAccessor) arrow;
		if (accessor.getPickup() != AbstractArrow.Pickup.ALLOWED)
			return;

		if (accessor.invokeGetPickupItem().getItem() != Items.ARROW)
			return;

		ListTag stuckArrows = ModNBTData.getList(mob, STUCK_ARROWS, Tag.TAG_COMPOUND);
		stuckArrows.add(accessor.invokeGetPickupItem().save(mob.level().registryAccess(), new CompoundTag()));
		ModNBTData.put(mob, STUCK_ARROWS, stuckArrows);
	}

	@SubscribeEvent
	public void onEntityDeath(LivingDeathEvent event) {
		if (!this.isEnabled()
				|| !(event.getEntity() instanceof Mob mob)
				|| mob.level().isClientSide
				|| !ModNBTData.contains(mob, STUCK_ARROWS))
			return;

		ListTag stuckArrows = ModNBTData.getList(mob, STUCK_ARROWS, Tag.TAG_COMPOUND);
		if (stuckArrows.isEmpty())
			return;

		Level level = mob.level();
		float chance = dropAmount.floatValue() / 100f;
		for (Tag arrowTag : stuckArrows) {
			if (mob.getRandom().nextFloat() >= chance)
				continue;
			ItemStack stack = ItemStack.parse(level.registryAccess(), arrowTag).orElse(ItemStack.EMPTY);
			if (stack.isEmpty())
				continue;
			ItemEntity item = new ItemEntity(level, mob.getX(), mob.getY(), mob.getZ(), stack);
			item.setDefaultPickUpDelay();
			level.addFreshEntity(item);
		}
	}
}
