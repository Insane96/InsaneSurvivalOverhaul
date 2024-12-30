package insane96mcp.iguanatweaksreborn.module.experience;

import insane96mcp.iguanatweaksreborn.IguanaTweaksReborn;
import insane96mcp.iguanatweaksreborn.data.generator.ITRBlockTagsProvider;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.module.mining.blockdata.BlockData;
import insane96mcp.iguanatweaksreborn.module.mining.blockdata.BlockDataReloadListener;
import insane96mcp.iguanatweaksreborn.network.message.SyncExperienceFeature;
import insane96mcp.insanelib.base.JsonFeature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.MinMax;
import insane96mcp.insanelib.setup.ILStrings;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.ThrownExperienceBottle;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Experience", description = "Various changes to experience. You can also use the iguanatweaks:disableExperience game rule to disable experience drops and experience bar.")
@LoadFeature(module = Modules.Ids.EXPERIENCE)
public class Experience extends JsonFeature {
	public static final GameRules.Key<GameRules.BooleanValue> RULE_DISABLEEXPERIENCE = GameRules.register("iguanatweaks:disableExperience", GameRules.Category.PLAYER, GameRules.BooleanValue.create(false, (server, booleanValue) -> {
		Experience.disableExperience = booleanValue.get();
		for (ServerPlayer serverPlayer : server.getPlayerList().getPlayers()) {
			SyncExperienceFeature.sync(booleanValue.get(), serverPlayer);
		}
	}));

