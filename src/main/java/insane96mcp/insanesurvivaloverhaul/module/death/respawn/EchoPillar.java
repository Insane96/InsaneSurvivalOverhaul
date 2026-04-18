package insane96mcp.insanesurvivaloverhaul.module.death.respawn;

import insane96mcp.insanelib.core.JsonFeature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.data.SerializableMobEffectInstance;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.data.criterion.ActivateEchoPillarTrigger;
import insane96mcp.insanesurvivaloverhaul.data.generator.ISOBlockTagsProvider;
import insane96mcp.insanesurvivaloverhaul.data.generator.ISOItemTagsProvider;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.module.misc.Packs;
import insane96mcp.insanesurvivaloverhaul.setup.ISORegistries;
import insane96mcp.insanesurvivaloverhaul.setup.SimpleBlockWithItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@LoadFeature(module = ISOModules.DEATH, description = "Adds Echo Pillars: ancient structures that act as spawn anchors and grant effects on respawn.")
public class EchoPillar extends JsonFeature {
	public static final TagKey<Block> ECHO_PILLAR_BLOCKS_TO_ROT = ISOBlockTagsProvider.create("structures/echo_pillar/blocks_to_rot");
	public static final TagKey<Item> ECHO_PILLAR_CATALYST = ISOItemTagsProvider.create("echo_pillar_catalyst");
	public static final MutableComponent ECHO_PILLAR_CATALYST_MESSAGE = Component.translatable(InsaneSO.lang("echo_pillar_catalyst")).withStyle(ChatFormatting.DARK_PURPLE);
	public static final MutableComponent FAIL_ECHO_PILLAR_MESSAGE = Component.translatable(InsaneSO.lang("fail_echo_pillar"));

	public static final Supplier<ActivateEchoPillarTrigger> ACTIVATE_ECHO_PILLAR =
			ISORegistries.registerTrigger("activate_echo_pillar", ActivateEchoPillarTrigger::new);

	public static final SimpleBlockWithItem ECHO_PILLAR = SimpleBlockWithItem.register("echo_pillar", () -> new EchoPillarBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(50.0F, 1200.0F).lightLevel(EchoPillarBlock::lightLevel)));

	@Config(description = "Data pack that makes Echo Pillars generate in the world")
	public static Boolean echoPillars = true;

	public static final List<SerializableMobEffectInstance> ECHO_PILLAR_EFFECTS_DEFAULT = List.of(
			new SerializableMobEffectInstance.Builder(MobEffects.REGENERATION, 45 * 20)
					.noParticles()
					.build(),
			new SerializableMobEffectInstance.Builder(MobEffects.ABSORPTION, 60 * 20)
					.setAmplifier(1)
					.noParticles()
					.build(),
			new SerializableMobEffectInstance.Builder(MobEffects.MOVEMENT_SPEED, 60 * 20)
					.noParticles()
					.build(),
			new SerializableMobEffectInstance.Builder(MobEffects.SATURATION, 20 * 20)
					.noParticles()
					.build(),
			new SerializableMobEffectInstance.Builder(LooseRespawn.GHOSTLY, 120 * 20)
					.noParticles()
					.build()
	);

	public static final ArrayList<SerializableMobEffectInstance> echoPillarEffects = new ArrayList<>();

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		getJsonConfigs().add(new JsonFeature.JsonConfig<>("echo_pillar_effects.json", echoPillarEffects, ECHO_PILLAR_EFFECTS_DEFAULT, SerializableMobEffectInstance.LIST_TYPE));
		InsaneSO.addServerPack("echo_pillar", "Insane's Survival Overhaul Echo Pillar", () -> this.isEnabled() && !Packs.disableAllDataPacks && echoPillars);
	}

	@Override
	public String getModConfigFolder() {
		return InsaneSO.CONFIG_FOLDER;
	}

	@SubscribeEvent
	public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
		if (!this.isEnabled()
				|| event.isEndConquered())
			return;

		tryEchoPillar(event);
	}

	private void tryEchoPillar(PlayerEvent.PlayerRespawnEvent event) {
		ServerPlayer player = (ServerPlayer) event.getEntity();
		BlockPos pos = player.getRespawnPosition();
		if (pos == null
				|| !player.level().getBlockState(pos).is(ECHO_PILLAR.block().get()))
			return;

		if (!player.level().getBlockState(pos).getValue(EchoPillarBlock.ENABLED)) {
			player.sendSystemMessage(FAIL_ECHO_PILLAR_MESSAGE);
			player.setRespawnPosition(Level.OVERWORLD, null, 0f, false, false);
			return;
		}
		EchoPillarBlock.onPillarRespawn(player, player.level(), pos);
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onTooltip(ItemTooltipEvent event) {
		if (!this.isEnabled()
				|| !event.getItemStack().is(ECHO_PILLAR_CATALYST))
			return;

		event.getToolTip().add(ECHO_PILLAR_CATALYST_MESSAGE);
	}
}