#!/usr/bin/env python3
"""Generate parent + per-module pom.xml for the decompiled FichaWorld plugins."""
import os, re, sys

WS = "/home/fluge/Загрузки/AyuGram Desktop/FichaWorld-src"
MODS = os.path.join(WS, "modules")
STUBS = "_api-stubs"
STUBS_VERSION = "1.0.0"

OWN = ("org.dimasik", "ru.kotikstasika", "ua.fichadev", "ua.fichamine", "ua.fichatime",
       "ru.nezxenka", "org.nezxenka", "ru.lolodimo4ka", "stickdev", "ru.holybackpack",
       "ru.litechat", "org.impldev", "ru.extas7y", "me.ones", "frozenpon")

# versions of the third-party plugin APIs actually shipped in the 1.21.4 server
PROVIDED = {
    "com.sk89q.worldguard":        ("local.plugins", "worldguard", "7.0.13"),
    "com.sk89q.worldedit":         ("local.plugins", "worldedit", "7.3.16"),
    "me.clip.placeholderapi":      ("local.plugins", "placeholderapi", "2.12.3"),
    "eu.decentsoftware.holograms": ("local.plugins", "decentholograms", "2.10.1"),
    "net.luckperms.api":           ("local.plugins", "luckperms", "5.5.53"),
    "net.milkbowl.vault":          ("local.plugins", "vault", "1.7.3"),
    "org.black_ixx.playerpoints":  ("local.plugins", "playerpoints", "3.3.5"),
    "dev.espi.protectionstones":   ("local.plugins", "protectionstones", "2.10.5"),
    "com.comphenix.protocol":      ("local.plugins", "protocollib", "5.4.0"),
    "com.github.retrooper":        ("local.plugins", "packetevents", "2.13.0"),
    "io.github.retrooper":         ("local.plugins", "packetevents", "2.13.0"),
    "com.earth2me.essentials":     ("local.plugins", "essentialsx", "2.22.0"),
    # only reachable if some module still binds versioned NMS - it must not on 1.21.4
    "net.minecraft.server":        ("local.plugins", "craftbukkit", "1.16.5"),
    "org.bukkit.craftbukkit":      ("local.plugins", "craftbukkit", "1.16.5"),
    "com.mojang.authlib":          ("local.plugins", "craftbukkit", "1.16.5"),
    "com.mojang.datafixers":       ("local.plugins", "craftbukkit", "1.16.5"),
}
SHADED = {
    "com.zaxxer.hikari":     ("com.zaxxer", "HikariCP", "4.0.3"),
    "com.google.gson":       ("com.google.code.gson", "gson", "2.10.1"),
    "redis.clients.jedis":   ("redis.clients", "jedis", "4.4.6"),
    "com.rabbitmq.client":   ("com.rabbitmq", "amqp-client", "5.20.0"),
    "io.nats.client":        ("io.nats", "jnats", "2.17.2"),
    "io.prometheus.client":  ("io.prometheus", "simpleclient", "0.16.0"),
    "io.micrometer.core":    ("io.micrometer", "micrometer-core", "1.12.4"),
    "com.codahale.metrics":  ("io.dropwizard.metrics", "metrics-core", "4.2.25"),
    "org.hibernate":         ("org.hibernate", "hibernate-core", "5.6.15.Final"),
    "javassist":             ("org.javassist", "javassist", "3.29.2-GA"),
    "org.java_websocket":    ("org.java-websocket", "Java-WebSocket", "1.5.6"),
    "org.json":              ("org.json", "json", "20240303"),
    "shaded.org.jdbi":       ("org.jdbi", "jdbi3-core", "3.45.1"),
    "org.slf4j":             ("org.slf4j", "slf4j-api", "2.0.12"),
}
EXTRA = {  # module -> extra deps maven can't infer
    "HolyExpKeeper": [("io.dropwizard.metrics", "metrics-healthchecks", "4.2.25", "compile")],
    # ProtectionStones exposes WorldGuard/WorldEdit types in its public API signatures
    "FichaPSAddon": [("local.plugins", "worldguard", "7.0.13", "provided"),
                     ("local.plugins", "worldedit", "7.3.16", "provided")],
    # jdbi was shaded into `shaded.org.jdbi`; imports rewritten, relocated back at package time
    "FichaCases": [("org.jdbi", "jdbi3-core", "3.45.1", "compile"),
                   ("org.jdbi", "jdbi3-sqlobject", "3.45.1", "compile"),
                   ("org.jdbi", "jdbi3-caffeine-cache", "3.45.1", "compile")],
}
# original jars shaded these under a plugin-private package; reproduce at package time
RELOCATE = {
    "FichaServerNotify": [("com.google.gson", "ru.kotikstasika.fichaservernotify.libs.gson")],
    "HolyExpKeeper":     [("com.zaxxer.hikari", "org.dimasik.mantiexpkeeper.libs.hikari")],
    "FichaCases":        [("org.jdbi", "shaded.org.jdbi")],
}
# dependency cycles are broken by compiling against the ORIGINAL jar for one direction
CYCLE_BREAK = {("HolyGoldSpawner", "HolyCustomEgg")}
ORIG_JAR_GAV = ("local.ficha", "{name}", "1.16.5")

