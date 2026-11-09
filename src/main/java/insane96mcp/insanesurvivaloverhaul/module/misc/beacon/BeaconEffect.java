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
    /**
     * Pyramid layers required by each level of the effect, indexed by amplifier.
     */
    private final int[] layersRequired;

    public BeaconEffect(String location, int... layersRequired) {
        this(ResourceLocation.parse(location), layersRequired);
    }

    public BeaconEffect(Holder<MobEffect> effect, int... layersRequired) {
        this(effect.unwrapKey().orElseThrow().location(), layersRequired);
    }

    private BeaconEffect(ResourceLocation location, int[] layersRequired) {
        this.location = location;
        this.layersRequired = layersRequired;
    }

    public ResourceLocation getLocation() {
        return this.location;
    }

    @Nullable
    public Holder<MobEffect> getEffect() {
        return BuiltInRegistries.MOB_EFFECT.getHolder(this.location).orElse(null);
    }

    public int getMaxAmplifier() {
        return this.layersRequired.length - 1;
    }

    public boolean isAvailableAt(int layers) {
        return layers >= this.layersRequired[0];
    }

    public boolean isAmplifierAvailableAt(int amplifier, int layers) {
        return amplifier >= 0 && amplifier < this.layersRequired.length && layers >= this.layersRequired[amplifier];
    }

    /**
     * Returns the highest amplifier, not above the requested one, allowed by the current pyramid.
     */
    public int clampAmplifier(int amplifier, int layers) {
        for (int i = Math.min(amplifier, this.getMaxAmplifier()); i > 0; i--) {
            if (this.isAmplifierAvailableAt(i, layers))
                return i;
        }
        return 0;
    }

    static class Serializer implements JsonSerializer<BeaconEffect>, JsonDeserializer<BeaconEffect> {
        @Override
        public BeaconEffect deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jObject = json.getAsJsonObject();
            JsonArray jArray = GsonHelper.getAsJsonArray(jObject, "layers_required");
            if (jArray.isEmpty() || jArray.size() > ISOBeaconBlockEntity.MAX_LAYERS)
                throw new JsonParseException("layers_required must have between 1 and %d elements".formatted(ISOBeaconBlockEntity.MAX_LAYERS));

            int[] layersRequired = new int[jArray.size()];
            for (int i = 0; i < layersRequired.length; i++) {
                layersRequired[i] = jArray.get(i).getAsInt();
                if (layersRequired[i] < 1 || layersRequired[i] > ISOBeaconBlockEntity.MAX_LAYERS)
                    throw new JsonParseException("layers_required values must be between 1 and %d".formatted(ISOBeaconBlockEntity.MAX_LAYERS));
                if (i > 0 && layersRequired[i] < layersRequired[i - 1])
                    throw new JsonParseException("layers_required values cannot decrease");
            }
            return new BeaconEffect(GsonHelper.getAsString(jObject, "id"), layersRequired);
        }

        @Override
        public JsonElement serialize(BeaconEffect src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jObject = new JsonObject();
            jObject.addProperty("id", src.location.toString());
            JsonArray jArray = new JsonArray();
            for (int layers : src.layersRequired) {
                jArray.add(layers);
            }
            jObject.add("layers_required", jArray);
            return jObject;
        }
    }
}
