package insane96mcp.insanesurvivaloverhaul.module.misc.beacon;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.reflect.TypeToken;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.effect.MobEffect;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.ArrayList;

@JsonAdapter(BeaconEffect.Serializer.class)
public class BeaconEffect {
    public static final Type LIST_TYPE = new TypeToken<ArrayList<BeaconEffect>>() {}.getType();

    private final ResourceLocation location;
    private final int maxAmplifier;
    private final int heightRequired;

    public BeaconEffect(String location, int maxAmplifier, int heightRequired) {
        this(ResourceLocation.parse(location), maxAmplifier, heightRequired);
    }

    public BeaconEffect(Holder<MobEffect> effect, int maxAmplifier, int heightRequired) {
        this(effect.unwrapKey().orElseThrow().location(), maxAmplifier, heightRequired);
    }

    private BeaconEffect(ResourceLocation location, int maxAmplifier, int heightRequired) {
        this.location = location;
        this.maxAmplifier = maxAmplifier;
        this.heightRequired = heightRequired;
    }

    public ResourceLocation getLocation() {
        return this.location;
    }

    @Nullable
    public Holder<MobEffect> getEffect() {
        return BuiltInRegistries.MOB_EFFECT.getHolder(this.location).orElse(null);
    }

    public int getMaxAmplifier() {
        return this.maxAmplifier;
    }

    public int getHeightRequired() {
        return this.heightRequired;
    }

    public boolean isAvailableAt(int layers) {
        return layers >= this.heightRequired;
    }

    /**
     * Ogni livello dell'effetto richiede un layer di piramide in più.
     */
    public int clampAmplifier(int amplifier, int layers) {
        return Math.max(0, Math.min(amplifier, Math.min(this.maxAmplifier, layers - 1)));
    }

    static class Serializer implements JsonSerializer<BeaconEffect>, JsonDeserializer<BeaconEffect> {
        @Override
        public BeaconEffect deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jObject = json.getAsJsonObject();
            int maxAmplifier = GsonHelper.getAsInt(jObject, "max_amplifier", 0);
            if (maxAmplifier < 0 || maxAmplifier >= ISOBeaconBlockEntity.MAX_LAYERS)
                throw new JsonParseException("max_amplifier must be between 0 and %d".formatted(ISOBeaconBlockEntity.MAX_LAYERS - 1));
            int heightRequired = GsonHelper.getAsInt(jObject, "height_required", 1);
            if (heightRequired < 1 || heightRequired > ISOBeaconBlockEntity.MAX_LAYERS)
                throw new JsonParseException("height_required must be between 1 and %d".formatted(ISOBeaconBlockEntity.MAX_LAYERS));
            return new BeaconEffect(GsonHelper.getAsString(jObject, "id"), maxAmplifier, heightRequired);
        }

        @Override
        public JsonElement serialize(BeaconEffect src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jObject = new JsonObject();
            jObject.addProperty("id", src.location.toString());
            jObject.addProperty("max_amplifier", src.maxAmplifier);
            jObject.addProperty("height_required", src.heightRequired);
            return jObject;
        }
    }
}
