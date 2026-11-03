package insane96mcp.insanesurvivaloverhaul.data.generator;

import insane96mcp.insanelib.data.FeatureEnabledCondition;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.data.criterion.OverweightPouchCarryTrigger;
import insane96mcp.insanesurvivaloverhaul.data.criterion.UnfairOneShotTrigger;
import insane96mcp.insanesurvivaloverhaul.module.combat.fletching.FletchingFeature;
import insane96mcp.insanesurvivaloverhaul.module.combat.unfaironeshot.UnfairOneShot;
import insane96mcp.insanesurvivaloverhaul.module.items.Recall;
import insane96mcp.insanesurvivaloverhaul.module.items.pouch.Pouch;
import insane96mcp.insanesurvivaloverhaul.module.items.repairkit.RepairKits;
import insane96mcp.insanesurvivaloverhaul.module.misc.glowblock.GlowBlockFeature;
import insane96mcp.insanesurvivaloverhaul.module.sleep.tiredness.Tiredness;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.neoforged.neoforge.common.conditions.WithConditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ISOAdvancementProvider implements DataProvider {
    private static final AdvancementHolder ADVENTURE_ROOT = new AdvancementHolder(ResourceLocation.withDefaultNamespace("adventure/root"), null);
    private static final AdvancementHolder BREW_POTION = new AdvancementHolder(ResourceLocation.withDefaultNamespace("nether/brew_potion"), null);

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

        AdvancementHolder obtainPouch = Advancement.Builder.advancement()
                .display(new DisplayInfo(
                        new ItemStack(Pouch.ITEM.get()),
                        Component.translatable("advancements.insanesurvivaloverhaul.obtain_pouch.title"),
                        Component.translatable("advancements.insanesurvivaloverhaul.obtain_pouch.description"),
                        Optional.empty(),
                        AdvancementType.TASK,
                        true, true, false))
                .parent(ADVENTURE_ROOT)
                .addCriterion("has_pouch", InventoryChangeTrigger.TriggerInstance.hasItems(Pouch.ITEM.get()))
                .build(InsaneSO.id("story/obtain_pouch"));
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
                .build(InsaneSO.id("story/overweight_pouch"));
        save(output, registries, futures, overweightPouch, pouchEnabled);

        FeatureEnabledCondition glowBlockEnabled = new FeatureEnabledCondition("Glow block");

        AdvancementHolder obtainGlowBlock = Advancement.Builder.advancement()
                .display(new DisplayInfo(
                        new ItemStack(GlowBlockFeature.GLOW_BLOCK.item().get()),
                        Component.translatable("advancements.insanesurvivaloverhaul.obtain_glow_block.title"),
                        Component.translatable("advancements.insanesurvivaloverhaul.obtain_glow_block.description"),
                        Optional.empty(),
                        AdvancementType.TASK,
                        true, true, false))
                .parent(ADVENTURE_ROOT)
                .addCriterion("has_glow_block", InventoryChangeTrigger.TriggerInstance.hasItems(GlowBlockFeature.GLOW_BLOCK.item().get()))
                .build(InsaneSO.id("story/obtain_glow_block"));
        save(output, registries, futures, obtainGlowBlock, glowBlockEnabled);

        FeatureEnabledCondition fletchingEnabled = new FeatureEnabledCondition("Fletching");

        AdvancementHolder fletchingTable = Advancement.Builder.advancement()
                .display(new DisplayInfo(
                        new ItemStack(Items.FLETCHING_TABLE),
                        Component.translatable("advancements.insanesurvivaloverhaul.fletching_table.title"),
                        Component.translatable("advancements.insanesurvivaloverhaul.fletching_table.description"),
                        Optional.empty(),
                        AdvancementType.TASK,
                        true, true, false))
                .parent(ADVENTURE_ROOT)
                .addCriterion("fletching_table", InventoryChangeTrigger.TriggerInstance.hasItems(Items.FLETCHING_TABLE))
                .build(InsaneSO.id("story/fletching_table"));
        save(output, registries, futures, fletchingTable, fletchingEnabled);

        arrowAdvancement(output, registries, futures, fletchingTable, fletchingEnabled,
                "torch_arrow", FletchingFeature.TORCH_ARROW_ITEM.get());
        arrowAdvancement(output, registries, futures, fletchingTable, fletchingEnabled,
                "explosive_arrow", FletchingFeature.EXPLOSIVE_ARROW_ITEM.get());
        arrowAdvancement(output, registries, futures, fletchingTable, fletchingEnabled,
                "quartz_arrow", FletchingFeature.QUARTZ_ARROW_ITEM.get());
        arrowAdvancement(output, registries, futures, fletchingTable, fletchingEnabled,
                "diamond_arrow", FletchingFeature.DIAMOND_ARROW_ITEM.get());
        arrowAdvancement(output, registries, futures, fletchingTable, fletchingEnabled,
                "ice_arrow", FletchingFeature.ICE_ARROW_ITEM.get());

        FeatureEnabledCondition repairKitsEnabled = new FeatureEnabledCondition("Repair Kits");

        AdvancementHolder repairKit = Advancement.Builder.advancement()
                .display(new DisplayInfo(
                        new ItemStack(RepairKits.ITEM.get()),
                        Component.translatable("advancements.insanesurvivaloverhaul.repair_kit.title"),
                        Component.translatable("advancements.insanesurvivaloverhaul.repair_kit.description"),
                        Optional.empty(),
                        AdvancementType.TASK,
                        true, true, false))
                .parent(ADVENTURE_ROOT)
                .addCriterion("repair_kit", InventoryChangeTrigger.TriggerInstance.hasItems(RepairKits.ITEM.get()))
                .build(InsaneSO.id("story/repair_kit"));
        save(output, registries, futures, repairKit, repairKitsEnabled);

        FeatureEnabledCondition unfairOneShotEnabled = new FeatureEnabledCondition("Unfair one-shot");

        AdvancementHolder unfairOneshot = Advancement.Builder.advancement()
                .display(new DisplayInfo(
                        new ItemStack(UnfairOneShot.HALF_HEART_TEXTURE.get()),
                        Component.translatable("advancements.insanesurvivaloverhaul.unfair_oneshot.title"),
                        Component.translatable("advancements.insanesurvivaloverhaul.unfair_oneshot.description"),
                        Optional.empty(),
                        AdvancementType.TASK,
                        true, true, true))
                .parent(ADVENTURE_ROOT)
                .addCriterion("unfair_oneshot", UnfairOneShot.UNFAIR_ONESHOT.get().createCriterion(
                        new UnfairOneShotTrigger.TriggerInstance(Optional.empty())))
                .build(InsaneSO.id("adventure/unfair_oneshot"));
        save(output, registries, futures, unfairOneshot, unfairOneShotEnabled);

        FeatureEnabledCondition tirednessEnabled = new FeatureEnabledCondition("Tiredness");

        AdvancementHolder tiredX = Advancement.Builder.advancement()
                .display(new DisplayInfo(
                        new ItemStack(Items.PHANTOM_MEMBRANE),
                        Component.translatable("advancements.insanesurvivaloverhaul.tired_x.title"),
                        Component.translatable("advancements.insanesurvivaloverhaul.tired_x.description"),
                        Optional.empty(),
                        AdvancementType.TASK,
                        true, true, true))
                .parent(ADVENTURE_ROOT)
                .addCriterion("tired_x", EffectsChangedTrigger.TriggerInstance.hasEffects(
                        MobEffectsPredicate.Builder.effects().and(Tiredness.TIRED,
                                new MobEffectsPredicate.MobEffectInstancePredicate(
                                        MinMaxBounds.Ints.atLeast(9), MinMaxBounds.Ints.ANY, Optional.empty(), Optional.empty()))))
                .build(InsaneSO.id("adventure/tired_x"));
        save(output, registries, futures, tiredX, tirednessEnabled);

        AdvancementHolder recallPotion = Advancement.Builder.advancement()
                .display(new DisplayInfo(
                        PotionContents.createItemStack(Items.POTION, Recall.RECALL),
                        Component.translatable("advancements.insanesurvivaloverhaul.recall_potion.title"),
                        Component.translatable("advancements.insanesurvivaloverhaul.recall_potion.description"),
                        Optional.empty(),
                        AdvancementType.TASK,
                        true, true, false))
                .parent(BREW_POTION)
                .addCriterion("recall_potion", InventoryChangeTrigger.TriggerInstance.hasItems(
                        ItemPredicate.Builder.item().of(Items.POTION)
                                .withSubPredicate(ItemSubPredicates.POTIONS, new ItemPotionsPredicate(HolderSet.direct(Recall.RECALL)))))
                .build(InsaneSO.id("nether/recall_potion"));
        save(output, registries, futures, recallPotion, new FeatureEnabledCondition("Recall Potion"));
    }

    private void arrowAdvancement(CachedOutput output, HolderLookup.Provider registries, List<CompletableFuture<?>> futures,
                                   AdvancementHolder parent, FeatureEnabledCondition condition, String name, Item item) {
        AdvancementHolder advancement = Advancement.Builder.advancement()
                .display(new DisplayInfo(
                        new ItemStack(item),
                        Component.translatable("advancements.insanesurvivaloverhaul." + name + ".title"),
                        Component.translatable("advancements.insanesurvivaloverhaul." + name + ".description"),
                        Optional.empty(),
                        AdvancementType.TASK,
                        true, true, false))
                .parent(parent)
                .addCriterion(name, InventoryChangeTrigger.TriggerInstance.hasItems(item))
                .build(InsaneSO.id("story/" + name));
        save(output, registries, futures, advancement, condition);
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
