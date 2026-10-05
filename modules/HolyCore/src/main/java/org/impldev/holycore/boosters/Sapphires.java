package org.impldev.holycore.boosters;

import java.util.UUID;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Сапфиры ☀ - это баланс PlayerPoints.
 *
 * <p>Работаем через его API, а не через консольную команду "p take", как это делает
 * HolyExpKeeper. Причина простая: {@code dispatchCommand} возвращает true, если команда была
 * обработана, а не если списание прошло, - покупку можно было бы засчитать без оплаты. У
 * {@link PlayerPointsAPI#take} возвращаемое значение означает именно результат списания, и сама
 * проверка с вычитанием происходит внутри, без гонки между чтением баланса и списанием.
 *
 * <p>Через Vault сапфиры недоступны намеренно: в конфиге PlayerPoints стоит {@code vault: false},
 * а Vault на этом сервере отдаёт монеты Essentials - другую валюту.
 */
public final class Sapphires {
   private static PlayerPointsAPI api;

   private Sapphires() {
   }

   private static PlayerPointsAPI api() {
      if (api == null && Bukkit.getPluginManager().isPluginEnabled("PlayerPoints")) {
         api = PlayerPoints.getInstance().getAPI();
      }

      return api;
   }

   public static long balance(Player player) {
      PlayerPointsAPI points = api();
      return points == null ? 0L : points.look(player.getUniqueId());
   }

   /** true только если списание действительно произошло. */
   public static boolean take(Player player, int amount) {
      PlayerPointsAPI points = api();
      if (points == null) {
         return false;
      }

      UUID uuid = player.getUniqueId();
      return points.look(uuid) >= amount && points.take(uuid, amount);
   }

   /**
    * Начисление по UUID: награда за топ инвестиций выдаётся и тем, кого сейчас нет на сервере,
    * поэтому {@link Player} здесь не подходит.
    */
   public static boolean give(UUID uuid, int amount) {
      PlayerPointsAPI points = api();
      return points != null && amount > 0 && points.give(uuid, amount);
   }
}
