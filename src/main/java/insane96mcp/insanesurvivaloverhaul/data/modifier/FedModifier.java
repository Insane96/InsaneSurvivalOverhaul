package insane96mcp.insanesurvivaloverhaul.data.modifier;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import insane96mcp.insanesurvivaloverhaul.module.farming.livestock.Livestock;
import net.minecraft.core.BlockPos;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

@JsonAdapter(FedModifier.Serializer.class)
public class FedModifier extends Modifier {
    protected FedModifier(float modifier, Operation operation) {
        super(modifier, operation);
    }

    @Override
    public boolean shouldApply(Level level, BlockPos pos, @Nullable LivingEntity entity) {
        return entity instanceof AgeableMob ageableMob
                && Livestock.hasBeenFedRecently(ageableMob);
    }

    public static class Serializer implements JsonDeserializer<FedModifier> {
        @Override
        public FedModifier deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jObject = json.getAsJsonObject();
            return new FedModifier(
                    GsonHelper.getAsFloat(jObject, "modifier"),
                    context.deserialize(jObject.get("operation"), Operation.class)
            );
        }
    }
}
