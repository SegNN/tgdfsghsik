package org.impldev.holybuyer;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Магазин скупщика: витрина и её правила.
 *
 * <p>Товары делятся на постоянные и сменные. Постоянные всегда лежат на своих местах и стоят
 * одинаково весь вайп. Сменные раз в сутки перетасовываются, и на витрину попадает лишь часть
 * из них - поэтому купить всё сразу нельзя; их цена растёт с каждой покупкой и трижды в сутки
 * сбрасывается обратно.
 *
 * <p>Первые сутки после вайпа магазин закрыт. Момент открытия считается от первого запуска
 * плагина и лежит в state-файле: привязка к дате старта сервера пережила бы перезапуск, а
 * счётчик "сколько плагин уже работает" - нет.
 */
public final class Shop {
   /** во сколько по МСК сбрасываются цены сменных товаров */
   private static final int[] RESET_HOURS = {0, 8, 16};
   private static final ZoneId MOSCOW = ZoneId.of("Europe/Moscow");

   /** Что делает товар при покупке, если это не просто выдача предмета. */
   public enum Action {
      NONE,
      EXP,
      BUYER_BOOSTER,
      ULTA
   }

   /**
    * Один товар магазина.
    *
    * @param price  начальная цена в очках скупщика
    * @param fixed  постоянный товар: не тасуется и не дорожает
    * @param give   выдать игроку сам предмет с витрины
    */
   public record Entry(String id, Material material, String name, List<String> lore, long price,
                       boolean fixed, int slot, Action action, int amount, int percent,
                       int minutes, boolean give, List<String> commands) {
   }

   private final JavaPlugin plugin;
   private final File stateFile;
   private final double growth;
   private final int rotatingVisible;
   private final List<Integer> rotatingSlots = new ArrayList<>();

   private final Map<String, Entry> entries = new LinkedHashMap<>();
   private final Map<String, Integer> purchases = new HashMap<>();
   private long openAt;
   private String resetStamp = "";

