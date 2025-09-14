package insane96mcp.iguanatweaksreborn.module.misc;

import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
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
}