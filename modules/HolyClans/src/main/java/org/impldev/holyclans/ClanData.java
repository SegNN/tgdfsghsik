package org.impldev.holyclans;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;

/**
 * Хранилище кланов: один yml-файл на клан в папке {@code clans/}.
 */
public final class ClanData {
   private final HolyClans plugin;
   private final File clansDir;

   /** Имя клана (lower-case) → клан. */
   private final Map<String, Clan> byName = new ConcurrentHashMap<>();
   /** UUID игрока → клан, в котором он состоит. */
   private final Map<UUID, Clan> byPlayer = new ConcurrentHashMap<>();

   public ClanData(HolyClans plugin) {
      this.plugin = plugin;
      this.clansDir = new File(plugin.getDataFolder(), "clans");
      if (!this.clansDir.exists()) {
         this.clansDir.mkdirs();
      }
      this.loadAll();
   }

   // ── загрузка ──────────────────────────────────────────────────────────────

   private void loadAll() {
      File[] files = this.clansDir.listFiles((d, n) -> n.endsWith(".yml"));
      if (files == null) return;
      for (File file : files) {
         try {
            Clan clan = this.load(file);
            if (clan != null) {
               this.byName.put(clan.nameLower(), clan);
               for (UUID uuid : clan.members().keySet()) {
                  this.byPlayer.put(uuid, clan);
               }
            }
         } catch (Exception e) {
            this.plugin.getLogger().warning("Не удалось прочитать " + file.getName() + ": " + e.getMessage());
         }
      }
      this.plugin.getLogger().info("Загружено " + this.byName.size() + " кланов.");
   }

   @Nullable
   private Clan load(File file) {
      YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
      String name = yml.getString("name");
      if (name == null) return null;

      UUID leader = UUID.fromString(yml.getString("leader"));
      long treasury = yml.getLong("treasury");
      int points = yml.getInt("points");
      int level = yml.getInt("level", 1);
      int slots = yml.getInt("slots", 2);
      boolean pvp = yml.getBoolean("pvp", false);
      int glowColor = yml.getInt("glow-color", -1);
      String glowMode = yml.getString("glow-mode", "OFF");

      Map<UUID, ClanMember> members = new LinkedHashMap<>();
      ConfigurationSection sec = yml.getConfigurationSection("members");
      if (sec != null) {
         for (String key : sec.getKeys(false)) {
            UUID uuid = UUID.fromString(key);
            String memberName = sec.getString(key + ".name", "?");
            ClanRole role = ClanRole.valueOf(sec.getString(key + ".role", "MEMBER"));
            members.put(uuid, new ClanMember(uuid, memberName, role));
         }
      }

      Map<Integer, Location> homes = new HashMap<>();
      ConfigurationSection homeSec = yml.getConfigurationSection("homes");
      if (homeSec != null) {
         for (String key : homeSec.getKeys(false)) {
            int idx = Integer.parseInt(key);
            String world = homeSec.getString(key + ".world");
            double x = homeSec.getDouble(key + ".x");
            double y = homeSec.getDouble(key + ".y");
            double z = homeSec.getDouble(key + ".z");
            float yaw = (float) homeSec.getDouble(key + ".yaw");
            float pitch = (float) homeSec.getDouble(key + ".pitch");
            if (world != null && Bukkit.getWorld(world) != null) {
               homes.put(idx, new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch));
            }
         }
      }

      Clan clan = new Clan(name, leader, treasury, points, level, slots, pvp, glowColor, glowMode, members, homes);
      return clan;
   }

   // ── сохранение ────────────────────────────────────────────────────────────

   public void save(Clan clan) {
      File file = new File(this.clansDir, clan.nameLower() + ".yml");
      YamlConfiguration yml = new YamlConfiguration();
      yml.set("name", clan.name());
      yml.set("leader", clan.leader().toString());
      yml.set("treasury", clan.treasury());
      yml.set("points", clan.points());
      yml.set("level", clan.level());
      yml.set("slots", clan.slots());
      yml.set("pvp", clan.pvp());
      yml.set("glow-color", clan.glowColor());
      yml.set("glow-mode", clan.glowMode());

      for (Map.Entry<UUID, ClanMember> entry : clan.members().entrySet()) {
         String path = "members." + entry.getKey().toString();
         yml.set(path + ".name", entry.getValue().name());
         yml.set(path + ".role", entry.getValue().role().name());
      }

      for (Map.Entry<Integer, Location> entry : clan.homes().entrySet()) {
         String path = "homes." + entry.getKey();
         Location loc = entry.getValue();
         yml.set(path + ".world", loc.getWorld().getName());
         yml.set(path + ".x", loc.getX());
         yml.set(path + ".y", loc.getY());
         yml.set(path + ".z", loc.getZ());
         yml.set(path + ".yaw", loc.getYaw());
         yml.set(path + ".pitch", loc.getPitch());
      }

      try {
         yml.save(file);
      } catch (IOException e) {
         this.plugin.getLogger().warning("Не удалось сохранить клан " + clan.name() + ": " + e.getMessage());
      }
   }

   public void saveAll() {
      for (Clan clan : this.byName.values()) {
         this.save(clan);
      }
   }

   public void delete(Clan clan) {
      for (UUID uuid : clan.members().keySet()) {
         this.byPlayer.remove(uuid);
      }
      this.byName.remove(clan.nameLower());
      File file = new File(this.clansDir, clan.nameLower() + ".yml");
      if (file.exists()) {
         file.delete();
      }
   }

   // ── доступ ────────────────────────────────────────────────────────────────

   @Nullable
   public Clan byName(String name) {
      return this.byName.get(name.toLowerCase());
   }

   @Nullable
   public Clan byPlayer(UUID uuid) {
      return this.byPlayer.get(uuid);
   }

   public Map<String, Clan> all() {
      return this.byName;
   }

   /** Создать новый клан. Возвращает null, если имя занято. */
   @Nullable
   public Clan create(String name, UUID leaderUuid, String leaderName) {
      if (this.byName.containsKey(name.toLowerCase())) return null;

      Map<UUID, ClanMember> members = new LinkedHashMap<>();
      members.put(leaderUuid, new ClanMember(leaderUuid, leaderName, ClanRole.LEADER));

      Clan clan = new Clan(name, leaderUuid, 0L, 0, 1,
            this.plugin.getConfig().getInt("initial-slots", 2),
            false, -1, "OFF", members, new HashMap<>());

      this.byName.put(clan.nameLower(), clan);
      this.byPlayer.put(leaderUuid, clan);
      this.save(clan);
      return clan;
   }

   /** Зарегистрировать игрока в кеше при добавлении в клан. */
   public void registerPlayer(UUID uuid, Clan clan) {
      this.byPlayer.put(uuid, clan);
   }

   /** Убрать игрока из кеша при исключении/выходе. */
   public void unregisterPlayer(UUID uuid) {
      this.byPlayer.remove(uuid);
   }

   /** Получить топ кланов по очкам. */
   public List<Clan> top(int limit) {
      List<Clan> list = new ArrayList<>(this.byName.values());
      list.sort((a, b) -> Integer.compare(b.points(), a.points()));
      if (list.size() > limit) list = list.subList(0, limit);
      return list;
   }
}
