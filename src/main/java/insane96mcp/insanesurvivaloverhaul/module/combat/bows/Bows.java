package insane96mcp.insanesurvivaloverhaul.module.combat.bows;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.event.PlayerUseItemMovSpeedEvent;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.setup.ISORegistries;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

@LoadFeature(module = ISOModules.COMBAT, description = "Bows, crossbows and short bows")
public class Bows extends Feature {
	@Config(description = "If true, Arrows from Bows and Crossbows will no longer deal more damage when fully charged.")
	public static Boolean disableCritArrowsBonusDamage = true;
	@Config(description = "If true, arrows will deal decimal instead of being rounded up.")
	public static Boolean decimalDamage = true;
	@Config(min = 0d, max = 10d, description = "Multiplies arrow's damage by this value. (this doesn't affect mobs arrows)")
	public static Double damageMultiplier = 0.70d;
	@Config(min = 0d, max = 10d, description = "Changes bows accuracy. Vanilla is 1.0")
	public static Double bowInaccuracy = 1.0d;
	@Config(min = 0d, max = 10d, description = "Speed at which arrows are shot from crossbows. Vanilla is 3.15")
	public static Double crossbowVelocity = 2.25d;
	@Config(description = "If true, crossbows will have an innate Piercing ability")
	public static Boolean piercingCrossbow = true;
	@Config(min = 0d, max = 10d, description = "Changes crossbows accuracy. Vanilla is 1.0")
	public static Double crossbowInaccuracy = 0.2d;
	@Config(description = "If true, arrows will not trigger invincibility frames, like Combat Test Snapshots")
	public static Boolean noArrowInvincibilityFrames = true;
	//TODO Reduce arrow knockback when bow spamming

	public static final DeferredHolder<Item, ShortbowItem> SHORTBOW = ISORegistries.ITEMS.register("shortbow", () -> new ShortbowItem(new Item.Properties().durability(127)));

	public static float getCrossbowVelocity() {
		if (!Feature.isEnabled(Bows.class))
			return 3.15f;
		return crossbowVelocity.floatValue();
	}

	public static int piercingCrossbows(int piercingCount, ItemStack firedFromWeapon) {
		if (!Feature.isEnabled(Bows.class)
				|| !Bows.piercingCrossbow
				|| !firedFromWeapon.is(Items.CROSSBOW))
			return piercingCount;
		return piercingCount + 1;
	}

	public static float getCrossbowInaccuracy(float original) {
		if (!Feature.isEnabled(Bows.class))
			return original;
		return crossbowInaccuracy.floatValue();
	}

	@SubscribeEvent
	public void onIncomingDamage(LivingIncomingDamageEvent event) {
		if (!this.isEnabled()
				|| !noArrowInvincibilityFrames
				|| !(event.getSource().getDirectEntity() instanceof AbstractArrow))
			return;
		event.setInvulnerabilityTicks(0);
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public void onPlayerUseItemSpeedModifier(PlayerUseItemMovSpeedEvent event) {
		if (!event.getUseItem().is(SHORTBOW.get()))
			return;

		event.setSpeedModifier(0.75f);
	}

}