package org.impldev.holyclans.menu;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.impldev.holyclans.Clan;
import org.impldev.holyclans.HolyClans;

public final class MenuService implements Listener {
   private final HolyClans plugin;

   public MenuService(HolyClans plugin) {
      this.plugin = plugin;
   }

   /** Открытие отложено на тик — иначе при открытии из клик-хэндлера случаются ghost items. */
   private void defer(Player player, Runnable open) {
      Bukkit.getScheduler().runTask(this.plugin, open);
   }

   // ── Открытие меню ──────────────────────────────────────────────────────────

   public void openCreate(Player player) {
      this.defer(player, () -> player.openInventory(new CreateMenu(this.plugin).getInventory()));
   }

   public void openMain(Player player, Clan clan) {
      this.defer(player, () -> player.openInventory(new MainMenu(this.plugin, clan).getInventory()));
   }

   public void openUpgrade(Player player, Clan clan) {
      this.defer(player, () -> player.openInventory(new UpgradeMenu(this.plugin, clan).getInventory()));
   }

   public void openGlow(Player player, Clan clan) {
      this.defer(player, () -> player.openInventory(new GlowMenu(this.plugin, clan).getInventory()));
   }

   public void openMembers(Player player, Clan clan) {
      this.defer(player, () -> player.openInventory(new MembersMenu(this.plugin, clan).getInventory()));
   }

   public void openHomes(Player player, Clan clan) {
      this.defer(player, () -> player.openInventory(new HomesMenu(this.plugin, clan).getInventory()));
   }

   public void openTreasury(Player player, Clan clan) {
      this.defer(player, () -> player.openInventory(new TreasuryMenu(this.plugin, clan).getInventory()));
   }

   public void openClanList(Player player) {
      this.defer(player, () -> player.openInventory(new ClanListMenu(this.plugin).getInventory()));
   }

   // ── Обработка кликов ────────────────────────────────────────────────────────

   @EventHandler
   public void onClick(InventoryClickEvent event) {
      if (!(event.getWhoClicked() instanceof Player player)) return;
      if (!(event.getInventory().getHolder() instanceof ClanMenu menu)) return;

      event.setCancelled(true);
      if (event.getClickedInventory() != event.getView().getTopInventory()) return;

      int slot = event.getRawSlot();
      ClickType click = event.getClick();
      Clan clan = this.plugin.data().byPlayer(player.getUniqueId());

      if (menu instanceof CreateMenu) {
         this.handleCreate(player, slot);
      } else if (menu instanceof MainMenu) {
         this.handleMain(player, clan, slot);
      } else if (menu instanceof UpgradeMenu) {
         this.handleUpgrade(player, clan, slot);
      } else if (menu instanceof GlowMenu gm) {
         this.handleGlow(player, clan, gm, slot);
      } else if (menu instanceof MembersMenu mm) {
         this.handleMembers(player, clan, mm, slot, click);
      } else if (menu instanceof HomesMenu hm) {
         this.handleHomes(player, clan, hm, slot);
      } else if (menu instanceof TreasuryMenu) {
         this.handleTreasury(player, clan, slot);
      } else if (menu instanceof ClanListMenu) {
         this.handleClanList(player, slot);
      }
   }

   @EventHandler
   public void onDrag(InventoryDragEvent event) {
      if (event.getInventory().getHolder() instanceof ClanMenu) {
         event.setCancelled(true);
      }
   }

   // ── Создание клана ──────────────────────────────────────────────────────────

   private void handleCreate(Player player, int slot) {
      switch (slot) {
         case 21 -> {
            player.closeInventory();
            this.plugin.awaitingName().add(player.getUniqueId());
            player.sendMessage(org.impldev.holyclans.Texts.color(
                  "&r\n&r\n&r\n"
                        + "&r &r&x&f&f&7&0&0&0▶ Придумайте название клана и &r&x&f&f&7&0&0&0&nвведите&r&x&f&f&7&0&0&0 его в чат.&r&f &r\n"
                        + "&r&f * Название клана может содержать до 12 символов, включая символы цвета&r\n"));
         }
         case 23 -> this.openClanList(player);
         case 44 -> player.closeInventory();
      }
   }

   // ── Главное меню клана ──────────────────────────────────────────────────────

   private void handleMain(Player player, Clan clan, int slot) {
      if (clan == null) return;
      switch (slot) {
         case 4 -> this.openUpgrade(player, clan);
         case 20 -> this.openGlow(player, clan);
         case 22 -> this.openMembers(player, clan);
         case 24 -> this.openHomes(player, clan);
         case 39 -> this.openTreasury(player, clan);
         case 41 -> this.openClanList(player);
         case 52 -> player.closeInventory();
      }
   }

   // ── Улучшение клана ─────────────────────────────────────────────────────────

