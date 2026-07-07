package insane96mcp.insanesurvivaloverhaul.module.death;

import insane96mcp.insanelib.core.ModNBTData;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.module.base.NbtTags;
import insane96mcp.insanelib.world.scheduled.ScheduledTasks;
import insane96mcp.insanelib.world.scheduled.ScheduledTickTask;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import net.minecraft.ChatFormatting;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.GameRules;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@LoadFeature(module = ISOModules.DEATH,
		description = "The mob that kills a player will not despawn, and when killed will drop more experience.")
public class PlayerKillerBounty extends Feature {
	public static ResourceLocation KILLED_PLAYERS;
	public static final String PLAYER_KILLER_LANG = InsaneSO.lang("player_killer");
	public static final String PLAYERS_KILLER_LANG = InsaneSO.lang("players_killer");
	public static final String VINDICATED_LANG = InsaneSO.lang("vindicated");
	public static final TagKey<EntityType<?>> KILLER_BLACKLIST = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(InsaneSO.MOD_ID, "killer_blacklist"));

	@Config(name = "Experience Multiplier", description = "Base experience multiplier applied when the bounty mob kills its first player. Each additional unique player killed adds this value again (additive).")
	public static Double experienceMultiplier = 5d;

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		KILLED_PLAYERS = this.createDataKey("killed_players");
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onPlayerDeath(LivingDeathEvent event) {
		if (!this.isEnabled()
				|| !(event.getEntity() instanceof ServerPlayer player)
				|| !(event.getSource().getEntity() instanceof Mob killer)
				|| isPlayerInKilledList(killer, player.getUUID())
				|| killer.isRemoved()
				|| killer.isDeadOrDying()
				|| killer.getType().is(KILLER_BLACKLIST))
			return;

		boolean firstKill = !ModNBTData.contains(killer, KILLED_PLAYERS);

		ScheduledTasks.schedule(new ScheduledTickTask(1) {
			@Override
			public void run() {
				if (firstKill) {
					killer.setPersistenceRequired();
					double xpMult = experienceMultiplier;
					if (ModNBTData.contains(killer, NbtTags.EXPERIENCE_MULTIPLIER))
						xpMult *= ModNBTData.get(killer, NbtTags.EXPERIENCE_MULTIPLIER, Double.class);
					NbtTags.setExperienceMultiplier(xpMult, killer);
				} else {
					double xpMult = ModNBTData.get(killer, NbtTags.EXPERIENCE_MULTIPLIER, Double.class);
					NbtTags.setExperienceMultiplier(xpMult + experienceMultiplier, killer);
				}
				addPlayerToKilledList(killer, player.getUUID());
				Component mobName = killer.getType().getDescription();
				Component name = firstKill
						? Component.translatable(PLAYER_KILLER_LANG, player.getGameProfile().getName(), mobName)
						: Component.translatable(PLAYERS_KILLER_LANG, mobName);
				killer.setCustomName(name.copy().withStyle(ChatFormatting.GRAY));
			}
		});
	}

	@SubscribeEvent
	public void onPlayerKillerDeath(LivingDeathEvent event) {
		if (!this.isEnabled()
				|| !(event.getEntity() instanceof Mob mob)
				|| !mob.level().getGameRules().getBoolean(GameRules.RULE_SHOWDEATHMESSAGES)
				|| !ModNBTData.contains(mob, KILLED_PLAYERS)
				|| mob.level().isClientSide)
			return;

		var server = mob.level().getServer();
		if (server == null)
			return;

		Component deathMessage = event.getEntity().getCombatTracker().getDeathMessage();
		server.getPlayerList().broadcastSystemMessage(deathMessage, false);

		ListTag killedPlayers = ModNBTData.getList(mob, KILLED_PLAYERS, Tag.TAG_INT_ARRAY);
		List<String> names = new ArrayList<>();
		for (Tag entry : killedPlayers) {
			UUID uuid = UUIDUtil.uuidFromIntArray(((IntArrayTag) entry).getAsIntArray());
			//noinspection DataFlowIssue
			server.getProfileCache().get(uuid).ifPresent(profile -> names.add(profile.getName()));
		}
		if (names.size() > 1) {
			Component vindicatedMessage = Component.translatable(VINDICATED_LANG, String.join(", ", names));
			server.getPlayerList().broadcastSystemMessage(vindicatedMessage, false);
		}
	}

	public static boolean isPlayerInKilledList(Mob killer, UUID playerUUID) {
		ListTag list = ModNBTData.getList(killer, KILLED_PLAYERS, Tag.TAG_INT_ARRAY);
		for (Tag entry : list) {
			if (UUIDUtil.uuidFromIntArray(((IntArrayTag) entry).getAsIntArray()).equals(playerUUID))
				return true;
		}
		return false;
	}

	public static void addPlayerToKilledList(Mob killer, UUID playerUUID) {
		ListTag list = ModNBTData.getList(killer, KILLED_PLAYERS, Tag.TAG_INT_ARRAY);
		list.add(new IntArrayTag(UUIDUtil.uuidToIntArray(playerUUID)));
		ModNBTData.put(killer, KILLED_PLAYERS, list);
	}
}
