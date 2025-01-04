package insane96mcp.iguanatweaksreborn.module.world.wanderingtrader;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.reflect.TypeToken;
import insane96mcp.iguanatweaksreborn.utils.ISOLogHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.loot.functions.ExplorationMapFunction;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@JsonAdapter(SerializableTrade.SerializableTradeSerializer.class)
public class SerializableTrade implements VillagerTrades.ItemListing {
	public ItemStack itemA;
	@Nullable
	public ItemStack itemB;
	public ItemStack result;
	private int maxUses;
	private int xp = 0;

	@Nullable
	private EnchantRandomly enchantRandomly;
	private final List<EnchantmentInstance> enchantments = new ArrayList<>();

	@Nullable
	private ExplorationMap explorationMap;

	public SerializableTrade() {

	}

	public SerializableTrade(ItemStack itemA, ItemStack result, int maxUses) {
		this(itemA, null, result, maxUses, 0);
	}

	public SerializableTrade(ItemStack itemA, @Nullable ItemStack itemB, ItemStack result, int maxUses, int xp) {
		this.itemA = itemA;
		this.itemB = itemB;
		this.result = result;
		this.maxUses = maxUses;
		this.xp = xp;
	}

	public SerializableTrade enchant(Enchantment enchantment, int level) {
		this.enchantments.add(new EnchantmentInstance(enchantment, level));
		return this;
	}

	public SerializableTrade enchantResult(int minLevel, int maxLevel, boolean treasure) {
		this.enchantRandomly = new EnchantRandomly(minLevel, maxLevel, treasure);
		return this;
	}

	public SerializableTrade explorationMap(TagKey<Structure> destination, MapDecoration.Type mapDecoration, byte zoom, int searchRadius, boolean skipKnownStructures) {
		this.explorationMap = new ExplorationMap(destination, mapDecoration, zoom, searchRadius, skipKnownStructures);
		return this;
	}

	@Nullable
	@Override
	public MerchantOffer getOffer(Entity entity, RandomSource random) {
		ItemStack result = this.result.copy();
		if (entity.level().isClientSide)
			return null;
		if (this.enchantRandomly != null)
			result = EnchantmentHelper.enchantItem(random, result, random.nextInt(this.enchantRandomly.minLevel, this.enchantRandomly.maxLevel + 1), this.enchantRandomly.treasure);
		for (EnchantmentInstance enchantmentInstance : this.enchantments) {
			if (result.is(Items.ENCHANTED_BOOK))
				EnchantedBookItem.addEnchantment(result, new EnchantmentInstance(enchantmentInstance.enchantment, enchantmentInstance.level));
			else if (result.is(Items.BOOK)) {
				CompoundTag tag = result.getTag();
				result = new ItemStack(Items.ENCHANTED_BOOK, result.getCount());
				result.setTag(tag);
				EnchantedBookItem.addEnchantment(result, new EnchantmentInstance(enchantmentInstance.enchantment, enchantmentInstance.level));
			}
			else
				result.enchant(enchantmentInstance.enchantment, enchantmentInstance.level);
		}
		if (this.explorationMap != null && result.is(Items.MAP)) {
			Vec3 vec3 = entity.position();
            ServerLevel serverlevel = (ServerLevel) entity.level();
            BlockPos blockpos = serverlevel.findNearestMapStructure(this.explorationMap.destination, BlockPos.containing(vec3), this.explorationMap.searchRadius, this.explorationMap.skipKnownStructures);
            if (blockpos != null) {
                result = MapItem.create(serverlevel, blockpos.getX(), blockpos.getZ(), this.explorationMap.zoom, true, true);
                MapItem.renderBiomePreviewMap(serverlevel, result);
                MapItemSavedData.addTargetDecoration(result, blockpos, "+", this.explorationMap.mapDecoration);
				result.setHoverName(this.result.getHoverName());
            }
        }
		return new MerchantOffer(this.itemA, this.itemB == null ? ItemStack.EMPTY : this.itemB, result, this.maxUses, this.xp, 1f);
	}

	public static final Type SERIALIZABLE_TRADE_LIST_TYPE = new TypeToken<ArrayList<SerializableTrade>>(){}.getType();

