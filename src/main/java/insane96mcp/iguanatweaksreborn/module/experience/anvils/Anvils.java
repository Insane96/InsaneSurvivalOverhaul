package insane96mcp.iguanatweaksreborn.module.experience.anvils;

import insane96mcp.iguanatweaksreborn.InsaneSO;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.data.IdTagMatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.AnvilRepairEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@LoadFeature(module = Modules.Ids.EXPERIENCE, description = "Better repair, free rename and merge. With this feature enabled, EMI's repairs and enchanting in anvil recipes are removed.")
public class Anvils extends Feature {

    public static final String CAN_BE_REPAIRED_WITH_LANG = InsaneSO.MOD_ID + ".can_be_repaired_with";

    @Config(description = "Removes the cost for renaming items in Anvil")
    public static Boolean renaming$noCost = true;
    @Config(description = "If true, anvils don't wear down when only used for renaming")
    public static Boolean renaming$noBreak = true;

    @Config(min = 0, description = "Set the cap for repairing items in the anvil (vanilla is 40)")
    public static Integer xpCost$repairCap = 1024;
    @Config(min = 0, description = "Multiplier for the levels required to repair or merge an item.")
    public static Double xpCost$Multiplier = 1d;
    @Config(description = "The cost when merging two items is calculated from the final item, no longer from a strange calculations between the two items merging.")
    public static Boolean xpCost$mergingCostIsBasedOffResult = true;
    @Config(description = "Repairing items in an anvil doesn't cost xp")
    public static Boolean xpCost$noCost = true;
    @Config(description = "XP cost when repairing an item in an anvil is based of the levels of the enchantments. This is not affected by 'Repair cost multiplier'")
    public static Boolean xpCost$differentRepairCost = false;
    @Config(min = 0, description = "Vanilla: 1")
    public static Integer xpCost$enchantmentsCostByRarity$common = 1;
    @Config(min = 0, description = "Vanilla: 2")
    public static Integer xpCost$enchantmentsCostByRarity$uncommon = 2;
    @Config(min = 0, description = "Vanilla: 4")
    public static Integer xpCost$enchantmentsCostByRarity$rare = 3;
    @Config(min = 0, description = "Vanilla: 8")
    public static Integer xpCost$enchantmentsCostByRarity$veryRare = 6;

    @Config(description = "Repairing items in an anvil costs more materials if the item is enchanted. This is the percentage increase for each level of enchantment on the item")
    public static Double materialCost$increaseMaterialsRequiredWithEnchantments = 0.05d;
    @Config(description = "Repairing items in an anvil costs more materials if the item is enchanted. This is the flat increase for each level of enchantment on the item")
    public static Double materialCost$increaseMaterialsRequiredWithEnchantmentsFlat = 0.5d;

    @Config(description = "Merging items in an anvil will reduce the repair amount if the left is enchanted. This is the multiplicative percentage decrease for each level of enchantment on the item. This is applied to the right item durability before the bonus repair.")
    public static Double mergeRepair$decreaseRepairAmountWithEnchantments = 0.05d;

    @Config(description = "If true you can merge items in the anvil and get a repair bonus")
    public static Boolean allowMergingItems = true;
    @Config(min = 0, max = 100, description = "Vanilla is 12%")
    public static Integer mergingRepairBonus = 10;
    @Config(min = 0, description = "Chance for an anvil to become chipped/damaged/break. Vanilla is 12%")
    public static Double degradationChance = 0.01d;
    @Config
    public static Boolean fixAnvilsWithIronBlocks = true;

    public Anvils(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }

    public static Optional<AnvilRepair> getCustomAnvilRepair(ItemStack left) {
        if (!Feature.isEnabled(Anvils.class))
            return Optional.empty();

        for (AnvilRepair anvilRepair : AnvilRepairReloadListener.REPAIRS.values()) {
            if (anvilRepair.isItemToRepair(left))
                return Optional.of(anvilRepair);
        }
        return Optional.empty();
    }

