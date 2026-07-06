# Anvil Recipe: Gson → Codec refactor

Scoping notes for migrating `AnvilRecipe`/`IngredientWithCount` from hand-written
Gson `JsonDeserializer`s + hand-written `StreamCodec.composite` to Mojang `Codec`s.
Not started yet — this is the plan to pick up later.

## Why

- Currently every field is serialized twice by hand: once in a Gson `Serializer`
  (JSON) and once in a `StreamCodec.composite` (network). A `Codec` gives both
  for free via `ByteBufCodecs.fromCodecWithRegistries(...)`.
- `AnvilRecipe.result` currently uses `BuiltInRegistries.ITEM.get(ResourceLocation)`,
  which silently falls back to `Items.AIR` for unknown/missing items (e.g. when a
  soft-dependency mod like Shields+ isn't installed). This is what caused the
  "Empty ItemStack not allowed" network crash (see `ISOModules`/anvil recipe sync
  bug, fixed short-term with a manual `neoforge:conditions` check in
  `AnvilRecipeReloadListener.apply`). Vanilla's `ItemStack.CODEC` uses
  `ITEM_NON_AIR_CODEC`, which returns a proper `DataResult` error instead —
  the bad recipe would be rejected and logged instead of loaded as an empty stack.
- `neoforge:conditions` support becomes idiomatic (`ConditionalOps.createConditionalCodec`)
  instead of the manual `ICondition.conditionsMatched(JsonOps.INSTANCE, ...)` check
  currently bolted onto `AnvilRecipeReloadListener`.

## Files that need changes

1. **`IngredientWithCount.java`**

   **Decision: do not support the bare-string shorthand or an "either" codec.**
   Drop the dual-shape parsing entirely and always require the object shape,
   the same way the `result` field already does it (`{"id": "...", "count": N}`,
   count optional/defaults to 1). This makes it a plain fixed-shape
   `RecordCodecBuilder` — no `Codec.either`, no branching on
   `json.isJsonPrimitive()`. The `#tag` prefix handling in `fromString` can stay
   exactly as-is, just always read from the object's `"id"` string field instead
   of being reachable from a bare top-level string.

   **Data migration this implies:** every existing `anvil_recipe` JSON currently
   uses the bare-string shorthand for `left_ingredient` (all 35 files under
   `src/main/resources/integrated_packs/equipment_forging/data/insanesurvivaloverhaul/anvil_recipe/**`,
   e.g. `"left_ingredient": "minecraft:copper_sword"`). `right_ingredient` is
   already always written in object form in every existing file. So this
   refactor requires a one-line edit to each of those 35 JSONs, changing
   `"left_ingredient": "minecraft:x"` → `"left_ingredient": {"id": "minecraft:x"}`.
   No recipe currently uses the `#tag` prefix, so there's nothing to migrate there.

   Network side: `STREAM_CODEC` (currently `Ingredient.CONTENTS_STREAM_CODEC`
   + `ByteBufCodecs.VAR_INT`) can stay as-is, or be derived via
   `ByteBufCodecs.fromCodecWithRegistries` once the JSON codec exists.

2. **`AnvilRecipe.java`**
   - Replace `Serializer` (`JsonDeserializer<AnvilRecipe>`) with
     `Codec<AnvilRecipe> CODEC = RecordCodecBuilder.create(...)` grouping:
     `leftIngredient`, `rightIngredient` (both `IngredientWithCount.CODEC`),
     `keepDurability` (`Codec.BOOL.optionalFieldOf("keep_durability", false)`),
     `experience` (`Codec.DOUBLE.optionalFieldOf("experience", 0.0)`),
     `result` (`ItemStack.CODEC.fieldOf("result")` — drop the custom
     `jObjectResult`/`GsonHelper` parsing entirely, vanilla's shape
     `{"id","count","components"}` is a superset of what's used today).
   - Drop the hand-written `STREAM_CODEC` composite in favor of one Codec used
     for both JSON and network (see `ClientboundAnvilRecipeSyncPacket` below).

3. **`AnvilRecipeReloadListener.java`**
   - Replace `GSON.fromJson(entry.getValue(), AnvilRecipe.class)` with
     `AnvilRecipe.CODEC.parse(JsonOps.INSTANCE, entry.getValue()).getOrThrow(...)`
     (or `.resultOrPartial(errorMsg -> LOGGER.error(...))` to keep the existing
     "log and skip" behavior per-recipe).
   - Optionally replace the manual `ICondition.conditionsMatched(...)` check
     (added as the short-term fix) with
     `ConditionalOps.createConditionalCodec(AnvilRecipe.CODEC)` wrapping, matching
     how vanilla `RecipeManager` does it (`Recipe.CONDITIONAL_CODEC`).

4. **`ClientboundAnvilRecipeSyncPacket.java`**
   - Replace the `StreamCodec.composite(AnvilRecipe.STREAM_CODEC.apply(ByteBufCodecs.list()), ...)`
     with `ByteBufCodecs.fromCodecWithRegistries(AnvilRecipe.CODEC.listOf())`
     (confirmed available: `net.minecraft.network.codec.ByteBufCodecs.fromCodecWithRegistries(Codec<T>)`
     → `StreamCodec<RegistryFriendlyByteBuf, T>`, in NeoForge 21.1.234 sources).

## Files that need zero changes

Confirmed via `AnvilRecipe`/`IngredientWithCount` usage search — these only touch
public fields/records, never the serializer or stream codec directly, so they're
unaffected as long as field names/types stay the same:

- `AnvilCrafting.java` (reads `.leftIngredient`, `.rightIngredient`, `.result`,
  `.keepDurability`, `.experience`)
- `module/mining/anvilcrafting/emi/EmiIsoAnvilRecipe.java` (constructor takes `AnvilRecipe`)
- `emi/ISOEmiPlugin.java`
- `setup/NetworkHandler.java` (registers `ClientboundAnvilRecipeSyncPacket.TYPE`/`STREAM_CODEC`
  by reference, doesn't care about internals)
- `InsaneSO.java` (only references `AnvilRecipeReloadListener.INSTANCE`)

## Estimate

~50-70 lines touched across the 4 files above, plus a one-line edit to each of
the 35 existing `anvil_recipe` JSONs to move `left_ingredient` from bare-string
shorthand to object form (see `IngredientWithCount` section above). With the
dual-shape codec dropped, this is now a mechanical port end to end — no
alternative/either codec needed anywhere. Test by reloading all existing
`anvil_recipe` JSONs
(`src/main/resources/integrated_packs/**/anvil_recipe/**/*.json`) after the
change and confirming the same recipe count logs as before.

## Reference

NeoForge/MC source jars for looking up exact Codec/StreamCodec APIs (see
memory `reference_source_jars.md`):
- NeoForge: `C:/Users/delvi/.gradle/caches/modules-2/files-2.1/net.neoforged/neoforge/21.1.234/4e270b39970b765d1c76505f9c305d43d035f635/neoforge-21.1.234-sources.jar`
- Vanilla (decompiled via moddev): `C:/Users/delvi/source/repos/Insane96/ISO/build/moddev/artifacts/neoforge-21.1.234-sources.jar`
