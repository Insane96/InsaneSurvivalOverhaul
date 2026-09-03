package insane96mcp.insanesurvivaloverhaul.module.combat.fletching;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.module.combat.fletching.entity.*;
import insane96mcp.insanesurvivaloverhaul.setup.ISORegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;

@LoadFeature(module = ISOModules.COMBAT, description = "Gives a use to the fletching table, letting you craft better arrows out of vanilla ones.")
public class FletchingFeature extends Feature {
    public static final DeferredHolder<RecipeType<?>, RecipeType<FletchingRecipe>> FLETCHING_RECIPE_TYPE =
            ISORegistries.RECIPE_TYPES.register("fletching", () -> RecipeType.simple(InsaneSO.id("fletching")));
    public static final DeferredHolder<RecipeSerializer<?>, FletchingRecipeSerializer> FLETCHING_RECIPE_SERIALIZER =
            ISORegistries.RECIPE_SERIALIZERS.register("fletching", FletchingRecipeSerializer::new);
    public static final DeferredHolder<MenuType<?>, MenuType<FletchingMenu>> FLETCHING_MENU_TYPE =
            ISORegistries.MENU_TYPES.register("fletching", () -> new MenuType<>(FletchingMenu::new, FeatureFlags.VANILLA_SET));

    public static final DeferredHolder<EntityType<?>, EntityType<QuartzArrow>> QUARTZ_ARROW = registerArrow("quartz_arrow", QuartzArrow::new);
    public static final DeferredHolder<EntityType<?>, EntityType<DiamondArrow>> DIAMOND_ARROW = registerArrow("diamond_arrow", DiamondArrow::new);
    public static final DeferredHolder<EntityType<?>, EntityType<ExplosiveArrow>> EXPLOSIVE_ARROW = registerArrow("explosive_arrow", ExplosiveArrow::new);
    public static final DeferredHolder<EntityType<?>, EntityType<TorchArrow>> TORCH_ARROW = registerArrow("torch_arrow", TorchArrow::new);
    public static final DeferredHolder<EntityType<?>, EntityType<IceArrow>> ICE_ARROW = registerArrow("ice_arrow", IceArrow::new);

    // Base damage values carried over from the 1.20.1 port (vanilla arrows deal 2.0).
    public static final DeferredHolder<Item, ISOArrowItem> QUARTZ_ARROW_ITEM =
            ISORegistries.ITEMS.register("quartz_arrow", () -> new ISOArrowItem(QUARTZ_ARROW::get, 0.5f, new Item.Properties()));
    public static final DeferredHolder<Item, ISOArrowItem> DIAMOND_ARROW_ITEM =
            ISORegistries.ITEMS.register("diamond_arrow", () -> new ISOArrowItem(DIAMOND_ARROW::get, 3f, new Item.Properties()));
    public static final DeferredHolder<Item, ISOArrowItem> EXPLOSIVE_ARROW_ITEM =
            ISORegistries.ITEMS.register("explosive_arrow", () -> new ISOArrowItem(EXPLOSIVE_ARROW::get, 0f, new Item.Properties()));
    public static final DeferredHolder<Item, ISOArrowItem> TORCH_ARROW_ITEM =
            ISORegistries.ITEMS.register("torch_arrow", () -> new ISOArrowItem(TORCH_ARROW::get, 1f, new Item.Properties()));
    public static final DeferredHolder<Item, ISOArrowItem> ICE_ARROW_ITEM =
            ISORegistries.ITEMS.register("ice_arrow", () -> new ISOArrowItem(ICE_ARROW::get, 1f, new Item.Properties()));

    @Config(description = "If disabled, torch arrows only set the target on fire instead of also placing a torch when they hit a block.")
    public static Boolean torchArrowsPlaceTorches = true;

    private static <T extends Arrow> DeferredHolder<EntityType<?>, EntityType<T>> registerArrow(String name, EntityType.EntityFactory<T> factory) {
        return ISORegistries.ENTITY_TYPES.register(name, () -> EntityType.Builder.<T>of(factory, MobCategory.MISC)
                .sized(0.5f, 0.5f)
                .clientTrackingRange(4)
                .updateInterval(3)
                .build(name));
    }
}
