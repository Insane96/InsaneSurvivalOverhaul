# Tools and Weapons Rework — Script

Run `generate.py` to regenerate all JSONs in `../data/insanesurvivaloverhaul/item_components/weapons/`:
```
python scripts/generate.py
```

## materials.csv columns

| Column | Description |
|---|---|
| `material` | Material name, used to build item id (`minecraft:{material}_{tool_type}`) |
| `max_damage` | `minecraft:max_damage` value |
| `atk_dmg_add` | `add_value` contribution to `attack_damage` from the material |
| `atk_spd_add` | `add_multiplied_base` percentage contribution to `attack_speed` from the material, applied to the tool type's post-`atk_spd_modifier` speed (e.g. 0.375 = +37.5%) |
| `crit_chance_add` | `add_value` modifier for `insanesurvivaloverhaul:critical_chance` |

## tools.csv columns

| Column | Description |
|---|---|
| `tool_type` | Tool type name, used to build item id (`minecraft:{material}_{tool_type}`) |
| `materials` | Semicolon-separated list of materials (from `materials.csv`) to combine with |
| `item_override` | If set, used as item id instead of `minecraft:{material}_{tool_type}` |
| `max_damage_override` | If set, overrides the material's `max_damage` |
| `atk_dmg_base` | `add_value` contribution to `attack_damage` from the tool type |
| `atk_spd_modifier` | `add_value` contribution to `attack_speed` from the tool type (player base = 4.0, so -3.2 → 0.8 attacks/sec base) |
| `entity_reach_add` | `add_value` modifier for `minecraft:player.entity_interaction_range` |
| `atk_knockback_add` | `add_value` modifier for `minecraft:generic.attack_knockback` |
| `crit_chance_add` | `add_value` modifier for `insanesurvivaloverhaul:critical_chance` |
| `crit_dmg_add` | `add_value` modifier for `insanesurvivaloverhaul:critical_damage` |
