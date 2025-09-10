package insane96mcp.iguanatweaksreborn.setup.client;

import net.minecraft.util.StringRepresentable;

public enum ISOBookCategory implements StringRepresentable {
    FLETCHING_MISC("fletching_misc"),
    UNKNOWN("unknown");

    public static final EnumCodec<ISOBookCategory> CODEC = StringRepresentable.fromEnum(ISOBookCategory::values);
    private final String name;

    ISOBookCategory(String name) {
        this.name = name;
    }

    public String getSerializedName() {
        return this.name;
    }
}
