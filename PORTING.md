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

- [ ] BoneMeal
- [ ] Crops
- [ ] Hoes
- [ ] Livestock
- [ ] PlantsGrowth

## Hunger & Health

- [x] Exhaustion
- [x] FoodDrinks
- [ ] HealthRegenHunger

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
- [ ] MiscItem
- [ ] Pouch
- [ ] RepairKits
- [ ] UnbreakableItems
- [ ] ExplosiveBarrel (ITE)
- [ ] Recall (ITE)
- [x] ~~Solarium~~ (ITE) No longer needed

## Mining

- [x] ~~Gold~~
- [x] MaterialsAndOres
- [x] MiningMisc
- [x] BlockDefinition
- [ ] BlockHardness
- [ ] BeegOreVeins (ITE)
- [x] ~~Durium~~ (ITE)
- [ ] Forging (ITE)
- [x] ~~Keego~~ (ITE)
- [ ] MiningCharge (ITE)
- [ ] MultiBlockFurnaces (ITE)
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

- [ ] BackwardsSlowdown
- [ ] BetterClimbable
- [ ] Boats
- [ ] ElytraNerf
- [x] ~~NoPillaring~~ No really liked
- [ ] Swimming
- [ ] Tagging
- [ ] TerrainSlowdown
- [ ] Minecarts
- [ ] WeightedArmor

## Sleep & Respawn

- [ ] Cloth
- [ ] Sleeping
- [ ] SleepingEffects
- [ ] Death
- [ ] Respawn
- [ ] RespawnPenalties
- [ ] Tiredness

## World

- [ ] Berries
- [ ] CyanFlower
- [x] Fluids
- [ ] Nether
- [ ] Sextant
- [ ] CoalFire
- [x] ~~ExplosionOverhaul~~ Standalone mod
- [x] ~~FasterDecayLeaves~~ Other mods do the same (and probably better)
- [ ] Seasons
- [ ] Spawners
- [ ] TimberTrees
- [ ] Weather