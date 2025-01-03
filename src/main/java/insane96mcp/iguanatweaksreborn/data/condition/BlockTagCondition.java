package insane96mcp.iguanatweaksreborn.data.condition;


import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import insane96mcp.iguanatweaksreborn.setup.ISORegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

import java.util.Set;

public class BlockTagCondition implements LootItemCondition {
    final TagKey<Block> blockTag;

    BlockTagCondition(TagKey<Block> blockTag) {
        this.blockTag = blockTag;
    }

    BlockTagCondition(ResourceLocation blockTag) {
        this.blockTag = TagKey.create(Registries.BLOCK, blockTag);
    }

    public LootItemConditionType getType() {
        return ISORegistries.BLOCK_TAG_MATCH.get();
    }

    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.BLOCK_STATE);
    }

    public boolean test(LootContext context) {
        if (context.hasParam(LootContextParams.BLOCK_STATE))
            return context.getParam(LootContextParams.BLOCK_STATE).is(this.blockTag);
        return true;
    }

    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<BlockTagCondition> {
        @Override
        public void serialize(JsonObject jsonObject, BlockTagCondition blockTagCondition, JsonSerializationContext context) {
            jsonObject.addProperty("block_tag", blockTagCondition.blockTag.location().toString());
        }

        @Override
        public BlockTagCondition deserialize(JsonObject jsonObject, JsonDeserializationContext context) {
            String s =  GsonHelper.getAsString(jsonObject, "block_tag");
            ResourceLocation rl = ResourceLocation.tryParse(s);
            if (rl == null)
                throw new JsonParseException("Failed to parse block_tag for block_tag_match condition");
            return new BlockTagCondition(rl);
        }
    }
}