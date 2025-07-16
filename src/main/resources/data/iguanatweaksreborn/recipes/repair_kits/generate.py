import json
import os
import re

INPUT_FILE = "kits.txt"
INPUT_PATH = os.path.abspath(INPUT_FILE)
OUTPUT_FOLDER = os.path.dirname(INPUT_PATH)

os.makedirs(OUTPUT_FOLDER, exist_ok=True)

def parse_line(line):
    parts = [p.strip().strip('"') for p in line.split(',')]
    if len(parts) < 5:
        raise ValueError("Riga troppo corta: " + line)

    key = parts[0]
    repair_material = parts[1]

    try:
        r, g, b = int(parts[2]), int(parts[3]), int(parts[4])
    except ValueError:
        raise ValueError(f"RGB non validi nella riga: {line}")

    color_int = (r << 16) + (g << 8) + b

    nbt = {
        "repair_material": f"\"{repair_material}\"",
        "color": str(color_int)
    }

    # Opzionali
    requires_mod = None
    extra = parts[5:]
    for p in extra:
        if "material_ratio:" in p:
            value = int(re.search(r'\d+', p).group())
            value = max(1, value)
            nbt["material_ratio"] = str(value)
        elif "max_repair:" in p:
            value = float(re.search(r'\d+(\.\d+)?', p).group())
            value = max(0.0, min(1.0, value))
            nbt["max_repair"] = str(value)
        elif p.startswith("requires_mod:"):
            _, mod = p.split("requires_mod:", 1)
            requires_mod = mod.strip().strip('"')

    return key, repair_material, nbt, requires_mod

def build_recipe_json(key, nbt, requires_mod):
    if key.startswith("#"):
        m_json = { "tag": key[1:] }
        key_suffix = key[1:].split(":")[-1]
    else:
        m_json = { "item": key }
        key_suffix = key.split(":")[-1]

    base_recipe = {
        "type": "minecraft:crafting_shaped",
        "pattern": ["MCM", " M "],
        "key": {
            "M": m_json,
            "C": { "item": "minecraft:amethyst_shard" }
        },
        "result": {
            "item": "iguanatweaksreborn:repair_kit",
            "nbt": "{" + ",".join(f"{k}:{v}" for k, v in nbt.items()) + "}"
        }
    }

    if requires_mod:
        final_recipe = {
            "type": "forge:conditional",
            "recipes": [
                {
                    "conditions": [
                        {
                            "type": "forge:mod_loaded",
                            "modid": requires_mod
                        }
                    ],
                    "recipe": base_recipe
                }
            ]
        }
    else:
        final_recipe = base_recipe

    filename = f"{key_suffix}_repair_kit.json"
    return filename, final_recipe

with open(INPUT_FILE, "r", encoding="utf-8") as f:
    for line in f:
        line = line.strip()
        if not line or line.startswith("#"):
            continue
        try:
            key, repair_material, nbt, requires_mod = parse_line(line)
            filename, recipe_json = build_recipe_json(key, nbt, requires_mod)
            path = os.path.join(OUTPUT_FOLDER, filename)
            with open(path, "w", encoding="utf-8") as out:
                json.dump(recipe_json, out, indent=2)
            print(f"[✔] Generated: {filename}")
        except Exception as e:
            print(f"[!] Row error: {line}\n    {e}")
