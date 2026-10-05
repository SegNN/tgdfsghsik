package org.impldev.holyclans.menu;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.impldev.holyclans.Clan;
import org.impldev.holyclans.HolyClans;
import org.impldev.holyclans.Texts;

/**
 * Главное меню клана (54 слота). Открывается по /clan, если игрок в клане.
 */
public final class MainMenu extends ClanMenu {
   private static final int[] GRAY = {0, 8, 45, 53};
   private static final int[] ORANGE = {9, 17, 18, 26, 27, 35, 36, 44};

   public MainMenu(HolyClans plugin, Clan clan) {
      this.inventory = Bukkit.createInventory(this, 54, Texts.color("&rКлан"));

      for (int s : GRAY) this.inventory.setItem(s, Menus.glass(Material.GRAY_STAINED_GLASS_PANE));
      for (int s : ORANGE) this.inventory.setItem(s, Menus.glass(Material.ORANGE_STAINED_GLASS_PANE));

      // Слот 4: Прокачать клан
      this.inventory.setItem(4, Menus.item(Material.END_CRYSTAL,
            "&r &r&x&0&0&d&8&f&f❏ Прокачать клан ❏",
            List.of("",
                  "&r &r&x&0&0&d&8&f&f&l&n▍&r&f В данном разделе ты можешь",
                  "&r &r&x&0&0&d&8&f&f&l&n▍&r&x&0&0&d&8&f&f приобрести клановые очки&r&f за",
                  "&r &r&x&0&0&d&8&f&f&l&n▍&r&f монетки. А также &r&x&0&0&d&8&f&fувеличивать",
                  "&r &r&x&0&0&d&8&f&f&l&n▍&r&x&0&0&d&8&f&f уровень клана&r&f, выполняя",
                  "&r &r&x&0&0&d&8&f&f&l▍&r&f необходимые требования.",
                  "",
                  "&r &r&x&0&0&d&8&f&f&l&n▍&r&f Чем больше очков и выше уровень,",
                  "&r &r&x&0&0&d&8&f&f&l&n▍&r&f тем больше стратегических",
                  "&r &r&x&0&0&d&8&f&f&l▍&r&f преимуществ получает ваш клан.",
                  "",
                  "&r &r&x&0&0&d&8&f&f▶ &r&fНажмите, чтобы открыть раздел")));

      // Слот 20: Цвет брони
      this.inventory.setItem(20, Menus.item(Material.LEATHER_CHESTPLATE,
            "&r &r&x&0&0&d&8&f&f❏ Цвет брони ❏",
            List.of("",
                  "&r &r&x&0&0&d&8&f&f&l&n▍&r&f В данном разделе ты",
                  "&r &r&x&0&0&d&8&f&f&l&n▍&r&f можешь выбрать &r&x&0&0&d&8&f&fцвет брони",
                  "&r &r&x&0&0&d&8&f&f&l▍&r&f для подсветки соклановцев",
                  "",
                  "&r &r&x&0&0&d&8&f&f▶ &r&fНажмите, чтобы открыть раздел")));

      // Слот 22: Голова клана
      this.inventory.setItem(22, this.clanHead(clan));

      // Слот 24: Клановые точки
      this.inventory.setItem(24, Menus.item(Material.WHITE_BED,
            "&r &r&x&0&0&d&8&f&f❏ Клановые точки ❏",
            List.of("",
                  "&r &r&x&0&0&d&8&f&f&l&n▍&r&f В данном разделе ты можешь",
                  "&r &r&x&0&0&d&8&f&f&l&n▍&r&f устанавливать &r&x&0&0&d&8&f&fклановые",
                  "&r &r&x&0&0&d&8&f&f&l▍&r&f &r&x&0&0&d&8&f&fточки &r&fдля быстрых телепортов.",
                  "",
                  "&r &r&x&0&0&d&8&f&f▶ &r&fНажмите, чтобы открыть раздел")));

      // Слот 39: Казна клана
      this.inventory.setItem(39, Menus.item(Material.GOLD_INGOT,
            "&r &r&x&0&0&d&8&f&f❏ Казна клана ❏",
            List.of("",
                  "&r &r&x&0&0&d&8&f&f&l&n▍&r&f В данном разделе ты",
                  "&r &r&x&0&0&d&8&f&f&l&n▍&r&f можешь &r&x&0&0&d&8&f&fпополнять деньгами",
                  "&r &r&x&0&0&d&8&f&f&l&n▍&r&f клановый баланс, или же",
                  "&r &r&x&0&0&d&8&f&f&l▍&r&f &r&x&0&0&d&8&f&fскоммуниздить деньги§а.",
                  "",
                  "&r &r&x&0&0&d&8&f&f&l&n▍&r&f Валюта необходима для покупки",
                  "&r &r&x&0&0&d&8&f&f&l▍&r&f различных клановых преимуществ.",
                  "",
                  "&r &r&x&0&0&d&8&f&f▶ &r&fНажмите, чтобы открыть раздел")));

      // Слот 41: Список кланов
      this.inventory.setItem(41, Menus.item(Material.PAPER,
            "&r &r&x&0&0&d&8&f&f❏ Список всех кланов ❏",
            List.of("",
                  "&r &r&x&0&0&d&8&f&f&l&n▍&r&f Подробная информация",
                  "&r &r&x&0&0&d&8&f&f&l▍&r&f по каждому клану на сервере.",
                  "",
                  "&r &r&x&0&0&d&8&f&f▶&r&f Нажмите, чтобы открыть раздел")));

      // Слот 46: Помощь
      this.inventory.setItem(46, this.helpBook());

      // Слот 52: Закрыть
      this.inventory.setItem(52, Menus.close());
   }

