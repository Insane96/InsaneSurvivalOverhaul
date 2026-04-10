package insane96mcp.insanesurvivaloverhaul.data.condition;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.MapCodec;
import insane96mcp.insanesurvivaloverhaul.module.farming.livestock.Livestock;
import insane96mcp.insanesurvivaloverhaul.setup.ISORegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public record FedCondition(LootContext.EntityTarget entityTarget) implements LootItemCondition {

    public static final MapCodec<FedCondition> CODEC = LootContext.EntityTarget.CODEC
            .fieldOf("entity")
            .xmap(FedCondition::new, FedCondition::entityTarget);

    @Override
    public @NotNull LootItemConditionType getType() {
        return ISORegistries.HAS_BEEN_FED_RECENTLY.get();
    }

    @Override
    public @NotNull Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(this.entityTarget.getParam());
    }

    @Override
    public boolean test(LootContext context) {
        Entity entity = context.getParamOrNull(this.entityTarget.getParam());
        return entity instanceof LivingEntity living && Livestock.hasBeenFedRecently(living);
    }
}
