package insane96mcp.insanesurvivaloverhaul.module.world.coalfire;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.event.BlockBurntEvent;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.mixin.accessor.GameRulesIntegerValueAccessor;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.module.misc.Packs;
import insane96mcp.insanesurvivaloverhaul.setup.ISORegistries;
import insane96mcp.insanesurvivaloverhaul.setup.SimpleBlockWithItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

@LoadFeature(module = ISOModules.WORLD, name = "Coal & Fire", description = "Control fire speed with insanesurvivaloverhaul:fire_speed_multiplier game rule. This feature also enables a recipe to get a flint from 3 gravel.")
public class CoalFire extends Feature {
    public static final SimpleBlockWithItem BURNT_LOG = SimpleBlockWithItem.register("burnt_log", () -> new BurntLogBlock(BlockBehaviour.Properties.of()
            .mapColor((p_152624_)
                    -> MapColor.TERRACOTTA_BLACK)
            .instrument(NoteBlockInstrument.BASS)
            .strength(1.0F)
            .sound(SoundType.WOOD)));

    public static final DeferredHolder<Item, FirestarterItem> FIRESTARTER = ISORegistries.ITEMS.register("firestarter", () -> new FirestarterItem(new Item.Properties().stacksTo(1).durability(11)));

    public static final GameRules.Key<GameRules.IntegerValue> RULE_FIRESPEEDMULTIPLIER = GameRules.register("insanesurvivaloverhaul:fire_speed_multiplier", GameRules.Category.UPDATES, GameRulesIntegerValueAccessor.invokeCreate(4, 1, 128, (server, value) -> {}));

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

    @Override
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
            Player player = context.getPlayer();
            Level level = context.getLevel();
            BlockPos blockpos = context.getClickedPos();
            BlockState blockstate = level.getBlockState(blockpos);
            BlockState blockstate2 = blockstate.getToolModifiedState(context, ItemAbilities.FIRESTARTER_LIGHT, false);
            if (blockstate2 == null && blockstate.hasProperty(BlockStateProperties.LIT) && !blockstate.getValue(BlockStateProperties.LIT)) {
                blockstate2 = blockstate.setValue(BlockStateProperties.LIT, true);
            }
            if (blockstate2 == null) {
                BlockPos blockpos1 = blockpos.relative(context.getClickedFace());
                if (BaseFireBlock.canBePlacedAt(level, blockpos1, context.getHorizontalDirection())) {
                    level.playSound(null, blockpos1, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);
                    level.setBlock(blockpos1, BaseFireBlock.getState(level, blockpos1), 11);
                    level.gameEvent(player, GameEvent.BLOCK_PLACE, blockpos);
                }
            } else {
                level.playSound(null, blockpos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);
                level.setBlock(blockpos, blockstate2, 11);
                level.gameEvent(player, GameEvent.BLOCK_CHANGE, blockpos);
            }
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
        player.onEquippedItemBroken(stack.getItem(), LivingEntity.getSlotForHand(hand));
        EventHooks.onPlayerDestroyItem(player, stack, hand);
    }

    public static class BurntLogBlock extends RotatedPillarBlock implements Fallable {
        public BurntLogBlock(Properties properties) {
            super(properties);
        }

        @Override
        protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
            level.scheduleTick(pos, this, 2);
        }

        @Override
        protected BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
            level.scheduleTick(currentPos, this, 2);
            return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
        }

        @Override
        protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
            if (FallingBlock.isFree(level.getBlockState(pos.below())) && pos.getY() >= level.getMinBuildHeight()) {
                FallingBlockEntity.fall(level, pos, state);
            }
        }
    }
}