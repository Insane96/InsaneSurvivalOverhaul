package insane96mcp.iguanatweaksreborn.module.experience;

import insane96mcp.iguanatweaksreborn.InsaneSO;
import insane96mcp.iguanatweaksreborn.data.generator.ISOBlockTagsProvider;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.module.mining.blockdefinition.BlockDefinition;
import insane96mcp.iguanatweaksreborn.module.mining.blockdefinition.BlockDefinitionReloadListener;
import insane96mcp.iguanatweaksreborn.network.message.SyncExperienceFeature;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.MinMax;
import insane96mcp.insanelib.module.base.TagsFeature;
import insane96mcp.insanelib.util.ModNBTData;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.*;
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

@LoadFeature(module = Modules.Ids.EXPERIENCE, description = "Various changes to experience. You can also use the iguanatweaks:disableExperience game rule to make experience disappear altogether.")
public class DroppedExperience extends Feature {
	public static final GameRules.Key<GameRules.BooleanValue> RULE_DISABLEEXPERIENCE = GameRules.register("iguanatweaks:disableExperience", GameRules.Category.PLAYER, GameRules.BooleanValue.create(false, (server, booleanValue) -> {
		DroppedExperience.disableExperience = booleanValue.get();
		for (ServerPlayer serverPlayer : server.getPlayerList().getPlayers()) {
			SyncExperienceFeature.sync(booleanValue.get(), serverPlayer);
		}
	}));

	public static final ResourceLocation XP_PROCESSED = InsaneSO.location("xp_processed");
	public static final TagKey<Block> NO_BLOCK_XP_MULTIPLIER = ISOBlockTagsProvider.create("no_xp_multiplier");
	public static final TagKey<EntityType<?>> NO_ENTITY_XP_MULTIPLIER = TagKey.create(Registries.ENTITY_TYPE, InsaneSO.location("no_xp_multiplier"));

	@Config(min = 0d, max = 128d, description = "ALL Experience dropped will be multiplied by this value, regardless if affected by another multiplier.\nUse the iguanatweaks:disableExperience game rule to disable experience completely.")
	public static Double globalMultiplier = 1d;

	@Config(min = 0d, max = 128d, description = "Experience dropped by blocks (Ores and Spawners) will be multiplied by this multiplier. Experience dropped by blocks are still affected by 'Global Experience Multiplier'\nCan be set to 0 to make blocks drop no experience")
	public static Double blockMultiplier = 1d;
	@Config(min = 0, max = 128d, name = "Mobs.Multiplier: Spawners", description = """
						Experience dropped from mobs that come from spawners will be multiplied by this multiplier.
						Experience dropped by mobs from spawners are still affected by 'Global Experience Multiplier'
						Can be set to 0 to disable experience drop from mob that come from spawners.""")
	public static Double mobs$multiplierSpawner = 0.5d;

	@Config(min = 0, max = 128d, name = "Mobs.Multiplier: Natural", description = """
						Experience dropped from mobs that DON'T come from spawners will be multiplied by this multiplier.
						Experience dropped from mobs that DON'T come from spawners is still affected by 'Global Experience Multiplier'
						Can be set to 0 to disable experience drop from mob that DON'T come from spawners.""")
	public static Double mobs$multiplierNatural = 1d;

	@Config(min = 0, description = "Vanilla mobs drop 1~4 xp per equipment they have.")
	public static Integer mobs$bonusExperiencePerEquipment = 2;
	@Config(min = 0, description = "This is added to 'Bonus experience per equipment'.")
	public static Integer bonusExperiencePerEnchantedEquipment = 3;

	@Config(min = 0, max = 512, name = "Bottle o' Enchanting XP", description = "Bottle o' enchanting will drop this amount of experience. Can be set to 0 to make Bottle o' enchanting drop no experience")
	public static Integer xpBottleDroppedXp = 40;

	@Config(min = 0, description = "Experience gained from harvesting Honey or Honeycombs from beehives")
	public static MinMax honeyHarvestExperience = new MinMax(3, 5);
	@Config(min = 0, description = "Experience obtained when cows or mooshrooms are milked or stewed. This only works if Fluid Cooldown is enabled.")
	public static MinMax milkXp = new MinMax(3, 5);
	@Config(min = 0, description = "Experience obtained when shearing sheep.")
	public static MinMax shearXp = new MinMax(2, 3);

