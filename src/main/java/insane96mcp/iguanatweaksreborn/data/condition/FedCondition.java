package insane96mcp.iguanatweaksreborn.data.condition;


import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import insane96mcp.iguanatweaksreborn.module.farming.livestock.Livestock;
import insane96mcp.iguanatweaksreborn.setup.ISORegistries;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class FedCondition implements LootItemCondition {

    final LootContext.EntityTarget entityTarget;

    FedCondition(LootContext.EntityTarget entityTarget) {
        this.entityTarget = entityTarget;
    }

    public @NotNull LootItemConditionType getType() {
        return ISORegistries.HAS_BEEN_FED_RECENTLY.get();
    }

    public @NotNull Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(this.entityTarget.getParam());
    }

    public boolean test(LootContext context) {
        Entity entity = context.getParamOrNull(this.entityTarget.getParam());
        return entity instanceof LivingEntity living && Livestock.hasBeenFedRecently(living);
    }

    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<FedCondition> {
        @Override
        public @NotNull FedCondition deserialize(@NotNull JsonObject jsonObject, @NotNull JsonDeserializationContext context) {
            return new FedCondition(GsonHelper.getAsObject(jsonObject, "entity", context, LootContext.EntityTarget.class));
        }

        @Override
        public void serialize(JsonObject jsonObject, FedCondition livestockAgeCondition, JsonSerializationContext context) {
            jsonObject.add("entity", context.serialize(livestockAgeCondition.entityTarget));
        }
    }
}