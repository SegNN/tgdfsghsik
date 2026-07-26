package ru.kotikstasika.holysfers.sferomant.listeners;

import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import ru.kotikstasika.holysfers.HolySfers;
import ru.kotikstasika.holysfers.sferomant.menu.Menu;

public class MenuClick implements Listener {
   public MenuClick() {
   }

   @EventHandler
   public void onInventoryClick(InventoryClickEvent event) {
      if (event.getInventory().getHolder() instanceof Menu.SferomantHolder) {
         event.setCancelled(true);
         if (event.getWhoClicked() instanceof Player) {
            Player player = (Player)event.getWhoClicked();
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem != null && clickedItem.getType() != Material.AIR && this.isSphereForSale(clickedItem)) {
               int requiredShards = this.getRequiredShards(clickedItem);
               if (requiredShards > 0) {
                  if (this.hasEnoughShards(player, requiredShards)) {
                     this.removeShards(player, requiredShards);
                     String slotKey = this.getSlotKey(event.getSlot());
                     if (slotKey != null) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "holysfers give " + player.getName() + " " + slotKey);
                     }
                  } else {
                     player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&x&f&b&0&0&0&0▶ &fУ вас недостаточно осколков сферы для покупки сферы."));
                  }
               }
            }
         }
      }
   }

   private boolean isSphereForSale(ItemStack item) {
      ItemMeta meta = item.getItemMeta();
      if (meta != null && meta.hasLore()) {
         List<String> lore = meta.getLore();
         if (lore == null) {
            return false;
         } else {
            for (String line : lore) {
               String strippedLine = ChatColor.stripColor(line);
               if (strippedLine.contains("Купить за") && strippedLine.contains("осколков")) {
                  return true;
               }
            }

            return false;
         }
      } else {
         return false;
      }
   }

   private int getRequiredShards(ItemStack item) {
      ItemMeta meta = item.getItemMeta();
      if (meta != null && meta.hasLore()) {
         List<String> lore = meta.getLore();
         if (lore == null) {
            return 0;
         } else {
            for (String line : lore) {
               String strippedLine = ChatColor.stripColor(line);
               if (strippedLine.contains("Купить за") && strippedLine.contains("осколков")) {
                  String[] parts = strippedLine.split(" ");

                  for (String part : parts) {
                     try {
                        return Integer.parseInt(part);
                     } catch (NumberFormatException var12) {
                     }
                  }
               }
            }

            return 0;
         }
      } else {
         return 0;
      }
   }

   private boolean hasEnoughShards(Player player, int required) {
      int count = 0;

      for (ItemStack item : player.getInventory().getContents()) {
         if (this.isShard(item)) {
            count += item.getAmount();
         }
      }

      return count >= required;
   }

   private void removeShards(Player player, int amount) {
      int remaining = amount;

      for (int i = 0; i < player.getInventory().getSize() && remaining > 0; i++) {
         ItemStack item = player.getInventory().getItem(i);
         if (this.isShard(item)) {
            if (item.getAmount() <= remaining) {
               remaining -= item.getAmount();
               player.getInventory().setItem(i, (ItemStack)null);
            } else {
               item.setAmount(item.getAmount() - remaining);
               remaining = 0;
            }
         }
      }
   }

   private String getSlotKey(int slot) {
      ConfigurationSection itemsSection = Menu.getConfig().getConfigurationSection("items");
      if (itemsSection != null) {
         for (String key : itemsSection.getKeys(false)) {
            ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);
            if (itemSection != null && itemSection.getInt("slot", -1) == slot) {
               return key;
            }
         }
      }

      return null;
   }

   private boolean isShard(ItemStack item) {
      if (item != null && item.getType() == Material.PLAYER_HEAD) {
         ItemMeta meta = item.getItemMeta();
         if (meta == null) {
            return false;
         } else {
            NamespacedKey key = new NamespacedKey(HolySfers.getInstance(), "oskolok");
            return meta.getPersistentDataContainer().has(key, PersistentDataType.INTEGER)
               && (Integer)meta.getPersistentDataContainer().get(key, PersistentDataType.INTEGER) == 1;
         }
      } else {
         return false;
      }
   }
}
