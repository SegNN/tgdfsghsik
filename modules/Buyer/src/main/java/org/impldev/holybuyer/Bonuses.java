package org.impldev.holybuyer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Надбавки, которые приходят снаружи скупщика: инвестиции и бустер из HolyCore.
 *
 * <p>Читаем прямо его файлы, а не классы: скупщик должен работать и без HolyCore, а жёсткая
 * зависимость на классы уронила бы плагин при его отсутствии. Файлы перечитываются не чаще
 * раза в {@link #REFRESH_MS} - меню открывают часто, а уровни меняются редко.
 */
public final class Bonuses {
   private static final long REFRESH_MS = 10_000L;

   /**
    * Сколько процентов даёт один уровень инвестиции "Товар скупщика".
    *
    * <p>Ровно так это и описано в вики: "каждый новый уровень инвестиции повышает стоимость
    * одного случайного ресурса", а в меню фильтра такие товары подписаны "+25%" и "+50%" -
    * то есть на один товар уровень может выпасть и дважды.
    */
   private static final double INVEST_STEP = 25.0;

   /** С чего начинается розыгрыш товаров по уровням: от него зависит, кому что достанется. */
   private static final long INVEST_SEED = 0x48_4F_4C_59L;

   private final JavaPlugin plugin;
   private final Catalogue catalogue;
   private final File investFile;
   private final File boosterFile;

   private long refreshedAt;
   private int investLevel = -1;
   private Map<String, Double> investBonus = Map.of();
   private YamlConfiguration boosters = new YamlConfiguration();

   public Bonuses(JavaPlugin plugin, Catalogue catalogue) {
      this.plugin = plugin;
      this.catalogue = catalogue;
      File core = new File(plugin.getDataFolder().getParentFile(), "HolyCore");
      this.investFile = new File(core, "invest.yml");
      this.boosterFile = new File(core, "boosters.yml");
   }

   private void refresh() {
      long now = System.currentTimeMillis();
      if (now - this.refreshedAt < REFRESH_MS) {
         return;
      }

      this.refreshedAt = now;

      int level = 0;
      if (this.investFile.isFile()) {
         level = YamlConfiguration.loadConfiguration(this.investFile).getInt("categories.buyer.level", 0);
      }

      if (level != this.investLevel) {
         this.investLevel = level;
         this.investBonus = roll(level, new ArrayList<>(this.catalogue.items().keySet()));
      }

      this.boosters = this.boosterFile.isFile()
            ? YamlConfiguration.loadConfiguration(this.boosterFile)
            : new YamlConfiguration();
   }

   /**
    * Разыгрывает, каким товарам достались уровни инвестиции.
    *
    * <p>Генератор засеян постоянным числом и прокручивается с нуля, поэтому набор всегда
    * получается один и тот же: после перезапуска сервера подорожавшие товары останутся теми
    * же самыми, а с каждым новым уровнем к ним добавится ещё один.
    */
   private static Map<String, Double> roll(int level, List<String> ids) {
      Map<String, Double> out = new HashMap<>();
      if (level <= 0 || ids.isEmpty()) {
         return out;
      }

      Random random = new Random(INVEST_SEED);
      for (int i = 0; i < level; i++) {
         out.merge(ids.get(random.nextInt(ids.size())), INVEST_STEP, Double::sum);
      }

      return out;
   }

   /** Прибавка от инвестиций к конкретному товару, в процентах. */
   public double invest(String itemId) {
      this.refresh();
      return this.investBonus.getOrDefault(itemId, 0.0);
   }

   /** Прибавка от бустера Скупщика, в процентах: по 2% за уровень. */
   public double booster(UUID uuid) {
      this.refresh();
      long until = this.boosters.getLong("players." + uuid + ".boosters.buyer.until", 0L);
      if (until <= System.currentTimeMillis()) {
         return 0.0;
      }

      return this.boosters.getLong("players." + uuid + ".boosters.buyer.level", 0L) * 2.0;
   }

   public int investLevel() {
      this.refresh();
      return this.investLevel;
   }
}
