package insane96mcp.iguanatweaksreborn.integration;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;
import slimeknights.tconstruct.tools.TinkerModifiers;

public class TConstruct {
    public static boolean hasBouncy(LivingEntity living) {
        return ModifierUtil.getModifierLevel(living.getItemBySlot(EquipmentSlot.FEET), TinkerModifiers.bouncy.getId()) > 0;
    }
}
