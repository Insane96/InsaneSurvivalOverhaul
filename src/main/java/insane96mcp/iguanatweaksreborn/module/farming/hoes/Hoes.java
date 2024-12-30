package insane96mcp.iguanatweaksreborn.module.farming.hoes;

import insane96mcp.iguanatweaksreborn.IguanaTweaksReborn;
import insane96mcp.iguanatweaksreborn.data.generator.ITRBlockTagsProvider;
import insane96mcp.iguanatweaksreborn.data.generator.ITRItemTagsProvider;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.network.message.BreakWithNoSound;
import insane96mcp.insanelib.InsaneLib;
import insane96mcp.insanelib.base.JsonFeature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.data.IdTagMatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

@Label(name = "Hoes", description = "Slower Hoes and more fragile. Hoes Properties are controlled via json in this feature's folder")
@LoadFeature(module = Modules.Ids.FARMING)
public class Hoes extends JsonFeature {

	public static final String TOO_WEAK = IguanaTweaksReborn.MOD_ID + ".weak_hoe";
	public static final String TILL_COOLDOWN = IguanaTweaksReborn.MOD_ID + ".till_cooldown";
	public static final String SCYTHE_RADIUS = IguanaTweaksReborn.MOD_ID + ".scythe_radius";
	public static final TagKey<Item> DISABLED_HOES = ITRItemTagsProvider.create("disabled_hoes");

	public static final ArrayList<HoeStat> HOES_STATS_DEFAULT = new ArrayList<>(List.of(
			new HoeStat(IdTagMatcher.newId("minecraft:wooden_hoe"), 0, 1),
			new HoeStat(IdTagMatcher.newId("minecraft:stone_hoe"), 0, 0),
			new HoeStat(IdTagMatcher.newId("iguanatweaksexpanded:flint_hoe"), 0, 0),
			new HoeStat(IdTagMatcher.newId("iguanatweaksexpanded:copper_hoe"), 0, 1),
			new HoeStat(IdTagMatcher.newId("minecraft:golden_hoe"), 0, 2),
			new HoeStat(IdTagMatcher.newId("minecraft:iron_hoe"), 0, 1),
			new HoeStat(IdTagMatcher.newId("iguanatweaksexpanded:solarium_hoe"), 0, 1),
			new HoeStat(IdTagMatcher.newId("iguanatweaksexpanded:durium_hoe"), 0, 0),
			new HoeStat(IdTagMatcher.newId("iguanatweaksexpanded:coated_copper_hoe"), 0, 1),
			new HoeStat(IdTagMatcher.newId("iguanatweaksexpanded:quaron_hoe"), 0, 1),
			new HoeStat(IdTagMatcher.newId("iguanatweaksexpanded:keego_hoe"), 0, 2),
			new HoeStat(IdTagMatcher.newId("minecraft:diamond_hoe"), 0, 2),
			new HoeStat(IdTagMatcher.newId("iguanatweaksexpanded:soul_steel_hoe"), 0, 2),
			new HoeStat(IdTagMatcher.newId("minecraft:netherite_hoe"), 0, 4)
	));

	public static final ArrayList<HoeStat> hoesStats = new ArrayList<>();

	@Config(min = 1)
	@Label(name = "Durability used on till")
	public static Integer durabilityOnTill = 4;
	@Config
	@Label(name = "Trigger only for farmland", description = "'Durability used on till' is only applied for farmland, and not e.g. when using hoes on rooted dirt")
	public static Boolean triggerOnlyForFarmland = true;

	public Hoes(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
		addSyncType(new ResourceLocation(IguanaTweaksReborn.MOD_ID, "hoes_stats"), new SyncType(json -> loadAndReadJson(json, hoesStats, HOES_STATS_DEFAULT, HoeStat.LIST_TYPE)));
		JSON_CONFIGS.add(new JsonConfig<>("hoes_stats.json", hoesStats, HOES_STATS_DEFAULT, HoeStat.LIST_TYPE, true, new ResourceLocation(IguanaTweaksReborn.MOD_ID, "hoes_stats")));
	}

	@Override
	public String getModConfigFolder() {
		return IguanaTweaksReborn.CONFIG_FOLDER;
	}

	@Override
	public void loadJsonConfigs() {
		if (!this.isEnabled())
			return;
		super.loadJsonConfigs();
	}

