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
import net.minecraft.util.Mth;
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
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@JsonAdapter(SerializableTrade.SerializableTradeSerializer.class)
public class SerializableTrade implements VillagerTrades.ItemListing {
	public Stack itemA;
	@Nullable
	public Stack itemB;
	public Stack result;

	private int maxUses = 1;
	private int xp = 0;

	@Nullable
	private EnchantRandomly enchantRandomly;
	private final List<EnchantmentInstance> enchantments = new ArrayList<>();

	@Nullable
	private ExplorationMap explorationMap;

	private boolean valid = false;

	public SerializableTrade() {

	}

	public static SerializableTrade itemToEmeralds(Item item, Count count, Count emeraldCount) {
		return new SerializableTrade().setItemA(item, count, null).setResult(Items.EMERALD, emeraldCount, null);
	}

	public static SerializableTrade itemToEmeralds(TagKey<Item> tag, Count count, Count emeraldCount) {
		return new SerializableTrade().setItemA(tag, count, null).setResult(Items.EMERALD, emeraldCount, null);
	}

	public static SerializableTrade emeraldToItems(Count emeraldCount, Item item, Count count) {
		return new SerializableTrade().setItemA(Items.EMERALD, emeraldCount, null).setResult(item, count, null);
	}

	public static SerializableTrade emeraldToItems(Count emeraldCount, TagKey<Item> item, Count count) {
		return new SerializableTrade().setItemA(Items.EMERALD, emeraldCount, null).setResult(item, count, null);
	}

	public SerializableTrade setItemA(Item item, Count count, CompoundTag nbt) {
		return this.setItemA(new Stack(item, null, count, nbt));
	}

	public SerializableTrade setItemA(TagKey<Item> tag, Count count, CompoundTag nbt) {
		return this.setItemA(new Stack(null, tag, count, nbt));
	}

	public SerializableTrade setItemA(Stack stack) {
		this.itemA = stack;
		return this;
	}

	public SerializableTrade setItemB(Item item, Count count, CompoundTag nbt) {
		return this.setItemB(new Stack(item, null, count, nbt));
	}

	public SerializableTrade setItemB(TagKey<Item> tag, Count count, CompoundTag nbt) {
		return this.setItemB(new Stack(null, tag, count, nbt));
	}

	public SerializableTrade setItemB(Stack stack) {
		this.itemB = stack;
		return this;
	}

	public SerializableTrade setResult(Item item, Count count, CompoundTag nbt) {
		return this.setResult(new Stack(item, null, count, nbt));
	}

	public SerializableTrade setResult(TagKey<Item> tag, Count count, CompoundTag nbt) {
		return this.setResult(new Stack(null, tag, count, nbt));
	}

	public SerializableTrade setResult(Stack stack) {
		this.result = stack;
		return this;
	}

	public SerializableTrade setMaxUses(int maxUses) {
		this.maxUses = maxUses;
		return this;
	}

