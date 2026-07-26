package ua.fichadev.fichacustompriv.functional.listeners;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import ua.fichadev.fichacustompriv.FichaCustomPriv;
import ua.fichadev.fichacustompriv.functional.menu.PrivilegeAdminMenu;
import ua.fichadev.fichacustompriv.utils.HexUtil;

public class PrivilegeMenuListener implements Listener {
   private FichaCustomPriv plugin;
   private HashMap<UUID, Integer> inputTasks;
   private HashMap<UUID, PrivilegeMenuListener.InputType> inputTypes;

   public PrivilegeMenuListener(FichaCustomPriv plugin) {
      this.plugin = plugin;
      this.inputTasks = new HashMap<>();
      this.inputTypes = new HashMap<>();
   }

   @EventHandler
   public void onInventoryClick(InventoryClickEvent event) {
      if (event.getWhoClicked() instanceof Player) {
         Player player = (Player)event.getWhoClicked();
         String title = event.getView().getTitle();
         if (title.equals(HexUtil.color("Кастомные привилегии"))) {
            event.setCancelled(true);
            this.handleMainMenuClick(event, player);
         } else if (title.equals(HexUtil.color("Создание привилегии"))) {
            event.setCancelled(true);
            this.handleCreationMenuClick(event, player);
         } else if (title.startsWith(HexUtil.color("Заявки на к.привилегии"))) {
            event.setCancelled(true);
            this.handleAdminMenuClick(event, player);
         }
      }
   }

   private void handleMainMenuClick(InventoryClickEvent event, Player player) {
      int slot = event.getSlot();
      if (slot == 47) {
         player.closeInventory();
         this.sendInfoMessage(player);
      } else if (slot == 49) {
         this.handleCreationButtonClick(player);
      } else if (slot >= 10 && slot <= 43) {
         this.handlePrivilegeClick(event, player);
      }
   }

   private void handlePrivilegeClick(InventoryClickEvent event, Player player) {
      ItemStack clicked = event.getCurrentItem();
      if (clicked != null && clicked.getType() == Material.PAPER) {
         List<String> lore = clicked.getItemMeta().getLore();
         if (lore != null && lore.size() >= 2) {
            String statusLine = lore.get(1);
            if (statusLine.contains("Одобрен")) {
               Integer privilegeId = this.parsePrivilegeId(lore);
               if (privilegeId != null) {
                  this.givePrivilegeById(player, privilegeId);
               } else {
                  String privilegeName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
                  this.givePrivilegeToPlayer(player, privilegeName);
               }

               player.closeInventory();
            } else {
               player.sendMessage(HexUtil.color("&#00ff00▶ &fВаша кастомная привилегия еще не одобрена"));
            }
         }
      }
   }

   private Integer parsePrivilegeId(List<String> lore) {
      try {
         for (String line : lore) {
            String plain = ChatColor.stripColor(line);
            if (plain.startsWith("ID: ")) {
               String num = plain.substring(4).trim();
               return Integer.parseInt(num);
            }
         }
      } catch (Exception var6) {
      }

      return null;
   }

   private void givePrivilegeToPlayer(Player player, String privilegeName) {
      for (Map<String, String> privilege : this.plugin.getPrivilegeManager().getApprovedPrivileges()) {
         String storedText = privilege.get("privilege_text");
         String cleanStoredText = this.removeColorCodes(storedText);
         if (privilege.get("player_uuid").equals(player.getUniqueId().toString()) && cleanStoredText.equals(privilegeName)) {
            String color = privilege.get("privilege_color");
            int durationDays = Integer.parseInt(privilege.get("duration_days"));
            this.givecustomka(player.getName(), privilegeName, color, durationDays);
            break;
         }
      }
   }

