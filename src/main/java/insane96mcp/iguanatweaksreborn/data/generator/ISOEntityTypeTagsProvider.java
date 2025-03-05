package insane96mcp.iguanatweaksreborn.data.generator;

import insane96mcp.iguanatweaksreborn.InsaneSurvivalOverhaul;
import insane96mcp.iguanatweaksreborn.module.experience.enchantments.enchantment.damage.BaneOfSSSS;
import insane96mcp.iguanatweaksreborn.module.experience.enchantments.enchantment.damage.Smite;
import insane96mcp.iguanatweaksreborn.module.mobs.MiscMobs;
import insane96mcp.iguanatweaksreborn.module.world.explosionoverhaul.ExplosionOverhaul;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ISOEntityTypeTagsProvider extends EntityTypeTagsProvider {
    public static final TagKey<EntityType<?>> WOLVES = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(InsaneSurvivalOverhaul.MOD_ID, "wolves"));
    public static final TagKey<EntityType<?>> CATS = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(InsaneSurvivalOverhaul.MOD_ID, "cats"));
    public static final TagKey<EntityType<?>> PARROTS = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(InsaneSurvivalOverhaul.MOD_ID, "parrots"));
    public static final TagKey<EntityType<?>> PETS = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(InsaneSurvivalOverhaul.MOD_ID, "pets"));

    public ISOEntityTypeTagsProvider(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pProvider, String modId, @Nullable ExistingFileHelper existingFileHelper) {
        super(pOutput, pProvider, modId, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider pProvider) {
        tag(BaneOfSSSS.AFFECTED_BY_BANE_OF_SSSSS)
                .add(EntityType.SPIDER, EntityType.CAVE_SPIDER, EntityType.SILVERFISH, EntityType.CREEPER)
                .addOptional(new ResourceLocation("caverns_and_chasms:deeper")).addOptional(new ResourceLocation("caverns_and_chasms:peeper"));
        tag(Smite.AFFECTED_BY_SMITE)
                .add(EntityType.ZOMBIE, EntityType.HUSK, EntityType.SKELETON, EntityType.STRAY, EntityType.WITHER_SKELETON, EntityType.WITHER, EntityType.SKELETON_HORSE, EntityType.ZOMBIE_HORSE, EntityType.ZOMBIE_VILLAGER, EntityType.ZOMBIFIED_PIGLIN, EntityType.ZOGLIN, EntityType.PHANTOM)
                .addOptional(new ResourceLocation("savage_and_ravage:skeleton_villager"))
                .addOptional(new ResourceLocation("pet_cemetery:zombie_cat"))
                .addOptional(new ResourceLocation("pet_cemetery:zombie_parrot"))
                .addOptional(new ResourceLocation("pet_cemetery:zombie_wolf"))
                .addOptional(new ResourceLocation("pet_cemetery:skeleton_cat"))
                .addOptional(new ResourceLocation("pet_cemetery:skeleton_parrot"))
                .addOptional(new ResourceLocation("pet_cemetery:skeleton_wolf"));
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
        tag(ExplosionOverhaul.ENTITY_BLACKLIST)
                .addOptional(new ResourceLocation("caverns_and_chasms:tmt"));
        tag(MiscMobs.PASSIVE_REGEN)
                .addTag(PETS)
                .add(EntityType.VILLAGER)
                .addTag(EntityTypeTags.RAIDERS);
    }
}
