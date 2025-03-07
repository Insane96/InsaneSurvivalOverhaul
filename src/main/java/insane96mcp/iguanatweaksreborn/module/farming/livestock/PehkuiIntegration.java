package insane96mcp.iguanatweaksreborn.module.farming.livestock;

import net.minecraft.world.entity.LivingEntity;
import virtuoel.pehkui.api.ScaleTypes;

public class PehkuiIntegration {

    public static void setSize(LivingEntity entity, Livestock.Age age) {
        float scale = switch (age) {
            case YOUNG -> 0.85f;
            case ADULT -> 1.15f;
            case ELDER -> 1.0f;
        };
        ScaleTypes.MODEL_WIDTH.getScaleData(entity).setScale(scale);
        ScaleTypes.MODEL_HEIGHT.getScaleData(entity).setScale(scale);
    }
}