    public static Optional<AnvilRepair.RepairData> getCustomAnvilRepair(ItemStack left, ItemStack right) {
        if (!Feature.isEnabled(Anvils.class))
            return Optional.empty();

        for (AnvilRepair anvilRepair : AnvilRepairReloadListener.REPAIRS.values()) {
            if (anvilRepair.isItemToRepair(left)) {
                Optional<AnvilRepair.RepairData> repairData = anvilRepair.getRepairDataFromMaterial(right);
                if (repairData.isPresent())
                    return repairData;
            }
        }
        return Optional.empty();
    }

    @SubscribeEvent
    public void onRightClickAnvil(PlayerInteractEvent.RightClickBlock event) {
        if (!this.isEnabled()
                || !fixAnvilsWithIronBlocks
                || !event.getItemStack().is(Items.IRON_BLOCK))
            return;
        BlockState state = event.getLevel().getBlockState(event.getPos());
        if (!state.is(BlockTags.ANVIL))
            return;

        Direction direction = state.getValue(AnvilBlock.FACING);
        boolean isChipped = state.is(Blocks.CHIPPED_ANVIL);
        boolean isDamaged = state.is(Blocks.DAMAGED_ANVIL);
        if (!isChipped && !isDamaged)
            return;

        event.setResult(Event.Result.DENY);
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (!event.getEntity().getAbilities().instabuild) {
            event.getItemStack().shrink(1);
        }
        event.getLevel().setBlockAndUpdate(event.getPos(), isChipped ? Blocks.ANVIL.defaultBlockState().setValue(AnvilBlock.FACING, direction) : Blocks.CHIPPED_ANVIL.defaultBlockState().setValue(AnvilBlock.FACING, direction));
        event.getLevel().playSound(event.getEntity(), event.getPos(), SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1f, 1.5f);

        if (event.getEntity() instanceof ServerPlayer serverPlayer)
            CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, event.getPos(), event.getItemStack());
    }

    @SubscribeEvent
    public void onAnvilRepair(AnvilRepairEvent event) {
        if (!this.isEnabled())
            return;

        event.setBreakChance(degradationChance.floatValue());
        if (renaming$noBreak && ItemStack.isSameItem(event.getLeft(), event.getOutput()) && event.getRight().isEmpty())
            event.setBreakChance(0);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onTooltip(ItemTooltipEvent event) {
        if (!this.isEnabled())
            return;

        Minecraft mc = Minecraft.getInstance();
        if (!(mc.screen instanceof AnvilScreen))
            return;

        List<Component> itemsDescriptions = new ArrayList<>();
        Optional<AnvilRepair> oCustomAnvilRepair = getCustomAnvilRepair(event.getItemStack());
        oCustomAnvilRepair.ifPresent(anvilRepair -> {
            for (AnvilRepair.RepairData repairData : anvilRepair.repairData) {
                if (repairData.repairMaterial().type == IdTagMatcher.Type.TAG) {
                    MutableComponent component = Component.literal("#").append(repairData.repairMaterial().location.toString());
                    if (repairData.maxRepair() < 1f)
                        component.append(CommonComponents.space().append(Component.translatable("iguanatweaksreborn.partial_repair")));
                    itemsDescriptions.add(component);
                }
                else {
                    Optional<Item> oItem = repairData.repairMaterial().getAllItems().stream().findAny();
                    if (oItem.isPresent()) {
                        MutableComponent component = oItem.get().getDescription().copy();
                        if (repairData.maxRepair() < 1f)
                            component.append(CommonComponents.space().append(Component.translatable("iguanatweaksreborn.partial_repair")));
                        itemsDescriptions.add(component);
                    }
                }
            }
        });

        if (!itemsDescriptions.isEmpty()) {
            String joined = itemsDescriptions.stream().map(Component::getString).collect(Collectors.joining(", "));
            event.getToolTip().add(Component.empty());
            event.getToolTip().add(Component.translatable(CAN_BE_REPAIRED_WITH_LANG, joined).withStyle(ChatFormatting.GREEN));
        }
    }

    public static int getRarityCost(Enchantment enchantment) {
        return switch (enchantment.getRarity()) {
            case COMMON -> xpCost$enchantmentsCostByRarity$common;
            case UNCOMMON -> xpCost$enchantmentsCostByRarity$uncommon;
            case RARE -> xpCost$enchantmentsCostByRarity$rare;
            case VERY_RARE -> xpCost$enchantmentsCostByRarity$veryRare;
        };
    }
}