	public static final String XP_PROCESSED = IguanaTweaksReborn.RESOURCE_PREFIX + "xp_processed";
	public static final TagKey<Block> NO_BLOCK_XP_MULTIPLIER = ITRBlockTagsProvider.create("no_xp_multiplier");
	public static final TagKey<EntityType<?>> NO_ENTITY_XP_MULTIPLIER = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(IguanaTweaksReborn.MOD_ID, "no_xp_multiplier"));

	@Config(min = 1d, max = 128d)
	@Label(name = "Global Experience Multiplier", description = "ALL Experience dropped will be multiplied by this value, regardless if affected by another multiplier.\nUse the iguanatweaks:disableExperience game rule to disable experience completely.")
	public static Double globalMultiplier = 1d;

	@Config(min = 0d, max = 128d)
	@Label(name = "Experience from Blocks Multiplier", description = "Experience dropped by blocks (Ores and Spawners) will be multiplied by this multiplier. Experience dropped by blocks are still affected by 'Global Experience Multiplier'\nCan be set to 0 to make blocks drop no experience")
	public static Double blockMultiplier = 1d;
	@Config(min = 0, max = 128d)
	@Label(name = "Mobs from Spawners Multiplier", description = """
						Experience dropped from mobs that come from spawners will be multiplied by this multiplier.
						Experience dropped by mobs from spawners are still affected by 'Global Experience Multiplier'
						Can be set to 0 to disable experience drop from mob that come from spawners.""")
	public static Double mobsFromSpawnersMultiplier = 0.5d;

	@Config(min = 0, max = 128d)
	@Label(name = "Natural Mobs Multiplier", description = """
						Experience dropped from mobs that DON'T come from spawners will be multiplied by this multiplier.
						Experience dropped from mobs that DON'T come from spawners is still affected by 'Global Experience Multiplier'
						Can be set to 0 to disable experience drop from mob that DON'T come from spawners.""")
	public static Double naturalMobsMultiplier = 1d;

	@Config(min = 0)
	@Label(name = "Bonus experience per equipment", description = "Vanilla mobs drop 1~4 xp per equipment they have.")
	public static Integer bonusExperiencePerEquipment = 2;
	@Config(min = 0)
	@Label(name = "Bonus experience per enchanted equipment", description = "This is added to 'Bonus experience per equipment'.")
	public static Integer bonusExperiencePerEnchantedEquipment = 2;

	@Config(min = 0, max = 512)
	@Label(name = "Bottle o' Enchanting Bonus XP", description = "Bottle o' enchanting will drop this more XP. Experience is still affected by 'Global Experience Multiplier'\nCan be set to 0 to make Bottle o' enchanting drop no bonus experience")
	public static Integer xpBottleBonus = 35;

	@Config(min = 0)
	@Label(name = "Honey Harvest Experience", description = "Experience gained from harvesting Honey or Honeycombs from beehives")
	public static MinMax honeyHarvestExperience = new MinMax(5, 10);

	/*public static final ArrayList<IdTagRange> CUSTOM_BLOCKS_EXPERIENCE_DEFAULT = new ArrayList<>(List.of(
			IdTagRange.newTag("minecraft:copper_ores", 0, 2),
			IdTagRange.newTag("minecraft:iron_ores", 1, 2),
			IdTagRange.newTag("minecraft:gold_ores", 2, 3),

			IdTagRange.newId("minecraft:sculk_catalyst", 35, 35),
			IdTagRange.newId("minecraft:spawner", 70, 70)
	));

	public static final ArrayList<IdTagRange> customBlocksExperience = new ArrayList<>();*/

	public static Boolean disableExperience = false;

	public Experience(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
		//JSON_CONFIGS.add(new JsonConfig<>("blocks_experience.json", customBlocksExperience, CUSTOM_BLOCKS_EXPERIENCE_DEFAULT, IdTagRange.LIST_TYPE));
	}

	@Override
	public String getModConfigFolder() {
		return IguanaTweaksReborn.CONFIG_FOLDER;
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onXPOrbDrop(EntityJoinLevelEvent event) {
		if (!this.isEnabled())
			return;

		if (event.getEntity() instanceof ExperienceOrb xpOrb) {
			if (disableExperience)
				event.setCanceled(true);

			handleGlobalExperience(xpOrb);
		}
		handleMobsMultiplier(event);
	}

	private static void handleGlobalExperience(ExperienceOrb xpOrb) {
		if (globalMultiplier == 1.0d
				|| xpOrb.getPersistentData().getBoolean(XP_PROCESSED)
				|| xpOrb.level().isClientSide)
			return;

		if (globalMultiplier == 0d)
			xpOrb.remove(Entity.RemovalReason.KILLED);
		else
			xpOrb.value *= globalMultiplier;

		xpOrb.getPersistentData().putBoolean(XP_PROCESSED, true);
		if (xpOrb.value <= 0d)
			xpOrb.remove(Entity.RemovalReason.KILLED);
	}

	public static void handleMobsMultiplier(EntityJoinLevelEvent event) {
		if ((mobsFromSpawnersMultiplier == 1d && naturalMobsMultiplier == 1d)
				|| !(event.getEntity() instanceof Mob mob)
				|| mob.getType().is(NO_ENTITY_XP_MULTIPLIER))
			return;

		if (mob.getPersistentData().getBoolean(ILStrings.Tags.SPAWNED_FROM_SPAWNER))
			mob.getPersistentData().putDouble(ILStrings.Tags.EXPERIENCE_MULTIPLIER, mobsFromSpawnersMultiplier);
		else
			mob.getPersistentData().putDouble(ILStrings.Tags.EXPERIENCE_MULTIPLIER, naturalMobsMultiplier);
	}

	//Run before smartness
	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onBlockXPDrop(BlockEvent.BreakEvent event) {
		if (!this.isEnabled()
				|| event.getState().is(NO_BLOCK_XP_MULTIPLIER))
			return;

		handleBlockDrop(event);
		handleMultiplier(event);
	}

	private static void handleBlockDrop(BlockEvent.BreakEvent event) {
		int silkTouchLevel = event.getPlayer().getMainHandItem().getEnchantmentLevel(Enchantments.SILK_TOUCH);
		if (silkTouchLevel > 0)
			return;
		for (BlockData blockData : BlockDataReloadListener.DATA) {
			if (blockData.matches(event.getState())) {
				int expDropped = blockData.getStateExperienceDropped(event.getLevel().getRandom());
				if (expDropped > -1)
					event.setExpToDrop(expDropped);
			}
		}
	}

	private static void handleMultiplier(BlockEvent.BreakEvent event) {
		if (blockMultiplier == 1d)
			return;
		int xpToDrop = event.getExpToDrop();
		xpToDrop *= blockMultiplier;
		event.setExpToDrop(xpToDrop);
	}

	// In vanilla, mobs drop loot before checking if they should drop more experience due to gear, this makes them never drop more experience if they drop equipment
	// This sets the xp reward before the loot drops (also changes the xp reward from 1~4 per equipment to 2 (+2 if the item is enchanted))
	@SubscribeEvent
	public void fixEquipmentExperience(LivingDeathEvent event) {
		if (!(event.getEntity() instanceof Mob mob)
				|| mob.xpReward <= 0)
			return;

		for (ItemStack stack : mob.getArmorSlots()) {
			if (!stack.isEmpty()) {
				mob.xpReward += bonusExperiencePerEquipment;
				if (stack.isEnchanted())
					mob.xpReward += bonusExperiencePerEnchantedEquipment;
			}
		}
		for (ItemStack stack : mob.getHandSlots()) {
			if (!stack.isEmpty()) {
				mob.xpReward += bonusExperiencePerEquipment;
				if (stack.isEnchanted())
					mob.xpReward += bonusExperiencePerEnchantedEquipment;
			}
		}
	}

	public static void onXpBottleHit(ThrownExperienceBottle xpBottle) {
		if (!isEnabled(Experience.class)
				|| xpBottleBonus == 0)
			return;

		if (xpBottle.level() instanceof ServerLevel)
			ExperienceOrb.award((ServerLevel)xpBottle.level(), xpBottle.position(), xpBottleBonus);
	}

	@SubscribeEvent
	public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
		SyncExperienceFeature.sync(event.getEntity().level().getGameRules().getBoolean(RULE_DISABLEEXPERIENCE), (ServerPlayer) event.getEntity());
	}

	//Render before Regenerating absorption
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent(priority = EventPriority.HIGH)
	public void removeExperienceBar(final RenderGuiOverlayEvent.Pre event) {
		if (!this.isEnabled()
			|| !disableExperience)
			return;

		if (Minecraft.getInstance().player.jumpableVehicle() == null && event.getOverlay().equals(VanillaGuiOverlay.VIGNETTE.type())) {
			((ForgeGui) Minecraft.getInstance().gui).rightHeight -= 6;
			((ForgeGui) Minecraft.getInstance().gui).leftHeight -= 6;
		}
		else if (event.getOverlay().equals(VanillaGuiOverlay.EXPERIENCE_BAR.type()))
			event.setCanceled(true);
	}
}