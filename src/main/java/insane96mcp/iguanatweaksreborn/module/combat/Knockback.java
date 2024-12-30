package insane96mcp.iguanatweaksreborn.module.combat;

import com.google.common.collect.Multimap;
import insane96mcp.iguanatweaksreborn.IguanaTweaksReborn;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.module.items.itemstats.ItemStats;
import insane96mcp.insanelib.base.JsonFeature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.data.IdTagValue;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

@Label(name = "Knockback", description = "Players will deal reduced knockback if attacking with a non-weapon or spamming.")
@LoadFeature(module = Modules.Ids.COMBAT)
public class Knockback extends JsonFeature {

	public static final String TIME_SINCE_LAST_SWING = IguanaTweaksReborn.RESOURCE_PREFIX + "ticks_since_last_swing";
	public static final String SHOULD_APPLY_NO_KNOCKBACK = IguanaTweaksReborn.RESOURCE_PREFIX + "should_apply_no_knockback";

	@Config(min = 0d, max = 1d)
	@Label(name = "No Weapon Penalty", description = "Percentage knockback dealt if the player is using an item that doesn't have the attack damage attribute. Broken items from the Items module count as No Weapon")
	public static Double noWeaponPenalty = 0.35d;
	@Config(min = 0d, max = 1d)
	@Label(name = "Spam Penalty", description = "Percentage knockback dealt if the player is attacking when the attack is not fully charged.")
	public static Double spamPenalty = 0.35d;

	//Knockback multipliers for items
	public static final ArrayList<IdTagValue> KNOCKBACK_MULTIPLIERS_DEFAULT = new ArrayList<>(List.of(
			IdTagValue.newTag("minecraft:pickaxes", 0.85d),
			IdTagValue.newTag("minecraft:axes", 0.9d),
			IdTagValue.newTag("minecraft:swords", 0.85d),
			IdTagValue.newTag("minecraft:hoes", 0.35d),
			IdTagValue.newTag("iguanatweaksexpanded:forge_hammers", 1d)
	));
	public static final ArrayList<IdTagValue> knockbackMultipliers = new ArrayList<>();

	public Knockback(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
		JSON_CONFIGS.add(new JsonConfig<>("knockback_multipliers.json", knockbackMultipliers, KNOCKBACK_MULTIPLIERS_DEFAULT, IdTagValue.LIST_TYPE));
	}

	@Override
	public String getModConfigFolder() {
		return IguanaTweaksReborn.CONFIG_FOLDER;
	}

	@SubscribeEvent
	public void onPlayerAttackEvent(AttackEntityEvent event) {
		if (!this.isEnabled())
			return;
		Player player = event.getEntity();
		if (player.getAbilities().instabuild)
			return;
		player.getPersistentData().putInt(TIME_SINCE_LAST_SWING, player.attackStrengthTicker);
	}

	@SubscribeEvent
	public void onLivingHurtEvent(LivingHurtEvent event) {
		if (!this.isEnabled()
				|| !(event.getSource().getDirectEntity() instanceof Player player))
			return;
		player.getPersistentData().putBoolean(SHOULD_APPLY_NO_KNOCKBACK, true);
	}

	@SubscribeEvent
	public void onKnockback(LivingKnockBackEvent event) {
		if (!this.isEnabled())
			return;

		reducedKnockback(event);
		itemKnockbackReduction(event);
	}

	public void reducedKnockback(LivingKnockBackEvent event) {
		if (event.getEntity().lastHurtByPlayer == null
				|| event.getEntity().lastHurtByPlayerTime != 100
				|| !(event.getEntity().getLastHurtByMob() instanceof Player player)
				|| player.getAbilities().instabuild)
			return;

		if (!player.getPersistentData().getBoolean(SHOULD_APPLY_NO_KNOCKBACK))
			return;

		ItemStack itemStack = player.getMainHandItem();

		float reducedKnockback = 1f;
		Multimap<Attribute, AttributeModifier> attributeModifiers = itemStack.getAttributeModifiers(EquipmentSlot.MAINHAND);
		if ((!attributeModifiers.containsKey(Attributes.ATTACK_DAMAGE)
				|| (isEnabled(ItemStats.class) && ItemStats.unbreakableItems && ItemStats.isBroken(itemStack)))
				&& noWeaponPenalty < 1d)
			reducedKnockback = Math.min(reducedKnockback, noWeaponPenalty.floatValue());

		int ticksSinceLastSwing = player.getPersistentData().getInt(TIME_SINCE_LAST_SWING);
		float cooldown = Mth.clamp((ticksSinceLastSwing + 0.5f) / player.getCurrentItemAttackStrengthDelay(), 0.0F, 1.0F);
		if (cooldown <= 0.9f)
			reducedKnockback = Math.min(reducedKnockback, spamPenalty.floatValue());
		if (reducedKnockback < 1f) {
			if (player.isSprinting())
				event.setStrength(event.getStrength() - 0.5f);
			event.setStrength(event.getStrength() * reducedKnockback);
		}
		player.getPersistentData().putBoolean(SHOULD_APPLY_NO_KNOCKBACK, false);
	}

	public void itemKnockbackReduction(LivingKnockBackEvent event) {
		if (event.getEntity().getLastHurtByMob() == null)
			return;
		float multiplier = 1f;
		for (IdTagValue idTagValue : knockbackMultipliers) {
			if (idTagValue.id.matchesItem(event.getEntity().getLastHurtByMob().getMainHandItem()))
				multiplier = (float) idTagValue.value;
		}
		event.setStrength(event.getStrength() * multiplier);
	}
}