#!/usr/bin/env python3
"""Systemic post-decompile fixes:
 1. strip IntelliJ @NotNull `$$$reportNull$$$` bytecode instrumentation
 2. replace relocated shaded libraries with real Maven deps + shade relocation
"""
import os, re, shutil

WS = "/home/fluge/Загрузки/AyuGram Desktop/FichaWorld-src"
MODS = os.path.join(WS, "modules")

# ---------------------------------------------------------------- fix 1
def strip_report_null(path):
    src = open(path, encoding="utf-8", errors="ignore").read()
    if "$$$reportNull$$$" not in src:
        return False
    orig = src

    # remove the synthetic reporter methods entirely (brace-balanced)
    while True:
        m = re.search(r"[ \t]*private static void \$\$\$reportNull\$\$\$\d+\([^)]*\)\s*\{", src)
        if not m:
            break
        i, depth = m.end() - 1, 0
        while i < len(src):
            if src[i] == "{": depth += 1
            elif src[i] == "}":
                depth -= 1
                if depth == 0: break
            i += 1
        src = src[:m.start()] + src[i + 1:]

    # remove `if (<cond>) { ... $$$reportNull$$$N(..); ... }` guard blocks
    while True:
        m = re.search(r"[ \t]*if \([^)]*\)\s*\{", src)
        found = None
        for m in re.finditer(r"[ \t]*if \([^{)]*\)\s*\{", src):
            i, depth = m.end() - 1, 0
            while i < len(src):
                if src[i] == "{": depth += 1
                elif src[i] == "}":
                    depth -= 1
                    if depth == 0: break
                i += 1
            block = src[m.end():i]
            if "$$$reportNull$$$" in block and "if" not in block.replace("$$$reportNull$$$", ""):
                found = (m.start(), i + 1)
                break
        if not found:
            break
        src = src[:found[0]] + src[found[1]:]

    # any straggler call lines
    src = re.sub(r"^[ \t]*\$\$\$reportNull\$\$\$\d+\([^)]*\);[ \t]*\n", "", src, flags=re.M)
    src = re.sub(r"\n{3,}", "\n\n", src)

    if src != orig:
        open(path, "w", encoding="utf-8").write(src)
        return True
    return False

n = 0
for root, _, files in os.walk(MODS):
    if "/target/" in root:
        continue
    for f in files:
        if f.endswith(".java") and strip_report_null(os.path.join(root, f)):
            n += 1
print(f"[1] stripped $$$reportNull$$$ instrumentation from {n} files")

# ---------------------------------------------------------------- fix 2
RELOC = {
    "FichaServerNotify": ("ru/kotikstasika/fichaservernotify/libs/gson",
                          "ru.kotikstasika.fichaservernotify.libs.gson", "com.google.gson"),
    "HolyExpKeeper":     ("org/dimasik/mantiexpkeeper/libs/hikari",
                          "org.dimasik.mantiexpkeeper.libs.hikari", "com.zaxxer.hikari"),
}
for mod, (reldir, oldpkg, newpkg) in RELOC.items():
    full = os.path.join(MODS, mod, "src/main/java", reldir)
    if os.path.isdir(full):
        cnt = sum(len([x for x in fs if x.endswith(".java")]) for _, _, fs in os.walk(full))
        shutil.rmtree(full)
        print(f"[2] {mod}: removed {cnt} relocated {newpkg} sources")
    for root, _, files in os.walk(os.path.join(MODS, mod, "src/main/java")):
        for f in files:
            if not f.endswith(".java"):
                continue
            p = os.path.join(root, f)
            s = open(p, encoding="utf-8", errors="ignore").read()
            if oldpkg in s:
                open(p, "w", encoding="utf-8").write(s.replace(oldpkg, newpkg))
                print(f"    rewrote imports in {f}")
print("done")
