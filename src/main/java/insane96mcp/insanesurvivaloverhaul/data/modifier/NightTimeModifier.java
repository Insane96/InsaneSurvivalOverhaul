package insane96mcp.insanesurvivaloverhaul.data.modifier;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import net.minecraft.core.BlockPos;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

@JsonAdapter(NightTimeModifier.Serializer.class)
public class NightTimeModifier extends Modifier {
    protected NightTimeModifier(float modifier, Operation operation) {
        super(modifier, operation);
    }

    @Override
    public boolean shouldApply(Level level, BlockPos pos, @Nullable LivingEntity entity) {
        int dayTime = (int) (level.dayTime() % 24000);
        return dayTime >= 12786 && dayTime < 23216;
    }

    public static class Serializer implements JsonDeserializer<NightTimeModifier> {
        @Override
        public NightTimeModifier deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jObject = json.getAsJsonObject();
            return new NightTimeModifier(
                    GsonHelper.getAsFloat(jObject, "modifier"),
                    context.deserialize(jObject.get("operation"), Operation.class)
            );
        }
    }
}