   public Shop(JavaPlugin plugin) {
      this.plugin = plugin;
      plugin.saveResource("shop.yml", false);
      this.stateFile = new File(plugin.getDataFolder(), "shop-state.yml");
      this.growth = plugin.getConfig().getDouble("shop.price-growth", 0.05);

      YamlConfiguration cfg = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "shop.yml"));
      this.rotatingVisible = cfg.getInt("rotating-visible", 10);
      for (int slot : cfg.getIntegerList("rotating-slots")) {
         this.rotatingSlots.add(slot);
      }

      ConfigurationSection section = cfg.getConfigurationSection("items");
      if (section != null) {
         for (String id : section.getKeys(false)) {
            Entry entry = read(section, id);
            if (entry != null) {
               this.entries.put(id, entry);
            } else {
               plugin.getLogger().warning("Магазин скупщика: пропущен товар " + id);
            }
         }
      }

      this.loadState();
      plugin.getLogger().info("Магазин скупщика: товаров - " + this.entries.size());
   }

   private static Entry read(ConfigurationSection section, String id) {
      Material material = Material.matchMaterial(section.getString(id + ".material", ""));
      if (material == null) {
         return null;
      }

      Action action;
      try {
         action = Action.valueOf(section.getString(id + ".action", "NONE").toUpperCase(Locale.ROOT));
      } catch (IllegalArgumentException e) {
         action = Action.NONE;
      }

      return new Entry(
            id,
            material,
            section.getString(id + ".name", id),
            section.getStringList(id + ".lore"),
            section.getLong(id + ".price", 0L),
            section.getBoolean(id + ".fixed", false),
            section.getInt(id + ".slot", -1),
            action,
            section.getInt(id + ".amount", 1),
            section.getInt(id + ".percent", 0),
            section.getInt(id + ".minutes", 60),
            section.getBoolean(id + ".give", false),
            section.getStringList(id + ".commands"));
   }

   // --- витрина -----------------------------------------------------------------------------

   /** Открылся ли магазин: первые сутки после вайпа он не работает. */
   public boolean open() {
      return System.currentTimeMillis() >= this.openAt;
   }

   public long opensIn() {
      return Math.max(0L, this.openAt - System.currentTimeMillis());
   }

   /**
    * Что сегодня лежит на витрине: слот -> товар.
    *
    * <p>Тасовка засеяна датой, поэтому в течение суток раскладка одна и та же для всех и
    * переживает перезапуск, а на следующий день сама собой становится другой.
    */
   public Map<Integer, Entry> showcase() {
      this.rollOver();

      Map<Integer, Entry> out = new LinkedHashMap<>();
      List<Entry> rotating = new ArrayList<>();
      for (Entry entry : this.entries.values()) {
         if (entry.fixed()) {
            if (entry.slot() >= 0) {
               out.put(entry.slot(), entry);
            }
         } else {
            rotating.add(entry);
         }
      }

      Collections.shuffle(rotating, new Random(LocalDate.now(MOSCOW).toEpochDay()));
      int shown = 0;
      for (int slot : this.rotatingSlots) {
         // Постоянные товары стоят на своих местах всегда, сменные занимают то, что осталось.
         if (out.containsKey(slot) || shown >= this.rotatingVisible || shown >= rotating.size()) {
            continue;
         }

         out.put(slot, rotating.get(shown++));
      }

      return out;
   }

   /** Цена с учётом того, сколько раз товар уже купили после последнего сброса. */
   public long price(Entry entry) {
      if (entry.fixed()) {
         return entry.price();
      }

      this.rollOver();
      int bought = this.purchases.getOrDefault(entry.id(), 0);
      return Math.round(entry.price() * (1.0 + this.growth * bought));
   }

   public void bought(Entry entry) {
      if (entry.fixed()) {
         return;
      }

      this.purchases.merge(entry.id(), 1, Integer::sum);
      this.saveState();
   }

   /** Сбрасывает накрутку цен, когда наступил очередной из {@link #RESET_HOURS}. */
   private void rollOver() {
      LocalDateTime now = LocalDateTime.now(MOSCOW);
      int hour = 0;
      for (int candidate : RESET_HOURS) {
         if (now.getHour() >= candidate) {
            hour = candidate;
         }
      }

      String stamp = now.toLocalDate() + "-" + hour;
      if (stamp.equals(this.resetStamp)) {
         return;
      }

      this.resetStamp = stamp;
      this.purchases.clear();
      this.saveState();
   }

   public Map<String, Entry> entries() {
      return Collections.unmodifiableMap(this.entries);
   }

   // --- состояние ---------------------------------------------------------------------------

   private void loadState() {
      long closedMs = this.plugin.getConfig().getLong("shop.closed-hours", 24L) * 3_600_000L;
      if (!this.stateFile.isFile()) {
         this.openAt = System.currentTimeMillis() + closedMs;
         this.saveState();
         return;
      }

      YamlConfiguration cfg = YamlConfiguration.loadConfiguration(this.stateFile);
      this.openAt = cfg.getLong("open-at", System.currentTimeMillis() + closedMs);
      this.resetStamp = cfg.getString("reset-stamp", "");
      ConfigurationSection bought = cfg.getConfigurationSection("purchases");
      if (bought != null) {
         for (String id : bought.getKeys(false)) {
            this.purchases.put(id, bought.getInt(id));
         }
      }
   }

   private void saveState() {
      YamlConfiguration cfg = new YamlConfiguration();
      cfg.set("open-at", this.openAt);
      cfg.set("reset-stamp", this.resetStamp);
      this.purchases.forEach((id, value) -> cfg.set("purchases." + id, value));

      try {
         this.plugin.getDataFolder().mkdirs();
         cfg.save(this.stateFile);
      } catch (IOException e) {
         this.plugin.getLogger().warning("Не удалось сохранить shop-state.yml: " + e.getMessage());
      }
   }
}
