package insane96mcp.iguanatweaksreborn.module.mobs;

import insane96mcp.iguanatweaksreborn.InsaneSurvivalOverhaul;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Misc Mobs")
@LoadFeature(module = Modules.Ids.MOBS)
public class MiscMobs extends Feature {
    public static final TagKey<EntityType<?>> PASSIVE_REGEN = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(InsaneSurvivalOverhaul.MOD_ID, "passive_regen"));

    @Config(min = 0)
    @Label(description = "1 in X chance each tick for mobs in the `iguanatweaksreborn:passive_regen` entity type tag to regain 1 health. Set to 0 to disable")
    public static Integer passiveRegenChance = 200;

    @Config
    @Label(description = "Instead of dealing magic and melee damage, guardians now only deal magic damage equal to their attack_damage attribute")
    public static Boolean guardianMagicDamageOnly = true;

    public MiscMobs(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }

    @SubscribeEvent
    public void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (!this.isEnabled()
                || passiveRegenChance == 0
                || !event.getEntity().getType().is(PASSIVE_REGEN))
            return;

        if (event.getEntity().getRandom().nextInt(passiveRegenChance) == 0)
            event.getEntity().heal(1f);
    }
}
