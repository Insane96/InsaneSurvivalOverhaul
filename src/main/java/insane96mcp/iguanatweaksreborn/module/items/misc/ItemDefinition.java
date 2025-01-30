package insane96mcp.iguanatweaksreborn.module.items.misc;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.reflect.TypeToken;
import insane96mcp.iguanatweaksreborn.mixin.TieredItemAccessor;
import insane96mcp.iguanatweaksreborn.module.combat.RegeneratingAbsorption;
import insane96mcp.iguanatweaksreborn.module.items.unbreakableitems.UnbreakableItems;
import insane96mcp.iguanatweaksreborn.utils.ISOLogHelper;
import insane96mcp.insanelib.base.JsonFeature;
import insane96mcp.insanelib.data.IdTagMatcher;
import insane96mcp.insanelib.data.SerializableAttributeModifier;
import insane96mcp.insanelib.util.json.ILGsonHelper;
import insane96mcp.insanelib.util.json.validator.DoubleMinMaxValidator;
import insane96mcp.insanelib.util.json.validator.FloatMinMaxValidator;
import insane96mcp.insanelib.util.json.validator.IntMinMaxValidator;
import net.minecraft.Util;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraftforge.common.TierSortingRegistry;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.UUID;

@JsonAdapter(ItemDefinition.Serializer.class)
public final class ItemDefinition {
	private final IdTagMatcher item;
	@Nullable
	private final Integer maxStackSize;
	private final Durability durability;
    @Nullable
    private final String harvestLevel;
	@Nullable
	private final Double efficiency;
	@Nullable
	private final Integer enchantability;
    @Nullable
    private final Double knockbackMultiplier;
    @Nullable
    private final Integer scytheRadius;
	@Nullable
	private final Double baseAttackDamage;
	@Nullable
	private final Double baseAttackSpeed;
	@Nullable
	private final Double baseArmor;
	@Nullable
	private final Double baseArmorToughness;
	@Nullable
	private final Double baseKnockbackResistance;
	@Nullable
	private final Double baseRegeneratingAbsorption;
	@Nullable
	private final Double baseRegenAbsorptionSpeed;
	@Nullable
	private final Double movementSpeedPenalty;
	@Nullable
	private final List<SerializableAttributeModifier> modifiers;
    public ItemDefinition(@NotNull IdTagMatcher item, @Nullable Integer maxStackSize, @Nullable Integer durability, @Nullable Integer durabilityBonus, @Nullable Float durabilityMultiplier, @Nullable String harvestLevel, @Nullable Double efficiency, @Nullable Integer enchantability, @Nullable Double knockbackMultiplier, @Nullable Integer scytheRadius, @Nullable Double baseAttackDamage, @Nullable Double baseAttackSpeed, @Nullable Double baseArmor, @Nullable Double baseArmorToughness, @Nullable Double baseKnockbackResistance, @Nullable Double baseRegeneratingAbsorption, @Nullable Double baseRegenAbsorptionSpeed, @Nullable Double movementSpeedPenalty, @Nullable List<SerializableAttributeModifier> modifiers) {
        this.item = item;
        this.maxStackSize = maxStackSize;
        this.durability = new Durability(durability, durabilityBonus, durabilityMultiplier);
        this.harvestLevel = harvestLevel;
        this.efficiency = efficiency;
        this.enchantability = enchantability;
        this.knockbackMultiplier = knockbackMultiplier;
        this.scytheRadius = scytheRadius;
        this.baseAttackDamage = baseAttackDamage;
        this.baseAttackSpeed = baseAttackSpeed;
        this.baseArmor = baseArmor;
        this.baseArmorToughness = baseArmorToughness;
        this.baseKnockbackResistance = baseKnockbackResistance;
        this.baseRegeneratingAbsorption = baseRegeneratingAbsorption;
        this.baseRegenAbsorptionSpeed = baseRegenAbsorptionSpeed;
        this.movementSpeedPenalty = movementSpeedPenalty;
        this.modifiers = modifiers;
    }

