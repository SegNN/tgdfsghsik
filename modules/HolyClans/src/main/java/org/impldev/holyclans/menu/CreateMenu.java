package org.impldev.holyclans.menu;

import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.impldev.holyclans.HolyClans;
import org.impldev.holyclans.Texts;

/**
 * Меню «Создание клана» (45 слотов). Открывается по /clan, если игрок не в клане.
 */
public final class CreateMenu extends ClanMenu {

   public CreateMenu(HolyClans plugin) {
      this.inventory = Bukkit.createInventory(this, 45, Texts.color("&rСоздание клана"));

      this.inventory.setItem(21, Menus.item(Material.TARGET,
            "&r &r&x&f&f&7&0&0&0❏ Создать клан ❏",
            List.of("",
                  "&r &r&x&f&f&7&0&0&0&l&n▍&r&x&d&9&d&9&d&9 &r&x&d&9&d&9&d&9В название разрешено использовать только",
                  "&r &r&x&f&f&7&0&0&0&l▍&r&x&d&9&d&9&d&9 &r&x&d&9&d&9&d&9цифры и буквы на латинице, кириллице.",
                  "",
                  "&r &r&x&f&f&7&0&0&0▶ &r&x&e&b&e&b&e&bНажмите, чтобы создать клан за &r&x&f&f&c&9&0&05 000 000 монеток")));

      this.inventory.setItem(23, Menus.item(Material.PAPER,
            "&r &r&x&0&0&d&8&f&f❏ Список всех кланов ❏",
            List.of("",
                  "&r &r&x&0&0&d&8&f&f&l&n▍&r&f Подробная информация",
                  "&r &r&x&0&0&d&8&f&f&l▍&r&f по каждому клану на сервере.",
                  "",
                  "&r &r&x&0&0&d&8&f&f▶&r&f Нажмите, чтобы открыть раздел")));

      this.inventory.setItem(44, Menus.close());
   }
}
