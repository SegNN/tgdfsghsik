package org.impldev.holycore.invest;

import java.io.File;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.impldev.holycore.boosters.Sapphires;

/**
 * Состояние инвестиций: уровни направлений общие для всего сервера, очки - личные.
 *
 * <p>Разделение именно такое, потому что прокачиваются глобальные бустеры: эффект от вложения
 * получают все, а в рейтинге учитывается, кто именно вложился. Личный счётчик поэтому хранится
 * дважды - всего и за неделю, иначе еженедельное обнуление рейтинга стирало бы и общий вклад.
 */
public final class InvestData {
   /** сколько сапфиров делится между топом каждые сутки */
   private static final int DAILY_POOL = 2500;
   private static final int TOP_SIZE = 10;
   private static final ZoneId MOSCOW = ZoneId.of("Europe/Moscow");

   private final JavaPlugin plugin;
   private final File file;

   private final Map<String, int[]> state = new HashMap<>();     // id -> {level, progress}
   private final Map<UUID, long[]> players = new HashMap<>();    // uuid -> {всего, за неделю}
   private final Map<UUID, String> names = new HashMap<>();
   private String weekStart = "";
   private String lastPayout = "";

   public InvestData(JavaPlugin plugin) {
      this.plugin = plugin;
      this.file = new File(plugin.getDataFolder(), "invest.yml");
      this.load();
   }

   public int level(String id) {
      return this.state.getOrDefault(id, new int[]{0, 0})[0];
   }

   public int progress(String id) {
      return this.state.getOrDefault(id, new int[]{0, 0})[1];
   }

   public long points(UUID uuid) {
      return this.players.getOrDefault(uuid, new long[]{0L, 0L})[0];
   }

   public long weekPoints(UUID uuid) {
      return this.players.getOrDefault(uuid, new long[]{0L, 0L})[1];
   }

   /** Возвращает true, если вложение подняло уровень. */
   public boolean invest(UUID uuid, String name, Category category) {
      int[] st = this.state.computeIfAbsent(category.id(), k -> new int[]{0, 0});
      long[] pl = this.players.computeIfAbsent(uuid, k -> new long[]{0L, 0L});
      this.names.put(uuid, name);

      pl[0]++;
      pl[1]++;
      st[1]++;

      boolean levelUp = false;
      if (st[1] >= category.need() && st[0] < category.maxLevel()) {
         st[0]++;
         st[1] = 0;
         levelUp = true;
      }

      this.save();
      return levelUp;
   }

   public record TopEntry(UUID uuid, String name, long points) {
   }

   /** Рейтинг строится по недельным очкам - именно он обнуляется каждый понедельник. */
   public List<TopEntry> top(int limit) {
      List<TopEntry> out = new ArrayList<>();
      this.players.forEach((uuid, values) -> {
         if (values[1] > 0) {
            out.add(new TopEntry(uuid, this.names.getOrDefault(uuid, uuid.toString().substring(0, 8)), values[1]));
         }
      });

      out.sort(Comparator.comparingLong(TopEntry::points).reversed());
      return out.size() > limit ? new ArrayList<>(out.subList(0, limit)) : out;
   }

   /**
    * Награда топ-10 сапфирами: каждый день в 00:00 по МСК разыгрывается {@link #DAILY_POOL},
    * доля игрока равна доле его очков в сумме очков топа.
    *
    * <p>Сравнивается дата, а не время: если сервер лежал в полночь, выплата пройдёт при первой
    * же проверке после запуска, а не потеряется. По этой же причине дата запоминается в
    * invest.yml - иначе рестарт в течение дня выдал бы награду повторно.
    *
    * <p>При первом запуске выплаты нет: дата просто запоминается. Иначе установка плагина
    * сразу же раздала бы сапфиры за очки, набранные до появления этой награды.
    */
   public void payoutIfNeeded() {
      String today = LocalDate.now(MOSCOW).toString();
      if (today.equals(this.lastPayout)) {
         return;
      }

      boolean first = this.lastPayout.isEmpty();
      this.lastPayout = today;

      if (first) {
         this.save();
         return;
      }

      List<TopEntry> top = this.top(TOP_SIZE);
      long total = top.stream().mapToLong(TopEntry::points).sum();
      if (total <= 0L) {
         this.save();
         return;
      }

      for (TopEntry entry : top) {
         int reward = (int)Math.round(DAILY_POOL * (double)entry.points() / total);
         if (reward > 0 && Sapphires.give(entry.uuid(), reward)) {
            this.plugin.getLogger().info("Награда за топ инвестиций: " + entry.name() + " +" + reward + " сапфиров");
         }
      }

      this.save();
   }

   /**
    * Обнуление привязано к дате начала недели, а не к таймеру: сервер может быть выключен в
    * понедельник, и тогда сброс по таймеру просто не случился бы.
    */
   public void resetWeekIfNeeded() {
      String current = LocalDate.now(MOSCOW).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toString();
      if (current.equals(this.weekStart)) {
         return;
      }

      this.weekStart = current;
      this.players.values().forEach(v -> v[1] = 0L);
      this.save();
      this.plugin.getLogger().info("Рейтинг инвестиций обнулён, неделя от " + current);
   }

   private void load() {
      if (!this.file.isFile()) {
         this.weekStart = LocalDate.now(MOSCOW).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toString();
         return;
      }

      FileConfiguration cfg = YamlConfiguration.loadConfiguration(this.file);
      this.weekStart = cfg.getString("week-start", "");
      this.lastPayout = cfg.getString("last-payout", "");

      ConfigurationSection cats = cfg.getConfigurationSection("categories");
      if (cats != null) {
         for (String id : cats.getKeys(false)) {
            this.state.put(id, new int[]{cats.getInt(id + ".level", 0), cats.getInt(id + ".progress", 0)});
         }
      }

      ConfigurationSection pls = cfg.getConfigurationSection("players");
      if (pls != null) {
         for (String raw : pls.getKeys(false)) {
            try {
               UUID uuid = UUID.fromString(raw);
               this.players.put(uuid, new long[]{pls.getLong(raw + ".points"), pls.getLong(raw + ".week")});
               String name = pls.getString(raw + ".name");
               if (name != null) {
                  this.names.put(uuid, name);
               }
            } catch (IllegalArgumentException ignored) {
               // битая запись не должна мешать загрузке остальных
            }
         }
      }
   }

   public void save() {
      FileConfiguration cfg = new YamlConfiguration();
      cfg.set("week-start", this.weekStart);
      cfg.set("last-payout", this.lastPayout);
      this.state.forEach((id, st) -> {
         cfg.set("categories." + id + ".level", st[0]);
         cfg.set("categories." + id + ".progress", st[1]);
      });
      this.players.forEach((uuid, values) -> {
         cfg.set("players." + uuid + ".points", values[0]);
         cfg.set("players." + uuid + ".week", values[1]);
         cfg.set("players." + uuid + ".name", this.names.get(uuid));
      });

      try {
         this.plugin.getDataFolder().mkdirs();
         cfg.save(this.file);
      } catch (IOException e) {
         this.plugin.getLogger().warning("Не удалось сохранить invest.yml: " + e.getMessage());
      }
   }
}
