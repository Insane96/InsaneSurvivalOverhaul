package insane96mcp.insanesurvivaloverhaul.module.misc;

import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanesurvivaloverhaul.data.generator.ISOItemTagsProvider;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

@LoadFeature(module = ISOModules.MISC, canBeDisabled = false)
public class CreativeRemoval {
	public static final TagKey<Item> CREATIVE_REMOVAL = ISOItemTagsProvider.create("creative_removal");
}