    public void applyStats(boolean isClientSide) {
        List<Item> items = JsonFeature.getAllItems(this.item, isClientSide);
        for (Item item : items) {
            Durability durability = new Durability(null, null, null);
            if (!ItemDefinitionsReloadListener.DURABILITY_MAP.containsKey(item))
                ItemDefinitionsReloadListener.DURABILITY_MAP.put(item, durability);
            else
                durability = ItemDefinitionsReloadListener.DURABILITY_MAP.get(item);
            if (this.durability.durability != null)
                durability.durability = this.durability.durability;
            if (this.durability.durabilityBonus != null)
                durability.durabilityBonus = this.durability.durabilityBonus;
            if (this.durability.durabilityMultiplier != null)
                durability.durabilityMultiplier = this.durability.durabilityMultiplier;
            if (item instanceof DiggerItem diggerItem) {
                if (this.efficiency != null)
                    diggerItem.speed = this.efficiency.floatValue();
                if (this.harvestLevel != null) {
                    Tier tier = TierSortingRegistry.byName(ResourceLocation.tryParse(harvestLevel));
                    if (tier != null)
                        ((TieredItemAccessor)diggerItem).setTier(tier);
                    else
                        ISOLogHelper.warn("Failed to parse harvest level %s", this.harvestLevel);
                }
            }
            if (this.maxStackSize != null)
                item.maxStackSize = this.maxStackSize;
        }
    }

    public static final UUID BASE_ATTACK_DAMAGE_UUID = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
    public static final UUID BASE_ATTACK_SPEED_UUID = UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3");
    private static final EnumMap<ArmorItem.Type, UUID> ARMOR_MODIFIER_UUID_PER_TYPE = Util.make(new EnumMap<>(ArmorItem.Type.class), (enumMap) -> {
        enumMap.put(ArmorItem.Type.BOOTS, UUID.fromString("845DB27C-C624-495F-8C9F-6020A9A58B6B"));
        enumMap.put(ArmorItem.Type.LEGGINGS, UUID.fromString("D8499B04-0E66-4726-AB29-64469D734E0D"));
        enumMap.put(ArmorItem.Type.CHESTPLATE, UUID.fromString("9F3D476D-C118-4544-8365-64846904B48E"));
        enumMap.put(ArmorItem.Type.HELMET, UUID.fromString("2AD3F246-FEE1-4E67-B886-69FD380BB150"));
    });

