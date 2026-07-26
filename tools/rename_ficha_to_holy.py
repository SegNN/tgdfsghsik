#!/usr/bin/env python3
"""Rename the Ficha* plugins to Holy*: module dirs, java packages, file names, plugin
names, permissions, commands and placeholders.

Deliberately NOT renamed, because these are external identifiers that exist outside this
codebase and renaming them would break a live link or a live database:
  - Telegram links / studio handle : t.me/FichaDev, t.me/FichaHentai, @FichaDev
  - MySQL database names           : s7_FichaWorld, s7_FichaWorld2
"""
import os, re, shutil, sys


def safe_move(src, dst):
    """shutil.move drops src INSIDE dst when dst is an existing dir - refuse instead."""
    if os.path.exists(dst):
        raise SystemExit(f"REFUSING: destination already exists: {dst}\n"
                         f"          clean the tree first (git clean -fdx modules)")
    shutil.move(src, dst)

WS = "/home/fluge/Загрузки/AyuGram Desktop/FichaWorld-src"
MODS = os.path.join(WS, "modules")
DRY = "--dry" in sys.argv

PROTECT = [
    # real links / branding - note these all carry a t.me/ or @ prefix, or a capital D,
    # so none of them can accidentally shadow the all-lowercase `ua.fichadev` package
    "t.me/FichaDev", "t.me/FichaHentai", "t.me/fichadev", "t.me/fichahentai",
    "@FichaDev", "@fichadev", "FichaDev", "FichaHentai",
    # live MySQL schemas
    "s7_FichaWorld2", "s7_FichaWorld",
]
SENTINEL = "\x00P{}\x00"
TEXT_EXT = (".java", ".yml", ".yaml", ".txt", ".sql", ".json", ".properties", ".xml")


def rename_text(s):
    saved = []
    for i, tok in enumerate(sorted(PROTECT, key=len, reverse=True)):
        if tok in s:
            mark = SENTINEL.format(i)
            s = s.replace(tok, mark)
            saved.append((mark, tok))
    s = s.replace("FICHA", "HOLY").replace("Ficha", "Holy").replace("ficha", "holy")
    for mark, tok in saved:
        s = s.replace(mark, tok)
    return s


stats = {"contents": 0, "files": 0, "dirs": 0, "modules": 0}

# ---- 1. contents first, while every path is still stable -------------------------
for root, dirs, files in os.walk(MODS):
    dirs[:] = [d for d in dirs if d != "target"]
    for fn in files:
        if not fn.endswith(TEXT_EXT):
            continue
        p = os.path.join(root, fn)
        try:
            s = open(p, encoding="utf-8", errors="ignore").read()
        except Exception:
            continue
        if "ficha" not in s.lower():
            continue
        new = rename_text(s)
        if new != s:
            stats["contents"] += 1
            if not DRY:
                open(p, "w", encoding="utf-8").write(new)

# ---- 2. file names --------------------------------------------------------------
for root, dirs, files in os.walk(MODS):
    dirs[:] = [d for d in dirs if d != "target"]
    for fn in files:
        if "ficha" not in fn.lower():
            continue
        new = rename_text(fn)
        if new != fn:
            stats["files"] += 1
            if not DRY:
                safe_move(os.path.join(root, fn), os.path.join(root, new))

# ---- 3. package dirs, deepest first so parents stay valid -----------------------
for root, dirs, _ in os.walk(MODS, topdown=False):
    if os.path.basename(root) == "target":
        continue
    for d in dirs:
        if d == "target" or "ficha" not in d.lower():
            continue
        new = rename_text(d)
        if new != d:
            stats["dirs"] += 1
            if not DRY:
                safe_move(os.path.join(root, d), os.path.join(root, new))

# ---- 4. module dirs -------------------------------------------------------------
renames = []
for d in sorted(os.listdir(MODS)):
    if d.startswith("Ficha"):
        new = "Holy" + d[len("Ficha"):]
        renames.append((d, new))
        stats["modules"] += 1
        if not DRY:
            safe_move(os.path.join(MODS, d), os.path.join(MODS, new))

print(("DRY RUN\n" if DRY else "") +
      f"contents rewritten : {stats['contents']}\n"
      f"files renamed      : {stats['files']}\n"
      f"package dirs       : {stats['dirs']}\n"
      f"modules            : {stats['modules']}")
for a, b in renames:
    print(f"    {a:24s} -> {b}")