   private void givePrivilegeById(Player player, int privilegeId) {
      for (Map<String, String> privilege : this.plugin.getPrivilegeManager().getApprovedPrivileges()) {
         try {
            if (Integer.parseInt(privilege.get("id")) == privilegeId && privilege.get("player_uuid").equals(player.getUniqueId().toString())) {
               String privilegeText = privilege.get("privilege_text");
               String color = privilege.get("privilege_color");
               int durationDays = Integer.parseInt(privilege.get("duration_days"));
               this.givecustomka(player.getName(), privilegeText, color, durationDays);
               return;
            }
         } catch (Exception var9) {
         }
      }
   }

   private void givecustomka(String playerName, String privilegeText, String privilegeColor, int durationDays) {
      try {
         String formattedPrefix = HexUtil.color(privilegeColor + privilegeText + " ");
         String escapedPrefix = formattedPrefix.replace("\"", "\\\"");
         this.executecmd("lp user " + playerName + " meta removeprefix 100");
         if (durationDays > 0) {
            String duration = durationDays + "d";
            this.executecmd("lp user " + playerName + " meta settempprefix 100 \"" + escapedPrefix + "\" " + duration);
         } else {
            this.executecmd("lp user " + playerName + " meta setprefix 100 \"" + escapedPrefix + "\"");
         }

         this.executecmd("lp user " + playerName + " parent add infinity");
         this.executecmd("lp user " + playerName + " permission set group.infinity true");
      } catch (Exception var81) {
         this.plugin.getLogger().severe("Ошибка при выдаче привилегии: " + var81.getMessage());
      }
   }

   private void executecmd(String command) {
      try {
         Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
         Thread.sleep(100L);
      } catch (Exception var3) {
         this.plugin.getLogger().warning("Ошибка выполнения команды: " + command);
      }
   }

   private void handleCreationMenuClick(InventoryClickEvent event, Player player) {
      int slot = event.getSlot();
      switch (slot) {
         case 19:
            player.closeInventory();
            player.sendMessage(HexUtil.color(this.plugin.getMessage("creation-cancelled")));
            this.plugin.getPrivilegeMenuManager().clearPlayerData(player);
            break;
         case 22:
            player.closeInventory();
            this.startPrefixInput(player);
            break;
         case 24:
            player.closeInventory();
            this.startColorInput(player);
            break;
         case 29:
            this.handlePrivilegeCreation(player);
      }
   }

   private void handleAdminMenuClick(InventoryClickEvent event, Player player) {
      ItemStack clicked = event.getCurrentItem();
      if (clicked != null && clicked.getType() != Material.AIR) {
         if (clicked.getType() == Material.CYAN_STAINED_GLASS_PANE || clicked.getType() == Material.ORANGE_STAINED_GLASS_PANE) {
            event.setCancelled(true);
         } else if (event.getInventory().getHolder() instanceof PrivilegeAdminMenu) {
            PrivilegeAdminMenu menu = (PrivilegeAdminMenu)event.getInventory().getHolder();
            Integer privilegeId = menu.getPrivilegeId(clicked);
            if (privilegeId != null && privilegeId != 0) {
               event.setCancelled(true);
               if (event.isLeftClick()) {
                  boolean success = this.plugin.getPrivilegeManager().approvePrivilege(privilegeId, player.getName());
                  Bukkit.getScheduler().runTask(this.plugin, () -> {
                     if (!success) {
                        player.sendMessage(HexUtil.color("&#00ff00▶ &fОшибка при одобрении привилегии!"));
                     } else {
                        player.sendMessage(HexUtil.color("&#00ff00▶ &fПривилегия одобрена"));
                        new PrivilegeAdminMenu(this.plugin, menu.getCurrentPage()).open(player);
                     }
                  });
               } else if (event.isRightClick()) {
                  boolean success = this.plugin.getPrivilegeManager().rejectPrivilege(privilegeId, player.getName());
                  Bukkit.getScheduler().runTask(this.plugin, () -> {
                     if (!success) {
                        player.sendMessage(HexUtil.color("&#00ff00▶ &fОшибка при отклонении привилегии!"));
                     } else {
                        player.sendMessage(HexUtil.color("&#00ff00▶ &fПривилегия отклонена!"));
                        new PrivilegeAdminMenu(this.plugin, menu.getCurrentPage()).open(player);
                     }
                  });
               }
            } else {
               event.setCancelled(true);
               this.handleAdminNavigation(player, clicked, menu.getCurrentPage());
            }
         }
      }
   }

