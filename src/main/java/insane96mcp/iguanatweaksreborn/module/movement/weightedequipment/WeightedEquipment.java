package insane96mcp.iguanatweaksreborn.module.movement.weightedequipment;

import com.google.common.collect.Multimap;
import insane96mcp.iguanatweaksreborn.IguanaTweaksReborn;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.module.combat.RegeneratingAbsorption;
import insane96mcp.insanelib.base.JsonFeature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.util.MCUtils;
import insane96mcp.insanelib.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Label(name = "Weighted Equipment", description = "Armor slows down the player. Enchantment Weights are controlled via json in this feature's folder. Armor weights are controlled via Item Definitions.")
@LoadFeature(module = Modules.Ids.MOVEMENT, enabledByDefault = false)
public class WeightedEquipment extends JsonFeature {
	public static final String ARMOR_SLOWDOWN = IguanaTweaksReborn.MOD_ID + ".armor_slowdown";
	public static final UUID ARMOR_SLOWDOWN_UUID = UUID.fromString("8588420e-ce50-4e4e-a3e4-974dfc8a98ec");

	//TODO Change formula to use attribute modifier
	public static final ArrayList<ArmorEnchantmentWeight> ENCHANTMENTS_LIST_DEFAULT = new ArrayList<>(List.of(
			//new ArmorEnchantmentWeight.Builder(IdTagMatcher.newId("minecraft:feather_falling")).setPercentageSlownessPerLevel(-0.005f).build()
	));
	public static final ArrayList<ArmorEnchantmentWeight> enchantmentsList = new ArrayList<>();

	@Config(min = 0, max = 1d)
	@Label(name = "Slowdown per Armor", description = "Percentage slowdown per point of armor the player is wearing.")
	public static Double slownessPerArmor = 0d;
	@Config(min = 0, max = 1d)
	@Label(name = "Percentage Increase per Toughness", description = """
						This value times the Armor Toughness worn by the player is a percentage increase of the Slowdown per Armor.
						Total percentage slowdown is '(slowness_per_armor * armor_points) * (1 + (toughness * percentage_per_toughness))'
						E.g. with 'Slowness per Armor' set to 0.005 and this set to 0.025 and the player wearing Diamond Armor the slowdown is '(0.005 * 20) * (1 + (8 * 0.025))' = '0.1 * 1.2'= '0.12' = -12% Speed applied to the player.""")
	public static Double percentagePerToughness = 0d;

	public WeightedEquipment(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
		addSyncType(new ResourceLocation(IguanaTweaksReborn.MOD_ID, "enchantments_weights"), new SyncType(json -> loadAndReadJson(json, enchantmentsList, ENCHANTMENTS_LIST_DEFAULT, ArmorEnchantmentWeight.LIST_TYPE)));
		JSON_CONFIGS.add(new JsonConfig<>("enchantments_weights.json", enchantmentsList, ENCHANTMENTS_LIST_DEFAULT, ArmorEnchantmentWeight.LIST_TYPE, true, new ResourceLocation(IguanaTweaksReborn.MOD_ID, "enchantments_weights")));
	}

	@Override
	public String getModConfigFolder() {
		return IguanaTweaksReborn.CONFIG_FOLDER;
	}

	@Override
	public void loadJsonConfigs() {
		if (!this.isEnabled())
			return;
		super.loadJsonConfigs();
	}

