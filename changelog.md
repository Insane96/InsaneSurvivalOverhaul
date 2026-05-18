## From 1.20.1
* Combat
  * Unfair one-shot
    * Increased Resistance II duration after Unfair oneshot
  * Critical Rework
    * Enchantment is still missing
  * Bows
    * Now includes No Arrow Invincibility Frames
  * Snowballs
    * Slightly increased freeze time
    * Added 4 ticks cooldown
    * Now stack up to 64 with an integrated data pack thanks to InsaneLib's Item Components feature
  * Misc Combat
    * Renamed from "Misc Stats"
* Farming
  * Bone meal
    * Rich farmland has been disabled by default
    * Crops can now grow with bone meal when farmland is not moist
* Hunger & Health
  * Exhaustion
    * Jumping while sprinting now consumes much more hunger (2.5x)
* Items
  * Stack sizes
    * Items and blocks (not food) now stack up to 99 (max stack size is increased by ~54.7%)
  * Stone tools gone
    * Now also replaces items in entities equipment slots
  * Copper equipment
    * The items now registered in the minecraft namespace to be able to (hopefully) update a modded world to newer versions
  * Unvanishable Items (renamed from Unbreakable Items)
* Mining
  * Materials and Ores
    * Ore generation data pack changed to
      * Removes the vanilla feature of discarding ores that are exposed to air. This applies to coal, diamond, gold and lapis.
      * Biome-based ore generation has been removed. _Might_ come back in the future
    * Ore Smelting data pack changed to:
      * Smelting Raw Copper and Raw Iron takes 2x time
      * Blasting raw minerals takes 2x time
      * Blasting ores now takes 4x time but yields more materials from minerals (2x the normal drops without Fortune), and yields 3x raw minerals for iron, gold and copper
      * You can no longer smelt anything other than Raw Copper and Raw Iron. A Blast Furnace is required
      * Heavily increased experience from smelting ores
  * Misc
    * Insta-break heads and silverfish blocks are now part of the `block_data` data pack
  * Big Ore Veins
    * Ported from ISE
    * Now generate in a sphere, with more ores closer to the center
    * Gold, Copper and Iron now generate respectively in hot, temperate and cold biomes
    * No poor or rich ores
* Misc
  * Potion and Effects
    * Potion and Effects now enables a data pack that changes potions stack sizes
  * Tweaks
    * Turtle Scute can now be placed by players on the ground and pick-block can be used on it
    * Vehicles now require less damage to break (configurable, default is 2, vanilla is 4) (partially ported from Boats)
* Mobs
  * Mobs Buffs
    * MPR Integration data pack has been merged in Enhanced AI's MPR Integration data pack
