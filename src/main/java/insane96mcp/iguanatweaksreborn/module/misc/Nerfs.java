package insane96mcp.iguanatweaksreborn.module.misc;

import insane96mcp.iguanatweaksreborn.IguanaTweaksReborn;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.ItemFishedEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.loading.FMLLoader;

@Label(name = "Nerfs", description = "Various Nerfs")
@LoadFeature(module = Modules.Ids.MISC)
public class Nerfs extends Feature {
	static final String LAST_FISHING_POS_TAG = IguanaTweaksReborn.RESOURCE_PREFIX + "last_fishing_pos";
	static final String LAST_FISHING_COUNT_TAG = IguanaTweaksReborn.RESOURCE_PREFIX + "last_fishing_count";
	@Config
	@Label(name = "Iron from Golems only when killed by Players", description = "If true, Iron golems will only drop Iron when killed by the player.")
	public static Boolean ironRequiresPlayer = true;
	@Config
	@Label(name = "No Coordinates", description = "If true, renderDebugInfo is enabled by default. Requires a world restart")
	public static Boolean noCoordinates = true;
	@Config
	@Label(name = "Reduced Random Tick Speed", description = "If true, randomTickSpeed is set to 2 from 3")
	public static Boolean reducedRandomTickSpeed = true;
	@Config
	@Label(name = "Reduced mob cramming", description = "If true, maxEntityCramming game rule is set to 6 from 24")
	public static Boolean reducedMobCramming = true;
	@Config
	@Label(name = "Less burn time for Kelp block", description = "Kelp blocks smelt 16 items instead of 20")
	public static Boolean lessBurnTimeForKelpBlock = true;

	@Config
	@Label(name = "Remove Falling Block Dupe across dimensions", description = "Prevents duping falling blocks when they travel across dimensions. (If quark is present this is disabled)")
	public static Boolean removeFallingBlockDupe = true;
	@Config
	@Label(name = "Remove piston physics exploit", description = "Fixes several piston physics exploits like TNT duping. (If quark is present this is disabled)")
	public static Boolean removePistonPhysicsExploit = true;

	@Config(min = 0d, max = 1d)
	@Label(name = "Fishing has a chance to fish a guardian")
	public static Double fishingCreatureChance = 0d;
	@Config
	@Label(name = "No fish if fishing in the same spot", description = "If enabled after fishing for a few times in the same spot you won't be able to fish again unless you move in another spot")
	public static Boolean antiFishingFarms = true;

	@Config(min = 0, max = 1)
	@Label(name = "Fall from mount chance", description = "When an entity is hit and on a mount they have this chance to fall")
	public static Double fallFromMountChance = 0.2d;
	@Config
	@Label(name = "Fall from mount player only", description = "If true, only players are affected by 'Fall from mount chance'")
	public static Boolean fallFromMountPlayerOnly = true;

	@Config(min = 0)
	@Label(name = "Prone mining speed multiplier", description = "When prone your mining speed is multiplied by this")
	public static Double proneMiningSpeedMultiplier = 0.5d;

