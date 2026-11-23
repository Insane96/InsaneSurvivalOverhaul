package insane96mcp.insanesurvivaloverhaul.module.mining.reversiblestonecutting;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.setup.ILTags;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.data.generator.ISOItemTagsProvider;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@LoadFeature(module = ISOModules.MINING, description = "Makes the Stonecutter reversible between full blocks of the same family: if the Stonecutter turns A into B and A into C (1 to 1), B and C can be cut back into A and into each other. Recipes added by data packs and other mods count too. Blocks in the `insanesurvivaloverhaul:stonecutter_reverse_blacklist` and `insanelib:recipe_removal` item tags are ignored. Changes apply on the next /reload. `/insanesurvivaloverhaul stonecutter_families dump` writes every family found to `logs/insanesurvivaloverhaul/`.")
public class ReversibleStonecutting extends Feature {
	public static final TagKey<Item> BLACKLIST = ISOItemTagsProvider.create("stonecutter_reverse_blacklist");
	public static final String RECIPE_PREFIX = "stonecutting_reverse/";

	@Config(name = "Log only", description = "If true, no recipe is added: every family found is written to the log with its blocks instead. Useful to check the families before actually adding the recipes.")
	public static Boolean logOnly = false;
	@Config(name = "Blacklisted keywords", description = "Blocks whose id path contains one of these words are ignored, like the ones in the `insanesurvivaloverhaul:stonecutter_reverse_blacklist` tag. Catches modded cracked and smooth blocks, that would let players skip smelting.")
	public static List<String> blacklistedKeywords = List.of("cracked", "smooth");
	@Config(min = 2, name = "Large family warning", description = "Families with more blocks than this are reported in the log as suspicious: a single 1 to 1 recipe from a mod can merge two unrelated families.")
	public static Integer largeFamilyWarning = 15;

	/**
	 * First resource load: when the first {@link TagsUpdatedEvent} fires the server doesn't exist yet, while here
	 * recipes, tags and the server config are all loaded.
	 * Runs with low priority, after InsaneLib's recipe removal.
	 */
	@SubscribeEvent(priority = EventPriority.LOW)
	public void onServerAboutToStart(ServerAboutToStartEvent event) {
		if (!this.isEnabled())
			return;
		MinecraftServer server = event.getServer();
		addReverseRecipes(server.getRecipeManager(), server.registryAccess());
	}

	/**
	 * Every following reload (e.g. /reload). Fires before the recipes are sent to the players.
	 */
	@SubscribeEvent(priority = EventPriority.LOW)
	public void onTagsUpdated(TagsUpdatedEvent event) {
		if (!this.isEnabled()
				|| event.getUpdateCause() != TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD)
			return;
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if (server == null)
			return;
		addReverseRecipes(server.getRecipeManager(), event.getRegistryAccess());
	}

	private static void addReverseRecipes(RecipeManager recipeManager, HolderLookup.Provider registries) {
		List<List<Item>> families = findFamilies(recipeManager, registries);
		int blocks = families.stream().mapToInt(List::size).sum();
		for (List<Item> family : families) {
			if (logOnly)
				InsaneSO.LOGGER.info("Reversible Stonecutting: family of {} blocks: {}", family.size(), joinIds(family));
			else if (family.size() > largeFamilyWarning)
				InsaneSO.LOGGER.warn("Reversible Stonecutting: suspiciously large family of {} blocks, check for recipes bridging unrelated families: {}", family.size(), joinIds(family));
		}
		if (logOnly) {
			InsaneSO.LOGGER.info("Reversible Stonecutting: found {} families ({} blocks), no recipe added (Log only is enabled)", families.size(), blocks);
			return;
		}

		Set<ItemPair> existing = existingStonecuttingPairs(recipeManager, registries);
		List<RecipeHolder<?>> recipes = new ArrayList<>(recipeManager.getRecipes());
		int added = 0;
		for (List<Item> family : families) {
			for (Item from : family) {
				for (Item to : family) {
					if (from == to || existing.contains(new ItemPair(from, to)))
						continue;
					ResourceLocation fromId = BuiltInRegistries.ITEM.getKey(from);
					ResourceLocation toId = BuiltInRegistries.ITEM.getKey(to);
					ResourceLocation id = InsaneSO.id(RECIPE_PREFIX + fromId.getNamespace() + "_" + fromId.getPath() + "_to_" + toId.getNamespace() + "_" + toId.getPath());
					recipes.add(new RecipeHolder<>(id, new StonecutterRecipe("", Ingredient.of(from), new ItemStack(to))));
					added++;
				}
			}
		}
		if (added > 0)
			recipeManager.replaceRecipes(recipes);
		InsaneSO.LOGGER.info("Reversible Stonecutting: added {} stonecutting recipes across {} families ({} blocks)", added, families.size(), blocks);
	}

