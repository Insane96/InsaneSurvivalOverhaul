package insane96mcp.iguanatweaksreborn.data.condition;


import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import insane96mcp.iguanatweaksreborn.setup.ISORegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

import java.util.Set;

public class LivingEntityCondition implements LootItemCondition {

    LivingEntityCondition() {

    }

    public LootItemConditionType getType() {
        return ISORegistries.LIVING_ENTITY.get();
    }

    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.THIS_ENTITY, LootContextParams.EXPLOSION_RADIUS);
    }

    public boolean test(LootContext context) {
        return context.hasParam(LootContextParams.THIS_ENTITY)
                && context.getParam(LootContextParams.THIS_ENTITY) instanceof LivingEntity
                && !context.hasParam(LootContextParams.EXPLOSION_RADIUS);
    }

    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<LivingEntityCondition> {
        @Override
        public void serialize(JsonObject jsonObject, LivingEntityCondition lootItemCurrentSeasonCondition, JsonSerializationContext context) {

        }

        @Override
        public LivingEntityCondition deserialize(JsonObject jsonObject, JsonDeserializationContext context) {
            return new LivingEntityCondition();
        }
    }
}