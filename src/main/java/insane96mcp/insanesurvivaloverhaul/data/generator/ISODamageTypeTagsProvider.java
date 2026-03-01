package insane96mcp.insanesurvivaloverhaul.data.generator;

import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.combat.PiercingDamage;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.DamageTypeTagsProvider;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class ISODamageTypeTagsProvider extends DamageTypeTagsProvider {

    public ISODamageTypeTagsProvider(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pLookupProvider, String modId, @Nullable ExistingFileHelper existingFileHelper) {
        super(pOutput, pLookupProvider, modId, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        tag(DamageTypeTags.BYPASSES_ARMOR)/*.add(Tweaks.COLLIDE_WITH_WALL)*/.addTag(PiercingDamage.PIERCING_DAMAGE_TYPE)/*.add(Livestock.OLD_AGE)*/;
        tag(DamageTypeTags.BYPASSES_COOLDOWN)/*.add(Tweaks.COLLIDE_WITH_WALL)*/.addTag(PiercingDamage.PIERCING_DAMAGE_TYPE)/*.add(Livestock.OLD_AGE)*/;
        tag(DamageTypeTags.BYPASSES_SHIELD)/*.add(Tweaks.COLLIDE_WITH_WALL)*/.addTag(PiercingDamage.PIERCING_DAMAGE_TYPE)/*.add(Livestock.OLD_AGE)*/;
        tag(DamageTypeTags.NO_IMPACT)/*.add(Tweaks.COLLIDE_WITH_WALL)*/.addTag(PiercingDamage.PIERCING_DAMAGE_TYPE).add(DamageTypes.WITHER)/*.add(Livestock.OLD_AGE)*/;

        tag(PiercingDamage.PIERCING_DAMAGE_TYPE).add(PiercingDamage.PIERCING_MOB_ATTACK, PiercingDamage.PIERCING_PLAYER_ATTACK);
        tag(PiercingDamage.DOESNT_TRIGGER_PIERCING).addTag(PiercingDamage.PIERCING_DAMAGE_TYPE);
    }

    public static TagKey<DamageType> create(String tagName) {
        return TagKey.create(Registries.DAMAGE_TYPE, InsaneSO.location(tagName));
    }
}
