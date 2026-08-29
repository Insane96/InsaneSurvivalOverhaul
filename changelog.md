# Upcoming
* Added Stuck Arrows
  * Mobs hit with arrows will now have a chance to drop on death (only normal arrows)
  * I recommend adding a mod that shows stuck arrows on the mob
* Added Glow Block
  * A block crafted with amethyst, glow berries and a block of copper that can be seen through walls. Useful to align yourself with the world without coordinates.
* Tools and Weapons Rework
  * Added advancements giving infos on the weapon
  * Hoes are now slower but hit much harder per swing but are slower. Also durability has been decreased to match swords, pickaxes etc. Also increased knockback
  * Swords and Pickaxes now have similar DPS
  * Axes hit harder at higher tiers, keeping them ahead of Swords all the way to Netherite instead of falling behind
  * Pickaxes and Shovels now hit harder at higher tiers, so Netherite feels like a real upgrade over Wooden instead of a marginal one
  * Material attack speed bonuses/penalties are now percentage-based instead of a flat value, so they scale fairly regardless of a weapon's base speed
* Snow golems now drop the pumpkin on their head on death
* Split Sweep Overhaul to its own feature with many config options
* Repair merging in the crafting grid can now combine more than 2 items at once (up to 9, configurable via Tweaks)
* Fixed missing trees to craftable saplings

# 5.9.0.0
* Added Main Bed feature
  * You can crouch-right-click on a bed with an empty hand to set it as your main bed. If you lose the respawn point of another bed, your spawn point will be set to your main bed.  
    This is useful if you set your respawn point far away and then break the bed and when home you forget to set your respawn point there.
* Tiredness: added fake mobs step sounds
* Craftable Saplings
  * A new Data Pack that removes sapling drops from tree and makes them craftable from leaves
  * Removed Sapling Drop Fix data pack
* Death Max Health penalty now only works on extra hearts past 10, and reverted crafting recipe to 27 diamonds
* Renamed Max Health Death Penalty to Death Max Health Penalty
* Renamed Death Penalty to Death Inventory Penalty
* Moved some features to new Module: Respawn
* Possibly fixed game crashing when other mods try to add items to creative inventory which were removed with Creative Removal
* Fixed Golden Apple removal not in the food_changes data pack
* Moved Creative Removal to InsaneLib

# 5.8.0.1-beta
* Nerfed apple pie
* Fixed bone meal on dirt near grass not working with non light-blocking blocks above
* Bone meal on dirt now checks in the same range as grass spreading
* Fixed Max Health Death Penalty resetting on world join
* Fixed missing infos from runes
* Fixed Runes using wrong item tag
* Fixed regenerating absorption rendering on the wrong side

# 5.8.0.0-beta
* Tweaks
  * Added 'Ocelots to Cats'
  * Split into six standalone features, each individually toggleable/configurable: Sponges, Nausea, Breathing, Turtles, Collide With Walls and Painful World Border. Some have been moved to other modules
* Armor rework data pack
  * Increased Wolf Armor durability by 3x
* Added back Conduit feature: greatly increases the conduit's protection range and damage against nearby enemies, and can optionally remove Conduit Power's haste effect
* Moved the Item Tooltips feature to InsaneLib: the `has_tooltip` item tag is now `insanelib:has_tooltip` (was `insanesurvivaloverhaul:has_tooltip`), and it gained a companion `insanelib:has_hidden_tooltip` tag whose tooltip only shows while holding SHIFT
* Increased armor sprint slowdown in the Armor Rework data pack: Iron 15% -> 25%, Diamond 30% -> 45%, Netherite 50% -> 70%
* Added Critical enchantment (max level V) data pack: +10% critical chance and +5% critical damage per level, incompatible with other damage enchantments (Sharpness, Smite, Bane of Arthropods, Impaling, Density, Breach)
  * Includes Rune Enchanting's Rune version
