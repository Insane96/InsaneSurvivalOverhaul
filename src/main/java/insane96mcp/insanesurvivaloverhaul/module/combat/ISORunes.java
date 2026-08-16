package insane96mcp.insanesurvivaloverhaul.module.combat;

import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.runeenchanting.runes.Rune;
import insane96mcp.runeenchanting.setup.RERunes;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IAttributeExtension;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import javax.annotation.Nullable;

/**
 * Registers the Critical and Armor Piercer runes into Rune Enchanting's rune registry.
 * Only ever constructed if Rune Enchanting is installed (see InsaneSO), so this class - and
 * therefore its references to Rune Enchanting's classes - is never loaded otherwise.
 */
public class ISORunes {
	public ISORunes(IEventBus modEventBus) {
		modEventBus.addListener(this::onRegister);
	}

	private void onRegister(RegisterEvent event) {
		event.register(RERunes.REGISTRY_KEY, helper -> {
			helper.register(InsaneSO.id("critical"), new CriticalRune());
			helper.register(InsaneSO.id("armor_piercer"), new ArmorPiercerRune());
		});
	}

	public static class CriticalRune extends Rune {
		@Override
		public String getName() {
			return "Critical";
		}

		@Override
		public String getDescription() {
			return "Increases critical chance and critical damage";
		}

		@Override
		public @Nullable String getInfo() {
			return "Critical chance: +%s%%, Critical damage: +%s%%";
		}

		@Override
		public MutableComponent getInfoComponent() {
			return Component.translatable(getInfoTranslationKey(),
					IAttributeExtension.FORMAT.format(CriticalRework.criticalRuneBonusChance * CriticalRework.rune$enchantmentLevelEquivalent * 100),
					IAttributeExtension.FORMAT.format(CriticalRework.criticalRuneBonusDamage * CriticalRework.rune$enchantmentLevelEquivalent * 100));
		}

		@Override
		public void addItemsToApplicableTag(IntrinsicHolderTagsProvider.IntrinsicTagAppender<Item> appender) {}

		@Override
		public void addAttributeModifiers(ItemAttributeModifierEvent event) {
			if (!CriticalRework.rune$enabled)
				return;
			event.addModifier(CriticalRework.CHANCE_ATTRIBUTE, new AttributeModifier(InsaneSO.id("rune_critical_chance"), CriticalRework.criticalRuneBonusChance * CriticalRework.rune$enchantmentLevelEquivalent, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
			event.addModifier(CriticalRework.DAMAGE_ATTRIBUTE, new AttributeModifier(InsaneSO.id("rune_critical_damage"), CriticalRework.criticalRuneBonusDamage * CriticalRework.rune$enchantmentLevelEquivalent, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
		}
	}

	public static class ArmorPiercerRune extends Rune {
		@Override
		public String getName() {
			return "Armor Piercer";
		}

		@Override
		public String getDescription() {
			return "Increases piercing damage";
		}

		@Override
		public @Nullable String getInfo() {
			return "Piercing damage: +%s per point of Attack Damage";
		}

		@Override
		public MutableComponent getInfoComponent() {
			return Component.translatable(getInfoTranslationKey(),
					IAttributeExtension.FORMAT.format(PiercingDamage.armorPiercerDamagePerAttackDamage * PiercingDamage.rune$enchantmentLevelEquivalent));
		}

		@Override
		public void addItemsToApplicableTag(IntrinsicHolderTagsProvider.IntrinsicTagAppender<Item> appender) {}

		@Override
		public void addAttributeModifiers(ItemAttributeModifierEvent event) {
			if (!PiercingDamage.rune$enabled)
				return;
			float bonus = PiercingDamage.getPiercingDamageBonus(event, PiercingDamage.rune$enchantmentLevelEquivalent);
			if (bonus <= 0f)
				return;
			event.addModifier(PiercingDamage.PIERCING_DAMAGE, new AttributeModifier(InsaneSO.id("rune_armor_piercer"), bonus, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
		}
	}
}
