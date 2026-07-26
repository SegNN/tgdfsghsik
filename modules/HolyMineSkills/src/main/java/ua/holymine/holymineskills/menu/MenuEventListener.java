package ua.holymine.holymineskills.menu;

import java.util.concurrent.CompletableFuture;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import ua.holymine.holymineskills.bonus.BonusSystem;
import ua.holymine.holymineskills.database.DatabaseManager;

public class MenuEventListener implements Listener {
   private final DatabaseManager databaseManager;

   public MenuEventListener(DatabaseManager databaseManager) {
      this.databaseManager = databaseManager;
   }

   @EventHandler(
      priority = EventPriority.HIGH
   )
   public void onInventoryClick(InventoryClickEvent event) {
      try {
         if (!(event.getWhoClicked() instanceof Player)) {
            return;
         }

         Player player = (Player)event.getWhoClicked();
         Inventory inventory = event.getInventory();
         if (!(inventory.getHolder() instanceof MenuManager.MenuHolder)) {
            return;
         }

         event.setCancelled(true);
         this.handleMenuClick(player, event.getView().getTitle(), event.getRawSlot());
      } catch (Exception var4) {
      }
   }

   private void handleMenuClick(Player player, String inventoryTitle, int slot) {
      try {
         int page = inventoryTitle != null && inventoryTitle.trim().endsWith("2") ? 2 : 1;
         BonusSystem.Bonus bonus = BonusSystem.getBonusByPageAndSlot(page, slot);
         if (bonus != null) {
            this.handleBonusClick(player, bonus);
            return;
         }
      } catch (Exception var6) {
      }
   }

   private void handleBonusClick(Player player, BonusSystem.Bonus bonus) {
      try {
         String bonusName = bonus.getEffect().getDisplayName();
         int level = bonus.getLevel();
         int requiredPoints = bonus.getRequiredPoints();

         try {
            CompletableFuture<Double> pointsFuture = this.databaseManager.getPointsAsync(player.getUniqueId());
            Double playerPoints = pointsFuture.get();
            if (playerPoints != null && bonus.isUnlocked(playerPoints)) {
               player.sendMessage(
                  ChatColor.translateAlternateColorCodes(
                     '&', "&x&0&5&F&B&0&0✔ &fБонус &x&0&5&F&B&0&0" + bonusName + " &fуровня &x&0&5&F&B&0&0" + level + " &fуже разблокирован!"
                  )
               );
            } else {
               player.sendMessage(
                  ChatColor.translateAlternateColorCodes(
                     '&',
                     "&x&F&F&2&2&2&2✘ &fБонус &x&F&F&2&2&2&2"
                        + bonusName
                        + " &fуровня &x&F&F&2&2&2&2"
                        + level
                        + " &fтребует &x&F&F&2&2&2&2"
                        + requiredPoints
                        + " &fочков"
                  )
               );
            }
         } catch (Exception var8) {
         }
      } catch (Exception var9) {
      }
   }
}