* Added Armor Piercer enchantment (max level V) data pack: per level, grants bonus piercing damage scaled by the weapon's Attack Damage (0.2 every 5 points, configurable), so weaker/faster weapons like hoes get less bonus than swords or axes. Incompatible with other damage enchantments (Sharpness, Smite, Bane of Arthropods, Impaling, Density, Breach, Critical)
  * Includes Rune Enchanting's Rune version
* All tools, swords and the Trident now always show their Critical Chance/Damage and Piercing Damage in the tooltip (if different from the default value)
* Changed Crystal Heart's recipe to require 8 diamonds instead of 27
* Increased TnT output from 2 to 4
* Duplicated anvil recipes are now logged and skipped
* Fixed crash without Rune enchanting installed

# 5.7.4.0
* Nerfed hoes to the ground
  * They were way too powerful, DPS only was twice as powerful as the other tools.
* Increased Trident damage per hit to 6
* Reduced Pickaxe and Shovel attack range by 0.25
* Removed Trident's extra attack range bonus

# 5.7.3.0
* Potions' duration is now doubled
  * Configurable!
* Disable Long Noses data pack now also disables Woodland Mansions
* Fix rare possible crash on world join

# 5.7.2.0
* Increased tool and sword durability, with weaker materials gaining proportionally more (Golden excluded, remains fragile)
* Increased Trident durability to 912
* Fixed Respiration enchantment increasing air consumption instead of reducing it
* Ominous Trial Vaults now have a chance to drop a Divine Fragment (Heart of the Sea)
* Fixed Elder Guardian and Spawner Divine Fragment drops not working due to a mismatched loot modifier reference

# 5.7.1.0
* Removed 'Render on Right' in Regenerating Absorption
* Heart of the Sea is now renamed to Divine Fragment and retextured accordingly (resource pack, can be disabled)
* Mob loot changes data pack
  * Piglin Brutes now drop Netherite Scrap (65% + Looting) and a Divine Fragment (Heart of the Sea, 25% + Looting)
  * Endermen now always drop an Ender Pearl instead of a 50% chance
  * Bogged now also have a chance to drop Glowstone Dust and/or a Mushroom
* Spawners no longer drop the Crystal Heart, instead they always drop the Heart of the Sea (Divine Fragment)
* You can now reach 15 hearts with Crystal Hearts instead of 10
* Moved InCombat to InsaneLib
* Fixed Quark's chests not working to craft Shulker Box (Sack)

