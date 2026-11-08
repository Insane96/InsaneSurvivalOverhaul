# Reintegrating villagers into ISO: analysis

Source: the *Matcha Flavoured DP* 1.12.9 datapack for Minecraft 26.3 (`MFDP_1_12_9_26_3.zip`), compared with ISO 1.21.1 and with the old IguanaTweaksReborn 1.20.1 (ITR) system.

This document is analysis only: no code files were changed and nothing was tested in game.

## 1. Summary

- **The datapack cannot be imported.** It uses the `villager_trade` and `trade_set` registries and the `item_model` component, which do not exist in 1.21.1. The trades must be rewritten in ISO's format, taking the idea and not the files.
- **The idea to bring over:** each profession becomes a themed supplier of materials, with progression by level, but with modest quantities and limited uses.
- **The infrastructure already exists in ITR** (`Villagers`, `VillagerTradesReloadListener`, `SerializableTrade`, `WanderingTrades`) and must be ported to 1.21.1.
- **Today a villager cannot be obtained in ISO.** There are three obstacles, all with a decided solution:
  1. `Spawning.replaceZombieVillagersWithNormalZombies` turns every zombie villager into a zombie: the option must be removed.
  2. The golden apple is removed from the game: the cure will use the golden carrot.
  3. Breeding is active and must be turned off with a mixin, because no usable event exists for villagers.
- **Igloos stay vanilla:** the villager and zombie villager in the basement are an accepted exception to the "cure only" rule.
- **Prices:** the cost of 1 emerald can be kept, by reducing the quantity received and abandoning the pack's 999 uses.
- **Trades per level:** they stay at 2 random ones as in vanilla, so no mixin is needed.
- **No infinite emeralds from illagers:** villages and outposts stay off and pillagers lose the emerald drop ISO adds, because patrols are infinite. Raids are still to be decided (§8).
- **Mansions can come back:** vindicators and evokers do not respawn, so they are a finite source.
- **New emerald source:** fishing, as a rare treasure.

## 2. Decided constraints

| Constraint | Consequence |
|---|---|
| The idea is brought over, not the datapack | Trades are rewritten in ISO's format, without importing the files. |
| Villages and outposts do not generate | `villages.json` and `pillager_outposts.json` stay in the `disable_long_noses` pack. The datapack's `village_beta` structure, "haunted" atmosphere and related events are excluded. |
| Mansions can exist | `woodland_mansions.json` must be removed from `disable_long_noses` and the option's description updated (§8). |
| Pillagers must not give emeralds endlessly | Remove the emeralds from `mob_loot_changes/.../entities/pillager.json` (§8). |
| Emeralds can be fished, rarely | To be added to fishing: ISO does not currently provide for it (§8). |
| Villagers obtainable only by curing zombies | *Asylum Seekers* (villager eggs sold by the trader) are excluded. The only accepted exception: igloos (§4.4). |
| `replaceZombieVillagersWithNormalZombies` removed | The option and its code in `Spawning.java` must be deleted. |
| Breeding disabled | Mixin on `Villager.canBreed` (§4.3). |
| Currency: emeralds | No conversion: in the datapack the "Obol" is already `minecraft:emerald` renamed by the resource pack. |
| Cure with golden carrot | Item replaced in `ZombieVillager.mobInteract` (§4.2). |
| 2 random trades per level | Vanilla behaviour of `Villager.updateTrades`, no change (§6). |
| Price of 1 emerald, reduced quantities, no 999 uses | Price model described in §8. |

## 3. Current state in ISO

