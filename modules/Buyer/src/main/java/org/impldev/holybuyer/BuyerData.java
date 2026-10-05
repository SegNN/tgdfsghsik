package org.impldev.holybuyer;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Хранилище скупщика: сколько чего сдано и что накопили игроки.
 *
 * <p>Счётчики сдач не растут вечно, а сгорают со временем - иначе "чем реже вы сдаёте этот
 * предмет, тем выше коэффициент" не работало бы: один раз забив товар, игрок навсегда обвалил
 * бы себе цену. Сгорание считается лениво, по метке времени: держать отдельную задачу ради
 * чисел, которые смотрят только при открытии меню, незачем.
 *
 * <p>Лежит в yml, а не в MySQL, как было у прошлого скупщика: таблицы {@code koefficent} и
 * {@code playerdata} на сервере пустые, переносить нечего, а база из старого конфига
 * ({@code s7_FichaWorld}) вообще не существует.
 */
public final class BuyerData {
   /** за сколько часов счётчик сдач падает вдвое */
   private final double halfLifeHours;

   private final JavaPlugin plugin;
   private final File playersDir;
   private final File globalFile;

   private final Map<String, Double> globalSold = new ConcurrentHashMap<>();
   private long globalDecayedAt = System.currentTimeMillis();
   private final Map<UUID, Profile> profiles = new ConcurrentHashMap<>();

   public BuyerData(JavaPlugin plugin) {
      this.plugin = plugin;
      this.halfLifeHours = Math.max(1.0, plugin.getConfig().getDouble("supply.half-life-hours", 72.0));
      this.playersDir = new File(plugin.getDataFolder(), "players");
      this.globalFile = new File(plugin.getDataFolder(), "supply.yml");
      this.playersDir.mkdirs();
      this.loadGlobal();
   }

   /** Три режима переключателя авто-продажи из меню фильтра. */
   public enum AutoMode {
      OFF,
      ON,
      UNTIL_QUIT
   }

   /** Личные данные одного игрока. */
   public static final class Profile {
      public final UUID uuid;
      public String name;

      /** очки скупщика, которые можно потратить в магазине */
      public double points;
      /** сколько очков заработано за вайп - от этого растёт базовый коэффициент */
      public double lifetime;
      public final Map<String, Double> categoryPoints = new LinkedHashMap<>();
      public final Map<String, Double> sold = new HashMap<>();
      public long soldDecayedAt = System.currentTimeMillis();

      public final Set<String> autoSell = new HashSet<>();
      public AutoMode autoMode = AutoMode.OFF;

      public boolean fastSell;
      public boolean ignoreHotbar = true;
      public boolean priceHints;
      public boolean autoSellMessages = true;

      /** товар -> сколько раз куплен локальный бонус за боевые фрагменты */
      public final Map<String, Integer> localBonus = new HashMap<>();
      public int fragmentAttempts;
      public long ultaUntil;

      /** временный бустер, купленный в магазине скупщика - отдельно от бустера из /boosters */
      public int shopBoosterPercent;
      public long shopBoosterUntil;

      private boolean dirty;

      Profile(UUID uuid, String name) {
         this.uuid = uuid;
         this.name = name;
      }

      public void touch() {
         this.dirty = true;
      }
   }

   public Profile profile(UUID uuid, String name) {
      Profile profile = this.profiles.computeIfAbsent(uuid, id -> this.loadPlayer(id, name));
      if (name != null && !name.equals(profile.name)) {
         profile.name = name;
         profile.touch();
      }

      return profile;
   }

   public Profile cached(UUID uuid) {
      return this.profiles.get(uuid);
   }

   /**
    * Профиль для чтения, без попадания в кэш.
    *
    * <p>Нужен плейсхолдерам: топ скупщика перебирает всех, кто когда-либо заходил, и держать
    * их профили в памяти после одного обращения незачем.
    */
   public Profile peek(UUID uuid) {
      Profile cached = this.profiles.get(uuid);
      return cached != null ? cached : this.loadPlayer(uuid, null);
   }

