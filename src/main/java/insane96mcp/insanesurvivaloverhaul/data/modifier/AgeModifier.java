package insane96mcp.insanesurvivaloverhaul.data.modifier;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import insane96mcp.insanesurvivaloverhaul.module.animals.Aging;
import net.minecraft.core.BlockPos;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

@JsonAdapter(AgeModifier.Serializer.class)
public class AgeModifier extends Modifier {
    Aging.Age age;
    protected AgeModifier(float modifier, Operation operation, Aging.Age age) {
        super(modifier, operation);
        this.age = age;
    }

    @Override
    public boolean shouldApply(Level level, BlockPos pos, @Nullable LivingEntity entity) {
        return entity instanceof AgeableMob ageableMob
                && Aging.getAge(ageableMob) == this.age;
    }

    public static class Serializer implements JsonDeserializer<AgeModifier> {
        @Override
        public AgeModifier deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jObject = json.getAsJsonObject();
            return new AgeModifier(
                    GsonHelper.getAsFloat(jObject, "modifier"),
                    context.deserialize(jObject.get("operation"), Operation.class),
                    context.deserialize(jObject.get("age"), Aging.Age.class)
            );
        }
    }
}
