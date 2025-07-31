package insane96mcp.iguanatweaksreborn.mixin;

import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(Villager.class)
public interface VillagerAccessor {
	@Accessor("WANTED_ITEMS")
	@Mutable
	static void setWantedItems(Set<Item> requestedItems) {
		throw new AssertionError();
	}
}
