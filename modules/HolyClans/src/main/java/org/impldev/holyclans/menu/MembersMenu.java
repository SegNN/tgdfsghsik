package org.impldev.holyclans.menu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.impldev.holyclans.Clan;
import org.impldev.holyclans.ClanMember;
import org.impldev.holyclans.ClanRole;
import org.impldev.holyclans.HolyClans;
import org.impldev.holyclans.Texts;

/**
 * Меню «Список участников» (54 слота).
 */
public final class MembersMenu extends ClanMenu {
   // Позиции для участников и заблокированных слотов
   private static final int[] MEMBER_SLOTS = {19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37};
   private static final int[] HEADER_ORANGE = {0, 1, 2, 3, 5, 6, 7, 8};

   private final Map<Integer, UUID> memberBySlot = new HashMap<>();
   private final Set<Integer> lockedSlots = new HashSet<>();

   public MembersMenu(HolyClans plugin, Clan clan) {
      this.inventory = Bukkit.createInventory(this, 54, Texts.color("&rСписок участников"));

      // Заголовок
      for (int s : HEADER_ORANGE) {
         this.inventory.setItem(s, Menus.glass(Material.ORANGE_STAINED_GLASS_PANE));
      }

      // Слот 4: голова клана
      this.inventory.setItem(4, this.clanHead(clan));

      // Разделитель
      for (int s = 9; s <= 17; s++) {
         this.inventory.setItem(s, Menus.glass(Material.GRAY_STAINED_GLASS_PANE));
      }
      this.inventory.setItem(18, Menus.glass(Material.GRAY_STAINED_GLASS_PANE));
      this.inventory.setItem(26, Menus.glass(Material.GRAY_STAINED_GLASS_PANE));
      this.inventory.setItem(27, Menus.glass(Material.GRAY_STAINED_GLASS_PANE));
      this.inventory.setItem(35, Menus.glass(Material.GRAY_STAINED_GLASS_PANE));
      this.inventory.setItem(36, Menus.glass(Material.GRAY_STAINED_GLASS_PANE));

      // Заполняем участников (без лидера — он на голове)
      List<ClanMember> members = new ArrayList<>();
      for (ClanMember m : clan.members().values()) {
         if (!clan.isLeader(m.uuid())) {
            members.add(m);
         }
      }

      int maxSlots = clan.slots() - 1; // слотов для участников (без лидера)
      for (int i = 0; i < MEMBER_SLOTS.length; i++) {
         int slot = MEMBER_SLOTS[i];
         if (i < members.size()) {
            // Есть участник
            ClanMember m = members.get(i);
            this.inventory.setItem(slot, this.memberHead(m));
            this.memberBySlot.put(slot, m.uuid());
         } else if (i < maxSlots) {
            // Открытый но пустой слот — ничего не ставим (пустой)
         } else {
            // Заблокированный слот
            long cost = plugin.getConfig().getLong("slot-cost", 1_000_000L);
            this.inventory.setItem(slot, Menus.item(Material.RED_STAINED_GLASS_PANE, "",
                  List.of("&r &r&x&f&f&2&2&2&2▶ &r&fЭтот слот вам &r&x&f&f&2&2&2&2недоступен&r&f.",
                        "",
                        "&r  &r&f* Вы можете открыть этот слот",
                        "&r    &r&fза &r&x&f&f&c&9&0&0" + Texts.num(cost) + "&r&x&f&f&c&9&0&0 монеток&r&f, чтобы пригласить",
                        "&r    &r&fеще одного игрока.",
                        "",
                        "&r &r&x&f&f&2&2&2&2▶ &r&fОткрыть слот за &r&x&f&f&c&9&0&0" + Texts.num(cost) + "&r&x&f&f&c&9&0&0 монеток")));
            this.lockedSlots.add(slot);
         }
      }

      // Нижняя панель
      for (int s = 38; s <= 53; s++) {
         if (this.inventory.getItem(s) == null) {
            this.inventory.setItem(s, Menus.glass(Material.GRAY_STAINED_GLASS_PANE));
         }
      }
      this.inventory.setItem(52, Menus.back());
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
            "&r &r&7   " + clan.memberNames(),
            "",
            "&r &r&x&0&0&d&8&f&f▶ &r&fНажмите, чтобы открыть список участников");
      meta.lore(lore.stream().map(Texts::color).toList());
      head.setItemMeta(meta);
      return head;
   }

   private ItemStack memberHead(ClanMember m) {
      ItemStack head = new ItemStack(Material.PLAYER_HEAD);
      SkullMeta meta = (SkullMeta) head.getItemMeta();
      meta.setOwningPlayer(Bukkit.getOfflinePlayer(m.uuid()));
      meta.displayName(net.kyori.adventure.text.Component.empty());

      String roleName = switch (m.role()) {
         case LEADER -> "Лидер";
         case DEPUTY -> "Заместитель";
         case MEMBER -> "Участник";
      };

      List<String> lore = List.of(
            "&r &r&x&0&0&d&8&f&f&l&n▍&r&f Участник клана: &r&x&0&0&d&8&f&f" + m.name(),
            "&r &r&x&0&0&d&8&f&f&l▍&r&f Роль: &r&x&0&0&d&8&f&f" + roleName,
            "",
            "&r &r&x&f&f&7&0&0&0▶ &r&fНажмите ПКМ, чтобы исключить участника.",
            "&r &r&x&f&f&7&0&0&0▶ &r&fНажмите Shift + ПКМ, чтобы назначить заместителем.",
            "&r &r&x&f&f&7&0&0&0▶ &r&fНажмите Shift + ЛКМ, чтобы передать клан участнику.");
      meta.lore(lore.stream().map(Texts::color).toList());
      head.setItemMeta(meta);
      return head;
   }

   /** UUID участника по слоту, или null. */
   public UUID memberAt(int slot) {
      return this.memberBySlot.get(slot);
   }

   /** Является ли данный слот заблокированным (красное стекло). */
   public boolean isLockedSlot(int slot) {
      return this.lockedSlots.contains(slot);
   }
}
