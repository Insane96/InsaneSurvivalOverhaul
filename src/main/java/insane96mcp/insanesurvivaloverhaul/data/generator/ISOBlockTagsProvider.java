package insane96mcp.insanesurvivaloverhaul.data.generator;

import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.death.respawn.EchoPillar;
import insane96mcp.insanesurvivaloverhaul.module.farming.bonemeal.BoneMeal;
import insane96mcp.insanesurvivaloverhaul.module.farming.crops.Crops;
import insane96mcp.insanesurvivaloverhaul.module.farming.hoes.Hoes;
import insane96mcp.insanesurvivaloverhaul.module.mining.beegoreveins.BigOreVeins;
import insane96mcp.insanesurvivaloverhaul.module.misc.tweaks.Tweaks;
import insane96mcp.insanesurvivaloverhaul.module.mobs.spawning.Spawning;
import insane96mcp.insanesurvivaloverhaul.module.movement.minecarts.Minecarts;
import insane96mcp.insanesurvivaloverhaul.module.sleep.tiredness.Tiredness;
import insane96mcp.insanesurvivaloverhaul.module.world.Nether;
import insane96mcp.insanesurvivaloverhaul.module.world.coalfire.CoalFire;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class ISOBlockTagsProvider extends BlockTagsProvider {
    public static final TagKey<Block> OBSIDIANS = create("obsidians");
    public static final TagKey<Block> SKULLS = create("skulls");
    public static final TagKey<Block> INFESTED = create("infested");
    //public static final TagKey<Block> GRASS_BLOCKS = create("grass_blocks");
    public static final TagKey<Block> TALL_GRASS = create("tall_grass");

    public static final TagKey<Block> HUD_CARDINAL_DIRECTION = create("hud/cardinal_direction");
    public static final TagKey<Block> HUD_SEASON = create("hud/season");
    public static final TagKey<Block> HUD_DEPTH = create("hud/depth");
    public static final TagKey<Block> HUD_TIME = create("hud/time");
    public static final TagKey<Block> HUD_BIOME = create("hud/biome");

    public ISOBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, String modId, @Nullable ExistingFileHelper existingFileHelper){
        super(output, lookupProvider, modId, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(Spawning.ECHO_LANTERN.block().get())
                .add(BigOreVeins.COPPER_ORE_ROCK.block().get(), BigOreVeins.IRON_ORE_ROCK.block().get(), BigOreVeins.GOLD_ORE_ROCK.block().get());
        tag(BlockTags.MINEABLE_WITH_SHOVEL)
                .add(BoneMeal.RICH_FARMLAND.block().get())
                .add(CoalFire.BURNT_LOG.block().get());
        tag(BlockTags.MINEABLE_WITH_HOE)
                .add(Crops.WILD_WHEAT.get(), Crops.WILD_CARROTS.get(), Crops.WILD_POTATOES.get(), Crops.WILD_BEETROOTS.get())
                .addTag(Crops.HARDER_CROPS_TAG);
        tag(BlockTags.MINEABLE_WITH_AXE)
                .remove(Blocks.WHEAT, Blocks.CARROTS, Blocks.POTATOES, Blocks.BEETROOTS);
        tag(BlockTags.NEEDS_STONE_TOOL)
                .add(BigOreVeins.IRON_ORE_ROCK.block().get(), BigOreVeins.COPPER_ORE_ROCK.block().get());
        tag(BlockTags.NEEDS_IRON_TOOL)
                .add(BigOreVeins.GOLD_ORE_ROCK.block().get());

        tag(OBSIDIANS)
                .add(Blocks.OBSIDIAN, Blocks.CRYING_OBSIDIAN);
        tag(INFESTED)
                .add(Blocks.INFESTED_DEEPSLATE, Blocks.INFESTED_STONE, Blocks.INFESTED_COBBLESTONE, Blocks.INFESTED_STONE_BRICKS, Blocks.INFESTED_CRACKED_STONE_BRICKS, Blocks.INFESTED_CHISELED_STONE_BRICKS, Blocks.INFESTED_MOSSY_STONE_BRICKS);
        tag(SKULLS)
                .add(Blocks.SKELETON_SKULL, Blocks.SKELETON_WALL_SKULL, Blocks.WITHER_SKELETON_SKULL, Blocks.WITHER_SKELETON_WALL_SKULL, Blocks.CREEPER_HEAD, Blocks.CREEPER_WALL_HEAD, Blocks.DRAGON_HEAD, Blocks.DRAGON_WALL_HEAD, Blocks.PIGLIN_HEAD, Blocks.PIGLIN_WALL_HEAD, Blocks.PLAYER_HEAD, Blocks.PLAYER_WALL_HEAD, Blocks.ZOMBIE_HEAD, Blocks.ZOMBIE_WALL_HEAD);

        //noinspection unchecked
        tag(Tweaks.BREAK_ON_FALL)
                .addTags(Tags.Blocks.GLASS_BLOCKS, BlockTags.LEAVES);
        //tag(HUD_TIME)
                //.addOptional(ForgeRegistries.BLOCKS.getKey(ModRegistry.CLOCK_BLOCK.get()));
        tag(TALL_GRASS)
                .add(Blocks.SHORT_GRASS).add(Blocks.TALL_GRASS).add(Blocks.FERN).add(Blocks.LARGE_FERN);

        tag(Nether.PORTAL_CORNERS)
                .add(Blocks.CRYING_OBSIDIAN);
        tag(BlockTags.RAILS)
                .add(Minecarts.COPPER_POWERED_RAIL.block().get(), Minecarts.GOLDEN_POWERED_RAIL.block().get());

        tag(Crops.HARDER_CROPS_TAG)
                .add(Blocks.WHEAT, Blocks.CARROTS, Blocks.POTATOES, Blocks.BEETROOTS)
                .add(Crops.WILD_WHEAT.get(), Crops.WILD_CARROTS.get(), Crops.WILD_POTATOES.get(), Crops.WILD_BEETROOTS.get())
                .addOptional(ResourceLocation.parse("supplementaries:flax")).addOptional(ResourceLocation.parse("supplementaries:wild_flax"))
                .addOptional(ResourceLocation.parse("farmersdelight:tomatoes")).addOptional(ResourceLocation.parse("farmersdelight:budding_tomatoes")).addOptional(ResourceLocation.parse("farmersdelight:rice")).addOptional(ResourceLocation.parse("farmersdelight:rice_panicles")).addOptional(ResourceLocation.parse("farmersdelight:cabbages")).addOptional(ResourceLocation.parse("farmersdelight:onions"));
        tag(Hoes.CAN_SCYTHE)
                .addTag(TALL_GRASS).addTag(BlockTags.FLOWERS).add(Blocks.DEAD_BUSH);
        tag(EchoPillar.ECHO_PILLAR_BLOCKS_TO_ROT)
                .add(Blocks.COBBLESTONE).add(Blocks.MOSSY_COBBLESTONE).add(Blocks.COBBLESTONE_SLAB).add(Blocks.MOSSY_COBBLESTONE_SLAB);

        tag(BigOreVeins.ORE_ROCKS)
                .add(BigOreVeins.COPPER_ORE_ROCK.block().get(), BigOreVeins.IRON_ORE_ROCK.block().get(), BigOreVeins.GOLD_ORE_ROCK.block().get());

        tag(Tiredness.SLEEPING_BLOCKS)
                .addTag(BlockTags.BEDS);
    }

    public static TagKey<Block> create(String tagName) {
        return TagKey.create(Registries.BLOCK, InsaneSO.id(tagName));
    }
}