    public Nerfs(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	public static boolean isFallingBlockDupeRemoved() {
		return isEnabled(Nerfs.class) && removeFallingBlockDupe;
	}

	public static boolean isPistonPhysicsExploitEnabled() {
		return isEnabled(Nerfs.class) && removePistonPhysicsExploit;
	}

	@SubscribeEvent
	public void onLivingDrop(LivingDropsEvent event) {
		if (!this.isEnabled())
			return;

		if (ironRequiresPlayer && event.getEntity() instanceof IronGolem && !(event.getSource().getDirectEntity() instanceof Player))
			event.getDrops().removeIf(itemEntity -> itemEntity.getItem().is(Items.IRON_INGOT));
	}

	@SubscribeEvent
	public void onBreakSpeed(PlayerEvent.BreakSpeed event) {
		if (!this.isEnabled()
				|| proneMiningSpeedMultiplier == 0
				|| event.getEntity().getPose() != Pose.SWIMMING)
			return;

		event.setNewSpeed(event.getNewSpeed() * proneMiningSpeedMultiplier.floatValue());
	}

	@SubscribeEvent
	public void onPlayerHit(LivingDamageEvent event) {
		if (!this.isEnabled()
				|| !event.getEntity().isPassenger()
				|| !(event.getSource().getEntity() instanceof LivingEntity
				|| event.getEntity().level().isClientSide)
				|| fallFromMountChance == 0)
			return;

		if (fallFromMountPlayerOnly && !(event.getSource().getEntity() instanceof Player))
			return;

		if (event.getEntity().getRandom().nextFloat() < fallFromMountChance) {
			event.getEntity().stopRiding();
			event.getEntity().level().playSound(null, event.getEntity(), SoundEvents.ARMOR_EQUIP_GENERIC, event.getEntity().getSoundSource(), 1f, 0.5f);
		}
	}

	@SubscribeEvent
	public void onServerStarted(ServerStartedEvent event) {
		if (!this.isEnabled())
			return;

		if (noCoordinates && FMLLoader.isProduction())
			event.getServer().getGameRules().getRule(GameRules.RULE_REDUCEDDEBUGINFO).set(true, event.getServer());
		if (reducedRandomTickSpeed)
			event.getServer().getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(2, event.getServer());
		if (reducedMobCramming)
			event.getServer().getGameRules().getRule(GameRules.RULE_MAX_ENTITY_CRAMMING).set(6, event.getServer());
	}

	@SubscribeEvent
	public void onFuelBurnTime(FurnaceFuelBurnTimeEvent event) {
		if (!this.isEnabled()
				|| !lessBurnTimeForKelpBlock
				|| !event.getItemStack().is(Items.DRIED_KELP_BLOCK))
			return;

		event.setBurnTime(3200);
	}

	@SubscribeEvent
	public void onRetrieveBobber(ItemFishedEvent event) {
		if (!this.isEnabled())
			return;

		trySummonGuardian(event);
		nerfAutoFishFarm(event);
	}

	public static void nerfAutoFishFarm(ItemFishedEvent event) {
		if (!antiFishingFarms
				|| event.getHookEntity().getPlayerOwner() == null)
			return;
		CompoundTag persistentData = event.getHookEntity().getPlayerOwner().getPersistentData();
		if (persistentData.contains(LAST_FISHING_POS_TAG)) {
			BlockPos lastFishingPos = new BlockPos(persistentData.getIntArray(LAST_FISHING_POS_TAG)[0], persistentData.getIntArray(LAST_FISHING_POS_TAG)[1], persistentData.getIntArray(LAST_FISHING_POS_TAG)[2]);
			int distance = lastFishingPos.distManhattan(event.getHookEntity().blockPosition());
			int lastFishingCount = persistentData.getInt(LAST_FISHING_COUNT_TAG);
			if (distance <= 6) {
				lastFishingCount++;
				if (lastFishingCount >= 10) {
					event.setCanceled(true);
					event.getHookEntity().getPlayerOwner().displayClientMessage(Component.translatable(IguanaTweaksReborn.MOD_ID + ".too_much_fishing_in_this_spot"), true);
				}
				persistentData.putInt(LAST_FISHING_COUNT_TAG, lastFishingCount);
			}
			else {
				persistentData.putIntArray(LAST_FISHING_POS_TAG, new int[] {event.getHookEntity().getBlockX(), event.getHookEntity().getBlockY(), event.getHookEntity().getBlockZ()});
				persistentData.putInt(LAST_FISHING_COUNT_TAG, 0);
			}
		}
		else {
			persistentData.putIntArray(LAST_FISHING_POS_TAG, new int[] {event.getHookEntity().getBlockX(), event.getHookEntity().getBlockY(), event.getHookEntity().getBlockZ()});
		}
	}

	public static void trySummonGuardian(ItemFishedEvent event) {
		if (fishingCreatureChance == 0d
				|| event.getHookEntity().level().random.nextFloat() > fishingCreatureChance)
			return;
		LivingEntity guardian = EntityType.GUARDIAN.create(event.getHookEntity().level());
		guardian.setPos(event.getHookEntity().position().add(0, guardian.getBbHeight(), 0));
		Player player = event.getHookEntity().getPlayerOwner();
		double d0 = player.getX() - event.getHookEntity().getX();
		double d1 = player.getY() - event.getHookEntity().getY();
		double d2 = player.getZ() - event.getHookEntity().getZ();
		guardian.setDeltaMovement(d0 * 0.1D, d1 * 0.1D + Math.sqrt(Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2)) * 0.08D, d2 * 0.1D);
		event.getHookEntity().level().addFreshEntity(guardian);
		event.setCanceled(true);
	}
}