   private void handleUpgrade(Player player, Clan clan, int slot) {
      if (clan == null) return;
      switch (slot) {
         case 21 -> {
            // Повышение уровня
            int nextLevel = clan.level() + 1;
            int needPoints = this.plugin.getConfig().getInt("level-requirements." + nextLevel + ".points", nextLevel - 1);
            long needTreasury = this.plugin.getConfig().getLong("level-requirements." + nextLevel + ".treasury", (long) (nextLevel - 1) * 100_000L);

            if (clan.points() < needPoints) {
               player.sendMessage(org.impldev.holyclans.Texts.color(
                     "&r&x&f&f&2&2&2&2▶ &r&fУ вашего клана &r&x&f&f&2&2&2&2недостаточно очков&r&f."));
               return;
            }
            if (clan.treasury() < needTreasury) {
               player.sendMessage(org.impldev.holyclans.Texts.color(
                     "&r&x&f&f&2&2&2&2▶ &r&fУ вашего клана &r&x&f&f&2&2&2&2недостаточно средств &r&fв казне."));
               return;
            }
            clan.setPoints(clan.points() - needPoints);
            clan.setTreasury(clan.treasury() - needTreasury);
            clan.setLevel(clan.level() + 1);
            this.plugin.data().save(clan);
            player.sendMessage(org.impldev.holyclans.Texts.color(
                  "&r&x&0&0&d&8&f&f▶ &r&fУровень клана повышен до &r&x&f&f&7&0&0&0" + clan.level() + "&r&f."));
            this.openUpgrade(player, clan);
         }
         case 23 -> {
            // Купить 1 очко клана
            long cost = this.plugin.getConfig().getLong("point-cost", 15_000L);
            if (this.plugin.economy() == null || this.plugin.economy().getBalance(player) < cost) {
               player.sendMessage(org.impldev.holyclans.Texts.color(
                     "&r&x&f&f&2&2&2&2▶ &r&fУ вас недостаточно средств."));
               return;
            }
            this.plugin.economy().withdrawPlayer(player, cost);
            clan.setPoints(clan.points() + 1);
            this.plugin.data().save(clan);
            player.sendMessage(org.impldev.holyclans.Texts.color(
                  "&r&x&f&f&7&0&0&0▶ &r&fВы купили &r&x&f&f&7&0&0&01 очко &r&fклана."));
            this.openUpgrade(player, clan);
         }
         case 43 -> this.openMain(player, clan);
      }
   }

   // ── Цвет брони ──────────────────────────────────────────────────────────────

   private void handleGlow(Player player, Clan clan, GlowMenu gm, int slot) {
      if (clan == null) return;
      if (slot == 52) {
         this.openMain(player, clan);
         return;
      }
      if (slot == 46) {
         // Переключение режима подсветки
         String mode = clan.glowMode();
         String next = switch (mode) {
            case "OFF" -> "TEMP";
            case "TEMP" -> "ON";
            default -> "OFF";
         };
         clan.setGlowMode(next);
         this.plugin.data().save(clan);
         this.openGlow(player, clan);
         return;
      }
      int colorIndex = gm.colorIndexAt(slot);
      if (colorIndex >= 0) {
         clan.setGlowColor(gm.colorRgb(colorIndex));
         this.plugin.data().save(clan);
         this.openGlow(player, clan);
      }
   }

   // ── Участники ───────────────────────────────────────────────────────────────