	/**
	 * Groups the full blocks connected by 1 to 1 stonecutting recipes (in either direction). Families and the blocks
	 * in them are sorted by id. Only families of at least 2 blocks are returned.
	 */
	public static List<List<Item>> findFamilies(RecipeManager recipeManager, HolderLookup.Provider registries) {
		Map<Item, Boolean> nodeCache = new HashMap<>();
		Map<Item, Item> parents = new HashMap<>();
		for (RecipeHolder<StonecutterRecipe> holder : recipeManager.getAllRecipesFor(RecipeType.STONECUTTING)) {
			ItemStack result = holder.value().getResultItem(registries);
			if (result.getCount() != 1
					|| !result.getComponentsPatch().isEmpty()
					|| !nodeCache.computeIfAbsent(result.getItem(), ReversibleStonecutting::isValidNode))
				continue;
			for (ItemStack stack : holder.value().getIngredients().getFirst().getItems()) {
				if (stack.getItem() != result.getItem()
						&& nodeCache.computeIfAbsent(stack.getItem(), ReversibleStonecutting::isValidNode))
					union(parents, stack.getItem(), result.getItem());
			}
		}

		Map<Item, List<Item>> byRoot = new HashMap<>();
		for (Item item : parents.keySet())
			byRoot.computeIfAbsent(find(parents, item), k -> new ArrayList<>()).add(item);
		Comparator<Item> byId = Comparator.comparing(item -> BuiltInRegistries.ITEM.getKey(item).toString());
		List<List<Item>> families = new ArrayList<>();
		for (List<Item> family : byRoot.values()) {
			if (family.size() < 2)
				continue;
			family.sort(byId);
			families.add(family);
		}
		families.sort(Comparator.comparing(family -> BuiltInRegistries.ITEM.getKey(family.getFirst()).toString()));
		return families;
	}

	/**
	 * Returns true if the item is a full block that's not blacklisted (tag, recipe removal or keyword)
	 */
	private static boolean isValidNode(Item item) {
		if (!(item instanceof BlockItem blockItem))
			return false;
		if (item.builtInRegistryHolder().is(BLACKLIST)
				|| item.builtInRegistryHolder().is(ILTags.Items.RECIPE_REMOVAL))
			return false;
		String path = BuiltInRegistries.ITEM.getKey(item).getPath();
		for (String keyword : blacklistedKeywords) {
			if (!keyword.isBlank() && path.contains(keyword))
				return false;
		}
		Block block = blockItem.getBlock();
		try {
			return Block.isShapeFullBlock(block.defaultBlockState().getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO));
		}
		catch (Exception e) {
			//Some modded blocks might not like being queried without a real level
			return false;
		}
	}

	/**
	 * Every (ingredient item, result item) pair of the existing stonecutting recipes, regardless of the count
	 */
	private static Set<ItemPair> existingStonecuttingPairs(RecipeManager recipeManager, HolderLookup.Provider registries) {
		Set<ItemPair> pairs = new HashSet<>();
		for (RecipeHolder<StonecutterRecipe> holder : recipeManager.getAllRecipesFor(RecipeType.STONECUTTING)) {
			Item result = holder.value().getResultItem(registries).getItem();
			for (ItemStack stack : holder.value().getIngredients().getFirst().getItems())
				pairs.add(new ItemPair(stack.getItem(), result));
		}
		return pairs;
	}

	private static Item find(Map<Item, Item> parents, Item item) {
		Item root = item;
		while (true) {
			Item parent = parents.computeIfAbsent(root, k -> k);
			if (parent == root)
				break;
			root = parent;
		}
		//Path compression
		while (item != root) {
			Item next = parents.get(item);
			parents.put(item, root);
			item = next;
		}
		return root;
	}

	private static void union(Map<Item, Item> parents, Item a, Item b) {
		Item rootA = find(parents, a);
		Item rootB = find(parents, b);
		if (rootA != rootB)
			parents.put(rootA, rootB);
	}

	private static String joinIds(List<Item> items) {
		StringJoiner joiner = new StringJoiner(", ");
		for (Item item : items)
			joiner.add(BuiltInRegistries.ITEM.getKey(item).toString());
		return joiner.toString();
	}

	/**
	 * {@code /insanesurvivaloverhaul stonecutter_families dump}: writes every family found in the current recipes
	 * (with the current blacklists) to {@code logs/insanesurvivaloverhaul/stonecutter_families_<date-time>.txt}.
	 * Works even if the feature is disabled. The reverse recipes already added don't change the families.
	 */
	public static int dump(CommandSourceStack source) {
		MinecraftServer server = source.getServer();
		List<List<Item>> families = findFamilies(server.getRecipeManager(), server.registryAccess());
		StringBuilder sb = new StringBuilder();
		for (List<Item> family : families) {
			sb.append(family.size()).append(" blocks");
			if (family.size() > largeFamilyWarning)
				sb.append(" (suspiciously large)");
			sb.append(": ").append(joinIds(family)).append(System.lineSeparator());
		}
		Path file = FMLPaths.GAMEDIR.get().resolve("logs").resolve(InsaneSO.MOD_ID).resolve("stonecutter_families_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss")) + ".txt");
		try {
			Files.createDirectories(file.getParent());
			Files.writeString(file, sb.toString());
		}
		catch (IOException e) {
			InsaneSO.LOGGER.error("Failed to write the stonecutter families dump", e);
			source.sendFailure(Component.literal("Failed to write the stonecutter families dump: " + e.getMessage()));
			return 0;
		}
		String path = file.toAbsolutePath().toString();
		source.sendSuccess(() -> Component.literal("Dumped %d stonecutter families to ".formatted(families.size()))
				.append(Component.literal(path).withStyle(style -> style.withUnderlined(true).withColor(ChatFormatting.AQUA).withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, path)))), false);
		return families.size();
	}

	private record ItemPair(Item from, Item to) {}
}