| Element | Where | State |
|---|---|---|
| Villages, outposts and mansions off | `integrated_packs/disable_long_noses`, config `Packs.disableLongNosesStructures` | Active by default: keep for villages and outposts, **remove for mansions** |
| Pillagers with 1-2 emeralds | `mob_loot_changes/data/minecraft/loot_table/entities/pillager.json` (added by ISO: they drop none in vanilla) | **To be removed** |
| Zombie villagers replaced by zombies | `module/mobs/spawning/Spawning.java:81` | Active by default, **to be removed** |
| Golden apple removed | `food_changes`: recipe with `neoforge:false` condition, loot replaced with 4 gold ingots, removed from creative | Stays this way: the cure moves to the golden carrot |
| Farmer and ISO seeds | `Crops.fixVillagers`, `VillagerAccessor` | Ready |
| Growth and breeding data | `livestock_data/villager.json` | Growth is still needed for cured babies; the breeding entries become useless |
| Zombie villager loot | `crops/.../replace_*_from_zombie_villager.json` | Ready |
| Villager passive regeneration | `MiscMobs.PASSIVE_REGEN` tag | Ready |
| Fletching table | `FletchingTableBlockMixin_Fletching` keeps the vanilla block | Remains a valid job site |
| Custom trades | None | To be ported from ITR |
| Wandering trader | No changes: vanilla spawn and trades | To be decided |

`Spawning.java` reacts to `EntityJoinLevelEvent` for every zombie villager, so it also hits those created from a killed villager and those reloaded from disk. This is why the option is removed entirely: that way not even the `true` value saved in existing `server.toml` files can keep it active.

## 4. The path to obtaining a villager

### 4.1 Where zombie villagers come from

- Natural spawn with weight 5 against 95 for zombies, and groups of 1 against 4: roughly 1 in 75 zombies, if ISO does not alter biome spawns.
- Conversion of a villager killed by a zombie: never on Easy, 50% on Normal, always on Hard (`Zombie.killedEntity`). Keeps profession, level, XP and trades.
- They burn during the day and despawn at a distance, except while being cured or if they have XP (`removeWhenFarAway`). The player must therefore capture them or cure them on the spot.
- The initial profession is random among the 15 in the registry (`ZombieVillager:75`). In practice it matters little: a cured villager with 0 XP and no job site goes back to unemployed (`ResetProfession`) and the player directs it with the job block.
- **Exception:** nitwits stay nitwits (`ResetProfession` excludes them). They are about 1 in 15, and in a world without breeding they are a wasted cure.

### 4.2 The cure

It requires Weakness on the zombie and the cure item (`ZombieVillager.mobInteract:158`); the duration is 3600-6000 ticks, sped up by beds and iron bars.

- **Cure item (decided): golden carrot.** The check is `itemstack.is(Items.GOLDEN_APPLE)`. It is replaced with a mixin, preferably pointing to an item tag with the golden carrot as default value, so it stays modifiable via datapack. The method runs on both sides, so the tag works fine.
- **Golden carrot recipe:** in ISO the item only has a component override, but it must be verified that the recipe has not been touched elsewhere.
- **Weakness:** in vanilla a fermented spider eye on water is enough, plus gunpowder for the splash potion. I found no changes in ISO. The real gate is the brewing stand, which requires a blaze rod, and therefore the Nether.
- **Cure discount:** the cure gives the curer 125 reputation (`MAJOR_POSITIVE` 20×5 + `MINOR_POSITIVE` 25). The discount is `reputation × priceMultiplier`. It has no effect on 1-emerald trades, because the price does not go below 1; it does matter on trades where the villager buys, where it reduces the required quantity (§8).

### 4.3 Disabling breeding

- **Recommended point:** `Villager.canBreed()` returning `false`. It stops both the partner search (`VillagerGoalPackages:165`) and `VillagerMakeLove:90`.
- `BabyEntitySpawnEvent` **is not usable**: `VillagerMakeLove` does not fire it.
- `getBreedOffspring` returning `null` would leave the animation and the food consumption in place.
- `Villager` does not extend `Animal`, so ISO's `Animal`-based breeding mechanics should not come into play.

### 4.4 Other villager sources

- **Igloos (decided: they stay vanilla):** `igloo/bottom.nbt` contains a villager and a zombie villager, and ISO does not touch igloos. It is an accepted exception that requires no changes.
- Creative spawn eggs and `/summon` are not a problem.

## 5. What the datapack does with villagers

