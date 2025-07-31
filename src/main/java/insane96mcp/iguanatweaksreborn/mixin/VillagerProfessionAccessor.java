package insane96mcp.iguanatweaksreborn.mixin;

import com.google.common.collect.ImmutableSet;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(VillagerProfession.class)
public interface VillagerProfessionAccessor {
	@Accessor
	@Mutable
	void setRequestedItems(ImmutableSet<Item> requestedItems);
}
