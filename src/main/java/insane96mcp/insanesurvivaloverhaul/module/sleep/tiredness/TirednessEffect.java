package insane96mcp.insanesurvivaloverhaul.module.sleep.tiredness;

import insane96mcp.insanelib.world.effect.ILMobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeMap;

public class TirednessEffect extends ILMobEffect {
    public TirednessEffect(MobEffectCategory typeIn, int liquidColorIn) {
        super(typeIn, liquidColorIn, false);
    }

    @Override
    public void addAttributeModifiers(AttributeMap attributeMap, int amplifier) {
        if (amplifier == 0)
            return;
        super.addAttributeModifiers(attributeMap, amplifier - 1);
    }
}