   private ItemStack clanHead(Clan clan) {
      ItemStack head = new ItemStack(Material.PLAYER_HEAD);
      SkullMeta meta = (SkullMeta) head.getItemMeta();
      meta.setOwningPlayer(Bukkit.getOfflinePlayer(clan.leader()));
      meta.displayName(Texts.color("&r &r&x&f&f&7&0&0&0" + clan.name() + "&r&f | &r&x&f&f&7&0&0&0"
            + clan.level() + "&r&x&f&f&7&0&0&0 уровень"));

      List<String> lore = new ArrayList<>();
      lore.add("");
      String leaderName = clan.members().containsKey(clan.leader())
            ? clan.members().get(clan.leader()).name() : "?";
      lore.add("&r &r&x&0&0&d&8&f&f&l&n▍&r&f Создатель:&r&x&0&0&d&8&f&f &r&x&0&0&d&8&f&f" + leaderName);
      lore.add("&r &r&x&0&0&d&8&f&f&l▍&r&f Клановых очков:&r&x&f&f&7&0&0&0 &r&x&f&f&7&0&0&0" + clan.points());
      lore.add("");
      lore.add("&r &r&x&0&0&d&8&f&f▼ &r&fУчастники клана:");
      lore.add("&r &r&7   " + clan.memberNames());
      lore.add("");
      lore.add("&r &r&x&0&0&d&8&f&f▶ &r&fНажмите, чтобы открыть список участников");

      meta.lore(lore.stream().map(Texts::color).toList());
      head.setItemMeta(meta);
      return head;
   }

   private ItemStack helpBook() {
      return Menus.item(Material.BOOK,
            "&r &r&x&0&0&d&8&f&fПомощь по клановой системе",
            List.of("",
                  "&r &r&x&0&0&d&8&f&f&l&n▍&r&x&0&0&d&8&f&f /clan money &r&f– баланс клана по монетам",
                  "&r &r&x&0&0&d&8&f&f&l&n▍&r&x&0&0&d&8&f&f /clan invest <сумма> &r&f– пополнить казну клана",
                  "&r &r&x&0&0&d&8&f&f&l&n▍&r&x&0&0&d&8&f&f /clan withdraw <сумма> &r&f– снять деньги с казны клана",
                  "&r &r&x&0&0&d&8&f&f&l▍&r&x&0&0&d&8&f&f /clan take <сумма> &r&f– снять деньги с казны клана",
                  "",
                  "&r &r&x&0&0&d&8&f&f&l&n▍&r&x&0&0&d&8&f&f /clan members &r&f– участники клана",
                  "&r &r&x&0&0&d&8&f&f&l&n▍&r&x&0&0&d&8&f&f /clan invite <ник> &r&f– пригласить игрока в клан",
                  "&r &r&x&0&0&d&8&f&f&l▍&r&x&0&0&d&8&f&f /clan kick <ник> &r&f– удалить игрока из клана",
                  "",
                  "&r &r&x&0&0&d&8&f&f&l&n▍&r&x&0&0&d&8&f&f /clan delete &r&f– удалить клан",
                  "&r &r&x&0&0&d&8&f&f&l▍&r&x&0&0&d&8&f&f /clan newleader <ник> &r&f– передать лидера клан",
                  "",
                  "&r &r&x&0&0&d&8&f&f&l&n▍&r&x&0&0&d&8&f&f /clan glow &r&f– выбор подсветки вашей брони",
                  "&r &r&x&0&0&d&8&f&f&l&n▍&r&x&0&0&d&8&f&f /clan pvp &r&f– вкл./выкл. урон по союзникам",
                  "&r &r&x&0&0&d&8&f&f&l&n▍&r&x&0&0&d&8&f&f /clan list &r&f– просмотр списка кланов",
                  "&r &r&x&0&0&d&8&f&f&l&n▍&r&x&0&0&d&8&f&f /clan chat &r&f– переключить свой чат на клановый чат",
                  "&r &r&x&0&0&d&8&f&f&l&n▍&r&x&0&0&d&8&f&f /clan sethome &r&f– установить клан хом",
                  "&r &r&x&0&0&d&8&f&f&l&n▍&r&x&0&0&d&8&f&f /clan home &r&f– телепортироваться на клан хом",
                  "&r &r&x&0&0&d&8&f&f&l▍&r&x&0&0&d&8&f&f /clan leave &r&f– покинуть клан",
                  ""));
   }
}
