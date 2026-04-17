package insane96mcp.insanesurvivaloverhaul.data.generator.client;

import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.combat.unfaironeshot.UnfairOneShot;
import insane96mcp.insanesurvivaloverhaul.module.farming.bonemeal.BoneMeal;
import insane96mcp.insanesurvivaloverhaul.module.farming.crops.Crops;
import insane96mcp.insanesurvivaloverhaul.module.hungerhealth.fooddrinks.FoodDrinks;
import insane96mcp.insanesurvivaloverhaul.module.items.copper.CopperEquipment;
import insane96mcp.insanesurvivaloverhaul.module.items.pouch.Pouch;
import insane96mcp.insanesurvivaloverhaul.module.mobs.spawning.Spawning;
import insane96mcp.insanesurvivaloverhaul.module.movement.minecarts.Minecarts;
import insane96mcp.insanesurvivaloverhaul.module.sleep.Cloth;
import insane96mcp.insanesurvivaloverhaul.module.world.Berries;
import insane96mcp.insanesurvivaloverhaul.module.world.CyanFlower;
import insane96mcp.insanesurvivaloverhaul.module.world.coalfire.CoalFire;
import insane96mcp.insanesurvivaloverhaul.setup.SimpleBlockWithItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.item.armortrim.TrimMaterials;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.LinkedHashMap;
import java.util.Objects;

public class ISOItemModelsProvider extends ItemModelProvider {
    private static final LinkedHashMap<ResourceKey<TrimMaterial>, Float> trimMaterials = new LinkedHashMap<>();
    static {
        trimMaterials.put(TrimMaterials.QUARTZ, 0.1F);
        trimMaterials.put(TrimMaterials.IRON, 0.2F);
        trimMaterials.put(TrimMaterials.NETHERITE, 0.3F);
        trimMaterials.put(TrimMaterials.REDSTONE, 0.4F);
        trimMaterials.put(TrimMaterials.COPPER, 0.5F);
        trimMaterials.put(TrimMaterials.GOLD, 0.6F);
        trimMaterials.put(TrimMaterials.EMERALD, 0.7F);
        trimMaterials.put(TrimMaterials.DIAMOND, 0.8F);
        trimMaterials.put(TrimMaterials.LAPIS, 0.9F);
        trimMaterials.put(TrimMaterials.AMETHYST, 1.0F);
    }

    public ISOItemModelsProvider(PackOutput output, String modid, ExistingFileHelper existingFileHelper) {
        super(output, modid, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        trimmedArmorItem(CopperEquipment.BOOTS);
        trimmedArmorItem(CopperEquipment.LEGGINGS);
        trimmedArmorItem(CopperEquipment.CHESTPLATE);
        trimmedArmorItem(CopperEquipment.HELMET);

        handHeld(CopperEquipment.AXE.get());
        handHeld(CopperEquipment.PICKAXE.get());
        handHeld(CopperEquipment.SHOVEL.get());
        handHeld(CopperEquipment.HOE.get());
        handHeld(CopperEquipment.SWORD.get());

        basicItem(UnfairOneShot.HALF_HEART_TEXTURE.get());

        basicItem(FoodDrinks.OVER_EASY_EGG.get());
        basicItem(FoodDrinks.BROWN_MUSHROOM_STEW.get());
        basicItem(FoodDrinks.RED_MUSHROOM_STEW.get());
        basicItem(FoodDrinks.NETHERIZED_STEW.get());
        basicItem(FoodDrinks.PUMPKIN_PULP.get());

        basicItem(Crops.CARROT_SEEDS.get());
        basicItem(Crops.ROOTED_POTATO.get());
        basicItemWithTexture(Crops.SOLANUM_NEOROSSII.item().get(), InsaneSO.location("block/solanum_neorossii"));
        basicItemWithTexture(CyanFlower.FLOWER.item().get(), InsaneSO.location("block/cyan_flower"));

        blockItem(BoneMeal.RICH_FARMLAND);

        basicItem(Spawning.ECHO_LANTERN.item().get());

        basicItem(Cloth.ITEM.get());

        basicItem(CoalFire.FIRESTARTER.get());
        blockItem(CoalFire.BURNT_LOG);

        basicItem(Berries.SWEET_BERRY_SEEDS.get());
        
        basicItemWithTexture(Minecarts.COPPER_POWERED_RAIL.item().get(), InsaneSO.location("block/copper_powered_rail"));
        withExistingParent("golden_powered_rail", ResourceLocation.parse("item/powered_rail"));

        basicItem(Pouch.ITEM.get());
    }

    private ItemModelBuilder blockItem(SimpleBlockWithItem block) {
        String id = block.block().getId().getPath();
        return withExistingParent(InsaneSO.MOD_ID + ":item/" + id, InsaneSO.MOD_ID + ":block/" + id);
    }

    public ItemModelBuilder basicItemWithTexture(Item item, ResourceLocation texture)
    {
        return basicItemWithTexture(Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(item)), texture);
    }

