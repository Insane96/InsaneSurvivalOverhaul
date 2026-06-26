package insane96mcp.insanesurvivaloverhaul.data.modifier;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import net.minecraft.core.BlockPos;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import org.jetbrains.annotations.Nullable;

@JsonAdapter(SunlightModifier.Serializer.class)
public class SunlightModifier extends Modifier {
    int minSunlight;
    protected SunlightModifier(float modifier, Operation operation, int minSunlight) {
        super(modifier, operation);
        this.minSunlight = minSunlight;
    }

    @Override
    public boolean shouldApply(Level level, BlockPos pos, @Nullable LivingEntity entity) {
        int skyLight = level.getBrightness(LightLayer.SKY, pos);
        return skyLight < this.minSunlight && level.isDay();
    }

    public static class Serializer implements JsonDeserializer<SunlightModifier> {
        @Override
        public SunlightModifier deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jObject = json.getAsJsonObject();
            return new SunlightModifier(
                    GsonHelper.getAsFloat(jObject, "modifier"),
                    context.deserialize(jObject.get("operation"), Operation.class),
                    GsonHelper.getAsInt(jObject, "min_sunlight")
            );
        }
    }
}
