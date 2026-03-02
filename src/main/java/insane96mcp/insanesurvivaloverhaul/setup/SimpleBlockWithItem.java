package insane96mcp.insanesurvivaloverhaul.setup;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.function.Supplier;

public record SimpleBlockWithItem(DeferredHolder<Block, ?> block, DeferredHolder<Item, BlockItem> item) {
    public static SimpleBlockWithItem register(String id, Supplier<Block> blockSupplier) {
        return register(id, blockSupplier, new Item.Properties());
    }

    public static SimpleBlockWithItem register(String id, Supplier<Block> blockSupplier, Item.Properties itemProperties) {
        DeferredHolder<Block, ?> block = ISORegistries.BLOCKS.register(id, blockSupplier);
        DeferredHolder<Item, BlockItem> item = ISORegistries.ITEMS.register(id, () -> new BlockItem(block.get(), itemProperties));
        return new SimpleBlockWithItem(block, item);
    }
}
