# Changelog

* Slightly increase tiredness
* don't hide hunger bar when riding
* witches invisib in water
* Giant tall grass drops seeds
* duck breeding

## Upcoming
### Changes
* Increased Tiredness per level (75 -> 125)

## 4.5.7
### Additions
* You can now merge incompatible enchantments in the Anvil
  * As long as Insane's Survival Extra is installed
* Fixed [MC-93826](https://bugs.mojang.com/browse/MC-93826)

### Changes
* Minor advancements rework
  * Re-enabled vanilla advancements tab
* Lowered zombies follow range
* [Chalks](https://www.curseforge.com/minecraft/mc-mods/chalk) are no longer unbreakable
* Tridents can now be repaired with Elder Guardian Spike from Progressive bosses and Thrasher Tooth from Upgrade Aquatic
* Reduced anvils degradation chance (7.5% -> 5%)
* Overall slightly reduced repair material amount
* Changed yet again saturation modifier formula (~~saturation_modifier * 1.2~~ -> saturation_modifier * hunger / 7)
* Animals auto breed now runs every 30 seconds instead of 10. This should fix the animals breeding like crazy
  * Chance to breed has been increased back to 2%

### Fixes
* Wolves missing regeneration
* Environmental plum trees not falling

## 4.5.6
### Additions
* Spawners now play the fire charge sound when spawning mobs

### Changes
* Magic damage no longer bypasses invincibility frames
  * This should fix Pufferfishes instant kill
* Asset override can now be easily disabled via Resource Pack screen
  * The config option was removes as didn't work

### Fixes
* Empowered spawners still spawning mobs faster when disabled

## 4.5.5
### Changes
* You can now always eat glow berries
* Re-enabled villagers advancements

## 4.5.4
### Additions
* Pets buffs
  * Wolves now have 2x health, 20% more speed, 50% more attack damage and 50% less attack speed (affected only with Enhanced AI)
  * Wolves, Cats, Ocelots, Parrots and Foxes now heal 1 hp every ~10 seconds

### Changes
* Disabled Depth hardness
  * Was way too much with the harder ores. Deepslate already does the job
* Season spawning in winter has been toned down
  * Mob cap (85 -> 70 (like vanilla))
  * Despawn distance (96 -> 112)
* Reduced animals auto breed chance (2% -> 1% every 10 seconds)
* Increased shields cooldown to be on par with Shields+
* Mid-age animals now drop 2x loot instead of 1.5x
  * Loot increase with age now also affects sheep shearing

### Fixes
* Shortbow not being repairable
* Respawn point not set when too hungry to sleep

## 4.5.3
### Additions
* Death screen now shows the time since last death replacing the score
* Experimental no explosion resistance fluids
  * Makes waterlogged blocks destroyed by explosions
* Config options for animals aging
* Anvil repair data for Savage and Ravage Cleaver of Beheading and Mask of Dishonesty 

### Changes
* Animals now drop 50% more loot when adult and middle-aged
  * Was only Adult
* You now get more hunger consumed when tired on wake up
* Slowed down starve speed
* Savage and Ravage Cleavers now accept damaging enchantments
* Further reduced mobs enchanted equipment chance

## 4.5.2
### Additions
* HUD Infos now works even when looking at item frames
* Eating cakes now gives 30s of Speed and Haste

### Changes
* Deepers are now affected by mobs buffs data pack
* Living Flesh is now made with Fermented Spider Eyes

### Fixes
* Item tooltips showing above the item name
* Curpic torch and torch crafting recipe

## 4.5.1
### Changes
* Reduced zombies and skeletons enchanted equipment
  * Chance changed to 15% (30 in full moon) for hand and armor
  * Levels changed 1~15

### Fixes
* Server startup crash

## 4.5.0
### Additions
* Added Shortbow
  * A variant of the bow that's lighter letting you move faster when charging at the costs of less damage and velocity
* Charging a (cross)bow now consumes hunger

### Changes
* Crossbows now have 3 attack damage and 1 attack speed
  * Have fun
* Foods and Drinks
  * Changing the .json config or food formulas no longer requires a minecraft restart
  * Increased default saturation_multiplier (~~x1.1~~ -> x1.2)
  * Changes Food Stack size formula to account for the above
* Sugar canes can now grow in spring at lower speed
* Upgrade Aquatic Mulberry now grow slower without sunlight and will not grow in WINTER and SUMMER
* Sleeping now depletes saturation again

### Fixes
* Fixed No Sleep If Hungry not working

## 4.4.2
* Server crash fix

## 4.4.1
### Fixes
* Copper and durium tools for zombies having wrong ids
* Startup crash

## 4.4.0
### Additions
* Added Hud Infos
  * A new Client Feature to display infos on the hud as long as you have the items
* Customizable villager trades
  * Thanks to magic of data packs, you can now customize villager trades
  * By default, the librarian trades have been rebalanced with an integrated data pack
* Bred villagers now have 10% chance to be nitwits
* Fix for Villager Comfort
  * Villagers that never slept would not get the negative comfort
  * If the villager has no bed now counts as max negative comfort
* Added a new StackMaxDamageEvent to change item stacks durability
* Added a new EnchantmentBonusEfficiencyEvent to change efficiency before the other modifiers

### Changes
* Heavily increased animal spawn caps
* Chance for bone meal to fail to grow saplings reduced (~~20%~~ -> 10%)
* Reduced chance for mobs to have enchanted gear (~~30% (+15% with full moon)~~ -> 20% (+20% with full moon))
* Increased max exhaustion (~~4~~ -> 6)
* Reduced netherite efficiency (7 -> 6)
* Villagers Nerfs has been renamed to Villagers
* Wandering Traders and Villagers Nerfs have been moved to Mobs module
* Enchantment descriptions now use the same translation keys as Enchantment descriptions mod
  * Also this mod's Enchantment Descriptions are now disabled if the other mod is installed

### Fixes
* Item tooltips not working
* Being able to crouch block with broken shields
* Villager not getting their trades locked the first time a villager would get a job

## 4.3.3
### Additions
* Added MiscItem.Item Tooltips
  * If enabled, items in the `iguanatweaksreborn:has_tooltip` item tag will have tooltips added from the vanilla name translation string + .tooltip (.e.g item.minecraft.arrow.tooltip)

### Changes
* Reduced auto-breed chance (5% -> 2%)
* Reduced Iron Golems sight
* Bane of SSSSS and Smite now affect caverns and chasms, savage and ravage and pet cemetery mobs
* Reduced ravagers attack speed

### Fixes
* Missing Caverns and Chasms actual redstone components
* Missing Caverns and Chasms Ore smelting recipes
* Always convert villager to zombie not working
* Missing Caverns and chasms anvil repairs
* Tiredness accumulating in creative/spectator mode
* Living flesh recipe with cloth enabled

## 4.3.2
### Additions
* `iguanatweaksreborn:explosion_damage_multiplier` NBT tag for entities to increase/decrease explosion damage
* Villagers now heal every ~10 seconds

### Changes
* Upgrade Aquatic beachgrass no longer drops seeds
* Loose respawn is now calculated earlier 
  * In servers, players should no longer be placed for a second at the spawn point
* Caverns and Chasms Deepers explosion now pierce through blocks more and have reduced knockback
* Reduced saturation from rabbit stews (1.6 -> 0.8). Vanilla was 0.6
* Increased overall saturation from foods (+10%)
* Re-enabled zombie villagers
  * You can get your long nosed ~~slaves~~ friends back, but you need to cure them
  * Bad omen has also been re-enabled
* Vex nerf changed
  * ~~-4 attack damage~~ -> -10 HP

### Fixes
* AppleSkin saturation stopping at old hunger
* Zombies spawning with stone tools
* Hopefully fixed Enhanced AI config resetting at random

## 4.3.1Increased wall hit min speed to get damaged
### Fixes
* Silver ingots in Ore Smelting data pack
* Crash fix

## 4.3.0
### Additions
* Item Definitions now have a new field: `harvest_level`
  * With this, Gold feature no longer changes harvest level, instead it's done by item definition

### Changes
* Empowered spawners now drop treasure enchanted books
* Loose respawn now tries to respawn on land instead of water
* Animals dying now have a proper death message
  * '<entity> died of old age'
* Increased hunger from food
  * 80% -> 100% of original value
* Heavy fogs will fade to the next fog faster
  * Previously only the "lasting" part was reduced
* Increased Rabbit Stew's saturation
* Increased foods stack sizes
* Caverns and Chasms integration
  * More ores generate in specific biomes
  * Beacon, Timber Trees and Respawn Obelisk blocks and items
  * A few config changes
  * Item Definitions
    * Silver and Necromium Tools are good for combat
    * Silver armor gives x1.05 attack damage
    * Necromium armor has really high defense at the cost of durability and enchantabilty
    * Sanguine armor has low defense but life steal
* Modifiers in item definitions without a slot now pick the default from the item
  * So if the `slot` is omitted for e.g. a chestplate, it will automatically go to the `chest` slot

### Fixes
* Hardcore Torches
  * Missing soul torch
  * Missing torch with cloth if ISE wasn't installed

## 4.2.6
### Fixes
* Animals breeding like crazy automatically
  * Animals auto-breed chance was 102% instead of 2%. It's now 5%
* Respawn Obelisk effects being like beacon ones
* Atmospheric pressure plates recipes
* Environmental redstone components recipes

## 4.2.5
### Additions
* Arrow Stats.Piercing Crossbows
  * Crossbow arrows now have an innate piercing level
* Arrow Stats.Crossbow Inaccuracy
  * Increased crossbow accuracy (1 -> 0.2)

### Changes
* Auto breed chance is now in livestock data
  * So you can change chance per animal via data pack
* Fog and thunderstorm intensity now advance when sleeping
* Heavy fogs now prevent undead from burning in sunlight
* Crossbows arrow speed is now lower (~~3.5~~ -> 2.25)
* Decreased Bows accuracy (~~0.5~~ -> 1) but increased damage (~~x0.5~~ -> x0.65)
* You now need to sneak to overwrite your obelisk respawn point with a bed

### Fixes
* Compatibility with Quality Foods
* Auto breed not working on most mobs

## 4.2.4
* Fixed mod requiring Serene Seasons

## 4.2.3
### Additions
* Added Discrete Name Tag gamerule
  * Enabled by default, name tags will no longer show behind walls

### Changes
* Heavy Fog weathers are now rarer
* Reduced mobs speed in hard but increased in easy/normal
* Increased zombies follow range in easy/normal
* Reduced zombies overall damage by 25% and attack speed by 50%
* Armor protection increased
  * Damage reduction at 20 armor (~~60%~~ -> 75%)

### Removed
* Swing Through Grass
  * Use Cut Through by Fuzs, works much better

### Fixes
* Data pack errors (recipes and loot tables) when other mods were not present
* Flint shield durability wrong
* Compatibility with latest Serene Seasons

## 4.2.2
* Startup crash fix with Allurement

## 4.2.1
* Startup crash fix

## 4.2.0
### Additions
* Sprint reduction when hungry
  * Instead of not being able to sprint, when you're hungry the sprint speed will be reduced

### Changes
* Changed sweeping and rebalanced weapons
  * Sweep attack is no longer on swords, instead is now on hoes
  * Sweep area has been increased by 0.5
  * Swords % damage (~~-40%~~ -> REMOVED) and knockback (~~85%~~ -> 95%) have been increased
  * Hoes base attack speed (~~2.2~~ -> 2) and % damage (~~-60%~~ -> -70%) have been reduced, increased knockback (~~35%~~ -> 50%)
  * Shovels % damage increased (~~+20%~~ -> +30%)
  * Axes base damage (~~2~~ -> 2.5) and % damage (~~+50%~~ -> +60%) have been increased
* Rebalanced material stats
  * Golden Tools efficiency has been increased (9 -> 10)
  * Iron Tools efficiency has been decreased (4.5 -> 4)
  * Diamond Tools 
    * Efficiency has been increased (4 -> 5.5)
    * Base damage has been increased (2.5 -> 3)
    * % damage has been reduced (**[NEW]** -5%)
  * Netherite tools 
    * Efficiency has been increased (5 -> 7)
    * Enchantability has been fixed (5 -> 4)
    * Durability has been reduced (2031 -> 1731)
    * Base damage has been increased (2.5 -> 4)
    * % attack speed has been reduced (**[NEW]** -5%)
    * % damage has been reduced (**[NEW]** -5%)
* Reduced efficiency bonus (~~+25%~~ -> +20% per level)
* Increased overall armor by ~20%

## 4.1.2
### Additions
* Health Regen & Hunger.Min hunger for sprint
  * Minimum hunger for players to sprint, has been reduced to 4 from vanilla 7
* Tiredness.Tired Overlay
  * Enables/Disables the overlay with the tired effect
* Config options for raw food poison amplifier and duration
* Timber Trees support for Environmental and Atmospheric
* Message when you can't pillar up

### Changes
* Sandstone recipe now gives 2 instead of 1
* Bundle recipe is now vanilla one but with cloth (1 string + 1 cloth)
* Increased raw food poison effectiveness
* Added venison to raw foods

### Removed
* Shields+ shield definitions in Shields+ Compat Data pack
  * Shields+ has now the same stats as here

### Fixes
* Missing flint shield definition
* 3 blocks high trees not falling
* Respawning inside blocks

## 4.1.1
### Additions
* Food & Drinks.Combat Snapshot eating saturation
  * When eating food the saturation will not sum, instead will just be set to the food's saturation (if higher than the current)

### Changes
* Enabled back Sleeping Effects
  * Since no hunger is no longer in the mod, this is now enabled by default
  * Effects have been removed (can be added back via json), you cannot sleep if you're hungry (15 or less hunger)
  * Saturation is no longer taken into account when depleting hunger
  * You can no longer sleep if you have the Hunger effect (configurable)
* Respawn Hunger and Saturation have been expanded
  * Added a Min and Max hunger and saturation when respawning.  
    If on death the player's hunger and saturation were below or above these values they will be set to them
* Durability repaired when merging items in an is now reduced if the item is enchanted
* Bonus durability when merging items in an anvil is now applied to the final durability, not the item's max durability
* Swing Through Grass no longer prevents the block from breaking
* Enhanced Foggy weather
  * Heavy fogs will now last much less time
  * Lighter fogs now start slightly closer to the player
  * Silent hill fog is now thicker
  * Chunks should no longer be much visible when looking at them thought the sky
  * `/insanesurvivaloverhaul foggy_weather clear` is now `set` so you can set the fog you want
* Shiba now also die of old age
* Reduced explosion damage
* Food Hunger and Saturation modifier can now be changed with a formula instead of being a simple multiplier
* Minor rebalancements for the mobs buffs data pack
* Nostalgic Tweaks has fixed the block outline override so "No outline" has been reverted back to "Red outline with wrong tool"

### Fixes
* Buzzier Bees' Moobloom loot table being wrong in the Livestock Changes data pack

## 4.1.0
### Additions
* Knockback.Projectile Knockback
  * Reduces projectiles knockback
* Player Attribute modifiers
  * Via Json Config you can now add attribute modifiers to players (the existing configs have been moved to the json)
* Explosion Overhaul.Don't Stack drops
  * Drops will no longer stack if enabled
* Livestock changes support for environmental
* No Food In Furnace support for Environmental, Atmospheric and Upgrade Aquatic

### Changes
* Wolves, Horses and Llamas can now die of old age
* Reduced movement speed bonus of mobs with Mobs Buffs data pack
* Respawn obelisk spawn point can now be overwritten by beds
  * A config option has been added to enable back the old behaviour
* Changed how loose respawn finds a valid y level
  * Should increase respawn performance
* Misc Tweaks Data Pack
  * Lodestone are now crafted with Iron Ingots instead of Netherite

### Removed
* No Hunger
  * Moved to standalone mod

### Fixes
* Enchantments replaced by the mod's ones still show up somewhere when disabled
* Grave spawning even when no items in inventory
* Missing tooltip on Firestarter

## 4.0.0
This is a breaking version: configs and data packs have got a huge update.
### Additions
* Better Climbamble
  * Added 'Not on climbable when on ground'
    * Prevents slowdown when walking near ladders
  * Added 'Only climb with jump'
    * Lets you climb ladders only with space instead of when walking near it
* Moved new foods back here from Expanded
* Stats Buffs has been moved here from Expanded
* Enhanced AI has been moved here from Expanded
* Minecarts has been moved here from Expanded
* Cloth has been moved here from Expanded
* Tool Belt Integration has been moved here from Expanded
* Respawn Obelisk has been moved here from Expanded
* Coal & Charcoal has been moved here from Expanded in Coal & Fire
* Flint Expansion has been moved here from Expanded
  * Also Shields+ support if installed (not mandatory!)
* Shields+ Integration has been split here from Expanded
* Ported advancements from ITE

### Changes
* Changed red outline on wrong tool to no outline on wrong tool
* Tall Grass no longer grows in hot seasons during night time
* Fast foods min speed to eat has been reduced (0.75s -> 0.6s)
* Smokers now require soul sand/soil to craft
* Hoes
  * Can now scythe flowers
* Mobs can no longer fall from mount (configurable)
* Actual Redstone Component data pack
  * Doubled amount of buttons, pressure plates and levers from crafting
  * Added Cinch's Better Deepslate, Better Archeology and Upgrade Aquatic support
* Cows now drop more leather

### Config & Data Pack changes
* Items
  * Item stats have been renamed to Item Definitions (folder from `item_stats` to `item_definitions`)
  * Disabled Items has its own feature now
  * Unbreakable Items has its own feature now
  * Item definitions has its own feature now
  * Ecologic wood has some new config options
* Block Data has been renamed to Block definitions (folder from `block_data` to `block_definitions`)
* Hoes
  * Scythable blocks are now defined in the `iguanatweaksreborn:can_scythe` block tag
  * Moved hoes scythe radius to Item Definition
* Knockback
  * No weapon penalty and spam penalty are now two separate config options
  * Knockback multiplier has been moved Item Definitions
* Twins chance has been moved to data packs (livestock_data)
* Health Regen
  * Passive regen has been moved here from No Hunger
* Night Vision Fade out time is now configurable
* Brightness forcing is now more configurable
* Client
  * World border has its own feature now
  * Death has its own feature now
* Combat
  * Player stats has its own feature now
    * Mining range reduction moved from Mining Misc
  * Arrow stats has its own feature now
* Experience.Dropped Experience
  * Renamed from Experience.Experience
  * Milk and shear xp have been moved here from Livestock
* Anvils
  * Removed 'Reduced repair with other item if enchanted'
* Exhaustion
  * Renamed from 'Exhaustion increase'
* Health Regen
  * Renamed from 'Hunger Health Regen'
* Cleaned up some config descriptions

### Removed
* Removed stackable soups. Just use item definitions
* Removed hoes cooldown

### Fixes
* Fixed drowning too fast
* Fixed milking cows dropping no experience if cooldown was disabled

## 3.21.5
* Replaced enchantments no longer appear in the creative menu
* Fixed 'Remove skeletons from Fortresses' 
  * Config was working the other way around
  * When "enabled" skeletons would no longer spawn anywhere
* Fixed crash with 8 or more enchantments on an item

## 3.21.4
* Removed vanilla beacon from creative menu
* Fixed yet another crash with Thunderstorm Intensity

## 3.21.3
* Requires InsaneLib 1.15.1
* Hopefully fixed (again) crash with thunderstorms
* Fixed ITR's Luck and Feather Falling not being disabled even when "Enable enchantments" is disabled
* Possibly fixed a crash with SOL Valheim

## 3.21.2
* Added a way to raise the hotbar position
  * Since the Raised mod is not compatible I've done it myself
* Added "Deep Ocean Guardians"
  * Guardians can now spawn naturally in deep oceans, with half health compared to monument guardians
* Unfair One-shot effects are now configurable
* Possible fix for thunderstorm intensity crash
* Wither effect damage no longer stops the player when damaged
* Removed "Phantoms spawn in the end"
  * Didn't work and was just setting the insomnia game rule
* Fixed unable to disable "Collide with walls damage"
* Fixed disabling the Enchanting feature not disabling the mod's enchantments
  * Also added a config option to disable ITR's Fire Aspect and Knockback
* Fixed disabling peaceful hunger preventing players from consuming hunger
* Possibly fixed crash at startup with JCraft

## 3.21.1
* Changed animal caps in different seasons
  * In hot season animal cap is heavily increased
  * Still really low in colder seasons
* Foggy weathers now last more time (also changed default times from 1~20 to 5~30 minutes)
  * But more foggy weathers last less
* Added weather gamerules descriptions
* Fixed increased ore generation not applying correctly in lush caves

## 3.21.0
* Added Scute blocks
  * When baby turtles now grow the scute will be placed on the ground instead of dropping, this way it doesn't despawn
  * Can be disabled in `Tweaks`
* Gold innate Looting is now lvl II instead of I
* Wild crops now have only 50% chance to drop the crop, +75% if using a hoe
* Fixed toughness in armor rework formula
* A missing `return false` broke the entire BlockData system

## 3.20.2
* Halved mining speed when prone
  * Added `Prone mining speed multiplier` in `Nerfs`
* Change the movement speed when frozen. Increased from vanilla -0.05 to -0.10
  * Added `Frozen Movement Speed modifier` in `Tweaks`
* Increased Shovels knockback (0.8 -> 1)
* Tiredness
  * Fake sounds times to play is now configurable
  * Doubled the cooldown between fake mobs sound when tired
    * And made it configurable
* Graves are now less explosion resistant

## 3.20.1
* Fixed foggy weather overwriting Blindness

## 3.20.0
* Sweeping overhaul
  * Sweeping attack now deals full damage to all entities hit
  * Sweeping edge now increases the range of the sweep attack
* Swords stats changed to account for Sweeping overhaul
  * Base attack damage +0.5 -> +1
  * % attack damage 0% -> -40%
* Every shovel now deals 2x knockback instead of being based on the attack damage
* Fixed knockback reduction being called twice (not sure if caused problems actually lol)
* Unfair one-shot overhaul
  * Now plays an animation similar to the totem of undying when activated
  * Changed the sound to the level up one
* Wild crops are no longer insta-break and can be broken faster with hoes
* Updated Elytra Nerf
  * You no longer get pulled down outside the end, instead you'll encounter more air resistance
* Added `/iguanatweaks foggy_weather get` and `/iguanatweaks thunderstorm_intensity get` commands
* Added a fix for Global GameRules which enforces survival in hardcore worlds

## 3.19.0
* Added `World`>`Weather` Feature
  * Added Foggy Weather. Weather can now be foggy and slowly changes overtime 
  * Added Thunderstorm intensity, making thunderstorms have more chance to lightning strike when intensity is higher.
    Intensity varies every few minutes
* Entities now have 20% chance to fall from their mount
* Moved respawning Health/Hunger/Saturation to `Respawn` and reworked respawning health
  * Instead of respawning with the same amount, each death now makes the player respawn with 1/2/2 less health (based on difficulty), down to a cap of 10/10/6 health
* Slightly enhanced Third person camera
* Fixed `instant heal saturation threshold` behaving incorrectly in rare cases
* Fixed Serene Seasons being mandatory again

## 3.18.2
* Due to SereneSeasons mess, the mod is no longer compatible with SS past 9.0.0.46

## 3.18.1
* Doubled bows arrows accuracy
  * Also configurable
* Reduced armor enchantability by ~20%

## 3.18.0
* Critical is now an Attribute modifier
  * Players no longer have 5% chance to crit
  * `iguanatweaksreborn:critical_chance` defines the chance for a crit to happen (between 0 and 1), defaults to 0
  * `iguanatweaksreborn:critical_damage` defines the damage multiplier for a crit. Defaults to 0.5 (+50%)
* Anvil Repair
  * Repairing Items in an anvil now costs ~20% less
  * Quark's Forgotten Hat can now be partially repaired with hide (like leather armor)
* Tiredness fake sounds now play less times when less tired
* If crit arrows bonus damage is disabled then arrows no longer emit particles

## 3.17.0
* Breeding
  * Animals now have 50% chance to have twins
  * Animals now have 2% chance every 10 seconds to fall in love without food
* Experience
  * Experience from blocks config has been moved to BlockData
  * Fully Grown Crops now give 1~2 experience
  * Amethyst clusters now give 1~2 experience
  * Harvesting Honey or Honeycombs from smoked Hives now awards 5 to 10 experience 
  * Milking or stewing cows / mooshrooms now awards 2 to 5 experience
  * Shearing sheep now awards 1 to 3 xp
* Reduced health on respawn

## 3.16.2
* Fixed missing biome compass recipe
* Quark ashen trees now fall
* Fixed server startup crash

## 3.16.1
* Forgot to run datagen. Biome Compass now has a texture
  * Also added it to creative tab
* Fixed cyan flowers not in the minecraft:flowers item tag (so can now be used to breed bees)

## 3.16.0
* Added Biome Compass
  * Tells you in which biome you're in
* Hitting a mob more than 40 blocks away with an arrow now plays the ding sound
* Timber Trees now breaks some leaves before falling (35% for Quark's Trumpet trees, 10% the others)
* Campfires are now extinguished when it starts to rain
* Changed Efficiency and Unbreaking formulas
  * Efficiency bonus: ~~+12/30/60/102/156/222%~~ -> +25% per level 
  * Unbreaking: tool lasts ~~40/115/190/265/340/415%~~ -> 50% per level more
* Reduced items enchantability by ~20%
* Iron Ore now generates more in lush caves (together with taigas)
* Material buffs
  * Wooden tools durability: 112 -> 118
  * Iron Tools efficiency: 4 -> 4.5
  * Gold Tools Efficiency: 8 -> 9
  * Diamond Tools Efficiency: 3.5 -> 4
  * Netherite Tools Efficiency: 4 -> 5
* Falling blocks can now stack up to 6 blocks (instead of 8)
* Overtime heal now decays 5x slower. It's now configurable
* Fixed animals always failing to breed
* Fixed Autumnity's Foul Berry Bushes not dropping pips
* Fixed a bug where block below sea level would be 10x harder than intended

## 3.15.4
* Heavily reduced silverfish veins
* Added Client > Misc > `Remove Score`
* Timber Trees 
  * Now has a `tree_info.json` config, for more control over logs and leaves (e.g. azalea trees have two leaves blocks)
  * No longer works if the max Y level is lower than the broken block + 3
  * Falling logs now deal max 20 damage and falling leaves max 4
  * Increased performance
  * Still not perfect for big trees like blossoms from Quark
* Falling blocks can now stack up to 8 blocks (instead of 4)
* Fixed crash with LivestockData when Serene Seasons was not present

## 3.15.3
* No hunger
  * Heal from foods is now rounded to the nearest 0.5 (e.g. food like beetroots now always heal half a heart every 2 eaten)
  * Changed food descriptions as follows (was Snack, Meal and **Meal**)
    * Snack: foods that heal instantly, less than half a heart
    * Nosh: foods that heal instantly half a heart or more
    * Meal: foods that heal overtime, 4 or fewer hearts
    * Feast: foods that heal overtime, more than 4 hearts
* Armor re-balance
  * Each armor piece now gives different regenerating absorption values (like armor)
  * Knockback resistance has been doubled for every armor
  * Golden armor no longer slows down
  * Armor points have been rounded to the nearest 0.5
  * Fixed chainmail helmet having 1 less armor than intended
* Death
  * Killer's names now include the original mob's name
  * Fixed vindication vs killer not working if graves were disabled or keepInventory was enabled
* Client Misc: Added 'Third Person Death'
  * When you die, your camera will switch to third person
  * The death screen has been changed to hide the buttons instead of having them disabled
  * The buttons are hidden for 2 seconds
* Fixed missing death.attack.collideWithWall.player

## 3.15.2
* Fixed not being able to apply books to items if Allow Merging Items was false

## 3.15.1
* Requires InsaneLib 1.15.0
* Serene Seasons missing no longer crashes the game
  * Serene Seasons features are no longer loaded if SS it's missing
* Rain and thunder are no longer reset when sleeping (with Tiredness enabled)
  * Instead, they advance like time
* Fire spread speed multiplier is now a gamerule instead of a config option
* Monster spawners are now 3x harder to break
* Halved falling logs damage
* Decreased the min knockback strength to get damaged against walls
* Fixed falling trees not stacking properly in fluids
* Fixed horse jump bar overlapping with the health and armor bars

## 3.15.0
* Added (Misc > Tweaks) 'Collide with walls damage'
  * When the player collides with a wall, they will take damage based of this value
* Changed Armor formula again
  * Heavily increased damage reduction with low armor
* Lowered movement speed reduction of old animals (~~-40%~~ -> -30%)
* Moved Serene Seasons integration here (from IguanaTweaks Expanded)
  * Livestock living days now scale with new day length if changed
* Explosion particles are now more similar to pre-1.15
* Fixed non-living entities (tnt, falling blocks) not being pushed by explosions
* Brushes now require feathers to be repaired
* Increased back base ore generation (-50% -> -32%)
* Added an experimental config option to make entities float in water
  * You have to swim against the water
* Decreased shovels bonus attack damage (+30% -> +20%)
* Changed Timber Trees so trees with more sideway logs will still fall
* Fixed scythe breaking sounds (no longer plays the sound each time for every block)
* Fixed duplicated recipe names in EMI

## 3.14.9
* Entering the nether now takes 2.5s instead of 4s (configurable)
* Added config option to prevent equipment from dropping enchanted
* Added config option to disable item merging in anvil
* Lowered hardness of blocks below sea level (~~+64%~~ -> +50% at Y=0 and below)

## 3.14.8
* Added `iguanatweaks:disenchant` loot modifier that allows to disenchant items from loot
* Golden tools fortune, looting and harvest levels are now configurable
* Added config option to disable the Nether
* Fixed 'Power Enchantment Multiplier' set to false not working

## 3.14.7
* Changed Bow's Power formula
  * Power potency now scales with the arrow's damage
  * `base_damage + 0.5 + 0.5 * lvl` -> `base_damage + (base_damage * 0.2 * lvl)` 
* Changed armor formula
  * Now protects slightly more
  * Toughness has been added and reduces damage by 1.75% per toughness

## 3.14.6
* Some beacon effects now only unlock with more pyramid height 
* Added `/iguanatweaks get_treasure_enchantments_book` command
* Fixed wrong effects in beacon breaking it
* Fixed tiredness crash

## 3.14.5
* Fixed `iguanatweaksreborn:reduced_drop_no_player_kill_blacklist` not loading when progressive bosses wasn't installed
* Fixed mixin error when serene season was not installed
* Fixed startup crash

## 3.14.4
* Added entity type tag `iguanatweaksreborn:reduced_drop_no_player_kill_blacklist`
  * Prevents entities in this tag from not dropping stuff. Fixes drops from Progressive Bosses Wither
* Renamed item tag `iguanatweaksreborn:reduced_no_player_kill_drop_blacklist` to `iguanatweaksreborn:reduced_drop_no_player_kill_blacklist`

## 3.14.3
* Changed Tiredness fake sounds when tired
* Rowing boats now gives exhaustion to the player
* Sweet berry bushes now grow 2x slower in autumn
* Added a Seasons feature
  * Has one config option that changes Serene Season's config to disable seasonal crops as it's handled by Plants Growth
* Added Serene Seasons fix (from ITE)
  * Fixes ice not melting in some conditions (near rivers mostly)

## 3.14.2
* Requires InsaneLib 1.14.0
* Fixed trapdoors still hard to break

## 3.14.1
* Wooden trapdoors are now 33% less hard to break
* Fixed blocking explosion with shields dealing no knockback
* Fixed experimental flying block feature not making blocks fly
* Experimental flying blocks now have a cap on knockback
* Shields blocking explosion damage config option has been removed

## 3.14.0
* Added Prevent fast swim up with jump
  * Enabled by default prevents the player from swimming up really fast if holding the jump key
* Added Faster egg time
  * Chickens can no longer be bred, instead when fed they will make eggs faster for 15 minutes
* Changed `iguanatweaks:deathLoseItems` to `iguanatweaks:deathLoseItemsPercentage`
  * A percentage of the items in the inventory to lose on death
  * Also added `iguanatweaks:deathLoseItemsEnchanted` that if false will prevent loosing enchanted items
* Reworked Plants and Livestock Modifiers. This feature was almost completely broken, so I rewrote it.
  * Added `operation` that can be either "add" or "multiply"
  * Renamed `multiplier` to `modifier`
  * Removed `sheep_wool_growth_chance`, use modifiers to set it to a different value, defaults to 1 (100% chance to regrow wool)
  * Removed `cow_fluid_cooldown`, use modifiers to set it to a different value, defaults to 1200 seconds (20 minutes) changed through 
  * Removed `breeding_fail_chance`, use modifiers to set it to a different value, defaults to 0 (0% chance to fail breeding)
  * Merged `correct_biome` and `wrong_biome` into `matches_biome`
    * The modifier is applied if the current biome matches a biome in the list.  
      If `inverse` is set to true then the modifier will apply only if the current biome doesn't match any biome in the list
  * Renamed `iguanatweaksreborn:global` to `iguanatweaksreborn:true`
* With the above, the following are the new modifiers to Livestock
  * Breedable animals 
    * Take 40 minutes (2x more time) to grow from baby
    * Take 7/10/12.5/8.5 minutes (1.4/2/2.5/1.7x more time) to be able to be bred again in Summer though Spring
    * Have 50% chance to fail to breed a baby, increased by 20% if the animal's "old" and +20/-10/-20% chance in Winter/Spring/Summer
  * Sheep have 20% chance to fail to grow wool, decreased to 10/0% in Autumn/Winter
  * Cows have 20 minute cooldown before can be milked/"stewed" again. This time is increased by 10 minutes if the animal's "old". In Winter/Spring/Summer 10/-5/-10 minutes are added to the cooldown.
  * Chickens lay eggs every 12.5 minutes, increased by 2.5 minutes when "old". In Winter/Spring/Summer 2.5/-2/-4 minutes are added to the cooldown.
  * Villagers now take 15 minutes to grow from baby, can breed every 30 minutes and fail to breed a child 50% of the time
* With the changes to modifiers, some plants should grow faster under some conditions
* Fixed leaves decay being twice as fast as should have been
* Fixed `large_fern` missing from `tall_grass` block tag
* Fixed `Cave vines underground` not working
* Added names and descriptions to ITR GameRules

## 3.13.5
* Added a new game rule `iguanatweaksreborn:deathLoseItems`
  * Defaults to false, if true players' items will disappear on death
* Killers
  * Killer mobs no longer drop 4 times more items
  * Killer mobs bonus experience increased (+300% -> +400%)
  * Added a `iguanatweaksreborn:killer_blacklist` entity type tag that defines which entities will not become killers. Defaults to "#forge:bosses"
* Breaking leaves or logs now initiates leaves decay twice as fast
* Sheep now have higher chance to regrow wool in cold seasons instead of hot seasons
* Reduced red color of the red outline on blocks
* Livestock data's `breeding_fail_chance_modifiers` are now added to the base chance instead of multiplying it
* Renamed a few tags
* Fixed Custom Food Properties' effect chance not saved and loaded

## 3.13.4
* Ore Generation
  * Redstone now generates more in oceans instead of savannas
  * Copper now generates more in savannas instead of oceans
  * Coal now generates between 0 and ~~192~~ -> 128 in deserts

## 3.13.3
* Added Red block outline with wrong tool
  * If the tool in hand will make the block drop no items then the block outline will be green tinted
* Added water push strength and push even if there are no blocks nearby
* Berry bushes patches now have fewer bushes and no longer generate all fully grown
* Slightly increased coal, gold, iron and lapis generation
* Huge mushrooms now generate less in Dark Oak Forests
* Fixed mobs arrows being affected by arrows damage multiplier

## 3.13.2
* Smite now applies 1s (plus up to 0.5s per level) of Weakness II to undead creatures
* Increased wooden tools durability
* Regenerating absorption 
  * Added a new sound when damaged thanks to Betterworld_sjq instead of using the shield break sound effect
  * Fixed rendering incorrectly above 20
  * Fixed damaged sound playing even when blocking the damage (e.g. with shields)
* Fixed health regen overlay wrong positioning with regen absorption or more health

## 3.13.1
* Increased items base damage by 0.5 but reduced enchantments damage
  * Sharpness (+0.6 -> +0.5 per level)
  * Smite (+1.25 -> +1 per level)
  * Bane of SSSSS (+1.2 -> +1 per level)
* Fixed timber trees wrongly checking sideways logs making the tree fall again
* Fixed wrong ore generation for diamonds and lapis causing conflicts with other world gen mod

## 3.13.0
* Overhauled Ore generation
  * Reduced ore generation by ~55%
  * Ores now generate more in specific biomes
    * Coal generates more in Deserts
    * Copper generates more in Oceans
    * Iron generates more in Taigas
    * Emerald generates only on mountain (like vanilla), but more
    * Gold generates even more in Mesas (also starts generating slightly lower)
    * Diamonds generate more in Jungles
    * Redstone generates more in savannas
    * Lapis generate more in non-cold plains
  * Disabled [Ore Veins](https://minecraft.wiki/w/Ore_vein)
* Fog
  * Added fog like pre-1.7.2 which starts closer to player. Also fog starts even closer if rains
* Force darkness now sets darkness to 15% only if the video settings are higher
* Added gamerule to disable experience (also disables experience bar)
* Ported season plant growth modifier and livestock changes from ITE

## 3.12.2
* Startup crash fix

## 3.12.1
* Leaves fast decay no longer checks for `LeavesBlock` class, instead checks for `#minecraft:leaves` block tag
* Explosions 
  * Now always give a minimal knock up
  * Slightly changed particles 
* Knockback reduction now correctly reduces sprinting knockback
* Removed no hunger gamerule
  * Had many problems, still the no hunger enabled config is synced when a player joins 

## 3.12.0
* Removed Desire Paths
  * Moved to a standalone mod
* Added Peaceful Hunger config option
  * Hunger and health no longer regenerates when in peaceful

## 3.11.1
* Fixed stamina rendered incorrectly if the player had regenerating absorption

## 3.11.0
* Removed Stamina
  * Moved to a standalone mod
  * Vigour and anything related has been removed
* Mobs from spawners now only have 20% chance to drop equipment
* Foods that restore more than 8 hp now show 'Meal' in bold
* Heavily reduced healing from high food values (changed `Food Heal.Over Time` formula)
* Foods eating speed is now capped between 1s and 3.75s (was only capped to min 1s)
* Fixed (again) iron, copper and gold ores not dropping experience

## 3.10.6
* Tentative balance
  * Swords now have -0.25 reach and lower knockback, but the sweeping attack now works even when running or jumping and deals the same knockback as the mob hit 
  * Shovels now reach farther (and fixed damage: was 2.5 base damage instead of 1 base damage +30%)
  * Axes and Forge Hammers have increased knockback
  * Pickaxes have slightly more knockback
* Fixed Echo Lantern being insta-break
* Fixed chunks not being removed from force loaded when in another dimension when the player died and a grave broke
* Fixed armor rendering both left and right if No Hunger was disabled

## 3.10.5
* Added a config option to change decay speed of regenerating absorption
* Fixed iron, copper and gold ores not dropping experience
* Revert 'Mending and Allurement's Reforming now work for items in the Tool belt'
  * Doesn't work and wastes experience. Was also crashing the game

## 3.10.4
* Splash potions can now be thrown much farther
* Blocks are now less hard to break in the nether
* Mending and Allurement's Reforming now work for items in the Tool belt
* Desire paths no longer form if the player's sneaking
* Torches made with fire charge now require fewer sticks
* Fixed echo lantern dropping nothing
* Fixed piercing damage ignored if the entity had regenerating absorption

## 3.10.3
* Increased chance for chickens, cows and sheep to drop food with Looting (Luck)
* Reduced Pigs food drop with looting
* Reduced Golden Tools durability and increased efficiency
* Slightly enhanced Echo lantern texture
* Protection enchantment now also protects from Magic damage
* Redstone lamp now breaks faster with pickaxes
* Increased 'Force Darkness' brightness (0% -> 15%)
* Fixed stamina applying to spectators
* Fixed mobs with bows dealing almost no damage

## 3.10.2
* Unbreaking overhaul
  * Increased max Unbreaking level to 5
* Renaming an item in an anvil no longer consumes it
* By default, you no longer get slowed down when stamina locks
* You can now sleep in creative even if you're not tired
* Fixed skeletons still spawning in Yung's Better Fortresses

## 3.10.1
* Added Remove skeletons from Fortresses
  * Disables normal skeletons from spawning in Fortresses
* Healing potions now heal `3*2^level` instead of `2*2^level` per level (like pre-1.6.1)
* Slightly increased spawners delay when empowered and increased experience from broken spawners
* Farmers' Delight cakes and pies now stack to 1
* Fixed Plant growth modifiers not working properly
  * Fixes nether wart not growing and some plants growing slower than they should
* Fixed infinite arrows with Infinity and Allurement

## 3.10.0
* Added Echo Lantern
  * When placed in the world, monsters in a 64 block radius will not be able to spawn
  * Breaking it will only return some materials used to craft it
* Critical Hits
  * Critical hits now have a chance to happen on hit, instead of happening when jumping (5% chance)
  * ITE Critical enchantment has been moved here. Increases chance to crit by 10% per level and damage multiplier by 30%
  * Crit multiplier increased back to 50%
* Added ecologic wood
  * Wooden items now consume much less durability when used in sunlight
  * Reduced wooden items base durability (127 -> 97)
* Added Force darkness
  * Forces darkness ignoring the video setting
* Enchanted Netherite items now require much less netherite to be repaired
* Fixed "multiply_total" attribute modifiers showing incorrectly
* Fixed wither minions from Progressive Bosses always dropping the main hand item

## 3.9.11
* Regenerating absorption now has visual effects when regenerating and hurt
* Further reduced Ender Chests hardness
* Reduced witches potions drop (~~2\~4~~ -> 0\~4 potions per witch)
* Knockback multipliers are now configurable through json

## 3.9.10
* Reduced hoes base damage (~~2~~ -> 1.5)
* Sped up leaves decay (~~11.25~~ -> 8.25 average seconds to decay)
* Poisonous food now shows snack or meal in red instead of green
* Heavily reduced coal size on mountains and slightly reduced coal size underground

## 3.9.9
* Reduced Regenerating Absorption from armors by ~25% but increased regeneration speed (1 point every ~~5~~ -> 4 seconds)
* Rebalanced materials efficiencies (also in ITE)
  * Low tier tools efficiency has been increased (wood, iron, etc)
  * High tier tools efficiency has been lowered (diamond, netherite, etc)
* Netherite equipment, ingot and block is now immune to (almost) any in-world damage
* The `minecraft:enchant_randomly` function now replaces BoA with Bane of SSSSS and Looting, LotS and Fortune with Luck
* Fixed Regenerating absorption being misplaced when rendered on right

## 3.9.8
* Minecarts and Boats are now easier to break
* Sea lanterns now break faster with pickaxes
* Reduced enchanting levels for fished Fishing Rods and Bows
* Fixed grave not generating if wearing only a tool belt
* Fixed Item Stats and stack sizes applied on `/reload` instead of post reload, conflicting
* Fixed Enchantments changes conflicting with Quark
* Fixed protection enchantments incompatible with feather falling

## 3.9.7
* Increased Flint and Steel durability to 61
* Fixed crash on world creation when missing Supplementaries

## 3.9.6
* Foods now shows "Snack" if instantly heal or "Meal" if heal overtime
* Sleeping when Tired II or above now skips 1 more minute per level, but Tiredness is now always reset to 0
* Conduit power now decreases stamina consumption when swimming by 10%
* Sponges
  * Now have a lower chance to dry if touching another wet sponge
  * Now have 100% chance to get wet in rain (was 20%)
* Enchanted bows now require more strings to be repaired
* Elder guardians no longer drop sponges and have a higher chance to drop the tide trim
* Honey bottles now stack to 16
* Pufferfishes now stack again up to 64
* Reduced FD Wild crops found in a patch 
* Fixed Respiration Nerf working as a buff

## 3.9.5
* Food Health regen formulas changed
  * Reduced high hunger foods' healing by about 15%
  * Healing speed now scales with food's hunger, higher hunger means lower healing speed
* Turtle Helmets now give 45 seconds of water breathing instead of 10 seconds
* Nerfed Respiration enchantment
  * Every level "increases" oxygen by 50% instead of 100%
* Bone meal no longer fails to grow stuff in autumn and lowered chance to fail in winter
* Cave vines now grow slower above sea level or if they can see the skylight
* Wild Flax is now rarer
* Reduced breeding cooldown (15 minutes -> 12.5 minutes)
* Tiredness
  * Mining hand swing animation is now slightly slower only at Tired IV or more
  * Fixed tiredness overwriting the swing speed when mining
* Reduced Ender Chest hardness and blacklisted from hardness changes
* Honey bottles
  * No longer take 2 seconds to drink (should be ~1 second)
  * Reduced healing (1.5 -> 1)
* Reverted rich soil boost chance to default value
* Farmers Delight Nourishment to Vigour duration conversion is now 2x 
* Fixed Rich farmland not minable with shovels
* Fixed Farmers Delight Pies not healing
* Fixed crash when missing Farmer's Delight

## 3.9.4
* Reworked bone meal
  * With serene seasons installed bone meal can fail to grow plants based off season
    * This should have already been the case with ITE but didn't work
  * Fixed bone meal being wonky on Supplementaries and Farmers Delight crops
  * Farmland moisture no longer prevents bone meal from working
  * Growth age can now be configured min and max instead of having NERFED and SLIGHTLY
* Added Rice Seeds and Rooted Onion textures
* Fixed foods with high effectiveness not getting stack reduced
  * This should fix de-sync of stack sizes on server
* Revert 'Reduced bones from skeletons'

## 3.9.3
* Fixed anvil event no longer fired
* Nerfed Unbreaking (items last ~~50%/120%/190%/260%~~ -> 40%/110%/180%/250% more)
* Reduced Sweet Berry Meatballs and Glowgurt nutrition
* Reduced Bone Drops from skeletons
* Removed hoes cooldown when tilling
* Middle-clicking carrots, potatoes, onions and rice now gives the seed instead of the crop
* Fixed hoe stats not synced in multiplayer
* Farmer's Delight
  * Fixed various loot tables
  * Fixed and refactored advancements
  * Hopefully fixed Farmers' Delight wild carrots, potatoes and beetroots still generating
  * Can no longer smelt Farmers' Delight stuff in furnace with no_food_in_furnace data pack enabled
* Forgotten hat can now be repaired again with leather

## Beta 3.9.2
* Broken items now show a red durability bar
* Slight buff to unbreaking (items last ~~35%/105%/175%/245%~~ -> 50%/120%/190%/260% more)

## Beta 3.9.1
* Farmer's delight integration
  * Changed all knives recipes to use swords instead
* If berry good is present a new data pack is enabled
* No Hunger can now be disabled again from config (the game rule still exists)
* Update to latest InsaneLib

## Alpha 3.9.0
* Added Farmers delight integration
  * https://github.com/Insane96/IguanaTweaksReborn/wiki/Farmer's-Delight-integration
* Spawners
  * Are now deactivated when finish being empowered
  * Can be reactivated with the echo shard they drop
  * Increased spawn speed (every 25\~100 -> 20\~80 seconds)
* Items can now be repaired again with Nuggets and the cost with enchanted items has been adjusted to match ingots
* Increased Hoes base attack speed bonus (~~+15%~~ -> +30%)
* Possibly fixed chunks staying loaded after player second death
* Fixed damaging enchantments (e.g. Smite) applying to sweep attack
* Fixed overtime healing bar having wrong width

## 3.8.4
* Stamina now unlocks at 50% instead of 75%
* Explosions no longer knockback entities if they are not hurt (e.g. PvP)
* Reduced Vigour effectiveness
* Vigour effect no longer increases mining, movement and attack speed
* Mobs now only have 50% chance to drop equipment
* Increased explosion damage
* Fixed no hunger gamerule not synced to newly joined players
* Fixed Nether wart not growing outside the end
* Fixed rich farmland having no drop
* Fixed the mod crashing if no other mod with MixinExtras was installed
* Fixed mending nerf stopping Allurement Alleviating from working
* The mod is now compatible with other mods that use EvalEx

## 3.8.3
* Anvil Repairs are now shown in EMI
* Reduced Hoglin drops
* Halved Mending repair ratio
  * 1 xp = ~~1~~ -> 0.5 durability
* Trident
  * Now repairable only up to 25% with prismarine and 75% with Crystals
  * Fixed not being able to repair with Crystals
* Animals
  * Base chance to fail to breed reduced (~~70%~~ -> 60%)
  * Old animals no longer take more time to breed, instead fail to breed more often
* Reduced Vigour effectiveness (~~-25%/-40%/-55%~~ -> -25%/-35%/-45% at levels I/II/III)
* Reduced fortune yield from crops (~~25%/50%/75%/100%~~ -> 10%/25%/42.85%/62.5% chance to get another drop with (Fortune) Luck I/II/III/IV)
* Revert 'Reduced cow fluid cooldown from 30 minutes to 20'
* Fixed unbreakable tools sometimes not working
* Fixed regenerating absorption not wrapping after 40
* Fixed new flowers in flower pots
* Fixed explosion particles with Small explosions (e.g. fireballs)
* Fixed autumnity and Quark buttons/pressure plate missing redstone in the recipe

## 3.8.2
* You can now use an empty Name Tag on a mob to remove its name tag
* Livestock
  * Old animals now take more time to produce milk, eggs and to be able to be bred again
  * Reduced cow fluid cooldown from 30 minutes to 20
* Reduced Golden tools efficiency (8 -> 7)
* Reduced Quark's Foxhound and Crab, Autumnity's Turkey drops
* Fixed chickens' laying eggs after a looong time when grown from babies

## 3.8.1
* Reduced Efficiency enchantment bonus speed (coefficient: 8% -> 6%)
* Stats
  * Reduced Gold Efficiency (9 -> 8), Base AD (1.5 -> 1), Attack speed (+10% -> +5%)
  * Reduced Diamond Efficiency (5 -> 4.5)
  * Reduced Netherite Efficiency (6 -> 5)
  * Increased Iron Efficiency (3 -> 3.5)
  * Reduced Gold Armor Armor (Full set: 5 -> 4)
* Wandering Trader Bottle o Enchanting Trade nerfed (3 -> 6 emeralds, max 8 -> 4 uses)
* Items can no longer be repaired with nuggets
  * This made repairing items with nuggets too good when they were enchanted
* Timber Trees
  * Hopefully fixed leaves sometimes not decaying after falling
  * Hopefully fixed blocks being places on water

## 3.8.0 - The Aging animals update
* Ageing animals!
  * The biggest problem with animals is that as long as you have space, you can have infinite of them (especially chickens). With this, you can have infinite but will die eventually
  * After growing from babies, animals will now have a life span, based off the type of animal
  * Cows (and mooshrooms), Chickens, Sheep, Pigs, Rabbits, Quark's Crabs, Autumnity Snails and Turkeys can die of old age
  * Animals have 4 "stages": Young, Adult, Mid age and elder
    * Young animals are considered as such when below 25% of their lifespan. They have normal drops
    * Adult animals are considered as such when between 25%~50% of their lifespan. They have increased drops
    * Mid age animals are considered as such when between 50%~75% of their lifespan. They have normal drops
    * Elder animals are considered as such when above 75% of their lifespan. They have reduced drops and are slowed down
  * If pehkui is installed, animals will also visually grow
* Graves 
  * Now drops only ... the drops instead of breaking the grave
  * Also, as soon as the player dies the drops will drop instead when the chunk is loaded
    * This fixes graves staying in the world even if the owner died, but it's no longer online 
* Changed golden tools mining level from iron tools to stone tools (so gold tools can no longer mine diamonds, coal, emeralds, redstone and gold)
  * The high enchantability and the fact that can mine like an Iron Pick makes them too strong 
* Slightly increased Tiredness debuffs (2% -> 2.5%)
* Arrow nerf now works differently
  * You may ask "In which way it works differently?", well, I don't really know, I just know I've reworked the code and used Mixins, since the old way was not behaving properly
  * HOWEVER, I know for sure that arrow's damage is no longer rounded up to the nearest integer, so between arrows and quartz arrows (from ITE) the difference now actually exists
  * The arrow damage nerf is also applied to crossbows now, but crossbows shoot arrows with more velocity
* Reduced treasure fishing loot's level of enchanting (30 -> 25) and can no longer be enchanted with treasure enchantments
  * Was way too high giving OP items and too easy obtaining treasure enchantments
* Vigour 
  * Vigour effect no longer increases stamina regeneration and uses the formula from the enchantment
  * Re-added back to beacons
  * Fixed enchantment formula (-25%/-40%/-45% -> -25%/-40%/-55% consumption)
  * Fixed enchantment not applying to jumps
* Increased Hoes damage modifier (-50% -> -60%)
* Fixed enchantments info not showing on enchanted books
* Fixed feather falling being too rare
* Removed a leftover log

## 3.7.5
* Added `iguanatweaks:explosionMobGriefing` game rule
* Reduced golden tools efficiency and attack speed
* On Unfair One-shot activation the player's now given resistance for a few seconds
* Feather Falling now reduces fall distance by 1 per level
* Changed Endermen drops (1 (+0\~1 per looting level) ender pearls -> 0\~1 (+-1~1 per looting level) ender pearls)
* Fixed vigour preventing stamina from consuming

## 3.7.4
* Reduced Vigour enchantment strength at higher levels (was -25% consumption per level, now is -25%/-45%/-60% at levels I/II/III)
* Reduced axes base damage (3 -> 2)
* Dying when tired II or more now resets the tiredness to Tired II instead of I
* Changed a few tired sounds
* Reduced coal generation on mountains
* Armor damage reduction now scales slightly less
* Fixed beetroots being insta-minable

## 3.7.3
* Repairing enchanted items in an anvil with another item now has a penalty on durability
* Fixed durability not synced client side
* Fixed missing allurement enchantment descriptions

## 3.7.2
* Rebalanced tools
  * Hoes: Increased durability by 30% and attack speed (~10% more). Increased base attack damage to 2 but applied a 50% attack damage reduction to it 
  * Axes: Decreased durability by 30% and changed damage (base damage 5 -> 2.5; bonus damage 0% -> 50%)
  * Pickaxes: Decreased base damage (0.5 -> 0) but added +20% bonus damage. Piercing is now 1 for each pickaxe + an amount given by materials
    * Piercing Pickaxes feature has been changed to Piercing Attribute since Piercing damage is added via item_stats data pack
  * Shovels: Decreased base damage (2.5 -> 1) but added +35% bonus damage
  * Stone tools have +5% attack damage
  * With this, bonus damage enchantments now scale with the percentage modifiers
* Air is now consumed faster and the more you're drowning, the more damage you're dealt
* Wandering traders now sell maps to Trail Ruins
* Slight rebalance of foods (stack sizes, heal over time, instant heal and eating time)
* Trail Ruins suspicious gravel loot has been slightly changed
* Fixed piercing damage not respecting new attack cooldown calculations
* Fixed mending working as both the old and new mending
* Fixed BlockData changing the air block when the id is not valid

## 3.7.1
* Reworked enchantment tooltips
  * Now are shown below the enchantment
* Wandering traders enchanted books can now have Treasure enchantments
* Reverted Mending
  * Mending now works again like vanilla but nerfed: 1 XP = ~~2~~ 1 repair
* Fixed crash when milking cows in multiplayer
* Graves now save the death message when broken
  * Also Graves now drop when players die again
* Fixed mod overwriting the bypasses_cooldown tag
* Fixed Wandering Trades not reloading on /reload

## 3.7.0 - The BlockData update
* Added block data
  * In the `data/block_data` folder can now be placed JSON to modify blocks properties, such as hardness, explosion resistance, speed and jump factor and a few more
  * With this, harder crops, custom hardness and terrain slowdown has been removed and moved to a brand new data pack (block_data)
    * To make crops able to be broken faster by hoes (and not axes) they still must be added in the `iguanatweaksreborn:harder_crops` block tag
* Sponges
  * Wet Sponges now dry when exposed to sun and Sponges now become wet when exposed to rain 
* Graves
  * Graves now save the death message of the player and can be seen when right-clicked
* Tiredness
  * When tired, random sounds will be played to the player
  * Increased stamina consumed and lowered stamina regenerated with the Tired effect
* Mending overhaul changed
  * Player experience **when item's used** instead of repairing on picked up experience
* Client > Misc
  * Player camera no longer tilts when taking specific damage types
* Falling while in a boat will now always break the boat and passenger will take fall damage

## 3.6.2
* Eggs no longer stack to 64
* Minecarts, horse armors and saddles now stack less
* Fixed wrong enchantment values on many materials
* Fixed pips crafting from berry good

## 3.6.1
* Fixed crash when joining servers

## 3.6.0 - The Abnormals' Release Update
* Autumnity Integration!
  * Eating with the Foul Taste effect now increases heal by 50%
  * Eating turkey or pancake now heals
* Other minor integrations with Autumnity and Berry Good
* Mending overhaul!
  * Mending now consumes player experience every 2 seconds instead of picked up experience
* Changed fortune formula to be similar to Vanilla but with reduced average
  * From previous version: Fixed increase 30%/60%/90%/120% -> Average increase 20%/50%/85.7%/125%
  * From vanilla: Average increase 25%/75%/120%/167% -> Average increase 20%/50%/85.7%/125%
* If player's stamina is below 20 (2 hearts) and stamina is locked, the player is slowed down by 20%
* Knockback
  * Pickaxes, axes and ITE Forge Hammers now deal reduced knockback
  * Moved all knockback related stuff to knockback feature (removed Bonking Shovels)
* Repairing an enchanted item material's cost is now 5% of total levels of enchantments + 0.5 per levels of enchantments
* Repairing Leather armor now requires fewer rabbit hide but can only be repaired partially
* Equipment dropped by mobs max durability left is now 60%
* Reduced swords base damage (-0.5)
* Reduced smite bonus damage (+1.5 -> +1.25)
* Vigour enchantment is now stronger
* 1 block in the nether is now 4 blocks in the overworld
* Added a flag config option to wipe the Data Pack list of a world, so they are loaded correctly
* Added an option for No Hunger to display on foods the regeneration amount
* Infested blocks now generate in every biome (more in mountains)
* Graves can now be crafted
* Increased material repair cost when repairing enchanted items
* Increased tired debuffs amount (1% -> 2%)
* Increased backward slowdown (25% -> 30%)
* Chance to break glass now increases exponentially
* Added config option to move the regenerating absorption hearts to the right
* Fixed crops being harder to break with axes
* Fixed items being usable when broken
* Fixed breeding broken with ITE installed
* Nerfed hoes

## 3.5.6
* Fortune has been nerfed
  * From 25%/75%/120% drop increase to 30%/60%/90%
* Nerossii can now be crafted into purple dye
* Reduced coal vein size
* Reduced block hardness in the nether
* Conduits no longer deal damage to all entities in the area, but only the nearest
* Reverted "No pillaring no longer has an allowed distance"
  * But jump boost no longer increases the allowed distance
* Dying in the void no longer generates a grave
  * Damage type tag `iguanatweaksreborn:doesnt_spawn_grave`
* Slightly rebalanced hardcore loot
* Air refills faster after 4 seconds that you can breathe
* Crossbows can now be crafted again
* Glowstone can now be mined faster with pickaxes
* Frost walker now decreases slowdown more on ice
* Fixed missing solanum neorossii loot table
* Fixed animals not dropping cooked food

## 3.5.5
* Added Vigour enchantment
  * A new chestplate enchantment that reduces stamina consumption
* Repairing enchanted items in an anvil now costs more materials
* OverTime healing now decays based off exhaustion instead of every tick
* Supplementaries 
  * Flax is now hard to break and grows slower. Also no longer drops more than 1 seed
  * Soap is no longer affected by food stack sizes
* Terrain slowndown now affects entities
* Some items now always drop regardless of the player kill
  * Music Discs
  * More items can be added to the `iguanatweaksreborn:reduced_no_player_kill_drop_blacklist` item tag
* Rebalanced materials enchantability
  * Lower tiers have higher enchantability
* Reduced Arrows and Power strength
* Quark's Forgotten is now affected by Smite
* Added a `StaminaEvent.Consumed` and `StaminaEvent.Regenerated`, two events to change the amount of stamina consumed and regenerated
* Fixed stackable soups having a strange behaviour when eaten and inventory was full
* Fixed desire paths no longer working

## 3.5.4
* Spawners now drop 1 echo shard when finish empowered. Also reduced activation range
* Regenerating absorption now applies before damage reduction from armor and enchantments
* BoneMeal event is ignored if other mods have allowed or denied the use of bone meal
* Reduced Feather Falling cost
* Rebalanced some materials
* No pillaring no longer has an allowed distance
* Reduced spawners player range

## 3.5.3
* Moved chicken food items to item tag `iguanatweaksreborn:chicken_food_items`
  * Also fixed not being able to use any item on chickens
* Increased experience from experience bottles
* Regenerating absorption
  * Added sound effect when regen absorption hearts are damaged
  * Revert regenerating absorption base speed (0.25/s -> 0.2/s)
  * Increased time to not get damaged before regenerating (5s -> 7.5s)
* Armor
  * Changed again armor formula
    * Toughness has been removed from the calculation and damage reduction has been increased
  * Toughness has been replaced with Regenerating absorption
  * Rebalanced armors accordingly
* Item statistics now accept `regenerating_abosorption`, `regenerating_absorption_speed` and `movement_speed_penalty`
* Moved HardCrops feature to Crops
* Reduced Critical hits damage (25% -> 20%)
* Fixed missing Leather armor repair data
  * Leather armor can now be repaired either with leather or with rabbit hide (with lower cost)
* Fixed stack sizes not syncing properly on clients

## 3.5.2
* Players are no longer able to pillar **only if** there are monsters nearby
* Instant heal food now heal more
* Reduced the speed at which over heal decays
* Reduced Honey bottle healing
* Reduced flint and steel durability
* Items on which enchantments can go now are fully data packable
  * Item tags: `iguanatweaksreborn:enchanting/accepts_damage_enchantments`, `iguanatweaksreborn:enchanting/accepts_fire_aspect`, `iguanatweaksreborn:enchanting/accepts_knockback`, `iguanatweaksreborn:enchanting/accepts_luck`
* Reduced thorns damage
* Fixed hoes having too much Attack Damage
  * With this, attack speed has been reverted to 2.5
* Fixed repairing enchanted items with non-enchanted ones costing levels
  * Also can no longer merge broken items
* Fixed enchantments not scaling with attack cooldown

## 3.5.1
* Spawners no longer give loot but give more experience
* Added back the "You should really get some sleep" message when reaching Tired V
  * Also the overlay is no longer applied at Tired I
* Over time heal decay when full health is now affected by regen speed
  * So with lower regen speed, the decay is slower
* Damaging enchantments are now applied earlier for various checks to apply
* Heavily reduced knockback without a weapon
* The player no longer has -0.5 attack reach 
  * Weapons have been changed accordingly to have the same reach as before
* Better Climbable (aka Better Ladders) now disables itself if Quark is installed
* Removed Hard mode
* Removed TnT from creepers drops
* Broken items can no longer attack
* Added a new flower can be obtained by breaking wild potatoes
  * Also new textures for wild crops
* Fortune (or Luck) now affects crops drops
* Fixed hoes putting out fire in a radius
* Fixed drop_multiplier Loot modifier applying to unstackable items
* Fixed snowballs dealing reduced damage to blazes

## 3.5.0
* Reworked spawners
  * Spawners now generate empowered and will spawn mobs faster  
    After 24 mobs spawned the spawner will slowdown but still remain active and award experience and loot
  * Also increased the range for activation from 16 to 32
  * Spawners no longer disable themselves after various mobs spawned and no longer give bonus experience when broken
* Tiredness
  * Energy boost effect is now stronger
  * On death only one level worth of Tired is removed
  * Now renders a texture overlay instead of darkening the screen
  * Reduced attack speed, movement speed, mining speed and stamina reduction
* Hunger and health
  * Foods with saturation below 4 now heal instantly, while >= 4 heal only overtime. Heal over time is also kept when full health and only slowly decays
  * Disabled vigour effect
  * If you die, you now respawn with less health based off difficulty
  * No Hunger can now be enabled/disabled with a gamerule
* Tools no longer take 2 damage when hurting entities
* Reduced Hoes base attack speed (2.5 -> 2.4) and knockback (-60% -> -75%)
* Reduced trident attack speed (1.1 -> 1.0), durability (475 -> 315) and attack damage (8 -> 6)
* Unbreaking formula changed from +`50% * level` to +`35% * level + 35% * (level - 1)`
* Changed armor formula
  * Armor reduces damage by a percentage with diminishing return
  * Toughness reduces a flat amount of damage
* Item's stats are now ordered
* Moved IEnchantmentTooltip to InsaneLib
* Fixed Graves keeping vanishing curse items
* Fixed Serializable trades not reading item_a_tag
* Fixed Netherite armor having knockback resistance

## 3.4.2
* Coal veins are now rarer
* Better Loot data pack now disables itself if ITE is present

## 3.4.1
* Added Better Haste / Mining Fatigue
  * Removes the attack speed changes from these effects
* Halved Ancient Debris hardness
* Slowed down spawners
* Fixed chickens feedable with the wrong seeds

## 3.4.0
* Reworked armor and toughness
  * Armor now reduces damage by a percentage
  * Toughness reduces a flat amount of damage
  * Absorption armor has been disabled
  * Golden armor now gives +2 regenerating absorption and +20% regen absorption speed instead of +5 max health
* Wandering Traders can now sell Jungle temple maps
* "Can be repaired with: ..." in an Anvil now shows if a material only partially repairs the item
* Regenerating absorption effect no longer gives regen speed
* Reduced netherite enchantability (13 -> 7)
* Fixed netherite stuff having wrong repair amount for diamonds
* Fixed infinity overhaul broken?

## 3.3.1
* Added No fish if fishing in the same spot
  * If fishing too many times in the same spot you'll start getting nothing
  * Disabled Fishing Guardian
* Nether portals now require Crying Obsidian only instead of Gold blocks as alternative
  * Also made the blocks required configurable with the iguanatweaksreborn:portal_corners block tag

## 3.3.0
* Added Luck enchantment
  * Unifies Looting, Fortune and Luck of the Sea
* Rebalanced Heal over time and instant heal
  * Increased Instant heal
  * Slightly reduced heal over time amount and strength
* Increased Smite bonus damage from 1.25 to 1.5 per level
* Knockback enchantment now scales with weapon damage (like Smite like enchantments)
* If hit, Regenerating Absorption now goes on cooldown for 4 seconds instead of 2
* Players now lose 20% xp on death instead of 15%
* Bows and crossbows now have halved durability
* Broken items can now still attack with 1 damage and reduced knockback if the Knockback feature is enabled
* Leaves are now twice as dull
* Tools and weapons now show attack and mine reach
* Fixed crash with Crossroads mod

## 3.2.1
* Added `iguanatweaks:deathGrave` game rule
  * If enabled the grave will generate on death
  * Removed the config option to disable graves
* Coal veins are much much bigger now
* Fixed crash with Aphotheosis
  * When apotheosis is present, enchantment value of items from Item Stats will not be applied
* World border height is now capped to 128 blocks
* Fixed instant heal foods leaving behind a never ending arrow
* Fixed 'Cap to health' config option not working
* Fixed breakable items showing 0 durability left
* Fixed Regenerating Absorption not decaying when at 0

## 3.2.0
* Added Chicken from egg chance
* Added Player no damage when spamming
* Plants Growth multipliers and Livestock data have been moved to data packs
  * A data pack is included that adds the same multiplier as before
* Regenerating absorption no longer uses vanilla absorption
  * Instead, a new shield like icons will appear above the XP bar
* Lower saturation foods now instantly heal the full amount instead of having an over time amount
* Nether wart no longer grows in the overworld
* Fixed wandering traders crashing when disabled
* Fixed cows milk cooldown not synced to clients
* Reduced priority for EnchantmentHelperMixin. Should fix incompatibility with apotheosis

## 3.1.13
* I hate client-server stuff

## 3.1.12
* Fixed enchantments not synced between client and server

## 3.1.11
* Fixed crash in multiplayer with Backwards slowdown

## 3.1.10
* Fork me

## 3.1.9
* Crash with ITE

## 3.1.8
* Totem of Undying now gives 5 seconds of Resistance IV
* Fixed fishing rods being usable when broken
* Fixed step sounds heard twice in multiplayer
* Fixed (hopefully and again) item stats not being synced when on server
* Fixed mod crashing the game before the dependencies screen

## 3.1.7
* Regenerating Absorption
  * Golden Apples now give regenerating absorption instead of absorption
  * Effect now gives 10% more regen speed per level
  * Fixed regenerating absorption blinking with floating point value armor
* Minor fixes and balancements
* Sleeping effects is now disabled by default
* Obsidian is less hard to break

## 3.1.6
* Added `iguanatweaks:doLooseRespawn` game rule to disable the loose respawn mechanic

## 3.1.5
* Changed armor (or regen. absorption) for all armor to use decimal values
  * This makes the armor they give more accurate, so it's better to mix and match armor pieces
  * Also minor changes to other stats
* Changed efficiency formula yet again
  * From +50% efficiency per level to +16/40/80/136/208/296% from level I to VI
* Fixed Regenerating Absorption>Un-damaged time to regen not applying if only absorption hearts were damaged
* Budding amethyst are now harder to break
* Tired I no longer applies modifiers until II

## 3.1.4
* Hoes 
  * No longer have +0.5 attack range
  * All hoes are damaged by 4 durability when tilling (no longer reduced for higher tier materials)
  * Reduced cooldown when tilling
    * About -33% cooldown of a ITE hammer
* Protection enchantment now protects from any source outside the minecraft:bypasses_armor damage type tag
* Fixed missing "Phantoms in the End" feature
* Fixed iron tools having -10% attack speed

## 3.1.3
* Increased wooden and stone tools efficiency (1.5 -> 1.7 and 2 -> 2.2)
* Reduced berries from berry bushes
* Slightly increased health healed overtime from foods
* Chickens are now tempted by carrot seeds
* Fixed plants growth multiplier not synced to server

## 3.1.2
* Fixed item stats not syncing the first time a server is joined
* Fixed compatiblity with Paxi
* Fixed stamina overlay not jiggling together with health when low
* Fixed stamina not regenerating correctly when close to locked stamina

## 3.1.1
* Sweet berries can no longer be planted, instead bushes now drop seeds
  * Bone meal has now 50% chance to fail when used on sweet berry bushes
* Protection enchantment is now treasure
* Absorption is now affected only by damage types outside the minecraft:bypasses_armor damage type tag
* Added a config option to disable the replacing of damaging enchantments
* Further reduced Shovels base damage (3 -> 2.5)
* Ancient debris can no longer be smelted in furnaces
* Fixed 'Player's killer bounty' name changing before the player's death
* Fixed diamond and netherite armor having vanilla toughness
* Fixed respawn mobs despawn despawning persistent mobs
* Fixed over easy egg from ITE being cookable in a furnace
* Paxi and Global Data mods now should run after this mod's, allowing for overwriting changes from integrated packs

## 3.1.0
* Replaced vanilla weapon enchantments with the mod's ones
  * Bane of SSSSS, Smite and Sharpness bonus damages now scales with the weapon's damage (So a hoe with one of those enchantments will not obliterate monsters)
  * Also, every tool can now get weapon enchantments
  * Knockback and Fire Aspect have also been replaced
    * Fire Aspect now deals damage .5s later so the first fire tick is not lost
  * With this, sharpness has been re-enabled
  * Bonus damage scales with weapon attack damage (every 5 AD). Sharpness bonus damage is 0.75 per level, specific enchantments instead have 1.25 bonus damage per level.
* Replaced vanilla protection enchantments with mod's ones
  * Protection has been re-enabled, max 1 level protects for 6% of damage
  * All the specific protections now have the same chance to appear in the enchanting table and same rarity
  * Projectile protection now reduces monster sight
* Item Stats
  * Now accepts `enchantability` to change the item's enchantability
* Rebalanced all the armors
* If ITE is installed and Harder Torches Data Pack is enabled, torches can be made with Cloth
* Reduced axes base damage (6 -> 5)
* Reduced shovels base damage (3.5 -> 3)
* Added a few config options for Graves and Better Ladders
* Each level of frost walker now reduces slowdown on ice by 25%
* Fall damage now ignores invincibility frames
* Fixed Elytra and fishing rod having wrong durability
* Fixed Infinity overhaul lost in SR split
* Fixed Weighted equipment armor and toughness modifiers not working if Absorption armor is enabled
* Fixed crash with Tiredness and Guard villagers
* Renegerating absorption 
  * 'Un-damaged time to regen' increased (1.5s -> 2.5s) but is now affected by regen speed
  * Fixed regen absorption speed behaving strangely when entity lost health
* Fixed disabling wild crops data pack still preventing potatoes and carrots from planting 

## 3.0.5
* Regeneration in beacon costs much more time
* Beacon now accepts only stacks of 1 item
* Fixed chainmail armor having wrong stats
* Fixed first enchantment on armor being invisible
* Fixed Quark compatibility

## 3.0.4
* Added player's killer bounty
  * Mobs that kill players will not despawn and will drop 4x more experience and loot
* Fixed diamond generation backport not working
* Fixed Wandering Trader trades being not stackable

## 3.0.3
* Sleeping now adds 10 minutes to the daytime instead of skipping the day/night
* Leather and chained copper armor are no longer unbreakable
* Slight re-balance of Iron Tools
* Fixed copper and flint tools being unbreakable

## Beta 3.0.2
* Mobs now despawn in a radius (48 for world respawn and 24 for bed respawn) from the player respawn point
* Missing IguanaTweaks Expanded stuff
* Fixed advancement triggers not being registered
* Fixed items tooltips with modifiers in multiple slots showing all in the last slot

## Beta 3.0.1
* Added missing cyan recipe from cyan flower
* General clean up

## Beta 3.0.0
Please note this changelog is based off the latest Survival Reimagined version, and not 1.19.2 ITR version
* Item Stats
  * Now Items statistics are defined via Data Pack
    * `item`: id or item tag
    * `max_stack`: The max stack size of the item
    * `durability`
    * `efficiency` (only for tools)
    * `attack_damage`, `attack_speed`
    * `armor`, `armor_toughness`, `knockback_resistance` (armor only)
    * `modifiers`: A list of modifiers to apply to the tool (stolen from Stats)
* Removed `item_modifiers.json`. Check Item Stats
  * Moved `iguanatweaksreborn:remove_original_modifiers`
* Removed `custom_stack_sizes.json`. Check Item Stats
* Client
  * Added config option to disable the shorter world border
  * Fixed End music being changed even if the Sound feature was disabled
* Added the possibility to change what a pumpkin drops when sheared with the loot table `iguanatweaksreborn:pumpkin_shear`
* The delay between breaking blocks is now reduced with higher speed tools 
* Fixed Pickaxes being OP
  * Too much damage (literally double any other tool / weapon)
  * Piercing damage didn't scale with attack cooldown
* Merged Global, Block, Mobs and Other experience into a single Feature