	public static class SerializableTradeSerializer implements JsonDeserializer<SerializableTrade>, JsonSerializer<SerializableTrade> {
		@Override
		public SerializableTrade deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			JsonObject jObject = json.getAsJsonObject();
			SerializableTrade serializableTrade = new SerializableTrade();
			String sItemA = GsonHelper.getAsString(jObject, "item_a");
			int itemACount = GsonHelper.getAsInt(jObject, "item_a_count", 1);
			ResourceLocation itemA = new ResourceLocation(sItemA);
			serializableTrade.itemA = new ItemStack(ForgeRegistries.ITEMS.getValue(itemA), itemACount);
			if (jObject.has("item_a_tag")) {
				String itemATag = GsonHelper.getAsString(jObject, "item_a_tag");
				try {
					CompoundTag resultTag = TagParser.parseTag(itemATag);
					serializableTrade.itemA.setTag(resultTag);
				} catch (Exception e) {
					throw new JsonParseException("Failed to parse item_a_tag %s".formatted(e.getMessage()));
				}
			}

			String sItemB = GsonHelper.getAsString(jObject, "item_b", "");
			if (!sItemB.isEmpty()) {
				int itemBCount = GsonHelper.getAsInt(jObject, "item_b_count", 1);
				ResourceLocation itemB = new ResourceLocation(sItemB);
				serializableTrade.itemB = new ItemStack(ForgeRegistries.ITEMS.getValue(itemB), itemBCount);
				if (jObject.has("item_b_tag")) {
					String itemBTag = GsonHelper.getAsString(jObject, "item_b_tag");
					try {
						CompoundTag resultTag = TagParser.parseTag(itemBTag);
						serializableTrade.itemB.setTag(resultTag);
					} catch (Exception e) {
						throw new JsonParseException("Failed to parse item_b_tag %s".formatted(e.getMessage()));
					}
				}
			}
			String sItemResult = GsonHelper.getAsString(jObject, "item_result");
			ResourceLocation itemResult = new ResourceLocation(sItemResult);
			int itemResultCount = GsonHelper.getAsInt(jObject, "item_result_count", 1);
			serializableTrade.result = new ItemStack(ForgeRegistries.ITEMS.getValue(itemResult), itemResultCount);
			if (jObject.has("item_result_tag")) {
				String itemResultTag = GsonHelper.getAsString(jObject, "item_result_tag");
				try {
					CompoundTag resultTag = TagParser.parseTag(itemResultTag);
					serializableTrade.result.setTag(resultTag);
				} catch (Exception e) {
					throw new JsonParseException("Failed to parse item_result_tag %s".formatted(e.getMessage()));
				}
			}
			JsonObject enchantRandomly = GsonHelper.getAsJsonObject(jObject, "enchant_randomly", null);
			if (enchantRandomly != null) {
				serializableTrade.enchantRandomly = new EnchantRandomly(GsonHelper.getAsInt(enchantRandomly, "min_levels"), GsonHelper.getAsInt(enchantRandomly, "max_levels"), GsonHelper.getAsBoolean(enchantRandomly, "treasure"));
			}
			JsonArray enchantments = GsonHelper.getAsJsonArray(jObject, "enchantments", null);
			if (enchantments != null) {
				enchantments.asList().forEach(jsonElement -> {
					String id = GsonHelper.getAsString(jsonElement.getAsJsonObject(), "id");
					int level = GsonHelper.getAsInt(jsonElement.getAsJsonObject(), "level", 1);
					serializableTrade.enchantments.add(new EnchantmentInstance(ForgeRegistries.ENCHANTMENTS.getValue(ResourceLocation.tryParse(id)), level));
				});
			}
			if (jObject.has("exploration_map"))
					serializableTrade.explorationMap = context.deserialize(jObject.get("exploration_map"), ExplorationMap.class);

			serializableTrade.maxUses = GsonHelper.getAsInt(jObject, "max_uses");
			serializableTrade.xp = GsonHelper.getAsInt(jObject, "xp", 0);

			return serializableTrade;
		}

