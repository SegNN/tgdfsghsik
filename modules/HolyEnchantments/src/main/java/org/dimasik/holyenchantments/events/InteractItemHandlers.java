package org.dimasik.holyenchantments.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.dimasik.holyenchantments.enchantscore.EnchantsMap;

public class InteractItemHandlers implements Listener {
   private final EnchantsMap enchantsMap;

   public InteractItemHandlers(EnchantsMap enchantsMap) {
      this.enchantsMap = enchantsMap;
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onDamageItem(PlayerInteractEvent event) {
      ItemStack itemStack = event.getItem();
      if (event.getItem() != null && itemStack.hasItemMeta()) {
         PersistentDataContainer dataContainer = itemStack.getItemMeta().getPersistentDataContainer();
         if (!dataContainer.isEmpty()) {
            Player player = event.getPlayer();
            if (dataContainer.has(this.enchantsMap.breakless.getNamespacedKey(), PersistentDataType.INTEGER)
               && event.getItem().getType().getMaxDurability() - event.getItem().getDurability() <= 10) {
               player.sendMessage("Побереги свой инструмент, он почти сломан");
               event.setCancelled(true);
            }
         }
      }
   }
}