| Mechanic | How it works | Proposal |
|---|---|---|
| Trades per profession | 291 files; each level almost always shows the entire pool | **To bring over as an idea**, with 2 random trades per level |
| "Filler" trades | Black wool → emerald with `discard` modifier: they generate no trade, they only keep a level from being empty | Not needed |
| No exhaustion | 272 trades out of 291 have 999 uses; `reputation_discount` 0 on 205 | **Rejected:** limited uses and restocking (§8) |
| *Local Flavours* | Every employed villager (except fishermen) with XP > 0 receives one trade, once: 1 random food out of 42 → 1 emerald | Optional, nice with ISO's foods |
| *Amnestic* | Craftable item (azure bluet, sugar, glowstone dust, paper): returns the villager to unemployed, level 1, 0 XP | Recommended: with rare villagers and random trades it allows fixing a mistake or rerolling |
| *Beacon Kindling* | Campfire with hay and emerald: after 10 minutes it summons an invulnerable trader that stays 5 minutes; 15-minute cooldown | Optional, gives the trader a clear purpose |
| Outposts without respawn | The pack generates outposts but with empty `spawn_overrides`, so pillagers do not reappear | Not needed: in ISO outposts stay off. Should they ever be wanted, this change would be enough |
| Pillagers and raids | Does not give emeralds back to pillagers; evokers with 0-1 emerald only if killed by a player. Does not disable patrols or raids | See §8 |
| Emeralds from fishing | In the fishing treasure, weight 72, 1-2 emeralds | **To bring over**, with a much lower chance (§8) |
| Wandering trader | Natural spawn not disabled; changed trades (discs, maps, eggs) and summoning through Beacon Kindling | To be decided (§10) |
| *Asylum Seeker* | The trader sells villager eggs | **Excluded by constraint** |
| Village maps | The trader sells maps of the 5 villages | **Excluded by constraint** |
| Villager schedule | 72000-tick timeline | Not applicable: tied to the pack's triple-length day, and in ISO `DayNightCycle` does not change the cycle |
| *Warding Stone* | Regenerates allies and damages enemies within 24 blocks | Out of scope, ISO already has the Echo Lantern |
| Hero of the Village gifts | Custom loot tables | Low priority: they depend on raids |

## 6. Trades per profession

In parentheses, the datapack's pool size for levels 1-5, excluding levels containing only filler. The "In the datapack" column describes the original: in the ISO version the reduced quantities and limited uses of §8 always apply.

With 2 random trades per level, ISO's pool must be built accordingly:

- A level with only 1 trade offers 1, with 2 it always offers both. At least 3 trades are needed for any randomness.
- A level can stay empty without problems: fillers are not needed.
- Important trades must go in small pools, otherwise a villager may never offer them.

