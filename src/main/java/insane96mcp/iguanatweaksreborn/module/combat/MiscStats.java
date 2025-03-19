package insane96mcp.iguanatweaksreborn.module.combat;

import com.google.common.collect.Multimap;
import insane96mcp.iguanatweaksreborn.InsaneSurvivalOverhaul;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.module.combat.criticalhits.CriticalRework;
import insane96mcp.iguanatweaksreborn.module.experience.enchantments.enchantment.damage.SweepingEdge;
import insane96mcp.iguanatweaksreborn.module.misc.DataPacks;
import insane96mcp.iguanatweaksreborn.setup.ISORegistries;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.effect.AttackDamageMobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Label(name = "Misc Stats")
@LoadFeature(module = Modules.Ids.COMBAT)
public class MiscStats extends Feature {
	public static final RegistryObject<Enchantment> SWEEPING_EDGE = ISORegistries.ENCHANTMENTS.register("sweeping_edge", SweepingEdge::new);

	@Config
	@Label(name = "Fix tooltips", description = "Vanilla tooltips on gear don't sum up multiple modifiers (e.g. a sword would have \"4 Attack Damage\" and \"-2 Attack Damage\" instead of \"2 Attack Damage\". This might break other mods messing with these Tooltips (e.g. Quark's improved tooltips)")
	public static Boolean fixTooltips = true;
	@Config
	@Label(name = "Better strength and weakness", description = "Changes Strength and Weakness +/-3 damage per level to +/-20% damage per level. (Requires a Minecraft restart)")
	public static Boolean betterStrengthWeakness = true;
	@Config
	@Label(name = "Better haste/mining fatigue", description = "Changes Mining fatigue and haste to no longer affects attack speed. (Requires a Minecraft restart)")
	public static Boolean betterHasteMiningFatigue = true;
	@Config
	@Label(name = "Better healing potion", description = "Changes Healing potions to work like pre 1.6.1 by healing 3 health per level")
	public static Boolean betterHealingPotion = true;
	@Config
	@Label(name = "1 damage for tools attacking", description = "If enabled, tools will not take 2 damage when used to hurt entities")
	public static Boolean oneDamageForToolAttacking = true;
	@Config
	@Label(name = "Sweeping overhaul", description = "Rework Sweeping attack. Sweeping is no longer on swords, instead it's on hoes. Also, the sweeping attack deals full damage and the Sweeping Edge enchantment increases the range. This also replaces the vanilla sweeping edge enchantment with a new one that can be applied to hoes instead of swords.")
	public static Boolean sweepingOverhaul = true;

	@Config
	@Label(description = "Enables a data pack that reworks armor, weapons and tools.")
	public static Boolean combatReworkDataPack = true;

	public MiscStats(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
		InsaneSurvivalOverhaul.addServerPack("combat_rework", "Insane's Survival Overhaul Combat Rework", () -> this.isEnabled() && !DataPacks.disableAllDataPacks && combatReworkDataPack);
	}