	public static Boolean disableExperience = false;

	public DroppedExperience(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	public static void tryGenerateMilkXp(Entity entity) {
		if (milkXp.min > 0 || milkXp.max > 0)
			entity.level().addFreshEntity(new ExperienceOrb(entity.level(), entity.getX(), entity.getY(), entity.getZ(), milkXp.getIntRandBetween(entity.level().random)));
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onEntityJoinLevel(EntityJoinLevelEvent event) {
		if (event.getEntity() instanceof ExperienceOrb xpOrb
				&& xpOrb.level().getGameRules().getBoolean(RULE_DISABLEEXPERIENCE)) {
			event.setCanceled(true);
			return;
		}
		if (!this.isEnabled())
			return;
		if (event.getEntity() instanceof ExperienceOrb xpOrb)
			handleGlobalExperience(xpOrb);
		handleMobsMultiplier(event);
	}

	private static void handleGlobalExperience(ExperienceOrb xpOrb) {
		if (globalMultiplier == 1d
				|| ModNBTData.get(xpOrb, XP_PROCESSED, Boolean.class)
				|| xpOrb.level().isClientSide)
			return;

		if (globalMultiplier == 0d)
			xpOrb.remove(Entity.RemovalReason.KILLED);
		else
			xpOrb.value *= globalMultiplier;

		ModNBTData.put(xpOrb, XP_PROCESSED, true);
		if (xpOrb.value <= 0d)
			xpOrb.remove(Entity.RemovalReason.KILLED);
	}

	public static void handleMobsMultiplier(EntityJoinLevelEvent event) {
		if ((mobs$multiplierSpawner == 1d && mobs$multiplierNatural == 1d)
				|| !(event.getEntity() instanceof Mob mob)
				|| mob.getType().is(NO_ENTITY_XP_MULTIPLIER))
			return;

		if (TagsFeature.isSpawnType(MobSpawnType.SPAWNER, mob))
			TagsFeature.setExperienceMultiplier(mobs$multiplierSpawner, mob);
		else
			TagsFeature.setExperienceMultiplier(mobs$multiplierNatural, mob);
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
		for (BlockDefinition blockDefinition : BlockDefinitionReloadListener.DEFINITIONS) {
			if (blockDefinition.matches(event.getState())) {
				int expDropped = blockDefinition.getStateExperienceDropped(event.getLevel().getRandom());
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
				mob.xpReward += mobs$bonusExperiencePerEquipment;
				if (stack.isEnchanted())
					mob.xpReward += bonusExperiencePerEnchantedEquipment;
			}
		}
		for (ItemStack stack : mob.getHandSlots()) {
			if (!stack.isEmpty()) {
				mob.xpReward += mobs$bonusExperiencePerEquipment;
				if (stack.isEnchanted())
					mob.xpReward += bonusExperiencePerEnchantedEquipment;
			}
		}
	}

	public static int getXpBottleDroppedExperience(ThrownExperienceBottle thrownExperienceBottle) {
		return xpBottleDroppedXp;
	}

	@SubscribeEvent
	public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
		SyncExperienceFeature.sync(event.getEntity().level().getGameRules().getBoolean(RULE_DISABLEEXPERIENCE), (ServerPlayer) event.getEntity());
	}

	//Render before Regenerating absorption
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent(priority = EventPriority.HIGH)
	public void removeExperienceBar(final RenderGuiOverlayEvent.Pre event) {
		if (!disableExperience
				|| Minecraft.getInstance().player == null)
			return;

		if (Minecraft.getInstance().player.jumpableVehicle() == null && event.getOverlay().equals(VanillaGuiOverlay.VIGNETTE.type())) {
			((ForgeGui) Minecraft.getInstance().gui).rightHeight -= 6;
			((ForgeGui) Minecraft.getInstance().gui).leftHeight -= 6;
		}
		else if (event.getOverlay().equals(VanillaGuiOverlay.EXPERIENCE_BAR.type()))
			event.setCanceled(true);
	}
}