package insane96mcp.insanesurvivaloverhaul.module.animals.equineattributes;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.reflect.TypeToken;
import insane96mcp.insanelib.core.feature.config.MinMaxConfig;
import insane96mcp.insanelib.data.ObjTag;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EntityType;

import javax.annotation.Nullable;
import java.util.ArrayList;

/**
 * Fixed (or randomized in a custom range) attributes for an equine entity (horses, donkeys, mules, llamas, ...).
 * Each value is optional: when missing, the vanilla value is left untouched.
 * In JSON each value can be either a number (fixed value) or an object with "min" and "max".
 */
@JsonAdapter(EquineStats.Serializer.class)
public class EquineStats {
    public ObjTag<EntityType<?>> entity;
    @Nullable
    public MinMaxConfig health;
    @Nullable
    public MinMaxConfig movementSpeed;
    @Nullable
    public MinMaxConfig jumpStrength;
    @Nullable
    public MinMaxConfig stepHeight;
    /** Llamas only. Strength defines the chest columns (3 slots each). Clamped by vanilla to 1-5 */
    @Nullable
    public MinMaxConfig strength;
    /** Llamas only. Chance for the baby to get +1 strength over the strongest parent when bred */
    @Nullable
    public Float extraStrengthChance;

    public EquineStats(ObjTag<EntityType<?>> entity, @Nullable MinMaxConfig health, @Nullable MinMaxConfig movementSpeed, @Nullable MinMaxConfig jumpStrength, @Nullable MinMaxConfig stepHeight, @Nullable MinMaxConfig strength, @Nullable Float extraStrengthChance) {
        this.entity = entity;
        this.health = health;
        this.movementSpeed = movementSpeed;
        this.jumpStrength = jumpStrength;
        this.stepHeight = stepHeight;
        this.strength = strength;
        this.extraStrengthChance = extraStrengthChance;
    }

    public static EquineStats of(String entity, double health, double movementSpeed, double jumpStrength, double stepHeight) {
        return new EquineStats(ObjTag.of(entity, Registries.ENTITY_TYPE), new MinMaxConfig(health), new MinMaxConfig(movementSpeed), new MinMaxConfig(jumpStrength), new MinMaxConfig(stepHeight), null, null);
    }

    public EquineStats withStrength(int strength, float extraStrengthChance) {
        this.strength = new MinMaxConfig(strength);
        this.extraStrengthChance = extraStrengthChance;
        return this;
    }

    public static final java.lang.reflect.Type LIST_TYPE = new TypeToken<ArrayList<EquineStats>>(){}.getType();

    public static class Serializer implements JsonDeserializer<EquineStats>, JsonSerializer<EquineStats> {
        @Override
        public EquineStats deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jObject = json.getAsJsonObject();
            ObjTag<EntityType<?>> entity = ObjTag.deserialize(jObject.get("entity"), Registries.ENTITY_TYPE);
            Float extraStrengthChance = jObject.has("extra_strength_chance") ? GsonHelper.getAsFloat(jObject, "extra_strength_chance") : null;
            return new EquineStats(entity,
                    deserializeRange(jObject, "health"),
                    deserializeRange(jObject, "movement_speed"),
                    deserializeRange(jObject, "jump_strength"),
                    deserializeRange(jObject, "step_height"),
                    deserializeRange(jObject, "strength"),
                    extraStrengthChance);
        }

        @Override
        public JsonElement serialize(EquineStats src, java.lang.reflect.Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.add("entity", src.entity.serialize());
            serializeRange(jsonObject, "health", src.health);
            serializeRange(jsonObject, "movement_speed", src.movementSpeed);
            serializeRange(jsonObject, "jump_strength", src.jumpStrength);
            serializeRange(jsonObject, "step_height", src.stepHeight);
            serializeRange(jsonObject, "strength", src.strength);
            if (src.extraStrengthChance != null)
                jsonObject.addProperty("extra_strength_chance", src.extraStrengthChance);
            return jsonObject;
        }

        /**
         * Reads either a number (fixed value) or an object with "min" and "max"
         */
        @Nullable
        private static MinMaxConfig deserializeRange(JsonObject jObject, String key) {
            if (!jObject.has(key))
                return null;
            JsonElement element = jObject.get(key);
            if (element.isJsonPrimitive())
                return new MinMaxConfig(element.getAsDouble());
            JsonObject range = GsonHelper.convertToJsonObject(element, key);
            double min = GsonHelper.getAsDouble(range, "min");
            double max = GsonHelper.getAsDouble(range, "max");
            if (min > max)
                throw new JsonParseException("Invalid %s range: min (%s) is greater than max (%s)".formatted(key, min, max));
            return new MinMaxConfig(min, max);
        }

        private static void serializeRange(JsonObject jsonObject, String key, @Nullable MinMaxConfig range) {
            if (range == null)
                return;
            if (range.min == range.max) {
                jsonObject.addProperty(key, range.min);
                return;
            }
            JsonObject rangeObject = new JsonObject();
            rangeObject.addProperty("min", range.min);
            rangeObject.addProperty("max", range.max);
            jsonObject.add(key, rangeObject);
        }
    }
}
