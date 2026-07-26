package org.impldev.holycore.boosters;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Состояние бустеров: уровень и время окончания, отдельно для каждого игрока.
 *
 * <p>Срок хранится абсолютным моментом времени, а не остатком: остаток пришлось бы уменьшать по
 * таймеру, и он врал бы после каждого перезапуска сервера. От абсолютного момента остаток
 * считается в любой момент и переживает любой простой.
 */
public final class BoosterData {
   private final JavaPlugin plugin;
   private final File file;

   /** uuid -> (бустер -> {уровень, момент окончания}) */
   private final Map<UUID, Map<String, long[]>> state = new HashMap<>();
   private final Map<UUID, Long> spent = new HashMap<>();

   public BoosterData(JavaPlugin plugin) {
      this.plugin = plugin;
      this.file = new File(plugin.getDataFolder(), "boosters.yml");
      this.load();
   }

   /** Уровень с учётом срока: истёкший бустер считается отсутствующим. */
   public int level(UUID uuid, String id) {
      long[] v = this.state.getOrDefault(uuid, Map.of()).get(id);
      if (v == null || v[1] <= System.currentTimeMillis()) {
         return 0;
      }

      return (int)v[0];
   }

   /** Сколько миллисекунд бустер ещё действует; 0 если не действует. */
   public long remaining(UUID uuid, String id) {
      long[] v = this.state.getOrDefault(uuid, Map.of()).get(id);
      if (v == null) {
         return 0L;
      }

      return Math.max(0L, v[1] - System.currentTimeMillis());
   }

   public long spent(UUID uuid) {
      return this.spent.getOrDefault(uuid, 0L);
   }

   /** Поднимает уровень на единицу и продлевает срок. */
   public void upgrade(UUID uuid, String id, long durationMs, int price) {
      Map<String, long[]> mine = this.state.computeIfAbsent(uuid, k -> new HashMap<>());
      long[] v = mine.get(id);
      int level = v == null || v[1] <= System.currentTimeMillis() ? 0 : (int)v[0];
      mine.put(id, new long[]{level + 1L, System.currentTimeMillis() + durationMs});
      this.spent.merge(uuid, (long)price, Long::sum);
      this.save();
   }

   private void load() {
      if (!this.file.isFile()) {
         return;
      }

      FileConfiguration cfg = YamlConfiguration.loadConfiguration(this.file);
      ConfigurationSection players = cfg.getConfigurationSection("players");
      if (players == null) {
         return;
      }

      for (String raw : players.getKeys(false)) {
         UUID uuid;
         try {
            uuid = UUID.fromString(raw);
         } catch (IllegalArgumentException ignored) {
            continue;
         }

         this.spent.put(uuid, players.getLong(raw + ".spent"));
         ConfigurationSection mine = players.getConfigurationSection(raw + ".boosters");
         if (mine == null) {
            continue;
         }

         Map<String, long[]> map = new HashMap<>();
         for (String id : mine.getKeys(false)) {
            map.put(id, new long[]{mine.getLong(id + ".level"), mine.getLong(id + ".until")});
         }

         this.state.put(uuid, map);
      }
   }

   public void save() {
      FileConfiguration cfg = new YamlConfiguration();
      this.state.forEach((uuid, mine) -> {
         cfg.set("players." + uuid + ".spent", this.spent.getOrDefault(uuid, 0L));
         mine.forEach((id, v) -> {
            cfg.set("players." + uuid + ".boosters." + id + ".level", v[0]);
            cfg.set("players." + uuid + ".boosters." + id + ".until", v[1]);
         });
      });

      try {
         this.plugin.getDataFolder().mkdirs();
         cfg.save(this.file);
      } catch (IOException e) {
         this.plugin.getLogger().warning("Не удалось сохранить boosters.yml: " + e.getMessage());
      }
   }
}
