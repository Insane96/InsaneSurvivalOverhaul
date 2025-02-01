package insane96mcp.iguanatweaksreborn.module.combat.bows;

import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.setup.ISORegistries;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.event.PlayerUseItemSpeedModifierEvent;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.RegistryObject;

@Label(name = "Bows", description = "Bows, crossbows and short bows")
@LoadFeature(module = Modules.Ids.COMBAT)
public class Bows extends Feature {
	@Config
	@Label(name = "Disable Critical Arrows bonus damage", description = "If true, Arrows from Bows and Crossbows will no longer deal more damage when fully charged.")
	public static Boolean disableCritArrowsBonusDamage = true;
	@Config
	@Label(name = "Float point damage", description = "If true, arrows will deal float damage instead of being rounded up.")
	public static Boolean floatPointDamage = true;
	@Config(min = 0d, max = 10d)
	@Label(name = "Damage Multiplier", description = "Multiplies arrow's damage by this value. (this doesn't affect mobs arrows)")
	public static Double damageMultiplier = 0.65d;
	@Config(min = 0d, max = 10d)
	@Label(name = "Bow Inaccuracy", description = "Changes bows accuracy. Vanilla is 1.0")
	public static Double bowInaccuracy = 1.0d;
	@Config(min = 0d, max = 10d)
	@Label(name = "Crossbow velocity", description = "Speed at which arrows are shot from crossbows. Vanilla is 3.15")
	public static Double crossbowVelocity = 2.25d;
	@Config
	@Label(name = "Piercing Crossbow", description = "If true, crossbows will have an innate Piercing ability")
	public static Boolean piercingCrossbow = true;
	@Config(min = 0d, max = 10d)
	@Label(name = "Crossbow Inaccuracy", description = "Changes crossbows accuracy. Vanilla is 1.0")
	public static Double crossbowInaccuracy = 0.2d;

	public static final RegistryObject<Item> SHORTBOW = ISORegistries.ITEMS.register("shortbow", () -> new ShortbowItem(new Item.Properties().durability(127)));

	public Bows(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	public static float getCrossbowVelocity() {
		if (!Feature.isEnabled(Bows.class))
			return 3.15f;
		return crossbowVelocity.floatValue();
	}

	public static void piercingCrossbows(AbstractArrow abstractArrow) {
		if (!Feature.isEnabled(Bows.class) ||
				!Bows.piercingCrossbow)
			return;
		abstractArrow.setPierceLevel((byte) (abstractArrow.getPierceLevel() + 1));
	}

	public static float getCrossbowInaccuracy(float original) {
		if (!Feature.isEnabled(Bows.class))
			return original;
		return crossbowInaccuracy.floatValue();
	}

	@SubscribeEvent
	public void onPlayerUseItemSpeedModifier(PlayerUseItemSpeedModifierEvent event) {
		if (!event.getUseItem().is(SHORTBOW.get()))
			return;

		event.setSpeedModifier(0.75f);
	}

}