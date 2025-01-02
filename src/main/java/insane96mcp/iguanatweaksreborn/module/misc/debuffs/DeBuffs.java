package insane96mcp.iguanatweaksreborn.module.misc.debuffs;

import insane96mcp.iguanatweaksreborn.InsaneSurvivalOverhaul;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.insanelib.base.JsonFeature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.util.MCUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

@Label(name = "DeBuffs", description = "Apply potion effects on certain hunger / health / experience level. Debuffs are controlled via json in this feature's folder")
@LoadFeature(module = Modules.Ids.MISC, enabledByDefault = false)
public class DeBuffs extends JsonFeature {
	public static final ArrayList<DeBuff> DEBUFFS_DEFAULT = new ArrayList<>(List.of(
			new DeBuff(DeBuff.Stat.HEALTH, Double.MIN_VALUE, 2d, MobEffects.DIG_SLOWDOWN, 0),
			new DeBuff(DeBuff.Stat.HUNGER, Double.MIN_VALUE, 6d, MobEffects.DIG_SLOWDOWN, 0)
	));
	public static final ArrayList<DeBuff> deBuffs = new ArrayList<>();

	public DeBuffs(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
		JSON_CONFIGS.add(new JsonConfig<>("debuffs.json", deBuffs, DEBUFFS_DEFAULT, DeBuff.LIST_TYPE));
	}

	@Override
	public String getModConfigFolder() {
		return InsaneSurvivalOverhaul.CONFIG_FOLDER;
	}

	@SubscribeEvent
	public void deBuffsOnLowStats(TickEvent.PlayerTickEvent event) {
		if (!this.isEnabled()
				|| event.phase != TickEvent.Phase.START
				|| deBuffs.isEmpty()
				|| event.player.level().isClientSide)
			return;

		ServerPlayer player = (ServerPlayer) event.player;

		if (!player.isAlive()
				|| player.tickCount % 20 != 0) return;

		for (DeBuff deBuff : deBuffs) {
			boolean pass = false;
			switch (deBuff.stat) {
				case HEALTH:
					if (player.getHealth() <= deBuff.max && player.getHealth() >= deBuff.min)
						pass = true;
					break;

				case HUNGER:
					if (player.getFoodData().getFoodLevel() <= deBuff.max && player.getFoodData().getFoodLevel() >= deBuff.min)
						pass = true;
					break;

				case EXPERIENCE_LEVEL:
					if (player.experienceLevel <= deBuff.max && player.experienceLevel >= deBuff.min)
						pass = true;
					break;
				default:
					break;
			}
			if (pass) {
				MobEffectInstance effectInstance = MCUtils.createEffectInstance(deBuff.effect, 30, deBuff.amplifier, true, false, true, false);
				player.addEffect(effectInstance);
			}
		}
	}
}