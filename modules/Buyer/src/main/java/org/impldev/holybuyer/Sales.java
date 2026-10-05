package org.impldev.holybuyer;

import java.util.LinkedHashMap;
import java.util.Map;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Сама продажа: посчитать и заплатить.
 *
 * <p>Цена берётся один раз на товар, до списания, и применяется ко всей пачке. Иначе продажа
 * стака пересчитывала бы личный коэффициент после каждой штуки, и игрок получал бы за стак
 * заметно меньше, чем ему обещало описание в меню.
 */
public final class Sales {
   private final Catalogue catalogue;
   private final BuyerData data;
   private final Prices prices;
   private final Economy economy;

   public Sales(Catalogue catalogue, BuyerData data, Prices prices, Economy economy) {
      this.catalogue = catalogue;
      this.data = data;
      this.prices = prices;
      this.economy = economy;
   }

   /** Итог продажи. */
   public record Result(int items, double coins, double points) {
      public boolean empty() {
         return this.items == 0;
      }
   }

   /**
    * Что дадут за содержимое слотов, ничего не меняя.
    *
    * @param filter если не null, берутся только товары, чей id проходит фильтр
    */
   public Result quote(Player player, BuyerData.Profile profile, Inventory inventory,
                       int from, int to, java.util.function.Predicate<BuyerItem> filter) {
      Map<BuyerItem, Integer> counts = this.collect(inventory, from, to, filter);
      return this.total(player, profile, counts);
   }

   /**
    * Продаёт содержимое слотов и начисляет монеты с очками.
    *
    * <p>Товары, которых скупщик не принимает, остаются на месте: игрок положил их в меню сам и
    * должен получить их обратно, а не потерять.
    */
   public Result sell(Player player, BuyerData.Profile profile, Inventory inventory,
                      int from, int to, java.util.function.Predicate<BuyerItem> filter) {
      Map<BuyerItem, Integer> counts = this.collect(inventory, from, to, filter);
      if (counts.isEmpty()) {
         return new Result(0, 0.0, 0.0);
      }

      // Очки за каждый товар считаем до единого списания: как только счётчик сдач вырастет,
      // личный коэффициент упадёт, и оставшиеся товары посчитались бы уже по новой цене.
      Map<BuyerItem, Double> pointsPer = new LinkedHashMap<>();
      counts.forEach((item, amount) -> pointsPer.put(item, this.prices.breakdown(player, profile, item).points()));
      Result result = this.total(player, profile, counts);

      for (int slot = from; slot < to; slot++) {
         ItemStack stack = inventory.getItem(slot);
         BuyerItem item = this.catalogue.match(stack);
         if (item != null && (filter == null || filter.test(item))) {
            inventory.setItem(slot, null);
         }
      }

      counts.forEach((item, amount) -> {
         this.data.addSold(profile, item.id(), amount);
         this.data.addPoints(profile, item.category(), pointsPer.get(item) * amount);
      });

      if (result.coins() > 0.0 && this.economy != null) {
         this.economy.depositPlayer(player, result.coins());
      }

      this.data.savePlayer(profile);
      return result;
   }

   private Map<BuyerItem, Integer> collect(Inventory inventory, int from, int to,
                                           java.util.function.Predicate<BuyerItem> filter) {
      Map<BuyerItem, Integer> counts = new LinkedHashMap<>();
      for (int slot = from; slot < to; slot++) {
         ItemStack stack = inventory.getItem(slot);
         BuyerItem item = this.catalogue.match(stack);
         if (item != null && (filter == null || filter.test(item))) {
            counts.merge(item, stack.getAmount(), Integer::sum);
         }
      }

      return counts;
   }

   private Result total(Player player, BuyerData.Profile profile, Map<BuyerItem, Integer> counts) {
      int items = 0;
      double coins = 0.0;
      double points = 0.0;

      for (Map.Entry<BuyerItem, Integer> entry : counts.entrySet()) {
         Prices.Breakdown breakdown = this.prices.breakdown(player, profile, entry.getKey());
         items += entry.getValue();
         coins += breakdown.current() * entry.getValue();
         points += breakdown.points() * entry.getValue();
      }

      return new Result(items, coins, points);
   }
}
