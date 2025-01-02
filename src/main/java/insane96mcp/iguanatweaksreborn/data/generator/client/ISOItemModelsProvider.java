package insane96mcp.iguanatweaksreborn.data.generator.client;

import insane96mcp.iguanatweaksreborn.InsaneSurvivalOverhaul;
import insane96mcp.iguanatweaksreborn.module.combat.UnfairOneShot;
import insane96mcp.iguanatweaksreborn.module.farming.crops.Crops;
import insane96mcp.iguanatweaksreborn.module.hungerhealth.fooddrinks.FoodDrinks;
import insane96mcp.iguanatweaksreborn.module.items.flintexpansion.FlintExpansion;
import insane96mcp.iguanatweaksreborn.module.movement.minecarts.Minecarts;
import insane96mcp.iguanatweaksreborn.module.sleeprespawn.Cloth;
import insane96mcp.iguanatweaksreborn.module.world.Berries;
import insane96mcp.iguanatweaksreborn.module.world.BiomeCompass;
import insane96mcp.iguanatweaksreborn.module.world.CyanFlower;
import insane96mcp.iguanatweaksreborn.module.world.coalfire.CoalFire;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;

public class ISOItemModelsProvider extends ItemModelProvider {
    public ISOItemModelsProvider(PackOutput output, String modid, ExistingFileHelper existingFileHelper) {
        super(output, modid, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(Crops.CARROT_SEEDS.get());
        basicItem(Crops.ROOTED_POTATO.get());
        basicItem(Crops.ROOTED_ONION.get());
        basicItem(Crops.RICE_SEEDS.get());

        basicItem(Berries.SWEET_BERRY_SEEDS.get());

        basicItemWithTexture(CyanFlower.FLOWER.item().get(), new ResourceLocation(InsaneSurvivalOverhaul.MOD_ID, "block/cyan_flower"));
        basicItemWithTexture(Crops.SOLANUM_NEOROSSII.item().get(), new ResourceLocation(InsaneSurvivalOverhaul.MOD_ID, "block/solanum_neorossii"));

        basicItem(BiomeCompass.COMPASS.get());
        basicItem(UnfairOneShot.HALF_HEART_TEXTURE.get());

        basicItem(FoodDrinks.OVER_EASY_EGG.get());
        basicItem(FoodDrinks.BROWN_MUSHROOM_STEW.get());
        basicItem(FoodDrinks.RED_MUSHROOM_STEW.get());
        basicItem(FoodDrinks.NETHERIZED_STEW.get());
        basicItem(FoodDrinks.PUMPKIN_PULP.get());

        basicItemWithTexture(Minecarts.COPPER_POWERED_RAIL.item().get(), new ResourceLocation(InsaneSurvivalOverhaul.MOD_ID, "block/copper_powered_rail"));
        withExistingParent("golden_powered_rail", new ResourceLocation("item/powered_rail"));

        basicItem(Cloth.CLOTH.get());

        handHeld(FlintExpansion.AXE.get());
        handHeld(FlintExpansion.PICKAXE.get());
        handHeld(FlintExpansion.SHOVEL.get());
        handHeld(FlintExpansion.HOE.get());
        handHeld(FlintExpansion.SWORD.get());
        //shield(FlintExpansion.SHIELD.get());

        withExistingParent("respawn_obelisk", new ResourceLocation(InsaneSurvivalOverhaul.MOD_ID, "block/respawn_obelisk_disabled"));

        withExistingParent("soul_sand_hellish_coal_ore", new ResourceLocation(InsaneSurvivalOverhaul.MOD_ID, "block/soul_sand_hellish_coal_ore"));
        withExistingParent("soul_soil_hellish_coal_ore", new ResourceLocation(InsaneSurvivalOverhaul.MOD_ID, "block/soul_soil_hellish_coal_ore"));

        withExistingParent("charcoal_layer", new ResourceLocation(InsaneSurvivalOverhaul.MOD_ID, "block/charcoal_layer/height_2"));
        basicItem(CoalFire.FIRESTARTER.get());
        basicItem(CoalFire.HELLISH_COAL.get());
    }

    public ItemModelBuilder basicItemWithTexture(Item item, ResourceLocation texture)
    {
        return basicItemWithTexture(Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(item)), texture);
    }

    public ItemModelBuilder basicItemWithTexture(ResourceLocation item, ResourceLocation texture)
    {
        return getBuilder(item.toString())
                .parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0", texture);
    }

    private ItemModelBuilder shield(Item item) {
        return shield(Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(item)));
    }

    private ItemModelBuilder shield(ResourceLocation item) {
        return getBuilder(item.toString())
                .parent(new ModelFile.UncheckedModelFile("shieldsplus:item/wooden_shield"))
                .override().predicate(new ResourceLocation("blocking"), 1)
                .model(new ModelFile.UncheckedModelFile("shieldsplus:item/wooden_shield_blocking"))
                .end();
    }

    private ItemModelBuilder handHeld(Item item) {
        return handHeld(Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(item)));
    }

    private ItemModelBuilder handHeld(ResourceLocation item) {
        return getBuilder(item.toString())
                .parent(new ModelFile.UncheckedModelFile("item/handheld"))
                .texture("layer0", new ResourceLocation(item.getNamespace(), "item/" + item.getPath()));
    }
}
