package insane96mcp.insanesurvivaloverhaul.data.generator;

import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.farming.livestock.Livestock;
import insane96mcp.insanesurvivaloverhaul.module.mobs.MiscMobs;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class ISOEntityTypeTagsProvider extends EntityTypeTagsProvider {
    public static final TagKey<EntityType<?>> WOLVES = TagKey.create(Registries.ENTITY_TYPE, InsaneSO.id("wolves"));
    public static final TagKey<EntityType<?>> CATS = TagKey.create(Registries.ENTITY_TYPE, InsaneSO.id("cats"));
    public static final TagKey<EntityType<?>> PARROTS = TagKey.create(Registries.ENTITY_TYPE, InsaneSO.id("parrots"));
    public static final TagKey<EntityType<?>> PETS = TagKey.create(Registries.ENTITY_TYPE, InsaneSO.id("pets"));

    public ISOEntityTypeTagsProvider(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pProvider, String modId, @Nullable ExistingFileHelper existingFileHelper) {
        super(pOutput, pProvider, modId, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider pProvider) {
        tag(WOLVES)
                .add(EntityType.WOLF)
                .addOptional(ResourceLocation.fromNamespaceAndPath("pet_cemetery", "zombie_wolf"))
                .addOptional(ResourceLocation.fromNamespaceAndPath("pet_cemetery", "skeleton_wolf"));
        tag(CATS)
                .add(EntityType.CAT)
                .addOptional(ResourceLocation.fromNamespaceAndPath("pet_cemetery", "zombie_cat"))
                .addOptional(ResourceLocation.fromNamespaceAndPath("pet_cemetery", "skeleton_cat"));
        tag(PARROTS)
                .add(EntityType.CAT)
                .addOptional(ResourceLocation.fromNamespaceAndPath("pet_cemetery", "zombie_parrot"))
                .addOptional(ResourceLocation.fromNamespaceAndPath("pet_cemetery", "skeleton_parrot"));
        tag(PETS)
                .add(EntityType.FOX, EntityType.OCELOT)
                .addTag(WOLVES)
                .addTag(CATS)
                .addTag(PARROTS);
        tag(MiscMobs.PASSIVE_REGEN)
                .addTag(PETS)
                .add(EntityType.VILLAGER)
                .addTag(EntityTypeTags.RAIDERS);
        tag(Livestock.MILKABLE)
                .add(EntityType.COW)
                .add(EntityType.MOOSHROOM)
                .add(EntityType.GOAT)
                .addOptional(ResourceLocation.fromNamespaceAndPath("buzzier_bees", "moobloom"))
                .addOptional(ResourceLocation.fromNamespaceAndPath("environmental", "yak"));
        tag(Livestock.PREVENT_BREEDING)
                .add(EntityType.COW)
                .add(EntityType.MOOSHROOM)
                .add(EntityType.PIG)
                .add(EntityType.HORSE)
                .add(EntityType.DONKEY)
                .add(EntityType.GOAT)
                .add(EntityType.SHEEP)
                .add(EntityType.WOLF)
                .add(EntityType.CAT)
                .add(EntityType.AXOLOTL)
                .add(EntityType.LLAMA)
                .add(EntityType.RABBIT)
                .add(EntityType.TURTLE)
                .add(EntityType.PANDA)
                .add(EntityType.FOX)
                .add(EntityType.STRIDER)
                .add(EntityType.HOGLIN)
                .add(EntityType.FROG)
                .addOptional(ResourceLocation.fromNamespaceAndPath("quark", "crab"))
                .addOptional(ResourceLocation.fromNamespaceAndPath("autumnity", "snail"))
                .addOptional(ResourceLocation.fromNamespaceAndPath("autumnity", "turkey"))
                .addOptional(ResourceLocation.fromNamespaceAndPath("environmental", "duck"))
                .addOptional(ResourceLocation.fromNamespaceAndPath("environmental", "deer"))
                .addOptional(ResourceLocation.fromNamespaceAndPath("environmental", "reindeer"))
                .addOptional(ResourceLocation.fromNamespaceAndPath("environmental", "slabfish"))
                .addOptional(ResourceLocation.fromNamespaceAndPath("environmental", "yak"));
    }
}
