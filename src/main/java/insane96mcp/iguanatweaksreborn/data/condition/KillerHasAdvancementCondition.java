package insane96mcp.iguanatweaksreborn.data.condition;


import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import insane96mcp.iguanatweaksreborn.setup.ISTRegistries;
import insane96mcp.insanelib.util.MCUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

import java.util.Set;

public class KillerHasAdvancementCondition implements LootItemCondition {

    final ResourceLocation advancement;

    KillerHasAdvancementCondition(ResourceLocation advancement) {
        this.advancement = advancement;
    }

    public LootItemConditionType getType() {
        return ISTRegistries.KILLER_HAS_ADVANCEMENT.get();
    }

    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.LAST_DAMAGE_PLAYER);
    }

    public boolean test(LootContext context) {
        if (!context.hasParam(LootContextParams.LAST_DAMAGE_PLAYER))
            return false;

        ServerPlayer player = (ServerPlayer) context.getParam(LootContextParams.LAST_DAMAGE_PLAYER);
        return MCUtils.isAdvancementDone(player, this.advancement);
    }

    public static Builder advancementCompleted(ResourceLocation advancement) {
        return () -> new KillerHasAdvancementCondition(advancement);
    }

    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<KillerHasAdvancementCondition> {
        @Override
        public void serialize(JsonObject jsonObject, KillerHasAdvancementCondition killerHasAdvancementCondition, JsonSerializationContext context) {
            jsonObject.addProperty("advancement", killerHasAdvancementCondition.advancement.toString());
        }

        @Override
        public KillerHasAdvancementCondition deserialize(JsonObject jsonObject, JsonDeserializationContext context) {
            return new KillerHasAdvancementCondition(ResourceLocation.tryParse(GsonHelper.getAsString(jsonObject, "advancement")));
        }
    }
}