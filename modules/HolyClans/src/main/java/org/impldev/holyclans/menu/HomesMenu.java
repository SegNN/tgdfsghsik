package org.impldev.holyclans.menu;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.impldev.holyclans.Clan;
import org.impldev.holyclans.HolyClans;
import org.impldev.holyclans.Texts;

/**
 * Меню «Клановые точки» (45 слотов) — 3 домашние точки, гейтятся наигрышем лидера.
 */
public final class HomesMenu extends ClanMenu {
   private static final int[] HOME_SLOTS = {20, 22, 24};
   private static final int[] GRAY = {0, 8, 36, 44};
   private static final int[] ORANGE = {9, 17, 18, 26, 27, 35};

   private final Map<Integer, Integer> slotToHome = new HashMap<>();

   public HomesMenu(HolyClans plugin, Clan clan) {
      this.inventory = Bukkit.createInventory(this, 45, Texts.color("&rКлановые точки"));

      for (int s : GRAY) this.inventory.setItem(s, Menus.glass(Material.GRAY_STAINED_GLASS_PANE));
      for (int s : ORANGE) this.inventory.setItem(s, Menus.glass(Material.ORANGE_STAINED_GLASS_PANE));

      int leaderHours = this.getLeaderHours(clan);

      for (int i = 0; i < 3; i++) {
         int slot = HOME_SLOTS[i];
         int threshold = plugin.getConfig().getInt("home-points." + (i + 1), (i + 1) * 10);
         this.slotToHome.put(slot, i);

         if (leaderHours >= threshold) {
            // Точка разблокирована
            if (clan.homes().containsKey(i)) {
               // Точка установлена — зелёная кровать
               org.bukkit.Location loc = clan.homes().get(i);
               this.inventory.setItem(slot, Menus.item(Material.GREEN_BED, "",
                     List.of("&r &r&x&0&5&f&b&0&0▶ &r&fКлановая точка &r&x&0&5&f&b&0&0установлена",
                           "",
                           "&r  &r&fМир: &r&x&0&0&d&8&f&f" + loc.getWorld().getName(),
                           "&r  &r&fX: &r&x&0&0&d&8&f&f" + (int) loc.getX()
                                 + " &r&fY: &r&x&0&0&d&8&f&f" + (int) loc.getY()
                                 + " &r&fZ: &r&x&0&0&d&8&f&f" + (int) loc.getZ(),
                           "",
                           "&r &r&x&0&0&d&8&f&f▶ &r&fНажмите, чтобы телепортироваться")));
            } else {
               // Точка доступна, но не установлена
               this.inventory.setItem(slot, Menus.item(Material.YELLOW_BED, "",
                     List.of("&r &r&x&f&f&7&0&0&0▶ &r&fКлановая точка &r&x&f&f&7&0&0&0доступна",
                           "",
                           "&r  &r&f* Используйте &r&x&0&0&d&8&f&f/clan sethome",
                           "&r    &r&fчтобы установить точку.",
                           "")));
            }
         } else {
            // Точка недоступна
            int remaining = threshold - leaderHours;
            this.inventory.setItem(slot, Menus.item(Material.RED_BED, "",
                  List.of("&r &r&x&f&f&2&2&2&2▶ &r&fКлановая точка &r&x&f&f&2&2&2&2недоступна",
                        "",
                        "&r  &r&f* Вашему лидеру нужно отыграть",
                        "&r    &r&fещё &r&x&f&f&2&2&2&2" + remaining + "&r&x&f&f&2&2&2&2 часов.",
                        "")));
         }
      }

      this.inventory.setItem(43, Menus.back());
   }

   private int getLeaderHours(Clan clan) {
      try {
         OfflinePlayer leader = Bukkit.getOfflinePlayer(clan.leader());
         int ticks = leader.getStatistic(Statistic.PLAY_ONE_MINUTE);
         return ticks / 72000;
      } catch (Exception e) {
         return 0;
      }
   }

   /** Индекс домашней точки по слоту, или -1. */
   public int homeIndexAt(int slot) {
      Integer idx = this.slotToHome.get(slot);
      return idx == null ? -1 : idx;
   }
}
