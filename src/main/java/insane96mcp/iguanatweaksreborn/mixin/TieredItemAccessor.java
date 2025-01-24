package insane96mcp.iguanatweaksreborn.mixin;

import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TieredItem.class)
public interface TieredItemAccessor {
    @Accessor
    @Mutable
    void setTier(Tier tier);
}
