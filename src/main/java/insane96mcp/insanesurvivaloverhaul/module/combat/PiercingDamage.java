package insane96mcp.insanesurvivaloverhaul.module.combat;

import insane96mcp.insanelib.core.ModNBTData;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.data.generator.ISODamageTypeTagsProvider;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.module.items.UnvanishableItems;
import insane96mcp.insanesurvivaloverhaul.setup.ISORegistries;
import insane96mcp.insanesurvivaloverhaul.util.MCUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.Nullable;

@LoadFeature(module = ISOModules.COMBAT, description = "Adds a new attribute that deals bonus damage that bypasses armor")
public class PiercingDamage extends Feature {
	public static ResourceKey<DamageType> PIERCING_MOB_ATTACK = ResourceKey.create(Registries.DAMAGE_TYPE, InsaneSO.id("piercing_mob_attack"));
	public static ResourceKey<DamageType> PIERCING_PLAYER_ATTACK = ResourceKey.create(Registries.DAMAGE_TYPE, InsaneSO.id("piercing_player_attack"));

	public static final DeferredHolder<Attribute, PiercingDamageAttribute> PIERCING_DAMAGE = ISORegistries.ATTRIBUTES.register("piercing_damage", () -> new PiercingDamageAttribute("attribute.name.piercing_damage", 0d, 0d, 1024d));
	/**
	 * ID of the base modifier for Piercing Damage.
	 */
	public static final ResourceLocation BASE_PIERCING_DAMAGE_ID = InsaneSO.id("base_piercing_damage");

	public static final TagKey<DamageType> PIERCING_DAMAGE_TYPE = ISODamageTypeTagsProvider.create("piercing_damage_type");
	public static final TagKey<DamageType> DOESNT_TRIGGER_PIERCING = ISODamageTypeTagsProvider.create("doesnt_trigger_piercing");

	public static ResourceLocation SHOULD_STOP_HURT;

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		SHOULD_STOP_HURT = this.createDataKey("should_stop_hurt");
	}

	public static void addAttribute(EntityAttributeModificationEvent event) {
		for (EntityType<? extends LivingEntity> entityType : event.getTypes()) {
			if (event.has(entityType, PIERCING_DAMAGE))
				continue;

			event.add(entityType, PIERCING_DAMAGE);
		}
	}

	//Run before Regenerating Absorption
	@SuppressWarnings("DataFlowIssue")
	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onEntityDamaged(LivingDamageEvent.Post event) {
		if (!this.isEnabled()
				|| !(event.getSource().getDirectEntity() instanceof LivingEntity attacker)
				|| event.getEntity().isDeadOrDying()
				|| event.getSource().is(DOESNT_TRIGGER_PIERCING)
				|| attacker.getAttribute(PIERCING_DAMAGE) == null
				|| (Feature.isEnabled(UnvanishableItems.class) && UnvanishableItems.isBroken(attacker.getMainHandItem())))
			return;

		AttributeInstance piercingInstance = attacker.getAttribute(PIERCING_DAMAGE);
		float amount = (float) piercingInstance.getValue();
		if (amount <= 0d)
			return;
		DamageSource piercingDamageSource = attacker.damageSources().source(PIERCING_MOB_ATTACK, attacker);
		if (attacker instanceof Player player) {
			piercingDamageSource = attacker.damageSources().source(PIERCING_PLAYER_ATTACK, attacker);
			float f = player.getAttackStrengthScale(0.5F);
			amount *= f * f;
		}

		boolean ret = MCUtils.attackEntityIgnoreInvFrames(piercingDamageSource, amount, event.getEntity(), event.getEntity(), true);
		if (event.getEntity().isDeadOrDying())
			ModNBTData.put(event.getEntity(), SHOULD_STOP_HURT, ret);
	}

	public static class PiercingDamageAttribute extends RangedAttribute {
		public PiercingDamageAttribute(String descriptionId, double defaultValue, double min, double max) {
			super(descriptionId, defaultValue, min, max);
		}

		@Override
		public @Nullable ResourceLocation getBaseId() {
			return BASE_PIERCING_DAMAGE_ID;
		}
	}
}