STUB_CLASSES = {"StickEcoAPI": "org.dimasik.stickeco", "AntiCheatStatus": "org.dimasik.anticheatstatus",
                "SyncAPI": "ru.nezxenka.syncservice", "PremiumAPI": "stickdev.siynes.stickhwpremium"}


def java_dir(mod):
    return os.path.join(MODS, mod, "src/main/java")


def module_list():
    return sorted(d for d in os.listdir(MODS)
                  if os.path.isdir(os.path.join(MODS, d)) and os.path.isdir(java_dir(d)))


def imports_of(mod):
    out = set()
    for root, _, files in os.walk(java_dir(mod)):
        for f in files:
            if not f.endswith(".java"):
                continue
            with open(os.path.join(root, f), encoding="utf-8", errors="ignore") as fh:
                for line in fh:
                    m = re.match(r"\s*import\s+(?:static\s+)?([\w.]+)", line)
                    if m:
                        out.add(m.group(1))
    return out


def plugin_meta(mod):
    p = os.path.join(MODS, mod, "src/main/resources/plugin.yml")
    if not os.path.isfile(p):
        return mod, "1.0", ""
    txt = open(p, encoding="utf-8", errors="ignore").read()

    def g(k):
        m = re.search(rf"^{k}:\s*(.+)$", txt, re.M)
        return m.group(1).strip().strip("'\"") if m else ""
    return g("name") or mod, g("version") or "1.0", g("main")


mods = module_list()
# package -> owning module
owner = {}
for m in mods:
    jd = java_dir(m)
    for root, _, fs in os.walk(jd):
        if any(x.endswith(".java") for x in fs):
            owner[os.path.relpath(root, jd).replace(os.sep, ".")] = m

IMPS = {m: imports_of(m) for m in mods}


def dep(g, a, v, scope):
    return f"""    <dependency>
      <groupId>{g}</groupId>
      <artifactId>{a}</artifactId>
      <version>{v}</version>
      <scope>{scope}</scope>
    </dependency>"""


summary = {}
for mod in mods:
    if mod == STUBS:
        continue
    name, ver, main = plugin_meta(mod)
    imps = IMPS[mod]
    dep_xml, seen = [], set()
    lombok = annot = False

    for i in sorted(imps):
        if i.startswith(("java.", "javax.")):
            continue
        if i.startswith("lombok"):
            lombok = True; continue
        if i.startswith("org.jetbrains.annotations"):
            annot = True; continue
        # inter-module / stub references
        if i.startswith(OWN):
            pkg = i.rsplit(".", 1)[0]
            o = owner.get(pkg) or owner.get(i.rsplit(".", 2)[0] if i.count(".") > 1 else pkg)
            if o and o != mod:
                if (mod, o) in CYCLE_BREAK:
                    g, a, v = ORIG_JAR_GAV[0], o, ORIG_JAR_GAV[2]
                else:
                    g, a, v = "world.ficha", o, (STUBS_VERSION if o == STUBS else plugin_meta(o)[1])
                if (g, a) not in seen:
                    seen.add((g, a)); dep_xml.append(dep(g, a, v, "provided"))
            elif o is None and any(i.startswith(p) for p in STUB_CLASSES.values()):
                if ("world.ficha", STUBS) not in seen:
                    seen.add(("world.ficha", STUBS))
                    dep_xml.append(dep("world.ficha", STUBS, STUBS_VERSION, "provided"))
            continue
        if i.startswith(("org.bukkit", "org.spigotmc", "net.md_5.bungee", "com.destroystokyo.paper",
                         "io.papermc.paper", "net.kyori.adventure", "com.google.common",
                         "org.yaml.snakeyaml", "org.apache.commons")) and not i.startswith("org.bukkit.craftbukkit"):
            continue
        for pref, d in PROVIDED.items():
            if i.startswith(pref):
                if d[:2] not in seen:
                    seen.add(d[:2]); dep_xml.append(dep(*d, "provided"))
                break
        else:
            for pref, d in SHADED.items():
                if i.startswith(pref):
                    if d[:2] not in seen:
                        seen.add(d[:2]); dep_xml.append(dep(*d, "compile"))
                    break

    for g, a, v, s in EXTRA.get(mod, []):
        if (g, a) not in seen:
            seen.add((g, a)); dep_xml.append(dep(g, a, v, s))
    if lombok:
        dep_xml.append(dep("org.projectlombok", "lombok", "1.18.32", "provided"))
    if annot:
        dep_xml.append(dep("org.jetbrains", "annotations", "24.1.0", "provided"))

    build = ""
    if mod in RELOCATE:
        rel = "\n".join(f"""                <relocation>
                  <pattern>{a}</pattern>
                  <shadedPattern>{b}</shadedPattern>
                </relocation>""" for a, b in RELOCATE[mod])
        build = f"""
  <build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-shade-plugin</artifactId>
        <executions>
          <execution>
            <phase>package</phase>
            <goals><goal>shade</goal></goals>
            <configuration>
              <createDependencyReducedPom>false</createDependencyReducedPom>
              <relocations>
{rel}
              </relocations>
            </configuration>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
"""

    pom = f"""<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <parent>
    <groupId>world.ficha</groupId>
    <artifactId>fichaworld-parent</artifactId>
    <version>1.0.0</version>
    <relativePath>../../pom.xml</relativePath>
  </parent>

  <artifactId>{mod}</artifactId>
  <version>{ver}</version>
  <name>{name}</name>
  <description>main: {main}</description>

  <dependencies>
{chr(10).join(dep_xml)}
  </dependencies>
{build}</project>
"""
    open(os.path.join(MODS, mod, "pom.xml"), "w", encoding="utf-8").write(pom)
    summary[mod] = [d[1] for d in ()] or dep_xml

