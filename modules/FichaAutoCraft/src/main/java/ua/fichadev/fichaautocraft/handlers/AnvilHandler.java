package ua.fichadev.fichaautocraft.handlers;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import ua.fichadev.fichaautocraft.FichaAutoCraft;
import ua.fichadev.fichaautocraft.service.ItemService;

public class AnvilHandler implements Listener {
   private final FichaAutoCraft plugin;

   public AnvilHandler(FichaAutoCraft plugin) {
      this.plugin = plugin;
   }

   @EventHandler
   public void onPrepareAnvil(PrepareAnvilEvent e) {
      ItemStack result = e.getResult();
      if (result != null && result.getType() == Material.DISPENSER) {
         String rename = e.getInventory().getRenameText();
         if (rename != null) {
            String stripped = ChatColor.stripColor(rename);
            String target = this.plugin.getConfigManager().getRenameText();
            if (stripped.equalsIgnoreCase(target)) {
               ItemService.applyAutoCraftProperties(result, this.plugin);
               e.setResult(result);
            }
         }
      }
   }
}