   private void handleAdminNavigation(Player player, ItemStack clicked, int currentPage) {
      String displayName = clicked.getItemMeta().getDisplayName();
      if (clicked.getType() == Material.ARROW) {
         if (displayName.contains("Предыдущая")) {
            new PrivilegeAdminMenu(this.plugin, currentPage - 1).open(player);
         } else if (displayName.contains("Следующая")) {
            new PrivilegeAdminMenu(this.plugin, currentPage + 1).open(player);
         }
      } else if (clicked.getType() == Material.BARRIER) {
         player.closeInventory();
      }
   }

   @EventHandler
   public void onPlayerChat(AsyncPlayerChatEvent event) {
      Player player = event.getPlayer();
      UUID playerId = player.getUniqueId();
      if (this.inputTypes.containsKey(playerId)) {
         event.setCancelled(true);
         String message = event.getMessage();
         this.handlePlayerInput(player, message);
      }
   }

   private void handlePlayerInput(Player player, String message) {
      UUID playerId = player.getUniqueId();
      PrivilegeMenuListener.InputType inputType = this.inputTypes.get(playerId);
      if (!message.equalsIgnoreCase("отмена") && !message.equalsIgnoreCase("cancel")) {
         switch (inputType) {
            case COLOR:
               this.handleColorInput(player, message);
               break;
            case PREFIX:
               this.handlePrefixInput(player, message);
         }
      } else {
         this.cancelInput(player);
         Bukkit.getScheduler().runTask(this.plugin, () -> this.plugin.getPrivilegeMenuManager().openCreationMenu(player));
      }
   }