    public void applyAttributes(ItemAttributeModifierEvent event, ItemStack stack, Multimap<Attribute, AttributeModifier> modifiers) {
        if (!this.item.matchesItem(stack))
            return;
        Multimap<Attribute, AttributeModifier> toAdd = HashMultimap.create();
        Multimap<Attribute, AttributeModifier> toRemove = HashMultimap.create();
        for (var entry : modifiers.entries()) {
            if (this.baseAttackDamage != null && event.getSlotType() == EquipmentSlot.MAINHAND) {
                double materialAd = 0d;
                if (stack.getItem() instanceof TieredItem tieredItem)
                    materialAd = tieredItem.getTier().getAttackDamageBonus();
                if (this.baseAttackDamage + materialAd > 0d) {
                    toAdd.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier", this.baseAttackDamage + materialAd, AttributeModifier.Operation.ADDITION));
                }
                if (entry.getValue().getId().equals(BASE_ATTACK_DAMAGE_UUID) && entry.getKey().equals(Attributes.ATTACK_DAMAGE))
                    toRemove.put(entry.getKey(), entry.getValue());
            }
            if (this.baseAttackSpeed != null && event.getSlotType() == EquipmentSlot.MAINHAND) {
                if (this.baseAttackSpeed > 0d)
                    toAdd.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier", -(4d - this.baseAttackSpeed), AttributeModifier.Operation.ADDITION));
                if (entry.getValue().getId().equals(BASE_ATTACK_SPEED_UUID) && entry.getKey().equals(Attributes.ATTACK_SPEED))
                    toRemove.put(entry.getKey(), entry.getValue());
            }
            if (this.baseArmor != null && stack.getItem() instanceof ArmorItem armorItem) {
                if (this.baseArmor > 0d)
                    toAdd.put(Attributes.ARMOR, new AttributeModifier(ARMOR_MODIFIER_UUID_PER_TYPE.get(armorItem.getType()), "Armor modifier", this.baseArmor, AttributeModifier.Operation.ADDITION));
                if (entry.getValue().getId().equals(ARMOR_MODIFIER_UUID_PER_TYPE.get(armorItem.getType())) && entry.getKey().equals(Attributes.ARMOR))
                    toRemove.put(entry.getKey(), entry.getValue());
            }
            if (this.baseArmorToughness != null && stack.getItem() instanceof ArmorItem armorItem) {
                if (this.baseArmorToughness > 0d)
                    toAdd.put(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(ARMOR_MODIFIER_UUID_PER_TYPE.get(armorItem.getType()), "Armor toughness", this.baseArmorToughness, AttributeModifier.Operation.ADDITION));
                if (entry.getValue().getId().equals(ARMOR_MODIFIER_UUID_PER_TYPE.get(armorItem.getType())) && entry.getKey().equals(Attributes.ARMOR_TOUGHNESS))
                    toRemove.put(entry.getKey(), entry.getValue());
            }
            if (this.baseKnockbackResistance != null && stack.getItem() instanceof ArmorItem armorItem) {
                if (this.baseKnockbackResistance > 0d)
                    toAdd.put(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(ARMOR_MODIFIER_UUID_PER_TYPE.get(armorItem.getType()), "Armor knockback resistance", this.baseKnockbackResistance, AttributeModifier.Operation.ADDITION));
                if (entry.getValue().getId().equals(ARMOR_MODIFIER_UUID_PER_TYPE.get(armorItem.getType())) && entry.getKey().equals(Attributes.KNOCKBACK_RESISTANCE))
                    toRemove.put(entry.getKey(), entry.getValue());
            }
            if (this.baseRegeneratingAbsorption != null && stack.getItem() instanceof ArmorItem armorItem) {
                if (this.baseRegeneratingAbsorption > 0d)
                    toAdd.put(RegeneratingAbsorption.ATTRIBUTE.get(), new AttributeModifier(ARMOR_MODIFIER_UUID_PER_TYPE.get(armorItem.getType()), "Armor Regenerating Absorption", this.baseRegeneratingAbsorption, AttributeModifier.Operation.ADDITION));
                if (entry.getValue().getId().equals(ARMOR_MODIFIER_UUID_PER_TYPE.get(armorItem.getType())) && entry.getKey().equals(RegeneratingAbsorption.ATTRIBUTE.get()))
                    toRemove.put(entry.getKey(), entry.getValue());
            }
            if (this.baseRegenAbsorptionSpeed != null && stack.getItem() instanceof ArmorItem armorItem) {
                if (this.baseRegenAbsorptionSpeed > 0d)
                    toAdd.put(RegeneratingAbsorption.SPEED_ATTRIBUTE.get(), new AttributeModifier(ARMOR_MODIFIER_UUID_PER_TYPE.get(armorItem.getType()), "Armor Regenerating Absorption Speed", this.baseRegenAbsorptionSpeed, AttributeModifier.Operation.ADDITION));
                if (entry.getValue().getId().equals(ARMOR_MODIFIER_UUID_PER_TYPE.get(armorItem.getType())) && entry.getKey().equals(RegeneratingAbsorption.SPEED_ATTRIBUTE.get()))
                    toRemove.put(entry.getKey(), entry.getValue());
            }
            if (this.movementSpeedPenalty != null && stack.getItem() instanceof ArmorItem armorItem) {
                if (this.movementSpeedPenalty > 0d)
                    toAdd.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(ARMOR_MODIFIER_UUID_PER_TYPE.get(armorItem.getType()), "Armor Movement Speed Reduction", getSpeedReductionPerArmor(-this.movementSpeedPenalty, armorItem), AttributeModifier.Operation.MULTIPLY_BASE));
            }
        }

        //Try to remove original modifiers first
        if (event.getItemStack().is(UnbreakableItems.REMOVE_ORIGINAL_MODIFIERS_TAG)) {
            Multimap<Attribute, AttributeModifier> originalModifiers = event.getOriginalModifiers();
            toRemove.putAll(originalModifiers);
        }

