package insane96mcp.insanesurvivaloverhaul.module.combat.bows;

import net.minecraft.world.item.BowItem;

public class ShortbowItem extends BowItem {
    public ShortbowItem(Properties pProperties) {
        super(pProperties);
    }

    /**
     * Gets the velocity of the arrow entity from the bow's charge
     */
    public static float getPowerForTime(int chargeTicks) {
        float power = ((float)chargeTicks / getFullChargeTicks());
        power = (power * power + power * 2.0F) / 3.0F;
        return Math.min(power, 1.0F) * 0.75f;
    }

    public static int getFullChargeTicks() {
        return 15;
    }
}