| Profession | In the datapack | Proposal for ISO |
|---|---|---|
| **Armorer** (5/3/4/3/1) | Bulk redstone components | Keep the theme. Check consistency with `actual_redstone_components`. The `lava_kit` (bundle) must be replaced. |
| **Butcher** (2/2/2/1) | The pack's foods and recipes, knife | Replace with ISO foods (`apple_pie`, `cod_chowder`, `fungi_stew`, `over_easy_egg`, `pufferfish_chowder`). |
| **Cartographer** (5/5/6/2/4) | Structure maps and pottery sherds | Keep, **removing village maps** (the mansion becomes valid again). Maps are generated with ITR's `explorationMap` logic. |
| **Cleric** (7) | Splash potions in stacks of 16 | Keep the theme, with few potions per trade. Only effects that exist in 1.21.1. |
| **Farmer** (5/3/6) | Bundles of seeds, bone meal, animal eggs | ISO seeds (`carrot_seeds`, `rooted_potato`, `sweet_berry_seeds`) instead of bundles. Spawn eggs must be weighed against `livestock`, bone meal against the `bone_meal` pack. |
| **Fisherman** (1/16/10/10/11) | Buys fish for emeralds, Almanac | **Main source of emeralds.** ISO only has vanilla fish, so this remains a small pool to redesign. |
| **Fletcher** (3/1/1) | Arrows and composite bow | ISO arrows (`quartz_arrow`, `diamond_arrow`, `torch_arrow`, `ice_arrow`, `explosive_arrow`) and `shortbow`. |
| **Leatherworker** (3/6/4/1) | Bulk logs, often with a sapling | Keep (1.21.1 species, no poplar). Check against `EcologicWood` and `craftable_saplings`. |
| **Librarian** (3/3/1/1/1) | Rare texts → hearts and enchantments | Depends on the pack's items: to be redesigned. Idea: reduced vanilla and `crystal_heart` at a high level. Consider Ender pearls in stacks of 16. |
| **Mason** (5/6/4/3) | The pack's "bulk" blocks | Vanilla blocks in modest quantities instead of bulk items. |
| **Shepherd** (16/4/1) | Coloured wool in stacks of 64, shears, sheep eggs | Keep the theme, with a restricted or cumulative wool pool: with 16 colours and 2 random trades, finding the desired one is unlikely. Check against `cloth`. |
| **Toolsmith** (4/1/1/2/4) | Jewellery and gem sales | Discard jewellery. It can become a buyer of non-renewable resources (§8). |
| **Weaponsmith** (4/49) | Recycling equipment into the pack's materials, gems → emeralds | The 49 trades are based on non-existent materials. Keep the *diamonds → emeralds* idea, extended to other non-renewable resources (§8); recycling overlaps with the `recycling` pack. |
| **Wandering Trader** | Buys discs for 1-8 emeralds, maps, eggs | Discs yes, as a source of emeralds (without `tears` and `lava_chicken`, absent in 1.21.1). Maps of active structures. ITR's pool is a ready alternative. |

Also not directly transferable: the bundle (behind a feature flag in 1.21.1: ISO has the `pouch`), the newer flowers and species, and all `poisonous_potato` items with `item_model`, i.e. the pack's custom items.

## 7. Technical differences with 1.21.1

| Aspect | 26.3 (datapack) | 1.21.1 (ISO) |
|---|---|---|
| Trade definition | `villager_trade` + `trade_set` registries | `VillagerTrades.ItemListing` via `VillagerTradesEvent` and `WandererTradesEvent` |
| Number of trades per level | `amount` in the `trade_set` | **Fixed at 2** in `Villager.updateTrades` (kept) |
| Per-trade discount | `reputation_discount` | `priceMultiplier` in `MerchantOffer` |
| Trader | Three `trade_set`s | 5 generic + 1 rare, fixed in `WanderingTrader.updateTrades` |
| Trader conditions | `merchant_predicate` (used by 15 trades, e.g. biome) | To be implemented in `getOffer`, if needed |
| Custom costs | `item_model` components | `ItemCost` with `DataComponentPredicate` |

Notes:

- `VillagerTradingManager` fires the events on `TagsUpdatedEvent` (`SERVER_DATA_LOAD`) starting again from a vanilla copy: ITR's model with a reload listener and high/low priority keeps working and does not accumulate trades on each `/reload`.
- Trades are saved in the villager's NBT: changing the JSONs does not update those already obtained, except for future levels.
- For the cost, ITR's mixin acted on `getCostA`. In 1.21.1 the stack limit is in `MerchantOffer.getModifiedCostCount:119`.
- The quantities received must be compared with the stack sizes reduced by the `item_stacks` pack.

## 8. Economy and balance

In the datapack the emerald is a currency with a controlled supply:

- 86 emeralds in total from 37 advancements, plus chests and archaeology.
- Sale of fish (up to 1 rare fish per emerald), gems, discs.
- Diamonds and lapis lazuli blocks to the weaponsmith.

Importing "1 emerald → 64 items, 999 uses" would wipe out the gathering of many materials as soon as a few emeralds are found. Any infinite source of emeralds must also be avoided.

### Emerald sources

