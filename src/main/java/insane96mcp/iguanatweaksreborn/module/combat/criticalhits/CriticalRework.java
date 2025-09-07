package insane96mcp.iguanatweaksreborn.module.combat.criticalhits;

import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.setup.ISORegistries;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.RegistryObject;

@LoadFeature(module = Modules.Ids.COMBAT, description = "Rework critical hits to be a chance to happen instead of damage on jump. Also the chance and bonus damage are now an attribute. By default critical_chance is 0 and can increase with the Critical enchantment and critical_damage is 0.5 (+50%).")
public class CriticalRework extends Feature {
	public static final RegistryObject<Attribute> CHANCE_ATTRIBUTE = ISORegistries.ATTRIBUTES.register("critical_chance", () -> new RangedAttribute("attribute.name.critical_chance", 0d, 0d, 1d));
	public static final RegistryObject<Attribute> DAMAGE_ATTRIBUTE = ISORegistries.ATTRIBUTES.register("critical_damage", () -> new RangedAttribute("attribute.name.critical_damage", 0.5d, 0d, Double.MAX_VALUE));

	public static final RegistryObject<Enchantment> CRITICAL_ENCHANTMENT = ISORegistries.ENCHANTMENTS.register("critical", CriticalEnchantment::new);

	@Config(min = -1d, max = 1d, description = "iguanatweaksreborn:critical_chance increase per level of Critical enchantment.")
	public static Double enchantmentChance = 0.1d;
	@Config(min = -1d, max = 1d, description = "iguanatweaksreborn:critical_damage increase per level of Critical enchantment.")
	public static Double enchantmentBonusDamage = 0.1d;

	public static void addAttribute(EntityAttributeModificationEvent event) {
		for (EntityType<? extends LivingEntity> entityType : event.getTypes()) {
			if (!event.has(entityType, CHANCE_ATTRIBUTE.get()))
				event.add(entityType, CHANCE_ATTRIBUTE.get());
			if (!event.has(entityType, DAMAGE_ATTRIBUTE.get()))
				event.add(entityType, DAMAGE_ATTRIBUTE.get());
		}
	}

	@SubscribeEvent
	public void onCriticalHit(CriticalHitEvent event) {
		if (!this.isEnabled())
			return;
		double chance = event.getEntity().getAttributeValue(CHANCE_ATTRIBUTE.get());
		if (chance >= 0) {
			event.setResult(Event.Result.DENY);
			if (event.getEntity().getRandom().nextFloat() < chance) {
				event.setDamageModifier((float) (event.getEntity().getAttributeValue(DAMAGE_ATTRIBUTE.get()) + 1f));
				event.setResult(Event.Result.ALLOW);
			}
		}
	}
}