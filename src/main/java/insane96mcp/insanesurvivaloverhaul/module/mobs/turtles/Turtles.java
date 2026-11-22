package insane96mcp.insanesurvivaloverhaul.module.mobs.turtles;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.module.base.items.ItemComponentsReloadListener;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.setup.ISORegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

@LoadFeature(module = ISOModules.MOBS, description = "Turtle Helmet gives Oxygen Bonus instead of Water Breathing and stackable scute blocks.")
public class Turtles extends Feature {
    public static final DeferredHolder<SoundEvent, SoundEvent> TURTLE_SHED_SCUTE = ISORegistries.SOUND_EVENTS.register("turtle_shed_scute", () -> SoundEvent.createVariableRangeEvent(InsaneSO.id("turtle_shed_scute")));

    public static final DeferredHolder<Block, ScuteBlock> TURTLE_SCUTE = ISORegistries.BLOCKS.register("turtle_scute", () -> new ScuteBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GREEN).strength(0.2F, 0.5F).offsetType(BlockBehaviour.OffsetType.XZ).dynamicShape().sound(SoundType.BONE_BLOCK)));

    public static final ResourceLocation HELMET_OXYGEN_BONUS_ID = InsaneSO.id("turtle_helmet_oxygen_bonus");

    @Config(min = 0d, description = "Oxygen Bonus attribute given by the Turtle Helmet, replacing its vanilla Water Breathing effect. Works like Respiration levels (stacks with it). Set to 0 to keep the vanilla Water Breathing.")
    public static Double helmetOxygenBonus = 2d;
    @Config(description = "If true, scute will drop as a block and not as item")
    public static Boolean scuteDropsAsBlock = true;

    @Override
    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        ItemComponentsReloadListener.PROGRAMMATIC_PROVIDERS.add((registryAccess, patches) -> {
            if (!this.isEnabled() || !replacesHelmetWaterBreathing())
                return;
            // Armor has no attribute_modifiers component in 1.21.1, setting it replaces the defaults, so they're copied
            ItemAttributeModifiers modifiers = Items.TURTLE_HELMET.getDefaultAttributeModifiers(Items.TURTLE_HELMET.getDefaultInstance())
                    .withModifierAdded(Attributes.OXYGEN_BONUS, new AttributeModifier(HELMET_OXYGEN_BONUS_ID, helmetOxygenBonus, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.HEAD);
            patches.put(Items.TURTLE_HELMET, DataComponentPatch.builder().set(DataComponents.ATTRIBUTE_MODIFIERS, modifiers).build());
        });
    }

    /**
     * If true, the Turtle Helmet gives Oxygen Bonus (through its components) instead of the vanilla Water Breathing
     */
    public static boolean replacesHelmetWaterBreathing() {
        return isEnabled(Turtles.class) && helmetOxygenBonus > 0d;
    }

    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!this.isEnabled()
                || !scuteDropsAsBlock
                || !event.getItemStack().is(Items.TURTLE_SCUTE))
            return;

        BlockPos clickedPos = event.getPos();
        BlockState clickedState = event.getLevel().getBlockState(clickedPos);

        BlockPos placePos;
        BlockState newState;

        // Stack on an existing scute block by clicking its top face
        if (clickedState.is(TURTLE_SCUTE.get()) && event.getFace() == Direction.UP) {
            int height = clickedState.getValue(ScuteBlock.HEIGHT);
            if (height >= 15)
                return;
            placePos = clickedPos;
            newState = clickedState.setValue(ScuteBlock.HEIGHT, height + 1);
        } else {
            placePos = clickedPos.relative(event.getFace());
            if (!event.getLevel().getWorldBorder().isWithinBounds(placePos))
                return;
            if (!event.getLevel().getBlockState(placePos).canBeReplaced())
                return;
            newState = TURTLE_SCUTE.get().defaultBlockState().setValue(ScuteBlock.HEIGHT, 0);
            if (!newState.canSurvive(event.getLevel(), placePos))
                return;
        }

        if (!event.getLevel().isClientSide()) {
            event.getLevel().setBlock(placePos, newState, Block.UPDATE_ALL);
            SoundType soundType = newState.getSoundType();
            event.getLevel().playSound(null, placePos, soundType.getPlaceSound(), SoundSource.BLOCKS, (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);
            if (!event.getEntity().getAbilities().instabuild)
                event.getItemStack().shrink(1);
        }
        event.getEntity().swing(event.getHand());
        event.setCanceled(true);
    }
}