| Source | Renewable? | Proposal |
|---|---|---|
| Ore (vanilla, mountains only; `ore_generation` does not touch it) | No | Stays |
| Pillagers, currently 1-2 emeralds (added by ISO) | **Yes**, patrols reappear without limit | **Remove the drop** |
| Vindicators and evokers in mansions, 0-1 emerald | No | Stay: they generate once with the structure |
| Vindicators and evokers in raids, 0-1 emerald | **Yes**, if raids are repeated | To be decided |
| Fishing | Yes, but slow | **To be added**, rare |
| Villager buying fish | Yes, limited by uses and restocks | Stays |
| Villager buying non-renewable resources (diamonds etc.) | No | **To be added** |

**Mansions.** In vanilla `mansion.json` has empty `spawn_overrides` and no rule makes illagers reappear; the only structure with pillager respawn is the outpost. Mansion mobs are placed at generation, and the chests contain no emeralds. So they can come back, by removing `woodland_mansions.json` from `disable_long_noses`. This only applies to chunks not yet generated.

**Pillagers.** Patrols come from `PatrolSpawner` independently of outposts: turning outposts off is not enough, the drop must be removed. `PatrolSpawner` generates only pillagers, so without that drop patrols no longer give emeralds.

**Raids.** Patrols have captains that give the ominous bottle, so raids remain repeatable and their vindicators and evokers give emeralds. The datapack changes nothing here. The options:

- Recommended: no emeralds from raiders with an active raid (`Raider.getCurrentRaid`, via `LivingDropsEvent`), leaving mansions intact.
- Disable raids entirely.

**Fishing.** In vanilla the treasure has no emeralds: 6 items of weight 1, and the treasure branch is about 5% of catches in open water without Luck of the Sea. ISO does not override that table: an entry can be added with an override of `gameplay/fishing/treasure`. With weight 1 and 1 emerald that is about 1 catch in 140, and `LowFish` slows fishing down further. The datapack's weight of 72 would be enormously more generous: it makes sense there because the pack has a much richer treasure.

**What can be sold to villagers.** Besides fish, only non-renewable resources are added. The datapack actually already buys a bit more: at the weaponsmith 4 diamonds or 24 lapis lazuli blocks for 1 emerald, at the toolsmith its own gems, at the trader discs. Of these, the principle is kept, restricted to the resource rule.

| Candidate | Notes |
|---|---|
| Diamond | The reference case; the datapack asks for 4 per emerald |
| Lapis lazuli | Non-renewable if no villager sells it |
| Ancient debris / netherite | To be evaluated: perhaps too valuable to be sold |
| Echo shard, Divine Fragment (heart of the sea) | Rare and tied to structures |
| To exclude | Iron (`farmable_iron`, golems), gold and quartz (piglins), coal (wither skeletons), amethyst (regrows), fish aside |

Each candidate must be rechecked against ISO's packs. Two constraints to respect:

- **No closed loops:** an item bought by one villager must not be sold by another.
- **Watch out for `recycling`:** if a villager sells diamond equipment and recycling returns diamonds, diamonds become renewable.

### Prices

**Decided model:** the price can stay at 1 emerald, but the villager gives fewer items and has limited uses.

How the 1.21.1 code behaves (`MerchantOffer`, `Villager`):

- The villager restocks at most 2 times a day and `resetUses` zeroes the uses: `maxUses` is therefore the limit per restock, about double per day.
- The cost is `base + floor(base × demand × priceMultiplier) + discounts`, clamped to a minimum of 1. With `priceMultiplier` 0, demand and reputation have no effect, and 1 emerald stays 1 emerald.
- With a multiplier greater than 0, demand can raise the price of heavily used trades: this is the only case where `clampNegativeDemand` is needed.
- Selling trades (villager buying, e.g. fish → emerald) have the cost in items, so discounts and demand really apply.

Indicative starting values, to be tuned by playing:

