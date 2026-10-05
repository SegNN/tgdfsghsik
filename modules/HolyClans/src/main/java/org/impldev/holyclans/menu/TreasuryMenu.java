package org.impldev.holyclans.menu;

import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.impldev.holyclans.Clan;
import org.impldev.holyclans.HolyClans;
import org.impldev.holyclans.Texts;

/**
 * Меню «Казна клана» (54 слота).
 */
public final class TreasuryMenu extends ClanMenu {

   public TreasuryMenu(HolyClans plugin, Clan clan) {
      this.inventory = Bukkit.createInventory(this, 54, Texts.color("&rКазна клана"));

      // Слот 13: Баланс казны
      this.inventory.setItem(13, Menus.item(Material.GOLD_INGOT, "",
            List.of("&r &r&x&f&f&c&9&0&0&l&n▍&r&f Казна вашего клана:",
                  "&r &r&x&f&f&c&9&0&0&l▍&r&x&f&f&c&9&0&0 &r&x&f&f&c&9&0&0"
                        + Texts.num(clan.treasury()) + "&r&x&f&f&c&9&0&0 монеток",
                  "")));

      // Инвестировать
      this.inventory.setItem(27, Menus.item(Material.GREEN_TERRACOTTA,
            "&r&x&0&5&f&b&0&0▶ &r&fИнвестировать &r&x&0&5&f&b&0&01000000&r&x&0&5&f&b&0&0 &r&fмонет"));
      this.inventory.setItem(28, Menus.item(Material.LIME_CONCRETE,
            "&r&x&0&5&f&b&0&0▶ &r&fИнвестировать &r&x&0&5&f&b&0&0100000&r&x&0&5&f&b&0&0 &r&fмонет"));
      this.inventory.setItem(29, Menus.item(Material.LIME_CONCRETE_POWDER,
            "&r&x&0&5&f&b&0&0▶ &r&fИнвестировать &r&x&0&5&f&b&0&010000&r&x&0&5&f&b&0&0 &r&fмонет"));
      this.inventory.setItem(30, Menus.item(Material.LIME_WOOL,
            "&r&x&0&5&f&b&0&0▶ &r&fИнвестировать &r&x&0&5&f&b&0&01000&r&x&0&5&f&b&0&0 &r&fмонет"));

      // Скоммуниздить
      this.inventory.setItem(32, Menus.item(Material.RED_WOOL,
            "&r&x&f&f&2&2&2&2▶ &r&fСкоммуниздить &r&x&f&f&2&2&2&21000&r&x&f&f&2&2&2&2 &r&fмонет"));
      this.inventory.setItem(33, Menus.item(Material.RED_CONCRETE_POWDER,
            "&r&x&f&f&2&2&2&2▶ &r&fСкоммуниздить &r&x&f&f&2&2&2&210000&r&x&f&f&2&2&2&2 &r&fмонет"));
      this.inventory.setItem(34, Menus.item(Material.RED_CONCRETE,
            "&r&x&f&f&2&2&2&2▶ &r&fСкоммуниздить &r&x&f&f&2&2&2&2100000&r&x&f&f&2&2&2&2 &r&fмонет"));
      this.inventory.setItem(35, Menus.item(Material.RED_TERRACOTTA,
            "&r&x&f&f&2&2&2&2▶ &r&fСкоммуниздить &r&x&f&f&2&2&2&21000000&r&x&f&f&2&2&2&2 &r&fмонет"));

      // Нижняя панель
      for (int s = 45; s <= 53; s++) {
         this.inventory.setItem(s, Menus.glass(Material.GRAY_STAINED_GLASS_PANE));
      }
      this.inventory.setItem(52, Menus.back());
   }
}
