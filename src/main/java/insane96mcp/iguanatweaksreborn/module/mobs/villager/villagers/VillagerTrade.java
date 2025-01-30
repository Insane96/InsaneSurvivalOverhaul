package insane96mcp.iguanatweaksreborn.module.mobs.villager.villagers;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import insane96mcp.iguanatweaksreborn.module.mobs.villager.SerializableTrade;
import net.minecraft.util.GsonHelper;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class VillagerTrade {
    public final int level;
    public final List<SerializableTrade> trades = new ArrayList<>();

    public VillagerTrade(int level) {
        this.level = level;
    }

    public void addTrade(SerializableTrade trade) {
        trades.add(trade);
    }

    public static final Type VILLAGER_TRADE_LIST_TYPE = new TypeToken<ArrayList<VillagerTrade>>(){}.getType();

    public static class Serializer implements JsonDeserializer<VillagerTrade>, JsonSerializer<VillagerTrade> {
        @Override
        public VillagerTrade deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jObject = json.getAsJsonObject();
            VillagerTrade villagerTrade = new VillagerTrade(GsonHelper.getAsInt(jObject, "level"));
            JsonArray trades = GsonHelper.getAsJsonArray(jObject, "trades");
            for (JsonElement trade : trades) {
                villagerTrade.addTrade(context.deserialize(trade, SerializableTrade.class));
            }
            return villagerTrade;
        }

        @Override
        public JsonElement serialize(VillagerTrade src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jObject = new JsonObject();
            jObject.addProperty("level", src.level);
            JsonArray jArray = new JsonArray();
            for (SerializableTrade trade : src.trades) {
                jArray.add(context.serialize(trade));
            }
            jObject.add("trades", jArray);
            return jObject;
        }
    }
}
