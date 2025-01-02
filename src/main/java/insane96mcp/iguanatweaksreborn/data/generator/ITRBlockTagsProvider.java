package insane96mcp.iguanatweaksreborn.data.generator;

import insane96mcp.iguanatweaksreborn.IguanaTweaksReborn;
import insane96mcp.iguanatweaksreborn.module.farming.bonemeal.BoneMeal;
import insane96mcp.iguanatweaksreborn.module.farming.crops.Crops;
import insane96mcp.iguanatweaksreborn.module.farming.hoes.Hoes;
import insane96mcp.iguanatweaksreborn.module.mining.blockhardness.BlockHardness;
import insane96mcp.iguanatweaksreborn.module.misc.beaconconduit.BeaconConduit;
import insane96mcp.iguanatweaksreborn.module.misc.tweaks.Tweaks;
import insane96mcp.iguanatweaksreborn.module.mobs.spawning.Spawning;
import insane96mcp.iguanatweaksreborn.module.movement.minecarts.Minecarts;
import insane96mcp.iguanatweaksreborn.module.sleeprespawn.death.Death;
import insane96mcp.iguanatweaksreborn.module.sleeprespawn.respawn.Respawn;
import insane96mcp.iguanatweaksreborn.module.world.Nether;
import insane96mcp.iguanatweaksreborn.module.world.timber.TimberTrees;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ITRBlockTagsProvider extends BlockTagsProvider {
    public static final TagKey<Block> OBSIDIANS = create("obsidians");
    public static final TagKey<Block> GRASS_BLOCKS = create("grass_blocks");
    public static final TagKey<Block> TALL_GRASS = create("tall_grass");
    public static final TagKey<Block> AZALEA_LEAVES = create("azalea_leaves");
    public static final TagKey<Block> OAK_LOG_LEAVES = create("oak_log_leaves");
    public static final TagKey<Block> MAPLE_LEAVES = create("maple_leaves");
    public static final TagKey<Block> TRUMPET_LEAVES = create("trumpet_leaves");

    public ITRBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, String modId, @Nullable ExistingFileHelper existingFileHelper){
        super(output, lookupProvider, modId, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        //Vanilla Tags
        tag(BlockTags.FALL_DAMAGE_RESETTING)
                .add(Blocks.COBWEB);
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(Death.GRAVE.block().get())
                .add(BeaconConduit.BEACON.block().get())
                .add(Spawning.ECHO_LANTERN.block().get());
        tag(BlockTags.MINEABLE_WITH_SHOVEL)
                .add(BoneMeal.RICH_FARMLAND.block().get());
        tag(BlockTags.RAILS)
                .add(Minecarts.COPPER_POWERED_RAIL.block().get(), Minecarts.GOLDEN_POWERED_RAIL.block().get());

        //Mod's tags
        tag(Respawn.RESPAWN_OBELISK_BLOCKS_TO_ROT)
                .add(Blocks.COBBLESTONE).add(Blocks.MOSSY_COBBLESTONE).add(Blocks.STONE_BRICKS).add(Blocks.STONE_BRICK_SLAB).add(Blocks.SANDSTONE).add(Blocks.SANDSTONE_SLAB).add(Blocks.ORANGE_TERRACOTTA).add(Blocks.COBBLESTONE_SLAB).add(Blocks.SNOW_BLOCK).add(Blocks.SNOW).add(Blocks.BLUE_ICE).add(Blocks.DEEPSLATE_BRICKS).add(Blocks.DEEPSLATE_BRICK_SLAB).add(Blocks.COBBLED_DEEPSLATE).add(Blocks.BIRCH_PLANKS).add(Blocks.BIRCH_SLAB).add(Blocks.BIRCH_LOG).add(Blocks.PRISMARINE).add(Blocks.PRISMARINE_BRICKS).add(Blocks.PRISMARINE_BRICK_SLAB);

        tag(OBSIDIANS)
                .add(Blocks.OBSIDIAN).add(Blocks.CRYING_OBSIDIAN);

        tag(Crops.HARDER_CROPS_TAG)
                .add(Blocks.WHEAT, Blocks.CARROTS, Blocks.POTATOES, Blocks.BEETROOTS)
                .add(Crops.WILD_WHEAT.get(), Crops.WILD_CARROTS.get(), Crops.WILD_POTATOES.get(), Crops.WILD_BEETROOTS.get())
                .addOptional(new ResourceLocation("supplementaries", "flax"))
                .addOptional(new ResourceLocation("farmersdelight", "tomatoes")).addOptional(new ResourceLocation("farmersdelight", "budding_tomatoes")).addOptional(new ResourceLocation("farmersdelight", "rice")).addOptional(new ResourceLocation("farmersdelight", "rice_panicles")).addOptional(new ResourceLocation("farmersdelight", "cabbages")).addOptional(new ResourceLocation("farmersdelight", "onions"));

        tag(BlockHardness.HARDNESS_BLACKLIST)
                .add(Blocks.ENDER_CHEST)
                .addTag(OBSIDIANS);
        tag(BlockHardness.DEPTH_MULTIPLIER_BLACKLIST)
                .add(Blocks.ENDER_CHEST)
                .addTag(OBSIDIANS);

        tag(TALL_GRASS)
                .add(Blocks.GRASS).add(Blocks.TALL_GRASS).add(Blocks.FERN).add(Blocks.LARGE_FERN).add(Blocks.DEAD_BUSH);
        tag(Hoes.CAN_SCYTHE)
                .addTag(TALL_GRASS).addTag(BlockTags.FLOWERS);

        tag(GRASS_BLOCKS)
                .add(Blocks.GRASS_BLOCK, Blocks.PODZOL, Blocks.MYCELIUM);

        tag(AZALEA_LEAVES)
                .add(Blocks.AZALEA_LEAVES, Blocks.FLOWERING_AZALEA_LEAVES);
        tag(OAK_LOG_LEAVES)
                .add(Blocks.OAK_LEAVES)
                .addTag(AZALEA_LEAVES);

        tag(TRUMPET_LEAVES)
                .addOptional(new ResourceLocation("quark:blue_blossom_leaves"))
                .addOptional(new ResourceLocation("quark:lavender_blossom_leaves"))
                .addOptional(new ResourceLocation("quark:orange_blossom_leaves"))
                .addOptional(new ResourceLocation("quark:yellow_blossom_leaves"))
                .addOptional(new ResourceLocation("quark:red_blossom_leaves"));

        tag(MAPLE_LEAVES)
                .addOptional(new ResourceLocation("autumnity:maple_leaves"))
                .addOptional(new ResourceLocation("autumnity:yellow_maple_leaves"))
                .addOptional(new ResourceLocation("autumnity:orange_maple_leaves"))
                .addOptional(new ResourceLocation("autumnity:red_maple_leaves"));

		//noinspection unchecked
		tag(Tweaks.BREAK_ON_FALL)
				.addTags(Tags.Blocks.GLASS, BlockTags.LEAVES);
		//noinspection unchecked
		tag(TimberTrees.TIMBER_TRUNKS)
				.addTags(BlockTags.OVERWORLD_NATURAL_LOGS)
                .addOptional(new ResourceLocation("quark:ancient_log"));

        tag(Nether.PORTAL_CORNERS)
                .add(Blocks.CRYING_OBSIDIAN);

    }

    public static TagKey<Block> create(String tagName) {
        return TagKey.create(Registries.BLOCK, new ResourceLocation(IguanaTweaksReborn.MOD_ID, tagName));
    }
}
