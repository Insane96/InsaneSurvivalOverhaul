package insane96mcp.iguanatweaksreborn.module.farming.livestock;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.reflect.TypeToken;
import insane96mcp.iguanatweaksreborn.modifier.Modifier;
import insane96mcp.iguanatweaksreborn.module.world.seasons.Seasons;
import insane96mcp.insanelib.data.IdTagMatcher;
import insane96mcp.insanelib.util.json.ILGsonHelper;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.fml.ModList;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@JsonAdapter(LivestockData.Serializer.class)
public class LivestockData {
	final IdTagMatcher entity;
	@Nullable
	final Float livingDays;
	final float livingDaysFluctuation;
	final List<Modifier> growthSpeedModifiers;
	final List<Modifier> breedingCooldownModifiers;
	final List<Modifier> eggLayCooldownModifiers;
	final List<Modifier> breedingFailChanceModifiers;
	final List<Modifier> cowFluidCooldownModifiers;
	final List<Modifier> sheepWoolGrowthChanceModifiers;
	final List<Modifier> twinsChanceModifiers;
	final List<Modifier> autoBreedChanceModifiers;

    public LivestockData(IdTagMatcher entity, @Nullable Float livingDays, float livingDaysFluctuation, List<Modifier> growthSpeedModifiers, List<Modifier> breedingCooldownModifiers, List<Modifier> eggLayCooldownModifiers, List<Modifier> breedingFailChanceModifiers, List<Modifier> cowFluidCooldownModifiers, List<Modifier> sheepWoolGrowthChanceModifiers, List<Modifier> twinsChanceModifiers, List<Modifier> autoBreedChanceModifiers) {
        this.entity = entity;
		this.livingDays = livingDays;
		this.livingDaysFluctuation = livingDaysFluctuation;
        this.growthSpeedModifiers = growthSpeedModifiers;
        this.breedingCooldownModifiers = breedingCooldownModifiers;
        this.eggLayCooldownModifiers = eggLayCooldownModifiers;
        this.breedingFailChanceModifiers = breedingFailChanceModifiers;
        this.cowFluidCooldownModifiers = cowFluidCooldownModifiers;
        this.sheepWoolGrowthChanceModifiers = sheepWoolGrowthChanceModifiers;
        this.twinsChanceModifiers = twinsChanceModifiers;
		this.autoBreedChanceModifiers = autoBreedChanceModifiers;
    }

	public boolean matches(Entity entity) {
		return this.entity.matchesEntity(entity);
	}

	public float getLivingDays(LivingEntity entity) {
		if (this.livingDays == null)
			return 0;
		float livingDays = this.livingDays + ((entity.getRandom().nextFloat() * this.livingDaysFluctuation * 2f) - this.livingDaysFluctuation);
		if (ModList.get().isLoaded("sereneseasons"))
			livingDays *= Seasons.getDayNightCycleModifier();
		return livingDays;
	}

	public static final java.lang.reflect.Type LIST_TYPE = new TypeToken<ArrayList<LivestockData>>(){}.getType();
    public static class Serializer implements JsonDeserializer<LivestockData> {
		@Override
		public LivestockData deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			JsonObject jObject = json.getAsJsonObject();
			IdTagMatcher entity = context.deserialize(jObject.get("entity"), IdTagMatcher.class);
			Float livingDays = ILGsonHelper.getAsNullableFloat(jObject, "living_days");
			float livingDaysFluctuation = GsonHelper.getAsFloat(jObject, "living_days_fluctuation", 0);
			List<Modifier> growthSpeed = Modifier.deserializeList(jObject, "growth_speed_modifiers", context);
			List<Modifier> breedingCooldown = Modifier.deserializeList(jObject, "breeding_cooldown_modifiers", context);
			List<Modifier> eggLayCooldown = Modifier.deserializeList(jObject, "egg_lay_cooldown_modifiers", context);
			List<Modifier> breedingFailChanceModifiers = Modifier.deserializeList(jObject, "breeding_fail_chance_modifiers", context);
			List<Modifier> cowFluidCooldownModifiers = Modifier.deserializeList(jObject, "cow_fluid_cooldown_modifiers", context);
			List<Modifier> sheepWoolGrowthChanceModifiers = Modifier.deserializeList(jObject, "sheep_wool_growth_chance_modifiers", context);
			List<Modifier> twinsChanceModifiers = Modifier.deserializeList(jObject, "twins_chance_modifiers", context);
			List<Modifier> autoBreedChanceModifiers = Modifier.deserializeList(jObject, "auto_breed_chance_modifiers", context);
			return new LivestockData(entity, livingDays, livingDaysFluctuation, growthSpeed, breedingCooldown, eggLayCooldown, breedingFailChanceModifiers, cowFluidCooldownModifiers, sheepWoolGrowthChanceModifiers, twinsChanceModifiers, autoBreedChanceModifiers);
		}
	}
}