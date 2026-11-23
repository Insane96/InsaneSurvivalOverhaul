package insane96mcp.insanesurvivaloverhaul.mixin.accessor;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PrimedTnt.class)
public interface PrimedTntAccessor {
    /**
     * Sets the entity that ignited the TNT, as the only vanilla constructor that sets it also forces the TNT entity type
     */
    @Accessor("owner")
    void insanesurvivaloverhaul$setOwner(LivingEntity owner);
}
