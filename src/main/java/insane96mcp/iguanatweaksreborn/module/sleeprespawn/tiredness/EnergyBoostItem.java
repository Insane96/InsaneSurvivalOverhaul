package insane96mcp.iguanatweaksreborn.module.sleeprespawn.tiredness;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.reflect.TypeToken;
import insane96mcp.insanelib.data.IdTagMatcher;
import insane96mcp.insanelib.util.MCUtils;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;

@JsonAdapter(EnergyBoostItem.Serializer.class)
public class EnergyBoostItem {
    IdTagMatcher edible;
    public int duration;
    public int amplifier;

    public EnergyBoostItem(IdTagMatcher edible, int duration, int amplifier) {
        this.edible = edible;
        this.duration = duration;
        this.amplifier = amplifier;
    }

    public void tryApply(Player player, ItemStack stack) {
        if (!this.edible.matchesItem(stack.getItem()))
            return;

        int duration;
        MobEffectInstance mobEffectInstance = player.getEffect(Tiredness.ENERGY_BOOST.get());
        if (mobEffectInstance == null)
            duration = 0;
        else if (mobEffectInstance.isInfiniteDuration())
            return;
        else
            duration = mobEffectInstance.duration;
        if (this.duration == 0) {
            FoodProperties food = stack.getFoodProperties(player);
            //noinspection ConstantConditions .getFoodProperties() == null is checked
            duration += (int) (MCUtils.getFoodEffectiveness(food) * 20 * Tiredness.defaultEnergyBoostDurationMultiplier);
        }
        else {
            duration += this.duration;
        }

        player.addEffect(MCUtils.createEffectInstance(Tiredness.ENERGY_BOOST.get(), duration, this.amplifier, false, false, true, false));
    }

    public static final java.lang.reflect.Type LIST_TYPE = new TypeToken<ArrayList<EnergyBoostItem>>(){}.getType();
    public static class Serializer implements JsonDeserializer<EnergyBoostItem>, JsonSerializer<EnergyBoostItem> {
        @Override
        public EnergyBoostItem deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            IdTagMatcher edible = context.deserialize(json.getAsJsonObject().get("edible"), IdTagMatcher.class);
            return new EnergyBoostItem(edible, GsonHelper.getAsInt(json.getAsJsonObject(), "duration", 0), GsonHelper.getAsInt(json.getAsJsonObject(), "amplifier", 0));
        }

        @Override
        public JsonElement serialize(EnergyBoostItem src, java.lang.reflect.Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.add("edible", context.serialize(src.edible));
            if (src.duration > 0)
                jsonObject.addProperty("duration", src.duration);
            if (src.amplifier > 0)
                jsonObject.addProperty("amplifier", src.amplifier);

            return jsonObject;
        }
    }

}