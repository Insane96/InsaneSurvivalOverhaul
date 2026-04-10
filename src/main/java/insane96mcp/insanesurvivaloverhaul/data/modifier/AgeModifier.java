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

@JsonAdapter(AgeModifier.Serializer.class)
public class AgeModifier extends Modifier {
    Livestock.Age age;
    protected AgeModifier(float modifier, Operation operation, Livestock.Age age) {
        super(modifier, operation);
        this.age = age;
    }

    @Override
    public boolean shouldApply(Level level, BlockPos pos, @Nullable LivingEntity entity) {
        return entity instanceof AgeableMob ageableMob
                && Livestock.getAge(ageableMob) == this.age;
    }

    public static class Serializer implements JsonDeserializer<AgeModifier> {
        @Override
        public AgeModifier deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jObject = json.getAsJsonObject();
            return new AgeModifier(
                    GsonHelper.getAsFloat(jObject, "modifier"),
                    context.deserialize(jObject.get("operation"), Operation.class),
                    context.deserialize(jObject.get("age"), Livestock.Age.class)
            );
        }
    }
}
