package insane96mcp.iguanatweaksreborn.data.generator;

import insane96mcp.iguanatweaksreborn.InsaneSurvivalTweaks;
import insane96mcp.iguanatweaksreborn.module.combat.PiercingDamage;
import insane96mcp.iguanatweaksreborn.module.misc.tweaks.Tweaks;
import insane96mcp.iguanatweaksreborn.module.sleeprespawn.death.Death;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.DamageTypeTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ITRDamageTypeTagsProvider extends DamageTypeTagsProvider {

    public ITRDamageTypeTagsProvider(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pLookupProvider, String modId, @Nullable ExistingFileHelper existingFileHelper) {
        super(pOutput, pLookupProvider, modId, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        tag(DamageTypeTags.BYPASSES_ARMOR).add(Tweaks.COLLIDE_WITH_WALL).addTag(PiercingDamage.PIERCING_DAMAGE_TYPE);
        tag(DamageTypeTags.BYPASSES_COOLDOWN).add(Tweaks.COLLIDE_WITH_WALL).addTag(PiercingDamage.PIERCING_DAMAGE_TYPE);
        tag(DamageTypeTags.BYPASSES_SHIELD).add(Tweaks.COLLIDE_WITH_WALL).addTag(PiercingDamage.PIERCING_DAMAGE_TYPE);
        tag(DamageTypeTags.NO_IMPACT).add(Tweaks.COLLIDE_WITH_WALL).addTag(PiercingDamage.PIERCING_DAMAGE_TYPE).add(DamageTypes.WITHER);

        tag(Death.DOESNT_SPAWN_GRAVE).add(DamageTypes.FELL_OUT_OF_WORLD);

        tag(PiercingDamage.PIERCING_DAMAGE_TYPE).add(PiercingDamage.PIERCING_MOB_ATTACK, PiercingDamage.PIERCING_PLAYER_ATTACK);
        tag(PiercingDamage.DOESNT_TRIGGER_PIERCING).addTag(PiercingDamage.PIERCING_DAMAGE_TYPE);
    }

    public static TagKey<DamageType> create(String tagName) {
        return TagKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(InsaneSurvivalTweaks.MOD_ID, tagName));
    }
}
