package ua.fichamine.fichaautomine.listeners;

import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class BreakListener implements Listener {
   public BreakListener() {
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onBreak(BlockBreakEvent event) {
      ItemStack itemStack = event.getPlayer().getInventory().getItemInMainHand();
      if (itemStack.getType() != Material.AIR && !event.isCancelled() && event.getBlock().getWorld().getName().equalsIgnoreCase("spawn")) {
         int cnt = 1;
         if (itemStack.hasItemMeta()) {
            int luck = itemStack.getEnchantmentLevel(Enchantment.FORTUNE);
            if (luck > 0) {
               cnt = new Random().nextInt(luck) + 2;
            }
         }

         boolean magnet = false;
         if (Bukkit.getPluginManager().getPlugin("HolyEnchantments") != null) {
            NamespacedKey key = new NamespacedKey(Bukkit.getPluginManager().getPlugin("HolyEnchantments"), "holyenchantments-magnet");
            if (itemStack.getItemMeta() != null && itemStack.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.INTEGER)) {
               magnet = true;
            }
         }

         Material blockType = event.getBlock().getType();
         if (blockType == Material.PURPLE_GLAZED_TERRACOTTA) {
            this.handleDrop(event, magnet, new ItemStack(Material.ENDER_PEARL, cnt));
         } else if (blockType == Material.PURPUR_BLOCK) {
            this.handleDrop(event, magnet, new ItemStack(Material.CHORUS_FRUIT, cnt));
         }
      }
   }

   private void handleDrop(BlockBreakEvent event, boolean magnet, ItemStack drop) {
      if (magnet) {
         this.addItemInventory(event.getPlayer().getInventory(), drop, event.getPlayer().getLocation());
      } else {
         event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), drop);
      }

      event.setDropItems(false);
   }

   private void addItemInventory(Inventory inventory, ItemStack itemStack, Location location) {
      for (ItemStack item : inventory.getStorageContents()) {
         if (item == null || item.getType() == Material.AIR) {
            inventory.addItem(new ItemStack[]{itemStack});
            return;
         }

         if (item.isSimilar(itemStack)) {
            int space = item.getMaxStackSize() - item.getAmount();
            if (space > 0) {
               if (itemStack.getAmount() <= space) {
                  item.setAmount(item.getAmount() + itemStack.getAmount());
                  return;
               }

               item.setAmount(item.getMaxStackSize());
               itemStack.setAmount(itemStack.getAmount() - space);
            }
         }
      }

      location.getWorld().dropItemNaturally(location, itemStack);
   }
}
