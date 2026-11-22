package insane96mcp.insanesurvivaloverhaul.mixin.accessor;

import net.minecraft.world.entity.item.FallingBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FallingBlockEntity.class)
public interface FallingBlockEntityAccessor {
    /**
     * Exposes whether the falling block already hurts entities (e.g. set by Anvils, Pointed Dripstone, commands or other mods)
     */
    @Accessor("hurtEntities")
    boolean insanesurvivaloverhaul$getHurtEntities();
}
