package insane96mcp.insanesurvivaloverhaul.module.misc;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanesurvivaloverhaul.data.generator.ISOItemTagsProvider;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

@LoadFeature(module = ISOModules.MISC, canBeDisabled = false)
public class CreativeRemoval extends Feature {
	public static final TagKey<Item> ITEM_TAG = ISOItemTagsProvider.create("creative_removal");
}