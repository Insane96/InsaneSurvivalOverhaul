# Reintegrating villagers into ISO: analysis

Source: the *Matcha Flavoured DP* 1.12.9 datapack for Minecraft 26.3 (`MFDP_1_12_9_26_3.zip`), compared with ISO 1.21.1 and with the old IguanaTweaksReborn 1.20.1 (ITR) system.

**Related reports:**
- `report_villager_trades_system.md`: the trade system (data format aligned with 26.1, data pack merging, wandering trader pools, dump command, technical notes on 1.21.1). It is a prerequisite of this overhaul.
- `report_villagers_modpack.md`: what the Survival Reimagined modpack must change when the overhaul is released.
- `report_villagers_enhanced_ai_prompt.md`: prompt for the Enhanced AI analysis (golem repair, villagers eating to heal).

This document is analysis only: no code files were changed and nothing was tested in game. Technical claims were checked against the NeoForge 21.1.234 sources and the vanilla 1.21.1 data; the only estimate left is the number of villager slots per village (§4.1).

## 1. Summary

- **The datapack cannot be imported.** It uses the `villager_trade` and `trade_set` registries and the `item_model` component, which do not exist in 1.21.1. ISO takes the idea, not the files, and does not port ITR's trade JSONs either: the trades are written from scratch in ISO's new format.
- **Today a villager cannot be obtained in ISO.** Three obstacles, all with a decided solution:
  1. `Spawning.replaceZombieVillagersWithNormalZombies` turns every zombie villager into a zombie: the option is removed.
  2. The golden apple is removed from the game: the cure uses the golden carrot.
  3. Breeding is active and is turned off with a mixin, because no usable event exists for villagers.
- **Villagers only come from curing zombie villagers** (accepted exceptions: igloos and the rare zombie villagers of abandoned villages). The cure has a configurable duration, followed by a configurable convalescence (no trading, Slowness). Conversion to zombie villager always happens, whatever the difficulty.
- **A single integrated data pack, `villagers_overhaul`,** holds the data side of the overhaul. It replaces `disable_long_noses` and `Packs.disableLongNosesStructures`, and is enabled only together with the `Villagers` feature.
- **World generation:**
  - villages come back, all abandoned: rarer, no inhabitants (rarely a few zombie villagers), no beds, bells or job sites, almost no food, loot mostly from archaeology;
  - outposts come back without respawn, with a one-time garrison;
  - mansions come back;
  - emerald ore generates rarely in every overworld biome, more often in mountains.
- **Economy:**
  - villagers only sell; the only exceptions are *Local Flavours* (a villager buys one complex food per restock) and the wandering trader, which buys very little;
  - emeralds come mostly from outside villagers (ore, archaeology, illagers, advancements, fishing): villagers are where they are spent;
  - prices start at 1 emerald, **rise with demand and mistreatment and never drop below the base**;
  - uses per restock are low, and special items have 1 use.
- **Trades:** each profession is a themed supplier with progression by level, 2 random trades per level as in vanilla. Nothing is enchanted except the librarian's books. Diamond equipment is always at level 5 and costs diamonds too, so it never generates diamonds through recycling.
- **Vanilla trades are never live:** the overhaul is released all together, after the trade system.

## 2. Current state in ISO

| Element | Where | State |
|---|---|---|
| Villages, outposts and mansions off | `integrated_packs/disable_long_noses`, config `Packs.disableLongNosesStructures` | **To be replaced** by the `villagers_overhaul` pack |
| Emerald ore | Vanilla, mountain biomes only; `ore_generation` does not touch it | To be extended to every biome (§4.4) |
| Pillagers with 1-2 emeralds | `mob_loot_changes/data/minecraft/loot_table/entities/pillager.json` (`set_count` 1-2, looting -1..1; vanilla Java drops none since 18w46a) | **To be reduced** to 0-1 and moved into `villagers_overhaul`, keeping the captain's ominous bottle pool (§5.2) |
| Zombie villagers drop emeralds | `crops/.../replace_iron_ingot_from_zombie_villager.json` turns the rare iron ingot into an emerald | Renewable but rare and tied to combat: stays |
| Zombie villagers replaced by zombies | `module/mobs/spawning/Spawning.java` | Active by default, **to be removed** |
| Golden apple removed | `food_changes`: recipe with `neoforge:false` condition, loot replaced with 4 gold ingots, removed from creative | Stays: the cure moves to the golden carrot |
| Golden carrot | `food_changes/.../item_components/golden_carrot.json` only; no recipe override | Ready |
| Farmer and ISO seeds | `Crops.fixVillagers`, `VillagerAccessor` | Ready |
| Growth and breeding data | `livestock_data/villager.json` | Growth is still needed for cured babies; the breeding entries become useless |
| Zombie villager loot | `crops/.../replace_*_from_zombie_villager.json` | Ready |
| Villager passive regeneration | `MiscMobs.PASSIVE_REGEN` tag | Ready |
| Fletching table | `FletchingTableBlockMixin_Fletching` keeps the vanilla block | Remains a valid job site |
| Iron farms | `Nerfs.ironFromGolemsRequiresPlayer` (default true): golems drop iron only when killed directly by a player | Already nerfed |
| Zombie siege | `module/mobs/ZombieSiege.java`: sieges happen near the player without a village | Stays: protecting villagers is up to the player (§3.5) |
| Totem | `Tweaks.totemResistance` | Totems are part of ISO's design: relevant for mansions and raids |
| Recycling | `Recycling` feature and `recycling` pack | Needs a fix before the overhaul (§5.4) |
| Custom trades | None | Written from scratch on the new system (§6) |
| Wandering trader | Vanilla spawn and trades | Redesigned (§6.15) |
| Emeralds in the modpack | Survival Reimagined removes them with `datapacks/no_emeralds_datapack` | The modpack drops it (§5.2) |

`Spawning.java` reacts to `EntityJoinLevelEvent` for every zombie villager, so it also hits those created from a killed villager and those reloaded from disk. This is why the option is removed entirely: not even the `true` value saved in existing `server.toml` files can keep it active.

## 3. Obtaining villagers

### 3.1 Where zombie villagers come from

- **Natural spawn:** weight 5 against 95 for zombies, and groups of 1 against 4. Roughly 1 in 75 zombies, if ISO does not alter biome spawns.
- **Abandoned villages:** occasionally a few, placed by the structure, with infinite Fire Resistance (§4.1).
- **Conversion of a villager killed by a zombie:** vanilla converts never on Easy, 50% on Normal, always on Hard (`Zombie.killedEntity`). **Decided: always, whatever the difficulty**, with ITR's `alwaysConvertZombie` brought back (default true). It needs a mixin on `Zombie.killedEntity`: the method exits early on Easy, and the conversion event can only cancel. The zombie villager keeps profession, level, XP and trades, so a lost villager can be cured back.
- **Despawn:** they burn during the day and despawn at a distance, except while being cured or if they have XP (`removeWhenFarAway`). So the player must cure them on the spot or move them.
  - **Decided: a zombie villager never despawns while converting.** Vanilla already guarantees it: `removeWhenFarAway` returns false while `isConverting()`, and every `checkDespawn` path goes through it (only Peaceful discards the zombie regardless). No code is needed.
  - A lead cannot be attached to a zombie villager (`Mob.canBeLeashed` is false for every `Enemy`), so moving one means a boat or a minecart.
  - The cure lasts minutes and the zombie burns in daylight, so it is cured at night or under cover.
