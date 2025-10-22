package insane96mcp.iguanatweaksreborn.module.world.coalfire;

import insane96mcp.iguanatweaksreborn.InsaneSO;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.module.misc.Packs;
import insane96mcp.iguanatweaksreborn.setup.ISORegistries;
import insane96mcp.iguanatweaksreborn.setup.registry.SimpleBlockWithItem;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.event.BlockBurntEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.RegistryObject;

@LoadFeature(module = Modules.Ids.WORLD, name = "Coal & Fire")
public class CoalFire extends Feature {
    public static final SimpleBlockWithItem BURNT_LOG = SimpleBlockWithItem.register("burnt_log", () -> new RotatedPillarBlock(BlockBehaviour.Properties.of()
            .mapColor((p_152624_)
                    -> MapColor.TERRACOTTA_BLACK)
            .instrument(NoteBlockInstrument.BASS)
            .strength(1.0F)
            .sound(SoundType.WOOD)));

    public static final RegistryObject<Item> FIRESTARTER = ISORegistries.ITEMS.register("firestarter", () -> new FirestarterItem(new Item.Properties().stacksTo(1).defaultDurability(11)));

    public static final GameRules.Key<GameRules.IntegerValue> RULE_FIRESPEEDMULTIPLIER = GameRules.register("iguanatweaks:fireSpeedMultiplier", GameRules.Category.UPDATES, GameRules.IntegerValue.create(4));

    @Config(min = 0d, max = 1d, description = "Chance for logs to yield burnt logs when burnt")
    public static Double burntLogsChance = 0.8d;

    @Config(description = """
			If enabled, a data pack will be enabled that:
			* Removes the Charcoal recipe from smelting
			* Makes Coal Ore require an Iron Pickaxe or better to mine
			* Replaces Flint and Steel in the overworld with firestarter
			* Changes flint and steel recipe to require blaze powder""")
    public static Boolean noCharcoalSmeltingAndIronCoal = true;

    @Config(description = "If true, two flints (on per hand) can start a fire")
    public static Boolean twoFlintFireStarter$enabled = true;
    @Config(min = 0d, max = 1d, description = "Chance to ignite a block when using two flints")
    public static Double twoFlintFireStarter$igniteChance = 0.35d;
    @Config(min = 0d, max = 1d, description = "Chance for the flint to break when using two flints")
    public static Double twoFlintFireStarter$breakChance = 0.3d;

    @Config(description = "If true, campfires must be lit")
    public static Boolean unlitCampfires = true;

    @Config(description = "If true, campfires will be extinguished when it starts to rain")
    public static Boolean campfireTurnOffUnderRain = true;

    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        InsaneSO.addServerPack("coal_fire", "Insane's Survival Overhaul No Charcoal Smelting and Iron Coal", () -> this.isEnabled() && !Packs.disableAllDataPacks && noCharcoalSmeltingAndIronCoal);
    }

    public static boolean areCampfiresUnlit() {
        return Feature.isEnabled(CoalFire.class) && unlitCampfires;
    }

    public static boolean canRainTurnOffCampfires() {
        return Feature.isEnabled(CoalFire.class) && campfireTurnOffUnderRain;
    }

    @SubscribeEvent
    public void onBlockBurnt(BlockBurntEvent event) {
        if (!this.isEnabled()
                || burntLogsChance == 0d)
            return;

        if (event.getLevel().getRandom().nextDouble() < burntLogsChance
                && event.getState().is(BlockTags.LOGS_THAT_BURN)) {
            FallingBlockEntity.fall((Level) event.getLevel(), event.getPos(), BURNT_LOG.block().get().defaultBlockState());
        }
    }

    @SubscribeEvent
    public void onBlockRightClicked(PlayerInteractEvent.RightClickBlock event) {
        if (!this.isEnabled()
                || !twoFlintFireStarter$enabled
                || event.getHand() != InteractionHand.MAIN_HAND
                || !event.getItemStack().is(Items.FLINT)
                || !event.getEntity().getOffhandItem().is(Items.FLINT)
                || event.getEntity().getCooldowns().isOnCooldown(Items.FLINT)
                || event.getLevel().isClientSide)
            return;

        double ignite = event.getEntity().getRandom().nextDouble();

        event.getEntity().swing(event.getHand(), true);
        event.setCanceled(true);
        event.getEntity().getCooldowns().addCooldown(event.getItemStack().getItem(), 15);

        if (ignite < twoFlintFireStarter$igniteChance) {
            UseOnContext context = new UseOnContext(event.getEntity(), event.getHand(), new BlockHitResult(event.getHitVec().getLocation(), event.getHitVec().getDirection(), event.getPos(), event.getHitVec().isInside()));
            //Yes, I copy-pasted FlintAndSteelItem#use
            Player player = context.getPlayer();
            Level level = context.getLevel();
            BlockPos blockpos = context.getClickedPos();
            BlockState blockstate = level.getBlockState(blockpos);
            if (!CampfireBlock.canLight(blockstate) && !CandleBlock.canLight(blockstate) && !CandleCakeBlock.canLight(blockstate)) {
                BlockPos clickedPos = blockpos.relative(context.getClickedFace());
                if (BaseFireBlock.canBePlacedAt(level, clickedPos, context.getHorizontalDirection())) {
                    BlockState state = BaseFireBlock.getState(level, clickedPos);
                    level.setBlock(clickedPos, state, 11);
                    level.gameEvent(player, GameEvent.BLOCK_PLACE, blockpos);
                }
            }
            else {
                level.setBlock(blockpos, blockstate.setValue(BlockStateProperties.LIT, Boolean.TRUE), 11);
                level.gameEvent(player, GameEvent.BLOCK_CHANGE, blockpos);
            }
            event.getLevel().playSound(null, event.getPos(), SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, event.getLevel().getRandom().nextFloat() * 0.4F + 0.8F);
        }
        //On fail, play a high-pitched sound
        else {
            event.getLevel().playSound(null, event.getPos(), SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, event.getLevel().getRandom().nextFloat() * 0.4F + 1.5F);
        }

        if (!event.getEntity().isCreative()) {
            tryBreakFlint(event.getEntity(), event.getHand(), event.getItemStack());
            tryBreakFlint(event.getEntity(), InteractionHand.OFF_HAND, event.getEntity().getOffhandItem());
        }
    }

    private static void tryBreakFlint(Player player, InteractionHand hand, ItemStack stack) {
        if (player.getRandom().nextDouble() >= twoFlintFireStarter$breakChance)
            return;
        stack.shrink(1);
        player.broadcastBreakEvent(hand);
        net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(player, stack, hand);
    }
}