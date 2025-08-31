package insane96mcp.iguanatweaksreborn.module.misc;

import insane96mcp.iguanatweaksreborn.InsaneSO;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.util.ModNBTData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.loading.FMLLoader;

@LoadFeature(module = Modules.Ids.MISC, description = "Various Nerfs")
public class Nerfs extends Feature {
	public static ResourceLocation LAST_FISHING_POS_TAG;
	public static ResourceLocation LAST_FISHING_COUNT_TAG;
	@Config(description = "If true, Iron golems will only drop Iron when killed by the player.")
	public static Boolean ironFromGolemsRequiresPlayer = true;
	@Config(description = "If true, renderDebugInfo is enabled by default. Requires a world restart.")
	public static Boolean noCoordinates = true;
	@Config(description = "If true, maxEntityCramming game rule is set to 6 from 24")
	public static Boolean reducedMobCramming = true;
	@Config(description = "Kelp blocks smelt 16 items instead of 20")
	public static Boolean lessBurnTimeForKelpBlock = true;

	@Config(description = "Prevents duping falling blocks when they travel across dimensions. (If quark is present this is disabled)")
	public static Boolean removeFallingBlockDupe = true;
	@Config(description = "Fixes several piston physics exploits like TNT duping. (If quark is present this is disabled)")
	public static Boolean removePistonPhysicsExploit = true;

	@Config(min = 0d, max = 1d, name = "Fishing has a chance to fish a guardian")
	public static Double fishingGuardianChance = 0d;
	@Config(description = "If enabled after fishing for a few times in the same spot you won't be able to fish again unless you move in another spot")
	public static Boolean antiFishingFarms = true;

	@Config(min = 0, max = 1, description = "When an entity is hit and on a mount they have this chance to fall")
	public static Double fallFromMountChance = 0.2d;
	@Config(description = "If true, only players are affected by 'Fall from mount chance'")
	public static Boolean fallFromMountPlayerOnly = true;

	@Config(min = 0, name = "Prone mining speed multiplier", description = "When prone your mining speed is multiplied by this")
	public static Double proneMiningSpeedMultiplier = 0.5d;

    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		LAST_FISHING_POS_TAG = this.createDataKey("last_fishing_pos");
		LAST_FISHING_COUNT_TAG = this.createDataKey("last_fishing_count");
	}

	@Override
	public void readConfig(ModConfigEvent event) {
		super.readConfig(event);
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

		if (ironFromGolemsRequiresPlayer && event.getEntity() instanceof IronGolem && !(event.getSource().getDirectEntity() instanceof Player))
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

		if (fallFromMountPlayerOnly && !(event.getEntity() instanceof Player))
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
		Player owner = event.getHookEntity().getPlayerOwner();
		int[] aPos = ModNBTData.getPersisted(owner, LAST_FISHING_POS_TAG, int[].class);
		if (aPos.length == 3) {
			BlockPos lastFishingPos = new BlockPos(aPos[0], aPos[1], aPos[2]);
			int distance = lastFishingPos.distManhattan(event.getHookEntity().blockPosition());
			int lastFishingCount = ModNBTData.getPersisted(owner, LAST_FISHING_COUNT_TAG, Integer.class);
			if (distance <= 6) {
				lastFishingCount++;
				if (lastFishingCount >= 8) {
					event.setCanceled(true);
					event.getHookEntity().getPlayerOwner().displayClientMessage(Component.translatable(InsaneSO.lang("too_much_fishing_in_this_spot")), true);
				}
				ModNBTData.putPersisted(owner, LAST_FISHING_COUNT_TAG, lastFishingCount);
			}
			else {
				ModNBTData.putPersisted(owner, LAST_FISHING_COUNT_TAG, 0);
				ModNBTData.putPersisted(owner, LAST_FISHING_POS_TAG, new int[] {event.getHookEntity().getBlockX(), event.getHookEntity().getBlockY(), event.getHookEntity().getBlockZ()});
			}
		}
		else {
			ModNBTData.putPersisted(owner, LAST_FISHING_POS_TAG, new int[] {event.getHookEntity().getBlockX(), event.getHookEntity().getBlockY(), event.getHookEntity().getBlockZ()});
		}
	}

	public static void trySummonGuardian(ItemFishedEvent event) {
		if (fishingGuardianChance == 0d
				|| event.getHookEntity().level().random.nextFloat() > fishingGuardianChance)
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