package insane96mcp.iguanatweaksreborn.module.mining.blockdata;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.reflect.TypeToken;
import insane96mcp.insanelib.base.config.MinMax;
import insane96mcp.insanelib.util.json.ILGsonHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITag;
import org.apache.commons.lang3.NotImplementedException;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@JsonAdapter(BlockData.Serializer.class)
public class BlockData {

	public Block block;
	public TagKey<Block> blockTag;
	private List<BlockState> blockStates = new ArrayList<>();
	@Nullable
	private Float stateHardness;
	@Nullable
	private Boolean stateRequiresCorrectToolForDrops;
	@Nullable
	private NoteBlockInstrument stateNoteBlockInstrument;
	@Nullable
	private MinMax stateExperienceDropped;

	@Nullable
	public Float explosionResistance;
	@Nullable
	public Float friction;
	@Nullable
	public Float speedFactor;
	@Nullable
	public Float jumpFactor;

	@Nullable
	public Float boneMealFailChance;

	public BlockData(Block block, List<BlockState> blockStates, @Nullable Float stateHardness, @Nullable Boolean stateRequiresCorrectToolForDrops, @Nullable NoteBlockInstrument stateNoteBlockInstrument, @Nullable MinMax stateExperienceDropped, @Nullable Float explosionResistance, @Nullable Float friction, @Nullable Float speedFactor, @Nullable Float jumpFactor, @Nullable Float boneMealFailChance) {
		this.block = block;
		this.blockStates = blockStates;
		this.stateHardness = stateHardness;
		this.stateRequiresCorrectToolForDrops = stateRequiresCorrectToolForDrops;
		this.stateNoteBlockInstrument = stateNoteBlockInstrument;
		this.stateExperienceDropped = stateExperienceDropped;
		this.explosionResistance = explosionResistance;
		this.friction = friction;
		this.speedFactor = speedFactor;
		this.jumpFactor = jumpFactor;
		this.boneMealFailChance = boneMealFailChance;
	}

	public BlockData(TagKey<Block> blockTag, @Nullable Float stateHardness, @Nullable Boolean stateRequiresCorrectToolForDrops, @Nullable NoteBlockInstrument stateNoteBlockInstrument, @Nullable MinMax stateExperienceDropped, @Nullable Float explosionResistance, @Nullable Float friction, @Nullable Float speedFactor, @Nullable Float jumpFactor, @Nullable Float boneMealFailChance) {
		this.blockTag = blockTag;
		this.stateHardness = stateHardness;
		this.stateRequiresCorrectToolForDrops = stateRequiresCorrectToolForDrops;
		this.stateNoteBlockInstrument = stateNoteBlockInstrument;
		this.stateExperienceDropped = stateExperienceDropped;
		this.explosionResistance = explosionResistance;
		this.friction = friction;
		this.speedFactor = speedFactor;
		this.jumpFactor = jumpFactor;
		this.boneMealFailChance = boneMealFailChance;
	}

	public BlockData(Block block) {
		this.block = block;
	}

	public BlockData(TagKey<Block> blockTag) {
		this.blockTag = blockTag;
	}

	public boolean matches(BlockState state) {
		if (this.block != null) {
			if (!state.is(this.block))
				return false;
			if (this.blockStates.isEmpty())
				return true;
			for (BlockState blockState : this.blockStates) {
				if (blockState == state)
					return true;
			}
			return false;
		}
		return state.is(this.blockTag);
	}

	public int getStateExperienceDropped(RandomSource random) {
		return this.stateExperienceDropped == null ? -1 : this.stateExperienceDropped.getIntRandBetween(random);
	}

