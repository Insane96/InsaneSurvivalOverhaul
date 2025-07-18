package insane96mcp.iguanatweaksreborn.data.condition;


import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import insane96mcp.iguanatweaksreborn.setup.ISORegistries;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import sereneseasons.api.season.Season;
import sereneseasons.api.season.SeasonHelper;

import java.util.List;
import java.util.Set;

public class LootItemCurrentSeasonCondition implements LootItemCondition {
    final List<Season> season;

    LootItemCurrentSeasonCondition(List<Season> season) {
        this.season = season;
    }

    public LootItemConditionType getType() {
        return ISORegistries.CURRENT_SEASON.get();
    }

    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.THIS_ENTITY);
    }

    public boolean test(LootContext context) {
        for (Season s : this.season) {
            if (SeasonHelper.getSeasonState(context.getLevel()).getSeason().equals(s))
                return true;
        }
        return false;
    }

    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<LootItemCurrentSeasonCondition> {
        @Override
        public void serialize(JsonObject jsonObject, LootItemCurrentSeasonCondition lootItemCurrentSeasonCondition, JsonSerializationContext context) {
            if (lootItemCurrentSeasonCondition.season.size() > 1) {
                JsonArray jArray = new JsonArray();
                for (Season s : lootItemCurrentSeasonCondition.season) {
                    jArray.add(s.name());
                }
                jsonObject.add("season", jArray);
            }
            else
                jsonObject.addProperty("season", lootItemCurrentSeasonCondition.season.get(0).name());
        }

        @Override
        public LootItemCurrentSeasonCondition deserialize(JsonObject jObject, JsonDeserializationContext context) {
            if (!jObject.get("season").isJsonArray()) {
                Season s = Enum.valueOf(Season.class, GsonHelper.getAsString(jObject, "season"));
                return new LootItemCurrentSeasonCondition(List.of(s));
            }
            else {
                JsonArray jArray = jObject.getAsJsonArray("season");
                Season[] seasons = new Season[jArray.size()];
                for (int i = 0; i < jArray.size(); i++) {
                    seasons[i] = Enum.valueOf(Season.class, jArray.get(i).getAsString());
                }
                List<Season> seasonList = List.of(seasons);
                return new LootItemCurrentSeasonCondition(seasonList);
            }
        }
    }
}