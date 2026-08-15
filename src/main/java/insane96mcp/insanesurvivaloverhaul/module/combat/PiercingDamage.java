package insane96mcp.insanesurvivaloverhaul.module.combat;

import insane96mcp.insanelib.core.ModNBTData;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.util.ILRangedAttribute;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.data.generator.ISODamageTypeTagsProvider;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.module.items.UnvanishableItems;
import insane96mcp.insanesurvivaloverhaul.module.misc.Packs;
import insane96mcp.insanesurvivaloverhaul.setup.ISORegistries;
import insane96mcp.insanesurvivaloverhaul.util.MCUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

@LoadFeature(module = ISOModules.COMBAT, description = "Adds a new attribute that deals bonus damage that bypasses armor")
public class PiercingDamage extends Feature {
	public static ResourceKey<DamageType> PIERCING_MOB_ATTACK = ResourceKey.create(Registries.DAMAGE_TYPE, InsaneSO.id("piercing_mob_attack"));
	public static ResourceKey<DamageType> PIERCING_PLAYER_ATTACK = ResourceKey.create(Registries.DAMAGE_TYPE, InsaneSO.id("piercing_player_attack"));

	public static final ResourceKey<Enchantment> ARMOR_PIERCER = ResourceKey.create(Registries.ENCHANTMENT, InsaneSO.id("armor_piercer"));

	/**
	 * ID of the base modifier (green) for Piercing Damage.
	 */
	public static final ResourceLocation BASE_PIERCING_DAMAGE_ID = InsaneSO.id("base_piercing_damage");
	public static final DeferredHolder<Attribute, Attribute> PIERCING_DAMAGE = ISORegistries.ATTRIBUTES.register("piercing_damage", () -> new ILRangedAttribute(0d, 0d, 1024d, BASE_PIERCING_DAMAGE_ID));

	public static final TagKey<DamageType> PIERCING_DAMAGE_TYPE = ISODamageTypeTagsProvider.create("piercing_damage_type");
	public static final TagKey<DamageType> DOESNT_TRIGGER_PIERCING = ISODamageTypeTagsProvider.create("doesnt_trigger_piercing");

	public static ResourceLocation SHOULD_STOP_HURT;

	@Config(description = "Enables a data pack that adds the Armor Piercer enchantment (max level V). Each level grants bonus Piercing Damage scaled by the weapon's Attack Damage (see armorPiercerDamagePerAttackDamage). It's incompatible with other damage enchantments (Sharpness, Smite, Bane of Arthropods, Impaling, Density, Breach, Critical).")
	public static Boolean armorPiercerEnchantmentDataPack = true;

	@Config(min = 0d, description = "Bonus Piercing Damage granted by the Armor Piercer enchantment, per level, per point of the weapon's Attack Damage. Default is 0.25 every 5 points of Attack Damage (0.05 per point), so weaker/faster weapons (e.g. hoes) get less bonus than a sword or axe.")
	public static Double armorPiercerDamagePerAttackDamage = 0.05d;

	@Config(description = "If Rune Enchanting is installed, enables the Armor Piercer rune.")
	public static Boolean armorPiercerRuneEnabled = true;
	@Config(description = "Bonus piercing damage granted by the Armor Piercer rune.")
	public static Double armorPiercerRuneBonusDamage = 0.3d;

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		SHOULD_STOP_HURT = this.createDataKey("should_stop_hurt");
		InsaneSO.addServerPack("armor_piercer_enchantment", "Insane's Survival Overhaul Armor Piercer Enchantment", () -> this.isEnabled() && !Packs.disableAllDataPacks && armorPiercerEnchantmentDataPack);
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

	@SubscribeEvent
	public void onItemAttributeModifier(ItemAttributeModifierEvent event) {
		if (!this.isEnabled())
			return;
		int level = getArmorPiercerLevel(event.getItemStack());
		if (level <= 0)
			return;
		float attackDamage = 0f;
		for (ItemAttributeModifiers.Entry entry : event.getModifiers()) {
			if (entry.attribute().is(Attributes.ATTACK_DAMAGE) && entry.modifier().operation() == AttributeModifier.Operation.ADD_VALUE)
				attackDamage += (float) entry.modifier().amount();
		}
		float bonus = level * attackDamage * armorPiercerDamagePerAttackDamage.floatValue();
		if (bonus <= 0f)
			return;
		event.addModifier(PIERCING_DAMAGE, new AttributeModifier(InsaneSO.id("armor_piercer_enchantment"), bonus, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
	}

	private static int getArmorPiercerLevel(ItemStack stack) {
		ItemEnchantments enchantments = stack.get(DataComponents.ENCHANTMENTS);
		if (enchantments == null)
			return 0;
		for (Holder<Enchantment> holder : enchantments.keySet()) {
			if (holder.unwrapKey().filter(key -> key.equals(ARMOR_PIERCER)).isPresent())
				return enchantments.getLevel(holder);
		}
		return 0;
	}
}