# 5.7.0.0-beta - The Matcha Flavored update
Thanks [Klei_Wright](https://www.youtube.com/watch?v=zyRH8W58fRI) for leaving the Matcha Flavored data pack with CC license. This update implements many ideas from the data pack, adapted to ISO.

* New feature 'Max Health Death Penalty'
  * Players now lose 1 max heart on death, down to min 3 hearts.
  * With a Heart of the Sea and 3 Diamond Blocks, craft a Crystal Heart (or find them by breaking spawners), to gain back lost health.
* Death Penalty
  * Players no longer lose part of the inventory, xp or durability. This feature is now disabled by default.
* Echo Pillar
  * Now disabled by default
* New Early Shulker Boxes data pack
  * Shulker Box recipe now uses Cloth instead of Shulker Shells
  * Shulkers now drop Shulker Boxes instead of Shulker Shells, 50% less often
  * Shulker Boxes are now renamed to Sack and retextured accordingly (resource pack, can be disabled)
  * Added an advancement after Cloth, awarded for getting a Sack
  * Removed Shulker Shell from the creative inventory
* Pouch
  * Now disabled by default
* Misc Tweaks data pack
  * Added Wool to String and Moss Carpet to Moss Block recipes
  * Moss Block to Carpet recipe now outputs 8 instead of 3
  * Banner recipes now output 3 instead of 1
  * Carpet recipes now output 8 instead of 3
  * Bricks recipe now outputs 2 instead of 1
  * TNT recipe now outputs 2 instead of 1 and also accepts Paper as an alternative to Sand/Red Sand
  * Target recipe now outputs 2 instead of 1
* New Prismarine Rework data pack
  * Prismarine is now made with Copper and Calcite, Dark Prismarine is easier to craft, and Sea Lantern out of Prismarine and Glowstone Dust
  * Guardians no longer drop Prismarine Shards/Crystals, they now drop Wet Sponge (if killed by a player) and (Cooked) Cod instead
  * Elder Guardians no longer drop Prismarine Shards/Crystals, they now roll the fishing loot table 3 times instead, still on top of the Tide Armor Trim Smithing Template (if killed by a player)
  * Sea Lantern no longer drops Prismarine Crystals when broken, it now always drops itself
  * Any remaining Prismarine Shard drop is now replaced with 40% as many Turtle Scutes, and any remaining Prismarine Crystals drop is now replaced with 40% as many Nautilus Shells
* Added back most advancements
* Mob spawners can no longer be reactivated with Echo Shards
* Glow berries now give Glowing effect for 5 seconds and can always be eaten
* Thrown tridents damage is now calculated based off the item thrown, instead of a fixed 8 (configurable)
* Lowered tridents damage
* Lowered Honey Nutrition
* Increased Wither Skeletons Head Drop chance (~~4% + 1%~~ -> 15% + 5% per looting)
* Dried kelp no longer gives nutrition
* Entities are no longer affected by discrete name tags
* Sweet berry bushes now grow faster in any cold biome, not only taigas
* Renamed Sprint Slowdown attribute to Sprint Speed
* Fixed burnt log minable with shovel instead of axe
* Fixed snowballs still stacking to 99 instead of 128
* Fixed iron tools (axe, pickaxe, shovel, hoe) missing their base critical damage reduction (-0.25), like swords already had
* Fixed Knockback's no-weapon/spam/projectile penalties not applying correctly on sweep attack hits (relies on InsaneLib's new `CurrentAttacker` fix)
* Fixed Ore Rocks showing flat/edge-on in the inventory instead of their normal angled view
* Probably more fixes I forgot to note

# 5.6.3.1
* Lowered default equipment drop chance for mobs spawned from spawners
* Fixed Respiration (oxygen_bonus attribute) not working
* Fixed horse riding consuming hunger

# 5.6.3.0
* Tiredness
  * Time skipped on sleep with Tired I is now 9h instead of 12h (still +3h per higher level of Tired)
    * Now configurable!

# 5.6.2.0
* Sprinting slowdown
  * Is now a -1\~0 value instead of 0\~1 value
  * Fixed not working as intended
* Lowered fungi stew nutrition

# 5.6.1.0
* Sprinting slowdown is now percentage based 0~1
  * With this, heavily increased the armor_reworks sprint slowdown
* Fixed Regenerating absorption and Hud Infos rendering with hidden UI
* Armor Rework
  * Knockback resistance reworked: removed from Iron and Chainmail, added to Diamond (5%) and Netherite (10%) per piece
  * Chainmail no longer grants knockback resistance; it now gives Regenerating Absorption instead
  * Retuned Regenerating Absorption on Leather, Iron and Chainmail; Copper's now matches Iron's
  * Significantly increased armor durability on all materials, with weaker materials gaining proportionally more durability than stronger ones

# 5.6.0.0-beta
* Added Recycling: damaged metal items can now be recycled in the Blast Furnace, giving back raw material proportional to their remaining durability
  * Also works with Shields+'s shields if installed
