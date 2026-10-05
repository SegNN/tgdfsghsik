package org.impldev.holybuyer;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Вся арифметика скупщика.
 *
 * <p>Цена товара складывается из двух независимых частей.
 *
 * <p>Первая - <b>стандартная цена</b>. Она общая для сервера и падает по мере того, как товар
 * сдают: чем больше алмазов вынесли на рынок, тем дешевле алмаз для всех. Начинается вайп с
 * цены из вики ("Начальная цена"), ниже {@code supply.min-factor} от неё не опускается, а
 * счётчик сдач постепенно сгорает, так что забытый товар со временем дорожает обратно.
 *
 * <p>Вторая - набор надбавок, которые складываются между собой и умножают стандартную цену:
 * личный коэффициент за частоту сдач ("Сдали мало/много"), базовый коэффициент за накопленные
 * очки, бонус за топовую категорию, буст от инвестиций, локальный бонус за боевые фрагменты,
 * бустер Скупщика и ульта. Именно сумма надбавок и показана в описании товара построчно.
 */
public final class Prices {
   private final Catalogue catalogue;
   private final BuyerData data;
   private final Bonuses bonuses;

   private final double globalTarget;
   private final double globalMin;
   private final double personalTarget;
   private final double personalMax;
   private final double personalMin;
   private final double coefStep;
   private final double coefPercent;
   private final double coefStingerPercent;
   private final double coefCap;
   private final double ultaBonus;
   private final double localStep;

   public Prices(JavaPlugin plugin, Catalogue catalogue, BuyerData data, Bonuses bonuses) {
      this.catalogue = catalogue;
      this.data = data;
      this.bonuses = bonuses;

      this.globalTarget = plugin.getConfig().getDouble("supply.global-target-coins", 50_000_000.0);
      this.globalMin = plugin.getConfig().getDouble("supply.min-factor", 0.25);
      this.personalTarget = plugin.getConfig().getDouble("supply.personal-target-coins", 500_000.0);
      this.personalMax = plugin.getConfig().getDouble("supply.personal-max-percent", 300.0);
      this.personalMin = plugin.getConfig().getDouble("supply.personal-min-percent", -60.0);
      this.coefStep = Math.max(1.0, plugin.getConfig().getDouble("multiplier.points-per-step", 1000.0));
      this.coefPercent = plugin.getConfig().getDouble("multiplier.percent-per-step", 1.0);
      this.coefStingerPercent = plugin.getConfig().getDouble("multiplier.percent-per-step-stinger", 2.0);
      this.coefCap = plugin.getConfig().getDouble("multiplier.cap-percent", 400.0);
      this.ultaBonus = plugin.getConfig().getDouble("ulta.bonus-percent", 10.0);
      this.localStep = plugin.getConfig().getDouble("fragments.bonus-percent", 20.0);
   }

   /** Разбор цены одного товара для конкретного игрока - ровно то, что печатается в описании. */
   public record Breakdown(BuyerItem item, double personal, double baseCoef, double categoryBonus,
                           double invest, double localBonus, double booster, double ulta,
                           double standard, double current, double points) {

      /** Сумма всех надбавок в процентах. */
      public double total() {
         return this.personal + this.baseCoef + this.categoryBonus
               + this.invest + this.localBonus + this.booster + this.ulta;
      }
   }

   public Breakdown breakdown(Player player, BuyerData.Profile profile, BuyerItem item) {
      double standard = this.standard(item);
      double personal = this.personal(profile, item);
      double baseCoef = this.baseCoef(player, profile);
      double categoryBonus = item.category().equals(this.data.topCategory(profile))
            ? this.catalogue.category(item.category()).topBonus()
            : 0.0;
      double invest = this.bonuses.invest(item.id());
      double localBonus = profile.localBonus.getOrDefault(item.id(), 0) * this.localStep;
      // Бустер из /boosters держится до конца вайпа, а купленный в магазине - на час;
      // складываются, потому что это два разных улучшения одного и того же множителя.
      double booster = this.bonuses.booster(profile.uuid)
            + (profile.shopBoosterUntil > System.currentTimeMillis() ? profile.shopBoosterPercent : 0);
      double ulta = profile.ultaUntil > System.currentTimeMillis() ? this.ultaBonus : 0.0;

      double multiplier = 1.0 + (personal + baseCoef + categoryBonus + invest + localBonus + booster + ulta) / 100.0;
      double current = standard * Math.max(0.0, multiplier);
      double points = item.points() * (1.0 + personal / 100.0);

      return new Breakdown(item, personal, baseCoef, categoryBonus, invest, localBonus, booster, ulta,
            standard, current, Math.max(0.0, points));
   }

   /**
    * Общая для сервера цена товара.
    *
    * <p>Порог считается в монетах, а не в штуках: 10 000 булыжника и 10 000 незеритовых слитков
    * - совершенно разный объём рынка, и один порог в штуках обвалил бы дорогие товары мгновенно.
    */
   public double standard(BuyerItem item) {
      if (item.price() <= 0.0) {
         return 0.0;
      }

      double target = this.globalTarget / item.price();
      double factor = 1.0 / (1.0 + this.data.globalSold(item.id()) / target);
      return item.price() * Math.max(this.globalMin, factor);
   }

   /** Личный коэффициент за частоту сдач: от {@code +300%} до {@code -60%}. */
   public double personal(BuyerData.Profile profile, BuyerItem item) {
      if (item.price() <= 0.0) {
         return 0.0;
      }

      double cap = this.personalTarget / item.price();
      double share = Math.min(1.0, this.data.personalSold(profile, item.id()) / cap);
      return this.personalMax - (this.personalMax - this.personalMin) * share;
   }

   /**
    * Базовый коэффициент за накопленные очки.
    *
    * <p>Обладателям Stinger каждый шаг даёт вдвое больше - так это и описано в вики.
    */
   public double baseCoef(Player player, BuyerData.Profile profile) {
      double per = player != null && player.hasPermission("buyer.stinger")
            ? this.coefStingerPercent
            : this.coefPercent;
      return Math.min(this.coefCap, Math.floor(profile.lifetime / this.coefStep) * per);
   }

   /** Цена очередной покупки локального бонуса в боевых фрагментах: 4n² + 8. */
   public int fragmentCost(int attempt) {
      int n = Math.max(1, attempt);
      return 4 * n * n + 8;
   }
}
