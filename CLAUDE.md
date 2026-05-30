# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Insane's Survival Overhaul (ISO)** is a Minecraft mod for NeoForge 1.21.1 (Java 21) that overhauls survival gameplay. It is being ported from older Forge versions of IguanaTweaksReborn (ITR) and IguanaTweaksExpanded (ITE). The porting checklist is tracked in `PORTING.md`.

- Mod ID: `insanesurvivaloverhaul`
- Dependencies: InsaneLib (custom library), MixinExtras, EvalEx (formula evaluation)
- Config folder: `config/insanesurvivaloverhaul/`

## Build Commands

```bash
# Build the mod jar
./gradlew build

# Run Minecraft client
./gradlew runClient

# Run Minecraft server (no GUI, uses run-server/ directory)
./gradlew runServer

# Run data generators (outputs to src/generated/resources/)
./gradlew runData

# Run game tests
./gradlew runGameTestServer
```

There are no unit tests — testing is done by running the game.

## Architecture

### Module/Feature System (from InsaneLib)

All gameplay changes are implemented as **Features** inside **Modules**. This is the core pattern:

- **Modules** (`ISOModules.java`) — top-level groupings registered in TOML config: `combat`, `farming`, `hunger_health`, `items`, `mining`, `misc`, `mobs`, `movement`, `world`. Client-side modules live in `ISOClientModules`.
- **Features** — classes annotated with `@LoadFeature(module = ISOModules.X)` that extend `Feature` or `JsonFeature`. Each feature auto-registers itself and its `@Config`-annotated fields into the config file. Features subscribe to NeoForge events via `@SubscribeEvent` methods on the class itself.
- **JsonFeature** — features that load additional configuration from JSON files in the config folder (e.g., `WeightedArmor` loads `enchantments_weights.json`). These can sync data to clients.

Feature check pattern: `Feature.isEnabled(MyFeature.class)` or `this.isEnabled()` inside a feature.

### Package Structure

```
module/
  combat/       — armor, crits, knockback, piercing, shields, bows, etc.
  farming/      — crops, bone meal, livestock, etc.
  hungerhealth/ — exhaustion, food/drinks, health regen
  items/        — disabled items, stack sizes, copper equipment, etc.
  mining/       — block definitions, materials/ores, mining misc
  misc/         — nerfs, packs (integrated data packs), tweaks
  mobs/         — mob equipment, spawning, zombie siege
  movement/     — terrain slowdown, weighted armor, climbable, swimming, etc.
  world/        — fluids, seasons, etc.

mixin/
  accessor/     — @Accessor mixins exposing private fields/methods
  module/       — gameplay mixins, mirroring the module/ structure

setup/          — ISORegistries (DeferredRegister wrappers), NetworkHandler, ClientSetup
data/generator/ — Data gen providers (tags, recipes, block states, item models)
```

### Mixins

- Accessor mixins in `mixin/accessor/` expose private Minecraft internals.
- Gameplay mixins in `mixin/module/` follow the naming convention `TargetClassMixin_FeatureName.java`.
- Mixin methods must be prefixed with `insanesurvivaloverhaul$` and have a javadoc explaining what they do.

### Integrated Data Packs

The `Packs` feature (`module/misc/Packs.java`) registers integrated resource/data packs from `src/main/resources/integrated_packs/`. These are optional packs bundled with the mod and conditionally enabled based on feature config (e.g., `disable_long_noses`, `crops`, `no_food_in_furnace`).

Packs are registered via `InsaneSO.addServerPack(...)` or `InsaneSO.addClientPack(...)`.

### Registries

`ISORegistries.java` holds all `DeferredRegister` instances. Two registers target the `minecraft` namespace (`MINECRAFT_ITEMS`, `MINECRAFT_ARMOR_MATERIALS`) to override vanilla entries.

### Data Generation

Run `./gradlew runData` to regenerate files into `src/generated/resources/`. Data providers are registered in `InsaneSO.gatherData()`. Generated files are committed to source control.

### Config

- Common config: `config/insanesurvivaloverhaul/common.toml`
- Client config: `config/insanesurvivaloverhaul/client.toml`
- JSON configs (per JsonFeature): `config/insanesurvivaloverhaul/<feature>/`

Don't write code unless prompted or confirmed to do.
The 1.20.1 version of the mod is in C:\Users\delvi\source\repos\Insane96\IguanaTweaksReborn\, whilest other mods, such as InsaneLib are in C:\Users\delvi\source\repos\Insane96\
If you need Minecraft/Neo code ask instead of going into a rabbit hole try to read it, so I can provide it.
Don't worry about imports, the IDE will sort them out.