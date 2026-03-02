package insane96mcp.insanesurvivaloverhaul.module.combat.unfaironeshot;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.data.criterion.UnfairOneShotTrigger;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.network.message.UnfairOneShotMessage;
import insane96mcp.insanesurvivaloverhaul.setup.ISORegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@LoadFeature(module = ISOModules.COMBAT, name = "Unfair one-shot", description = "Players live with a heart when too much damage that would kill them is dealt (only works for damage taken from mobs)")
public class UnfairOneShot extends Feature {
    public static final DeferredHolder<SoundEvent, SoundEvent> UNFAIR_ONE_SHOT = ISORegistries.SOUND_EVENTS.register("unfair_one_shot", () -> SoundEvent.createFixedRangeEvent(InsaneSO.location("unfair_one_shot"), 16f));
	public static final DeferredHolder<Item, Item> HALF_HEART_TEXTURE = ISORegistries.ITEMS.register("half_heart_texture", () -> new Item(new Item.Properties()));

	public static final Supplier<UnfairOneShotTrigger> UNFAIR_ONESHOT =
			ISORegistries.registerTrigger("unfair_oneshot", UnfairOneShotTrigger::new);

	@Config(description = "A list of effects to give when Unfair One Shot triggers, separated by semi-colons in the format effect,duration,amplifier")
	public static String effectsConfig = "minecraft:resistance,50,4;minecraft:resistance,100,3;minecraft:resistance,200,1";
	private static final List<MobEffectInstance> effects = new ArrayList<>();
	@Config(description = "If true, a sound is played on activation")
	public static Boolean playSound = true;
	@Config(description = "If true, an animation is played on activation")
	public static Boolean animation = true;
    @Config(min = 1, max = 20, description = "How much health you need to have for this to activate")
    public static Double activationHealth = 15d;
    @Config(min = 1, max = 20, description = "How much heal you're left with when this activates. Must be at least 1 lower than 'Activation health'")
    public static Double leftoverHealth = 2d;

	@Override
	public void readConfig(ModConfigEvent event) {
		super.readConfig(event);

		effects.clear();
		String[] effectsArray = effectsConfig.split(";");
		for (String effect : effectsArray) {
			if (!effect.isEmpty()) {
				String[] effectArray = effect.split(",");
				Optional<Holder.Reference<MobEffect>> mobEffect = BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.parse(effectArray[0]));
				if (mobEffect.isEmpty())
					continue;
				int duration = Integer.parseInt(effectArray[1]);
				int amplifier = Integer.parseInt(effectArray[2]);
				effects.add(new MobEffectInstance(mobEffect.get(), duration, amplifier));
			}
		}

        if (leftoverHealth >= activationHealth)
            leftoverHealth = activationHealth - 1;
	}

	@SubscribeEvent
	public void onPlayerAttackEvent(LivingDamageEvent.Pre event) {
		if (!this.isEnabled()
				|| !(event.getSource().getEntity() instanceof LivingEntity)
				|| !(event.getEntity() instanceof ServerPlayer player))
			return;

		if (player.getHealth() >= activationHealth && player.getHealth() - event.getNewDamage() <= 0) {
			event.setNewDamage(player.getHealth() - leftoverHealth.floatValue());
			UNFAIR_ONESHOT.get().trigger(player);
			for (MobEffectInstance effect : effects)
				player.addEffect(new MobEffectInstance(effect));

			if (playSound)
				player.level().playSound(null, player.blockPosition(), UNFAIR_ONE_SHOT.get(), SoundSource.PLAYERS, 2f, 0.8f);
			if (animation)
				UnfairOneShotMessage.send(player);
		}
	}
}