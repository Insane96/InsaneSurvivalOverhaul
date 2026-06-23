# ISO Porting Checklist

Features to port from IguanaTweaksReborn (ITR) and IguanaTweaksExpanded (ITE) to ISO.

## Client

- [x] Death
- [x] Fog
- [x] Light
- [x] Misc
- [x] Sound
- [x] WorldBorder
- [x] HudInfos

## Combat

- [x] AbsorptionArmor
- [x] ArmorRework
- [x] AttackInvincibility
- [x] AttackSounds
- [x] Knockback
- [x] MiscStats
- [x] PiercingDamage
- [x] ~~PlayerStats~~ Split into PlayerAttributes (in InsaneLib) and no damage when spamming has been moved to Misc Combat
- [x] RegeneratingAbsorption
- [x] Shields
- [ ] ShieldsPlus
- [ ] ShieldsPlus (ITE)
- [x] Snowballs
- [x] UnfairOneShot
- [x] Bows
- [x] CriticalRework
- [ ] Fletching

## ~~Experience~~ Moving to new mod

- [x] ~~DroppedExperience~~
- [x] ~~PlayerExperience~~
- [x] ~~Anvils~~
- [x] ~~EnchantmentsFeature~~
- [x] ~~NewEnchantmentsFeature~~ (ITE)

## Farming

- [x] BoneMeal
- [x] Crops
- [x] Hoes
- [x] Livestock
- [x] PlantsGrowth

## Hunger & Health

- [x] Exhaustion
- [x] FoodDrinks
- [x] HealthRegenHunger

## Items

- [x] DisabledItems
- [x] EcologicWood
- [x] NameTags
- [x] StackSizes
- [x] StoneToolsGone
- [ ] Altimeter
- [ ] Blinker
- [x] CopperEquipment
- [ ] ~~FlintExpansion~~ Equipment is no longer needed and gravel can be _easily_ found for flint, so ...
- [x] MiscItem (renamed to ItemTooltips)
- [x] Pouch
- [ ] RepairKits
- [x] UnbreakableItems (Renamed to UnvanishableItems)
- [ ] ExplosiveBarrel (ITE)
- [ ] Recall (ITE)
- [x] ~~Solarium~~ (ITE) No longer needed

## Mining

- [x] ~~Gold~~
- [x] MaterialsAndOres
- [x] MiningMisc
- [x] BlockDefinition
- [ ] BlockHardness
- [x] BeegOreVeins (ITE)
- [x] ~~Durium~~ (ITE)
- [ ] Forging (ITE)
- [x] ~~Keego~~ (ITE)
- [ ] MiningCharge (ITE)
- [x] ~~MultiBlockFurnaces~~ (ITE)
- [x] ~~Quaron~~ (ITE)
- [x] ~~SoulSteel~~ (ITE)

## Misc

- [x] Nerfs
- [x] Packs
- [x] PotionsAndEffects
- [ ] BeaconConduit
- [x] ~~DeBuffs~~ Don't like how it works. Will be remade from scratch if someone needs it
- [x] LowFish
- [x] Tweaks

## Mobs

- [x] MiscMobs
- [x] ~~StatsBuffs~~ Moved to Enhanced AI
- [x] ZombieSiege
- [x] Equipment
- [x] Spawning
- [x] ~~Villagers~~ Data Packable Villager trades are painful to port, and they're coming in 26.1 anyway, and I will 100% just remove villagers from the modpack
- [x] ~~WanderingTrades~~ Same as above

## Movement

- [x] BackwardsSlowdown
- [x] BetterClimbable
- [x] Boats
- [x] ElytraNerf
- [x] ~~NoPillaring~~ Not really liked
- [x] Swimming
- [x] Tagging
- [x] TerrainSlowdown
- [x] Minecarts
- [x] WeightedArmor

## Sleep & Respawn

- [x] Cloth
- [x] ~~Sleeping~~ (unused)
- [x] ~~SleepingEffects~~ (unused)
- [x] Death
- [x] Respawn (Split into Echo Pillar and Loose Respawn)
- [x] RespawnPenalties
- [x] Tiredness

## World

- [x] Berries
- [x] CyanFlower
- [x] Fluids
- [x] Nether
- [x] ~~Sextant~~ (Maybe in the future)
- [x] CoalFire
- [x] ~~ExplosionOverhaul~~ Standalone mod
- [x] ~~FasterDecayLeaves~~ Other mods do the same (and probably better)
- [x] ~~Seasons~~ No more Seasons
- [x] Spawners
- [ ] ~~TimberTrees~~ (HC's TreeChop is good enough)
- [ ] ~~Weather~~