   private void handlePrefixInput(Player player, String prefix) {
      int maxLength = this.plugin.getConfig().getInt("settings.creation.max-prefix-length", 8);
      String cleanPrefix = this.removeColorCodes(prefix);
      if (cleanPrefix.length() <= maxLength) {
         this.plugin.getPrivilegeMenuManager().setPlayerPrefix(player, prefix);
         this.cancelInput(player);
         Bukkit.getScheduler().runTask(this.plugin, () -> this.plugin.getPrivilegeMenuManager().openCreationMenu(player));
      } else {
         player.sendMessage(HexUtil.color("&#00ff00▶ &fТекст привилегии не может быть длиннее " + maxLength + " символов! (без учета цветовых кодов)"));
         player.sendMessage(HexUtil.color("&#00ff00▶ &fТекущая длина: " + cleanPrefix.length() + " символов"));
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

   private void handleCreationButtonClick(Player player) {
      if (this.plugin.getTokenManager().hasPrivilegeTokens(player)) {
         this.plugin.getPrivilegeMenuManager().openCreationMenu(player);
      } else {
         player.sendMessage(HexUtil.color(this.plugin.getMessage("no-tokens")));
      }
   }

   private void handlePrivilegeCreation(Player player) {
      if (this.plugin.getTokenManager().consumePrivilegeToken(player)) {
         this.createCustomPrivilege(player);
      } else {
         player.sendMessage(HexUtil.color(this.plugin.getMessage("no-tokens")));
      }
   }

   private void createCustomPrivilege(Player player) {
      String color = this.plugin.getPrivilegeMenuManager().getPlayerColor(player);
      String prefix = this.plugin.getPrivilegeMenuManager().getPlayerPrefix(player);
      int durationDays = this.plugin.getTokenManager().getDurationDays(player);
      String cleanPrefix = this.removeColorCodes(prefix);
      int maxLength = this.plugin.getConfig().getInt("settings.creation.max-prefix-length", 8);
      if (cleanPrefix.length() > maxLength) {
         player.sendMessage(HexUtil.color("&#00ff00▶ &fОшибка: текст привилегии слишком длинный!"));
         this.plugin.getTokenManager().refundLastPrivilegeToken(player);
      } else {
         boolean success = this.plugin
            .getPrivilegeManager()
            .createPendingPrivilege(player.getName(), player.getUniqueId().toString(), prefix, color, durationDays);
         if (!success) {
            player.sendMessage(HexUtil.color("&#00ff00▶ &fОшибка при создании заявки"));
            this.plugin.getTokenManager().refundLastPrivilegeToken(player);
         } else {
            player.closeInventory();
            player.sendMessage(HexUtil.color(this.plugin.getMessage("success-creation")));
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
            this.plugin.getPrivilegeMenuManager().clearPlayerData(player);
            this.plugin.getLogger().info("Заявка создана: " + player.getName() + " - " + prefix + " (" + color + ")");
         }
      }
   }

   private void startColorInput(Player player) {
      this.startInput(player, PrivilegeMenuListener.InputType.COLOR, this.plugin.getMessage("enter-color"));
   }

   private void startPrefixInput(Player player) {
      int maxLength = this.plugin.getConfig().getInt("settings.creation.max-prefix-length", 8);
      String message = this.plugin.getMessage("enter-prefix") + " (макс. " + maxLength + " символов без учета цветовых кодов)";
      this.startInput(player, PrivilegeMenuListener.InputType.PREFIX, message);
   }

   private void startInput(final Player player, PrivilegeMenuListener.InputType type, final String message) {
      final UUID playerId = player.getUniqueId();
      this.inputTypes.put(playerId, type);
      int taskId = (new BukkitRunnable() {
         public void run() {
            if (PrivilegeMenuListener.this.inputTypes.containsKey(playerId)) {
               player.sendMessage(HexUtil.color(message));
            } else {
               this.cancel();
            }
         }
      }).runTaskTimer(this.plugin, 0L, 60L).getTaskId();
      this.inputTasks.put(playerId, taskId);
      player.sendMessage(HexUtil.color(message));
   }

   private void handleColorInput(Player player, String input) {
      if (input.equalsIgnoreCase("отмена")) {
         this.cancelInput(player);
         player.sendMessage(HexUtil.color(this.plugin.getMessage("input-cancelled")));
         Bukkit.getScheduler().runTask(this.plugin, () -> this.plugin.getPrivilegeMenuManager().openCreationMenu(player));
      } else {
         String colorCode = HexUtil.parseColorInput(input);
         if (colorCode.equals("&f") && !input.equalsIgnoreCase("f") && !input.equalsIgnoreCase("&f")) {
            player.sendMessage(HexUtil.color("&#00ff00▶ &fНеверный код цвета! Используйте формат: FF0000 или &c"));
         } else {
            this.plugin.getPrivilegeMenuManager().setPlayerColor(player, colorCode);
            this.cancelInput(player);
            Bukkit.getScheduler().runTask(this.plugin, () -> this.plugin.getPrivilegeMenuManager().openCreationMenu(player));
         }
      }
   }

   private void cancelInput(Player player) {
      UUID playerId = player.getUniqueId();
      this.inputTypes.remove(playerId);
      if (this.inputTasks.containsKey(playerId)) {
         Bukkit.getScheduler().cancelTask(this.inputTasks.get(playerId));
         this.inputTasks.remove(playerId);
      }
   }

   private void sendInfoMessage(Player player) {
      for (String line : this.plugin.getMessage("info-message").split("\n")) {
         player.sendMessage(HexUtil.color(line));
      }
   }

   static enum InputType {
      COLOR,
      PREFIX;

      private InputType() {
      }

      private static PrivilegeMenuListener.InputType[] $values() {
         return new PrivilegeMenuListener.InputType[]{COLOR, PREFIX};
      }
   }
}