# ---- stubs module ----
open(os.path.join(MODS, STUBS, "pom.xml"), "w", encoding="utf-8").write(f"""<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <parent>
    <groupId>world.ficha</groupId>
    <artifactId>fichaworld-parent</artifactId>
    <version>1.0.0</version>
    <relativePath>../../pom.xml</relativePath>
  </parent>
  <artifactId>{STUBS}</artifactId>
  <version>{STUBS_VERSION}</version>
  <name>API stubs for plugins absent from this build</name>
</project>
""")

# ---- parent ----
ordered = [STUBS] + [m for m in mods if m != STUBS]
mods_xml = "\n".join(f"    <module>modules/{m}</module>" for m in ordered)
open(os.path.join(WS, "pom.xml"), "w", encoding="utf-8").write(f"""<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>world.ficha</groupId>
  <artifactId>fichaworld-parent</artifactId>
  <version>1.0.0</version>
  <packaging>pom</packaging>
  <name>FichaWorld plugins</name>

  <properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <maven.compiler.release>21</maven.compiler.release>
  </properties>

  <modules>
{mods_xml}
  </modules>

  <repositories>
    <repository><id>papermc</id><url>https://repo.papermc.io/repository/maven-public/</url></repository>
    <repository><id>spigotmc</id><url>https://hub.spigotmc.org/nexus/content/repositories/snapshots/</url></repository>
    <repository><id>sonatype</id><url>https://oss.sonatype.org/content/groups/public/</url></repository>
    <repository><id>jitpack</id><url>https://jitpack.io</url></repository>
  </repositories>

  <dependencies>
    <dependency>
      <groupId>io.papermc.paper</groupId>
      <artifactId>paper-api</artifactId>
      <version>1.21.4-R0.1-SNAPSHOT</version>
      <scope>provided</scope>
    </dependency>
  </dependencies>

  <build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <version>3.13.0</version>
        <configuration>
          <compilerArgs><arg>-nowarn</arg></compilerArgs>
          <showWarnings>false</showWarnings>
        </configuration>
      </plugin>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-shade-plugin</artifactId>
        <version>3.5.3</version>
        <executions>
          <execution>
            <phase>package</phase>
            <goals><goal>shade</goal></goals>
            <configuration>
              <createDependencyReducedPom>false</createDependencyReducedPom>
              <filters>
                <filter><artifact>*:*</artifact>
                  <excludes><exclude>META-INF/*.SF</exclude><exclude>META-INF/*.DSA</exclude><exclude>META-INF/*.RSA</exclude><exclude>module-info.class</exclude></excludes>
                </filter>
              </filters>
            </configuration>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
</project>
""")
print(f"generated parent + {len(ordered)} module poms (incl. {STUBS})")
