package insane96mcp.iguanatweaksreborn.module.movement.weightedequipment;

import com.ezylang.evalex.Expression;
import com.google.common.collect.Multimap;
import insane96mcp.iguanatweaksreborn.InsaneSurvivalOverhaul;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.module.combat.RegeneratingAbsorption;
import insane96mcp.insanelib.base.JsonFeature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.data.IdTagMatcher;
import insane96mcp.insanelib.util.LogHelper;
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

@Label(name = "Weighted Armor", description = "Armor slows down the player. Enchantment Weights are controlled via json in this feature's folder. If you need more control over armor, use item definitions")
@LoadFeature(module = Modules.Ids.MOVEMENT, enabledByDefault = false)
public class WeightedArmor extends JsonFeature {
    public static final String ARMOR_SLOWDOWN = InsaneSurvivalOverhaul.MOD_ID + ".armor_slowdown";
    public static final UUID ARMOR_SLOWDOWN_UUID = UUID.fromString("8588420e-ce50-4e4e-a3e4-974dfc8a98ec");

    public static final ArrayList<ArmorEnchantmentWeight> ENCHANTMENTS_LIST_DEFAULT = new ArrayList<>(List.of(
            new ArmorEnchantmentWeight.Builder(IdTagMatcher.newId("minecraft:feather_falling")).setPercentageSlownessPerLevel(-0.005f).build()
    ));
    public static final ArrayList<ArmorEnchantmentWeight> enchantmentsList = new ArrayList<>();

    @Config
    @Label(description = "Formula to calculate the slowdown of a piece of armor. Variables available are: armor, armor_toughness. This is evaluated with EvalEx https://ezylang.github.io/EvalEx/concepts/parsing_evaluation.html. Please note that 1 means 100% slowdown")
    public static String slowdownFormula = "(armor * 0.01) * (1 + (armor_toughness * 0.05))";

    public WeightedArmor(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
        addSyncType(new ResourceLocation(InsaneSurvivalOverhaul.MOD_ID, "enchantments_weights"), new SyncType(json -> loadAndReadJson(json, enchantmentsList, ENCHANTMENTS_LIST_DEFAULT, ArmorEnchantmentWeight.LIST_TYPE)));
        JSON_CONFIGS.add(new JsonConfig<>("enchantments_weights.json", enchantmentsList, ENCHANTMENTS_LIST_DEFAULT, ArmorEnchantmentWeight.LIST_TYPE, true, new ResourceLocation(InsaneSurvivalOverhaul.MOD_ID, "enchantments_weights")));
    }

    @Override
    public String getModConfigFolder() {
        return InsaneSurvivalOverhaul.CONFIG_FOLDER;
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
        if (!(itemStack.getItem() instanceof ArmorItem armorItem))
            return 0d;
        double slowdown = 0d;
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
        slowdown = evaluateWeight(armor, armorToughness);
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

	private float evaluateWeight(double armor, double armorToughness) {
		Expression expression = new Expression(slowdownFormula)
				.with("armor", armor)
				.with("armor_toughness", armorToughness);
		try {
			return expression.evaluate().getNumberValue().floatValue();
		}
		catch (Exception ex) {
			LogHelper.error("Failed to evaluate armor formula: %s\n%s", slowdownFormula, ex);
			return 0;
		}
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