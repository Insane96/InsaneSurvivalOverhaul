package insane96mcp.iguanatweaksreborn.data.condition;


import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import insane96mcp.iguanatweaksreborn.module.sleeprespawn.death.Death;
import insane96mcp.iguanatweaksreborn.setup.ISORegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

import java.util.Set;
import java.util.UUID;

public class KilledByKilledPlayerCondition implements LootItemCondition {

    final LootContext.EntityTarget entityTarget;

    KilledByKilledPlayerCondition(LootContext.EntityTarget entityTarget) {
        this.entityTarget = entityTarget;
    }

    public LootItemConditionType getType() {
        return ISORegistries.KILLED_BY_KILLED_PLAYER.get();
    }

    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.LAST_DAMAGE_PLAYER, this.entityTarget.getParam());
    }

    public boolean test(LootContext context) {
        Entity entity = context.getParamOrNull(this.entityTarget.getParam());
        if (!context.hasParam(LootContextParams.LAST_DAMAGE_PLAYER) || entity == null)
            return false;

        if (!entity.getPersistentData().contains(Death.KILLED_PLAYER))
            return false;
        UUID killedPlayer = entity.getPersistentData().getUUID(Death.KILLED_PLAYER);
        ServerPlayer player = (ServerPlayer) context.getParam(LootContextParams.LAST_DAMAGE_PLAYER);
        return player.getUUID().equals(killedPlayer);
    }

    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<KilledByKilledPlayerCondition> {
        @Override
        public void serialize(JsonObject jsonObject, KilledByKilledPlayerCondition killedByKilledPlayerCondition, JsonSerializationContext context) {
            jsonObject.add("entity", context.serialize(killedByKilledPlayerCondition.entityTarget));
        }

        @Override
        public KilledByKilledPlayerCondition deserialize(JsonObject jsonObject, JsonDeserializationContext context) {
            return new KilledByKilledPlayerCondition(GsonHelper.getAsObject(jsonObject, "entity", context, LootContext.EntityTarget.class));
        }
    }
}