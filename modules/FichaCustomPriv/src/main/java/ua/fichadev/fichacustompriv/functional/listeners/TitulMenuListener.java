package ua.fichadev.fichacustompriv.functional.listeners;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import ua.fichadev.fichacustompriv.FichaCustomPriv;
import ua.fichadev.fichacustompriv.functional.menu.TitulAdmin;
import ua.fichadev.fichacustompriv.utils.HexUtil;

public class TitulMenuListener implements Listener {
   private FichaCustomPriv plugin;
   private HashMap<UUID, Boolean> inputWaiting;

   public TitulMenuListener(FichaCustomPriv plugin) {
      this.plugin = plugin;
      this.inputWaiting = new HashMap<>();
   }

   @EventHandler
   public void onInventoryClick(InventoryClickEvent event) {
      if (event.getWhoClicked() instanceof Player) {
         Player player = (Player)event.getWhoClicked();
         String title = event.getView().getTitle();
         if (title.equals(HexUtil.color("Кастомные титулы"))) {
            event.setCancelled(true);
            this.handleMainMenuClick(event, player);
         } else if (title.equals(HexUtil.color("Создание титула"))) {
            event.setCancelled(true);
            this.handleCreationMenuClick(event, player);
         } else if (title.startsWith(HexUtil.color("Заявки на к.титулы"))) {
            event.setCancelled(true);
            this.handleAdminMenuClick(event, player);
         }
      }
   }

   private void handleMainMenuClick(InventoryClickEvent event, Player player) {
      int slot = event.getSlot();
      ItemStack clicked = event.getCurrentItem();
      if (clicked != null && clicked.getType() != Material.AIR) {
         if (slot == 0) {
            player.closeInventory();
            this.plugin.getTitulManager().removeTitle(player.getName());
            player.sendMessage(HexUtil.color("&x&5&A&D&9&F&B▶ &fТитул убран"));
         } else if (slot == 49) {
            this.plugin.getTitulMenuManager().openCreationMenu(player);
         } else if (slot >= 10 && slot <= 43) {
            this.handleTitleClick(event, player);
         }
      }
   }

   private void handleTitleClick(InventoryClickEvent event, Player player) {
      ItemStack clicked = event.getCurrentItem();
      if (clicked != null && clicked.getType() == Material.PAPER) {
         try {
            List<String> lore = clicked.getItemMeta().getLore();
            if (lore != null && lore.size() >= 2) {
               String statusLine = lore.get(1);
               if (statusLine.contains("Одобрен")) {
                  String titleLine = lore.get(0);
                  String title = titleLine.substring(titleLine.indexOf(":") + 2).replace("&e", "").replace("&f", "").trim();
                  if (!title.isEmpty()) {
                     this.plugin.getTitulManager().giveLuckPermsTitle(player.getName(), title);
                     player.closeInventory();
                     player.sendMessage(HexUtil.color("&x&5&A&D&9&F&B▶ &fТитул установлен"));
                  }
               } else {
                  player.sendMessage(HexUtil.color(this.plugin.getMessage("title_on_moderation")));
               }
            }
         } catch (Exception var8) {
            var8.printStackTrace();
         }
      }
   }

   private void handleCreationMenuClick(InventoryClickEvent event, Player player) {
      int slot = event.getSlot();
      switch (slot) {
         case 21:
            player.closeInventory();
            String link = this.plugin.getConfig().getString("link");
            List<String> lines = this.plugin.getConfig().getStringList("linkmessage");
            HexUtil.sendClickableMessage(player, lines, link, this.plugin);
            break;
         case 31:
            player.closeInventory();
            this.startTitleInput(player);
            break;
         case 53:
            this.plugin.getTitulMenuManager().openMainMenu(player);
      }
   }

