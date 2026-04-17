package insane96mcp.insanesurvivaloverhaul.data.generator;

import insane96mcp.insanelib.data.FeatureEnabledCondition;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.data.criterion.OverweightPouchCarryTrigger;
import insane96mcp.insanesurvivaloverhaul.module.items.pouch.Pouch;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.conditions.WithConditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ISOAdvancementProvider implements DataProvider {
    private final PackOutput.PathProvider pathProvider;
    private final CompletableFuture<HolderLookup.Provider> registries;

    public ISOAdvancementProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        this.pathProvider = output.createRegistryElementsPathProvider(Registries.ADVANCEMENT);
        this.registries = registries;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        return registries.thenCompose(registries -> {
            List<CompletableFuture<?>> futures = new ArrayList<>();
            generate(output, registries, futures);
            return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
        });
    }

    private void generate(CachedOutput output, HolderLookup.Provider registries, List<CompletableFuture<?>> futures) {
        FeatureEnabledCondition pouchEnabled = new FeatureEnabledCondition("Pouch");

        @SuppressWarnings("removal")
        AdvancementHolder obtainPouch = Advancement.Builder.advancement()
                .display(new DisplayInfo(
                        new ItemStack(Pouch.ITEM.get()),
                        Component.translatable("advancements.insanesurvivaloverhaul.obtain_pouch.title"),
                        Component.translatable("advancements.insanesurvivaloverhaul.obtain_pouch.description"),
                        Optional.empty(),
                        AdvancementType.TASK,
                        true, true, false))
                .parent(ResourceLocation.withDefaultNamespace("adventure/root"))
                .addCriterion("has_pouch", InventoryChangeTrigger.TriggerInstance.hasItems(Pouch.ITEM.get()))
                .build(InsaneSO.location("story/obtain_pouch"));
        save(output, registries, futures, obtainPouch, pouchEnabled);

        AdvancementHolder overweightPouch = Advancement.Builder.advancement()
                .display(new DisplayInfo(
                        new ItemStack(Pouch.ITEM.get()),
                        Component.translatable("advancements.insanesurvivaloverhaul.overweight_pouch.title"),
                        Component.translatable("advancements.insanesurvivaloverhaul.overweight_pouch.description"),
                        Optional.empty(),
                        AdvancementType.TASK,
                        true, true, true))
                .parent(obtainPouch)
                .addCriterion("overweight_pouch_carry", Pouch.OVERWEIGHT_POUCH_CARRY.get().createCriterion(
                        new OverweightPouchCarryTrigger.TriggerInstance(Optional.empty())))
                .build(InsaneSO.location("story/overweight_pouch"));
        save(output, registries, futures, overweightPouch, pouchEnabled);
    }

    private void save(CachedOutput output, HolderLookup.Provider registries, List<CompletableFuture<?>> futures,
                      AdvancementHolder holder, FeatureEnabledCondition... conditions) {
        futures.add(DataProvider.saveStable(
                output, registries,
                Advancement.CONDITIONAL_CODEC,
                Optional.of(new WithConditions<>(holder.value(), conditions)),
                pathProvider.json(holder.id())));
    }

    @Override
    public String getName() {
        return "ISO Advancements";
    }
}