* Movement
  * Minecarts
    * Minecarts with passengers and chests now force load chunks they are in, allowing them to travel through unloaded chunks
  * Boats
    * The feature (that wasn't configurable) to make boats easier to break has been moved to Tweaks as it affects vehicles in general
  * Better climbable
    * No longer disable themselves if quark is present
  * Elytra
    * Now reduces Firework Rockets strength 
    * Allows you to take off from the ground with fireworks without having to jump
    * Renamed from "Elytra Nerf"
* Sleep module has been split from Sleep & Respawn and contains Cloth and Tiredness features
* Death module has been split from Sleep & Respawn
  * Death Penalty
    * On death the player will now lose 15% of durability on the items in the inventory and will drop 30% of their inventory items. Will also drop 30% of their experience
  * Player killer bounty
    * Player killer bounty now accounts for multiple unique players killed, increasing the experience multiplier
  * Loose Respawn (Split from Respawn)
  * Echo Pillar (Split from Respawn, renamed from Respawn Obelisk)
    * Now 2 block tall
    * No longer stores the bed spawn point, so it works similarly to Respawn Anchors.
    * Prevents item loss when respawning.
    * Force loads the chunk it's activated in
* World
  * Coal & Fire
    * Burnt Logs now correctly fall like gravel and sand
* Added command to get and set food stats (nutrition, saturation and exhaustion)
* What happened to the other features? If a feature is not listed here, it's still in the "do I want to keep it or not" limbo
  * Graves: there are a sh*t ton of mods that do graves, corpses and more, so there's really no need. Plus the death penalties have been changed so it's really no longer needed.
  * Player Stats: No damage when spamming has been moved into Misc Combat. Player attributes instead have been moved to InsaneLib as they can work server-side only.
  * Experience module: moved to Insane's Experience Overhaul. Not sure about the future of that mod if I find some other mod that satisfies me in terms of Enchanting overhaul.
  * Flint expansion: flint tools weren't used anymore so have been discarded and gravel isn't that hard to find so ground flint has also been discarded
  * Gold: not really needed anymore. The good thing about gold equipment is the high enchantability. No need for fortune. Still need to change the mining level via data packs.
  * Solarium, Durium, etc.: Already disabled in 1.20.1, these only added further clunckyness to the system. I think something like Caverns and Chasms with Silver, Necormium and the other is enough. Will not be supported by the mod tho.
  * Debuffs: don't like how it works and I don't think it's used. If anyone needs it I will re-add it remaking it from scratch
  * Stats Buffs: Merged the MPR data pack to Enhanced AI
  * Villagers: Data Packable Villager trades are painful to port, and they're coming in 26.1 anyway, and I will 100% just remove villagers from the modpack I'm making
  * Wandering traders: same as above
  * No Pillaring: not liked and unused
  * Sleeping and sleeping effects: unused
  * Sextant: not needed anymore as was made for biome based ores
  * Explosion Overhaul: moved to standalone mod
  * Fast leaf decay: many mods already do it, probably better
  * Seasons: as much as I like seasons, I'm trying to keep the mod as vanilla and as other mods dependency free as possible
  * Timber trees: HC's TreeChop is good enough with a few config changes
  * Foggy Weather: foggy weather was quite neat, but not a fan of it anymore
  * Third-person death: was buggy and not that cool

## Upcoming
* Added back data packs 
  * Copper furnace
  * Hardcore torches
  * Misc Tweaks
    * Merged Cheaper chains in it
  * Actual Redstone Components
* Fixed crossbows having no base attack damage and speed
* Fixed items keeping components when dropped on death

## 5.3.0.2-alpha
* Critical damage now once again defaults to 50%
* Updated burnt log texture
* Fixed ore rocks not broken faster with pickaxes and added advancement
* Fixed Echo Pillar advancement
* Fixed sweet berries loot table
* Fixed vanilla cutting the selected-slot highlight texture for ... reasons 
* Crash fix with latest InsaneLib

## 5.3.0.1-alpha
* Removed Sweeping Edge enchantment
* Snowballs now stack to 99
* Fixed Echo Pillar advancement
* Fixed unable to turn on campfires with two flints
* Fixed livestock loot tables
* Fixed many recipes and advancements having wrong folders thus not working

## 5.3.0.0-alpha
* Added back Farming features: Crops, Bone Meal, Livestock, Hoes, Plants Growth
  * Bone meal
    * Rich farmland has been disabled by default
    * Crops can now grow with bone meal when farmland is not moist
* Added back Sleep (split from Sleep & Respawn) features: Cloth, Tiredness
* Added back Death (split from Sleep & Respawn) features: Death Penalty (renamed from Death), Player killer bounty (split from Death), Respawn Penalties
  * Death Penalty
    * On death the player will now lose 15% of durability on the items in the inventory and will drop 30% of their inventory items. Will also drop 30% of their experience
  * Player killer bounty
    * Player killer bounty now accounts for multiple unique players killed, increasing the experience multiplier
  * Removed Grave
* Added back Item features: Unvanishable Items (aka Unbreakable Items), Pouch, Item tooltips (renamed from Misc items)
* Added back Movement features: Minecarts
  * Minecarts with passengers and chests now forceload chunks they are in, allowing them to travel through unloaded chunks
* Added back Mining features: Big Ore Veins (from ISE)
* Added back World features: Coal & Fire, Cyan Flower, Berries, Nether, Thunderstorm Intensity
* Exhaustion when jumping is now configurable
  * Jumping while sprinting now consumes much more hunger (2.5x)
* Added command to get and set food stats (nutrition, saturation and exhaustion)
* Added back Armor Rework and Tools and Weapons Rework (split from Combat rework)
* Burnt Logs now correctly fall like gravel and sand
* Absorption armor is disabled by default
* Re-added Armor Rework formula
* Fixed Water Fall Damage feature not working properly

## 5.2.0.0-alpha
* Added back Mobs features: Equipment, Misc Mobs, Spawning, Zombie Siege
  * Not sure if and when Villager feature will come back.
* Added back Movement features: Boats, Better climbable, Tagging, Swimming, Terrain Slowdown, Elytra (renamed from Elytra Nerf), Backwards slowdown, Weighted Armor
  * The feature (that wasn't configurable) to make boats easier to break has been moved to Tweaks as it affects vehicles in general
  * Better climbing features no longer disable themselves if quark is present
  * Expanded Elytra Nerf to reduce Firework Rockets strength and allow to take off from ground with fireworks
* Added back Misc features: Packs, Nerfs, Tweaks, Potions and Effects, Low Fish
  * Potion and Effects now enables a data pack that changes potions stack sizes
  * Turtle Scute can now be placed by players on the ground and pick-block can be used on it
  * Vehicles now require less damage to break (configurable, default is 2, vanilla is 4)
* Added back Items features: Stack Sizes, Name Tags, Disabled Items, Stone Tools, Copper equipment gone and Ecologic Wood
  * Items and blocks (not food) now stack up to 99 (max stack size is increased by ~54.7%)
  * Stone tools gone now also replaces items in entities equipment slots
  * Copper items are now registered in the minecraft namespace to be able to (hopefully) update a modded world to newer versions
* Added back Mining features: Materials and Ores, Misc
  * Ore generation data pack changed to: removes the vanilla feature of discarding ores that are exposed to air. This applies to coal, diamond, gold and lapis.
    Biome based ore gen has been removed. _Might_ come back in the future
  * Ore Smelting data pack changed to:
    * Smelting Raw Copper and Raw Iron takes 2x time
    * Blasting raw minerals takes 2x time
    * Blasting ores now takes 4x time but yields more materials from minerals (2x the normal drops without Fortune), and yields 3x raw minerals for iron, gold and copper
    * You can no longer smelt anything other than Raw Copper and Raw Iron. A Blast Furnace is required
    * Heavily increased experience from smelting ores
  * Insta-break heads and silverfish blocks are now part of the `block_data` data pack
* Added back Hunger & Health features: Foods Drinks, Health Regen & Hunger, Exhaustion
* Added back World features: Fluids
* Mobs buffs data pack has been merged in Enhanced AI's MPR Integration data pack

## 5.1.0.0-alpha
* Added back Absorption Armor, Armor rework, regen absorption, Bows, Snowballs, Piercing Damage, Unfair oneshot and Misc Stats
  * Absorption armor is now enabled by default (for now)
  * Bows feature now includes No Arrow Invincibility Frames 
  * Snowballs
    * Slightly increased freeze time
    * Added 4 ticks cooldown
    * Now stack up to 64 with an integrated data pack thanks to InsaneLib's Item Components feature
  * Increased Resistance II duration after Unfair oneshot
  * Misc Stats is not complete yet, missing combat data pack and attribute tooltips
* Added back Client features: Hud Infos, Death, Fog, Light, Sounds & Music, Misc and World Border
  * Removed Third person death
* Ported Attack Speed Based Invincibility from InsaneLib

## 5.0.0.0-alpha
* Ported Knockback and Critical Hits features
  * Crit damage attribute is now 0 by default instead of 50%