   private void handleAdminMenuClick(InventoryClickEvent event, final Player player) {
      ItemStack clicked = event.getCurrentItem();
      if (clicked != null && clicked.getType() != Material.AIR) {
         if (event.getInventory().getHolder() instanceof TitulAdmin) {
            final TitulAdmin menu = (TitulAdmin)event.getInventory().getHolder();
            Integer titleId = menu.getTitleId(clicked);
            if (titleId != null) {
               event.setCancelled(true);
               if (event.isLeftClick()) {
                  final boolean success = this.plugin.getTitulManager().approveTitle(titleId, player.getName());
                  (new BukkitRunnable() {
                     public void run() {
                        if (!success) {
                           player.sendMessage(HexUtil.color("&#00ff00▶ &fОшибка при одобрении титула!"));
                        } else {
                           player.sendMessage(HexUtil.color(TitulMenuListener.this.plugin.getMessage("admin.approve")));
                           new TitulAdmin(TitulMenuListener.this.plugin, menu.getCurrentPage()).open(player);
                        }
                     }
                  }).runTask(this.plugin);
               } else if (event.isRightClick()) {
                  final boolean success = this.plugin.getTitulManager().rejectTitle(titleId);
                  (new BukkitRunnable() {
                     public void run() {
                        if (!success) {
                           player.sendMessage(HexUtil.color("&#00ff00▶ &fОшибка при отклонении титула!"));
                        } else {
                           player.sendMessage(HexUtil.color(TitulMenuListener.this.plugin.getMessage("admin.reject")));
                           new TitulAdmin(TitulMenuListener.this.plugin, menu.getCurrentPage()).open(player);
                        }
                     }
                  }).runTask(this.plugin);
               }
            } else {
               this.handleAdminNavigation(player, clicked, menu.getCurrentPage());
            }
         }
      }
   }

   private void handleAdminNavigation(Player player, ItemStack clicked, int currentPage) {
      if (clicked.getType() == Material.ARROW) {
         String displayName = clicked.getItemMeta().getDisplayName();
         if (displayName.contains("Предыдущая")) {
            new TitulAdmin(this.plugin, currentPage - 1).open(player);
         } else if (displayName.contains("Следующая")) {
            new TitulAdmin(this.plugin, currentPage + 1).open(player);
         }
      } else if (clicked.getType() == Material.BARRIER) {
         player.closeInventory();
      }
   }

   @EventHandler
   public void onPlayerChat(AsyncPlayerChatEvent event) {
      Player player = event.getPlayer();
      UUID playerId = player.getUniqueId();
      if (this.inputWaiting.containsKey(playerId)) {
         event.setCancelled(true);
         String message = event.getMessage().trim();
         if (!message.isEmpty()) {
            this.handlePlayerInput(player, message);
         }
      }
   }

   private void handlePlayerInput(Player player, String message) {
      UUID playerId = player.getUniqueId();
      String cleanTitle = this.removeColorCodes(message);
      int maxLength = this.plugin.getConfig().getInt("settings.creation.max-title-length", 12);
      if (cleanTitle.length() > maxLength) {
         player.sendMessage(HexUtil.color("&#00ff00▶ &fТитул не может быть длиннее " + maxLength + " символов! (без учета цветовых кодов)"));
         player.sendMessage(HexUtil.color("&#00ff00▶ &fТекущая длина: " + cleanTitle.length() + " символов"));
         this.inputWaiting.remove(playerId);
      } else {
         if (!message.equalsIgnoreCase("отмена") && !message.equalsIgnoreCase("cancel")) {
            if (this.plugin.getTokenManager().consumeTitulToken(player)) {
               boolean success = this.plugin.getTitulManager().createPendingTitle(player.getName(), player.getUniqueId().toString(), message);
               if (!success) {
                  player.sendMessage(HexUtil.color("&#00ff00▶ &fОшибка при создании заявки"));
                  this.plugin.getTokenManager().refundTitulToken(player);
               } else {
                  player.sendMessage(HexUtil.color(this.plugin.getMessage("pending")));
               }
            } else {
               player.sendMessage(HexUtil.color(this.plugin.getMessage("no-tokens")));
            }
         } else {
            player.sendMessage(HexUtil.color(this.plugin.getMessage("input-cancelled")));
         }

         this.inputWaiting.remove(playerId);
      }
   }

   private String removeColorCodes(String text) {
      if (text == null) {
         return "";
      } else {
         String cleaned = text.replaceAll("&#[0-9A-Fa-f]{6}", "");
         cleaned = cleaned.replaceAll("&[0-9a-fk-or]", "");
         return cleaned.replaceAll("&&", "");
      }
   }

   private void startTitleInput(Player player) {
      UUID playerId = player.getUniqueId();
      this.inputWaiting.put(playerId, true);
      int maxLength = this.plugin.getConfig().getInt("settings.creation.max-title-length", 12);
      player.sendMessage(HexUtil.color(this.plugin.getMessage("create.chat")));
      player.sendMessage(HexUtil.color("&#00ff00▶ &fНапишите 'отмена' для отмены"));
      player.sendMessage(HexUtil.color("&#00ff00▶ &fМаксимальная длина: " + maxLength + " символов (без учета цветовых кодов)"));
   }

   @EventHandler
   public void onPlayerQuit(PlayerQuitEvent event) {
      Player player = event.getPlayer();
      this.inputWaiting.remove(player.getUniqueId());
   }
}