- **Initial profession:** random among the 15 in the registry (`ZombieVillager:75`). In practice it matters little: a cured villager with 0 XP and no job site goes back to unemployed (`ResetProfession`), and the player directs it with the job block.
- **Nitwits (decided: removed).** They stay nitwits after the cure (`ResetProfession` excludes them). They are about 1 in 15, and in a world without breeding they are a wasted cure. In `LivingConversionEvent.Post`, if the result is a nitwit `Villager`, its profession becomes `NONE` and `refreshBrain` is called: the brain was built with the nitwit profile before the event, and without the refresh the villager could never acquire a job. No mixin is needed.

### 3.2 The cure

It requires Weakness on the zombie and the cure item (`ZombieVillager.mobInteract:158`).

- **Cure item (decided): golden carrot.**
  - The check is `itemstack.is(Items.GOLDEN_APPLE)`. A mixin replaces it, preferably pointing to an item tag with the golden carrot as default, so it stays modifiable via datapack. The method runs on both sides, so the tag works fine.
  - The golden carrot costs 8 nuggets instead of the apple's 8 ingots, so a cure is about 9 times cheaper, and gold is renewable. This is a deliberate choice: the real gates are the rarity of zombie villagers and Weakness.
- **Weakness:** in vanilla a fermented spider eye on water is enough, plus gunpowder for the splash potion. No changes in ISO. The real gate is the brewing stand, which requires a blaze rod, and therefore the Nether.
- **Cure duration (decided: configurable).** Vanilla is 3600-6000 ticks, rolled in `mobInteract`, sped up by beds and iron bars (`getConversionProgress`). The change goes on the call in `mobInteract`, not in `startConverting`, which also runs on load from NBT with the remaining time. Making the speed-up configurable too is to be evaluated.
- **Convalescence (decided, all parts configurable).** After the conversion, the villager is just back from being a zombie:
  - **no trading** for a configurable time: the villager refuses the interaction with feedback, like vanilla's "no" head shake;
  - **Slowness** for the same period (or its own);
  - no Weakness: on a villager it would only be cosmetic.
  - *Technical:*
    - it is applied in `LivingConversionEvent.Post`;
    - blocking trades needs a state saved on the villager, checked on interaction (`PlayerInteractEvent.EntityInteract` or a mixin on `Villager.mobInteract`);
    - a custom "convalescence" `MobEffect` would show the player the remaining time and could carry the Slowness too;
    - to decide during implementation: whether the villager can work and restock meanwhile.
- **No cure discount (decided).** The cure gives the curer 125 reputation (`MAJOR_POSITIVE` 20×5 + `MINOR_POSITIVE` 25), and the discount is `reputation × priceMultiplier`. Since every villager is cured, a discount would become the norm. Positive reputation is ignored in price calculation (§5.3).

### 3.3 Disabling breeding

- **Recommended point:** `Villager.canBreed()` returning `false`. It stops both the partner search (`VillagerGoalPackages:165`) and `VillagerMakeLove:90`.
- `BabyEntitySpawnEvent` **is not usable**: `VillagerMakeLove` does not fire it.
- `getBreedOffspring` returning `null` would leave the animation and the food consumption in place.
- `Villager` does not extend `Animal`, so ISO's `Animal`-based breeding mechanics should not come into play.

### 3.4 Other villager sources

- **Igloos (decided: they stay vanilla):** `igloo/bottom.nbt` contains a villager and a zombie villager, and ISO does not touch igloos. It is an accepted exception that requires no changes.
- Creative spawn eggs and `/summon` are not a problem.

### 3.5 Risks for obtained villagers

- **Zombie sieges:** `ZombieSiege` starts sieges near the player even without a village, so villagers kept at the base are exposed.
- **Raids:** an area counts as a village only with an occupied POI (a bed, job site or bell claimed by a villager). A base with beds alone does not count; a base whose villagers claimed one can be raided.
- **Natural zombies:** zombies attack villagers, and the conversion is always on (§3.1).

**Decided:** these risks stay as they are: protecting villagers is up to the player. A villager killed by a zombie becomes a zombie villager that keeps its trades, so it can be cured back.

**Iron golems (decided):** `spawnGolemIfNeeded` stays active (5 villagers when gossiping, 3 when panicking; each must have slept in the last day). Iron farms are already nerfed by `Nerfs.ironFromGolemsRequiresPlayer`.

**Out of scope, for Enhanced AI:** villagers eating food to recover health (ISO already gives them passive regeneration through `MiscMobs.PASSIVE_REGEN`) and iron-working villagers repairing golems.

## 4. World generation

### 4.1 Abandoned villages (decided)

**What Matcha Flavoured does** (checked in the datapack):
- **Structures:** all 5 `village_*` structures point to a single custom jigsaw, `village_beta` (start pool `village_beta/town_centers`, size 5, empty `spawn_overrides`). The biome variants are lost.
- **Rarity:** the `villages` structure set uses spacing 80 / separation 50, against vanilla's 34/8: about 1/5.5 of the density.
- **Buildings:** 9 beta-style ruins, a well and 4 roads, with cobwebs and doors. **No beds, bells, job sites (only one crafting table), crops, hay, or entities**, except one house with a "haunted" `mannequin` and a jukebox `marker`. So: no POIs, no villagers, no zombie villagers.
- **Loot:** in suspicious blocks, with the `archaeology/village_plains` loot table (pottery sherds, emerald, iron ingot, poppy, wheat, and a link to `matcha:archaeology/statues`). A couple of chests have no loot table.
- **Village chests:** the `chests/village/*` loot tables are modified but unused, since vanilla villages no longer generate.
- **"Haunted" atmosphere** (`matcha:environmental/village/*`): music stopped, eerie sounds depending on the block walked on, a mannequin that disappears when the player gets close, a jukebox playing "Dry Hands". It relies on 26.x features (`mannequin`, `stopwatch`).
- **Abbey:** the custom `abbey_overgrown` structure has a 16-chunk exclusion zone from villages.

**ISO's version.** ISO takes the idea, not the files: it keeps the vanilla structures with their biome variants and modifies them in the `villagers_overhaul` pack.
- **Inhabitants:**
  - the inhabitant pools are emptied, keeping a low weight for zombie villagers, using the templates of the vanilla zombie variants. Each villager slot of a village rolls the pool independently, so the weight is best chosen from the **expected zombie villagers per village**: with about 5-10 slots per village (estimate: 2-3 in the town centre plus 0.3-0.5 per house; the number of houses was not measured), a 5% weight gives about 0.25-0.5 per village (one village in 2-4 has one), 10% about 0.5-1. **Decided: 15%**, about 0.75-1.5 zombie villagers per village;
  - iron golems and cats are removed; animals are to be evaluated;
  - in plains, `village/plains/villagers` has nitwit 1, baby 1, unemployed 10, and `village/plains/zombie/villagers` has nitwit 1, unemployed 10. The zombie villager templates carry `PersistenceRequired:1b`, so they do not despawn;
  - the vanilla zombie variants (about 2% of town centres) fill every slot of their houses from `zombie/villagers`, and contain beds, bells and job sites: their pools and processors are overridden too, or the zombie town centres are removed;
  - **these zombie villagers have infinite Fire Resistance (decided)**, so the sun does not kill them before the player finds them. It is applied when they are placed (`FinalizeSpawnEvent` with spawn type `STRUCTURE`, or `active_effects` written in custom templates);
  - **the effect does not carry over to the cured villager:** `Mob.convertTo` and `ZombieVillager.finishConversion` do not copy effects, so no removal is needed.
