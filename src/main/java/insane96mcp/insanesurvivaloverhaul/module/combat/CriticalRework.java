package insane96mcp.insanesurvivaloverhaul.module.combat;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.setup.ISORegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

@LoadFeature(module = ISOModules.COMBAT, description = "Rework critical hits to be a chance to happen instead of damage on jump. Also the chance and bonus damage are now an attribute. By default critical_chance is 0 and can increase with the Critical enchantment and critical_damage is 0.5 (+50%).")
public class CriticalRework extends Feature {
	public static final DeferredHolder<Attribute, Attribute> CHANCE_ATTRIBUTE = ISORegistries.ATTRIBUTES.register("critical_chance", () -> new RangedAttribute("attribute.name.critical_chance", 0d, 0d, 1d));
	public static final DeferredHolder<Attribute, Attribute> DAMAGE_ATTRIBUTE = ISORegistries.ATTRIBUTES.register("critical_damage", () -> new RangedAttribute("attribute.name.critical_damage", 0d, 0d, Double.MAX_VALUE));

	//public static final DeferredHolder<Enchantment> CRITICAL_ENCHANTMENT = ISORegistries.ENCHANTMENTS.register("critical", CriticalEnchantment::new);

	//@Config(min = -1d, max = 1d, description = "iguanatweaksreborn:critical_chance increase per level of Critical enchantment.")
	//public static Double enchantmentChance = 0.1d;
	//@Config(min = -1d, max = 1d, description = "iguanatweaksreborn:critical_damage increase per level of Critical enchantment.")
	//public static Double enchantmentBonusDamage = 0.1d;

	public static void addAttribute(EntityAttributeModificationEvent event) {
		for (EntityType<? extends LivingEntity> entityType : event.getTypes()) {
			if (!event.has(entityType, CHANCE_ATTRIBUTE))
				event.add(entityType, CHANCE_ATTRIBUTE);
			if (!event.has(entityType, DAMAGE_ATTRIBUTE))
				event.add(entityType, DAMAGE_ATTRIBUTE);
		}
	}

	@SubscribeEvent
	public void onCriticalHit(CriticalHitEvent event) {
		if (!this.isEnabled())
			return;
		double chance = event.getEntity().getAttributeValue(CHANCE_ATTRIBUTE);
		event.setCriticalHit(false);
		if (chance >= 0) {
			if (event.getEntity().getRandom().nextFloat() < chance) {
				event.setDamageMultiplier((float) (event.getEntity().getAttributeValue(DAMAGE_ATTRIBUTE) + 1f));
				event.setCriticalHit(true);
			}
		}
	}
}