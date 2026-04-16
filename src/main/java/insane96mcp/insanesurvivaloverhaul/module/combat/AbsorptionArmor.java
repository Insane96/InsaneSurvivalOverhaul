package insane96mcp.insanesurvivaloverhaul.module.combat;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.module.combat.regeneratingabsorption.RegeneratingAbsorption;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;

import java.util.ArrayList;
import java.util.List;

@LoadFeature(module = ISOModules.COMBAT,
        description = "Armor gives regenerating absorption and regen absorption speed instead of armor and toughness",
        enabledByDefault = false)
public class AbsorptionArmor extends Feature {

    @Config(min = 0, description = "If > 0, only Armor Toughness on items will be converted to absorption, multiplying it by this.")
    public static Double toughnessToAbsorptionRatio = 0.25d;

    @SubscribeEvent
    public void onAttributeEvent(ItemAttributeModifierEvent event) {
        if (!this.isEnabled())
            return;

        List<ItemAttributeModifiers.Entry> toAdd = new ArrayList<>();
        List<ItemAttributeModifiers.Entry> toRemove = new ArrayList<>();
        event.getModifiers().forEach(entry -> {
            if (entry.attribute().equals(Attributes.ARMOR) && toughnessToAbsorptionRatio == 0) {
                toAdd.add(new ItemAttributeModifiers.Entry(RegeneratingAbsorption.ATTRIBUTE, new AttributeModifier(entry.modifier().id(), entry.modifier().amount(), entry.modifier().operation()), entry.slot()));
                toRemove.add(entry);
            }
            if (entry.attribute().equals(Attributes.ARMOR_TOUGHNESS)) {
                if (toughnessToAbsorptionRatio > 0) {
                    toAdd.add(new ItemAttributeModifiers.Entry(RegeneratingAbsorption.ATTRIBUTE, new AttributeModifier(entry.modifier().id(), entry.modifier().amount() * toughnessToAbsorptionRatio, entry.modifier().operation()), entry.slot()));
                }
                else {
                    if (entry.modifier().operation() == AttributeModifier.Operation.ADD_VALUE)
                        toAdd.add(new ItemAttributeModifiers.Entry(RegeneratingAbsorption.SPEED_ATTRIBUTE, new AttributeModifier(entry.modifier().id(), entry.modifier().amount() * 0.1d, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), entry.slot()));
                    else
                        toAdd.add(new ItemAttributeModifiers.Entry(RegeneratingAbsorption.SPEED_ATTRIBUTE, new AttributeModifier(entry.modifier().id(), entry.modifier().amount(), AttributeModifier.Operation.ADD_MULTIPLIED_BASE), entry.slot()));
                }
                toRemove.add(entry);
            }
        });
        toRemove.forEach(entry -> event.removeModifier(entry.attribute(), entry.modifier().id()));
        toAdd.forEach(entry -> event.addModifier(entry.attribute(), entry.modifier(), entry.slot()));
    }
}
