package insane96mcp.iguanatweaksreborn.data.generator;

import insane96mcp.iguanatweaksreborn.InsaneSO;
import insane96mcp.iguanatweaksreborn.module.combat.fletching.Fletching;
import insane96mcp.iguanatweaksreborn.module.farming.bonemeal.BoneMeal;
import insane96mcp.iguanatweaksreborn.module.farming.crops.Crops;
import insane96mcp.iguanatweaksreborn.module.farming.hoes.Hoes;
import insane96mcp.iguanatweaksreborn.module.items.flintexpansion.FlintExpansion;
import insane96mcp.iguanatweaksreborn.module.mining.blockhardness.BlockHardness;
import insane96mcp.iguanatweaksreborn.module.misc.beaconconduit.BeaconConduit;
import insane96mcp.iguanatweaksreborn.module.misc.tweaks.Tweaks;
import insane96mcp.iguanatweaksreborn.module.mobs.spawning.Spawning;
import insane96mcp.iguanatweaksreborn.module.movement.minecarts.Minecarts;
import insane96mcp.iguanatweaksreborn.module.sleeprespawn.death.Death;
import insane96mcp.iguanatweaksreborn.module.sleeprespawn.respawn.Respawn;
import insane96mcp.iguanatweaksreborn.module.world.Nether;
import insane96mcp.iguanatweaksreborn.module.world.coalfire.CoalFire;
import insane96mcp.iguanatweaksreborn.module.world.explosionoverhaul.ExplosionOverhaul;
import insane96mcp.iguanatweaksreborn.module.world.seasons.Seasons;
import insane96mcp.iguanatweaksreborn.module.world.timber.TimberTrees;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
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
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ISOBlockTagsProvider extends BlockTagsProvider {
    public static final TagKey<Block> OBSIDIANS = create("obsidians");
    public static final TagKey<Block> GRASS_BLOCKS = create("grass_blocks");
    public static final TagKey<Block> TALL_GRASS = create("tall_grass");
    public static final TagKey<Block> AZALEA_LEAVES = create("azalea_leaves");
    public static final TagKey<Block> OAK_LOG_LEAVES = create("oak_log_leaves");
    public static final TagKey<Block> MAPLE_LEAVES = create("maple_leaves");
    public static final TagKey<Block> TRUMPET_LEAVES = create("trumpet_leaves");
    public static final TagKey<Block> LAUREL_LEAVES = create("laurel_leaves");
    public static final TagKey<Block> MORADO_LEAVES = create("morado_leaves");
    public static final TagKey<Block> WISTERIA_LEAVES = create("wisteria_leaves");
    public static final TagKey<Block> ASPEN_LOGS = create("aspen_logs");
    public static final TagKey<Block> ASPEN_LEAVES = create("aspen_leaves");
    public static final TagKey<Block> PLUM_LEAVES = create("plum_leaves");

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
        //Vanilla Tags
        tag(BlockTags.FALL_DAMAGE_RESETTING)
                .add(Blocks.COBWEB);
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(Death.GRAVE.block().get())
                .add(BeaconConduit.BEACON.block().get())
                .add(Spawning.ECHO_LANTERN.block().get())
                .add(FlintExpansion.FLINT_ROCK.block().get());
        tag(BlockTags.MINEABLE_WITH_SHOVEL)
                .add(BoneMeal.RICH_FARMLAND.block().get());
        tag(BlockTags.MINEABLE_WITH_HOE)
                .add(Crops.WILD_WHEAT.get(), Crops.WILD_CARROTS.get(), Crops.WILD_POTATOES.get(), Crops.WILD_BEETROOTS.get());
        tag(BlockTags.MINEABLE_WITH_AXE)
                .add(Fletching.FLETCHING_TABLE.block().get())
                .add(CoalFire.BURNT_LOG.block().get());
        tag(BlockTags.RAILS)
                .add(Minecarts.COPPER_POWERED_RAIL.block().get(), Minecarts.GOLDEN_POWERED_RAIL.block().get());
        tag(BlockTags.REPLACEABLE_BY_TREES)
                .add(FlintExpansion.FLINT_ROCK.block().get());

        //Mod's tags
        tag(Respawn.RESPAWN_OBELISK_BLOCKS_TO_ROT)
                .add(Blocks.COBBLESTONE).add(Blocks.MOSSY_COBBLESTONE).add(Blocks.COBBLESTONE_SLAB).add(Blocks.MOSSY_COBBLESTONE_SLAB);

        tag(OBSIDIANS)
                .add(Blocks.OBSIDIAN).add(Blocks.CRYING_OBSIDIAN);

        tag(Crops.HARDER_CROPS_TAG)
                .add(Blocks.WHEAT, Blocks.CARROTS, Blocks.POTATOES, Blocks.BEETROOTS)
                .add(Crops.WILD_WHEAT.get(), Crops.WILD_CARROTS.get(), Crops.WILD_POTATOES.get(), Crops.WILD_BEETROOTS.get())
                .addOptional(ResourceLocation.parse("supplementaries:flax")).addOptional(ResourceLocation.parse("supplementaries:wild_flax"))
                .addOptional(ResourceLocation.parse("farmersdelight:tomatoes")).addOptional(ResourceLocation.parse("farmersdelight:budding_tomatoes")).addOptional(ResourceLocation.parse("farmersdelight:rice")).addOptional(ResourceLocation.parse("farmersdelight:rice_panicles")).addOptional(ResourceLocation.parse("farmersdelight:cabbages")).addOptional(ResourceLocation.parse("farmersdelight:onions"));

        tag(BlockHardness.HARDNESS_BLACKLIST)
                .add(Blocks.ENDER_CHEST)
                .addTag(OBSIDIANS);
        tag(BlockHardness.DEPTH_MULTIPLIER_BLACKLIST)
                .add(Blocks.ENDER_CHEST)
                .addTag(OBSIDIANS);

        tag(TALL_GRASS)
                .add(Blocks.GRASS).add(Blocks.TALL_GRASS).add(Blocks.FERN).add(Blocks.LARGE_FERN);
        tag(Hoes.CAN_SCYTHE)
                .addTag(TALL_GRASS).addTag(BlockTags.FLOWERS).add(Blocks.DEAD_BUSH);

        tag(GRASS_BLOCKS)
                .add(Blocks.GRASS_BLOCK, Blocks.PODZOL, Blocks.MYCELIUM);

        tag(AZALEA_LEAVES)
                .add(Blocks.AZALEA_LEAVES, Blocks.FLOWERING_AZALEA_LEAVES);
        tag(OAK_LOG_LEAVES)
                .add(Blocks.OAK_LEAVES)
                .addTag(AZALEA_LEAVES);

        tag(TRUMPET_LEAVES)
                .addOptional(ResourceLocation.parse("quark:blue_blossom_leaves"))
                .addOptional(ResourceLocation.parse("quark:lavender_blossom_leaves"))
                .addOptional(ResourceLocation.parse("quark:orange_blossom_leaves"))
                .addOptional(ResourceLocation.parse("quark:yellow_blossom_leaves"))
                .addOptional(ResourceLocation.parse("quark:red_blossom_leaves"));

        tag(MAPLE_LEAVES)
                .addOptional(ResourceLocation.parse("autumnity:maple_leaves"))
                .addOptional(ResourceLocation.parse("autumnity:yellow_maple_leaves"))
                .addOptional(ResourceLocation.parse("autumnity:orange_maple_leaves"))
                .addOptional(ResourceLocation.parse("autumnity:red_maple_leaves"));

        tag(LAUREL_LEAVES)
                .addOptional(ResourceLocation.parse("atmospheric:laurel_leaves"))
                .addOptional(ResourceLocation.parse("atmospheric:dry_laurel_leaves"));

        tag(MORADO_LEAVES)
                .addOptional(ResourceLocation.parse("atmospheric:morado_leaves"))
                .addOptional(ResourceLocation.parse("atmospheric:flowering_morado_leaves"));

        tag(WISTERIA_LEAVES)
                .addOptional(ResourceLocation.parse("environmental:wisteria_leaves"))
                .addOptional(ResourceLocation.parse("environmental:pink_wisteria_leaves"))
                .addOptional(ResourceLocation.parse("environmental:purple_wisteria_leaves"))
                .addOptional(ResourceLocation.parse("environmental:blue_wisteria_leaves"))
                .addOptional(ResourceLocation.parse("environmental:white_wisteria_leaves"));
        tag(PLUM_LEAVES)
                .addOptional(ResourceLocation.parse("environmental:plum_leaves"))
                .addOptional(ResourceLocation.parse("environmental:cheerful_plum_leaves"))
                .addOptional(ResourceLocation.parse("environmental:moody_plum_leaves"));

        tag(ASPEN_LOGS)
                .addOptional(ResourceLocation.parse("atmospheric:aspen_log"))
                .addOptional(ResourceLocation.parse("atmospheric:watchful_aspen_log"));
        tag(ASPEN_LEAVES)
                .addOptional(ResourceLocation.parse("atmospheric:aspen_leaves"))
                .addOptional(ResourceLocation.parse("atmospheric:green_aspen_leaves"));

		//noinspection unchecked
		tag(Tweaks.BREAK_ON_FALL)
				.addTags(Tags.Blocks.GLASS, BlockTags.LEAVES);
		//noinspection unchecked
		tag(TimberTrees.TIMBER_TRUNKS)
				.addTags(BlockTags.OVERWORLD_NATURAL_LOGS)
                .addOptional(ResourceLocation.parse("quark:ancient_log"))
                .addOptional(ResourceLocation.parse("tconstruct:greenheart_log"))
                .addOptional(ResourceLocation.parse("tconstruct:skyroot_log"))
                .addOptional(ResourceLocation.parse("tconstruct:enderbark_log"));

        tag(TimberTrees.ATTACHED_BLOCKS)
                .add(Blocks.BEEHIVE, Blocks.BEE_NEST);

        tag(Nether.PORTAL_CORNERS)
                .add(Blocks.CRYING_OBSIDIAN);

        tag(Seasons.PLANTS_TO_DECAY)
                .add(Blocks.GRASS, Blocks.FERN, Blocks.TALL_GRASS, Blocks.LARGE_FERN)
                .addOptional(ResourceLocation.parse("environmental:giant_tall_grass"));

        tag(Seasons.PLANTS_TO_DEAD_BUSH)
                .addTag(BlockTags.SAPLINGS);

        tag(ExplosionOverhaul.FLYING_BLOCKS_EXPLOSION_BLACKLIST)
                .add(Blocks.TNT)
                .addTag(BlockTags.BEDS)
                .addTag(BlockTags.DOORS)
                .addTag(BlockTags.TALL_FLOWERS)
                .add(Blocks.CHEST, Blocks.TRAPPED_CHEST)
                .add(Blocks.TALL_GRASS, Blocks.LARGE_FERN);

        tag(HUD_TIME)
                .addOptional(ForgeRegistries.BLOCKS.getKey(ModRegistry.CLOCK_BLOCK.get()));
    }

    public static TagKey<Block> create(String tagName) {
        return TagKey.create(Registries.BLOCK, InsaneSO.location(tagName));
    }
}
