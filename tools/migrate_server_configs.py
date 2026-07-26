#!/usr/bin/env python3
"""Rename Ficha -> Holy inside the LIVE server's plugin configs.

Only text config files are touched. Anything that identifies something OUTSIDE this
codebase is protected, because renaming it would break a live link, a live MySQL schema
or a plugin that is not part of this build:

  t.me/FichaDev, t.me/FichaHentai, @FichaDev, fichadev  - links / studio handle
  s7_FichaWorld, s7_FichaWorld2, FichaLiteShop          - live MySQL database names
  fichapasslite                                          - plugin absent from this build
"""
import os, sys

ROOT = "/home/fluge/Загрузки/AyuGram Desktop/FichaWorld v6.1 (1.21.4)/plugins"
DRY = "--dry" in sys.argv

PROTECT = [
    "t.me/FichaDev", "t.me/FichaHentai", "t.me/fichadev", "t.me/fichahentai",
    "@FichaDev", "@fichadev", "FichaDev", "FichaHentai", "fichadev", "FICHADEV",
    "s7_FichaWorld2", "s7_FichaWorld", "FichaLiteShop",
    "fichapasslite", "FichaPassLite", "fichatrade", "FichaTrade",
]
SENTINEL = "\x00P{}\x00"
TEXT_EXT = (".yml", ".yaml", ".json", ".txt", ".properties", ".conf", ".ini", ".sql", ".log")
SKIP_DIRS = {"libs", "translations"}


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


changed, examples = 0, []
for root, dirs, files in os.walk(ROOT):
    dirs[:] = [d for d in dirs if d not in SKIP_DIRS]
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
        if new == s:
            continue
        changed += 1
        if len(examples) < 12:
            examples.append(os.path.relpath(p, ROOT))
        if not DRY:
            open(p, "w", encoding="utf-8").write(new)

print(("DRY RUN\n" if DRY else "") + f"config files rewritten: {changed}")
for e in examples:
    print(f"    {e}")