	@SubscribeEvent
	public void onHoeUse(BlockEvent.BlockToolModificationEvent event) {
		if (!this.isEnabled()
				|| event.getPlayer() == null
				|| event.isSimulated()
				|| event.getToolAction() != ToolActions.HOE_TILL)
			return;

		boolean isHoeDisabled = disabledHoes(event);
		if (event.getPlayer() != null && event.getPlayer().level().isClientSide)
			return;
		BlockState finalState = event.getState().getBlock().getToolModifiedState(event.getState(), event.getContext(), event.getToolAction(), true);
		if (finalState == null || (!finalState.is(Blocks.FARMLAND) && triggerOnlyForFarmland))
			return;
		if (!isHoeDisabled)
			hoesCooldown(event);
	}

	public boolean disabledHoes(BlockEvent.BlockToolModificationEvent event) {
		if (!event.getHeldItemStack().is(DISABLED_HOES))
			return false;

		//noinspection ConstantConditions getPlayer can't be null as it's called from onHoeUse that checks if player's null
		event.getPlayer().displayClientMessage(Component.translatable(TOO_WEAK), true);
		event.setCanceled(true);
		return true;
	}

	public void hoesCooldown(BlockEvent.BlockToolModificationEvent event) {
		ItemStack hoeStack = event.getHeldItemStack();
		//noinspection ConstantConditions getPlayer can't be null as it's called from onHoeUse that checks if player's null
		Player player = event.getPlayer();
        if (player == null
				|| player.getCooldowns().isOnCooldown(hoeStack.getItem()))
			return;
        for (HoeStat hoeStat : hoesStats) {
			if (hoeStat.hoe.matchesItem(hoeStack.getItem(), null)) {
				if (hoeStat.cooldown > 0) {
					//int efficiency = hoeStack.getEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY);
					int cooldown = hoeStat.cooldown /*- (efficiency * efficiencyCooldownReduction)*/;
					if (hoeStack.getItem() instanceof IHoeCooldownModifier cooldownModifier)
						cooldown = cooldownModifier.getCooldownOnUse(cooldown, player, player.level());
					if (cooldown > 0)
						player.getCooldowns().addCooldown(hoeStack.getItem(), cooldown);
				}
				if (durabilityOnTill > 1) {
					hoeStack.hurtAndBreak(durabilityOnTill - 1, player, (livingEntity) -> livingEntity.broadcastBreakEvent(livingEntity.getUsedItemHand()));
				}
				break;
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public void onBlockBreak(BlockEvent.BreakEvent event) {
		if (!this.isEnabled()
				|| !event.getState().canBeReplaced()
				|| event.getState().destroySpeed > 0f)
			return;
		for (HoeStat hoeStat : hoesStats) {
			if (hoeStat.hoe.matchesItem(event.getPlayer().getMainHandItem().getItem(), null) && hoeStat.scytheRadius > 0) {
				BlockPos.betweenClosedStream(event.getPos().offset(-hoeStat.scytheRadius, -(hoeStat.scytheRadius - 1), -hoeStat.scytheRadius), event.getPos().offset(hoeStat.scytheRadius, hoeStat.scytheRadius - 1, hoeStat.scytheRadius))
						.forEach(pos -> {
							BlockState state = event.getPlayer().level().getBlockState(pos);
							if (!state.is(ITRBlockTagsProvider.TALL_GRASS)
									|| state.destroySpeed > 0f
									|| pos.equals(event.getPos()))
								return;
							//event.getPlayer().level().addDestroyBlockEffect(pos, state);
							event.getPlayer().level().removeBlock(pos, false);
							BreakWithNoSound.send((ServerPlayer) event.getPlayer(), pos, state);
						});
				break;
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onTooltip(ItemTooltipEvent event) {
		if (!this.isEnabled())
			return;

		if (event.getItemStack().is(DISABLED_HOES)) {
			event.getToolTip().add(Component.translatable(TOO_WEAK).withStyle(ChatFormatting.RED));
		}
		else {
			for (HoeStat hoeStat : hoesStats) {
				if (!hoeStat.hoe.matchesItem(event.getItemStack().getItem(), null))
					continue;

				if (hoeStat.cooldown > 0)
					event.getToolTip().add(CommonComponents.space().append(Component.translatable(TILL_COOLDOWN, InsaneLib.ONE_DECIMAL_FORMATTER.format(hoeStat.cooldown / 20f)).withStyle(ChatFormatting.DARK_GREEN)));
				if (hoeStat.scytheRadius > 0)
					event.getToolTip().add(CommonComponents.space().append(Component.translatable(SCYTHE_RADIUS, hoeStat.scytheRadius).withStyle(ChatFormatting.DARK_GREEN)));
				break;
			}
		}
	}
}