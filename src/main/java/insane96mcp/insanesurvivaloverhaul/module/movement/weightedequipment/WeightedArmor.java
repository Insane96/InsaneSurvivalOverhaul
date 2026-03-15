package insane96mcp.insanesurvivaloverhaul.module.movement.weightedequipment;

import com.ezylang.evalex.Expression;
import insane96mcp.insanelib.core.JsonFeature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.data.ObjTag;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.module.combat.regeneratingabsorption.RegeneratingAbsorption;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;

import java.util.ArrayList;
import java.util.List;

@LoadFeature(module = ISOModules.MOVEMENT, enabledByDefault = false, description = "Armor slows down the player. Enchantment Weights are controlled via json in this feature's folder. Please note that default mods' armor slowdown is done through item definitions within the combat_rework data pack")
public class WeightedArmor extends JsonFeature {
    public static final ResourceLocation ARMOR_SLOWDOWN_ID = InsaneSO.location("armor_slowdown");

    public static final ArrayList<ArmorEnchantmentWeight> ENCHANTMENTS_LIST_DEFAULT = new ArrayList<>();
    public static final ArrayList<ArmorEnchantmentWeight> enchantmentsList = new ArrayList<>();
    public static final ResourceLocation ENCHANTMENTS_WEIGHTS_SYNC_ID = InsaneSO.location("enchantments_weights");

    @Config(description = "Formula to calculate the slowdown of a piece of armor. Variables available are: armor, armor_toughness. This is evaluated with EvalEx https://ezylang.github.io/EvalEx/concepts/parsing_evaluation.html. Please note that 1 means 100% slowdown. Regenerating Absorption counts as Toughness")
    public static String slowdownFormula = "(armor * 0.01) * (1 + (armor_toughness * 0.05))";

    @Override
    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        ENCHANTMENTS_LIST_DEFAULT.add(new ArmorEnchantmentWeight.Builder(ObjTag.of("minecraft:feather_falling", Registries.ENCHANTMENT)).setFlatSlownessPerLevel(-0.005f).build());
        addSyncType(ENCHANTMENTS_WEIGHTS_SYNC_ID, new SyncType(json -> loadAndReadJson(json, enchantmentsList, ENCHANTMENTS_LIST_DEFAULT, ArmorEnchantmentWeight.LIST_TYPE, Registries.ENCHANTMENT)));
        this.getJsonConfigs().add(new JsonConfig<>("enchantments_weights.json", enchantmentsList, ENCHANTMENTS_LIST_DEFAULT, ArmorEnchantmentWeight.LIST_TYPE).syncToClient(ENCHANTMENTS_WEIGHTS_SYNC_ID));
    }

    @Override
    public String getModConfigFolder() {
        return InsaneSO.CONFIG_FOLDER;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onItemAttributeModifier(ItemAttributeModifierEvent event) {
        if (!this.isEnabled()
                || !(event.getItemStack().getItem() instanceof ArmorItem armorItem))
            return;
        double slowdown = getArmorSlowdown(event.getModifiers(), event.getItemStack());
        if (slowdown <= 0)
            return;
        event.addModifier(Attributes.MOVEMENT_SPEED, new AttributeModifier(ARMOR_SLOWDOWN_ID, -slowdown, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), EquipmentSlotGroup.bySlot(armorItem.getEquipmentSlot()));
    }

    private double getArmorSlowdown(List<ItemAttributeModifiers.Entry> modifiers, ItemStack itemStack) {
        double armor = 0d;
        double armorToughness = 0d;
        for (ItemAttributeModifiers.Entry entry : modifiers) {
            if (!entry.modifier().operation().equals(AttributeModifier.Operation.ADD_VALUE))
                continue;
            if (entry.attribute().is(Attributes.ARMOR))
                armor += entry.modifier().amount();
            else if (entry.attribute().is(Attributes.ARMOR_TOUGHNESS) || entry.attribute().is(RegeneratingAbsorption.ATTRIBUTE))
                armorToughness += entry.modifier().amount();
        }
        double slowdown = evaluateWeight(armor, armorToughness);
        double flatEnchantmentSlowdown = 0d, percentageEnchantmentSlowdown = 0d;
        for (ArmorEnchantmentWeight enchantmentWeight : enchantmentsList) {
            int enchantmentLevel = getEnchantmentLevel(itemStack, enchantmentWeight.enchantment);
            if (enchantmentLevel == 0)
                continue;
            flatEnchantmentSlowdown += (enchantmentWeight.flatSlowness + (enchantmentWeight.flatSlownessPerLevel * enchantmentLevel));
            percentageEnchantmentSlowdown += (enchantmentWeight.percentageSlowness + (enchantmentWeight.percentageSlownessPerLevel * enchantmentLevel));
        }
        slowdown += flatEnchantmentSlowdown;
        slowdown *= 1 + percentageEnchantmentSlowdown;
        return slowdown;
    }

    private static int getEnchantmentLevel(ItemStack stack, ObjTag<Enchantment> enchantment) {
        Holder<Enchantment> holder = enchantment.asHolder();
        if (holder != null)
            return stack.getEnchantmentLevel(holder);
        ItemEnchantments enchantments = stack.get(DataComponents.ENCHANTMENTS);
        if (enchantments == null)
            return 0;
        int max = 0;
        for (Holder<Enchantment> enchHolder : enchantments.keySet()) {
            if (enchantment.matches(enchHolder))
                max = Math.max(max, enchantments.getLevel(enchHolder));
        }
        return max;
    }

	private float evaluateWeight(double armor, double armorToughness) {
		Expression expression = new Expression(slowdownFormula)
				.with("armor", armor)
				.with("armor_toughness", armorToughness);
		try {
			return expression.evaluate().getNumberValue().floatValue();
		}
		catch (Exception ex) {
			InsaneSO.LOGGER.error("Failed to evaluate armor formula: {}\n{}", slowdownFormula, ex);
			return 0;
		}
	}

    /*@OnlyIn(Dist.CLIENT)
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
        AttributeModifier modifier = movementSpeed.getModifier(ARMOR_SLOWDOWN_ID);
        if (mc.options.renderDebug && !mc.showOnlyReducedInfo() && modifier != null) {
            event.getLeft().add(String.format("Armor Slowdown: %s%%", Utils.formatDecimal(Math.abs(modifier.getAmount()) * 100f, "#.#")));
        }
    }*/
}