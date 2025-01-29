package insane96mcp.iguanatweaksreborn.data.generator;

import insane96mcp.iguanatweaksreborn.module.experience.enchantments.enchantment.damage.BaneOfSSSS;
import insane96mcp.iguanatweaksreborn.module.experience.enchantments.enchantment.damage.Smite;
import insane96mcp.iguanatweaksreborn.module.world.explosionoverhaul.ExplosionOverhaul;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ISOEntityTypeTagsProvider extends EntityTypeTagsProvider {

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
                .addOptional(new ResourceLocation("pet_cemetery:zombie_cat")).addOptional(new ResourceLocation("pet_cemetery:zombie_parrot")).addOptional(new ResourceLocation("pet_cemetery:zombie_wolf")).addOptional(new ResourceLocation("pet_cemetery:skeleton_cat")).addOptional(new ResourceLocation("pet_cemetery:skeleton_parrot")).addOptional(new ResourceLocation("pet_cemetery:skeleton_wolf"));
        tag(ExplosionOverhaul.ENTITY_BLACKLIST)
                .addOptional(new ResourceLocation("caverns_and_chasms:tmt"));
    }
}
