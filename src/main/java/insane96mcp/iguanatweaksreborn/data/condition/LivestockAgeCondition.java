package insane96mcp.iguanatweaksreborn.data.condition;


import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import insane96mcp.iguanatweaksreborn.module.farming.livestock.Livestock;
import insane96mcp.iguanatweaksreborn.setup.ISORegistries;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class LivestockAgeCondition implements LootItemCondition {

    final LootContext.EntityTarget entityTarget;
    final float minAge;
    final float maxAge;

    LivestockAgeCondition(LootContext.EntityTarget entityTarget, float minAge, float maxAge) {
        this.entityTarget = entityTarget;
        this.minAge = minAge;
        this.maxAge = maxAge;
    }

    public @NotNull LootItemConditionType getType() {
        return ISORegistries.LIVESTOCK_AGE_CONDITION.get();
    }

    public @NotNull Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(this.entityTarget.getParam());
    }

    public boolean test(LootContext context) {
        Entity entity = context.getParamOrNull(this.entityTarget.getParam());
        if (!(entity instanceof AgeableMob mob))
            return false;
        float ageRatio = Livestock.getAgeRatio(mob);
        return ageRatio >= this.minAge && ageRatio < this.maxAge;
    }

    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<LivestockAgeCondition> {
        @Override
        public void serialize(JsonObject jsonObject, LivestockAgeCondition livestockAgeCondition, JsonSerializationContext context) {
            jsonObject.add("entity", context.serialize(livestockAgeCondition.entityTarget));
            jsonObject.addProperty("min_age", livestockAgeCondition.minAge);
            jsonObject.addProperty("max_age", livestockAgeCondition.maxAge);
        }

        @Override
        public LivestockAgeCondition deserialize(JsonObject jsonObject, JsonDeserializationContext context) {
            return new LivestockAgeCondition(GsonHelper.getAsObject(jsonObject, "entity", context, LootContext.EntityTarget.class), GsonHelper.getAsFloat(jsonObject, "min_age",  0f), GsonHelper.getAsFloat(jsonObject, "max_age", 1f));
        }
    }
}