	public void apply(boolean applyingOriginal) {
        BlockData originalData;
        if (this.block != null) {
            originalData = new BlockData(block);
			this.block.getStateDefinition().getPossibleStates().forEach(blockState -> {
				if (this.blockStates.isEmpty() || this.blockStates.contains(blockState)) {
					originalData.blockStates.add(blockState);
					if (this.stateHardness != null) {
						originalData.stateHardness = blockState.destroySpeed;
						blockState.destroySpeed = this.stateHardness;
					}
					if (this.stateRequiresCorrectToolForDrops != null) {
						originalData.stateRequiresCorrectToolForDrops = blockState.requiresCorrectToolForDrops;
						blockState.requiresCorrectToolForDrops = this.stateRequiresCorrectToolForDrops;
					}
					if (this.stateNoteBlockInstrument != null) {
						originalData.stateNoteBlockInstrument = blockState.instrument;
						blockState.instrument = this.stateNoteBlockInstrument;
					}
				}
			});
			if (this.explosionResistance != null) {
				originalData.explosionResistance = this.block.explosionResistance;
				this.block.explosionResistance = this.explosionResistance;
			}
			if (this.friction != null) {
				originalData.friction = this.block.friction;
				this.block.friction = this.friction;
			}
			if (this.speedFactor != null) {
				originalData.speedFactor = this.block.speedFactor;
				this.block.speedFactor = this.speedFactor;
			}
			if (this.jumpFactor != null) {
				originalData.jumpFactor = this.block.jumpFactor;
				this.block.jumpFactor = this.jumpFactor;
			}
        }
		else {
            originalData = new BlockData(this.blockTag);
			ITag<Block> blockTag = ForgeRegistries.BLOCKS.tags().getTag(this.blockTag);
			blockTag.stream().forEach(block -> {
				block.getStateDefinition().getPossibleStates().forEach(blockState -> {
					if (this.stateHardness != null) {
						originalData.stateHardness = blockState.destroySpeed;
						blockState.destroySpeed = this.stateHardness;
					}
					if (this.stateRequiresCorrectToolForDrops != null) {
						originalData.stateRequiresCorrectToolForDrops = blockState.requiresCorrectToolForDrops;
						blockState.requiresCorrectToolForDrops = this.stateRequiresCorrectToolForDrops;
					}
					if (this.stateNoteBlockInstrument != null) {
						originalData.stateNoteBlockInstrument = blockState.instrument;
						blockState.instrument = this.stateNoteBlockInstrument;
					}
				});
				if (this.explosionResistance != null) {
					originalData.explosionResistance = block.explosionResistance;
					block.explosionResistance = this.explosionResistance;
				}
				if (this.friction != null) {
					originalData.friction = block.friction;
					block.friction = this.friction;
				}
				if (this.speedFactor != null) {
					originalData.speedFactor = block.speedFactor;
					block.speedFactor = this.speedFactor;
				}
				if (this.jumpFactor != null) {
					originalData.jumpFactor = block.jumpFactor;
					block.jumpFactor = this.jumpFactor;
				}
			});
        }
        if (!applyingOriginal)
            BlockDataReloadListener.ORIGINAL_DATA.add(originalData);
    }

	public static final java.lang.reflect.Type LIST_TYPE = new TypeToken<ArrayList<BlockData>>(){}.getType();

	public static class Serializer implements JsonDeserializer<BlockData>, JsonSerializer<BlockData> {
		@Override
		public BlockData deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			JsonObject jObject = json.getAsJsonObject();
			Float hardness = ILGsonHelper.getAsNullableFloat(jObject, "state_hardness");
			Boolean requiresCorrectToolForDrops = ILGsonHelper.getAsNullableBoolean(jObject, "state_requires_correct_tool_for_drops");
			NoteBlockInstrument instrument = null;
			if (jObject.has("state_instrument")) {
				String stringInstrument = GsonHelper.getAsString(jObject, "state_instrument");
				instrument = Arrays.stream(NoteBlockInstrument.values())
						.filter(noteBlockInstrument -> noteBlockInstrument.getSerializedName().equals(stringInstrument))
						.findFirst()
						.orElseThrow();
			}
			MinMax experienceDropped = context.deserialize(jObject.get("state_experience_dropped"), MinMax.class);
			Float explosionResistance = ILGsonHelper.getAsNullableFloat(jObject, "explosion_resistance");
			Float friction = ILGsonHelper.getAsNullableFloat(jObject, "friction");
			Float speedFactor = ILGsonHelper.getAsNullableFloat(jObject, "speed_factor");
			Float jumpFactor = ILGsonHelper.getAsNullableFloat(jObject, "jump_factor");
			Float boneMealFailChance = ILGsonHelper.getAsNullableFloat(jObject, "bone_meal_fail_chance");

