package insane96mcp.iguanatweaksreborn.module.farming.livestock;

import net.minecraft.world.entity.LivingEntity;
import virtuoel.pehkui.api.ScaleTypes;

public class PehkuiIntegration {

    public static void setSize(LivingEntity entity, Livestock.Age age) {
        float scale = switch (age) {
            case YOUNG -> 0.75f;
            case ADULT -> 0.9f;
            case MID_AGE -> 1.2f;
            case OLD -> 1.05f;
        };
        ScaleTypes.MODEL_WIDTH.getScaleData(entity).setScale(scale);
        ScaleTypes.MODEL_HEIGHT.getScaleData(entity).setScale(scale);
    }
}
