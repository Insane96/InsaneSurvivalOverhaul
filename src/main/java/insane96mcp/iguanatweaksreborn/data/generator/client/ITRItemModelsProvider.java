package insane96mcp.iguanatweaksreborn.data.generator.client;

import insane96mcp.iguanatweaksreborn.IguanaTweaksReborn;
import insane96mcp.iguanatweaksreborn.module.combat.UnfairOneShot;
import insane96mcp.iguanatweaksreborn.module.farming.crops.Crops;
import insane96mcp.iguanatweaksreborn.module.hungerhealth.fooddrinks.FoodDrinks;
import insane96mcp.iguanatweaksreborn.module.movement.minecarts.Minecarts;
import insane96mcp.iguanatweaksreborn.module.sleeprespawn.Cloth;
import insane96mcp.iguanatweaksreborn.module.world.Berries;
import insane96mcp.iguanatweaksreborn.module.world.BiomeCompass;
import insane96mcp.iguanatweaksreborn.module.world.CyanFlower;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;

public class ITRItemModelsProvider extends ItemModelProvider {
    public ITRItemModelsProvider(PackOutput output, String modid, ExistingFileHelper existingFileHelper) {
        super(output, modid, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(Crops.CARROT_SEEDS.get());
        basicItem(Crops.ROOTED_POTATO.get());
        basicItem(Crops.ROOTED_ONION.get());
        basicItem(Crops.RICE_SEEDS.get());

        basicItem(Berries.SWEET_BERRY_SEEDS.get());

        basicItemWithTexture(CyanFlower.FLOWER.item().get(), new ResourceLocation(IguanaTweaksReborn.MOD_ID, "block/cyan_flower"));
        basicItemWithTexture(Crops.SOLANUM_NEOROSSII.item().get(), new ResourceLocation(IguanaTweaksReborn.MOD_ID, "block/solanum_neorossii"));

        basicItem(BiomeCompass.COMPASS.get());
        basicItem(UnfairOneShot.HALF_HEART_TEXTURE.get());

        basicItem(FoodDrinks.OVER_EASY_EGG.get());
        basicItem(FoodDrinks.BROWN_MUSHROOM_STEW.get());
        basicItem(FoodDrinks.RED_MUSHROOM_STEW.get());
        basicItem(FoodDrinks.NETHERIZED_STEW.get());
        basicItem(FoodDrinks.PUMPKIN_PULP.get());

        basicItemWithTexture(Minecarts.COPPER_POWERED_RAIL.item().get(), new ResourceLocation(IguanaTweaksReborn.MOD_ID, "block/copper_powered_rail"));
        withExistingParent("golden_powered_rail", new ResourceLocation("item/powered_rail"));

        basicItem(Cloth.CLOTH.get());

        withExistingParent("respawn_obelisk", new ResourceLocation(IguanaTweaksReborn.MOD_ID, "block/respawn_obelisk_disabled"));
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
}
