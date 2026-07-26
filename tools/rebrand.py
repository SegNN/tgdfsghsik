#!/usr/bin/env python3
"""Final rebranding pass over the sources AND the live server configs.

  telegram links   t.me/Ficha*          -> t.me/ExampleTG
  handle           @FichaDev            -> @HolyDev
  leftover names   FichaDev/FichaHentai -> HolyDev
  mysql schemas    s7_FichaWorld{,2}, FichaLiteShop -> holydev
  plugin authors   FichaDev             -> HolyZako
  removed lines    "Leaked by ...", "Studio: ..."

NOT touched: ops.json / whitelist.json / usercache.json - the string "FichaDev" there is a
PLAYER NICKNAME bound to a UUID; renaming it would drop that account's operator status.
Folder names are left alone as requested.
"""
import os, re, sys

TARGETS = [
    "/home/fluge/Загрузки/AyuGram Desktop/FichaWorld-src/modules",
    "/home/fluge/Загрузки/AyuGram Desktop/FichaWorld v6.1 (1.21.4)/plugins",
]
DRY = "--dry" in sys.argv
TEXT_EXT = (".java", ".yml", ".yaml", ".txt", ".json", ".properties", ".sql", ".conf")
SKIP_FILES = {"ops.json", "whitelist.json", "usercache.json", "banned-players.json", "banned-ips.json"}
SKIP_DIRS = {"target", "libs", "translations"}

# order matters: longest / most specific first
SUBS = [
    (r"https?://t\.me/Ficha[A-Za-z]*", "https://t.me/ExampleTG"),
    (r"t\.me/[Ff]icha[A-Za-z]*", "t.me/ExampleTG"),
    (r"@[Ff]icha[Dd]ev", "@HolyDev"),
    (r"\bs7_FichaWorld2\b", "holydev"),
    (r"\bs7_FichaWorld\b", "holydev"),
    (r"\bFichaLiteShop\b", "holydev"),
    (r"\bFichaHentai\b", "HolyDev"),
    (r"\bFichaDev\b", "HolyDev"),
    (r"\bfichadev\b", "holydev"),
    (r"\bFICHADEV\b", "HOLYDEV"),
]
DROP_LINE = re.compile(r"leaked\s+by|^\s*#\s*Studio\s*:|^\s*#\s*Hentai\s*:", re.I)
AUTHOR_LINE = re.compile(r"^(\s*(?:-\s*)?)(HolyDev|FichaDev)\s*$")

stats = {"files": 0, "lines_dropped": 0, "authors": 0, "db": 0}

for base in TARGETS:
    for root, dirs, files in os.walk(base):
        dirs[:] = [d for d in dirs if d not in SKIP_DIRS]
        for fn in files:
            if fn in SKIP_FILES or not fn.endswith(TEXT_EXT):
                continue
            p = os.path.join(root, fn)
            try:
                src = open(p, encoding="utf-8", errors="ignore").read()
            except Exception:
                continue
            low = src.lower()
            if "ficha" not in low and "leaked by" not in low:
                continue

            out = []
            for line in src.splitlines(keepends=True):
                if DROP_LINE.search(line):
                    stats["lines_dropped"] += 1
                    continue
                before = line
                for pat, rep in SUBS:
                    if "database" in line and rep == "holydev":
                        stats["db"] += line.count("Ficha") + line.count("s7_")
                    line = re.sub(pat, rep, line)
                out.append(line)

            new = "".join(out)
            # plugin.yml author entries -> HolyZako
            if fn == "plugin.yml":
                fixed = []
                for line in new.splitlines(keepends=True):
                    m = AUTHOR_LINE.match(line.rstrip("\n"))
                    if m:
                        line = f"{m.group(1)}HolyZako\n"
                        stats["authors"] += 1
                    fixed.append(line)
                new = "".join(fixed)
                new = re.sub(r"^(author:\s*)(HolyDev|FichaDev)\s*$", r"\1HolyZako", new, flags=re.M)
                new = re.sub(r"^(authors:\s*\[)\s*(HolyDev|FichaDev)\s*(\])", r"\1HolyZako\3", new, flags=re.M)

            if new != src:
                stats["files"] += 1
                if not DRY:
                    open(p, "w", encoding="utf-8").write(new)

print(("DRY RUN\n" if DRY else "") +
      f"files changed   : {stats['files']}\n"
      f"lines removed   : {stats['lines_dropped']}  (Leaked by / Studio / Hentai headers)\n"
      f"author entries  : {stats['authors']} -> HolyZako\n"
      f"db references   : {stats['db']} -> holydev")
