package insane96mcp.iguanatweaksreborn.module.mobs.villager;

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
import net.minecraft.world.item.*;
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
import net.minecraftforge.registries.tags.ITag;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@JsonAdapter(SerializableTrade.SerializableTradeSerializer.class)
public class SerializableTrade implements VillagerTrades.ItemListing {
	public ItemStack itemA;
	@Nullable
	public TagKey<Item> tagA;
	private int tagACount = 1;
	private List<Item> tagACache = new ArrayList<>();
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

	public SerializableTrade(TagKey<Item> tagA, int tagACount, ItemStack result, int maxUses) {
		this(tagA, tagACount, ItemStack.EMPTY, result, maxUses, 0);
	}

	public SerializableTrade(ItemStack itemA, ItemStack result, int maxUses) {
		this(itemA, ItemStack.EMPTY, result, maxUses, 0);
	}

	public SerializableTrade(ItemStack itemA, ItemStack itemB, ItemStack result, int maxUses, int xp) {
		this.itemA = itemA;
		this.itemB = itemB;
		this.result = result;
		this.maxUses = maxUses;
		this.xp = xp;
	}

	public SerializableTrade(@Nullable TagKey<Item> tagA, int tagACount, ItemStack itemB, ItemStack result, int maxUses, int xp) {
		this.itemA = ItemStack.EMPTY;
		this.tagA = tagA;
		this.tagACount = tagACount;
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
		if (this.result.isEmpty()
				|| (this.itemA.isEmpty() && this.tagA == null))
			return null;
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
		ItemStack stackA = this.itemA;
		if (stackA.isEmpty()) {
			if (this.tagACache.isEmpty()) {
				ITag<Item> itemTag = ForgeRegistries.ITEMS.tags().getTag(this.tagA);
				this.tagACache = itemTag.stream().toList();
			}
			stackA = new ItemStack(this.tagACache.get(random.nextInt(this.tagACache.size())), this.tagACount);
		}
		return new MerchantOffer(stackA, this.itemB, result, this.maxUses, this.xp, 1f);
	}

	public static final Type LIST_TYPE = new TypeToken<ArrayList<SerializableTrade>>(){}.getType();

	public static class SerializableTradeSerializer implements JsonDeserializer<SerializableTrade>, JsonSerializer<SerializableTrade> {
		@Override
		public SerializableTrade deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			JsonObject jObject = json.getAsJsonObject();
			SerializableTrade serializableTrade = new SerializableTrade();
			serializableTrade.itemA = stackFromJson("item_a", jObject, context);
			serializableTrade.itemB = stackFromJson("item_b", jObject, context);
			serializableTrade.result = stackFromJson("item_result", jObject, context);

			if (jObject.has("tag_a")) {
				serializableTrade.tagA = TagKey.create(Registries.ITEM, ResourceLocation.parse(GsonHelper.getAsString(jObject, "tag_a")));
				serializableTrade.tagACount = GsonHelper.getAsInt(jObject, "item_a_count", 1);
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
			JsonObject jObject = new JsonObject();
			stackToJson(jObject, "item_a", src.itemA, context);
			stackToJson(jObject, "item_b", src.itemB, context);
			stackToJson(jObject, "item_result", src.result, context);
			if (src.enchantRandomly != null) {
				JsonObject enchantRandomly = new JsonObject();
				enchantRandomly.addProperty("min_levels", src.enchantRandomly.minLevel);
				enchantRandomly.addProperty("max_levels", src.enchantRandomly.maxLevel);
				enchantRandomly.addProperty("treasure", src.enchantRandomly.treasure);
				jObject.add("enchant_randomly", enchantRandomly);
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
				jObject.add("enchantments", jsonArray);
			}
			if (src.explorationMap != null) {
				jObject.add("exploration_map", context.serialize(src.explorationMap));
			}
			jObject.addProperty("max_uses", src.maxUses);
			jObject.addProperty("xp", src.xp);
			return jObject;
		}
	}

	private static ItemStack stackFromJson(String name, JsonObject jObject, JsonDeserializationContext context) throws JsonParseException {
		if (!jObject.has(name))
			return ItemStack.EMPTY;
		String itemString = GsonHelper.getAsString(jObject, name);
		int count = GsonHelper.getAsInt(jObject, name + "_count", 1);
		Item item = ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(itemString));
		if (item == Items.AIR) {
			ISOLogHelper.warn("Item %s for SerializableTrade does not exist, ignoring".formatted(itemString));
			return ItemStack.EMPTY;
		}
		else {
			ItemStack stack = new ItemStack(item, count);
			if (!jObject.has(name + "_tag"))
				return stack;

			String tagString = GsonHelper.getAsString(jObject, name + "_tag");
			try {
				CompoundTag compoundTag = TagParser.parseTag(tagString);
				stack.setTag(compoundTag);
			} catch (Exception e) {
				throw new JsonParseException("Failed to parse %s_tag %s".formatted(name, e.getMessage()));
			}
			return stack;
		}
	}

	private static void stackToJson(JsonObject jObject, String name, ItemStack stack, JsonSerializationContext context) throws JsonParseException {
		if (stack.isEmpty())
			return;
		jObject.addProperty(name, ForgeRegistries.ITEMS.getKey(stack.getItem()).toString());
		if (stack.getCount() > 1)
			jObject.addProperty(name + "_count", stack.getCount());
		if (stack.getTag() != null)
			jObject.addProperty(name + "_tag", stack.getTag().toString());
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
				return TagKey.create(Registries.STRUCTURE, ResourceLocation.parse(s));
			}
		}
}