   // --- сдачи -------------------------------------------------------------------------------

   /** Сколько товара сдал сервер целиком, с учётом сгорания. */
   public double globalSold(String itemId) {
      this.decayGlobal();
      return this.globalSold.getOrDefault(itemId, 0.0);
   }

   /** Сколько товара сдал лично этот игрок, с учётом сгорания. */
   public double personalSold(Profile profile, String itemId) {
      this.decayProfile(profile);
      return profile.sold.getOrDefault(itemId, 0.0);
   }

   public void addSold(Profile profile, String itemId, int amount) {
      this.decayGlobal();
      this.decayProfile(profile);
      this.globalSold.merge(itemId, (double)amount, Double::sum);
      profile.sold.merge(itemId, (double)amount, Double::sum);
      profile.touch();
   }

   public void addPoints(Profile profile, String category, double points) {
      profile.points += points;
      profile.lifetime += points;
      profile.categoryPoints.merge(category, points, Double::sum);
      profile.touch();
   }

   /** true, если очков хватило и они списаны. */
   public boolean takePoints(Profile profile, double points) {
      if (profile.points < points) {
         return false;
      }

      profile.points -= points;
      profile.touch();
      return true;
   }

   /** Категория, принёсшая игроку больше всего очков, - её бонус и применяется. */
   public String topCategory(Profile profile) {
      String best = null;
      double bestValue = 0.0;
      for (Map.Entry<String, Double> entry : profile.categoryPoints.entrySet()) {
         if (entry.getValue() > bestValue) {
            bestValue = entry.getValue();
            best = entry.getKey();
         }
      }

      return best;
   }

   private void decayGlobal() {
      long now = System.currentTimeMillis();
      double factor = this.factor(this.globalDecayedAt, now);
      if (factor >= 1.0) {
         return;
      }

      this.globalDecayedAt = now;
      this.globalSold.replaceAll((id, value) -> value * factor);
      this.globalSold.values().removeIf(value -> value < 0.01);
   }

   private void decayProfile(Profile profile) {
      long now = System.currentTimeMillis();
      double factor = this.factor(profile.soldDecayedAt, now);
      if (factor >= 1.0) {
         return;
      }

      profile.soldDecayedAt = now;
      profile.sold.replaceAll((id, value) -> value * factor);
      profile.sold.values().removeIf(value -> value < 0.01);
      profile.touch();
   }

   private double factor(long since, long now) {
      double hours = (now - since) / 3_600_000.0;
      return hours <= 0.0 ? 1.0 : Math.pow(0.5, hours / this.halfLifeHours);
   }

   // --- запись ------------------------------------------------------------------------------

   private Profile loadPlayer(UUID uuid, String name) {
      Profile profile = new Profile(uuid, name);
      File file = new File(this.playersDir, uuid + ".yml");
      if (!file.isFile()) {
         return profile;
      }

      YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
      profile.name = cfg.getString("name", name);
      profile.points = cfg.getDouble("points");
      profile.lifetime = cfg.getDouble("lifetime");
      profile.soldDecayedAt = cfg.getLong("sold-decayed-at", System.currentTimeMillis());
      profile.fastSell = cfg.getBoolean("settings.fast-sell", false);
      profile.ignoreHotbar = cfg.getBoolean("settings.ignore-hotbar", true);
      profile.priceHints = cfg.getBoolean("settings.price-hints", false);
      profile.autoSellMessages = cfg.getBoolean("settings.auto-sell-messages", true);
      profile.fragmentAttempts = cfg.getInt("fragment-attempts");
      profile.ultaUntil = cfg.getLong("ulta-until");
      profile.shopBoosterPercent = cfg.getInt("shop-booster.percent");
      profile.shopBoosterUntil = cfg.getLong("shop-booster.until");
      profile.autoSell.addAll(cfg.getStringList("auto-sell"));

      try {
         profile.autoMode = AutoMode.valueOf(cfg.getString("auto-mode", "OFF"));
      } catch (IllegalArgumentException ignored) {
         profile.autoMode = AutoMode.OFF;
      }

      // "включено до выхода" на то и до выхода: после перезахода режим снова выключен
      if (profile.autoMode == AutoMode.UNTIL_QUIT) {
         profile.autoMode = AutoMode.OFF;
      }

      readDoubles(cfg.getConfigurationSection("sold"), profile.sold);
      readDoubles(cfg.getConfigurationSection("category-points"), profile.categoryPoints);

      ConfigurationSection bonus = cfg.getConfigurationSection("local-bonus");
      if (bonus != null) {
         for (String id : bonus.getKeys(false)) {
            profile.localBonus.put(id, bonus.getInt(id));
         }
      }

      return profile;
   }