			if (jObject.has("block_tag") && jObject.has("states"))
				throw new JsonParseException("`block_tag` and `states` cannot be used together");
			if (!jObject.has("block") && !jObject.has("block_tag"))
				throw new JsonParseException("Either `block` or `block_tag` must be present");
			boolean required = GsonHelper.getAsBoolean(jObject, "required", true);
			if (jObject.has("block")) {
				String sBlockId = jObject.get("block").getAsString();
				ResourceLocation blockId = ResourceLocation.tryParse(sBlockId);
				if (blockId == null)
					throw new JsonParseException("Failed to parse block id for %s".formatted(sBlockId));
				Block block = ForgeRegistries.BLOCKS.getValue(blockId);
				if (block == Blocks.AIR) {
					if (!required)
						return null;
					else
						throw new JsonParseException("Failed to get block for %s".formatted(sBlockId));
				}
				List<BlockState> blockStates = new ArrayList<>();
				if (jObject.has("states")) {
					JsonArray array = jObject.getAsJsonArray("states");
					for (JsonElement element : array) {
						PropertiesAndValues propertyAndValues = PropertiesAndValues.of(block.getStateDefinition(), element.getAsString());
						block.getStateDefinition().getPossibleStates().forEach(blockState -> {
							if (propertyAndValues.match(blockState))
								blockStates.add(blockState);
						});
					}
				}
				return new BlockData(block, blockStates, hardness, requiresCorrectToolForDrops, instrument, experienceDropped, explosionResistance, friction, speedFactor, jumpFactor, boneMealFailChance);
			}
			else {
				ResourceLocation blockTagId = ResourceLocation.tryParse(jObject.get("block_tag").getAsString());
				if (blockTagId == null)
					throw new JsonParseException("Failed to parse block tag id for %s".formatted(jObject.get("block_tag").getAsString()));
				TagKey<Block> blockTag = TagKey.create(Registries.BLOCK, blockTagId);
				return new BlockData(blockTag, hardness, requiresCorrectToolForDrops, instrument, experienceDropped, explosionResistance, friction, speedFactor, jumpFactor, boneMealFailChance);
			}
		}

