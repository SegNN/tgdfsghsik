#!/usr/bin/env python3
"""Port 1.16.5 -> 1.21.4 registry/enum renames.

Every mapping below was verified against the real paper-api 1.21.4 jar.
Rewrites are keyed by the OWNING type, because the same simple name moved
differently in different registries:
    Enchantment.LUCK      -> LUCK_OF_THE_SEA      (PotionEffectType.LUCK still exists!)
    PotionEffectType.JUMP -> JUMP_BOOST
    PotionType.JUMP       -> LEAPING
    Particle.FIREWORKS_SPARK -> FIREWORK          (EntityType.FIREWORK -> FIREWORK_ROCKET)
"""
import os, re, sys, collections

WS = "/home/fluge/Загрузки/AyuGram Desktop/FichaWorld-src"
MODS = os.path.join(WS, "modules")

RENAMES = {
    "Enchantment": {                      # 1.20.5: renamed to vanilla ids
        "DURABILITY": "UNBREAKING",
        "LUCK": "LUCK_OF_THE_SEA",
        "LOOT_BONUS_MOBS": "LOOTING",
        "LOOT_BONUS_BLOCKS": "FORTUNE",
        "ARROW_INFINITE": "INFINITY",
        "ARROW_DAMAGE": "POWER",
        "ARROW_KNOCKBACK": "PUNCH",
        "ARROW_FIRE": "FLAME",
        "DAMAGE_ALL": "SHARPNESS",
        "DAMAGE_UNDEAD": "SMITE",
        "DAMAGE_ARTHROPODS": "BANE_OF_ARTHROPODS",
        "DIG_SPEED": "EFFICIENCY",
        "PROTECTION_ENVIRONMENTAL": "PROTECTION",
        "PROTECTION_FIRE": "FIRE_PROTECTION",
        "PROTECTION_FALL": "FEATHER_FALLING",
        "PROTECTION_EXPLOSIONS": "BLAST_PROTECTION",
        "PROTECTION_PROJECTILE": "PROJECTILE_PROTECTION",
        "OXYGEN": "RESPIRATION",
        "WATER_WORKER": "AQUA_AFFINITY",
        "DEPTH_STRIDER": "DEPTH_STRIDER",
        "FROST_WALKER": "FROST_WALKER",
        "BINDING_CURSE": "BINDING_CURSE",
        "VANISHING_CURSE": "VANISHING_CURSE",
        "SILK_TOUCH": "SILK_TOUCH",
        "FIRE_ASPECT": "FIRE_ASPECT",
        "LURE": "LURE",
        "MENDING": "MENDING",
        "SWEEPING_EDGE": "SWEEPING_EDGE",
        "SWEEPING": "SWEEPING_EDGE",
        "THORNS": "THORNS",
        "KNOCKBACK": "KNOCKBACK",
        "LOYALTY": "LOYALTY",
        "IMPALING": "IMPALING",
        "RIPTIDE": "RIPTIDE",
        "CHANNELING": "CHANNELING",
        "MULTISHOT": "MULTISHOT",
        "QUICK_CHARGE": "QUICK_CHARGE",
        "PIERCING": "PIERCING",
        "SOUL_SPEED": "SOUL_SPEED",
    },
    "PotionEffectType": {                 # 1.20.5: renamed to vanilla ids
        "FAST_DIGGING": "HASTE",
        "SLOW_DIGGING": "MINING_FATIGUE",
        "SLOW": "SLOWNESS",
        "JUMP": "JUMP_BOOST",
        "DAMAGE_RESISTANCE": "RESISTANCE",
        "INCREASE_DAMAGE": "STRENGTH",
        "HEAL": "INSTANT_HEALTH",
        "HARM": "INSTANT_DAMAGE",
        "CONFUSION": "NAUSEA",
        "UNLUCK": "UNLUCK",
    },
    "PotionType": {                       # 1.20.5: renamed to vanilla ids
        "SPEED": "SWIFTNESS",
        "INSTANT_HEAL": "HEALING",
        "INSTANT_DAMAGE": "HARMING",
        "REGEN": "REGENERATION",
        "JUMP": "LEAPING",
    },
    "Particle": {                         # 1.20.5: renamed to vanilla ids
        "REDSTONE": "DUST",
        "SPELL_WITCH": "WITCH",
        "SPELL_MOB": "ENTITY_EFFECT",
        "SPELL_INSTANT": "INSTANT_EFFECT",
        "SMOKE_LARGE": "LARGE_SMOKE",
        "SMOKE_NORMAL": "SMOKE",
        "FIREWORKS_SPARK": "FIREWORK",
        "EXPLOSION_NORMAL": "POOF",
        "EXPLOSION_LARGE": "EXPLOSION",
        "EXPLOSION_HUGE": "EXPLOSION_EMITTER",
        "VILLAGER_HAPPY": "HAPPY_VILLAGER",
        "VILLAGER_ANGRY": "ANGRY_VILLAGER",
        "WATER_DROP": "DRIPPING_WATER",
        "WATER_SPLASH": "SPLASH",
        "TOWN_AURA": "MYCELIUM",
        "CRIT_MAGIC": "ENCHANTED_HIT",
        "DRIP_LAVA": "DRIPPING_LAVA",
        "DRIP_WATER": "DRIPPING_WATER",
        "SNOWBALL": "ITEM_SNOWBALL",
        "SLIME": "ITEM_SLIME",
        "REDSTONE_DUST": "DUST",
    },
    "Attribute": {                        # 1.21.3: dropped the GENERIC_/HORSE_/ZOMBIE_ prefixes
        "GENERIC_MAX_HEALTH": "MAX_HEALTH",
        "GENERIC_MOVEMENT_SPEED": "MOVEMENT_SPEED",
        "GENERIC_ATTACK_SPEED": "ATTACK_SPEED",
        "GENERIC_ATTACK_DAMAGE": "ATTACK_DAMAGE",
        "GENERIC_ATTACK_KNOCKBACK": "ATTACK_KNOCKBACK",
        "GENERIC_ARMOR": "ARMOR",
        "GENERIC_ARMOR_TOUGHNESS": "ARMOR_TOUGHNESS",
        "GENERIC_KNOCKBACK_RESISTANCE": "KNOCKBACK_RESISTANCE",
        "GENERIC_LUCK": "LUCK",
        "GENERIC_FLYING_SPEED": "FLYING_SPEED",
        "GENERIC_FOLLOW_RANGE": "FOLLOW_RANGE",
        "GENERIC_MAX_ABSORPTION": "MAX_ABSORPTION",
        "HORSE_JUMP_STRENGTH": "JUMP_STRENGTH",
        "ZOMBIE_SPAWN_REINFORCEMENTS": "SPAWN_REINFORCEMENTS",
    },
    "EntityType": {
        "MUSHROOM_COW": "MOOSHROOM",
        "ENDER_CRYSTAL": "END_CRYSTAL",
        "FIREWORK": "FIREWORK_ROCKET",
        "LIGHTNING": "LIGHTNING_BOLT",
        "FISHING_HOOK": "FISHING_BOBBER",
        "LEASH_HITCH": "LEASH_KNOT",
        "SPLASH_POTION": "POTION",
        "DROPPED_ITEM": "ITEM",
        "PRIMED_TNT": "TNT",
        "MINECART_CHEST": "CHEST_MINECART",
        "MINECART_FURNACE": "FURNACE_MINECART",
        "MINECART_TNT": "TNT_MINECART",
        "MINECART_HOPPER": "HOPPER_MINECART",
        "MINECART_MOB_SPAWNER": "SPAWNER_MINECART",
        "MINECART_COMMAND": "COMMAND_BLOCK_MINECART",
    },
    "ItemFlag": {
        "HIDE_POTION_EFFECTS": "HIDE_ADDITIONAL_TOOLTIP",
    },
}

DRY = "--dry" in sys.argv
changes = collections.Counter()
files_changed = set()

for root, dirs, files in os.walk(MODS):
    if "/target/" in root:
        dirs[:] = [d for d in dirs if d != "target"]
        continue
    for fn in files:
        if not fn.endswith(".java"):
            continue
        p = os.path.join(root, fn)
        src = open(p, encoding="utf-8", errors="ignore").read()
        orig = src
        for owner, table in RENAMES.items():
            for old, new in table.items():
                if old == new:
                    continue
                pat = re.compile(r"\b" + owner + r"\s*\.\s*" + old + r"\b")
                src, n = pat.subn(f"{owner}.{new}", src)
                if n:
                    changes[f"{owner}.{old} -> {new}"] += n
        if src != orig:
            files_changed.add(p)
            if not DRY:
                open(p, "w", encoding="utf-8").write(src)

print(("DRY RUN: " if DRY else "") + f"{sum(changes.values())} replacements in {len(files_changed)} files\n")
for k, v in changes.most_common():
    print(f"  {v:4d}  {k}")
