#!/usr/bin/env python3
"""
Generate item_components JSON files for tools and weapons.

Reads materials.csv and tools.csv, computes per-item attribute modifier values
by combining tool-type and material contributions, and writes one JSON per item
to ../data/insanesurvivaloverhaul/item_components/weapons/

Run from any directory:
    python scripts/generate.py
"""

import csv
import json
import os
from pathlib import Path

SCRIPT_DIR = Path(__file__).parent
OUTPUT_DIR = SCRIPT_DIR / "../data/insanesurvivaloverhaul/item_components/weapons"

# 1.21.1 NeoForge attribute ids
ATTR = {
    "atk_dmg":        "minecraft:generic.attack_damage",
    "atk_spd":        "minecraft:generic.attack_speed",
    "entity_reach":   "minecraft:player.entity_interaction_range",
    "atk_knockback":  "minecraft:generic.attack_knockback",
    "crit_chance":    "insanesurvivaloverhaul:critical_chance",
    "crit_damage":    "insanesurvivaloverhaul:critical_damage",
    "piercing_dmg":   "insanesurvivaloverhaul:piercing_damage",
}

MOD_ID = {
    "atk_dmg":        "minecraft:base_attack_damage",
    "atk_spd":        "minecraft:base_attack_speed",
    "entity_reach":   "minecraft:base_entity_reach",
    "atk_knockback":  "insanesurvivaloverhaul:weapon_attack_knockback",
    "crit_chance":    "insanesurvivaloverhaul:base_critical_chance",
    "crit_damage":    "insanesurvivaloverhaul:base_critical_damage",
    "piercing_dmg":   "insanesurvivaloverhaul:base_piercing_damage",
}


def fv(s):
    """Parse float from string, treating blank as 0.0."""
    s = s.strip()
    return float(s) if s else 0.0


def load_csv(path):
    with open(path, newline="") as fp:
        return list(csv.DictReader(fp))


def load_materials():
    return {r["material"]: r for r in load_csv(SCRIPT_DIR / "materials.csv")}


def load_tools():
    return load_csv(SCRIPT_DIR / "tools.csv")


def modifier(type_id, mod_id, amount, operation, slot="mainhand"):
    return {
        "type":      type_id,
        "id":        mod_id,
        "amount":    amount,
        "operation": operation,
        "slot":      slot,
    }


def build_tool_component(tool, mat):
    tool_tag = tool.get("tool_tag", "").strip()
    harvest_level_tag = mat.get("harvest_level_tag", "").strip()
    efficiency = mat.get("efficiency", "").strip()
    if not tool_tag or not harvest_level_tag or not efficiency:
        return None
    return {
        "rules": [
            {"blocks": f"#minecraft:incorrect_for_{harvest_level_tag}_tool", "correct_for_drops": False},
            {"blocks": f"#minecraft:{tool_tag}", "speed": float(efficiency), "correct_for_drops": True},
        ]
    }


def build_modifiers(tool, mat):
    mods = []

    atk_dmg = fv(tool["atk_dmg_base"]) + fv(mat["atk_dmg_add"]) + fv(mat["tier_atk_dmg_bonus"])
    if atk_dmg:
        mods.append(modifier(ATTR["atk_dmg"], MOD_ID["atk_dmg"], atk_dmg, "add_value"))

    atk_spd = fv(tool["atk_spd_modifier"]) + fv(mat["atk_spd_add"])
    if atk_spd:
        mods.append(modifier(ATTR["atk_spd"], MOD_ID["atk_spd"], atk_spd, "add_value"))

    entity_reach = fv(tool["entity_reach_add"])
    mods.append(modifier(ATTR["entity_reach"], MOD_ID["entity_reach"], entity_reach, "add_value"))

    atk_kb = fv(tool["atk_knockback_add"])
    if atk_kb:
        mods.append(modifier(ATTR["atk_knockback"], MOD_ID["atk_knockback"], atk_kb, "add_value"))

    # Always emitted (even at 0.0) so every weapon has a base modifier for the Critical
    # enchantment's bonus to merge into in the tooltip, instead of showing as a separate line.
    crit_chance = fv(tool["crit_chance_add"]) + fv(mat["crit_chance_add"])
    mods.append(modifier(ATTR["crit_chance"], MOD_ID["crit_chance"], crit_chance, "add_value"))

    crit_dmg = fv(tool["crit_dmg_add"])
    if tool["tool_type"] != "sword":
        crit_dmg += fv(mat.get("crit_dmg_add", ""))
    mods.append(modifier(ATTR["crit_damage"], MOD_ID["crit_damage"], crit_dmg, "add_value"))

    if tool["tool_type"] == "pickaxe":
        piercing = fv(mat.get("pickaxe_piercing_dmg", ""))
        if piercing:
            mods.append(modifier(ATTR["piercing_dmg"], MOD_ID["piercing_dmg"], piercing, "add_value"))

    return mods


def generate():
    os.makedirs(OUTPUT_DIR, exist_ok=True)
    materials = load_materials()
    tools = load_tools()
    written = 0

    for tool in tools:
        tool_type = tool["tool_type"]
        item_override = tool["item_override"]
        max_dmg_override = tool["max_damage_override"]
        tool_materials = [m for m in tool["materials"].split(";") if m]

        for material in tool_materials:
            mat = materials[material]
            item = item_override if item_override else f"minecraft:{material}_{tool_type}"
            dur_mult = fv(tool.get("durability_multiplier", "")) or 1.0
            max_damage = int(max_dmg_override) if max_dmg_override else int(round(fv(mat["max_damage"]) * dur_mult))
            mods = build_modifiers(tool, mat)
            tool_component = build_tool_component(tool, mat)

            enchantability = mat.get("enchantability", "").strip()

            components = {
                "minecraft:max_damage": max_damage,
                "minecraft:attribute_modifiers": {"modifiers": mods},
            }
            if tool_component:
                components["minecraft:tool"] = tool_component
            if enchantability:
                components["insanelib:enchantability"] = int(enchantability)

            data = {
                "item": item,
                "components": components,
            }

            filename = item.split(":")[-1] + ".json"
            out_path = OUTPUT_DIR / filename
            with open(out_path, "w") as fp:
                json.dump(data, fp, indent=4)
            print(f"  {filename}")
            written += 1

    print(f"\nDone — {written} files written to {OUTPUT_DIR.resolve()}")


if __name__ == "__main__":
    generate()
