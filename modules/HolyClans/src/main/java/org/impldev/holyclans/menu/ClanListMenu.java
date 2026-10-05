package org.impldev.holyclans.menu;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.impldev.holyclans.Clan;
import org.impldev.holyclans.HolyClans;
import org.impldev.holyclans.Texts;

/**
 * Меню «Список всех кланов» (54 слота).
 */
public final class ClanListMenu extends ClanMenu {
   private static final int[] CLAN_SLOTS = {
         10, 11, 12, 13, 14, 15, 16,
         19, 20, 21, 22, 23, 24, 25,
         28, 29, 30, 31, 32, 33, 34,
         37, 38, 39, 40, 41, 42, 43
   };

   public ClanListMenu(HolyClans plugin) {
      this.inventory = Bukkit.createInventory(this, 54, Texts.color("&rСписок кланов"));

      // Декорация
      for (int s = 0; s <= 8; s++) this.inventory.setItem(s, Menus.glass(Material.ORANGE_STAINED_GLASS_PANE));
      this.inventory.setItem(9, Menus.glass(Material.GRAY_STAINED_GLASS_PANE));
      this.inventory.setItem(17, Menus.glass(Material.GRAY_STAINED_GLASS_PANE));
      this.inventory.setItem(18, Menus.glass(Material.GRAY_STAINED_GLASS_PANE));
      this.inventory.setItem(26, Menus.glass(Material.GRAY_STAINED_GLASS_PANE));
      this.inventory.setItem(27, Menus.glass(Material.GRAY_STAINED_GLASS_PANE));
      this.inventory.setItem(35, Menus.glass(Material.GRAY_STAINED_GLASS_PANE));
      this.inventory.setItem(36, Menus.glass(Material.GRAY_STAINED_GLASS_PANE));
      this.inventory.setItem(44, Menus.glass(Material.GRAY_STAINED_GLASS_PANE));

      for (int s = 45; s <= 53; s++) this.inventory.setItem(s, Menus.glass(Material.GRAY_STAINED_GLASS_PANE));
      this.inventory.setItem(52, Menus.back());

      // Заголовок
      this.inventory.setItem(4, Menus.item(Material.PAPER,
            "&r &r&x&0&0&d&8&f&f❏ Список всех кланов ❏"));

      // Список кланов по очкам
      List<Clan> clans = new ArrayList<>(plugin.data().all().values());
      clans.sort(Comparator.comparingInt(Clan::points).reversed());

      int count = Math.min(clans.size(), CLAN_SLOTS.length);
      for (int i = 0; i < count; i++) {
         Clan clan = clans.get(i);
         this.inventory.setItem(CLAN_SLOTS[i], this.clanHead(clan));
      }
   }

   private ItemStack clanHead(Clan clan) {
      ItemStack head = new ItemStack(Material.PLAYER_HEAD);
      SkullMeta meta = (SkullMeta) head.getItemMeta();
      meta.setOwningPlayer(Bukkit.getOfflinePlayer(clan.leader()));
      meta.displayName(Texts.color("&r &r&x&f&f&7&0&0&0" + clan.name() + "&r&f | &r&x&f&f&7&0&0&0"
            + clan.level() + "&r&x&f&f&7&0&0&0 уровень"));

      String leaderName = clan.members().containsKey(clan.leader())
            ? clan.members().get(clan.leader()).name() : "?";

      List<String> lore = List.of("",
            "&r &r&x&0&0&d&8&f&f&l&n▍&r&f Создатель:&r&x&0&0&d&8&f&f &r&x&0&0&d&8&f&f" + leaderName,
            "&r &r&x&0&0&d&8&f&f&l▍&r&f Клановых очков:&r&x&f&f&7&0&0&0 &r&x&f&f&7&0&0&0" + clan.points(),
            "",
            "&r &r&x&0&0&d&8&f&f▼ &r&fУчастники клана:",
            "&r &r&7   " + clan.memberNames());
      meta.lore(lore.stream().map(Texts::color).toList());
      head.setItemMeta(meta);
      return head;
   }
}
