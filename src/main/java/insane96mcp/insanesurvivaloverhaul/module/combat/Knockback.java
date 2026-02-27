package insane96mcp.insanesurvivaloverhaul.module.combat;

import insane96mcp.insanelib.core.ModNBTData;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.mixin.accessor.LivingEntityAccessor;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;

@LoadFeature(module = ISOModules.COMBAT, description = "Players will deal reduced knockback if attacking with a non-weapon or spamming. Knockback reductions are defined via Data Packs with Item Definitions (the item_stats integrated data pack already does this)")
public class Knockback extends Feature {

	public ResourceLocation TIME_SINCE_LAST_SWING;
	public ResourceLocation SHOULD_APPLY_NO_KNOCKBACK;
	public ResourceLocation PROJECTILE_KNOCKBACK;

	@Config(min = 0d, max = 1d, description = "Percentage knockback dealt if the player is using an item that doesn't have the attack damage attribute. Broken items from the Items module count as No Weapon")
	public static Double noWeaponPenalty = 0.35d;
	@Config(min = 0d, max = 1d, description = "Percentage knockback dealt if the player is attacking when the attack is not fully charged.")
	public static Double spamPenalty = 0.35d;
	@Config(min = 0d, max = 1d, description = "Percentage knockback dealt by projectiles.")
	public static Double projectileKnockback = 0.7d;

	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		TIME_SINCE_LAST_SWING = this.createDataKey("ticks_since_last_swing");
		SHOULD_APPLY_NO_KNOCKBACK = this.createDataKey("should_apply_no_knockback");
		PROJECTILE_KNOCKBACK = this.createDataKey("projectile_knockback");
	}

	@SubscribeEvent
	public void onPlayerAttackEvent(AttackEntityEvent event) {
		if (!this.isEnabled())
			return;
		Player player = event.getEntity();
		if (player.getAbilities().instabuild)
			return;
		ModNBTData.put(player, TIME_SINCE_LAST_SWING, ((LivingEntityAccessor) player).getAttackStrengthTicker());
	}

	@SubscribeEvent
	public void onLivingHurtEvent(LivingDamageEvent.Post event) {
		if (!this.isEnabled())
			return;
		if (event.getSource().getDirectEntity() instanceof Player player)
			ModNBTData.put(player, SHOULD_APPLY_NO_KNOCKBACK, true);
		else if (event.getSource().getDirectEntity() instanceof Projectile projectile && projectile.getOwner() != null && projectileKnockback < 1d)
			ModNBTData.put(projectile.getOwner(), PROJECTILE_KNOCKBACK, true);
	}

	// Run after InsaneLib's knockback_multiplier data component
	@SubscribeEvent(priority = EventPriority.LOW)
	public void onKnockback(LivingKnockBackEvent event) {
		if (!this.isEnabled())
			return;

		if (event.getEntity().getLastHurtByMobTimestamp() != event.getEntity().tickCount)
			return;

		LivingEntity lastHurtByMob = event.getEntity().getLastHurtByMob();
		if (lastHurtByMob == null)
			return;

		if (lastHurtByMob instanceof ServerPlayer player && !player.gameMode.isSurvival())
			return;

		ItemStack itemStack = lastHurtByMob.getMainHandItem();

		float knockbackMultiplier = 1f;
		if (lastHurtByMob instanceof Player player && ModNBTData.get(lastHurtByMob, SHOULD_APPLY_NO_KNOCKBACK, Boolean.class)) {
			if ((itemStack.getAttributeModifiers().modifiers().stream().noneMatch(e -> (e.slot() == EquipmentSlotGroup.MAINHAND || e.slot() == EquipmentSlotGroup.HAND) && e.attribute() == Attributes.ATTACK_DAMAGE)
					/*|| (isEnabled(UnbreakableItems.class) && Feature.isEnabled(UnbreakableItems.class) && UnbreakableItems.isBroken(itemStack))*/)
					&& noWeaponPenalty < 1d)
				knockbackMultiplier = Math.min(knockbackMultiplier, noWeaponPenalty.floatValue());

			int ticksSinceLastSwing = ModNBTData.get(player, TIME_SINCE_LAST_SWING, Integer.class);
			float cooldown = Mth.clamp((ticksSinceLastSwing + 0.5f) / player.getCurrentItemAttackStrengthDelay(), 0.0F, 1.0F);
			if (cooldown <= 0.9f)
				knockbackMultiplier = Math.min(knockbackMultiplier, spamPenalty.floatValue());
		}
		if (ModNBTData.get(lastHurtByMob, PROJECTILE_KNOCKBACK, Boolean.class)) {
			knockbackMultiplier = Math.min(knockbackMultiplier, projectileKnockback.floatValue());
			ModNBTData.remove(lastHurtByMob, PROJECTILE_KNOCKBACK);
		}

		if (knockbackMultiplier < 1f) {
			if (lastHurtByMob.isSprinting() && lastHurtByMob instanceof Player)
				event.setStrength(event.getStrength() - 0.5f);
			event.setStrength(event.getStrength() * knockbackMultiplier);
		}
		ModNBTData.put(lastHurtByMob, SHOULD_APPLY_NO_KNOCKBACK, false);
	}
}