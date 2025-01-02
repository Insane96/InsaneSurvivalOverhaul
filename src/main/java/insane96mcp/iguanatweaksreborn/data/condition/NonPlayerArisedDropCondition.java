package insane96mcp.iguanatweaksreborn.data.condition;


import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import insane96mcp.iguanatweaksreborn.setup.ISTRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

import java.util.Set;

public class NonPlayerArisedDropCondition implements LootItemCondition {

    NonPlayerArisedDropCondition() {

    }

    public LootItemConditionType getType() {
        return ISTRegistries.NON_PLAYER_ARISED_DROP.get();
    }

    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.THIS_ENTITY, LootContextParams.LAST_DAMAGE_PLAYER, LootContextParams.EXPLOSION_RADIUS, LootContextParams.TOOL);
    }

    public boolean test(LootContext context) {
        if (!(context.hasParam(LootContextParams.THIS_ENTITY))
                || !(context.getParam(LootContextParams.THIS_ENTITY) instanceof LivingEntity)
                || context.getParam(LootContextParams.THIS_ENTITY) instanceof Player
                || context.hasParam(LootContextParams.EXPLOSION_RADIUS)
                || context.hasParam(LootContextParams.TOOL))
            return false;

        return !context.hasParam(LootContextParams.LAST_DAMAGE_PLAYER);
    }

    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<NonPlayerArisedDropCondition> {
        @Override
        public void serialize(JsonObject jsonObject, NonPlayerArisedDropCondition lootItemCurrentSeasonCondition, JsonSerializationContext context) {

        }

        @Override
        public NonPlayerArisedDropCondition deserialize(JsonObject jsonObject, JsonDeserializationContext context) {
            return new NonPlayerArisedDropCondition();
        }
    }
}