* Armor Rework's movement speed penalty now only applies while sprinting, via the new `insanesurvivaloverhaul:sprint_slowdown` attribute
* Parrots now only dismount if falling more than 4 blocks
* Item stacks are no longer limited to 99, thanks to InsaneLib, so now item stacks go up to 128
* Added textures for new foods
* Removed red and brown mushroom stews
  * You can craft normal mushroom stew with any mushroom (also quark's glow shrooms)
* Increased poison duration from Pufferfish Chowder
* Halved time it takes for blasting items
* Saturation is no longer capped to hunger
* Added item tags for attack sounds
* Naturally spawning guardians no longer spawn above a certain Y level and have less health and are smaller
* Fixed horse armors, minecarts and saddles not stacking
* Fixed unfair one-shot animation

# 5.5.1.2-alpha
* Lowered iron armor regenerating absorption
* `insanesurvivaloverhaul:no_stack_size_changes` has been split into `insanesurvivaloverhaul:no_block_stack_size_changes`, `insanesurvivaloverhaul:no_item_stack_size_changes` and `insanesurvivaloverhaul:no_food_stack_size_changes`
* Fixed missing copper nugget in creative inventory
* Fixed copper powered rail recipes
* Fixed golden carrots still edible

# 5.5.1.1-alpha
* Added name tag recipe to Misc Tweaks data pack
* Sweeping edge rune is now disabled with Sweeping Overhaul enabled
* Fixed power enchantment not working
* Fixed missing lang entries for new attributes

# 5.5.1.0-alpha
* Added Copper Nugget
* Added placeholder texture for new foods
* Tools and Weapons data pack now allow weapon runes (from Rune Enchanting mod) to be applied to pickaxes, shovels and hoes
* Saplings now grow slower
* Players can no longer place more than the items required to craft a recipe in the anvil
* Removed Quark's actual redstone components integration

# 5.5.0.2-alpha
* Fixed crash if items in anvil recipes are not registered
* Fixed crash with latest InsaneLib

# 5.5.0.1-alpha
* Fixed crash if items in anvil recipes are not registered

# 5.5.0.0-alpha - The Food update Part 1
* Food & Drinks
  * Added Apple Pie, Cod Chowder and Pufferfish chowder
  * Removed Golden Apples
  * Golden carrots are no longer edible
  * Reduced single mushroom stews and over easy eggs saturation
  * Netherized stews (renamed to Fungi Stew) now give more nutrition and effect has been changed (~~80% chance for 30s of poison~~ -> 45s of slowness and mining fatigue)
* Players now starve again at (~~1~~ -> 0) hunger, taking damage every (~~8~~ -> 5) seconds
* Shields+ recipes are now changed with equipment forging enabled
* Fixed rain not advancing on sleeping
* Fixed ghostly not synced in multiplayer
* Fixed anvil crafting not synced in multiplayer
* Fixed too tired message not showing anymore
* Hopefully fixed tired message spam

# 5.4.4.0
* Added a new Creative Removal feature
  * The `insanesurvivaloverhaul:creative_removal` item tag can be used to remove items from the creative inventory
* Undead now burn in daylight even if there's a block above them
* Enchanted items (and Runed items with Rune Enchanting mod) can no longer be used for anvil crafting
* Fixed Sunlight modifier not taking into account the day time (it only checked for sky light)

# 5.4.3.1
* Sprinting consumes much more hunger (5x)
  * Also heavily increased sprint jumping exhaustion
  * Reduced non-sprint jumping exhaustion
* Fixed disabling the Minecart feature still breaking other mods changes to minecarts

# 5.4.3.0
* Added Anvil Crafting
  * Allows adding crafting recipes to the anvil via data pack
  * Adds a data pack that makes so iron, gold and diamond equipment must be forged in an anvil, from copper, wood/leather and gold respectively
  * Includes EMI integration
* Added a new attribute for Exhaustion
  * `insanesurvivaloverhaul:exhaustion_multiplier`: Multiplies the exhaustion applied to the player. Defaults to 1.
  * Effective hunger now uses this attribute
* Lowered single mushroom stews saturation modifier
* Added back EMI integration
  * Yes, EMI good, I will support it only
* Fixed pouch tooltip being too high

# 5.4.2.2
* Tiredness
  * Fixed not being able to sleep during the day
  * Removed 'Should prevent spawn point' config option
* Fixed pumpkins not being able to be crafted to seeds

# 5.4.2.1
* Fixed iron taking not enough time to smelt

# 5.4.2.0
* Ghostly
  * Moved to its own feature
  * Players now render translucent
  * Increased duration by 30 seconds
* Disable stone tools feature now also disables stone shields from Shields+

# 5.4.1.2
* Sweet Berries Patches are now 3x rarer
* Fixed Nether Fog changes not working

# 5.4.1.1
* Ore rock now require the correct pickaxe tier to be mined
* Fixed setting insanesurvivaloverhaul:fire_speed_multiplier gamerule to 0 crashing the game
* Fixed melon dropping block without silk touch

# 5.4.1.0
* Tweaks
  * Added two Nausea features:
    * Hunger when eating: if nauseous, eating will apply hunger
    * Chance to fail to hit: if nauseous, there's a chance for an attack to miss
* Big ore veins are now twice as rare
* Fixed log spam when generating big ore veins

# 5.4.0.1
* Added bundle to creative inventory when cloth is enabled
* Fixed copper anvil repairs not working
* Fixed no ignite sound with two flints

# 5.4.0.0
* "New" feature: Sprinting
  * Split from 'Health Regen & Hunger'
* Renamed 'Health Regen & Hunger' to 'Hunger and health regen'
  * When in combat, health regen is now reduced by 80% for 10 seconds. In combat means taking or dealing damage.
  * Added 5 new attributes to control the health regen and hunger system
    * `insanesurvivaloverhaul:passive_regen_per_second`: How much health each second is regenerated without consuming hunger
    * `insanesurvivaloverhaul:regen_per_second`: How much health each second is regenerated by consuming hunger. Hunger is consumed at `insanesurvivaloverhaul:hunger_consumed` rate each half heart regenerated.
    * `insanesurvivaloverhaul:max_exhaustion`: How much exhaustion is required to consume hunger
    * `insanesurvivaloverhaul:hunger_consumed`: Hunger consumed per half heart healed. Decimal values are treated as chances to consume one more (e.g. this set to 0.5 means that there's 50% chance to consume one hunger)
    * `insanesurvivaloverhaul:hunger_required_to_regen`: How much hunger is required to regenerate health
* Foods
  * Now restores 20% more hunger (for foods that have enough to be increased, so e.g. berries have not been increased)
  * Now take more time to be eaten, and lower effectiveness foods take less
  * Eating speed now uses item components
  * Raw foods have been moved to a data pack with item components
    * The data pack also includes minor adjustments to foods, such as cookies, honey, pumpkin pie, rotten flesh and spider eyes. Also makes golden apples give regenerating absorption instead of absorption
* Farmable iron data pack is now disabled by default
* Fixed missing some lang entries
* Fixed possible lag caused by Echo Lanterns feature

# From 1.20.1
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
  * Mob loot changes
    * Looting extra drops have been reduced (+50% -> +33% per level)
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

# 5.3.1.0-beta
* Added back data packs 
  * Copper furnace
  * Hardcore torches
  * Misc Tweaks
    * Merged Cheaper chains in it
  * Actual Redstone Components
  * Mob loot changes
    * Looting is now less impactful
  * Sapling drop fix
  * Dark forest vegetation
  * Increase end cities
* Cake buffs now stack with diminishing returns, up to 2 minutes
* Fixed crossbows having no base attack damage and speed
* Fixed items keeping components when dropped on death

# 5.3.0.2-alpha
* Critical damage now once again defaults to 50%
* Updated burnt log texture
* Fixed ore rocks not broken faster with pickaxes and added advancement
* Fixed Echo Pillar advancement
* Fixed sweet berries loot table
* Fixed vanilla cutting the selected-slot highlight texture for ... reasons 
* Crash fix with latest InsaneLib

# 5.3.0.1-alpha
* Removed Sweeping Edge enchantment
* Snowballs now stack to 99
* Fixed Echo Pillar advancement
* Fixed unable to turn on campfires with two flints
* Fixed livestock loot tables
* Fixed many recipes and advancements having wrong folders thus not working

# 5.3.0.0-alpha
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

# 5.2.0.0-alpha
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

# 5.1.0.0-alpha
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

# 5.0.0.0-alpha
* Ported Knockback and Critical Hits features
  * Crit damage attribute is now 0 by default instead of 50%