		@Override
		public JsonElement serialize(BlockData src, java.lang.reflect.Type typeOfSrc, JsonSerializationContext context) {
			throw new NotImplementedException();
			/*JsonObject jObject = new JsonObject();
			if (!src.blockStates.isEmpty()) {
				JsonArray array = new JsonArray();
				for (BlockState state : src.blockStates) {
					array.add("%s=%s".formatted(property.property.getName(), property.value));
				}
			}
			if (src.stateHardness != null)
				jObject.addProperty("hardness", src.stateHardness);
			if (src.explosionResistance != null)
				jObject.addProperty("explosion_resistance", src.explosionResistance);
			return jObject;*/
		}
	}

	public static BlockData fromNetwork(FriendlyByteBuf byteBuf) {
		Block block = byteBuf.readNullable(b -> b.readById(BuiltInRegistries.BLOCK));
		ResourceLocation blockTagId = byteBuf.readNullable(FriendlyByteBuf::readResourceLocation);
		List<BlockState> blockStates = new ArrayList<>();
		TagKey<Block> blockTag = null;
		if (block != null) {
			byte stateCount = byteBuf.readByte();
			for (int i = 0; i < stateCount; i++) {
				PropertiesAndValues propertiesAndValues = PropertiesAndValues.of(block.getStateDefinition(), byteBuf.readUtf());
				block.getStateDefinition().getPossibleStates().forEach(blockState -> {
					if (propertiesAndValues.match(blockState))
						blockStates.add(blockState);
				});
			}
		}
		else {
			blockTag = TagKey.create(Registries.BLOCK, blockTagId);
		}
		Float hardness = byteBuf.readNullable(FriendlyByteBuf::readFloat);
		Boolean requiresCorrectToolForDrops = byteBuf.readNullable(FriendlyByteBuf::readBoolean);
		NoteBlockInstrument noteBlockInstrument = byteBuf.readNullable(buf -> buf.readEnum(NoteBlockInstrument.class));
		MinMax experienceDropped = byteBuf.readNullable(b -> new MinMax(b.readDouble(), b.readDouble()));
		Float explosionResistance = byteBuf.readNullable(FriendlyByteBuf::readFloat);
		Float friction = byteBuf.readNullable(FriendlyByteBuf::readFloat);
		Float speedFactor = byteBuf.readNullable(FriendlyByteBuf::readFloat);
		Float jumpFactor = byteBuf.readNullable(FriendlyByteBuf::readFloat);
		Float boneMealFailChance = byteBuf.readNullable(FriendlyByteBuf::readFloat);
		if (block != null)
			return new BlockData(block, blockStates, hardness, requiresCorrectToolForDrops, noteBlockInstrument, experienceDropped, explosionResistance, friction, speedFactor, jumpFactor, boneMealFailChance);
		else
			return new BlockData(blockTag, hardness, requiresCorrectToolForDrops, noteBlockInstrument, experienceDropped, explosionResistance, friction, speedFactor, jumpFactor, boneMealFailChance);
	}

	public void toNetwork(FriendlyByteBuf byteBuf) {
		byteBuf.writeNullable(this.block, (b, value) -> b.writeId(BuiltInRegistries.BLOCK, value));
		byteBuf.writeNullable(this.blockTag, (b, value) -> b.writeResourceLocation(value.location()));
		if (this.block != null) {
			byteBuf.writeByte(this.blockStates.size());
			for (BlockState state : this.blockStates) {
				StringBuilder stringBuilder = new StringBuilder();
				for (Property<?> property : state.getProperties()) {
					if (!stringBuilder.isEmpty())
						stringBuilder.append(",");
					stringBuilder.append(property.getName()).append("=").append(state.getValue(property));
				}
				byteBuf.writeUtf(stringBuilder.toString());
			}
		}
		byteBuf.writeNullable(this.stateHardness, FriendlyByteBuf::writeFloat);
		byteBuf.writeNullable(this.stateRequiresCorrectToolForDrops, FriendlyByteBuf::writeBoolean);
		byteBuf.writeNullable(this.stateNoteBlockInstrument, FriendlyByteBuf::writeEnum);
		byteBuf.writeNullable(this.stateExperienceDropped, (b, minMax) -> {
			b.writeDouble(minMax.min);
			b.writeDouble(minMax.max);
		});
		byteBuf.writeNullable(this.explosionResistance, FriendlyByteBuf::writeFloat);
		byteBuf.writeNullable(this.friction, FriendlyByteBuf::writeFloat);
		byteBuf.writeNullable(this.speedFactor, FriendlyByteBuf::writeFloat);
		byteBuf.writeNullable(this.jumpFactor, FriendlyByteBuf::writeFloat);
		byteBuf.writeNullable(this.boneMealFailChance, FriendlyByteBuf::writeFloat);
	}

	//Thanks Random832
	public record PropertyAndValue<T extends Comparable<T>>(Property<T> property, T value) {
		static <T extends Comparable<T>> PropertyAndValue<?> of(StateDefinition definition, String string) {
			String[] split = string.split("=", 2);
			Property<T> prop = (Property<T>) definition.getProperty(split[0]);
			if (prop == null)
				throw new NullPointerException("Property %s doesn't belong to %s".formatted(split[0], definition));
			T value = prop.getValue(split[1]).orElseThrow();
			return new PropertyAndValue<>(prop, value);
		}

		boolean match(BlockState state) {
			return state.getValue(property) == value;
		}
	}

	public static class PropertiesAndValues extends ArrayList<PropertyAndValue<?>> {
		public static PropertiesAndValues of(StateDefinition definition, String string) {
			PropertiesAndValues propertiesAndValues = new PropertiesAndValues();
			String[] split = string.split(",");
			for (String s : split) {
				propertiesAndValues.add(PropertyAndValue.of(definition, s));
			}
			return propertiesAndValues;
		}

		public boolean match(BlockState state) {
			for (PropertyAndValue<?> propertyAndValue : this) {
				if (!propertyAndValue.match(state))
					return false;
			}
			return true;
		}
	}
}