| Category | 1 emerald gives | Uses per restock |
|---|---|---|
| Common blocks (stone, dirt, logs) | 8-16 | 8-12 |
| Processed blocks and wool | 4-8 | 8 |
| Redstone and components | 2-4 | 4-8 |
| Arrows | 8-16 | 8 |
| Seeds and crops | 2-4 | 8 |
| Potions, special arrows | 1-2 | 2-4 |
| Equipment and unique items | 1, at a higher cost | 1-3 |
| Selling (items → 1 emerald) | n/a | 12-16 |

The level acts as the lever: same price, but better materials at high levels.

## 9. Implementation proposal

To be carried out only after confirmation.

**Features** in `module/mobs/villager/`:

- `Villagers`: port from ITR, without `nitwitChance`, which only concerns breeding. Evaluate `lockTrades`, `maxDiscount`, `clampNegativeDemand`, `experienceToLevelUpMultiplier`.
- `VillagerTradesReloadListener`, `VillagerTrade`, `SerializableTrade`: ported to 1.21.1 (`CompoundTag` → components, `ItemCost`, `Holder<Enchantment>`), with optional mod trades ignored if the item is missing.
- `WanderingTrades`: if the custom trader is kept.
- Integrated `villager_trades` pack to be added with its own option.

**Mixins** (`mixin/module/mobs/villager/`):

| Target | Purpose |
|---|---|
| `Villager.canBreed` | Disables breeding |
| `ZombieVillager.mobInteract` | Cure item from tag (golden carrot) |
| `MerchantOffer` | Discount limit, demand, cost stack (`getModifiedCostCount`) |
| `VillagerData` (accessor) | Level XP multiplier |
| `WanderingTrader.updateTrades` | Number of trades (only if customised) |

**Other changes:** removal of `replaceZombieVillagersWithNormalZombies` and its code, cure item tag, removal of emeralds from `pillager.json`, return of mansions, emeralds in fishing, update of `PORTING.md`.

**Phases:**

1. Base path: no zombie replacement, cure with golden carrot, no breeding, pillagers without emeralds, mansions, fishing. This alone makes villagers obtainable, with vanilla trades.
2. Trade infrastructure.
3. Trade JSONs, one profession at a time, with the price model of §8.
4. Extras: Amnestic, Local Flavours, Beacon Kindling.

## 10. Open decisions

Already decided and reported above: cure item, igloos, number of trades, price model, villages and outposts off, mansions on, emeralds removed from pillagers, emeralds from fishing, selling to villagers limited to fish and non-renewable resources.

1. **Cure discount:** zero it (`priceMultiplier` 0, recommended) or limit it? It only concerns trades where the villager buys items. With every villager being cured, the discount would become the norm.
2. **Trade locking:** with 2 random trades, without `lockTrades` they can be rerolled by changing the job block before the first trade. Re-enable it? I recommend yes, leaving the Amnestic as the intended way to reroll.
3. **Nitwits from zombies:** exclude them? I recommend yes.
4. **Wandering trader:** currently vanilla in ISO. The datapack keeps the natural spawn, changes the trades and adds summoning through Beacon Kindling. The choice is between natural spawn, summon only, or both; and with which trades. If it buys discs it is another renewable source of emeralds.
5. **Extras:** which of Amnestic, Local Flavours and Beacon Kindling?
6. **Iron golems:** with 5 villagers `spawnGolemIfNeeded` stays active, and opens up iron farms. Leave it or turn it off?
7. **Conversion on death:** ITR had `alwaysConvertZombie`. Bring it back, given that every lost villager is costly?
8. **Raids:** remove emeralds from raiders with an active raid (recommended) or disable raids?
9. **Sellable resources:** which candidates from §8 to keep, besides diamonds, and which profession to assign them to?

## 11. Limits of the analysis

- No in-game testing: the conclusions come from reading the 1.21.1 sources and the pack's files.
- The check on job site recipes is based on file names in `src`: they appear vanilla, except `smoker`, overridden by `no_food_in_furnace`. To be confirmed in game.
- I did not verify whether ISO's changes alter biome spawn weights.
- Raid behaviour in ISO has not been tested: patrols do not depend on outposts and villagers would now be real targets.