	public SerializableTrade setXp(int xp) {
		this.xp = xp;
		return this;
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
	public MerchantOffer getOffer(@NotNull Entity entity, @NotNull RandomSource random) {
        if (this.valid
				|| entity.level().isClientSide)
			return null;
        ItemStack result = this.result.get(random);
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
                ItemStack mapResult = MapItem.create(serverlevel, blockpos.getX(), blockpos.getZ(), this.explorationMap.zoom, true, true);
                MapItem.renderBiomePreviewMap(serverlevel, mapResult);
                MapItemSavedData.addTargetDecoration(mapResult, blockpos, "+", this.explorationMap.mapDecoration);
				if (result.hasCustomHoverName())
					mapResult.setHoverName(result.getHoverName());
				result = mapResult;
            }
        }
		ItemStack stackA = this.itemA.get(random);
		ItemStack stackB = ItemStack.EMPTY;
		if (this.itemB != null)
			stackB = this.itemB.get(random);
		return new MerchantOffer(stackA, stackB, result, this.maxUses, this.xp, 1f);
	}

	public boolean isValid() {
		return this.valid;
	}

	public static final Type LIST_TYPE = new TypeToken<ArrayList<SerializableTrade>>(){}.getType();

	public static class SerializableTradeSerializer implements JsonDeserializer<SerializableTrade>, JsonSerializer<SerializableTrade> {
		@Override
		public SerializableTrade deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			JsonObject jObject = json.getAsJsonObject();
			SerializableTrade serializableTrade = new SerializableTrade();
			serializableTrade.itemA = GsonHelper.getAsObject(jObject, "item_a", context, Stack.class);
			if (serializableTrade.itemA.item != null && serializableTrade.itemA.item.equals(Items.AIR))
				serializableTrade.valid = false;
			serializableTrade.itemB = GsonHelper.getAsObject(jObject, "item_b", null, context, Stack.class);
			if (serializableTrade.itemB != null && serializableTrade.itemB.item != null && serializableTrade.itemB.item.equals(Items.AIR))
				serializableTrade.valid = false;
			serializableTrade.result = GsonHelper.getAsObject(jObject, "result", context, Stack.class);
			if (serializableTrade.result.item != null && serializableTrade.result.item.equals(Items.AIR))
				serializableTrade.valid = false;

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

			serializableTrade.maxUses = GsonHelper.getAsInt(jObject, "max_uses", 1);
			serializableTrade.xp = GsonHelper.getAsInt(jObject, "xp", 0);

			return serializableTrade;
		}

		@Override
		public JsonElement serialize(SerializableTrade src, Type typeOfSrc, JsonSerializationContext context) {
			JsonObject jObject = new JsonObject();
			jObject.add("item_a", context.serialize(src.itemA));
			if (src.itemB != null)
				jObject.add("item_b", context.serialize(src.itemB));
			jObject.add("result", context.serialize(src.result));
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
			if (src.explorationMap != null)
				jObject.add("exploration_map", context.serialize(src.explorationMap));
			if (src.maxUses != 1)
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

	@JsonAdapter(Stack.Serializer.class)
	public static class Stack {
		@Nullable
		Item item;
		@Nullable
		TagKey<Item> tag;
		private List<Item> tagCache = new ArrayList<>();
		Count count;
		@Nullable
		CompoundTag nbt;

		public Stack(@Nullable Item item, @Nullable TagKey<Item> tag) {
			this(item, tag, Count.ONE);
		}

		public Stack(@Nullable Item item, @Nullable TagKey<Item> tag, Count count) {
			this(item, tag, count, null);
		}

		public Stack(@Nullable Item item, @Nullable TagKey<Item> tag, Count count, @Nullable CompoundTag nbt) {
			this.item = item;
			this.tag = tag;
			this.count = count;
			this.nbt = nbt;
		}

		public ItemStack get(RandomSource random) {
			Item resultItem = this.getItem(random);
			if (resultItem == null || resultItem.equals(Items.AIR))
				return ItemStack.EMPTY;
			ItemStack itemStack = new ItemStack(resultItem, count.get(random));
			if (this.nbt != null)
				itemStack.setTag(this.nbt);
			return itemStack;
		}

		public Item getItem(RandomSource random) {
			if (item != null)
				return item;
			if (this.tagCache.isEmpty()) {
				ITag<Item> itemTag = ForgeRegistries.ITEMS.tags().getTag(this.tag);
				this.tagCache = itemTag.stream().toList();
			}
			if (this.tagCache.isEmpty()) {
				ISOLogHelper.warn("Tag {} does not exist or is empty", this.tag.location());
				return Items.AIR;
			}
			return this.tagCache.get(random.nextInt(this.tagCache.size()));
		}

		public static class Serializer implements JsonDeserializer<Stack>, JsonSerializer<Stack> {
			@Override
			public Stack deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
				TagKey<Item> tag = null;
				Item item = null;
				if (json.isJsonPrimitive()) {
					String itemString = json.getAsString();
					if (itemString.startsWith("#")) {
						tag = TagKey.create(Registries.ITEM, ResourceLocation.parse(itemString.substring(1)));
					}
					else {
						item = ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(itemString));
						if (item == Items.AIR) {
							ISOLogHelper.warn("Item %s does not exist".formatted(itemString));
							return null;
						}
					}
					return new Stack(item, tag);
				}
				JsonObject jObject = json.getAsJsonObject();
				String itemString = GsonHelper.getAsString(jObject, "item");
				if (itemString.startsWith("#")) {
					tag = TagKey.create(Registries.ITEM, ResourceLocation.parse(itemString.substring(1)));
				}
				else {
					item = ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(itemString));
					if (item == Items.AIR) {
						ISOLogHelper.warn("Item %s does not exist".formatted(itemString));
						return null;
					}
				}
				Count count = GsonHelper.getAsObject(jObject, "count", Count.ONE, context, Count.class);
				String tagString = GsonHelper.getAsString(jObject, "nbt", "");
				Stack stack = new Stack(item, tag, count);
				if (!tagString.isEmpty()) {
					try {
                        stack.nbt = TagParser.parseTag(tagString);
					} catch (Exception e) {
						throw new JsonParseException("Failed to parse tag %s".formatted(e.getMessage()));
					}
				}
				return stack;
			}

			@Override
			public JsonElement serialize(Stack src, Type typeOfSrc, JsonSerializationContext context) {
				if (src.count.equals(Count.ONE) && src.nbt == null) {
					if (src.item != null)
						return new JsonPrimitive(ForgeRegistries.ITEMS.getKey(src.item).toString());
					else
						return new JsonPrimitive("#" + src.tag.location());
				}

				JsonObject jsonObject = new JsonObject();
				if (src.item != null)
					jsonObject.add("item", new JsonPrimitive(ForgeRegistries.ITEMS.getKey(src.item).toString()));
				else
					jsonObject.add("item", new JsonPrimitive("#" + src.tag.location()));
				if (!src.count.equals(Count.ONE))
					jsonObject.add("count", context.serialize(src.count));
				if (src.nbt != null)
					jsonObject.add("nbt", new JsonPrimitive(src.nbt.getAsString()));
				return jsonObject;
			}
		}
	}

	@JsonAdapter(Count.Serializer.class)
	public static class Count {
		public static Count ONE = new Count(1);

		public int min;
		public int max;

		public Count(int count) {
			this(count, count);
		}

		public Count(int min, int max) {
			this.min = min;
			this.max = max;
		}

		public static Count of(int min, int max) {
			return new Count(min, max);
		}

		public static Count of(int count) {
			return new Count(count);
		}

		public int get(RandomSource random) {
			if (min == max)
				return min;
			return Mth.nextInt(random, min, max);
		}

		public static class Serializer implements JsonDeserializer<Count>, JsonSerializer<Count> {
			@Override
			public Count deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
				if (json.isJsonPrimitive())
					return new Count(json.getAsInt(), json.getAsInt());
				JsonObject jObject = json.getAsJsonObject();
				return new Count(
						GsonHelper.getAsInt(jObject, "min"),
						GsonHelper.getAsInt(jObject, "max")
				);
			}

			@Override
			public JsonElement serialize(Count src, Type typeOfSrc, JsonSerializationContext context) {
				if (src.min == src.max)
					return new JsonPrimitive(src.min);
				JsonObject jsonObject = new JsonObject();
				jsonObject.addProperty("min", src.min);
				jsonObject.addProperty("max", src.max);
				return jsonObject;
			}
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			return obj instanceof Count count && this.min == count.min && this.max == count.max;
		}
	}
}
