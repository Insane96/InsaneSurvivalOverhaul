package insane96mcp.insanesurvivaloverhaul.module.farming.crops;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

public class SeedsBlockItem extends BlockItem {

    public SeedsBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    public String getDescriptionId() {
        return this.getBlock().getDescriptionId() + ".seeds";
    }
}
