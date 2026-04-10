package insane96mcp.insanesurvivaloverhaul.data.condition;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import insane96mcp.insanesurvivaloverhaul.module.farming.livestock.Livestock;
import insane96mcp.insanesurvivaloverhaul.setup.ISORegistries;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public record LivestockAgeCondition(LootContext.EntityTarget entityTarget, Livestock.Age age) implements LootItemCondition {

    public static final MapCodec<LivestockAgeCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            LootContext.EntityTarget.CODEC.fieldOf("entity").forGetter(LivestockAgeCondition::entityTarget),
            Livestock.Age.CODEC.fieldOf("age").forGetter(LivestockAgeCondition::age)
    ).apply(instance, LivestockAgeCondition::new));

    @Override
    public @NotNull LootItemConditionType getType() {
        return ISORegistries.LIVESTOCK_AGE_CONDITION.get();
    }

    @Override
    public @NotNull Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(this.entityTarget.getParam());
    }

    @Override
    public boolean test(LootContext context) {
        Entity entity = context.getParamOrNull(this.entityTarget.getParam());
        if (!(entity instanceof AgeableMob mob))
            return false;
        return Livestock.getAge(mob) == this.age;
    }
}
