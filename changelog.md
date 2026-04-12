## Upcoming
* Added back Farming features: Crops, Bone Meal, Livestock, Hoes, Plants Growth
  * Rich farmland has been disabled by default
  * Crops can now grow with bone meal when farmland is not moist
* Added back Sleep features: Cloth, Tiredness
* Added back Death features: Death
  * On death the player will now lose 15% of durability on the items in the inventory and will drop 30% of their inventory items 
  * Removed Grave
* Added back Item features: Unvanishable Items (aka Unbreakable Items)
* Added back World features: Coal & Fire
* Exhaustion when jumping is now configurable
  * Jumping while sprinting now consumes much more hunger (2.5x)
* Added command to get and set food stats (nutrition, saturation and exhaustion)
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
* Ported Attack Speed Based Invincibility from ISO

## 5.0.0.0-alpha
* Ported Knockback and Critical Hits features
  * Crit damage attribute is now 0 by default instead of 50%