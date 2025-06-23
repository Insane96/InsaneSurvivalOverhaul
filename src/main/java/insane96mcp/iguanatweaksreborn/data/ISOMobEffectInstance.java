package insane96mcp.iguanatweaksreborn.data;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.reflect.TypeToken;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.function.Supplier;

/**
 * Serializable version of MobEffectInstance with Supplier for effect so can be used at runtime
 */
@JsonAdapter(ISOMobEffectInstance.Serializer.class)
public class ISOMobEffectInstance {
	public final Supplier<MobEffect> effect;
	public int duration;
	public int amplifier = 0;
	public boolean ambient = false;
	public boolean visible = true;
	public boolean showIcon = true;

	public ISOMobEffectInstance(MobEffect effect, int duration) {
		this.effect = () -> effect;
		this.duration = duration;
	}

	public ISOMobEffectInstance(Supplier<MobEffect> effect, int duration) {
		this.effect = effect;
		this.duration = duration;
	}

	public static class Builder {
		public ISOMobEffectInstance mobEffectInstance;

		public Builder(MobEffect effect, int duration) {
			this.mobEffectInstance = new ISOMobEffectInstance(effect, duration);
		}

		public Builder(Supplier<MobEffect> effectSupplier, int duration) {
			this.mobEffectInstance = new ISOMobEffectInstance(effectSupplier, duration);
		}

		public Builder setAmplifier(int amplifier) {
			this.mobEffectInstance.amplifier = amplifier;
			return this;
		}

		public Builder ambientParticles() {
			this.mobEffectInstance.ambient = true;
			return this;
		}

		public Builder noParticles() {
			this.mobEffectInstance.visible = false;
			return this;
		}

		public Builder hideIcon() {
			this.mobEffectInstance.showIcon = false;
			return this;
		}

		public ISOMobEffectInstance build() {
			return this.mobEffectInstance;
		}
	}

	public MobEffectInstance getMobEffectInstance() {
		return new MobEffectInstance(this.effect.get(), this.duration, this.amplifier, this.ambient, this.visible, this.showIcon);
	}

	public static final java.lang.reflect.Type LIST_TYPE = new TypeToken<ArrayList<ISOMobEffectInstance>>(){}.getType();
	public static class Serializer implements JsonDeserializer<ISOMobEffectInstance>, JsonSerializer<ISOMobEffectInstance> {
		@Override
		public ISOMobEffectInstance deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			JsonObject jObject = json.getAsJsonObject();
			String id = GsonHelper.getAsString(jObject, "id", "");

			if (!id.isEmpty() && !ResourceLocation.isValidResourceLocation(id)) {
				throw new JsonParseException("Invalid id: %s".formatted(id));
			}
			MobEffect mobEffect = ForgeRegistries.MOB_EFFECTS.getValue(ResourceLocation.parse(id));
			if (mobEffect == null) {
				throw new JsonParseException("%d is not a known mob_effect".formatted(id));
			}
			int duration = GsonHelper.getAsInt(jObject, "duration");
			Builder builder = new Builder(() -> mobEffect, duration);
			if (jObject.has("amplifier"))
				builder.setAmplifier(GsonHelper.getAsInt(jObject, "amplifier"));
			if (jObject.has("amplifier"))
				builder.setAmplifier(GsonHelper.getAsInt(jObject, "amplifier"));
			if (GsonHelper.getAsBoolean(jObject, "ambient", false))
				builder.ambientParticles();
			if (GsonHelper.getAsBoolean(jObject, "hide_particles", false))
				builder.noParticles();
			if (GsonHelper.getAsBoolean(jObject, "hide_icon", false))
				builder.hideIcon();

			return builder.build();
		}

		@Override
		public JsonElement serialize(ISOMobEffectInstance src, java.lang.reflect.Type typeOfSrc, JsonSerializationContext context) {
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("id", ForgeRegistries.MOB_EFFECTS.getKey(src.effect.get()).toString());
			jsonObject.addProperty("duration", src.duration);
			if (src.amplifier > 0)
				jsonObject.addProperty("amplifier", src.amplifier);
			if (src.ambient)
				jsonObject.addProperty("ambient", true);
			if (!src.visible)
				jsonObject.addProperty("hide_particles", true);
			if (!src.showIcon)
				jsonObject.addProperty("hide_icon", true);


			return jsonObject;
		}
	}
}
