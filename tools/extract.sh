#!/usr/bin/env bash
# Decompile every custom (self-written) plugin into a Maven module.
set -u
WS="/home/fluge/Загрузки/AyuGram Desktop/FichaWorld-src"
PLUGINS="/home/fluge/Загрузки/AyuGram Desktop/FichaWorld v6.0 Free/plugins"
VF="$WS/tools/vineflower.jar"
MODS="$WS/modules"
REPORT="$WS/tools/extract-report.txt"

# vendor package roots that identify self-written plugins
VENDORS='org/dimasik ru/kotikstasika ua/fichadev ua/fichamine ua/fichatime ru/nezxenka org/nezxenka ru/lolodimo4ka stickdev ru/holybackpack ru/litechat org/impldev ru/extas7y me/ones frozenpon'

mkdir -p "$MODS"
: > "$REPORT"

vendor_root() {          # $1 = main class -> vendor package path
  local pkg="${1%.*}" ; pkg="${pkg//./\/}"
  local a b
  a="${pkg%%/*}"
  case "$a" in
    org|ru|ua|com|me|net|io|dev) b="${pkg#*/}"; b="${b%%/*}"; echo "$a/$b" ;;
    *) echo "$a" ;;
  esac
}

cd "$PLUGINS" || exit 1
for jar in *; do
  [ -f "$jar" ] || continue
  unzip -l "$jar" >/dev/null 2>&1 || continue
  yml=$(unzip -p "$jar" plugin.yml 2>/dev/null | tr -d '\r') || continue
  [ -z "$yml" ] && continue

  main=$(printf '%s\n' "$yml" | grep -m1 '^main:' | sed 's/^main:[[:space:]]*//; s/["'"'"']//g' | tr -d '[:space:]')
  name=$(printf '%s\n' "$yml" | grep -m1 '^name:' | sed 's/^name:[[:space:]]*//; s/["'"'"']//g' | tr -d '[:space:]')
  ver=$(printf '%s\n' "$yml"  | grep -m1 '^version:' | sed 's/^version:[[:space:]]*//; s/["'"'"']//g' | tr -d '[:space:]')
  [ -z "$main" ] && continue

  root=$(vendor_root "$main")
  # only self-written vendors
  match=0; for v in $VENDORS; do [ "$root" = "$v" ] && match=1; done
  [ "$match" -eq 0 ] && continue

  mod="$MODS/$name"
  rm -rf "$mod"; mkdir -p "$mod/src/main/java" "$mod/src/main/resources"
  work=$(mktemp -d)

  # --- own classes ---
  unzip -qo "$jar" "$root/*" -d "$work/cls" 2>/dev/null
  find "$work/cls" -name '*.class' ! -path "*/$root/*" -delete 2>/dev/null
  ncls=$(find "$work/cls" -name '*.class' 2>/dev/null | wc -l)
  if [ "$ncls" -eq 0 ]; then echo "SKIP  $name (no classes under $root)" >> "$REPORT"; rm -rf "$work"; continue; fi

  java -jar "$VF" -dgs=1 -hdc=0 -jrt=1 -rsy=1 -din=1 -log=ERROR \
       -e="$PLUGINS/$jar" "$work/cls" "$mod/src/main/java" >/dev/null 2>&1
  njava=$(find "$mod/src/main/java" -name '*.java' | wc -l)

  # --- resources: everything non-class that isn't META-INF or a shaded library dir ---
  libdirs=$(unzip -l "$jar" 2>/dev/null | awk '{print $4}' | grep '\.class$' \
            | grep -v "^$root/" | cut -d/ -f1 | sort -u)
  unzip -qo "$jar" -d "$work/res" -x '*.class' 2>/dev/null
  ( cd "$work/res" 2>/dev/null || exit 0
    rm -rf META-INF
    for d in $libdirs; do rm -rf "$d"; done
    # drop now-empty own package tree remnants
    find . -type d -empty -delete 2>/dev/null
    cp -r . "$mod/src/main/resources/" 2>/dev/null )
  nres=$(find "$mod/src/main/resources" -type f | wc -l)

  printf 'OK    %-26s root=%-18s classes=%-5s java=%-5s res=%-4s ver=%s\n' \
         "$name" "$root" "$ncls" "$njava" "$nres" "$ver" >> "$REPORT"
  echo "$name|$main|$ver|$root|$jar" >> "$WS/tools/modules.idx"
  rm -rf "$work"
done
echo "--- done ---" >> "$REPORT"