   private void handleMembers(Player player, Clan clan, MembersMenu mm, int slot, ClickType click) {
      if (clan == null) return;
      if (slot == 52) {
         this.openMain(player, clan);
         return;
      }

      // Покупка слота
      if (mm.isLockedSlot(slot) && clan.isLeaderOrDeputy(player.getUniqueId())) {
         long cost = this.plugin.getConfig().getLong("slot-cost", 1_000_000L);
         if (this.plugin.economy() == null || this.plugin.economy().getBalance(player) < cost) {
            player.sendMessage(org.impldev.holyclans.Texts.color(
                  "&r&x&f&f&2&2&2&2▶ &r&fУ вас недостаточно средств."));
            return;
         }
         this.plugin.economy().withdrawPlayer(player, cost);
         clan.setSlots(clan.slots() + 1);
         this.plugin.data().save(clan);
         this.openMembers(player, clan);
         return;
      }

      // Действия с участниками
      java.util.UUID target = mm.memberAt(slot);
      if (target == null || !clan.isLeader(player.getUniqueId())) return;
      if (clan.isLeader(target)) return;

      if (click == ClickType.RIGHT) {
         // ПКМ — кик
         String name = clan.members().get(target).name();
         clan.members().remove(target);
         this.plugin.data().unregisterPlayer(target);
         this.plugin.clanChatActive().remove(target);
         this.plugin.data().save(clan);
         player.sendMessage(org.impldev.holyclans.Texts.color(
               "&r&x&f&f&2&2&2&2▶ &r&fИгрок &r&x&f&f&2&2&2&2" + name + "&r&x&f&f&2&2&2&2 &r&fбыл изгнан из клана."));
         this.openMembers(player, clan);
      } else if (click == ClickType.SHIFT_RIGHT) {
         // Shift+ПКМ — заместитель
         org.impldev.holyclans.ClanMember m = clan.members().get(target);
         if (m.role() == org.impldev.holyclans.ClanRole.DEPUTY) {
            m.setRole(org.impldev.holyclans.ClanRole.MEMBER);
         } else {
            m.setRole(org.impldev.holyclans.ClanRole.DEPUTY);
         }
         this.plugin.data().save(clan);
         this.openMembers(player, clan);
      } else if (click == ClickType.SHIFT_LEFT) {
         // Shift+ЛКМ — передача клана
         org.impldev.holyclans.ClanMember old = clan.members().get(clan.leader());
         if (old != null) old.setRole(org.impldev.holyclans.ClanRole.MEMBER);
         clan.setLeader(target);
         org.impldev.holyclans.ClanMember newLeader = clan.members().get(target);
         if (newLeader != null) newLeader.setRole(org.impldev.holyclans.ClanRole.LEADER);
         this.plugin.data().save(clan);
         player.sendMessage(org.impldev.holyclans.Texts.color("&r&x&0&0&d&8&f&f▶ &r&fЛидерство передано."));
         this.openMembers(player, clan);
      }
   }

   // ── Клановые точки ──────────────────────────────────────────────────────────

   private void handleHomes(Player player, Clan clan, HomesMenu hm, int slot) {
      if (clan == null) return;
      if (slot == 43) {
         this.openMain(player, clan);
         return;
      }
      int homeIdx = hm.homeIndexAt(slot);
      if (homeIdx < 0) return;
      org.bukkit.Location loc = clan.homes().get(homeIdx);
      if (loc != null) {
         player.closeInventory();
         player.teleport(loc);
         player.sendMessage(org.impldev.holyclans.Texts.color("&r&x&0&0&d&8&f&f▶ &r&fТелепортация на клановый хом."));
      }
   }

   // ── Казна ───────────────────────────────────────────────────────────────────

   private void handleTreasury(Player player, Clan clan, int slot) {
      if (clan == null) return;
      if (slot == 52) {
         this.openMain(player, clan);
         return;
      }
      long amount = switch (slot) {
         case 27 -> 1_000_000L;
         case 28 -> 100_000L;
         case 29 -> 10_000L;
         case 30 -> 1_000L;
         case 32 -> -1_000L;
         case 33 -> -10_000L;
         case 34 -> -100_000L;
         case 35 -> -1_000_000L;
         default -> 0L;
      };
      if (amount == 0) return;

      if (amount > 0) {
         // Инвестировать
         if (this.plugin.economy() == null || this.plugin.economy().getBalance(player) < amount) {
            player.sendMessage(org.impldev.holyclans.Texts.color("&r&x&f&f&2&2&2&2▶ &r&fУ вас недостаточно средств."));
            return;
         }
         this.plugin.economy().withdrawPlayer(player, amount);
         clan.setTreasury(clan.treasury() + amount);
         this.plugin.data().save(clan);
         player.sendMessage(org.impldev.holyclans.Texts.color("&r&x&0&0&d&8&f&f▶ &r&fВы инвестировали монеты в казну клана."));
      } else {
         // Снять (только лидер/зам)
         if (!clan.isLeaderOrDeputy(player.getUniqueId())) {
            player.sendMessage(org.impldev.holyclans.Texts.color("&r&x&f&f&2&2&2&2▶ &r&fТолько лидер или заместитель может снимать деньги."));
            return;
         }
         long abs = -amount;
         if (clan.treasury() < abs) {
            player.sendMessage(org.impldev.holyclans.Texts.color("&r&x&f&f&2&2&2&2▶ &r&fУ вашего клана &r&x&f&f&2&2&2&2недостаточно средств &r&fв казне."));
            return;
         }
         clan.setTreasury(clan.treasury() - abs);
         this.plugin.economy().depositPlayer(player, abs);
         this.plugin.data().save(clan);
         player.sendMessage(org.impldev.holyclans.Texts.color("&r&x&f&f&2&2&2&2▶ &r&fВы списали монеты с казны клана."));
      }
      this.openTreasury(player, clan);
   }

   // ── Список кланов ───────────────────────────────────────────────────────────

   private void handleClanList(Player player, int slot) {
      if (slot == 52) {
         Clan clan = this.plugin.data().byPlayer(player.getUniqueId());
         if (clan != null) {
            this.openMain(player, clan);
         } else {
            this.openCreate(player);
         }
      }
   }
}