        if (this.modifiers != null) {
            for (SerializableAttributeModifier attributeModifier : this.modifiers) {
                if (!attributeModifier.slots().isEmpty() && !attributeModifier.slots().contains(event.getSlotType()))
                    continue;
                if (LivingEntity.getEquipmentSlotForItem(stack) != event.getSlotType())
                    continue;
                AttributeModifier modifier = attributeModifier.getModifier();
                toAdd.put(attributeModifier.attribute().get(), modifier);
            }
        }
        toRemove.forEach(event::removeModifier);
        toAdd.forEach(event::addModifier);
    }

    public boolean matches(ItemStack stack) {
        return this.item.matchesItem(stack);
    }

    public boolean matches(Item stack) {
        return this.item.matchesItem(stack);
    }

    private static double getSpeedReductionPerArmor(double totalReduction, ArmorItem item) {
        return switch (item.getEquipmentSlot()) {
            case HEAD -> totalReduction * 0.2d;
            case CHEST -> totalReduction * 0.35d;
            case LEGS -> totalReduction * 0.3d;
            default -> totalReduction * 0.15d;
        };
    }

    public static final Type LIST_TYPE = new TypeToken<ArrayList<ItemDefinition>>() {}.getType();

    public static class Serializer implements JsonDeserializer<ItemDefinition>, JsonSerializer<ItemDefinition> {
        @Override
        public ItemDefinition deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jObject = json.getAsJsonObject();
            IdTagMatcher item = context.deserialize(jObject.get("item"), IdTagMatcher.class);
            Integer maxStackSize = ILGsonHelper.getAsNullableInt(jObject, "max_stack", IntMinMaxValidator.between(1, 64));
            Integer durability = ILGsonHelper.getAsNullableInt(jObject, "durability", IntMinMaxValidator.atLeast(1));
            Integer durabilityBonus = ILGsonHelper.getAsNullableInt(jObject, "durability_bonus", IntMinMaxValidator.atLeast(1));
            Float durabilityMultiplier = ILGsonHelper.getAsNullableFloat(jObject, "durability_multiplier",  FloatMinMaxValidator.atLeast(0));
            String harvestLevel = GsonHelper.getAsString(jObject, "harvest_level", null);
            Double efficiency = ILGsonHelper.getAsNullableDouble(jObject, "efficiency", DoubleMinMaxValidator.atLeast(0));
            Integer enchantability = ILGsonHelper.getAsNullableInt(jObject, "enchantability", IntMinMaxValidator.atLeast(0));
            Double knockbackMultiplier = ILGsonHelper.getAsNullableDouble(jObject, "knockback_multiplier", DoubleMinMaxValidator.between(0, 1));
            Integer scytheRadius = ILGsonHelper.getAsNullableInt(jObject, "scythe_radius", IntMinMaxValidator.atLeast(0));
            Double baseAttackDamage = ILGsonHelper.getAsNullableDouble(jObject, "attack_damage");
            Double baseAttackSpeed = ILGsonHelper.getAsNullableDouble(jObject, "attack_speed");
            Double baseArmor = ILGsonHelper.getAsNullableDouble(jObject, "armor");
            Double baseToughness = ILGsonHelper.getAsNullableDouble(jObject, "armor_toughness");
            Double regeneratingAbsorption = ILGsonHelper.getAsNullableDouble(jObject, "regenerating_absorption");
            Double regeneratingAbsorptionSpeed = ILGsonHelper.getAsNullableDouble(jObject, "regenerating_absorption_speed");
            Double baseKnockbackResistance = ILGsonHelper.getAsNullableDouble(jObject, "knockback_resistance");
            Double movementSpeedPenalty = ILGsonHelper.getAsNullableDouble(jObject, "movement_speed_penalty");
            List<SerializableAttributeModifier> modifiers = null;
            if (jObject.has("modifiers"))
                modifiers = context.deserialize(jObject.get("modifiers"), SerializableAttributeModifier.LIST_TYPE);
            return new ItemDefinition(item, maxStackSize, durability, durabilityBonus, durabilityMultiplier, harvestLevel, efficiency, enchantability, knockbackMultiplier, scytheRadius, baseAttackDamage, baseAttackSpeed, baseArmor, baseToughness, baseKnockbackResistance, regeneratingAbsorption, regeneratingAbsorptionSpeed, movementSpeedPenalty, modifiers);
        }

        @Override
        public JsonElement serialize(ItemDefinition src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jObject = new JsonObject();
            ILGsonHelper.add(jObject, context, "item", src.item);
            ILGsonHelper.addProperty(jObject, "max_stack", src.maxStackSize);
            ILGsonHelper.addProperty(jObject, "durability", src.durability.durability);
            ILGsonHelper.addProperty(jObject, "durability_bonus", src.durability.durabilityBonus);
            ILGsonHelper.addProperty(jObject, "durability_multiplier", src.durability.durabilityMultiplier);
            ILGsonHelper.addProperty(jObject, "harvest_level", src.harvestLevel);
            ILGsonHelper.addProperty(jObject, "efficiency", src.efficiency);
            ILGsonHelper.addProperty(jObject, "enchantability", src.enchantability);
            ILGsonHelper.addProperty(jObject, "knockback_multiplier", src.knockbackMultiplier);
            ILGsonHelper.addProperty(jObject, "scythe_radius", src.scytheRadius);
            ILGsonHelper.addProperty(jObject, "attack_damage", src.baseAttackDamage);
            ILGsonHelper.addProperty(jObject, "attack_speed", src.baseAttackSpeed);
            ILGsonHelper.addProperty(jObject, "armor", src.baseArmor);
            ILGsonHelper.addProperty(jObject, "armor_toughness", src.baseArmorToughness);
            ILGsonHelper.addProperty(jObject, "knockback_resistance", src.baseKnockbackResistance);
            ILGsonHelper.addProperty(jObject, "regenerating_absorption", src.baseRegeneratingAbsorption);
            ILGsonHelper.addProperty(jObject, "regenerating_absorption_speed", src.baseRegenAbsorptionSpeed);
            ILGsonHelper.addProperty(jObject, "movement_speed_penalty", src.movementSpeedPenalty);
            ILGsonHelper.add(jObject, context, "modifiers", src.modifiers, SerializableAttributeModifier.LIST_TYPE);
            return jObject;
        }
    }

    public static ItemDefinition fromNetwork(FriendlyByteBuf byteBuf) {
        String utf = byteBuf.readUtf();
        IdTagMatcher item = IdTagMatcher.parseLine(utf);
        if (item == null)
            throw new NullPointerException("Parsing item from %s for Item Statistics returned null".formatted(utf));
        Integer maxStackSize = byteBuf.readNullable(FriendlyByteBuf::readInt);
        Integer durability = byteBuf.readNullable(FriendlyByteBuf::readInt);
        Integer durabilityBonus = byteBuf.readNullable(FriendlyByteBuf::readInt);
        Float durabilityMultiplier = byteBuf.readNullable(FriendlyByteBuf::readFloat);
        String harvestLevel = byteBuf.readNullable(FriendlyByteBuf::readUtf);
        Double efficiency = byteBuf.readNullable(FriendlyByteBuf::readDouble);
        Integer enchantability = byteBuf.readNullable(FriendlyByteBuf::readInt);
        Double knockbackMultiplier = byteBuf.readNullable(FriendlyByteBuf::readDouble);
        Integer scytheRadius = byteBuf.readNullable(FriendlyByteBuf::readInt);
        Double baseAttackDamage = byteBuf.readNullable(FriendlyByteBuf::readDouble);
        Double baseAttackSpeed = byteBuf.readNullable(FriendlyByteBuf::readDouble);
        Double baseArmor = byteBuf.readNullable(FriendlyByteBuf::readDouble);
        Double baseToughness = byteBuf.readNullable(FriendlyByteBuf::readDouble);
        Double baseKnockbackResistance = byteBuf.readNullable(FriendlyByteBuf::readDouble);
        Double baseRegeneratingAbsorption = byteBuf.readNullable(FriendlyByteBuf::readDouble);
        Double baseRegeneratingAbsorptionSpeed = byteBuf.readNullable(FriendlyByteBuf::readDouble);
        Double movementSpeedPenalty = byteBuf.readNullable(FriendlyByteBuf::readDouble);
        boolean hasModifiers = byteBuf.readBoolean();
        List<SerializableAttributeModifier> modifiers = null;
        if (hasModifiers) {
            int size = byteBuf.readInt();
            modifiers = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                modifiers.add(SerializableAttributeModifier.fromNetwork(byteBuf));
            }
        }
        return new ItemDefinition(item, maxStackSize, durability, durabilityBonus, durabilityMultiplier, harvestLevel, efficiency, enchantability, knockbackMultiplier, scytheRadius, baseAttackDamage, baseAttackSpeed, baseArmor, baseToughness, baseKnockbackResistance, baseRegeneratingAbsorption, baseRegeneratingAbsorptionSpeed, movementSpeedPenalty, modifiers);
    }

    public void toNetwork(FriendlyByteBuf byteBuf) {
        byteBuf.writeUtf(this.item.getSerializedName());
        byteBuf.writeNullable(this.maxStackSize, FriendlyByteBuf::writeInt);
        byteBuf.writeNullable(this.durability.durability, FriendlyByteBuf::writeInt);
        byteBuf.writeNullable(this.durability.durabilityBonus, FriendlyByteBuf::writeInt);
        byteBuf.writeNullable(this.durability.durabilityMultiplier, FriendlyByteBuf::writeFloat);
        byteBuf.writeNullable(this.harvestLevel, FriendlyByteBuf::writeUtf);
        byteBuf.writeNullable(this.efficiency, FriendlyByteBuf::writeDouble);
        byteBuf.writeNullable(this.enchantability, FriendlyByteBuf::writeInt);
        byteBuf.writeNullable(this.knockbackMultiplier, FriendlyByteBuf::writeDouble);
        byteBuf.writeNullable(this.scytheRadius, FriendlyByteBuf::writeInt);
        byteBuf.writeNullable(this.baseAttackDamage, FriendlyByteBuf::writeDouble);
        byteBuf.writeNullable(this.baseAttackSpeed, FriendlyByteBuf::writeDouble);
        byteBuf.writeNullable(this.baseArmor, FriendlyByteBuf::writeDouble);
        byteBuf.writeNullable(this.baseArmorToughness, FriendlyByteBuf::writeDouble);
        byteBuf.writeNullable(this.baseKnockbackResistance, FriendlyByteBuf::writeDouble);
        byteBuf.writeNullable(this.baseRegeneratingAbsorption, FriendlyByteBuf::writeDouble);
        byteBuf.writeNullable(this.baseRegenAbsorptionSpeed, FriendlyByteBuf::writeDouble);
        byteBuf.writeNullable(this.movementSpeedPenalty, FriendlyByteBuf::writeDouble);
        if (this.modifiers != null) {
            byteBuf.writeBoolean(true);
            byteBuf.writeInt(this.modifiers.size());
            for (SerializableAttributeModifier attributeModifier : this.modifiers) {
                attributeModifier.toNetwork(byteBuf);
            }
        }
        else {
            byteBuf.writeBoolean(false);
        }
    }

    public IdTagMatcher item() {
        return item;
    }

    public Durability durability() {
        return durability;
    }

    @Nullable
    public Double efficiency() {
        return efficiency;
    }

    @Nullable
    public Integer enchantability() {
        return enchantability;
    }

    @Nullable
    public Double knockbackMultiplier() {
        return knockbackMultiplier;
    }

    @Nullable
    public Integer scytheRadius() {
        return scytheRadius;
    }

    @Nullable
    public List<SerializableAttributeModifier> modifiers() {
        return modifiers;
    }

    public static final class Durability {
        @Nullable
        public Integer durability;
        @Nullable
        public Integer durabilityBonus;
        @Nullable
        public Float durabilityMultiplier;

        public Durability(@Nullable Integer durability, @Nullable Integer durabilityBonus, @Nullable Float durabilityMultiplier) {
            this.durability = durability;
            this.durabilityBonus = durabilityBonus;
            this.durabilityMultiplier = durabilityMultiplier;
        }

        public void apply(Item item) {
            if (ItemDefinitionsReloadListener.ORIGINAL_DURABILITY.containsKey(item))
                item.maxDamage = ItemDefinitionsReloadListener.ORIGINAL_DURABILITY.get(item);
            else
                ItemDefinitionsReloadListener.ORIGINAL_DURABILITY.put(item, item.maxDamage);
            if (this.durability != null)
                item.maxDamage = this.durability;
            if (this.durabilityBonus != null)
                item.maxDamage += this.durabilityBonus;
            if (this.durabilityMultiplier != null)
                item.maxDamage *= this.durabilityMultiplier;
        }

    }
}
