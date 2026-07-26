#!/usr/bin/env python3
"""Generate parent + per-module pom.xml for the decompiled FichaWorld plugins."""
import os, re, subprocess, zipfile, sys, json

WS = "/home/fluge/Загрузки/AyuGram Desktop/FichaWorld-src"
PLUGINS = "/home/fluge/Загрузки/AyuGram Desktop/FichaWorld v6.0 Free/plugins"
MODS = os.path.join(WS, "modules")
MC = sys.argv[1] if len(sys.argv) > 1 else "1.16.5"

OWN = ("org.dimasik", "ru.kotikstasika", "ua.fichadev", "ua.fichamine", "ua.fichatime",
       "ru.nezxenka", "org.nezxenka", "ru.lolodimo4ka", "stickdev", "ru.holybackpack",
       "ru.litechat", "org.impldev", "ru.extas7y", "me.ones", "frozenpon")

# import-prefix -> (groupId, artifactId, version, scope)
PROVIDED = {
    "com.sk89q.worldguard":        ("local.plugins", "worldguard", "7.0.5", "provided"),
    "com.sk89q.worldedit":         ("local.plugins", "worldedit", "7.2.16", "provided"),
    "me.clip.placeholderapi":      ("local.plugins", "placeholderapi", "2.11.6", "provided"),
    "eu.decentsoftware.holograms": ("local.plugins", "decentholograms", "2.9.7", "provided"),
    "net.luckperms.api":           ("local.plugins", "luckperms", "5.4.166", "provided"),
    "net.milkbowl.vault":          ("local.plugins", "vault", "1.7.3", "provided"),
    "org.black_ixx.playerpoints":  ("local.plugins", "playerpoints", "3.3.2", "provided"),
    "dev.espi.protectionstones":   ("local.plugins", "protectionstones", "2.8.5", "provided"),
    "com.comphenix.protocol":      ("local.plugins", "protocollib", "5.3.0", "provided"),
    "com.github.retrooper":        ("local.plugins", "packetevents", "2.9.5", "provided"),
    "io.github.retrooper":         ("local.plugins", "packetevents", "2.9.5", "provided"),
    "com.earth2me.essentials":     ("local.plugins", "essentialsx", "2.22.0", "provided"),
    "net.minecraft.server":        ("local.plugins", "craftbukkit", "1.16.5", "provided"),
    "org.bukkit.craftbukkit":      ("local.plugins", "craftbukkit", "1.16.5", "provided"),
    "com.mojang.authlib":          ("local.plugins", "craftbukkit", "1.16.5", "provided"),
    "com.mojang.datafixers":       ("local.plugins", "craftbukkit", "1.16.5", "provided"),
}
# libraries the original jar shaded in -> bundled again
SHADED = {
    "com.zaxxer.hikari":     ("com.zaxxer", "HikariCP", "4.0.3"),
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

def imports(mod):
    out = set()
    for root, _, files in os.walk(os.path.join(MODS, mod, "src/main/java")):
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
    txt = open(p, encoding="utf-8", errors="ignore").read()
    def g(k):
        m = re.search(rf"^{k}:\s*(.+)$", txt, re.M)
        return m.group(1).strip().strip("'\"") if m else ""
    return g("name"), g("version"), g("main")

modules = sorted(d for d in os.listdir(MODS) if os.path.isdir(os.path.join(MODS, d)))
summary = {}

for mod in modules:
    name, ver, main = plugin_meta(mod)
    imps = imports(mod)
    deps, needs_lombok, needs_annot = [], False, False
    seen = set()
    for i in sorted(imps):
        if i.startswith(OWN) or i.startswith(("java.", "javax.")):
            continue
        if i.startswith("lombok"):
            needs_lombok = True; continue
        if i.startswith("org.jetbrains.annotations"):
            needs_annot = True; continue
        if i.startswith(("org.bukkit", "org.spigotmc", "net.md_5.bungee",
                         "com.destroystokyo.paper", "io.papermc.paper",
                         "net.kyori.adventure", "com.google.common",
                         "com.google.gson", "org.yaml.snakeyaml", "org.apache.commons")):
            if not i.startswith("org.bukkit.craftbukkit"):
                continue
        for pref, d in PROVIDED.items():
            if i.startswith(pref) and d[:2] not in seen:
                seen.add(d[:2]); deps.append(d + (False,)); break
        else:
            for pref, d in SHADED.items():
                if i.startswith(pref) and d[:2] not in seen:
                    seen.add(d[:2]); deps.append(d + ("compile", True)); break

    dep_xml = []
    for d in deps:
        g, a, v, scope = d[0], d[1], d[2], d[3]
        dep_xml.append(f"""    <dependency>
      <groupId>{g}</groupId>
      <artifactId>{a}</artifactId>
      <version>{v}</version>
      <scope>{scope}</scope>
    </dependency>""")
    if needs_lombok:
        dep_xml.append("""    <dependency>
      <groupId>org.projectlombok</groupId>
      <artifactId>lombok</artifactId>
      <version>1.18.32</version>
      <scope>provided</scope>
    </dependency>""")
    if needs_annot:
        dep_xml.append("""    <dependency>
      <groupId>org.jetbrains</groupId>
      <artifactId>annotations</artifactId>
      <version>24.1.0</version>
      <scope>provided</scope>
    </dependency>""")

    shade = any(len(d) > 4 and d[4] for d in deps)
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

  <artifactId>{name}</artifactId>
  <version>{ver or '1.0'}</version>
  <name>{name}</name>
  <description>main: {main}</description>

  <dependencies>
{chr(10).join(dep_xml)}
  </dependencies>
</project>
"""
    open(os.path.join(MODS, mod, "pom.xml"), "w", encoding="utf-8").write(pom)
    summary[name] = {"deps": [f"{d[1]}" for d in deps], "lombok": needs_lombok, "shade": shade}

# ---------- parent ----------
mods_xml = "\n".join(f"    <module>modules/{m}</module>" for m in modules)
parent = f"""<?xml version="1.0" encoding="UTF-8"?>
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
    <maven.compiler.source>16</maven.compiler.source>
    <maven.compiler.target>16</maven.compiler.target>
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
      <groupId>com.destroystokyo.paper</groupId>
      <artifactId>paper-api</artifactId>
      <version>1.16.5-R0.1-SNAPSHOT</version>
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
          <release>16</release>
          <compilerArgs><arg>-nowarn</arg><arg>-Xlint:none</arg></compilerArgs>
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
                  <excludes><exclude>META-INF/*.SF</exclude><exclude>META-INF/*.DSA</exclude><exclude>META-INF/*.RSA</exclude></excludes>
                </filter>
              </filters>
            </configuration>
          </execution>
        </executions>
      </plugin>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-resources-plugin</artifactId>
        <version>3.3.1</version>
      </plugin>
    </plugins>
    <resources>
      <resource><directory>src/main/resources</directory><filtering>false</filtering></resource>
    </resources>
  </build>
</project>
"""
open(os.path.join(WS, "pom.xml"), "w", encoding="utf-8").write(parent)
print(f"generated parent + {len(modules)} module poms")
for k, v in sorted(summary.items()):
    if v["deps"] or v["lombok"]:
        print(f"  {k:24s} lombok={str(v['lombok']):5s} shade={str(v['shade']):5s} {','.join(v['deps'])}")
