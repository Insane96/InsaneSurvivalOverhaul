package insane96mcp.iguanatweaksreborn.data.condition;


import com.google.common.collect.ImmutableSet;
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

import java.util.Set;

public class LootItemCurrentSeasonCondition implements LootItemCondition {
    final Season season;

    LootItemCurrentSeasonCondition(Season season) {
        this.season = season;
    }

    public LootItemConditionType getType() {
        return ISORegistries.CURRENT_SEASON.get();
    }

    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.THIS_ENTITY);
    }

    public boolean test(LootContext context) {
        return SeasonHelper.getSeasonState(context.getLevel()).getSeason().equals(this.season);
    }

    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<LootItemCurrentSeasonCondition> {
        @Override
        public void serialize(JsonObject jsonObject, LootItemCurrentSeasonCondition lootItemCurrentSeasonCondition, JsonSerializationContext context) {
            jsonObject.addProperty("season", lootItemCurrentSeasonCondition.season.name());
        }

        @Override
        public LootItemCurrentSeasonCondition deserialize(JsonObject jsonObject, JsonDeserializationContext context) {
            Season s = Enum.valueOf(Season.class, GsonHelper.getAsString(jsonObject, "season"));
            return new LootItemCurrentSeasonCondition(s);
        }
    }
}