- **No POIs:** processors remove beds, bells and job sites (or break them, e.g. bed → wool, bell → air). This reinforces the abandoned look. Vanilla raids and sieges need an occupied POI, so an uninhabited village does not count anyway; removing the POIs also stops a villager cured on the spot from claiming them. So there can be no raid farm in an abandoned village.
- **Rarer (decided):** spacing 80 / separation 50, as in Matcha.
- **Food:**
  - drastically less food in `chests/village/*`;
  - the food that is part of the blocks is removed too (crop fields, hay bales, pumpkins, melons), with processors that replace it, e.g. crops → dead bushes or coarse dirt;
  - to be compared with the `crops` and `food_changes` packs.
- **Loot (decided):**
  - **chests stay, with drastically reduced loot** (`chests/village/*` overrides);
  - **most of the loot comes from archaeology** (Matcha's idea): houses get suspicious blocks (gravel or sand, by biome) containing the items of vanilla villages. Each house's vanilla chest loot becomes themed archaeology loot (armorer, weaponsmith, cartographer, mason, tannery, temple, fletcher, shepherd...), with little or no food, plus sherds and rare emeralds;
  - it is a finite source. Valuable items of the vanilla chests (diamonds from the toolsmith and the weaponsmith, lapis from the temple) are checked against §5;
  - suspicious blocks with a loot table are placed with a `rule` processor with an `append_loot` block entity modifier, inside a `capped` processor to limit their number, as trail ruins do.
- **Haunted atmosphere:** not brought over (decided).
- **Village maps:** sold by the wandering trader (§6.15).

### 4.2 Outposts without respawn (decided)

In vanilla, outpost pillagers do not come from the structure's templates: they come only from the structure's `spawn_overrides`, which keep spawning them anywhere inside the bounding box. Matcha empties `spawn_overrides`; its outposts are also rebuilt (abbey-themed towers, a zombie spawner with `NoDrops`, archaeology), so in practice they are empty of pillagers.

ISO brings outposts back with empty `spawn_overrides` and a **fixed, one-time garrison**:
- **pillagers added to the outpost templates**, including a captain. Vanilla outpost templates contain no pillagers, and mansion illagers are not a model (they come from data markers handled in code), so the `.nbt` files must be changed;
- every pillager needs `PersistenceRequired:1b`: `removeWhenFarAway` is true for non-patrolling pillagers, so they would despawn beyond 128 blocks;
- the captain only needs `PatrolLeader:1b`: `finalizeSpawn` puts the ominous banner on it, with no code.

Outposts thus give finite emeralds (pillagers 0-1) and a finite ominous bottle (the captain).

### 4.3 Mansions

In vanilla `mansion.json` has empty `spawn_overrides` and no rule makes illagers reappear; mansion mobs are placed at generation, and the chests contain no emeralds.
- Mansions come back simply because `villagers_overhaul`, unlike `disable_long_noses`, does not disable them. This only applies to chunks not yet generated.
- Vindicators and evokers are a finite source of emeralds (0-1) and totems.
- `food_changes`' loot modifiers have no conditions, so they also cover `chests/woodland_mansion`: golden apples become 4 gold ingots, enchanted golden apples a gold block.

### 4.4 Emerald ore (decided)

In vanilla the `ore_emerald` placed feature is only listed by the mountain biomes. The overhaul makes emerald ore **rare in every overworld biome and more frequent in mountains**:
- **mountains: vanilla** (the vanilla feature stays as it is);
- **every overworld biome: 1/4 of the vanilla rate**, with a new placed feature (e.g. `insanesurvivaloverhaul:ore_emerald_rare`) added with a NeoForge `neoforge:add_features` biome modifier inside `villagers_overhaul`, to `#minecraft:is_overworld`. Mountains get it too, on top of the vanilla feature (decided): about 1.25 times vanilla there.
- Same height range (trapezoid -16..480) and vein size (3) as vanilla; only the attempts per chunk are divided by 4, from 100 to 25. About 6% of the attempts fall below y≈70, so flat biomes get about 1.5 attempts per chunk: to be checked by playtesting.

## 5. Economy

In Matcha the emerald is a currency with a controlled supply: 86 emeralds from 37 advancements, plus chests, archaeology, fish, gems and discs sold to villagers. Importing "1 emerald → 64 items, 999 uses" would wipe out the gathering of many materials as soon as a few emeralds are found.

### 5.1 Principles (decided)

- **Villagers only sell.** No villager buys items for emeralds, except *Local Flavours* (§6.16). The datapack's ideas of selling fish, diamonds, lapis, gems and discs to villagers are dropped. The wandering trader is the only merchant that buys a little (§6.15).
- **Vanilla trades are never live.** They buy renewable resources (crops, paper, sticks, wool, rotten flesh, meat), sell diamond equipment (renewable diamonds through `recycling`) and enchanted books. Villagers become obtainable, villages come back and the new trades go live **all together**, in a single release (§8).
- **The rule for emerald sources.** "Renewable or not" is not the right test: fishing, *Local Flavours* and patrols are all renewable. Two levels instead:
  1. **sources that go through villagers** are capped at `N villagers × maxUses × 2 restocks per day`. Since villagers only sell, the only such source is *Local Flavours*, with 1 use per restock: 6 villagers give at most 12 emeralds a day;
  2. **sources outside villagers** (drops, fishing, loot) must be finite, or tied to active work and not automatable.
- **No economy target (decided):** the values in this report are the starting point, tuned by playtesting.

### 5.2 Emerald sources

| Source | Kind | Decision |
|---|---|---|
| Ore | Finite | Every overworld biome, rarely; more in mountains (§4.4) |
| Abandoned village archaeology | Finite | Rare emeralds (§4.1) |
| Mansion vindicators and evokers, 0-1 | Finite | Stay (§4.3) |
| Outpost garrison, 0-1 per pillager | Finite (no respawn) | Stays (§4.2) |
| Advancements that give XP | Finite (once per player) | Added (below) |
| Pillagers in patrols | Renewable, slow, tied to combat | 0-1 emerald, looting at most +1 (below) |
| Raiders in raids | Renewable, difficult | Stay, with their drops |
| Zombie villagers (iron ingot → emerald) | Renewable, rare, tied to combat | Stays |
| Fishing | Renewable, slow; `LowFish` stops AFK farms | Added, rare (below) |
| *Local Flavours* | Capped: 1 per villager per restock | Added (§6.16) |
| Wandering trader purchases | Renewable, very limited (2 trades with 1 use per trader) | Added (§6.15) |
| Chest loot | Finite | Vanilla; the modpack stops removing emeralds (below) |

**Pillagers (decided).** Patrols come from `PatrolSpawner` independently of outposts. They stay, because without them Bad Omen would only come from trial chambers and would be too rare.
- The drop is reduced to **0-1 emerald** (vanilla 1.21.1 has none, Bedrock is more generous).
- **Looting** adds at most +1 emerald with a 33/66/99% chance at Looting I/II/III, through a separate pool with `random_chance_with_enchanted_bonus` (`unenchanted_chance` 0, 0.33 per level). `enchanted_count_increase` is not suitable: it multiplies a single roll by the level.
- The loot moves from `mob_loot_changes` into `villagers_overhaul`, so the whole emerald economy is switched on and off together. Every override of this table must keep the vanilla pool with the captain's ominous bottle: without it, patrols give no raids.

**Raids (decided).** Patrol captains give the ominous bottle, so raids remain repeatable. They stay, and their vindicators and evokers keep their emeralds and totems: they are difficult, so it is an accepted reward. Abandoned villages cannot host a raid (§4.1).

**Advancements (decided).** Every advancement that gives XP as a reward also gives emeralds. It is a finite, one-off source per player, like Matcha's. In the modpack, where experience is disabled, it becomes the only reward of those advancements.
- **How:** no advancement overrides. ISO listens to the advancement being earned (NeoForge `AdvancementEvent.AdvancementEarnEvent`), reads its XP reward and gives the emeralds. It covers vanilla, ISO and mod advancements automatically.
- Amount: proportional to the XP reward (e.g. 1 emerald per 25-50 XP), to be tuned.

**Fishing (decided).** In vanilla the treasure has no emeralds: 6 items of weight 1, and the treasure branch is about 5% of catches in open water without Luck of the Sea.
- A global loot modifier on `gameplay/fishing/treasure` does nothing (verified by the author): NeoForge does not run loot modifiers on nested tables, and `gameplay/fishing` rolls `treasure` as a nested table. The modpack hit the same limit with vault loot.
- **Decided:** a loot modifier on `gameplay/fishing` (the table actually rolled), with the conditions of the treasure branch: the hook `in_open_water` (`fishing_hook` sub-predicate) and a random chance of about 1/140 per catch. It replaces the caught item, so a catch still gives one item. Luck of the Sea can count through `random_chance_with_enchanted_bonus`. Overriding the treasure table was rejected, because it conflicts with any other change to that table.
- `LowFish` slows down fishing after 10 catches in the same spot (range 8, +100 ticks per catch beyond), the real defence against AFK farms. An estimate of emeralds per hour is still to be done.

**The modpack (decided).** Survival Reimagined currently removes emeralds through `datapacks/no_emeralds_datapack` (empty ore placement, every emerald from any loot table turned into runes). With the overhaul emeralds no longer become runes and the ore comes back, so the modpack drops that data pack (`report_villagers_modpack.md`).

### 5.3 Prices (decided)

**Model:** the base price is 1 emerald, the villager gives few items and uses are limited. **The price rises with demand and with mistreatment, and never drops below the base.**

How the 1.21.1 code behaves (`MerchantOffer`, `Villager`):
- The villager restocks at most 2 times a day and `resetUses` zeroes the uses: `maxUses` is the limit per restock, about double per day.
- The cost is `base + max(0, floor(base × demand × priceMultiplier)) + specialPriceDiff`, clamped between 1 and the stack size.
  - **Demand:** at each restock it changes by `uses − (maxUses − uses)`. Heavily used trades get more expensive.
  - **`specialPriceDiff`:** `updateSpecialPrices` subtracts `floor(reputation × priceMultiplier)` when the player opens the trade screen. Positive reputation (cure, trading) lowers the price; negative reputation (hitting villagers is `MINOR_NEGATIVE`, killing them `MAJOR_NEGATIVE`) raises it. Hero of the Village subtracts a share of the cost.

What ISO does:
- **`reputation_discount` 0.2 on every ISO trade**, for both demand and mistreatment. A fully used 8-use trade costs 2 emeralds after one restock and about 4 after two; hitting a villager adds about 5 emeralds until the gossip fades (about 2 days). Vanilla's 0.05 would barely be felt on 1-emerald trades.
- **Mixin on `Villager.updateSpecialPrices`:** only negative reputation applies; positive reputation (cure discount) and the Hero of the Village discount are ignored. `specialPriceDiff` is never negative. Only the negative gossip types are summed: `getPlayerReputation` adds them all, and a curer holds a permanent +100 (`MAJOR_POSITIVE` never decays) that would cancel any mistreatment.
- **Slower decay of positive demand:** a mixin on the demand update (`MerchantOffer.updateDemand`) multiplies the decrease by **0.6** while demand is above 0. In vanilla an unused trade drops by `maxUses` per restock and is back to base after one restock; with 0.6 and 8 uses, a trade at demand 24 (price 5) needs about 5 restocks, 2-3 days. Increases, and the behaviour below 0, stay as in vanilla.
- **Negative demand stays**, with ITR's `clampNegativeDemand` at `-maxUses`, so a trade can always get more expensive again.
- **Demand also cools with time, as in vanilla (decided).** At each new day `catchUpDemand` applies one update for every restock missed that day (`2 − restocks`), so an unused trade loses `2 × maxUses` a day, times 0.6 while above 0. It only happens while the villager goes to its job site (`shouldRestock` is only called by `WorkAtPoi`). A restock itself needs at least one used trade (`needsToRestock`).
- ITR's `maxDiscount` and `preventCureDiscount` are not needed: there are no discounts.
- *Local Flavours* follows the same rules: its cost is the food, so demand and mistreatment raise the amount of food asked.

**Quantities and uses** (starting values, to be tuned against the anchor):

| Category | 1 emerald gives | Uses per restock |
|---|---|---|
| Simple blocks (stone, dirt) | 64 | 4-6 |
| Logs | 24-48 | 4-6 |
| Processed blocks | 24-48 | 4 |
| Wool | 12-24 | 4 |
| Redstone components | 2-4 | 2-4 |
| Arrows | 8-16 | 4 |
| Seeds and crops | 2-4 | 4 |
| Potions, special arrows | 1-2 | 1-2 |
| **Special items** | 1, sometimes at a higher cost | **1** |
| *Local Flavours* (food → 1 emerald) | n/a | 1 |
| Wandering trader buying | n/a | 1 |

**Special items** have 1 use per restock: equipment (armour, tools, weapons, bows, crossbows, fishing rods, shields, horse armour, saddles), workstations (anvil, smithing table, grindstone, crafter), bells, lava buckets, maps, enchanted books and the Divine Fragment.

The level acts as the lever: same price, better materials at high levels.

### 5.4 Diamond equipment and recycling (decided)

ISO's recycling gives back the material in proportion to the remaining durability, times `Recycling.outputMultiplier`. A villager selling diamond equipment for emeralds only would turn emeralds into diamonds.

**Rule:** diamond equipment is **always at level 5**, for any profession, and costs **1 emerald + 75% of the diamonds in the recipe, rounded down**. Recycling at 0.75 gives back exactly the same rounded-down amount, so buying and recycling never gains diamonds, and diamonds stay non-renewable. The saving is small but real.

| Item | Diamonds in the recipe | Price | Recycled at 0.75 |
|---|---|---|---|
| Chestplate | 8 | 1 emerald + 6 | 6 |
| Leggings | 7 | 1 + 5 | 5 |
| Helmet | 5 | 1 + 3 | 3 |
| Boots | 4 | 1 + 3 | 3 |
| Pickaxe, axe | 3 | 1 + 2 | 2 |
| Sword, hoe | 2 | 1 + 1 | 1 |
| Shovel, `diamond_dagger` | 1 | 1 emerald only | — (no recipe) |

**Prerequisite, to be done in ISO's recycling** (by the author):
- `Recycling.outputMultiplier` default **0.75** (today 1.0; the modpack already uses 0.75). At 1.0 a chestplate would recycle into 8 diamonds against the 6 paid.
- Recipes of items without durability back to their full value, so that the multiplier is the only knob. Today their ~75% is written in the recipes (iron door 13 nuggets out of 18, anvil 23 ingots out of 31...) and the multiplier is not applied; recipe viewers also show the multiplier on them by mistake (`getResultItem`).
- **No recycling recipe for `diamond_shovel` and `diamond_dagger`:** 1 diamond at 0.75 rounds down to 0, and `Recycling.minimumOutput` (default 1) would turn it into 1, an emerald → diamond conversion. Removing the minimum would instead let the player smelt the item for nothing, so the recipe should not exist. The minimum stays for every other item.

## 6. Trades

### 6.1 General rules (decided)

- **2 random trades per level**, as in vanilla (`Villager.updateTrades`). A level with 1 trade always offers it, with 2 always both; at least 3 are needed for randomness. Important trades go in small pools. Empty levels are fine (Matcha's "filler" trades are not needed).
- **Only sells** (§5.1), with the quantities, uses and prices of §5.3.
- **Nothing is enchanted, except the librarian's books.** Tools, bows, rods and armour are sold plain, so they are cheap and sit at low levels.
- **Diamond equipment** follows §5.4.
- **Random results** (flower, colours, potions, tipped arrows, pottery sherds) are picked when the offer is created and stay fixed for that offer, through ISO's random loot functions (`report_villager_trades_system.md` §4).
- **Maps** cost 1 emerald + 1 empty map and use the `exploration_map` loot function. The trader sells surface structures, the cartographer underground ones and the ocean monument. **No map at all** for buried treasure (exclusive to shipwreck maps), ocean ruins, shipwrecks, ruined portals, mineshafts, outposts, strongholds (eyes of ender would become pointless) and biomes.
- **Smiths:** the armorer, the toolsmith and the weaponsmith all sell the bell (level 2), the anvil and the lava bucket. Matcha's redstone components are split among them.
- **Modpack:** it adds its own mods' items (foods, eggs, stones, woods) to the matching trade tags, in ISO's format.

### 6.2 Overview

In parentheses, Matcha's pool size for levels 1-5, excluding levels with only filler.

| Profession | In Matcha | In ISO |
|---|---|---|
| **Armorer** (5/3/4/3/1) | A redstone engineer: components ×32-64, a lava kit at level 5 | Copper and iron armour, smith goods, part of the redstone, diamond armour |
| **Butcher** (2/2/2/1) | The pack's dishes and their "recipes", a butcher knife | Daggers, little food, leather |
| **Cartographer** (5/5/6/2/4) | 12 structure maps, pottery sherds, brush, statues | 3 maps as progression steps, paper, tools, decorations |
| **Cleric** (7) | 16 splash potions per emerald, mostly harmful; levels 2-5 empty | Brewing supplier: ingredients, then random potions |
| **Farmer** (5/3/6) | Bone meal ×64, "bundles" of seeds, flowers, mushrooms; chicken, calf and piglet spawn eggs | Bone meal, flowers, renewable seeds, eggs, honey; non-renewable seeds at level 5 |
| **Fisherman** (1/16/10/10/11) | Buys ~45 custom fish species, fewer per emerald as rarity rises; Almanac book | Sells fish by processing, rod, nautilus shells, decorations |
| **Fletcher** (3/1/1) | 64 arrows, healing and poison tipped arrows, spectral arrows, a special bow | Arrows, bows, ISO's special arrows, random tipped arrows |
| **Leatherworker** (3/6/4/1) | A lumberjack: logs ×64 (exotic species also cost a sapling), an iron axe | Logs at 1-3, leather goods at 4-5 |
| **Librarian** (3/3/1/1/1) | Rare books → heart containers, sacred texts → enchanted "ofuda", crystal heart; ender pearls | Paper, lapis, random enchanted books, Divine Fragment |
| **Mason** (5/6/4/3) | "Bulk" stones (items standing for whole stacks) | Real blocks, 64 simple, 24-48 processed |
| **Shepherd** (16/4/1) | 16 wool colours ×64, sheep spawn eggs, shears | Wool and coloured items with random colours, furnishings |
| **Toolsmith** (4/1/1/2/4) | Jewellery (head-slot items with +1 armour, made of the pack's gems), gems | Plain tools, smith goods, workstations, diamond tools |
| **Weaponsmith** (4/49) | Buys diamonds and the pack's metals; 49 items of metals that do not exist in 1.21.1 | Plain weapons, shield, smith goods, part of the redstone, diamond weapons |
| **Wandering Trader** | Buys discs; sells village maps and villager eggs | ITR direction: sells nature and maps, buys very little |

### 6.3 Armorer

| Level | Sells |
|---|---|
| 1 | Copper armour (helmet, chestplate, leggings, boots) |
| 2 | Iron armour (helmet, chestplate, leggings, boots), bell |
| 3 | Anvil, lava bucket |
| 4 | Pistons, hoppers, dispensers |
| 5 | Iron horse armour, diamond armour (helmet, chestplate, leggings, boots) |

- Redstone components: 2-4 per emerald; ISO's `actual_redstone_components` makes redstone matter.
- The lava bucket recalls Matcha's "lava kit".
- No chainmail armour (craftable with cloth in ISO).

### 6.4 Butcher

Food is a challenge in ISO (`livestock` reduces meat), so the butcher sells little of it. It never sells *Local Flavours* foods, which would make a pointless buy-and-resell loop.

| Level | Sells |
|---|---|
| 1 | `iron_dagger`, `copper_dagger` (like Matcha's butcher knife; iron is cheap once villagers exist) |
| 2 | Raw chicken, raw mutton, raw rabbit: 2 per emerald |
| 3 | Cooked chicken, cooked mutton, `over_easy_egg`: 1 per emerald |
| 4 | Leather, cooked porkchop, cooked beef: 1 per emerald |
| 5 | Rabbit's foot, cooked rabbit, `diamond_dagger` (1 emerald only, §5.4) |

### 6.5 Cartographer

Each map sits in a level with only 2 trades, so it is always offered.

| Level | Sells |
|---|---|
| 1 | Paper, empty map, compass |
| 2 | **Trial chambers map**, brush |
| 3 | **Ocean monument map**, spyglass |
| 4 | A random pottery sherd, a banner of a random colour, item frame |
| 5 | **Ancient city map**, a random pottery sherd |

- Selling pottery sherds makes them renewable, which is fine for decorative items.
- Banners and item frames are also sold by the shepherd: accepted overlap.

### 6.6 Cleric

A brewing supplier: ingredients first, then random potions. ISO's stack sizes apply (potions 16, splash potions 4).

| Level | Sells |
|---|---|
| 1 | Glass bottles, redstone dust, glowstone dust |
| 2 | Nether wart, fermented spider eye, sugar, magma cream |
| 3 | A random level I potion, a random level I splash potion: **2 per emerald** |
| 4 | A random level II potion, a random level II splash potion: **1 for 2 emeralds** |
| 5 | A random lingering potion (**1 for 2 emeralds**); blaze powder, ghast tears, ender pearls (low counts) |

- Long variants are excluded.
- No fixed Weakness potion: the cure still needs the player to brew it, at least the first time.
- Nether wart lets the player brew without going back to the Nether; whoever has a villager has already been there.

### 6.7 Farmer

No spawn eggs: animals still have to be found and bred (`livestock`).

| Level | Sells |
|---|---|
| 1 | Bone meal (about 16 per emerald, weighed against the `bone_meal` pack's nerf), a random flower |
| 2 | Pumpkin seeds, melon seeds, cocoa beans, red and brown mushrooms, sugar cane |
| 3 | Chicken eggs (`minecraft:egg`) |
| 4 | Glow berries, honey bottles, honeycomb |
| 5 | **Non-renewable seeds, 1 per emerald:** wheat seeds, beetroot seeds, `carrot_seeds`, `rooted_potato`, `sweet_berry_seeds` |

- In ISO the level 5 seeds only come from wild crops (the `crops` pack removes seeds from grass and ferns): selling them only at the last level, one per emerald, keeps exploration meaningful.
- Sniffer seeds (torchflower, pitcher pod) are excluded.
- The modpack adds other egg types to the level 3 tag.

### 6.8 Fisherman

ISO only has the 4 vanilla fish, so Matcha's rarity ladder cannot be copied: the levels follow how processed the fish is.

| Level | Sells |
|---|---|
| 1 | Raw cod, raw salmon, dried kelp, fishing rod |
| 2 | Cooked cod, cooked salmon, pufferfish |
| 3 | `cod_chowder`, `pufferfish_chowder`, a bucket of fish |
| 4 | Nautilus shell (1 per emerald), buckets of fish |
| 5 | Decorative items that fishing also gives: lily pads, ink sacs, bamboo, tropical fish buckets |

- Selling nautilus shells makes conduits a little easier to reach: accepted.
- Counts must respect the fish stack sizes of the `item_stacks` pack (to check).

### 6.9 Fletcher

ISO's special arrows are made at the fletching table: torch (1 torch + 1 arrow → 1), spectral (1 glowstone dust + 2 → 2), quartz (1 quartz + 2 → 2), ice (1 blue ice + 4 → 4), explosive (1 TNT + 4 → 4), diamond (1 diamond + 12 → 12).

| Level | Sells |
|---|---|
| 1 | Arrows (8-16), flint, feathers, `shortbow` |
| 2 | Bow, crossbow, torch arrows |
| 3 | Spectral arrows, quartz arrows, tipped arrows with a random level I effect |
| 4 | Ice arrows, tipped arrows with a random level II effect |
| 5 | Explosive arrows, diamond arrows (few, e.g. 2 per emerald) |

- Tipped arrows: any effect; level 3 uses the base potions, level 4 the `strong_` ones; long variants excluded.
- `recycling` only recycles the crossbow (into iron), not arrows: diamond arrows create no diamonds.

### 6.10 Leatherworker

Logs at levels 1-3 (Matcha's lumberjack, without the sapling as second cost), leather goods at 4-5.

| Level | Sells |
|---|---|
| 1 | Oak, birch and spruce logs (24-48 per emerald); copper axe, iron axe |
| 2 | Acacia, jungle, dark oak, cherry and mangrove logs (24-48) |
| 3 | Crimson and warped stems, bamboo blocks, mushroom stems (24-48) |
| 4 | Undyed leather armour: helmet, chestplate, leggings, boots |
| 5 | Saddle, leather horse armour, lead |

- Plain leather is sold by the butcher, not here.
- The saddle is craftable in ISO (leather and iron): selling it is a convenience.

### 6.11 Librarian

The only villager that sells enchanted items; it also sells lapis, since it acts as the enchanter.

| Level | Sells |
|---|---|
| 1 | Paper, books |
| 2 | Lapis (8-16), bookshelves, lanterns, a random enchanted book |
| 3 | Lapis (16-32), a random enchanted book, compass, clock |
| 4 | A random enchanted book, book and quill |
| 5 | Divine Fragment (heart of the sea) for 1 emerald + 1 echo shard |

- **Enchanted books:** random, via `enchant_randomly` / `enchant_with_levels` in `given_item_modifiers`, limited to the `#tradeable` enchantments, stronger at higher levels. Cost 1 emerald + 1 book, 1 use per restock. They are a minor convenience: the enchanting table at level 30 gives better enchantments.
- **Divine Fragment:** 1 use per restock. Echo shards only come from ancient city chests, so the Divine Fragment stays bounded by a finite resource, as Matcha does with its crystal heart. Echo shards are also ISO's `spawner_reactivator`, so the player chooses between the two uses.
- No name tag.
- Matcha's prices for reference: 131 trades cost 1 emerald; only shears (3) and the toolsmith's jewellery (4-14) cost more. Valuable trades ask rare items, not many emeralds.
- Modpack: enchanted books become random runes, with a different second cost.

### 6.12 Mason

Bulk items do not exist in 1.21.1, so the mason sells real blocks: **simple blocks 64 per emerald** (stone, andesite, diorite, granite, dirt, deepslate, tuff, calcite, dripstone block, blackstone), **processed blocks 24-48** (glass, smooth sandstone, smooth red sandstone, smooth basalt, smooth quartz, terracotta, mud bricks, concrete, glazed terracotta).

| Level | Sells |
|---|---|
| 1 | Stone, andesite, diorite, granite, dirt |
| 2 | Deepslate, glass, smooth sandstone, smooth red sandstone, tuff |
| 3 | Calcite, dripstone block, mud bricks, terracotta of a random colour |
| 4 | Blackstone, smooth basalt, smooth quartz |
| 5 | Concrete of a random colour, glazed terracotta of a random colour |

### 6.13 Shepherd

With 16 colours and 2 random trades, one trade per colour makes a given colour almost impossible to find: coloured items are one trade with a random colour. No spawn eggs.

| Level | Sells |
|---|---|
| 1 | White wool (12-24), shears, string |
| 2 | Wool of a random colour (12-24), a random dye, a carpet of a random colour |
| 3 | Bed of a random colour, banner of a random colour, another random-colour wool trade |
| 4 | Painting, item frame, glow item frame, another random-colour wool trade |
| 5 | A random craftable banner pattern (flower, creeper, skull, bricks...) |

### 6.14 Toolsmith and weaponsmith

**Toolsmith**

| Level | Sells |
|---|---|
| 1 | Copper pickaxe, shovel and hoe |
| 2 | Iron pickaxe, shovel and hoe; bell |
| 3 | Anvil, lava bucket |
| 4 | Crafter, smithing table, grindstone |
| 5 | Diamond pickaxe, shovel, axe and hoe |

**Weaponsmith**

| Level | Sells |
|---|---|
| 1 | Copper sword, `copper_dagger`, copper axe |
| 2 | Iron sword, `iron_dagger`, iron axe, shield; bell |
| 3 | Anvil, lava bucket |
| 4 | Repeaters, comparators, observers |
| 5 | Diamond sword, `diamond_dagger` |

- Overlaps (daggers at the butcher, axes at the leatherworker) are accepted.
- Modpack: the shield becomes the Shields Plus one.

### 6.15 Wandering trader

- **Spawn:** natural, as in vanilla. No Beacon Kindling.
- **Direction:** as in ITR, with ideas from Matcha: it sells almost everything and buys very little.
- **Pools**, named as the 26.x trade sets. The counts (ITR's values) are read from the trade sets' `amount`; a mixin on `WanderingTrader.updateTrades` is needed anyway for the `buying` pool (see the system report).

| Pool | 1.21.1 | Trades rolled | Content |
|---|---|---|---|
| `common` | generic | 6 | Nature from other biomes: saplings of every species, a random flower, cactus, sugar cane, bamboo, vines, lily pads, glow lichen, moss, small dripleaf, pointed dripstone, rooted dirt, kelp, sea pickles, random coral blocks, red and brown mushrooms, podzol, mycelium, packed ice, blue ice; seeds as in ITR (wheat, beetroot, `carrot_seeds`, `rooted_potato`) |
| `uncommon` | rare | 2 | Maps of surface structures, 1 emerald + 1 empty map: desert pyramid, jungle pyramid, swamp hut, igloo, woodland mansion, abandoned villages, trail ruins |
| `buying` | none, added by ISO | 2 | Buys music discs (fixed low price, 1-2 emeralds) and one item as in ITR, like vanilla's trade rebalance (baked potatoes, fermented spider eyes, hay bales, milk, potions, water); 1 use per restock |

- Quantities: 12-24 for common plants and blocks, 1-4 for the rarer ones (blue ice, coral, sea pickles).
- Seeds are sold as in ITR, although rare in ISO: the trader is a random, occasional source.
- No villager eggs (Matcha's *Asylum Seekers*).
- Vanilla 1.21.1 cartographer maps are ocean monument (level 2), woodland mansion and trial chambers (level 3): with the decided split, the mansion moves to the trader.

### 6.16 Local Flavours (decided)

The only villager trade where the villager buys. Every employed villager with XP > 0 gets one extra trade, once: 1 random complex food → 1 emerald.
- **Only complex foods:** vanilla rabbit stew; ISO `fungi_stew`, `pufferfish_chowder`. The list is an item tag, so the modpack can add its own dishes.
- **1 use per restock**: at most 2 emeralds a day per villager.
- All professions, fishermen included.
- *Technical:* the trade is added outside the 2 random per level. It needs code (e.g. a check on level-up or on the first restock with XP > 0, plus a saved flag), and it must survive later levels.

### 6.17 Hero of the Village (decided)

Vanilla gives a discount (`0.3 + 0.0625 × amplifier` of the cost count, minimum 1, independent of `priceMultiplier`) and gifts thrown by villagers, from `gameplay/hero_of_the_village/<profession>_gift`. Matcha only rewrites the gifts with themed items.

- **Discount removed** (mixin on `Villager.updateSpecialPrices`, §5.3).
- **Gifts scale with the effect level.** The level (I-V) equals the raid omen level of the won raid (the omens absorbed, capped at 5). Hero level N lets the villager gift the items its profession sells **up to level N**, in reduced amounts (1 piece, or about half the trade's quantity).
  - A **mixin** on `GiveGiftToHero.throwGift`, the only method there that receives the player, reads the Hero level and uses the matching loot table, e.g. `<profession>_gift_level_<n>`. It is needed because `getItemToThrow` and the gift's loot context do not include the player.
  - The loot tables are generated from the trade files (a data provider in ISO's `runData`, or a script), so they stay in sync.
  - Random trades become loot table entries with the same random choice.
  - **Excluded:** every trade with a second cost (enchanted books, Divine Fragment, diamond equipment except the 1-emerald shovel and dagger, maps).
  - Unemployed and nitwits keep the vanilla gift (wheat seeds), babies the poppy.

## 7. Matcha Flavoured: reference

What the datapack does with villagers, and what ISO took from it.

| Mechanic | How it works | Decision for ISO |
|---|---|---|
| Trades per profession | 291 files; each level almost always shows the entire pool | Inspiration for the trades, 2 random per level (§6) |
| "Filler" trades | Black wool → emerald with `discard` modifier: they keep a level's trade set from being empty in 26.x | Not needed in 1.21.1 |
| No exhaustion | 272 trades out of 291 have 999 uses; `reputation_discount` 0 on 205 | Rejected: limited uses, rising prices (§5.3) |
| *Local Flavours* | Each employed villager (except fishermen) with XP > 0 gets one trade: 1 random food out of 42 → 1 emerald | Brought over with complex foods only (§6.16) |
| *Amnestic* | Craftable item: returns the villager to unemployed, level 1, 0 XP | Rejected: villagers are a bonus, not something to grind |
| *Beacon Kindling* | Campfire with hay and emerald summons the trader | Rejected: natural spawn only |
| Villages | `village_beta` ruins, rarer, no POIs, archaeology, haunted atmosphere | Idea brought over on vanilla structures, without the atmosphere (§4.1) |
| Outposts without respawn | Empty `spawn_overrides` | Brought over, with a one-time garrison (§4.2) |
| Pillagers and raids | No emeralds for pillagers; evokers 0-1 if killed by a player; patrols and raids untouched | Rare emerald drop kept (§5.2) |
| Emeralds from fishing | In the treasure, weight 72, 1-2 emeralds | Brought over, much rarer (§5.2) |
| Wandering trader | Buys discs (1-8 emeralds, 999 uses), sells village maps, a witch hut map and villager eggs | ITR direction with Matcha's ideas (§6.15) |
| *Asylum Seeker* | The trader sells villager eggs | Excluded |
| Villager schedule | `timeline/villager_schedule.json`, stretched over a 72000-tick day | Not applicable: ISO does not change the day length (`DayNightCycle` is only a multiplier for its own calculations) |
| *Warding Stone* | Regenerates allies and damages enemies within 24 blocks | Rejected: villagers will heal by eating (Enhanced AI), and the future 1.20.1 Beacon port will work on villagers |
| Hero of the Village gifts | One themed loot table per profession | Rewritten, scaling with the effect level (§6.17) |

Not transferable: the bundle (behind a feature flag in 1.21.1; ISO has the `pouch`), newer flowers and species, and all the pack's custom items (`poisonous_potato` with `item_model`).

## 8. Implementation proposal

To be carried out only after confirmation.

**Features** in `module/mobs/villager/`:
- `Villagers`, ported from ITR without `nitwitChance` (breeding only), `maxDiscount` and `preventCureDiscount` (no discounts).
  - Options: `alwaysConvertZombie` (default true), `lockTrades` (on: trades can never be rerolled), `clampNegativeDemand` (at `-maxUses`), positive demand decay factor (0.6), cure duration, convalescence (duration, no trading, Slowness), nitwit exclusion, Fire Resistance for structure zombie villagers.
  - To evaluate: `experienceToLevelUpMultiplier`.
  - Code: Local Flavours, advancement emeralds.
- **`VillagersCommon`** with `@ConfigType(ModConfig.Type.COMMON)`: the toggle of the integrated pack, because integrated packs are found before the SERVER config is loaded. The pack is enabled only when `Villagers` is.
- **Integrated `villagers_overhaul` pack** (`InsaneSO.addServerPack`):
  - the trades of every profession and of the wandering trader, with `remove: "all"`;
  - abandoned villages: template pool, processor and structure set overrides, archaeology and chest loot tables (§4.1);
  - outposts without respawn and their garrison (§4.2);
  - the emerald ore biome modifier (§4.4);
  - pillager loot (moved from `mob_loot_changes`) and the fishing loot modifier (§5.2);
  - Hero of the Village gift loot tables (§6.17).
- **Removed:** the `disable_long_noses` pack and `Packs.disableLongNosesStructures`; the `replaceZombieVillagersWithNormalZombies` option and its code.
- **Other changes:** the cure item tag; the recycling fixes of §5.4 (prerequisite); update of `PORTING.md`.

**Mixins** (`mixin/module/mobs/villager/`):

| Target | Purpose |
|---|---|
| `Villager.canBreed` | Disables breeding |
| `ZombieVillager.mobInteract` | Cure item from tag (golden carrot) |
| `ZombieVillager.mobInteract` (call to `startConverting`) | Configurable cure duration |
| `Zombie.killedEntity` | Conversion to zombie villager on every difficulty |
| `Villager.mobInteract` (or an event) | No trading during convalescence |
| `Villager.updateSpecialPrices` | Only negative gossip types raise prices; no cure or Hero discount |
| `MerchantOffer` (demand update) | Slower decay of positive demand, `clampNegativeDemand` |
| `GiveGiftToHero.throwGift` | Gift loot table by Hero of the Village level |
| `VillagerData` (accessor) | Level XP multiplier |

**Phases:**
1. **Trade system** (see its report). It can be released on its own and is tested by spawning villagers in creative with spawn eggs or commands, while villagers stay unobtainable in survival.
2. **Recycling fixes** (§5.4), before the diamond trades go live.
3. **Overhaul, all together:** villagers obtainable (no zombie replacement, conversion always on, golden carrot, cure and convalescence, no breeding, no nitwits), abandoned villages, outposts, mansions, emerald ore, fishing, pillager loot, advancement emeralds, and the final trades of every profession and of the wandering trader. Vanilla trades are never live.
4. Local Flavours and Hero of the Village gifts, in the same release or right after.

## 9. Decisions taken

**Villagers and the cure**
- Villagers only from curing; exceptions: igloos, rare zombie villagers in abandoned villages (§3.4, §4.1).
- `replaceZombieVillagersWithNormalZombies` removed; breeding disabled; nitwits become unemployed after the cure (§3.1, §3.3).
- Conversion to zombie villager always on; no despawn while converting (§3.1).
- Cure with the golden carrot (item tag); configurable duration; convalescence with no trading and Slowness (§3.2).
- Zombie sieges, raids and zombies left to the player (§3.5).

**World generation**
- One `villagers_overhaul` pack, replacing `disable_long_noses`, enabled only with the `Villagers` feature (§8).
- Villages all abandoned: rarer, rare fire-resistant zombie villagers, no POIs, almost no food, reduced chests, loot mostly from archaeology, no haunted atmosphere (§4.1).
- Outposts back without respawn, with a fixed garrison and captain (§4.2).
- Mansions back (§4.3).
- Emerald ore: an extra vein at 1/4 of the vanilla rate in every overworld biome, mountains included, on top of vanilla's mountain feature (§4.4).
- Villages: spacing 80 / separation 50; 15% zombie villager weight per villager slot (§4.1).
- Iron golem spawning kept: iron farms are already nerfed by `Nerfs.ironFromGolemsRequiresPlayer` (§3.5).

**Economy**
- Villagers only sell; *Local Flavours* and the wandering trader's few purchases are the exceptions (§5.1).
- Vanilla trades never live; single release (§5.1, §8).
- Pillagers 0-1 emerald, looting at most +1 at 33% per level; patrols and raids kept (§5.2).
- Emeralds from advancements with XP rewards, through the earn event (§5.2).
- Fishing emeralds through a loot modifier on `gameplay/fishing` (§5.2).
- The modpack drops `no_emeralds_datapack` (§5.2).
- Prices: base 1 emerald, `reputation_discount` 0.2, rise with demand and mistreatment, never drop below the base; positive demand decays at 0.6, also with time as in vanilla; negative demand kept (§5.3).
- Low uses per restock; special items 1 use (§5.3).
- Diamond equipment at level 5, 1 emerald + 75% of the recipe's diamonds rounded down; recycling fixes as prerequisite (§5.4).

**Trades**
- 2 random trades per level; `lockTrades` on; no Amnestic (§6.1, §7).
- Nothing enchanted except the librarian's books (§6.1).
- Random results fixed per offer; maps at 1 emerald + 1 empty map, split between trader and cartographer (§6.1).
- Tables of all 13 professions (§6.3-§6.14).
- Wandering trader: natural spawn, `common`/`uncommon`/`buying` pools with ITR's counts (§6.15).
- *Local Flavours* with complex foods, 1 use per restock (§6.16).
- Hero of the Village: no discount, gifts scaling with the effect level (§6.17).

**Tuning**
- No economy target: the report's values are a starting point, tuned by playtesting (§5.1).

**System**
- The trade system is analysed in its own report, with a format aligned to 26.1.

## 10. Open decisions

None at the analysis level. What is left is tuning by playtesting (quantities, uses, emerald amounts).

## 11. Limits of the analysis

- No in-game testing: the conclusions come from reading the 1.21.1 sources, the ITR 1.20.1 sources, ISO's code and data, and the datapack's files.
- Technical claims were checked by reading the sources and data of NeoForge 21.1.234, not in game; the villager slots per village are an estimate.
- The check on job site recipes is based on file names in `src`: they appear vanilla, except `smoker`, overridden by `no_food_in_furnace`. To be confirmed in game.
- It was not verified whether ISO's changes alter biome spawn weights.
- Raid behaviour in ISO has not been tested: patrols do not depend on outposts, and villagers become real targets.
- The modpack's changes are in their own report.
