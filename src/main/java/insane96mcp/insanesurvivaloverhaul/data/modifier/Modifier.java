package insane96mcp.insanesurvivaloverhaul.data.modifier;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@JsonAdapter(Modifier.Serializer.class)
public class Modifier {
    protected final float modifier;
    protected final Operation operation;

    protected Modifier(float modifier, Operation operation) {
        this.modifier = modifier;
        this.operation = operation;
    }

    public boolean shouldApply(Level level, BlockPos pos, @Nullable LivingEntity entity) {
        return true;
    }

    public float getModifier() {
        return this.modifier;
    }

    public Operation getOperation() {
        return this.operation;
    }

    public static class Serializer implements JsonDeserializer<Modifier> {
        @Override
        public Modifier deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jObject = json.getAsJsonObject();
            return new Modifier(
                    GsonHelper.getAsFloat(jObject, "modifier"),
                    context.deserialize(jObject.get("operation"), Operation.class)
            );
        }
    }

    public static List<Modifier> deserializeList(JsonObject jObject, String memberName, JsonDeserializationContext context) {
        List<Modifier> modifiers = new ArrayList<>();
        if (!jObject.has(memberName))
            return modifiers;
        JsonArray aModifiers = GsonHelper.getAsJsonArray(jObject, memberName);
        for (JsonElement jsonElement : aModifiers) {
            JsonObject jObjectModifier = jsonElement.getAsJsonObject();
            ResourceLocation modifierId = ResourceLocation.tryParse(GsonHelper.getAsString(jObjectModifier, "id"));
            Type modifierType = Modifiers.MODIFIERS.get(modifierId);
            if (modifierType == null) {
                InsaneSO.LOGGER.warn("modifier %s does not exist. Skipping".formatted(modifierId));
                continue;
            }
            modifiers.add(context.deserialize(jObjectModifier, modifierType));
        }
        return modifiers;
    }

    public static float applyModifiers(float originalValue, List<Modifier> modifiers, Level level, BlockPos pos, @Nullable LivingEntity entity) {
        List<Modifier> addModifiers = modifiers.stream().filter(modifier -> modifier.operation == Operation.ADD).toList();
        List<Modifier> multiplyModifiers = modifiers.stream().filter(modifier -> modifier.operation == Operation.MULTIPLY).toList();
        float addedUpValue = originalValue;
        for (Modifier modifier : addModifiers) {
            if (!modifier.shouldApply(level, pos, entity))
                continue;
            addedUpValue += modifier.getModifier();
        }
        float finalValue = addedUpValue;
        for (Modifier modifier : multiplyModifiers) {
            if (!modifier.shouldApply(level, pos, entity))
                continue;
            finalValue += addedUpValue * modifier.getModifier();
        }
        return finalValue;
    }

    public enum Operation {
        @SerializedName("add")
        ADD,
        @SerializedName("multiply")
        MULTIPLY
    }
}