		@Override
		public JsonElement serialize(SerializableTrade src, Type typeOfSrc, JsonSerializationContext context) {
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("item_a", ForgeRegistries.ITEMS.getKey(src.itemA.getItem()).toString());
			jsonObject.addProperty("item_a_count", src.itemA.getCount());
			if (src.itemA.getTag() != null)
				jsonObject.addProperty("item_a_tag", src.itemA.getTag().toString());
			if (src.itemB != null) {
				jsonObject.addProperty("item_b", ForgeRegistries.ITEMS.getKey(src.itemB.getItem()).toString());
				jsonObject.addProperty("item_b_count", src.itemB.getCount());
				if (src.itemB.getTag() != null)
					jsonObject.addProperty("item_b_tag", src.itemB.getTag().toString());
			}
			jsonObject.addProperty("item_result", ForgeRegistries.ITEMS.getKey(src.result.getItem()).toString());
			jsonObject.addProperty("item_result_count", src.result.getCount());
			if (src.result.getTag() != null)
				jsonObject.addProperty("item_result_tag", src.result.getTag().toString());
			if (src.enchantRandomly != null) {
				JsonObject enchantRandomly = new JsonObject();
				enchantRandomly.addProperty("min_levels", src.enchantRandomly.minLevel);
				enchantRandomly.addProperty("max_levels", src.enchantRandomly.maxLevel);
				enchantRandomly.addProperty("treasure", src.enchantRandomly.treasure);
				jsonObject.add("enchant_randomly", enchantRandomly);
			}
			if (!src.enchantments.isEmpty()) {
				JsonArray jsonArray = new JsonArray();
				src.enchantments.forEach(enchantmentInstance -> {
					JsonObject enchantmentsObject = new JsonObject();
					enchantmentsObject.addProperty("id", ForgeRegistries.ENCHANTMENTS.getKey(enchantmentInstance.enchantment).toString());
					if (enchantmentInstance.level > 1)
						enchantmentsObject.addProperty("level", enchantmentInstance.level);
					jsonArray.add(enchantmentsObject);
				});
				jsonObject.add("enchantments", jsonArray);
			}
			if (src.explorationMap != null) {
				jsonObject.add("exploration_map", context.serialize(src.explorationMap));
			}
			jsonObject.addProperty("max_uses", src.maxUses);
			jsonObject.addProperty("xp", src.xp);
			return jsonObject;
		}
	}

	private record EnchantRandomly(int minLevel, int maxLevel, boolean treasure) {
	}

	@JsonAdapter(ExplorationMap.ExplorationMapSerializer.class)
		private record ExplorationMap(TagKey<Structure> destination, MapDecoration.Type mapDecoration, byte zoom,
									  int searchRadius, boolean skipKnownStructures) {

		public static class ExplorationMapSerializer implements JsonDeserializer<ExplorationMap>, JsonSerializer<ExplorationMap> {
				@Override
				public ExplorationMap deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
					TagKey<Structure> tagkey = readStructure(json.getAsJsonObject());
					String s = json.getAsJsonObject().has("decoration") ? GsonHelper.getAsString(json.getAsJsonObject(), "decoration") : "mansion";
					MapDecoration.Type mapdecoration$type = ExplorationMapFunction.DEFAULT_DECORATION;

					try {
						mapdecoration$type = MapDecoration.Type.valueOf(s.toUpperCase(Locale.ROOT));
					} catch (IllegalArgumentException illegalargumentexception) {
						ISOLogHelper.error("Error while parsing loot table decoration entry. Found {}. Defaulting to {}", s, ExplorationMapFunction.DEFAULT_DECORATION);
					}

					byte b0 = GsonHelper.getAsByte(json.getAsJsonObject(), "zoom", ExplorationMapFunction.DEFAULT_ZOOM);
					int i = GsonHelper.getAsInt(json.getAsJsonObject(), "search_radius", ExplorationMapFunction.DEFAULT_SEARCH_RADIUS);
					boolean flag = GsonHelper.getAsBoolean(json.getAsJsonObject(), "skip_existing_chunks", ExplorationMapFunction.DEFAULT_SKIP_EXISTING);
					return new ExplorationMap(tagkey, mapdecoration$type, b0, i, flag);
				}

				@Override
				public JsonElement serialize(ExplorationMap src, Type typeOfSrc, JsonSerializationContext context) {
					JsonObject jsonObject = new JsonObject();
					jsonObject.addProperty("destination", src.destination.location().toString());
					if (src.mapDecoration != ExplorationMapFunction.DEFAULT_DECORATION)
						jsonObject.add("decoration", context.serialize(src.mapDecoration.toString().toLowerCase(Locale.ROOT)));
					if (src.zoom != 2)
						jsonObject.addProperty("zoom", src.zoom);
					if (src.searchRadius != 50)
						jsonObject.addProperty("search_radius", src.searchRadius);
					if (!src.skipKnownStructures)
						jsonObject.addProperty("skip_existing_chunks", src.skipKnownStructures);
					return jsonObject;
				}
			}

			private static TagKey<Structure> readStructure(JsonObject pJson) {
				String s = GsonHelper.getAsString(pJson, "destination");
				return TagKey.create(Registries.STRUCTURE, new ResourceLocation(s));
			}
		}
}