	//Can't use ItemAttributeModifierEvent as I need all the modifiers of the item (ItemStack#getAttributeModifiers) and that causes a loop
	@SubscribeEvent
	public void playerTick(TickEvent.PlayerTickEvent event) {
		if (event.phase != TickEvent.Phase.END)
			return;
		Player player = event.player;
		AttributeInstance movementSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);
		if (movementSpeed == null)
			return;
		AttributeModifier modifier = movementSpeed.getModifier(ARMOR_SLOWDOWN_UUID);
		if (!this.isEnabled()) {
			//If the feature has been disabled remove the slowdown from the player
			if (modifier != null)
				movementSpeed.removeModifier(modifier);
			return;
		}
		double slowdown = 0d;
		for (ItemStack stack : player.getArmorSlots()) {
			slowdown += getArmorSlowdown(stack);
		}
		//If it's 0 then there's no slowdown applicable
		if (slowdown == 0d) {
			if (modifier != null)
				movementSpeed.removeModifier(modifier);
			return;
		}
		if (modifier == null || modifier.getAmount() != -slowdown) {
			modifier = new AttributeModifier(ARMOR_SLOWDOWN_UUID, ARMOR_SLOWDOWN, -slowdown, AttributeModifier.Operation.MULTIPLY_BASE);
			movementSpeed.removeModifier(ARMOR_SLOWDOWN_UUID);
			movementSpeed.addTransientModifier(modifier);
		}
	}

	private double getArmorSlowdown(ItemStack itemStack) {
		if (!(itemStack.getItem() instanceof ArmorItem))
			return 0d;
		double slowdown = 0d;
		/*boolean hasMaterialSlowdown = false;
		for (IdTagValue idTagValue : armorWeights) {
			if (!idTagValue.id.matchesItem(itemStack.getItem())
					|| !(itemStack.getItem() instanceof ArmorItem armorItem))
				continue;
			EquipmentSlot slot = armorItem.getEquipmentSlot();
            slowdown = idTagValue.value * materialRequiredAmountRatio.get(slot);
			hasMaterialSlowdown = true;
			break;
		}*/
		//If no slowdown was found in the material weight
		//if (!hasMaterialSlowdown) {
			ArmorItem armorItem = (ArmorItem) itemStack.getItem();
			Multimap<Attribute, AttributeModifier> attributeModifiers = itemStack.getAttributeModifiers(armorItem.getEquipmentSlot());
			double armor = 0d;
			Collection<AttributeModifier> armorModifiers = attributeModifiers.get(Attributes.ARMOR);
			if (armorModifiers.isEmpty())
				armorModifiers = attributeModifiers.get(RegeneratingAbsorption.ATTRIBUTE.get());
			for (AttributeModifier attributeModifier : armorModifiers) {
				if (!attributeModifier.getOperation().equals(AttributeModifier.Operation.ADDITION))
					continue;
				armor += attributeModifier.getAmount();
			}
			double armorToughness = 0d;
			Collection<AttributeModifier> toughnessModifiers = attributeModifiers.get(Attributes.ARMOR_TOUGHNESS);
			if (armorModifiers.isEmpty())
				toughnessModifiers = attributeModifiers.get(RegeneratingAbsorption.SPEED_ATTRIBUTE.get());
			for (AttributeModifier attributeModifier : toughnessModifiers) {
				if (!attributeModifier.getOperation().equals(AttributeModifier.Operation.ADDITION))
					continue;
				armorToughness += attributeModifier.getAmount();
			}
			double armorSlowdown = armor * slownessPerArmor;
			double toughnessSlowdown = armorToughness * percentagePerToughness;
			slowdown = armorSlowdown * (1 + toughnessSlowdown);
		//}
		double flatEnchantmentSlowdown = 0d, percentageEnchantmentSlowdown = 0d;
		for (ArmorEnchantmentWeight enchantmentWeight : enchantmentsList) {
			int enchantmentLevel = MCUtils.getEnchantmentLevel(enchantmentWeight.enchantment.location, itemStack);
			if (enchantmentLevel == 0)
				continue;
			flatEnchantmentSlowdown += (enchantmentWeight.flatSlowness + (enchantmentWeight.flatSlownessPerLevel * enchantmentLevel));
			percentageEnchantmentSlowdown += (enchantmentWeight.percentageSlowness + (enchantmentWeight.percentageSlownessPerLevel * enchantmentLevel));
		}
		slowdown += flatEnchantmentSlowdown;
		slowdown *= 1 + percentageEnchantmentSlowdown;
		return slowdown;
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public void onTooltip(ItemTooltipEvent event) {
		if (!this.isEnabled())
			return;
		ItemStack stack = event.getItemStack();
		double slowdown = getArmorSlowdown(stack) * 100d;
		if (slowdown <= 0d)
			return;
		event.getToolTip().add(Component.translatable(ARMOR_SLOWDOWN, Utils.formatDecimal(slowdown, "#.#")).withStyle(ChatFormatting.RED));
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public void debugScreen(CustomizeGuiOverlayEvent.DebugText event) {
		if (!this.isEnabled())
			return;
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer playerEntity = mc.player;
		if (playerEntity == null)
			return;
		AttributeInstance movementSpeed = playerEntity.getAttribute(Attributes.MOVEMENT_SPEED);
		if (movementSpeed == null)
			return;
		AttributeModifier modifier = movementSpeed.getModifier(ARMOR_SLOWDOWN_UUID);
		if (mc.options.renderDebug && !mc.showOnlyReducedInfo() && modifier != null) {
			event.getLeft().add(String.format("Armor Slowdown: %s%%", Utils.formatDecimal(Math.abs(modifier.getAmount()) * 100f, "#.#")));
		}
	}
}