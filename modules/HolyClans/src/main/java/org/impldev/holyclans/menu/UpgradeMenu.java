package org.impldev.holyclans.menu;

import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.impldev.holyclans.Clan;
import org.impldev.holyclans.HolyClans;
import org.impldev.holyclans.Texts;

/**
 * Меню «Улучшение клана» (45 слотов).
 */
public final class UpgradeMenu extends ClanMenu {
   private static final int[] GRAY = {0, 8, 36, 44};
   private static final int[] ORANGE = {9, 17, 18, 26, 27, 35};

   public UpgradeMenu(HolyClans plugin, Clan clan) {
      this.inventory = Bukkit.createInventory(this, 45, Texts.color("&rУлучшение клана"));

      for (int s : GRAY) this.inventory.setItem(s, Menus.glass(Material.GRAY_STAINED_GLASS_PANE));
      for (int s : ORANGE) this.inventory.setItem(s, Menus.glass(Material.ORANGE_STAINED_GLASS_PANE));

      int nextLevel = clan.level() + 1;
      int needPoints = plugin.getConfig().getInt("level-requirements." + nextLevel + ".points", nextLevel - 1);
      long needTreasury = plugin.getConfig().getLong("level-requirements." + nextLevel + ".treasury", (long)(nextLevel - 1) * 100_000L);

      // Слот 21: Повышение уровня
      this.inventory.setItem(21, Menus.item(Material.DIAMOND,
            "&r &r&x&f&f&7&0&0&0Повышение уровня клана",
            List.of("&r ",
                  "&r &r&x&f&f&7&0&0&0● &r&fВаш клан &r&x&f&f&7&0&0&0" + clan.level() + "&r&x&f&f&7&0&0&0 уровня",
                  "",
                  "&r    &r&x&f&f&7&0&0&0• &r&fДля повышения уровня требуется:",
                  "&r        &r&x&f&f&c&9&0&0- &r&x&f&f&c&9&0&0" + needPoints + "&r&x&f&f&c&9&0&0 очков клана",
                  "&r        &r&x&f&f&c&9&0&0- &r&x&f&f&c&9&0&0" + Texts.num(needTreasury) + "&r&x&f&f&c&9&0&0 монет в казне",
                  "",
                  "&r    &r&f* При повышении уровня ",
                  "&r      &r&fочки и монетки снимаются.",
                  "&r ",
                  "&r &r&x&f&f&7&0&0&0▶ &r&fНажмите, чтобы повысить уровень")));

      // Слот 23: Купить очко клана
      long pointCost = plugin.getConfig().getLong("point-cost", 15_000L);
      this.inventory.setItem(23, Menus.item(Material.EMERALD,
            "&r &r&x&0&0&d&8&f&fКлановые очки",
            List.of("&r ",
                  "&r &r&x&0&0&d&8&f&f&l&n▍&r&f У вашего клана на",
                  "&r &r&x&0&0&d&8&f&f&l▍&r&f данный момент &r&x&f&f&c&9&0&0" + clan.points() + "&r&x&f&f&c&9&0&0 очков",
                  "&r ",
                  "&r &r&x&0&0&d&8&f&f▶ &r&fКупить &r&x&f&f&c&9&0&01 очко клана &r&fза &r&x&f&f&c&9&0&0"
                        + Texts.num(pointCost) + "&r&x&f&f&c&9&0&0 монеток")));

      // Слот 43: Назад
      this.inventory.setItem(43, Menus.back());
   }
}