   private static void readDoubles(ConfigurationSection section, Map<String, Double> target) {
      if (section == null) {
         return;
      }

      for (String id : section.getKeys(false)) {
         target.put(id, section.getDouble(id));
      }
   }

   public void savePlayer(Profile profile) {
      if (!profile.dirty) {
         return;
      }

      YamlConfiguration cfg = new YamlConfiguration();
      cfg.set("name", profile.name);
      cfg.set("points", profile.points);
      cfg.set("lifetime", profile.lifetime);
      cfg.set("sold-decayed-at", profile.soldDecayedAt);
      cfg.set("auto-mode", profile.autoMode.name());
      cfg.set("auto-sell", new java.util.ArrayList<>(profile.autoSell));
      cfg.set("fragment-attempts", profile.fragmentAttempts);
      cfg.set("ulta-until", profile.ultaUntil);
      cfg.set("shop-booster.percent", profile.shopBoosterPercent);
      cfg.set("shop-booster.until", profile.shopBoosterUntil);
      cfg.set("settings.fast-sell", profile.fastSell);
      cfg.set("settings.ignore-hotbar", profile.ignoreHotbar);
      cfg.set("settings.price-hints", profile.priceHints);
      cfg.set("settings.auto-sell-messages", profile.autoSellMessages);
      profile.sold.forEach((id, value) -> cfg.set("sold." + id, value));
      profile.categoryPoints.forEach((id, value) -> cfg.set("category-points." + id, value));
      profile.localBonus.forEach((id, value) -> cfg.set("local-bonus." + id, value));

      try {
         cfg.save(new File(this.playersDir, profile.uuid + ".yml"));
         profile.dirty = false;
      } catch (IOException e) {
         this.plugin.getLogger().warning("Не удалось сохранить скупщика игрока " + profile.name + ": " + e.getMessage());
      }
   }

   public void unload(UUID uuid) {
      Profile profile = this.profiles.remove(uuid);
      if (profile != null) {
         this.savePlayer(profile);
      }
   }

   public void saveAll() {
      this.profiles.values().forEach(this::savePlayer);
      this.saveGlobal();
   }

   private void loadGlobal() {
      if (!this.globalFile.isFile()) {
         return;
      }

      YamlConfiguration cfg = YamlConfiguration.loadConfiguration(this.globalFile);
      this.globalDecayedAt = cfg.getLong("decayed-at", System.currentTimeMillis());
      ConfigurationSection sold = cfg.getConfigurationSection("sold");
      if (sold != null) {
         for (String id : sold.getKeys(false)) {
            this.globalSold.put(id, sold.getDouble(id));
         }
      }
   }

   public void saveGlobal() {
      YamlConfiguration cfg = new YamlConfiguration();
      cfg.set("decayed-at", this.globalDecayedAt);
      this.globalSold.forEach((id, value) -> cfg.set("sold." + id, value));

      try {
         this.plugin.getDataFolder().mkdirs();
         cfg.save(this.globalFile);
      } catch (IOException e) {
         this.plugin.getLogger().warning("Не удалось сохранить supply.yml: " + e.getMessage());
      }
   }
}