    public ItemModelBuilder basicItemWithTexture(ResourceLocation item, ResourceLocation texture)
    {
        return getBuilder(item.toString())
                .parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0", texture);
    }

    private ItemModelBuilder shield(Item item) {
        return shield(Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(item)));
    }

    private ItemModelBuilder shield(ResourceLocation item) {
        return getBuilder(item.toString())
                .parent(new ModelFile.UncheckedModelFile("shieldsplus:item/wooden_shield"))
                .override().predicate(ResourceLocation.parse("blocking"), 1)
                .model(new ModelFile.UncheckedModelFile("shieldsplus:item/wooden_shield_blocking"))
                .end();
    }

    private ItemModelBuilder handHeld(Item item) {
        return handHeld(Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(item)));
    }

    private ItemModelBuilder handHeld(ResourceLocation item) {
        return getBuilder(item.toString())
                .parent(new ModelFile.UncheckedModelFile("item/handheld"))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(item.getNamespace(), "item/" + item.getPath()));
    }

    // Shoutout to El_Redstoniano for making this
    private void trimmedArmorItem(DeferredHolder<Item, ArmorItem> itemRegistryObject) {
        if (itemRegistryObject.get() instanceof ArmorItem armorItem) {
            trimMaterials.forEach((trimMaterial, value) -> {
                float trimValue = value;

                String armorType = switch (armorItem.getEquipmentSlot()) {
                    case HEAD -> "helmet";
                    case CHEST -> "chestplate";
                    case LEGS -> "leggings";
                    case FEET -> "boots";
                    default -> "";
                };

                String armorItemPath = "item/" + itemRegistryObject.getId().getPath();
                String trimPath = "trims/items/" + armorType + "_trim_" + trimMaterial.location().getPath();
                String currentTrimName = armorItemPath + "_" + trimMaterial.location().getPath() + "_trim";
                ResourceLocation armorItemResLoc = ResourceLocation.fromNamespaceAndPath(itemRegistryObject.getId().getNamespace(), armorItemPath);
                ResourceLocation trimResLoc = ResourceLocation.parse(trimPath); // minecraft namespace
                ResourceLocation trimNameResLoc = InsaneSO.location(currentTrimName);

                // This is used for making the ExistingFileHelper acknowledge that this texture exist, so this will
                // avoid an IllegalArgumentException
                existingFileHelper.trackGenerated(trimResLoc, PackType.CLIENT_RESOURCES, ".png", "textures");

                // Trimmed armorItem files
                getBuilder(currentTrimName)
                        .parent(new ModelFile.UncheckedModelFile("item/generated"))
                        .texture("layer0", armorItemResLoc)
                        .texture("layer1", trimResLoc);

                // Non-trimmed armorItem file (normal variant)
                this.withExistingParent(itemRegistryObject.getId().toString(),
                                mcLoc("item/generated"))
                        .override()
                        .model(new ModelFile.UncheckedModelFile(trimNameResLoc))
                        .predicate(mcLoc("trim_type"), trimValue).end()
                        .texture("layer0", ResourceLocation.fromNamespaceAndPath(itemRegistryObject.getId().getNamespace(), "item/" + itemRegistryObject.getId().getPath()));
            });
        }
    }
}