	@Override
	public void readConfig(ModConfigEvent event) {
		super.readConfig(event);
		if (betterStrengthWeakness) {
			MobEffects.DAMAGE_BOOST.attributeModifiers.remove(Attributes.ATTACK_DAMAGE);
			MobEffects.DAMAGE_BOOST.addAttributeModifier(Attributes.ATTACK_DAMAGE, "648D7064-6A60-4F59-8ABE-C2C23A6DD7A9", 0.0D, AttributeModifier.Operation.MULTIPLY_BASE);
			((AttackDamageMobEffect)MobEffects.DAMAGE_BOOST).multiplier = 0.2d;
		}
		if (betterStrengthWeakness) {
			MobEffects.WEAKNESS.attributeModifiers.remove(Attributes.ATTACK_DAMAGE);
			MobEffects.WEAKNESS.addAttributeModifier(Attributes.ATTACK_DAMAGE, "22653B89-116E-49DC-9B6B-9971489B5BE5", 0.0D, AttributeModifier.Operation.MULTIPLY_BASE);
			((AttackDamageMobEffect)MobEffects.WEAKNESS).multiplier = -0.2d;
		}
		if (betterHasteMiningFatigue) {
			MobEffects.DIG_SPEED.attributeModifiers.remove(Attributes.ATTACK_SPEED);
			MobEffects.DIG_SLOWDOWN.attributeModifiers.remove(Attributes.ATTACK_SPEED);
		}

		if (sweepingOverhaul) {
			ToolActions.DEFAULT_SWORD_ACTIONS.remove(ToolActions.SWORD_SWEEP);
			ToolActions.DEFAULT_HOE_ACTIONS.add(ToolActions.SWORD_SWEEP);
			Enchantments.SWEEPING_EDGE = SWEEPING_EDGE.get();
		}
		else {
            ToolActions.DEFAULT_SWORD_ACTIONS.add(ToolActions.SWORD_SWEEP);
			ToolActions.DEFAULT_HOE_ACTIONS.remove(ToolActions.SWORD_SWEEP);
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onItemTooltipEvent(ItemTooltipEvent event) {
		if (!this.isEnabled()
				|| !fixTooltips
				|| event.getItemStack().getItem() instanceof PotionItem)
			return;

		List<Component> toRemove = new ArrayList<>();
		boolean hasModifiersTooltip = false;

		Component emptyLine = null;
		for (Component mutableComponent : event.getToolTip()) {
			if (emptyLine == null)
				emptyLine = mutableComponent.getSiblings().isEmpty() && mutableComponent.getContents().equals(ComponentContents.EMPTY) ? mutableComponent : null;
			if (mutableComponent.getContents() instanceof TranslatableContents t) {
				if (t.getKey().startsWith("item.modifiers.")) {
					hasModifiersTooltip = true;
					toRemove.add(mutableComponent);
					if (emptyLine != null)
						toRemove.add(emptyLine);
					emptyLine = null;
				}
				else if (t.getKey().startsWith("attribute.modifier."))
					toRemove.add(mutableComponent);
			}

			if (!hasModifiersTooltip)
				continue;

			List<Component> siblings = mutableComponent.getSiblings();
			for (Component component : siblings) {
				if (component.getContents() instanceof TranslatableContents translatableContents && translatableContents.getKey().startsWith("attribute.modifier.")) {
					toRemove.add(mutableComponent);
				}
			}
		}

		toRemove.forEach(component -> event.getToolTip().remove(component));

		for(EquipmentSlot equipmentslot : EquipmentSlot.values()) {
			Multimap<Attribute, AttributeModifier> multimap = event.getItemStack().getAttributeModifiers(equipmentslot);
			if (!multimap.isEmpty()) {
				event.getToolTip().add(CommonComponents.EMPTY);
				event.getToolTip().add(Component.translatable("item.modifiers." + equipmentslot.getName()).withStyle(ChatFormatting.GRAY));
				multimap.keySet().stream().sorted(Comparator.comparing(attr -> ForgeRegistries.ATTRIBUTES.getKey(attr).getPath())).forEach(
					attribute -> {
						Map<AttributeModifier.Operation, List<AttributeModifier>> modifiersByOperation = multimap.get(attribute).stream().collect(Collectors.groupingBy(AttributeModifier::getOperation));
						modifiersByOperation.forEach((operation, modifier) -> {
							double amount = modifier.stream().mapToDouble(AttributeModifier::getAmount).sum();

							boolean isEqualTooltip = false;
							if (event.getEntity() != null && operation == AttributeModifier.Operation.ADDITION && equipmentslot == EquipmentSlot.MAINHAND) {
								if (attribute.equals(Attributes.ATTACK_DAMAGE)) {
									amount += event.getEntity().getAttributeBaseValue(Attributes.ATTACK_DAMAGE);
									//amount += EnchantmentHelper.getDamageBonus(event.getItemStack(), MobType.UNDEFINED);
									isEqualTooltip = true;
								}
								if (attribute.equals(PiercingDamage.PIERCING_DAMAGE.get())
										|| attribute.equals(Attributes.ATTACK_SPEED)
										|| attribute.equals(Attributes.KNOCKBACK_RESISTANCE)
										|| attribute.equals(ForgeMod.ENTITY_REACH.get())
										|| attribute.equals(ForgeMod.BLOCK_REACH.get())
										|| attribute.equals(CriticalRework.CHANCE_ATTRIBUTE.get())
										|| attribute.equals(CriticalRework.DAMAGE_ATTRIBUTE.get())) {
									amount += event.getEntity().getAttributeBaseValue(attribute);
									isEqualTooltip = true;
									if (attribute.equals(CriticalRework.DAMAGE_ATTRIBUTE.get()))
										amount += 1;
								}
							}
							if (!isEqualTooltip && amount == 0d)
								return;

							MutableComponent component = null;
							String translationString = "attribute.modifier.plus.";
							if (isEqualTooltip || operation == AttributeModifier.Operation.MULTIPLY_TOTAL)
								translationString = "attribute.modifier.equals.";
							else if (amount < 0)
								translationString = "attribute.modifier.take.";
							final MutableComponent attributeComponent = Component.translatable(attribute.getDescriptionId());
							switch (operation) {
								case ADDITION -> {
									if (attribute.equals(Attributes.KNOCKBACK_RESISTANCE) || attribute.equals(CriticalRework.CHANCE_ATTRIBUTE.get()) || attribute.equals(CriticalRework.DAMAGE_ATTRIBUTE.get()))
										component = Component.translatable(translationString + operation.toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(amount * 100) + "%", attributeComponent);
									else
										component = Component.translatable(translationString + operation.toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(Math.abs(amount)), attributeComponent);
								}
								case MULTIPLY_BASE -> component = Component.translatable(translationString + operation.toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(Math.abs(amount) * 100), attributeComponent);
								case MULTIPLY_TOTAL -> component = Component.translatable(translationString + operation.toValue(), "x" + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(amount > 0 ? Math.abs(amount) + 1f : 1f - Math.abs(amount)), attributeComponent);
							}
							if (isEqualTooltip)
								component = CommonComponents.space().append(component.withStyle(ChatFormatting.DARK_GREEN));
							else if (amount > 0)
								component.withStyle(ChatFormatting.BLUE);
							else
								component.withStyle(ChatFormatting.RED);
							event.getToolTip().add(component);
						});
					}
				);
			}
		}
	}

}