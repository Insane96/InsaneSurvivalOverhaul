package insane96mcp.insanesurvivaloverhaul.module.items.dagger;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.neoforge.common.ItemAbility;

public class DaggerItem extends SwordItem {
	public DaggerItem(Tier tier, Item.Properties properties) {
		super(tier, properties);
	}

	/**
	 * Daggers never sweep, regardless of SweepOverhaul's sweepOnSwords toggle: SwordItem#canPerformAction
	 * checks the shared static ItemAbilities.DEFAULT_SWORD_ACTIONS set, which that feature mutates, so
	 * inheriting it unchanged would make dagger sweep follow sword's config instead of being independently off.
	 */
	@Override
	public boolean canPerformAction(ItemStack stack, ItemAbility itemAbility) {
		return false;
	}

	/**
	 * Damage scales at half the rate of a vanilla sword's tier bonus (0.5 per point instead of 1), so gold
	 * lands equal to wood and each subsequent tier step is 0.5 instead of 1, mirroring vanilla's own shape.
	 */
	public static ItemAttributeModifiers createDaggerAttributes(Tier tier, float attackSpeed, float entityReachAdd) {
		float damage = 1.5F + 0.5F * tier.getAttackDamageBonus();
		return ItemAttributeModifiers.builder()
				.add(
						Attributes.ATTACK_DAMAGE,
						new AttributeModifier(BASE_ATTACK_DAMAGE_ID, damage, AttributeModifier.Operation.ADD_VALUE),
						EquipmentSlotGroup.MAINHAND
				)
				.add(
						Attributes.ATTACK_SPEED,
						new AttributeModifier(BASE_ATTACK_SPEED_ID, attackSpeed, AttributeModifier.Operation.ADD_VALUE),
						EquipmentSlotGroup.MAINHAND
				)
				.add(
						Attributes.ENTITY_INTERACTION_RANGE,
						new AttributeModifier(ResourceLocation.withDefaultNamespace("base_entity_reach"), entityReachAdd, AttributeModifier.Operation.ADD_VALUE),
						EquipmentSlotGroup.MAINHAND
				)
				.build();
	}
}
