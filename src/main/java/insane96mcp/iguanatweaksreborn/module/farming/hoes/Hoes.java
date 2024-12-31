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

	public static final ArrayList<HoeDefinition> HOES_DEFINITION_DEFAULT = new ArrayList<>(List.of(
			new HoeDefinition(IdTagMatcher.newId("minecraft:wooden_hoe"), 1),
			new HoeDefinition(IdTagMatcher.newId("minecraft:stone_hoe"), 0),
			new HoeDefinition(IdTagMatcher.newId("iguanatweaksexpanded:flint_hoe"), 0),
			new HoeDefinition(IdTagMatcher.newId("iguanatweaksexpanded:copper_hoe"), 1),
			new HoeDefinition(IdTagMatcher.newId("minecraft:golden_hoe"), 2),
			new HoeDefinition(IdTagMatcher.newId("minecraft:iron_hoe"), 1),
			new HoeDefinition(IdTagMatcher.newId("iguanatweaksexpanded:solarium_hoe"), 1),
			new HoeDefinition(IdTagMatcher.newId("iguanatweaksexpanded:durium_hoe"), 0),
			new HoeDefinition(IdTagMatcher.newId("iguanatweaksexpanded:coated_copper_hoe"), 1),
			new HoeDefinition(IdTagMatcher.newId("iguanatweaksexpanded:quaron_hoe"), 1),
			new HoeDefinition(IdTagMatcher.newId("iguanatweaksexpanded:keego_hoe"), 2),
			new HoeDefinition(IdTagMatcher.newId("minecraft:diamond_hoe"), 2),
			new HoeDefinition(IdTagMatcher.newId("iguanatweaksexpanded:soul_steel_hoe"), 2),
			new HoeDefinition(IdTagMatcher.newId("minecraft:netherite_hoe"), 2)
	));

	public static final ArrayList<HoeDefinition> hoeDefinitions = new ArrayList<>();

	@Config(min = 1)
	@Label(name = "Durability used on right-click")
	public static Integer durabilityOnRightClick = 4;
	@Config
	@Label(name = "Extra durability only for tilling", description = "'Durability used on right-click' is only applied for farmland, and not e.g. when using hoes on rooted dirt")
	public static Boolean extraDurabilityOnlyForTilling = true;

	public Hoes(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
		addSyncType(new ResourceLocation(IguanaTweaksReborn.MOD_ID, "hoes_definitions"), new SyncType(json -> loadAndReadJson(json, hoeDefinitions, HOES_DEFINITION_DEFAULT, HoeDefinition.LIST_TYPE)));
		JSON_CONFIGS.add(new JsonConfig<>("hoes_definitions.json", hoeDefinitions, HOES_DEFINITION_DEFAULT, HoeDefinition.LIST_TYPE, true, new ResourceLocation(IguanaTweaksReborn.MOD_ID, "hoes_definitions")));
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
		if (finalState == null || (!finalState.is(Blocks.FARMLAND) && extraDurabilityOnlyForTilling))
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
        for (HoeDefinition hoeDefinition : hoeDefinitions) {
			if (hoeDefinition.hoe.matchesItem(hoeStack.getItem(), null)) {
				if (hoeDefinition.cooldown > 0) {
					//int efficiency = hoeStack.getEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY);
					int cooldown = hoeDefinition.cooldown /*- (efficiency * efficiencyCooldownReduction)*/;
					if (hoeStack.getItem() instanceof IHoeCooldownModifier cooldownModifier)
						cooldown = cooldownModifier.getCooldownOnUse(cooldown, player, player.level());
					if (cooldown > 0)
						player.getCooldowns().addCooldown(hoeStack.getItem(), cooldown);
				}
				if (durabilityOnRightClick > 1) {
					hoeStack.hurtAndBreak(durabilityOnRightClick - 1, player, (livingEntity) -> livingEntity.broadcastBreakEvent(livingEntity.getUsedItemHand()));
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
		for (HoeDefinition hoeDefinition : hoeDefinitions) {
			if (hoeDefinition.hoe.matchesItem(event.getPlayer().getMainHandItem().getItem(), null) && hoeDefinition.scytheRadius > 0) {
				BlockPos.betweenClosedStream(event.getPos().offset(-hoeDefinition.scytheRadius, -(hoeDefinition.scytheRadius - 1), -hoeDefinition.scytheRadius), event.getPos().offset(hoeDefinition.scytheRadius, hoeDefinition.scytheRadius - 1, hoeDefinition.scytheRadius))
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
			for (HoeDefinition hoeDefinition : hoeDefinitions) {
				if (!hoeDefinition.hoe.matchesItem(event.getItemStack().getItem(), null))
					continue;

				if (hoeDefinition.cooldown > 0)
					event.getToolTip().add(CommonComponents.space().append(Component.translatable(TILL_COOLDOWN, InsaneLib.ONE_DECIMAL_FORMATTER.format(hoeDefinition.cooldown / 20f)).withStyle(ChatFormatting.DARK_GREEN)));
				if (hoeDefinition.scytheRadius > 0)
					event.getToolTip().add(CommonComponents.space().append(Component.translatable(SCYTHE_RADIUS, hoeDefinition.scytheRadius).withStyle(ChatFormatting.DARK_GREEN)));
				break;
			}
		}
	}
}